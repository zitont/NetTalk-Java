package com.example.component;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
// import java.awt.geom.RoundRectangle2D;

/**
 * 输入面板组件
 */
public class InputPanel extends JPanel {
    private JTextArea inputField;
    private JButton sendButton;
    private MessageSendListener sendListener;
    
    // 颜色常量
    private final Color primaryColor;
    private final Color primaryHoverColor;
    private final Color backgroundColor;
    private final Color borderColor;
    private final Font textFont;

    /**
     * 创建输入面板
     * @param primaryColor 主色调
     * @param primaryHoverColor 悬停色调
     * @param backgroundColor 背景色
     * @param borderColor 边框色
     * @param textFont 文本字体
     */
    public InputPanel(Color primaryColor, Color primaryHoverColor, Color backgroundColor, 
                     Color borderColor, Font textFont) {
        this.primaryColor = primaryColor;
        this.primaryHoverColor = primaryHoverColor;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.textFont = textFont;
        
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 20, 20, 20));
        setBackground(Color.WHITE);

        // 创建输入框容器
        JPanel inputWrapper = new JPanel(new BorderLayout());
        inputWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        inputWrapper.setBackground(backgroundColor);

        // 创建多行输入框
        inputField = new JTextArea(3, 20);
        inputField.setFont(textFont);
        inputField.setLineWrap(true);
        inputField.setWrapStyleWord(true);
        inputField.setBackground(backgroundColor);
        inputField.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        
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
        
        // 添加回车键处理
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
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
        
        add(inputWrapper, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);
    }

    // 创建发送按钮
    private JButton createSendButton() {
        JButton button = new JButton("发送") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bgColor = isEnabled() ? 
                    (getModel().isPressed() ? primaryHoverColor : primaryColor) : 
                    new Color(108, 117, 125);
                
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                super.paintComponent(g);
                g2.dispose();
            }
        };
        
        button.setFont(new Font(textFont.getName(), Font.BOLD, textFont.getSize()));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setEnabled(false);
        button.addActionListener(e -> sendMessage());
        
        return button;
    }

    // 发送消息
    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty() && sendListener != null) {
            sendListener.onMessageSend(message);
            resetInputField();
        }
    }

    // 重置输入框
    private void resetInputField() {
        inputField.setText("");
        sendButton.setEnabled(false);
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }

    /**
     * 设置消息发送监听器
     * @param listener 消息发送监听器
     */
    public void setMessageSendListener(MessageSendListener listener) {
        this.sendListener = listener;
    }

    /**
     * 获取输入框
     * @return 输入框组件
     */
    public JTextArea getInputField() {
        return inputField;
    }

    /**
     * 获取发送按钮
     * @return 发送按钮组件
     */
    public JButton getSendButton() {
        return sendButton;
    }

    /**
     * 消息发送监听器接口
     */
    public interface MessageSendListener {
        void onMessageSend(String message);
    }
}