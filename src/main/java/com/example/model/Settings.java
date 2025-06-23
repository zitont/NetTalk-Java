package com.example.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Settings {
    private static Settings instance;
    private String serverHost = "localhost";
    private int serverPort = 8888;
    private boolean startServerMode = false;
    private Properties properties = new Properties();
    private static final String CONFIG_FILE_PATH = "src/main/resources/config.properties";

    private Settings() {
        loadProperties();
    }

    public static synchronized Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    private void loadProperties() {
        try {
            properties.load(new FileInputStream(CONFIG_FILE_PATH));
            
            // 从配置文件加载设置
            serverHost = properties.getProperty("server.host", "localhost");
            serverPort = Integer.parseInt(properties.getProperty("server.port", "8888"));
            startServerMode = Boolean.parseBoolean(properties.getProperty("server.start", "false"));
            
        } catch (IOException e) {
            System.err.println("无法加载配置文件，使用默认设置: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("配置文件中的端口号格式错误，使用默认端口: " + e.getMessage());
        }
    }

    public void saveSettings() {
        try {
            // 更新属性
            properties.setProperty("server.host", serverHost);
            properties.setProperty("server.port", String.valueOf(serverPort));
            properties.setProperty("server.start", String.valueOf(startServerMode));
            
            // 保存到文件
            try (FileOutputStream out = new FileOutputStream(CONFIG_FILE_PATH)) {
                properties.store(out, "Updated settings");
                System.out.println("设置已保存到配置文件");
            }
        } catch (IOException e) {
            System.err.println("保存设置时出错: " + e.getMessage());
        }
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public boolean isStartServerMode() {
        return startServerMode;
    }

    public void setStartServerMode(boolean startServerMode) {
        this.startServerMode = startServerMode;
    }
}