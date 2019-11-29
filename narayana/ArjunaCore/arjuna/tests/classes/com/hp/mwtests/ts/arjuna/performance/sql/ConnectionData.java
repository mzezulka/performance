package com.hp.mwtests.ts.arjuna.performance.sql;

public class ConnectionData implements Cloneable {
    public static String HOST_PARAM = "host";
    public static String PORT_PARAM = "port";
    public static String URL_PARAM = "url";
    public static String DATABASE_PARAM = "database";
    public static String USER_PARAM = "user";
    public static String PASSWORD_PARAM = "password";
    public static String DBTYPE_PARAM = "dbtype";
    private final String url, user, pass, db, host, port;

    /**
     * Use method {@link #url()} to instantiate this class.
     */
    private ConnectionData(String connectionUrl, Builder builder) {
        this.url = connectionUrl;
        this.user = builder.user;
        this.pass = builder.pass;
        this.db = builder.db;
        this.host = builder.host;
        this.port = builder.port;
    }

    public String url() {
        return url;
    }

    public String user() {
        return user;
    }

    public String pass() {
        return pass;
    }

    public String db() {
        return db;
    }

    public String host() {
        return host;
    }

    public String port() {
        return port;
    }

    public int portAsInt() {
        return Integer.parseInt(port);
    }

    public String toString() {
        return String.format("type: '%s', connection props: %s:%s %s/%s", url, host,
                port, user, pass);
    }

    public static class Builder {
        private String host = System.getProperty(ConnectionData.HOST_PARAM);
        private String port = System.getProperty(ConnectionData.PORT_PARAM);
        private String db = System.getProperty(ConnectionData.DATABASE_PARAM, "crashrec");
        private String user = System.getProperty(ConnectionData.USER_PARAM, "crashrec");
        private String pass = System.getProperty(ConnectionData.PASSWORD_PARAM, "crashrec");

        public Builder() {
            this.host = System.getProperty(ConnectionData.HOST_PARAM);
            this.port = System.getProperty(ConnectionData.PORT_PARAM);
        }

        /**
         * If system properties for host and port is not defined then default params are
         * used.
         */
        public Builder(String defaultHost, String defaultPort) {
            this.host = defaultHost;
            this.port = defaultPort;
        }

        public Builder user(String userName) {
            this.user = userName;
            return this;
        }

        public Builder pass(String password) {
            this.pass = password;
            return this;
        }

        public Builder db(String databaseName) {
            this.db = databaseName;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }
        
        public Builder port(String port) {
            this.port = port;
            return this;
        }
        
        public ConnectionData build() {
            String connUrl = "jdbc:h2:~" + "/" + db;
            return new ConnectionData(connUrl, this);
        }
    }
}
