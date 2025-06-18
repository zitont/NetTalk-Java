package com.example.service;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class SocketService {
    private static final int PORT = 8888;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final Map<Long, Socket> onlineUsers = new ConcurrentHashMap<>();
    private final Map<Long, PrintWriter> userWriters = new ConcurrentHashMap<>();
    private ServerSocket serverSocket;

    public void startServer() {
        threadPool.submit(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                System.out.println("Server started on port " + PORT);
                while (!Thread.currentThread().isInterrupted()) {
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
                // Get user name from database or use default
                userName = "User" + userId; // You can replace this with actual user name lookup
                
                onlineUsers.put(userId, socket);
                userWriters.put(userId, out);
                
                // Notify all users about the new user
                broadcastUserJoined(userId, userName);

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("GET_USERS")) {
                        // Send user list to the requesting client
                        sendUserList(userId);
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
                    // Notify all users that this user has left
                    broadcastUserLeft(userId, userName != null ? userName : "User" + userId);
                    System.out.println("User " + userId + " disconnected");
                }
                closeSocket(socket);
            }
        });
    }

    private void broadcastMessage(long senderId, String content) {
        String formattedMessage = "User" + senderId + ": " + content;
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
        try {
            for (Socket socket : onlineUsers.values()) {
                closeSocket(socket);
            }
            onlineUsers.clear();
            userWriters.clear();

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdownNow();
            System.out.println("Server shutdown complete");
        } catch (IOException e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }

    // Send the list of online users to a specific user
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
                userListStr.append(userId).append(":").append("User").append(userId);
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
}
