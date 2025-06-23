package com.example.controller;

import com.example.dao.UserDAO;
import com.example.model.User;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class AuthController {
    private final UserDAO userDAO;
    private User currentUser;
    // 添加用户缓存
    private static Map<Long, User> userCache = new HashMap<>();
    // 添加在线用户集合
    private static Map<Long, Boolean> onlineUsers = new HashMap<>();

    public AuthController() {
        this.userDAO = new UserDAO();
        // 初始化时加载所有用户到缓存
        loadAllUsersToCache();
    }

    // 加载所有用户到缓存
    private void loadAllUsersToCache() {
        List<User> allUsers = userDAO.getAllUsers();
        for (User user : allUsers) {
            userCache.put(user.getId(), user);
        }
        System.out.println("已加载 " + userCache.size() + " 个用户到缓存");
    }

    public boolean login(String username, String password) {
        User user = userDAO.getUserByName(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            // 添加到缓存
            userCache.put(user.getId(), user);
            // 标记为在线
            onlineUsers.put(user.getId(), true);
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
        boolean success = userDAO.addUser(newUser);
        
        // 如果注册成功，添加到缓存
        if (success && newUser.getId() > 0) {
            userCache.put(newUser.getId(), newUser);
        }
        
        return success;
    }

    public User getCurrentUser() {
        return currentUser;
    }
    
    // 获取缓存中的用户
    public static User getUserFromCache(long userId) {
        return userCache.get(userId);
    }
    
    // 检查用户是否在线
    public static boolean isUserOnline(long userId) {
        return onlineUsers.getOrDefault(userId, false);
    }
    
    // 设置用户在线状态
    public static void setUserOnlineStatus(long userId, boolean isOnline) {
        onlineUsers.put(userId, isOnline);
    }
    
    // 获取所有缓存的用户
    public static Map<Long, User> getAllCachedUsers() {
        return userCache;
    }
}
