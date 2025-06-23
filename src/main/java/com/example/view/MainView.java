package com.example.view;

import com.example.component.ChatPanel;
import com.example.model.User;
import com.example.model.Settings;
import com.example.view.PrivateChatView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
// import javax.swing.Timer;
import java.util.HashMap;
import java.util.Map;

public class MainView extends JFrame {
    // 简化颜色方案
    private static final Color PRIMARY_COLOR = new Color(64, 123, 255);  // 主色调蓝色
    private static final Color PRIMARY_HOVER = new Color(45, 106, 255);  // 悬停时的蓝色
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);  // 浅灰背景
    private static final Color CHAT_BACKGROUND = Color.WHITE;  // 聊天区域白色背景
    private static final Color SENT_BUBBLE_COLOR = new Color(64, 123, 255);  // 发送消息气泡蓝色
    private static final Color RECEIVED_BUBBLE_COLOR = new Color(240, 242, 245);  // 接收消息气泡浅灰色
    private static final Color TEXT_COLOR = new Color(33, 37, 41);  // 主文本深灰色
    private static final Color SECONDARY_TEXT = new Color(108, 117, 125);  // 次要文本中灰色
    private static final Color BORDER_COLOR = new Color(222, 226, 230);  // 边框浅灰色
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);  // 成功绿色



    // 成员变量
    private JTextArea inputField;
    private JButton sendButton;
    private ChatPanel chatPanel; // 修改为ChatPanel类型
    private JScrollPane scrollPane;
    private JPanel inputPanel;
    private User currentUser;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listeningThread;
    // Add new field for user list
    private JPanel userListPanel;
    private DefaultListModel<User> userListModel;
    private JList<User> userList;

    // 字体常量
    private static final Font CHINESE_FONT = new Font("Microsoft YaHei", Font.PLAIN, 14); // 微软雅黑
    private static final Font CHINESE_FONT_BOLD = new Font("Microsoft YaHei", Font.BOLD, 14); // 微软雅黑粗体

    public MainView(User user) {
        this.currentUser = user;
        initUI();
        connectToServer();
        startMessageListening();

        // 添加窗口关闭监听器以强制结束进程
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("强制结束进程...");

                // 打印所有活动线程
                System.out.println("当前活动线程:");
                Thread.getAllStackTraces().keySet().forEach(thread -> {
                    System.out.println(thread.getName() + " - 守护线程: " + thread.isDaemon() +
                                      " - 状态: " + thread.getState());
                });

                // 尝试关闭连接，但不等待
                try {
                    closeConnection();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    // 先尝试正常关闭
                    dispose();
                    // 然后强制结束进程 - 使用最直接的方式
                    System.out.println("执行强制终止...");
                    Runtime.getRuntime().halt(0);
                }
            }
        });
    }

    private void initUI() {
        // 设置现代化外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("通信软件 - " + currentUser.getName());
        setSize(900, 700);
        // 设置为DO_NOTHING_ON_CLOSE，让我们的windowClosing事件处理器完全控制关闭行为
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        // 添加现代化标题栏
        initTitleBar();
        initChatPanel();
        initInputPanel();
        initUserListPanel();

        add(userListPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

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

        // 用户头像（使用首字母）
        JLabel avatar = createUserAvatar(currentUser.getName());
        userInfo.add(avatar);
        userInfo.add(Box.createHorizontalStrut(12));

        // 用户名和状态的垂直布局
        JPanel nameStatusPanel = new JPanel();
        nameStatusPanel.setLayout(new BoxLayout(nameStatusPanel, BoxLayout.Y_AXIS));
        nameStatusPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(currentUser.getName());
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        nameLabel.setForeground(TEXT_COLOR);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        statusPanel.setOpaque(false);
        
        // 添加设置按钮
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        
        JButton settingsButton = new JButton("设置");
        settingsButton.setFont(CHINESE_FONT);
        settingsButton.setForeground(PRIMARY_COLOR);
        settingsButton.setBorderPainted(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setFocusPainted(false);
        settingsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        settingsButton.addActionListener(e -> openSettings());
        
        actionPanel.add(settingsButton);
        titleBar.add(actionPanel, BorderLayout.EAST);
        
        // 其余代码保持不变...
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
        String initials = getInitials(name);
        JLabel avatar = new JLabel(initials, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制圆形背景
                g2.setColor(PRIMARY_COLOR);
                g2.fillOval(0, 0, getWidth(), getHeight());

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

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) {
            return "?";
        }
        return name.substring(0, 1).toUpperCase();
    }

    private void initChatPanel() {
        // 使用ChatPanel组件替代原来的实现
        chatPanel = new ChatPanel(
            CHAT_BACKGROUND,
            SENT_BUBBLE_COLOR,
            RECEIVED_BUBBLE_COLOR,
            TEXT_COLOR,
            SECONDARY_TEXT,
            CHINESE_FONT
        );

        // 获取ChatPanel的滚动面板
        scrollPane = chatPanel.getScrollPane();

        // 设置现代化滚动条
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUI(new ModernScrollBarUI());
        verticalScrollBar.setUnitIncrement(16);

        // 设置首选大小，确保有足够的空间
        scrollPane.setPreferredSize(new Dimension(600, 400));
    }

    private void initInputPanel() {
        inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));
        inputPanel.setBackground(CHAT_BACKGROUND);

        // 创建输入框容器
        JPanel inputWrapper = new JPanel(new BorderLayout());
        inputWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        inputWrapper.setBackground(BACKGROUND_COLOR);

        // 将JTextField改为JTextArea以支持多行输入
        inputField = new JTextArea(3, 20);
        inputField.setFont(CHINESE_FONT); // 使用支持中文的字体
        inputField.setLineWrap(true); // 自动换行
        inputField.setWrapStyleWord(true); // 按单词边界换行
        inputField.setBackground(BACKGROUND_COLOR);
        inputField.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        inputField.setEnabled(true); // 确保输入框启用

        // 创建带滚动条的输入区域
        JScrollPane inputScrollPane = new JScrollPane(inputField);
        inputScrollPane.setBorder(BorderFactory.createEmptyBorder());
        inputScrollPane.setOpaque(false);
        inputScrollPane.getViewport().setOpaque(false);

        inputWrapper.add(inputScrollPane, BorderLayout.CENTER);

        // 添加文档监听器以启用/禁用发送按钮
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

        // 简化回车键处理
        inputField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    if (evt.isShiftDown()) {
                        // Shift+Enter 插入换行符
                        inputField.append("\n");
                    } else {
                        // 仅Enter键发送消息
                        evt.consume(); // 阻止默认的换行行为
                        if (!inputField.getText().trim().isEmpty()) {
                            sendMessage();
                        }
                    }
                }
            }
        });

        // 添加发送按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(sendButton);

        inputPanel.add(inputWrapper, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
    }

    private void connectToServer() {
        try {
            Settings settings = Settings.getInstance();
            clientSocket = new Socket(settings.getServerHost(), settings.getServerPort());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            
            // Send user ID to server
            out.println(currentUser.getId());
            updateConnectionStatus(true); // 连接成功
            
            // Add current user to the list first
            addUserToList(currentUser);
            
            // Request user list from server after connection is established
            out.println("GET_USERS");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "无法连接到服务器: " + e.getMessage());
            updateConnectionStatus(false); // 连接失败
        }
    }

    private void closeConnection() {
        System.out.println("正在关闭连接...");
        try {
            if (listeningThread != null) {
                listeningThread.interrupt(); // 中断监听线程
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

            System.out.println("连接已关闭");
        } catch (IOException e) {
            System.err.println("关闭连接时出错: " + e.getMessage());
        }
    }

    // 添加连接状态更新方法
    private void updateConnectionStatus(boolean isConnected) {
        // 可以在这里更新UI显示连接状态
        String status = isConnected ? "已连接服务器" : "未连接服务器";
        SwingUtilities.invokeLater(() -> {
            setTitle("通信软件 - " + currentUser.getName() + " (" + status + ")");
        });
    }

    // 添加消息监听方法实现
    private void startMessageListening() {
        listeningThread = new Thread(() -> {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("USER_LIST:")) {
                        // 处理用户列表更新
                        handleUserListUpdate(message.substring(10));
                    } else if (message.startsWith("USER_JOINED:")) {
                        // 处理新用户加入
                        handleUserJoined(message.substring(12));
                    } else if (message.startsWith("USER_LEFT:")) {
                        // 处理用户离开
                        handleUserLeft(message.substring(10));
                    } else if (message.startsWith("PM:")) {
                        // 处理私聊消息
                        handlePrivateMessage(message.substring(3));
                    } else {
                        // 处理普通消息
                        addMessageBubble(message, false);
                    }
                }
            } catch (IOException e) {
                if (!clientSocket.isClosed()) {
                    JOptionPane.showMessageDialog(this, "服务器连接断开: " + e.getMessage());
                    updateConnectionStatus(false);
                }
            }
        });
        listeningThread.start();
    }

    private void addMessageBubble(String message, boolean isOwnMessage) {
        SwingUtilities.invokeLater(() -> {
            // 解析消息
            String[] parts = message.split(": ", 2);
            String username = parts[0];
            String content = parts.length > 1 ? parts[1] : message;

            // 使用ChatPanel添加消息
            chatPanel.addMessage(content, isOwnMessage, isOwnMessage ? null : username);
        });
    }

    // 创建消息行
    // 添加自己消息的组件
    // 添加他人消息的组件
    // 创建用户名标签
    // 消息气泡创建 - 修复内容溢出问题
    // 处理段落间距
    // 计算段落数量
    // 优化的中文换行算法 - 计算文本换行后的行数
    // 优化的中文换行算法 - 找出文本在指定宽度内的断点
    // 消息出现动画
    // 滚动到底部
    // 添加时间分隔线的方法 - 修改为只显示小时和分钟，无背景

    // 现代化滚动条UI
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(200, 200, 200);
            this.trackColor = CHAT_BACKGROUND;
            this.thumbHighlightColor = new Color(150, 150, 150);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2,
                           thumbBounds.width - 4, thumbBounds.height - 4, 6, 6);
            g2.dispose();
        }
    }



    // 添加支持透明度的JPanel子类
    // 添加发送按钮创建方法
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
        button.addActionListener(e -> sendMessage());

        return button;
    }

    // 添加发送消息方法
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty() && out != null) {
            out.println(message);
            addMessageBubble(currentUser.getName() + ": " + message, true);
            resetInputField();
        }
    }

    private void resetInputField() {
        inputField.setText("");
        sendButton.setEnabled(false);
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }

    // 消息数据类
    // 添加一个main方法测试窗口关闭
    // public static void main(String[] args) {
    //     // 仅用于测试窗口关闭功能
    //     SwingUtilities.invokeLater(() -> {
    //         User testUser = new User(1L, "TestUser");
    //         testUser.setPassword(""); // 如果需要设置密码
    //         MainView view = new MainView(testUser);
    //         view.setVisible(true);
    //     });
    // }

    private void initUserListPanel() {
        userListPanel = new JPanel(new BorderLayout());
        userListPanel.setPreferredSize(new Dimension(200, 0));
        userListPanel.setBackground(BACKGROUND_COLOR);
        userListPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        // Create header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CHAT_BACKGROUND);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("在线用户");
        titleLabel.setFont(CHINESE_FONT_BOLD);
        titleLabel.setForeground(TEXT_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Create user list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setCellRenderer(new UserListCellRenderer());
        userList.setBackground(BACKGROUND_COLOR);
        userList.setBorder(null);

        // 添加鼠标监听器处理用户点击
        userList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = userList.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        User selectedUser = userListModel.getElementAt(index);
                        // 不要和自己聊天
                        if (selectedUser.getId() != currentUser.getId()) {
                            openPrivateChat(selectedUser);
                        }
                    }
                }
            }
        });

        JScrollPane listScrollPane = new JScrollPane(userList);
        listScrollPane.setBorder(null);
        listScrollPane.getViewport().setBackground(BACKGROUND_COLOR);

        userListPanel.add(headerPanel, BorderLayout.NORTH);
        userListPanel.add(listScrollPane, BorderLayout.CENTER);
    }

    private class UserListCellRenderer extends JPanel implements ListCellRenderer<User> {
        private JLabel avatarLabel;
        private JLabel nameLabel;
        private JLabel statusDot;

        public UserListCellRenderer() {
            setLayout(new BorderLayout(10, 0));
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            leftPanel.setOpaque(false);

            avatarLabel = new JLabel();
            avatarLabel.setPreferredSize(new Dimension(32, 32));
            leftPanel.add(avatarLabel);

            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setOpaque(false);

            nameLabel = new JLabel();
            nameLabel.setFont(CHINESE_FONT);
            nameLabel.setForeground(TEXT_COLOR);
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            statusPanel.setOpaque(false);

            statusDot = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(SUCCESS_COLOR);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            statusDot.setPreferredSize(new Dimension(6, 6));

            JLabel statusLabel = new JLabel("在线");
            statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            statusLabel.setForeground(SECONDARY_TEXT);

            statusPanel.add(statusDot);
            statusPanel.add(statusLabel);

            rightPanel.add(nameLabel);
            rightPanel.add(statusPanel);

            add(leftPanel, BorderLayout.WEST);
            add(rightPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends User> list, User user,
                                                    int index, boolean isSelected, boolean cellHasFocus) {
            nameLabel.setText(user.getName());

            // Create avatar with user's initial
            String initial = getInitials(user.getName());
            avatarLabel.setText(initial);
            avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
            avatarLabel.setVerticalAlignment(SwingConstants.CENTER);
            avatarLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
            avatarLabel.setForeground(Color.WHITE);
            avatarLabel.setOpaque(true);
            avatarLabel.setBackground(PRIMARY_COLOR);

            if (isSelected) {
                setBackground(new Color(232, 240, 254));
            } else {
                setBackground(BACKGROUND_COLOR);
            }

            return this;
        }
    }

    private void handleUserListUpdate(String userListStr) {
        SwingUtilities.invokeLater(() -> {
            // Clear the list but keep the current user
            userListModel.clear();
            addUserToList(currentUser);

            // If the list is empty, don't process further
            if (userListStr.isEmpty()) {
                return;
            }

            // Parse and add other users
            String[] userInfos = userListStr.split(",");
            for (String userInfo : userInfos) {
                String[] parts = userInfo.split(":");
                if (parts.length == 2) {
                    long userId = Long.parseLong(parts[0]);
                    String userName = parts[1];

                    // Skip current user as we already added them
                    if (userId != currentUser.getId()) {
                        User user = new User(userId, userName);
                        addUserToList(user);
                    }
                }
            }

            // Debug output
            System.out.println("Updated user list. Total users: " + userListModel.size());
        });
    }

    private void handleUserJoined(String userInfo) {
        String[] parts = userInfo.split(":");
        if (parts.length == 2) {
            long userId = Long.parseLong(parts[0]);
            String userName = parts[1];

            // Skip if it's the current user
            if (userId != currentUser.getId()) {
                User user = new User(userId, userName);
                addUserToList(user);

                // Add system message
                addSystemMessage(userName + " 已加入聊天");
            }
        }
    }

    private void handleUserLeft(String userInfo) {
        String[] parts = userInfo.split(":");
        if (parts.length == 2) {
            long userId = Long.parseLong(parts[0]);
            String userName = parts[1];

            removeUserFromList(userId);

            // Add system message
            addSystemMessage(userName + " 已离开聊天");
        }
    }

    private void addUserToList(User user) {
        SwingUtilities.invokeLater(() -> {
            // Check if user already exists
            for (int i = 0; i < userListModel.size(); i++) {
                if (userListModel.getElementAt(i).getId() == user.getId()) {
                    return;
                }
            }
            userListModel.addElement(user);
        });
    }

    private void removeUserFromList(long userId) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < userListModel.size(); i++) {
                if (userListModel.getElementAt(i).getId() == userId) {
                    userListModel.remove(i);
                    break;
                }
            }
        });
    }

    private void addSystemMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatPanel.addSystemMessage(message);
        });
    }

    // Debug method to print current user list
    private void printUserList() {
        System.out.println("Current user list:");
        for (int i = 0; i < userListModel.size(); i++) {
            User user = userListModel.getElementAt(i);
            System.out.println("  - " + user.getId() + ": " + user.getName());
        }
    }

    // 添加私聊窗口管理
    private Map<Long, PrivateChatView> privateChatWindows = new HashMap<>();

    // 打开私聊窗口
    private void openPrivateChat(User targetUser) {
        // 检查是否已经有与该用户的聊天窗口
        if (!privateChatWindows.containsKey(targetUser.getId())) {
            PrivateChatView chatView = new PrivateChatView(currentUser, targetUser, clientSocket);
            privateChatWindows.put(targetUser.getId(), chatView);
            chatView.setVisible(true);

            // 当窗口关闭时从映射中移除
            chatView.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    privateChatWindows.remove(targetUser.getId());
                }
            });
        } else {
            // 如果窗口已存在，将其置于前台
            privateChatWindows.get(targetUser.getId()).toFront();
        }
    }

    // 处理私聊消息
    private void handlePrivateMessage(String message) {
        // 私聊消息格式: 发送者ID:消息内容
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            long senderId = Long.parseLong(parts[0]);
            String content = parts[1];

            // 查找发送者
            User sender = findUserById(senderId);
            if (sender != null) {
                SwingUtilities.invokeLater(() -> {
                    // 检查是否已有与该用户的聊天窗口
                    if (privateChatWindows.containsKey(senderId)) {
                        // 如果有，直接在窗口中显示消息
                        privateChatWindows.get(senderId).receiveMessage(content);
                    } else {
                        // 如果没有，创建新窗口并显示消息
                        PrivateChatView chatView = new PrivateChatView(currentUser, sender, clientSocket);
                        privateChatWindows.put(senderId, chatView);
                        chatView.setVisible(true);
                        chatView.receiveMessage(content);

                        // 当窗口关闭时从映射中移除
                        chatView.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosed(WindowEvent e) {
                                privateChatWindows.remove(senderId);
                            }
                        });
                    }
                });
            }
        }
    }

    // 根据ID查找用户
    private User findUserById(long userId) {
        for (int i = 0; i < userListModel.size(); i++) {
            User user = userListModel.getElementAt(i);
            if (user.getId() == userId) {
                return user;
            }
        }
        return null;
    }

    // 添加打开设置页面的方法
    // 添加一个静态变量来跟踪设置窗口实例
    private static SettingsView settingsView = null;

    private void openSettings() {
        // 如果设置窗口已经存在，则将其置于前台
        if (settingsView != null && settingsView.isDisplayable()) {
            settingsView.toFront();
            settingsView.requestFocus();
        } else {
            // 创建新的设置窗口
            settingsView = new SettingsView();
            // 添加窗口关闭监听器，在窗口关闭时清除引用
            settingsView.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    settingsView = null;
                }
            });
            settingsView.setVisible(true);
        }
    }
}
