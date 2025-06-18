package com.example;

import com.example.controller.AuthController;

public class DBTest {
    public static void main(String[] args) {
        AuthController authController = new AuthController();

        // 模拟用户输入的用户名和密码
        String username = "root";
        String password = "123";

        // 调用登录方法
        boolean loginSuccess = authController.login(username, password);

        // 输出结果
        if (loginSuccess) {
            System.out.println("✅ 登录成功！");
        } else {
            System.out.println("❌ 登录失败，请检查用户名或密码");
        }
    }
}