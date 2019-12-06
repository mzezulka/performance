package com.hp.mwtests.ts.arjuna.performance.sql;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

public class JdbcXAResourceProvider {
  
    private final H2XAConnectionUtil util;
    private XAConnection xaConn;
    private Connection conn;

    public JdbcXAResourceProvider() {
        util = new H2XAConnectionUtil();
    }
    
    public void createTestTable() {
        util.createTestTableIfNecessary();
    }

    public void executeStatement() {
        try {
            util.prepareInsertQuery(conn, 1, "hello perf test").executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public XAResource getJdbcResource() {
        if(xaConn == null) {
            init();
        }
        try {
            return xaConn.getXAResource();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void init() {
        xaConn = util.getXAConnection();
        try {
            conn = xaConn.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            conn.close();
            xaConn.close();
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        }
    }
}
