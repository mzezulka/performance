package com.hp.mwtests.ts.arjuna.performance.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.sql.XAConnection;
import org.h2.jdbcx.JdbcDataSource;

public class H2XAConnectionUtil {

    private static final String testTableName = "PERF_" + charTimestamp();
    private static final String driverClass = "org.h2.Driver";
    protected ConnectionData data;
    private static boolean testTableExists = false; 

    public H2XAConnectionUtil() {
        ConnectionData.Builder dataBuilder = new ConnectionData.Builder().user("sa").pass("sa").db("test")
                .host("localhost").port("9092");
        data = dataBuilder.build();
    }

    private static String charTimestamp() {
        int diff = 'A' - '0';
        String stamp = String.valueOf(new Timestamp(System.currentTimeMillis()).getTime());
        StringBuilder sb = new StringBuilder();
        for(char s : stamp.toCharArray()) {
            sb.append(s + diff);
        }
        return sb.toString();
    }
    
    private synchronized void classloadDriver() {
        try {
            if (!isClassLoaded(driverClass)) {
                Class.forName(driverClass);
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Can't get connection of " + data.url() + " [" + data.user() + "," + data.pass() + "]", e);
        }
    }

    public XAConnection getXAConnection() {
        try {
            // Create the XA data source and XA ready connection.
            JdbcDataSource ds = new JdbcDataSource();
            ds.setUser(data.user());
            ds.setPassword(data.pass());
            ds.setUrl(data.url());
            return ds.getXAConnection();
        } catch (Exception e) {
            throw new RuntimeException(getCreateXAConnectionErrorString(data), e);
        }
    }

    public void setConnectionData(ConnectionData data) {
        this.data = data;
    }

    public ConnectionData getConnectionData() {
        return this.data;
    }

    public Connection getConnection() {
        try {
            classloadDriver();
            return DriverManager.getConnection(data.url(), data.user(), data.pass());
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Can't get connection of " + data.url() + " [" + data.user() + "," + data.pass() + "]", e);
        }
    }

    public boolean isDriverClassLoaded() throws Exception {
        return isClassLoaded(driverClass);
    }

    public void createTestTableIfNecessary() {
        if(testTableExists) return;
        try (Connection con = getConnection()) {
            Statement stmt = con.createStatement();

            try {
                stmt.executeUpdate("DROP TABLE " + testTableName);
            } catch (Exception e) {
                // when table does not exist we ignore failure from DROP
            }
            stmt.executeUpdate("CREATE TABLE " + testTableName + " (f1 int, f2 " + getVarCharTypeSpec() + ")");
            testTableExists = true;
        } catch (Exception e) {
            String msgerr = String.format("Can't create table %s", testTableName);
            throw new RuntimeException(msgerr, e);
        }
    }

    public String getVarCharTypeSpec() {
        return "varchar";
    }

    public ResultSet selectTestTable(Connection con) {
        try {
            // Open a new connection and read back the record to verify that it
            // worked.
            return con.createStatement().executeQuery("SELECT * FROM " + testTableName);
        } catch (Exception e) {
            String msgerr = String.format("Can't do select of table %s on connection %s", testTableName, con);
            throw new RuntimeException(msgerr, e);
        }
    }

    public PreparedStatement prepareInsertQuery(Connection con, int index, String value) {
        try {
            PreparedStatement pstmt = con.prepareStatement("INSERT INTO " + testTableName + " (f1,f2) VALUES (?, ?)");
            pstmt.setInt(1, index);
            pstmt.setString(2, value);
            return pstmt;
        } catch (Exception e) {
            String msgerr = String.format("Can't create prepared statement for values '%s', '%s' on connection %s",
                    index, value, con);
            throw new RuntimeException(msgerr, e);
        }
    }

    protected String getCreateXAConnectionErrorString(ConnectionData connectionData) {
        return String.format("Can't create XA connection to: %s:%s %s/%s/%s", connectionData.host(),
                connectionData.port(), connectionData.db(), connectionData.user(), connectionData.pass());
    }

    private boolean isClassLoaded(String className) throws Exception {
        java.lang.reflect.Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass",
                new Class[] { String.class });
        m.setAccessible(true);
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        return m.invoke(cl, className) != null;
    }
}
