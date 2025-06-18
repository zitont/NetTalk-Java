package com.example.component;

import javax.swing.*;
import java.awt.*;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
// import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 聊天面板组件，包含消息气泡和时间线
 */
public class ChatPanel extends JPanel {
    private final JPanel contentPanel;
    private final JScrollPane scrollPane;
    // private final Color chatBackground;
    private final Color sentBubbleColor;
    private final Color receivedBubbleColor;
    private final Color textColor;
    private final Color secondaryTextColor;
    private final Font textFont;
    private final Font secondaryFont;
    
    private String lastTimestamp = "";

    /**
     * 创建聊天面板
     * @param chatBackground 聊天背景色
     * @param sentBubbleColor 发送消息气泡色
     * @param receivedBubbleColor 接收消息气泡色
     * @param textColor 文本颜色
     * @param secondaryTextColor 次要文本颜色
     * @param textFont 文本字体
     */
    public ChatPanel(Color chatBackground, Color sentBubbleColor, Color receivedBubbleColor,
                    Color textColor, Color secondaryTextColor, Font textFont) {
        this.sentBubbleColor = sentBubbleColor;
        this.receivedBubbleColor = receivedBubbleColor;
        this.textColor = textColor;
        this.secondaryTextColor = secondaryTextColor;
        this.textFont = textFont;
        this.secondaryFont = new Font(textFont.getName(), Font.PLAIN, textFont.getSize() - 2);
        
        setLayout(new BorderLayout());
        setBackground(chatBackground);
        
        // 创建内容面板
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(chatBackground);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 创建滚动面板
        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(chatBackground);
        
        // 设置现代化滚动条
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * 添加消息气泡
     * @param content 消息内容
     * @param isOwnMessage 是否为自己发送的消息
     * @param username 用户名（如果不是自己发送的消息）
     */
    public void addMessage(String content, boolean isOwnMessage, String username) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        
        addTimelineIfNeeded(timestamp);
        JPanel messageRow = createMessageRow(content, isOwnMessage, username);
        
        contentPanel.add(messageRow);
        contentPanel.revalidate();
        contentPanel.repaint();
        scrollToBottom();
    }
    
    /**
     * 如果需要，添加时间线
     * @param timestamp 时间戳
     */
    private void addTimelineIfNeeded(String timestamp) {
        // 检查是否需要添加时间线（每隔一段时间或首条消息）
        boolean shouldAddTimeline = lastTimestamp.isEmpty() || 
                                   !timestamp.substring(0, 16).equals(lastTimestamp.substring(0, 16));
        
        if (shouldAddTimeline) {
            // 提取小时和分钟
            String hourMinute = timestamp.substring(11, 16);
            
            // 创建时间分隔线
            JPanel timelinePanel = new JPanel(new BorderLayout());
            timelinePanel.setOpaque(false);
            timelinePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            
            // 时间标签
            JLabel timeLabel = new JLabel(hourMinute);
            timeLabel.setFont(secondaryFont);
            timeLabel.setForeground(secondaryTextColor);
            timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            // 添加到时间线面板
            timelinePanel.add(timeLabel, BorderLayout.CENTER);
            
            // 添加到聊天面板
            contentPanel.add(timelinePanel);
            
            // 更新最后时间戳
            lastTimestamp = timestamp;
        }
    }
    
    /**
     * 创建消息行
     * @param content 消息内容
     * @param isOwnMessage 是否为自己发送的消息
     * @param username 用户名（如果不是自己发送的消息）
     * @return 消息行面板
     */
    private JPanel createMessageRow(String content, boolean isOwnMessage, String username) {
        JPanel messageRow = new JPanel();
        messageRow.setOpaque(false);
        messageRow.setLayout(new BorderLayout());
        messageRow.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        
        JPanel bubbleContainer = new JPanel();
        bubbleContainer.setOpaque(false);
        bubbleContainer.setLayout(new BoxLayout(bubbleContainer, BoxLayout.Y_AXIS));
        
        // 如果不是自己的消息且有用户名，添加用户名标签
        if (!isOwnMessage && username != null) {
            JLabel usernameLabel = new JLabel(username);
            usernameLabel.setFont(new Font(textFont.getName(), Font.BOLD, textFont.getSize() - 2));
            usernameLabel.setForeground(new Color(120, 120, 120));
            usernameLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 2, 0));
            
            bubbleContainer.add(usernameLabel);
        }
        
        // 创建消息气泡
        JPanel bubble = createMessageBubble(content, isOwnMessage);
        
        // 添加气泡到容器
        bubbleContainer.add(bubble);
        
        // 根据消息类型设置对齐方式
        if (isOwnMessage) {
            messageRow.add(Box.createHorizontalGlue(), BorderLayout.WEST);
            messageRow.add(bubbleContainer, BorderLayout.EAST);
        } else {
            messageRow.add(bubbleContainer, BorderLayout.WEST);
            messageRow.add(Box.createHorizontalGlue(), BorderLayout.EAST);
        }
        
        return messageRow;
    }
    
    /**
     * 创建消息气泡
     * @param content 消息内容
     * @param isOwnMessage 是否为自己发送的消息
     * @return 消息气泡面板
     */
    private JPanel createMessageBubble(String content, boolean isOwnMessage) {
        // 使用MessageBubble类创建气泡
        MessageBubble bubble = new MessageBubble(
            content, 
            isOwnMessage, 
            isOwnMessage ? sentBubbleColor : receivedBubbleColor,
            isOwnMessage ? Color.WHITE : textColor,
            textFont
        );
        
        // 设置气泡监听器
        bubble.setMessageBubbleListener(new MessageBubble.MessageBubbleListener() {
            @Override
            public void onBubbleClicked(MessageBubble bubble) {
                // 可以在这里处理气泡点击事件
            }
            
            @Override
            public void onBubbleLongPressed(MessageBubble bubble) {
                // 可以在这里处理气泡长按事件
            }
            
            @Override
            public void onBubbleDoubleClicked(MessageBubble bubble) {
                // 可以在这里处理气泡双击事件
            }
            
            @Override
            public void onTextSelected(MessageBubble bubble, String selectedText) {
                // 可以在这里处理文本选中事件
            }
        });
        
        return bubble;
    }
    
    /**
     * 滚动到底部
     */
    public void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }
    
    /**
     * 清空聊天面板
     */
    public void clear() {
        contentPanel.removeAll();
        contentPanel.revalidate();
        contentPanel.repaint();
        lastTimestamp = "";
    }
    
    /**
     * 获取内容面板
     * @return 内容面板
     */
    public JPanel getContentPanel() {
        return contentPanel;
    }
    
    /**
     * 获取滚动面板
     * @return 滚动面板
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    /**
     * 添加系统消息
     * @param message 系统消息内容
     */
    public void addSystemMessage(String message) {
        JPanel messageRow = new JPanel(new BorderLayout());
        messageRow.setOpaque(false);
        messageRow.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JLabel systemLabel = new JLabel(message, SwingConstants.CENTER);
        systemLabel.setFont(new Font(textFont.getName(), Font.ITALIC, textFont.getSize() - 1));
        systemLabel.setForeground(secondaryTextColor);
        systemLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        messageRow.add(systemLabel, BorderLayout.CENTER);
        
        contentPanel.add(messageRow);
        contentPanel.revalidate();
        contentPanel.repaint();
        scrollToBottom();
    }
}



