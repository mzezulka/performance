package com.hp.mwtests.ts.arjuna.performance.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.sql.XAConnection;
import org.h2.jdbcx.JdbcDataSource;

public class H2XAConnectionUtil {

    private static String TEST_TABLE_NAME = "PERF_" + timestamp();
    private static final String DRIVER_CLASS = "org.h2.Driver";
    private static ConnectionData data = new ConnectionData.Builder().user("sa").pass("sa").db("test")
            .host("localhost").port("9092").build();
    private static boolean testTableExists = false;

    private static String timestamp() {
        return String.valueOf(new Timestamp(System.currentTimeMillis()).getTime());
    }
    
    private static synchronized void classloadDriver() {
        try {
            if (!isClassLoaded(DRIVER_CLASS)) {
                Class.forName(DRIVER_CLASS);
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Can't get connection of " + data.url() + " [" + data.user() + "," + data.pass() + "]", e);
        }
    }

    public static XAConnection getXAConnection() {
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

    public static Connection getConnection() {
        try {
            classloadDriver();
            return DriverManager.getConnection(data.url(), data.user(), data.pass());
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Can't get connection of " + data.url() + " [" + data.user() + "," + data.pass() + "]", e);
        }
    }

    public static boolean isDriverClassLoaded() throws Exception {
        return isClassLoaded(DRIVER_CLASS);
    }

    public synchronized static void createTestTableIfNecessary() {
        if(testTableExists) return;
        try (Connection con = getConnection()) {
            Statement stmt = con.createStatement();

            try {
                stmt.executeUpdate("DROP TABLE " + TEST_TABLE_NAME);
            } catch (Exception e) {
                // when table does not exist we ignore failure from DROP
            }
            stmt.executeUpdate("CREATE TABLE " + TEST_TABLE_NAME + " (f1 int, f2 " + getVarCharTypeSpec() + ")");
            testTableExists = true;
        } catch (Exception e) {
            // let's be cheeky and create another table instead
            try (Connection conn = getConnection()) {
                TEST_TABLE_NAME = "PERF_" + timestamp();
                conn.createStatement().executeUpdate("CREATE TABLE " + TEST_TABLE_NAME + " (f1 int, f2 " + getVarCharTypeSpec() + ")");
                testTableExists = true;
            } catch (SQLException sqle) {
                // we can't be bothered trying at this point, really
                throw new RuntimeException("Can't create table.", sqle);
            }
        }
    }

    public static String getVarCharTypeSpec() {
        return "varchar";
    }

    public static PreparedStatement prepareInsertQuery(Connection con, int index, String value) {
        try {
            PreparedStatement pstmt = con.prepareStatement("INSERT INTO " + TEST_TABLE_NAME + " (f1,f2) VALUES (?, ?)");
            pstmt.setInt(1, index);
            pstmt.setString(2, value);
            return pstmt;
        } catch (Exception e) {
            String msgerr = String.format("Can't create prepared statement for values '%s', '%s' on connection %s",
                    index, value, con);
            throw new RuntimeException(msgerr, e);
        }
    }

    protected static String getCreateXAConnectionErrorString(ConnectionData connectionData) {
        return String.format("Can't create XA connection to: %s:%s %s/%s/%s", connectionData.host(),
                connectionData.port(), connectionData.db(), connectionData.user(), connectionData.pass());
    }

    private static boolean isClassLoaded(String className) throws Exception {
        java.lang.reflect.Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass",
                new Class[] { String.class });
        m.setAccessible(true);
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        return m.invoke(cl, className) != null;
    }
}
