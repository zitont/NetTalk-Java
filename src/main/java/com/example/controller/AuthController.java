package com.example.controller;

import com.example.dao.UserDAO;
import com.example.model.User;

public class AuthController {
    private final UserDAO userDAO;
    private User currentUser;

    public AuthController() {
        this.userDAO = new UserDAO();
    }

    public boolean login(String username, String password) {
        User user = userDAO.getUserByName(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public boolean register(String username, String password) {
        // 检查用户名是否已存在
        if (userDAO.getUserByName(username) != null) {
            return false;
        }

        User newUser = new User();
        newUser.setName(username);
        newUser.setPassword(password);
        return userDAO.addUser(newUser);
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
