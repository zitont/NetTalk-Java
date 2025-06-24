package com.example.service;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class SocketService {
    private static final int PORT = 8888;
    private static final int DISCOVERY_PORT = 8889;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final Map<Long, Socket> onlineUsers = new ConcurrentHashMap<>();
    private final Map<Long, PrintWriter> userWriters = new ConcurrentHashMap<>();
    private final Map<Long, String> userNames = new ConcurrentHashMap<>();
    private ServerSocket serverSocket;
    private DatagramSocket discoverySocket;
    private boolean isRunning = false;
    
    // Start the server with automatic discovery service
    public void startServer(int port) {
        final int serverPort = port > 0 ? port : PORT;
        isRunning = true;
        
        // Start TCP server
        threadPool.submit(() -> {
            try {
                serverSocket = new ServerSocket(serverPort);
                System.out.println("Server started on port " + serverPort);
                
                // Start discovery service
                startDiscoveryService();
                
                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    Socket socket = serverSocket.accept();
                    handleClientConnection(socket);
                }
            } catch (IOException e) {
                if (!"socket closed".equals(e.getMessage())) {
                    System.err.println("Server error: " + e.getMessage());
                }
            } finally {
                shutdown();
            }
        });
    }
    
    // Start UDP discovery service to allow clients to find the server
    private void startDiscoveryService() {
        threadPool.submit(() -> {
            try {
                discoverySocket = new DatagramSocket(DISCOVERY_PORT);
                byte[] buffer = new byte[256];
                
                System.out.println("Discovery service started on port " + DISCOVERY_PORT);
                
                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    discoverySocket.receive(packet);
                    
                    String message = new String(packet.getData(), 0, packet.getLength());
                    if ("DISCOVER_SERVER".equals(message)) {
                        // Get server's IP address
                        String serverIP = InetAddress.getLocalHost().getHostAddress();
                        String response = serverIP + ":" + serverSocket.getLocalPort();
                        
                        // Send response back to client
                        byte[] responseData = response.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(
                            responseData, 
                            responseData.length, 
                            packet.getAddress(), 
                            packet.getPort()
                        );
                        discoverySocket.send(responsePacket);
                        
                        System.out.println("Discovery request from " + packet.getAddress() + 
                                          ", sent server info: " + response);
                    }
                }
            } catch (IOException e) {
                if (!Thread.currentThread().isInterrupted()) {
                    System.err.println("Discovery service error: " + e.getMessage());
                }
            } finally {
                if (discoverySocket != null && !discoverySocket.isClosed()) {
                    discoverySocket.close();
                }
            }
        });
    }
    
    private void handleClientConnection(Socket socket) {
        threadPool.submit(() -> {
            Long userId = null;
            String userName = null;
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                String idLine = in.readLine();
                if (idLine == null) throw new IOException("Client disconnected");

                userId = Long.parseLong(idLine);
                
                // 从数据库获取真实用户名
                // 这里需要添加数据库查询代码，暂时使用默认名称
                userName = getUserNameFromDatabase(userId);
                if (userName == null || userName.isEmpty()) {
                    userName = "User" + userId;
                }
                
                onlineUsers.put(userId, socket);
                userWriters.put(userId, out);
                
                // 保存用户名到映射中
                userNames.put(userId, userName);
                
                // 通知所有用户有新用户加入
                broadcastUserJoined(userId, userName);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("GET_USERS")) {
                        // 发送用户列表给请求的客户端
                        sendUserList(userId);
                    } else if (message.startsWith("PM:")) {
                        // 处理私聊消息
                        handlePrivateMessage(userId, message.substring(3));
                    } else {
                        broadcastMessage(userId, message);
                    }
                }

            } catch (Exception e) {
                System.err.println("Client error: " + e.getMessage());
            } finally {
                if (userId != null) {
                    onlineUsers.remove(userId);
                    userWriters.remove(userId);
                    userNames.remove(userId);
                    // 通知所有用户该用户已离开
                    broadcastUserLeft(userId, userName != null ? userName : "User" + userId);
                    System.out.println("User " + userId + " disconnected");
                }
                closeSocket(socket);
            }
        });
    }

    private void broadcastMessage(long senderId, String content) {
        // 使用真实用户名
        String senderName = userNames.getOrDefault(senderId, "User" + senderId);
        String formattedMessage = senderName + ": " + content;
        
        for (Map.Entry<Long, PrintWriter> entry : userWriters.entrySet()) {
            if (entry.getKey() != senderId) {
                sendMessage(entry.getKey(), formattedMessage);
            }
        }
    }

    public void sendMessage(long userId, String content) {
        PrintWriter writer = userWriters.get(userId);
        if (writer != null) {
            writer.println(content);
        }
    }

    private void closeSocket(Socket socket) {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        isRunning = false;
        try {
            // Close discovery socket
            if (discoverySocket != null && !discoverySocket.isClosed()) {
                discoverySocket.close();
            }
            
            // Close all client connections
            for (Socket socket : onlineUsers.values()) {
                closeSocket(socket);
            }
            onlineUsers.clear();
            userWriters.clear();

            // Close server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            threadPool.shutdownNow();
            System.out.println("Server shutdown complete");
        } catch (IOException e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }

    // 向特定用户发送在线用户列表
    private void sendUserList(long requestingUserId) {
        PrintWriter writer = userWriters.get(requestingUserId);
        if (writer != null) {
            StringBuilder userListStr = new StringBuilder("USER_LIST:");
            boolean first = true;
            
            for (Long userId : onlineUsers.keySet()) {
                if (!first) {
                    userListStr.append(",");
                }
                first = false;
                
                // 使用真实用户名
                String userName = userNames.getOrDefault(userId, "User" + userId);
                userListStr.append(userId).append(":").append(userName);
            }
            
            writer.println(userListStr.toString());
        }
    }

    // Broadcast to all users that a new user has joined
    private void broadcastUserJoined(long userId, String userName) {
        String joinMessage = "USER_JOINED:" + userId + ":" + userName;
        for (Map.Entry<Long, PrintWriter> entry : userWriters.entrySet()) {
            if (entry.getKey() != userId) { // Don't send to the user who joined
                entry.getValue().println(joinMessage);
            }
        }
    }

    // Broadcast to all users that a user has left
    private void broadcastUserLeft(long userId, String userName) {
        String leftMessage = "USER_LEFT:" + userId + ":" + userName;
        for (PrintWriter writer : userWriters.values()) {
            writer.println(leftMessage);
        }
    }

    // 处理私聊消息
    private void handlePrivateMessage(long senderId, String message) {
        // 私聊消息格式: 接收者ID:消息内容
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            long receiverId = Long.parseLong(parts[0]);
            String content = parts[1];
            
            // 向接收者发送私聊消息
            PrintWriter receiverWriter = userWriters.get(receiverId);
            if (receiverWriter != null) {
                // 发送格式: PM:发送者ID:消息内容
                receiverWriter.println("PM:" + senderId + ":" + content);
                System.out.println("Private message from " + senderId + " to " + receiverId + ": " + content);
            }
        }
    }

    // 从数据库获取用户名的方法
    private String getUserNameFromDatabase(Long userId) {
        // 这里应该添加数据库查询代码
        // 暂时返回null，使用默认名称
        return null;
    }
}
