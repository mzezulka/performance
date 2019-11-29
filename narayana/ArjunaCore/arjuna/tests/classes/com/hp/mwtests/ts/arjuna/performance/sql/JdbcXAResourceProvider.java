package com.hp.mwtests.ts.arjuna.performance.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class JdbcXAResourceProvider {

    private static final JdbcXAResourceProvider INSTANCE = new JdbcXAResourceProvider();
    private final H2XAConnectionUtil util;
    private XAConnection xaConn;
    private Connection conn;

    public static JdbcXAResourceProvider getInstance() {
        return INSTANCE;
    }

    private JdbcXAResourceProvider() {
        util = new H2XAConnectionUtil();
        util.createTestTable();
    }

    public void executeStatement() {
        try {
            util.prepareInsertQuery(conn, 1, "hello perf test").executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public XAResource getJdbcResource() {
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
            throw new RuntimeException("<init>: Cannot create a singleton instance", e);
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
