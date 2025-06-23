package com.example.view;

import com.example.controller.AuthController;
import com.example.model.User;
import javax.swing.*;
import java.awt.*;
// import java.awt.geom.RoundRectangle2D;

public class LoginView extends JFrame {
    private final AuthController authController;
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    // 现代化配色方案 - 与MainView保持一致
    private static final Color PRIMARY_COLOR = new Color(64, 123, 255);
    private static final Color PRIMARY_HOVER = new Color(45, 106, 255);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    // private static final Color SECONDARY_TEXT = new Color(108, 117, 125);
    private static final Color BORDER_COLOR = new Color(222, 226, 230);

    public LoginView() {
        this.authController = new AuthController();
        initUI();
    }

    private void initUI() {
        // 设置现代化外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        setTitle("通信软件 - 登录/注册");
        setSize(400, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(BACKGROUND_COLOR);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        
        // 添加标题
        JLabel titleLabel = new JLabel("欢迎使用通信软件", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // 创建表单面板
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        // 用户名输入框
        JLabel usernameLabel = new JLabel("用户名");
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        usernameLabel.setForeground(TEXT_COLOR);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        usernameField = createStyledTextField();
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // 密码输入框
        JLabel passwordLabel = new JLabel("密码");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordLabel.setForeground(TEXT_COLOR);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        passwordField = createStyledPasswordField();
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // 按钮面板 - 改为使用FlowLayout以确保按钮大小一致
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // 登录按钮
        JButton loginBtn = createStyledButton("登录");
        loginBtn.addActionListener(e -> handleLogin());
        
        // 注册按钮
        JButton registerBtn = createStyledButton("注册");
        registerBtn.addActionListener(e -> handleRegister());
        
        // 添加组件到表单面板
        formPanel.add(usernameLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(passwordField);
        
        // 添加按钮到按钮面板
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        
        // 添加所有面板到主面板
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                super.paintComponent(g);
            }
            
            // 固定首选大小
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 40);
            }
            
            // 固定最大大小
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(300, 40);
            }
        };
        field.setOpaque(false);
        field.setBackground(Color.WHITE);
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }
    
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                super.paintComponent(g);
            }
            
            // 固定首选大小
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 40);
            }
            
            // 固定最大大小
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(300, 40);
            }
        };
        field.setOpaque(false);
        field.setBackground(Color.WHITE);
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bgColor = getModel().isPressed() ? PRIMARY_HOVER : PRIMARY_COLOR;
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                super.paintComponent(g);
            }
            
            // 固定首选大小
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(200, 40);
            }
        };
        button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // 验证输入不为空
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空", "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (authController.login(username, password)) {
            openMainView(authController.getCurrentUser());
        } else {
            JOptionPane.showMessageDialog(this, "登录失败，请检查用户名和密码", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // 验证输入不为空
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空", "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (authController.register(username, password)) {
            JOptionPane.showMessageDialog(this, "注册成功，请登录", "成功", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "注册失败，用户名可能已存在", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openMainView(User user) {
        // 打开主界面
        this.dispose();
        new MainView(user).setVisible(true);
    }
}
