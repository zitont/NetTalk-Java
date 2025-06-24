package com.example.view;

import com.example.model.Settings;
import com.example.service.SocketService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsView extends JFrame {
    // 颜色方案
    private static final Color PRIMARY_COLOR = new Color(64, 123, 255);
    private static final Color PRIMARY_HOVER = new Color(45, 106, 255);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color BORDER_COLOR = new Color(222, 226, 230);
    
    // 字体
    private static final Font CHINESE_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    private static final Font CHINESE_FONT_BOLD = new Font("微软雅黑", Font.BOLD, 14);
    
    // 组件
    private JTextField hostField;
    private JTextField portField;
    private JCheckBox serverModeCheckbox;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton startServerButton;
    private boolean isServerRunning = false;
    private   SocketService socketService;
    
    // 设置实例
    private Settings settings;
    
    public SettingsView() {
        settings = Settings.getInstance();
        socketService = new SocketService();
        
        // 检查服务器是否已经在运行
        isServerRunning = settings.isStartServerMode();
        
        initUI();
    }
    
    private void initUI() {
        setTitle("设置");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(BACKGROUND_COLOR);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 标题
        JLabel titleLabel = new JLabel("连接设置", SwingConstants.LEFT);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // 创建输入框
        hostField = new JTextField(settings.getServerHost(), 20);
        hostField.setFont(CHINESE_FONT);
        hostField.setPreferredSize(new Dimension(200, 30));
        
        portField = new JTextField(String.valueOf(settings.getServerPort()), 20);
        portField.setFont(CHINESE_FONT);
        portField.setPreferredSize(new Dimension(200, 30));
        
        // 服务器地址设置
        JPanel hostPanel = createFormGroup("服务器地址:", hostField);
        mainPanel.add(hostPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // 端口设置
        JPanel portPanel = createFormGroup("服务器端口:", portField);
        mainPanel.add(portPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // 服务器模式设置
        serverModeCheckbox = new JCheckBox("启动服务器模式", settings.isStartServerMode());
        serverModeCheckbox.setFont(CHINESE_FONT);
        serverModeCheckbox.setBackground(BACKGROUND_COLOR);
        serverModeCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(serverModeCheckbox);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // 添加服务器控制按钮
        startServerButton = new JButton(isServerRunning ? "停止服务器" : "启动服务器");
        startServerButton.setFont(CHINESE_FONT);
        startServerButton.setBackground(PRIMARY_COLOR);
        startServerButton.setForeground(Color.WHITE);
        startServerButton.setFocusPainted(false);
        startServerButton.setBorderPainted(false);
        startServerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startServerButton.addActionListener(e -> toggleServer());
        
        JPanel serverControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        serverControlPanel.setBackground(BACKGROUND_COLOR);
        serverControlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        serverControlPanel.add(startServerButton);
        
        mainPanel.add(serverControlPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        cancelButton = createButton("取消");
        cancelButton.addActionListener(e -> dispose());
        
        saveButton = createButton("保存");
        saveButton.addActionListener(e -> saveSettings());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(saveButton);
        mainPanel.add(buttonPanel);
        
        add(mainPanel);
        
        // 确保窗口大小适合内容
        pack();
        setMinimumSize(new Dimension(450, 350));
    }
    
    private JPanel createFormGroup(String labelText, JComponent component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel label = new JLabel(labelText);
        label.setFont(CHINESE_FONT);
        label.setPreferredSize(new Dimension(100, 30));
        panel.add(label);
        
        // 添加传入的组件
        if (component != null) {
            panel.add(component);
        }
        
        return panel;
    }
    
    private JButton createButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bgColor = getModel().isPressed() ? PRIMARY_HOVER : PRIMARY_COLOR;
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                super.paintComponent(g);
                g2.dispose();
            }
        };
        
        button.setFont(CHINESE_FONT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void saveSettings() {
        try {
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            boolean serverMode = serverModeCheckbox.isSelected();
            
            if (host.isEmpty()) {
                JOptionPane.showMessageDialog(this, "服务器地址不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (port <= 0 || port > 65535) {
                JOptionPane.showMessageDialog(this, "端口号必须在1-65535之间", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            settings.setServerHost(host);
            settings.setServerPort(port);
            settings.setStartServerMode(serverMode);
            settings.saveSettings();
            
            // 在保存设置后，如果服务器状态与设置不一致，则更新服务器状态
            if (serverMode != isServerRunning) {
                if (serverMode) {
                    // 需要启动服务器
                    socketService.startServer(port);
                    isServerRunning = true;
                    startServerButton.setText("停止服务器");
                } else {
                    // 需要停止服务器
                    socketService.shutdown();
                    isServerRunning = false;
                    startServerButton.setText("启动服务器");
                }
            }
            
            JOptionPane.showMessageDialog(this, "设置已保存，重启应用后生效", "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "端口号必须是有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 切换服务器状态（启动/停止）
     */
    private void toggleServer() {
        if (!isServerRunning) {
            // 启动服务器
            int port = Integer.parseInt(portField.getText().trim());
            socketService.startServer(port);
            
            // 更新按钮和状态
            startServerButton.setText("停止服务器");
            isServerRunning = true;
            
            JOptionPane.showMessageDialog(this, 
                "服务器已在端口 " + port + " 启动", 
                "服务器状态", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            // 停止服务器
            socketService.shutdown();
            
            // 更新按钮和状态
            startServerButton.setText("启动服务器");
            isServerRunning = false;
            
            JOptionPane.showMessageDialog(this, 
                "服务器已停止", 
                "服务器状态", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
