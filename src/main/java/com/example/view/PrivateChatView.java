package com.example.view;

import com.example.component.ChatPanel;
import com.example.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.List;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class PrivateChatView extends JFrame {
    // 颜色方案 - 与MainView保持一致
    private static final Color PRIMARY_COLOR = new Color(64, 123, 255);
    private static final Color PRIMARY_HOVER = new Color(45, 106, 255);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color CHAT_BACKGROUND = Color.WHITE;
    private static final Color SENT_BUBBLE_COLOR = new Color(64, 123, 255);
    private static final Color RECEIVED_BUBBLE_COLOR = new Color(240, 242, 245);
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color SECONDARY_TEXT = new Color(108, 117, 125);
    private static final Color BORDER_COLOR = new Color(222, 226, 230);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Font CHINESE_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    private static final Font CHINESE_FONT_BOLD = new Font("微软雅黑", Font.BOLD, 14);
    
    // 成员变量
    private User currentUser;
    private User targetUser;
    private Socket clientSocket;
    private PrintWriter out;
    private JTextArea inputField;
    private JButton sendButton;
    private ChatPanel chatPanel;
    
    public PrivateChatView(User currentUser, User targetUser, Socket clientSocket) {
        this.currentUser = currentUser;
        this.targetUser = targetUser;
        this.clientSocket = clientSocket;
        
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        initUI();
    }
    
    private void initUI() {
        setTitle("与 " + targetUser.getName() + " 的私聊");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // 初始化标题栏
        initTitleBar();
        
        // 初始化聊天面板
        initChatPanel();
        
        // 初始化输入面板
        initInputPanel();
        
        // 确保输入框可用
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }
    
    private void initTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(CHAT_BACKGROUND);
        titleBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // 用户信息面板
        JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        userInfo.setBackground(CHAT_BACKGROUND);
        
        // 用户头像
        JLabel avatar = createUserAvatar(targetUser.getName());
        userInfo.add(avatar);
        userInfo.add(Box.createHorizontalStrut(12));
        
        // 用户名和状态
        JPanel nameStatusPanel = new JPanel();
        nameStatusPanel.setLayout(new BoxLayout(nameStatusPanel, BoxLayout.Y_AXIS));
        nameStatusPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(targetUser.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        nameLabel.setForeground(TEXT_COLOR);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        statusPanel.setOpaque(false);
        
        JLabel statusDot = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SUCCESS_COLOR);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        statusDot.setPreferredSize(new Dimension(8, 8));
        statusDot.setOpaque(false);
        
        JLabel statusLabel = new JLabel("在线");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(SECONDARY_TEXT);
        
        statusPanel.add(statusDot);
        statusPanel.add(statusLabel);
        
        nameStatusPanel.add(nameLabel);
        nameStatusPanel.add(statusPanel);
        
        userInfo.add(nameStatusPanel);
        
        titleBar.add(userInfo, BorderLayout.WEST);
        add(titleBar, BorderLayout.NORTH);
    }
    
    private JLabel createUserAvatar(String name) {
        String initials = name.substring(0, 1).toUpperCase();
        JLabel avatar = new JLabel(initials, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制方形背景，带圆角
                g2.setColor(PRIMARY_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                super.paintComponent(g);
                g2.dispose();
            }
        };
        
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setFont(new Font("微软雅黑", Font.BOLD, 14));
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(false);
        
        return avatar;
    }
    
    private void initChatPanel() {
        chatPanel = new ChatPanel(
            CHAT_BACKGROUND,
            SENT_BUBBLE_COLOR,
            RECEIVED_BUBBLE_COLOR,
            TEXT_COLOR,
            SECONDARY_TEXT,
            CHINESE_FONT
        );
        
        JScrollPane scrollPane = chatPanel.getScrollPane();
        add(scrollPane, BorderLayout.CENTER);
        
        // 设置转发消息监听器
        chatPanel.setForwardMessageListener((content, isOwnMessage) -> {
            // 获取可转发的用户列表
            List<User> forwardUsers = getForwardableUsers();
            
            // 显示转发对话框
            chatPanel.showForwardDialog(content, forwardUsers, (messageContent, targetUser) -> {
                // 执行转发操作
                forwardMessage(messageContent, targetUser);
            });
        });
    }
    
    private void initInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));
        inputPanel.setBackground(CHAT_BACKGROUND);
        
        // 创建输入框容器
        JPanel inputWrapper = new JPanel(new BorderLayout());
        inputWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        inputWrapper.setBackground(BACKGROUND_COLOR);
        
        // 多行输入框
        inputField = new JTextArea(3, 20);
        inputField.setFont(CHINESE_FONT);
        inputField.setLineWrap(true);
        inputField.setWrapStyleWord(true);
        inputField.setBackground(BACKGROUND_COLOR);
        inputField.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        
        JScrollPane inputScrollPane = new JScrollPane(inputField);
        inputScrollPane.setBorder(BorderFactory.createEmptyBorder());
        inputScrollPane.setOpaque(false);
        inputScrollPane.getViewport().setOpaque(false);
        
        inputWrapper.add(inputScrollPane, BorderLayout.CENTER);
        
        // 发送按钮
        sendButton = createSendButton();
        inputField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSendButton();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSendButton();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSendButton();
            }
            
            private void updateSendButton() {
                sendButton.setEnabled(!inputField.getText().trim().isEmpty());
            }
        });
        
        // 回车键处理
        inputField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    if (evt.isShiftDown()) {
                        // Shift+Enter 插入换行符
                        inputField.append("\n");
                    } else {
                        // 仅Enter键发送消息
                        evt.consume();
                        if (!inputField.getText().trim().isEmpty()) {
                            sendPrivateMessage();
                        }
                    }
                }
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(sendButton);
        
        inputPanel.add(inputWrapper, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(inputPanel, BorderLayout.SOUTH);
    }
    
    private JButton createSendButton() {
        JButton button = new JButton("发送") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bgColor = isEnabled() ? 
                    (getModel().isPressed() ? PRIMARY_HOVER : PRIMARY_COLOR) : 
                    SECONDARY_TEXT;
                
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                super.paintComponent(g);
                g2.dispose();
            }
        };
        
        button.setFont(CHINESE_FONT_BOLD);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setEnabled(false);
        button.addActionListener(e -> sendPrivateMessage());
        
        return button;
    }
    
    private void sendPrivateMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty() && out != null) {
            // 发送私聊消息格式: PM:接收者ID:消息内容
            String pmCommand = "PM:" + targetUser.getId() + ":" + message;
            out.println(pmCommand);
            
            // 在自己的聊天窗口显示消息，传递当前用户名
            chatPanel.addMessage(message, true, currentUser.getName());
            
            // 重置输入框
            inputField.setText("");
            sendButton.setEnabled(false);
            SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
        }
    }
    
    /**
     * 接收私聊消息
     * @param message 消息内容
     */
    public void receiveMessage(String message) {
        // 在聊天面板显示接收到的消息，传递目标用户名
        chatPanel.addMessage(message, false, targetUser.getName());
    }

    /**
     * 接收离线消息
     * @param message 消息内容
     */
    public void receiveOfflineMessage(String message) {
        // 在聊天面板显示接收到的离线消息，添加特殊标记
        chatPanel.addMessage(message, false, targetUser.getName());
        
        // 可以添加一个小标记表示这是离线消息
        chatPanel.addSystemMessage("以上是离线消息");
    }

    /**
     * 获取可转发的用户列表
     * @return 用户列表
     */
    private List<User> getForwardableUsers() {
        // 这里应该从主窗口获取用户列表
        // 临时实现，实际应用中需要修改
        List<User> users = new ArrayList<>();
        
        // 添加当前聊天对象，如果不是当前用户
        if (targetUser.getId() != currentUser.getId()) {
            users.add(targetUser);
        }
        
        return users;
    }

    /**
     * 转发消息给指定用户
     * @param content 消息内容
     * @param targetUser 目标用户
     */
    private void forwardMessage(String content, User targetUser) {
        if (out != null) {
            // 发送私聊消息格式: PM:接收者ID:消息内容
            String pmCommand = "PM:" + targetUser.getId() + ":" + content;
            out.println(pmCommand);
            
            // 显示转发成功提示
            chatPanel.addSystemMessage("已转发消息给 " + targetUser.getName());
        }
    }

    /**
     * 发送消息
     * @param message 消息内容
     */
    public void sendMessage(String message) {
        if (!message.isEmpty() && out != null) {
            // 发送私聊消息格式: PM:接收者ID:消息内容
            String pmCommand = "PM:" + targetUser.getId() + ":" + message;
            out.println(pmCommand);
            
            // 在自己的聊天窗口显示消息，传递当前用户名
            chatPanel.addMessage(message, true, currentUser.getName());
        }
    }
}
