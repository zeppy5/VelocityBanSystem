package de.zeppy5.bansystem.util;

import java.sql.*;

public class MySQL {

    private final String HOST;

    private final String DATABASE;

    private final String USER;

    private final String PASSWORD;

    private final int PORT;

    private Connection connection;

    public MySQL(String host, String database, String user, String password, int port) {
        this.HOST = host;
        this.DATABASE = database;
        this.USER = user;
        this.PASSWORD = password;
        this.PORT = port;

        connect();
        setup();
    }

    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" +
                    HOST + ":" +
                    PORT + "/" +
                    DATABASE + "?autoReconnect=true";
            connection = DriverManager.getConnection(url, USER, PASSWORD);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not connect to Database. Is the config configured correctly?");
        }
    }

    public void setup() {
        try {
            PreparedStatement st = connection.prepareStatement("CREATE TABLE IF NOT EXISTS bans(" +
                    "UUID VARCHAR(100)," +
                    " ID VARCHAR(100)," +
                    " DATE BIGINT," +
                    " LENGTH BIGINT," +
                    " EXPIRES BIGINT," +
                    " REASON VARCHAR(200)," +
                    " STATUS INT," +
                    " BANNED_BY VARCHAR(100))");
            st.executeUpdate();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
