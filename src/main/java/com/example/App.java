package com.example;

import com.example.service.SocketService;
import com.example.view.LoginView;

import javax.swing.*;

public class App {
    private static final SocketService socketService = new SocketService();

    public static void main(String[] args) {
        // 启动Socket服务器
        socketService.startServer();

        /* 添加关闭钩子 */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            socketService.shutdown();
        }));

        // 启动GUI
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }
}
