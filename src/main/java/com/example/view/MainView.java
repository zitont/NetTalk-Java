package com.example.view;

import com.example.component.ChatPanel;
import com.example.model.User;
import com.example.model.Settings;
import com.example.dao.UserDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.*;
import java.net.Socket;
import javax.swing.border.AbstractBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class MainView extends JFrame {
    // 更现代的颜色方案
    private static final Color PRIMARY_COLOR = new Color(56, 129, 244);  // 更鲜亮的蓝色
    private static final Color PRIMARY_HOVER = new Color(25, 103, 210);  // 更深的悬停蓝色
    private static final Color BACKGROUND_COLOR = new Color(250, 250, 252);  // 更柔和的背景色
    private static final Color CHAT_BACKGROUND = Color.WHITE;  // 保持白色背景
    private static final Color SENT_BUBBLE_COLOR = new Color(56, 129, 244);  // 匹配主色调
    private static final Color RECEIVED_BUBBLE_COLOR = new Color(245, 245, 247);  // 更柔和的灰色
    private static final Color TEXT_COLOR = new Color(30, 30, 30);  // 更深的文本色
    private static final Color SECONDARY_TEXT = new Color(115, 115, 125);  // 更现代的次要文本色
    private static final Color BORDER_COLOR = new Color(230, 230, 235);  // 更柔和的边框色
    private static final Color SUCCESS_COLOR = new Color(46, 184, 92);  // 更鲜亮的绿色



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
    // Add new fields for user list management
    private JToggleButton showAllUsersToggle;
    private Set<Long> onlineUserIds = new HashSet<>();
    private List<User> allUsers = new ArrayList<>();
    private boolean showingAllUsers = false;

    // 字体常量
    private static final Font CHINESE_FONT = new Font("Microsoft YaHei", Font.PLAIN, 14); // 微软雅黑
    private static final Font CHINESE_FONT_BOLD = new Font("Microsoft YaHei", Font.BOLD, 14); // 微软雅黑粗体

    // Add UserDAO field
    private UserDAO userDAO;

    public MainView(User user) {
        this.currentUser = user;
        this.userDAO = new UserDAO();
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

        // 添加窗口阴影和圆角效果
        getRootPane().setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
    }

    private void initTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(CHAT_BACKGROUND);
        titleBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // 用户信息面板
        JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        userInfo.setBackground(CHAT_BACKGROUND);

        // 使用方形头像
        JLabel avatar = createSquareAvatar(currentUser.getName(), 40);
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

        // 添加设置按钮和服务器列表按钮
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);
        
        JButton serverListButton = new JButton("服务器列表");
        serverListButton.setFont(CHINESE_FONT);
        serverListButton.setForeground(PRIMARY_COLOR);
        serverListButton.setBorderPainted(false);
        serverListButton.setContentAreaFilled(false);
        serverListButton.setFocusPainted(false);
        serverListButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        serverListButton.addActionListener(e -> openServerList());
        
        JButton settingsButton = new JButton("设置");
        settingsButton.setFont(CHINESE_FONT);
        settingsButton.setForeground(PRIMARY_COLOR);
        settingsButton.setBorderPainted(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setFocusPainted(false);
        settingsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        settingsButton.addActionListener(e -> openSettings());
        
        actionPanel.add(serverListButton);
        actionPanel.add(settingsButton);
        
        titleBar.add(userInfo, BorderLayout.WEST);
        titleBar.add(actionPanel, BorderLayout.EAST);
        add(titleBar, BorderLayout.NORTH);
    }

    // 创建统一的方形头像方法 - 手动绘制文本
    private JLabel createSquareAvatar(String name, int size) {
        final String initial = getInitials(name);
        
        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // 绘制方形背景，带圆角
                g2.setColor(PRIMARY_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // 手动绘制文本
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("微软雅黑", Font.BOLD, getWidth() / 2));
                
                // 计算文本位置以居中显示
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(initial);
                int textHeight = fm.getHeight();
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - textHeight) / 2 + fm.getAscent();
                
                g2.drawString(initial, x, y);
                g2.dispose();
            }
        };
        
        avatar.setPreferredSize(new Dimension(size, size));
        avatar.setOpaque(false);
        
        return avatar;
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

        // 设置转发消息监听器
        chatPanel.setForwardMessageListener((content, isOwnMessage) -> {
            // 获取在线用户列表，排除当前用户
            List<User> forwardUsers = new ArrayList<>();
            for (User user : allUsers) {
                if (user.getId() != currentUser.getId() && onlineUserIds.contains(user.getId())) {
                    forwardUsers.add(user);
                }
            }
            
            // 显示转发对话框
            chatPanel.showForwardDialog(content, forwardUsers, (messageContent, targetUser) -> {
                // 执行转发操作
                forwardMessage(messageContent, targetUser);
            });
        });
    }

    private void initInputPanel() {
        inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BACKGROUND_COLOR);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // 创建更现代的输入框
        inputField = new JTextArea(3, 20);
        inputField.setFont(CHINESE_FONT);
        inputField.setLineWrap(true);
        inputField.setWrapStyleWord(true);
        
        // 添加圆角和轻微阴影的输入框面板
        JPanel inputWrapper = new JPanel(new BorderLayout());
        inputWrapper.setBackground(CHAT_BACKGROUND);
        inputWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        inputWrapper.add(inputField, BorderLayout.CENTER);
        
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

            System.out.println("连接已关闭");
            updateConnectionStatus(false);
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
        // If there's already a listening thread, stop it
        if (listeningThread != null) {
            listeningThread.interrupt();
        }
        
        listeningThread = new Thread(() -> {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    System.out.println("收到服务器消息: " + message);
                    
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
                    } else if (message.startsWith("OFFLINE_STAT:")) {
                        // 处理离线消息统计
                        handleOfflineMessageStat(message.substring(13));
                    } else if (message.startsWith("OFFLINE_MSG:")) {
                        // 处理离线消息
                        handleOfflineMessage(message.substring(12));
                    } else {
                        // 处理普通消息
                        addMessageBubble(message, false);
                    }
                }
            } catch (IOException e) {
                if (!Thread.currentThread().isInterrupted() && clientSocket != null && !clientSocket.isClosed()) {
                    JOptionPane.showMessageDialog(MainView.this, "服务器连接断开: " + e.getMessage());
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

            // 如果是系统生成的用户名格式 (User + 数字)，尝试查找真实用户名
            if (!isOwnMessage && username.matches("User\\d+")) {
                try {
                    // 从用户名中提取用户ID
                    long userId = Long.parseLong(username.substring(4));
                    
                    // 在用户列表中查找对应的用户
                    User user = findUserById(userId);
                    if (user != null) {
                        username = user.getName();
                    }
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    // 如果解析失败，保留原始用户名
                    System.err.println("无法解析用户ID: " + e.getMessage());
                }
            }

            // 使用ChatPanel添加消息
            chatPanel.addMessage(content, isOwnMessage, isOwnMessage ? currentUser.getName() : username);
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

                // 渐变背景效果
                Color bgColor = isEnabled() ?
                    (getModel().isPressed() ? PRIMARY_HOVER : PRIMARY_COLOR) :
                    SECONDARY_TEXT;
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, bgColor,
                    0, getHeight(), new Color(
                        Math.max(0, bgColor.getRed() - 20),
                        Math.max(0, bgColor.getGreen() - 20),
                        Math.max(0, bgColor.getBlue() - 20)
                    )
                );
                
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // 更大的圆角

                super.paintComponent(g);
                g2.dispose();
            }
        };

        button.setFont(CHINESE_FONT_BOLD);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(12, 28, 12, 28)); // 更大的内边距
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



    private void initUserListPanel() {
        userListPanel = new JPanel(new BorderLayout());
        userListPanel.setPreferredSize(new Dimension(240, 0)); // 稍微宽一点
        userListPanel.setBackground(Color.WHITE); // 白色背景更现代
        userListPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        // 创建标题面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE); // 白色背景
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(18, 18, 18, 18) // 更大的内边距
        ));

        // Create title with user count
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("在线用户");
        titleLabel.setFont(CHINESE_FONT_BOLD);
        titleLabel.setForeground(TEXT_COLOR);
        titlePanel.add(titleLabel);
        
        JLabel countLabel = new JLabel(" (0)");
        countLabel.setFont(CHINESE_FONT);
        countLabel.setForeground(SECONDARY_TEXT);
        titlePanel.add(countLabel);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        // Add toggle button with modern styling
        showAllUsersToggle = new JToggleButton("全部");
        showAllUsersToggle.setFont(CHINESE_FONT);
        showAllUsersToggle.setToolTipText("显示所有用户");
        showAllUsersToggle.setForeground(PRIMARY_COLOR);
        showAllUsersToggle.setBorderPainted(false);
        showAllUsersToggle.setContentAreaFilled(false);
        showAllUsersToggle.setFocusPainted(false);
        showAllUsersToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showAllUsersToggle.addActionListener(e -> {
            toggleUserListMode();
            // 更新计数标签
            int count = showingAllUsers ? allUsers.size() : onlineUserIds.size();
            countLabel.setText(" (" + count + ")");
            // 更新标题标签
            titleLabel.setText(showingAllUsers ? "所有用户" : "在线用户");
            // 更新按钮文本
            showAllUsersToggle.setText(showingAllUsers ? "在线" : "全部");
            // 更新工具提示
            showAllUsersToggle.setToolTipText(showingAllUsers ? "显示在线用户" : "显示所有用户");
        });
        headerPanel.add(showAllUsersToggle, BorderLayout.EAST);

        // Create search panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(BACKGROUND_COLOR);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField searchField = new JTextField();
        searchField.setFont(CHINESE_FONT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12) // 更大的内边距
        ));
        searchField.setBackground(Color.WHITE);
        searchField.setForeground(TEXT_COLOR);
        searchField.setCaretColor(PRIMARY_COLOR);
        
        // Add placeholder text
        searchField.setText("搜索用户...");
        searchField.setForeground(SECONDARY_TEXT);
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("搜索用户...")) {
                    searchField.setText("");
                    searchField.setForeground(TEXT_COLOR);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("搜索用户...");
                    searchField.setForeground(SECONDARY_TEXT);
                }
            }
        });
        
        // Add search functionality
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterUserList(searchField.getText());
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                filterUserList(searchField.getText());
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                filterUserList(searchField.getText());
            }
        });
        
        searchPanel.add(searchField, BorderLayout.CENTER);

        // Create user list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setCellRenderer(new UserListCellRenderer());
        userList.setBackground(BACKGROUND_COLOR);
        userList.setBorder(null);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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

        // Create a custom scroll pane with modern styling
        JScrollPane listScrollPane = new JScrollPane(userList);
        listScrollPane.setBorder(null);
        listScrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        
        // Set modern scrollbar UI
        listScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        listScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        listScrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        // Add components to user list panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(listScrollPane, BorderLayout.CENTER);
        
        userListPanel.add(headerPanel, BorderLayout.NORTH);
        userListPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Load all users from database
        loadAllUsers();
    }

    private class UserListCellRenderer extends JPanel implements ListCellRenderer<User> {
        private JLabel avatarLabel;
        private JLabel nameLabel;
        private JPanel statusIndicator;
        private JLabel statusLabel;

        public UserListCellRenderer() {
            setLayout(new BorderLayout(10, 0));
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Avatar panel (left side)
            JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            avatarPanel.setOpaque(false);
            avatarPanel.setPreferredSize(new Dimension(40, 40));

            avatarLabel = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    
                    // 绘制方形背景，带圆角
                    g2.setColor(PRIMARY_COLOR);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    
                    // 手动绘制文本
                    String text = getText();
                    if (text != null && !text.isEmpty()) {
                        g2.setColor(Color.WHITE);
                        g2.setFont(getFont());
                        
                        // 计算文本位置以居中显示
                        FontMetrics fm = g2.getFontMetrics();
                        int textWidth = fm.stringWidth(text);
                        int textHeight = fm.getHeight();
                        int x = (getWidth() - textWidth) / 2;
                        int y = (getHeight() - textHeight) / 2 + fm.getAscent();
                        
                        g2.drawString(text, x, y);
                    }
                    
                    g2.dispose();
                }
            };
            avatarLabel.setPreferredSize(new Dimension(36, 36));
            avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
            avatarLabel.setVerticalAlignment(SwingConstants.CENTER);
            avatarLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            avatarLabel.setForeground(Color.WHITE);
            
            avatarPanel.add(avatarLabel);

            // Info panel (right side)
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            nameLabel = new JLabel();
            nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            nameLabel.setForeground(TEXT_COLOR);
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Status panel with indicator
            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            statusPanel.setOpaque(false);

            statusIndicator = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            statusIndicator.setPreferredSize(new Dimension(8, 8));
            statusIndicator.setOpaque(false);

            statusLabel = new JLabel();
            statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            statusLabel.setForeground(SECONDARY_TEXT);

            statusPanel.add(statusIndicator);
            statusPanel.add(statusLabel);

            infoPanel.add(nameLabel);
            infoPanel.add(Box.createVerticalStrut(2));
            infoPanel.add(statusPanel);

            add(avatarPanel, BorderLayout.WEST);
            add(infoPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends User> list, User user,
                                                    int index, boolean isSelected, boolean cellHasFocus) {
            // Set user name
            nameLabel.setText(user.getName());

            // Create avatar with user's initial
            String initial = MainView.this.getInitials(user.getName());
            avatarLabel.setText(initial);

            // Check if user is online
            boolean isOnline = onlineUserIds.contains(user.getId());
            
            // Update status indicator
            if (isOnline) {
                statusLabel.setText("在线");
                statusIndicator.setBackground(SUCCESS_COLOR);
            } else {
                statusLabel.setText("离线");
                statusIndicator.setBackground(new Color(180, 180, 180));
            }

            // Set background color based on selection
            if (isSelected) {
                setBackground(new Color(232, 240, 254));
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 230, 245)),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            } else {
                setBackground(BACKGROUND_COLOR);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }

            return this;
        }
    }

    private void handleUserListUpdate(String userListStr) {
        SwingUtilities.invokeLater(() -> {
            // Clear the online users set
            onlineUserIds.clear();
            
            // Add current user to online users
            onlineUserIds.add(currentUser.getId());
            
            // Update online status for all users
            for (User user : allUsers) {
                user.setOnline(user.getId() == currentUser.getId());
            }
            
            // If the list is empty, don't process further
            if (!userListStr.isEmpty()) {
                // Parse and add other users
                String[] userInfos = userListStr.split(",");
                for (String userInfo : userInfos) {
                    String[] parts = userInfo.split(":");
                    if (parts.length == 2) {
                        long userId = Long.parseLong(parts[0]);
                        String userName = parts[1];
                        
                        // Add to online users set
                        onlineUserIds.add(userId);
                        
                        // Update online status for this user
                        for (User user : allUsers) {
                            if (user.getId() == userId) {
                                user.setOnline(true);
                                break;
                            }
                        }
                        
                        // Check if user exists in all users list
                        boolean userExists = false;
                        for (User user : allUsers) {
                            if (user.getId() == userId) {
                                userExists = true;
                                break;
                            }
                        }
                        
                        // If user doesn't exist, add to all users list
                        if (!userExists) {
                            User user = new User(userId, userName);
                            user.setOnline(true);
                            allUsers.add(user);
                        }
                    }
                }
            }
            
            // Update the user list based on current mode
            if (showingAllUsers) {
                updateUserListWithAllUsers();
            } else {
                updateUserListWithOnlineUsers();
            }
        });
    }

    private void handleUserJoined(String userInfo) {
        String[] parts = userInfo.split(":");
        if (parts.length == 2) {
            long userId = Long.parseLong(parts[0]);
            String userName = parts[1];
            
            // Add to online users set
            onlineUserIds.add(userId);
            
            // Check if user exists in all users list
            boolean userExists = false;
            for (User user : allUsers) {
                if (user.getId() == userId) {
                    userExists = true;
                    break;
                }
            }
            
            // If user doesn't exist, add to all users list
            if (!userExists) {
                User user = new User(userId, userName);
                allUsers.add(user);
            }
            
            // Update the user list based on current mode
            if (showingAllUsers) {
                updateUserListWithAllUsers();
            } else {
                updateUserListWithOnlineUsers();
            }
            
            // Add system message
            addSystemMessage(userName + " 已加入聊天");
        }
    }

    private void handleUserLeft(String userInfo) {
        String[] parts = userInfo.split(":");
        if (parts.length == 2) {
            long userId = Long.parseLong(parts[0]);
            String userName = parts[1];
            
            // Remove from online users set
            onlineUserIds.remove(userId);
            
            // Update the user list based on current mode
            if (showingAllUsers) {
                updateUserListWithAllUsers();
            } else {
                updateUserListWithOnlineUsers();
            }
            
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
            
            // 获取与该用户的离线消息
            if (out != null) {
                // 发送获取离线消息的命令: GET_OFFLINE_MSG:发送者ID (不带冒号)
                String command = "GET_OFFLINE_MSG:" + targetUser.getId();
                System.out.println("发送获取离线消息命令: " + command);
                out.println(command);
            }
            
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

    // 添加打开服务器列表的方法
    private void openServerList() {
        ServerDiscoveryDialog dialog = new ServerDiscoveryDialog(this);
        dialog.setVisible(true);
        
        String selectedServer = dialog.getSelectedServer();
        if (selectedServer != null) {
            // 解析服务器地址和端口
            String[] parts = selectedServer.split(":");
            if (parts.length == 2) {
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                
                // 断开当前连接
                closeConnection();
                
                // 连接到新服务器
                try {
                    clientSocket = new Socket(host, port);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    
                    // 发送用户ID
                    out.println(currentUser.getId());
                    updateConnectionStatus(true);
                    
                    // 清空并重新添加当前用户到列表
                    userListModel.clear();
                    addUserToList(currentUser);
                    
                    // 请求用户列表
                    out.println("GET_USERS");
                    
                    // 重新启动消息监听
                    startMessageListening();
                    
                    // 更新设置中的服务器地址和端口
                    Settings settings = Settings.getInstance();
                    settings.setServerHost(host);
                    settings.setServerPort(port);
                    settings.saveSettings();
                    
                    JOptionPane.showMessageDialog(this, 
                        "已连接到服务器: " + host + ":" + port, 
                        "连接成功", 
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, 
                        "无法连接到服务器: " + e.getMessage(), 
                        "连接失败", 
                        JOptionPane.ERROR_MESSAGE);
                    updateConnectionStatus(false);
                }
            }
        }
    }

    // Toggle between showing online users only and all users
    private void toggleUserListMode() {
        showingAllUsers = !showingAllUsers;
        if (showingAllUsers) {
            updateUserListWithAllUsers();
        } else {
            updateUserListWithOnlineUsers();
        }
    }

    // Load all users from database
    private void loadAllUsers() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                // Get all users from database
                List<User> users = userDAO.getAllUsers();
                
                // Make sure current user is in the list
                boolean currentUserFound = false;
                for (User user : users) {
                    if (user.getId() == currentUser.getId()) {
                        currentUserFound = true;
                        break;
                    }
                }
                
                if (!currentUserFound) {
                    users.add(currentUser);
                }
                
                return users;
            }
            
            @Override
            protected void done() {
                try {
                    allUsers = get();
                    // Mark current user as online
                    for (User user : allUsers) {
                        if (user.getId() == currentUser.getId()) {
                            user.setOnline(true);
                            break;
                        }
                    }
                    
                    // If we're showing all users, update the list
                    if (showingAllUsers) {
                        updateUserListWithAllUsers();
                    } else {
                        updateUserListWithOnlineUsers();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }

    // Update user list to show all users
    private void updateUserListWithAllUsers() {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (User user : allUsers) {
                userListModel.addElement(user);
            }
        });
    }

    // Update user list to show only online users
    private void updateUserListWithOnlineUsers() {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (User user : allUsers) {
                if (onlineUserIds.contains(user.getId())) {
                    userListModel.addElement(user);
                }
            }
        });
    }

    // Add method to filter user list based on search text
    private void filterUserList(String searchText) {
        if (searchText.equals("搜索用户...") || searchText.isEmpty()) {
            // Reset to normal view
            if (showingAllUsers) {
                updateUserListWithAllUsers();
            } else {
                updateUserListWithOnlineUsers();
            }
            return;
        }
        
        // Filter users based on search text
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            
            String lowerCaseSearch = searchText.toLowerCase();
            
            for (User user : allUsers) {
                if (user.getName().toLowerCase().contains(lowerCaseSearch)) {
                    // If showing only online users, check if user is online
                    if (!showingAllUsers && !onlineUserIds.contains(user.getId())) {
                        continue;
                    }
                    userListModel.addElement(user);
                }
            }
        });
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) {
            return "?";
        }
        return name.substring(0, 1).toUpperCase();
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
            
            // 如果已有与该用户的私聊窗口，也在窗口中显示消息
            if (privateChatWindows.containsKey(targetUser.getId())) {
                privateChatWindows.get(targetUser.getId()).sendMessage(content);
            }
        }
    }

    /**
     * 处理离线消息
     * @param message 离线消息内容
     */
    private void handleOfflineMessage(String message) {
        System.out.println("处理离线消息: " + message);
        
        // 离线消息格式: 发送者ID:消息内容
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            try {
                long senderId = Long.parseLong(parts[0]);
                String content = parts[1];
                
                System.out.println("解析离线消息: 发送者ID=" + senderId + ", 内容=" + content);

                // 查找发送者
                User sender = findUserById(senderId);
                if (sender != null) {
                    // 检查是否已有与该用户的聊天窗口
                    if (privateChatWindows.containsKey(senderId)) {
                        // 如果有，直接在窗口中显示离线消息
                        System.out.println("在现有私聊窗口显示离线消息");
                        privateChatWindows.get(senderId).receiveOfflineMessage(content);
                    } else {
                        // 如果没有，创建新窗口并显示离线消息
                        System.out.println("创建新私聊窗口并显示离线消息");
                        PrivateChatView chatView = new PrivateChatView(currentUser, sender, clientSocket);
                        privateChatWindows.put(senderId, chatView);
                        chatView.setVisible(true);
                        chatView.receiveOfflineMessage(content);

                        // 当窗口关闭时从映射中移除
                        chatView.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosed(WindowEvent e) {
                                privateChatWindows.remove(senderId);
                            }
                        });
                    }
                } else {
                    System.err.println("找不到发送者: ID=" + senderId);
                }
            } catch (NumberFormatException e) {
                System.err.println("解析发送者ID失败: " + e.getMessage());
            }
        } else {
            System.err.println("离线消息格式错误: " + message);
        }
    }

    /**
     * 处理离线消息统计
     * @param message 离线消息统计内容
     */
    private void handleOfflineMessageStat(String message) {
        // 离线消息统计格式: 发送者ID:消息数量
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            long senderId = Long.parseLong(parts[0]);
            int count = Integer.parseInt(parts[1]);
            
            // 查找发送者
            User sender = findUserById(senderId);
            if (sender != null) {
                String senderName = sender.getName();
                
                // 添加系统消息提示有离线消息
                SwingUtilities.invokeLater(() -> {
                    chatPanel.addSystemMessage("您有 " + count + " 条来自 " + senderName + " 的未读消息");
                });
                
                // 可以选择自动打开私聊窗口或者高亮显示用户列表中的该用户
                // 这里选择添加一个提示，让用户手动点击打开私聊
            }
        }
    }
}
