package com.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {
    private static final String PROP_FILE = "/config.properties";
    private static String url;
    private static String user;
    private static String password;

    static {
        try {
            Properties prop = new Properties();
            prop.load(DBUtil.class.getResourceAsStream(PROP_FILE));

            url = prop.getProperty("db.url");
            user = prop.getProperty("db.user");
            password = prop.getProperty("db.password");

            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to load DB configuration");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
