package com.example.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {
    private static final String PROP_FILE_NAME = "config.properties";
    private static final String PROP_FILE_PATH = "/config.properties";
    private static String url;
    private static String user;
    private static String password;

    static {
        try {
            Properties prop = new Properties();
            boolean loaded = false;
            
            // 首先尝试从当前目录加载
            File configFile = new File(PROP_FILE_NAME);
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    prop.load(fis);
                    loaded = true;
                } catch (Exception e) {
                    System.err.println("无法从当前目录加载配置: " + e.getMessage());
                }
            }
            
            // 如果从当前目录加载失败，尝试从类路径加载
            if (!loaded) {
                try (InputStream is = DBUtil.class.getResourceAsStream(PROP_FILE_PATH)) {
                    if (is != null) {
                        prop.load(is);
                        loaded = true;
                    }
                } catch (Exception e) {
                    System.err.println("无法从类路径加载配置: " + e.getMessage());
                }
            }
            
            if (!loaded) {
                throw new RuntimeException("无法加载数据库配置");
            }

            url = prop.getProperty("db.url");
            user = prop.getProperty("db.user");
            password = prop.getProperty("db.password");

            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("数据库配置已加载: " + url);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("Failed to load DB configuration: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}