package com.example.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;
import com.example.model.User;

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
        
        // 创建内容面板 - 使用BorderLayout而不是BoxLayout
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
        
        // 创建一个包装面板，用于控制对齐
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        
        JPanel messageRow = createMessageRow(content, isOwnMessage, username);
        
        // 根据消息类型添加到不同位置
        if (isOwnMessage) {
            wrapperPanel.add(messageRow, BorderLayout.EAST);
        } else {
            wrapperPanel.add(messageRow, BorderLayout.WEST);
        }
        
        contentPanel.add(wrapperPanel);
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
     * 创建消息行 - 简化版本，不使用复杂布局
     */
    private JPanel createMessageRow(String content, boolean isOwnMessage, String username) {
        JPanel messageRow = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 0));
        messageRow.setOpaque(false);
        
        // 创建头像
        JLabel avatar = createUserAvatar(isOwnMessage ? "我" : (username != null ? username : "?"), 28);
        
        // 创建包含用户名和气泡的容器
        JPanel contentContainer = new JPanel();
        contentContainer.setOpaque(false);
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        
        // 如果不是自己的消息且有用户名，添加用户名标签
        if (!isOwnMessage && username != null && !username.isEmpty()) {
            JLabel usernameLabel = new JLabel(username);
            usernameLabel.setFont(new Font(textFont.getName(), Font.BOLD, textFont.getSize() - 2));
            usernameLabel.setForeground(new Color(120, 120, 120));
            usernameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
            usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            contentContainer.add(usernameLabel);
        }
        
        // 创建消息气泡
        JPanel bubble = createMessageBubble(content, isOwnMessage);
        bubble.setAlignmentX(isOwnMessage ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        
        // 添加气泡到容器
        contentContainer.add(bubble);
        
        // 根据消息类型设置组件顺序
        if (isOwnMessage) {
            messageRow.add(contentContainer);
            messageRow.add(avatar);
        } else {
            messageRow.add(avatar);
            messageRow.add(contentContainer);
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
            
            @Override
            public void onBubbleForwarded(MessageBubble bubble, String content) {
                // 处理转发事件
                if (forwardListener != null) {
                    forwardListener.onMessageForward(content, bubble.isOwnMessage());
                }
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

    // 添加创建头像的方法
    private JLabel createUserAvatar(String name, int size) {
        String initial = name.substring(0, 1).toUpperCase();
        
        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // 绘制方形背景，带圆角
                g2.setColor(new Color(56, 129, 244)); // 使用主色调
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                
                // 手动绘制文本
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("微软雅黑", Font.BOLD, size / 2));
                
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
        
        // 确保头像大小固定
        avatar.setPreferredSize(new Dimension(size, size));
        avatar.setMinimumSize(new Dimension(size, size));
        avatar.setMaximumSize(new Dimension(size, size));
        avatar.setOpaque(false);
        
        return avatar;
    }

    /**
     * 添加翻译气泡
     * @param originalBubble 原始气泡
     * @param translatedText 翻译后的文本
     */
    public void addTranslationBubble(MessageBubble originalBubble, String translatedText) {
        System.out.println("ChatPanel.addTranslationBubble 被调用");
        
        // 获取原始气泡的属性
        boolean isOwnMessage = originalBubble.isOwnMessage();
        
        // 创建翻译气泡 - 使用更精致的样式
        Color translationBubbleColor;
        Color translationTextColor;
        
        if (isOwnMessage) {
            // 自己发送的消息的翻译 - 使用原气泡颜色的浅色版本
            translationBubbleColor = new Color(
                Math.min(sentBubbleColor.getRed() + 40, 255),
                Math.min(sentBubbleColor.getGreen() + 40, 255),
                Math.min(sentBubbleColor.getBlue() + 40, 255),
                220
            );
            translationTextColor = Color.WHITE;
        } else {
            // 他人发送的消息的翻译 - 使用浅灰色
            translationBubbleColor = new Color(240, 240, 240);
            translationTextColor = new Color(80, 80, 80);
        }
        
        // 创建翻译气泡，使用斜体和稍小的字体
        MessageBubble translationBubble = new MessageBubble(
                translatedText,
                isOwnMessage,
                translationBubbleColor,
                translationTextColor,
                new Font(textFont.getName(), Font.ITALIC, textFont.getSize() - 1)
        );
        
        // 添加标识，防止翻译气泡再次被翻译
        translationBubble.putClientProperty("isTranslation", true);
        
        // 添加翻译标记图标
        translationBubble.addTranslationIndicator();
        
        // 查找原始气泡所在的消息行
        Component messageRow = SwingUtilities.getAncestorOfClass(JPanel.class, originalBubble);
        while (messageRow != null && messageRow.getParent() != contentPanel) {
            messageRow = messageRow.getParent();
        }
        
        if (messageRow == null) {
            System.err.println("未找到原始消息行");
            return;
        }
        
        // 获取消息行在内容面板中的索引
        int rowIndex = -1;
        Component[] components = contentPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] == messageRow) {
                rowIndex = i;
                break;
            }
        }
        
        if (rowIndex == -1) {
            System.err.println("未找到消息行索引");
            return;
        }
        
        System.out.println("找到原始消息行索引: " + rowIndex);
        
        // 检查是否已经有翻译气泡
        boolean hasTranslation = false;
        int translationIndex = -1;
        
        // 查找下一个组件是否是翻译气泡
        if (rowIndex + 1 < components.length) {
            Component nextComponent = components[rowIndex + 1];
            // 检查是否包含翻译气泡
            if (isTranslationBubbleContainer(nextComponent)) {
                hasTranslation = true;
                translationIndex = rowIndex + 1;
                System.out.println("找到现有翻译气泡，索引: " + translationIndex);
            }
        }
        
        // 创建新的消息行，用于放置翻译气泡
        JPanel translationRow = new JPanel(new BorderLayout());
        translationRow.setOpaque(false);
        translationRow.setBorder(BorderFactory.createEmptyBorder(2, 0, 4, 0)); // 添加一些垂直间距
        
        // 创建内部面板，用于控制气泡位置
        JPanel innerPanel = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 0));
        innerPanel.setOpaque(false);
        
        // 如果不是自己的消息，添加一些左边距，与原始消息对齐
        if (!isOwnMessage) {
            innerPanel.setBorder(BorderFactory.createEmptyBorder(0, 36, 0, 0)); // 36px是头像宽度+间距
        }
        
        innerPanel.add(translationBubble);
        
        // 添加到消息行
        if (isOwnMessage) {
            translationRow.add(innerPanel, BorderLayout.EAST);
        } else {
            translationRow.add(innerPanel, BorderLayout.WEST);
        }
        
        // 如果已有翻译气泡，替换它
        if (hasTranslation) {
            System.out.println("替换现有翻译气泡");
            contentPanel.remove(translationIndex);
            contentPanel.add(translationRow, translationIndex);
        } else {
            // 否则，在原始消息行后添加新的翻译行
            System.out.println("添加新翻译气泡");
            contentPanel.add(translationRow, rowIndex + 1);
        }
        
        // 重新布局
        contentPanel.revalidate();
        contentPanel.repaint();
        
        // 滚动到底部
        scrollToBottom();
        System.out.println("翻译气泡添加完成");
    }

    /**
     * 检查组件是否包含翻译气泡
     * @param component 要检查的组件
     * @return 是否包含翻译气泡
     */
    private boolean isTranslationBubbleContainer(Component component) {
        if (!(component instanceof JPanel)) {
            return false;
        }
        
        // 递归检查所有子组件
        Component[] components = ((JPanel) component).getComponents();
        for (Component comp : components) {
            if (comp instanceof MessageBubble && 
                ((MessageBubble) comp).getClientProperty("isTranslation") != null) {
                return true;
            }
            
            if (comp instanceof JPanel) {
                if (isTranslationBubbleContainer(comp)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    // 转发监听器
    private ForwardMessageListener forwardListener;

    /**
     * 设置转发消息监听器
     * @param listener 转发消息监听器
     */
    public void setForwardMessageListener(ForwardMessageListener listener) {
        this.forwardListener = listener;
    }

    /**
     * 转发消息监听器接口
     */
    public interface ForwardMessageListener {
        /**
         * 当消息被转发时调用
         * @param content 消息内容
         * @param isOwnMessage 是否是自己的消息
         */
        void onMessageForward(String content, boolean isOwnMessage);
    }

    /**
     * 显示转发对话框
     * @param content 要转发的内容
     * @param userList 用户列表
     * @param forwardCallback 转发回调
     */
    public void showForwardDialog(String content, List<User> userList, BiConsumer<String, User> forwardCallback) {
        // 创建用户选择对话框
        JDialog forwardDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "转发消息", true);
        forwardDialog.setLayout(new BorderLayout());
        forwardDialog.setSize(300, 400);
        forwardDialog.setLocationRelativeTo(this);
        
        // 创建消息预览面板
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel previewLabel = new JLabel("消息预览:");
        previewLabel.setFont(new Font(textFont.getName(), Font.BOLD, textFont.getSize()));
        previewPanel.add(previewLabel, BorderLayout.NORTH);
        
        JTextArea previewText = new JTextArea(content);
        previewText.setEditable(false);
        previewText.setLineWrap(true);
        previewText.setWrapStyleWord(true);
        previewText.setFont(textFont);
        previewText.setRows(3);
        previewText.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        previewPanel.add(new JScrollPane(previewText), BorderLayout.CENTER);
        
        forwardDialog.add(previewPanel, BorderLayout.NORTH);
        
        // 创建用户列表面板
        JPanel userListPanel = new JPanel();
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));
        userListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("选择转发对象:");
        titleLabel.setFont(new Font(textFont.getName(), Font.BOLD, textFont.getSize()));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        userListPanel.add(titleLabel);
        userListPanel.add(Box.createVerticalStrut(10));
        
        // 添加用户列表
        if (userList != null && !userList.isEmpty()) {
            for (User user : userList) {
                JPanel userItem = createUserItem(user, content, forwardCallback, forwardDialog);
                userItem.setAlignmentX(Component.LEFT_ALIGNMENT);
                userListPanel.add(userItem);
                userListPanel.add(Box.createVerticalStrut(5));
            }
        } else {
            JLabel noUserLabel = new JLabel("没有可转发的用户");
            noUserLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            userListPanel.add(noUserLabel);
        }
        
        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(userListPanel);
        forwardDialog.add(scrollPane, BorderLayout.CENTER);
        
        // 添加按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> forwardDialog.dispose());
        buttonPanel.add(cancelButton);
        
        forwardDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // 显示对话框
        forwardDialog.setVisible(true);
    }

    /**
     * 创建用户项
     */
    private JPanel createUserItem(User user, String content, BiConsumer<String, User> forwardCallback, JDialog dialog) {
        JPanel userItem = new JPanel(new BorderLayout());
        userItem.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        userItem.setBackground(Color.WHITE);
        
        // 创建用户头像和名称
        JPanel userInfoPanel = new JPanel(new BorderLayout(10, 0));
        userInfoPanel.setOpaque(false);
        
        // 创建头像
        JLabel avatar = createUserAvatar(user.getName().substring(0, 1).toUpperCase(), 30);
        userInfoPanel.add(avatar, BorderLayout.WEST);
        
        // 创建用户名
        JLabel nameLabel = new JLabel(user.getName());
        nameLabel.setFont(textFont);
        userInfoPanel.add(nameLabel, BorderLayout.CENTER);
        
        userItem.add(userInfoPanel, BorderLayout.CENTER);
        
        // 创建转发按钮
        JButton forwardButton = new JButton("转发");
        forwardButton.addActionListener(e -> {
            if (forwardCallback != null) {
                forwardCallback.accept(content, user);
            }
            dialog.dispose();
        });
        userItem.add(forwardButton, BorderLayout.EAST);
        
        // 添加鼠标悬停效果
        userItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                userItem.setBackground(new Color(245, 245, 245));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                userItem.setBackground(Color.WHITE);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                forwardButton.doClick();
            }
        });
        
        return userItem;
    }
}
