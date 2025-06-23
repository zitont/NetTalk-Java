package com.example.component;

import com.example.model.Settings;
import java.io.*;
import java.net.Socket;

/**
 * 聊天客户端组件，负责与服务器的通信
 */
public class ChatClient {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listeningThread;
    private MessageListener messageListener;
    private ConnectionStatusListener statusListener;
    private Long userId;
    private boolean connected = false;

    /**
     * 创建聊天客户端
     * @param userId 用户ID
     */
    public ChatClient(Long userId) {
        this.userId = userId;
    }

    /**
     * 连接到服务器
     * @param host 服务器主机名
     * @param port 服务器端口
     * @return 是否连接成功
     */
    public boolean connect(String host, int port) {
        try {
            clientSocket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            
            // 发送用户ID
            out.println(userId);
            
            // 开始监听消息
            startListening();
            
            connected = true;
            if (statusListener != null) {
                statusListener.onConnectionStatusChanged(true);
            }
            return true;
        } catch (IOException e) {
            System.err.println("连接服务器失败: " + e.getMessage());
            if (statusListener != null) {
                statusListener.onConnectionStatusChanged(false);
            }
            return false;
        }
    }

    /**
     * 发送消息
     * @param message 消息内容
     * @return 是否发送成功
     */
    public boolean sendMessage(String message) {
        if (out != null && connected) {
            out.println(message);
            return true;
        }
        return false;
    }

    /**
     * 关闭连接
     */
    public void disconnect() {
        connected = false;
        
        try {
            if (listeningThread != null) {
                listeningThread.interrupt();
                listeningThread = null;
            }
            
            if (out != null) {
                out.close();
                out = null;
            }
            
            if (in != null) {
                in.close();
                in = null;
            }
            
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                clientSocket = null;
            }
            
            if (statusListener != null) {
                statusListener.onConnectionStatusChanged(false);
            }
        } catch (IOException e) {
            System.err.println("关闭连接时出错: " + e.getMessage());
        }
    }

    /**
     * 开始监听服务器消息
     */
    private void startListening() {
        listeningThread = new Thread(() -> {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    if (messageListener != null) {
                        messageListener.onMessageReceived(message);
                    }
                }
            } catch (IOException e) {
                if (!Thread.currentThread().isInterrupted() && connected) {
                    System.err.println("接收消息时出错: " + e.getMessage());
                    if (statusListener != null) {
                        statusListener.onConnectionStatusChanged(false);
                    }
                }
            }
        });
        listeningThread.setDaemon(true);
        listeningThread.start();
    }

    /**
     * 设置消息监听器
     * @param listener 消息监听器
     */
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    /**
     * 设置连接状态监听器
     * @param listener 连接状态监听器
     */
    public void setConnectionStatusListener(ConnectionStatusListener listener) {
        this.statusListener = listener;
    }

    /**
     * 是否已连接
     * @return 连接状态
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * 消息监听器接口
     */
    public interface MessageListener {
        void onMessageReceived(String message);
    }

    /**
     * 连接状态监听器接口
     */
    public interface ConnectionStatusListener {
        void onConnectionStatusChanged(boolean connected);
    }
}