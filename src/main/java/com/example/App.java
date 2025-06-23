package com.example;

import com.example.model.Settings;
import com.example.service.SocketService;
import com.example.view.LoginView;

import javax.swing.*;

public class App {
    private static final SocketService socketService = new SocketService();

    public static void main(String[] args) {
        // 加载设置
        Settings settings = Settings.getInstance();
        
        // 根据设置决定是否启动服务器
        if (settings.isStartServerMode()) {
            // 启动Socket服务器
            socketService.startServer(settings.getServerPort());
            
            /* 添加关闭钩子 */
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                socketService.shutdown();
            }));
        }

        // 启动GUI
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }
}
