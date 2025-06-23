package com.example.model;

import java.io.*;
import java.util.Properties;

public class Settings {
    private static Settings instance;
    private String serverHost = "localhost";
    private int serverPort = 8888;
    private boolean startServerMode = false;
    private Properties properties = new Properties();
    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final String CONFIG_FILE_PATH = "src/main/resources/" + CONFIG_FILE_NAME;

    private Settings() {
        loadProperties();
    }

    public static synchronized Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    // 重新加载配置
    public void reloadSettings() {
        loadProperties();
    }

    private void loadProperties() {
        try {
            // 首先尝试从当前目录加载
            File configFile = new File(CONFIG_FILE_NAME);
            
            // 如果当前目录没有配置文件，尝试从资源目录加载
            if (!configFile.exists()) {
                configFile = new File(CONFIG_FILE_PATH);
            }
            
            // 如果文件存在，加载它
            if (configFile.exists()) {
                try (FileInputStream in = new FileInputStream(configFile)) {
                    properties.load(in);
                }
            } else {
                // 如果文件不存在，尝试从类路径加载
                InputStream in = getClass().getResourceAsStream("/" + CONFIG_FILE_NAME);
                if (in != null) {
                    properties.load(in);
                    in.close();
                } else {
                    System.err.println("无法找到配置文件，使用默认设置");
                }
            }
            
            // 从配置文件加载设置
            serverHost = properties.getProperty("server.host", "localhost");
            serverPort = Integer.parseInt(properties.getProperty("server.port", "8888"));
            startServerMode = Boolean.parseBoolean(properties.getProperty("server.start", "false"));
            
            System.out.println("已加载配置: 服务器=" + serverHost + ":" + serverPort);
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
            
            // 首先尝试保存到当前目录
            File configFile = new File(CONFIG_FILE_NAME);
            
            // 如果当前目录不可写，尝试保存到资源目录
            if (!configFile.canWrite() && !configFile.exists()) {
                configFile = new File(CONFIG_FILE_PATH);
            }
            
            // 保存到文件
            try (FileOutputStream out = new FileOutputStream(configFile)) {
                properties.store(out, "Updated settings");
                System.out.println("设置已保存到配置文件: " + configFile.getAbsolutePath());
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

    /**
     * 获取属性值
     * @param key 属性键
     * @param defaultValue 默认值
     * @return 属性值，如果不存在则返回默认值
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * 设置属性值
     * @param key 属性键
     * @param value 属性值
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}
