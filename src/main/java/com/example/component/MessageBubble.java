package com.example.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

/**
 * 消息气泡组件
 */
public class MessageBubble extends JPanel {
    private static final int MAX_BUBBLE_WIDTH = 400;   // 最大气泡宽度
    private static final int MIN_BUBBLE_HEIGHT = 35;   // 最小气泡高度
    private static final int PADDING_VERTICAL = 16;    // 垂直内边距总和
    private static final int PADDING_HORIZONTAL = 24;  // 水平内边距总和
    private static final int PARAGRAPH_SPACING = 8;    // 段落间距
    
    private final String content;
    private final boolean isOwnMessage;
    private final Color bubbleColor;
    private final Color textColor;
    private final Font textFont;
    private JTextArea textArea;
    
    // 消息气泡监听器
    private MessageBubbleListener bubbleListener;

    /**
     * 创建消息气泡
     * @param content 消息内容
     * @param isOwnMessage 是否是自己发送的消息
     * @param bubbleColor 气泡背景色
     * @param textColor 文本颜色
     * @param textFont 文本字体
     */
    public MessageBubble(String content, boolean isOwnMessage, Color bubbleColor, Color textColor, Font textFont) {
        this.content = content;
        this.isOwnMessage = isOwnMessage;
        this.bubbleColor = bubbleColor;
        this.textColor = textColor;
        this.textFont = textFont;
        
        initUI();
    }

    private void initUI() {
        setOpaque(false);
        setLayout(new BorderLayout());
        
        // 处理段落间距
        String processedContent = processParagraphs(content);
        
        // 判断是否为短消息 - 更严格的判断
        final boolean isShortMessage = content.length() <= 30 && !content.contains("\n");
        
        // 创建文本区域 - 可选择
        textArea = new JTextArea(processedContent);
        textArea.setFont(textFont);
        textArea.setForeground(textColor);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false); // 不可编辑，但可选择
        textArea.setFocusable(true); // 允许获取焦点以便选择
        textArea.setOpaque(false);
        
        // 减小文本区域的内边距，避免额外的高度
        textArea.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        
        // 添加右键菜单
        JPopupMenu popupMenu = createTextPopupMenu(textArea);
        textArea.setComponentPopupMenu(popupMenu);
        
        // 短消息不自动换行，长消息自动换行
        textArea.setLineWrap(!isShortMessage);
        
        // 计算文本区域的首选大小
        FontMetrics fm = textArea.getFontMetrics(textFont);
        int textWidth = 0;
        int textHeight = 0;
        
        // 计算文本的实际宽度和高度
        if (isShortMessage) {
            // 短消息：直接计算文本宽度
            textWidth = fm.stringWidth(content) + PADDING_HORIZONTAL;
            // 短消息高度 = 单行文本高度 + 内边距
            textHeight = fm.getHeight() + PADDING_VERTICAL;
        } else {
            // 长消息：使用固定宽度
            textWidth = MAX_BUBBLE_WIDTH;
            
            // 使用优化的换行算法计算行数
            int effectiveWidth = MAX_BUBBLE_WIDTH - PADDING_HORIZONTAL;
            int lineCount = calculateLineCount(processedContent, fm, effectiveWidth);
            
            // 计算段落数量
            int paragraphCount = countParagraphs(content);
            
            // 计算总高度 = 行高 * 行数 + 段落间距 + 内边距
            textHeight = fm.getHeight() * lineCount + 
                        (paragraphCount > 1 ? (paragraphCount - 1) * PARAGRAPH_SPACING : 0) + 
                        PADDING_VERTICAL;
        }
        
        // 确保最小高度
        textHeight = Math.max(textHeight, MIN_BUBBLE_HEIGHT);
        
        // 设置气泡大小
        Dimension bubbleSize = new Dimension(textWidth, textHeight);
        setPreferredSize(bubbleSize);
        setMinimumSize(bubbleSize);
        setMaximumSize(bubbleSize);
        
        // 将文本区域添加到气泡中
        add(textArea, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(bubbleColor);
        
        // 创建圆角矩形
        int radius = 16;
        RoundRectangle2D roundRect = new RoundRectangle2D.Float(
            0, 0, getWidth(), getHeight(), radius, radius
        );
        g2.fill(roundRect);
        g2.dispose();
    }

    // 处理段落间距
    private String processParagraphs(String text) {
        // 将连续的换行符替换为单个换行符，确保段落间有一致的间距
        return text.replaceAll("\\n{2,}", "\n\n");
    }

    // 计算段落数量
    private int countParagraphs(String text) {
        if (text == null || text.isEmpty()) {
            return 1;
        }
        // 按换行符分割，计算段落数
        String[] paragraphs = text.split("\\n");
        return paragraphs.length;
    }

    // 创建文本右键菜单
    private JPopupMenu createTextPopupMenu(JTextArea textArea) {
        JPopupMenu popupMenu = new JPopupMenu();
        
        // 复制选中文本
        JMenuItem copyItem = new JMenuItem("复制");
        copyItem.addActionListener(e -> {
            textArea.copy();
        });
        popupMenu.add(copyItem);
        
        // 复制全部文本
        JMenuItem copyAllItem = new JMenuItem("复制全部");
        copyAllItem.addActionListener(e -> {
            textArea.selectAll();
            textArea.copy();
        });
        popupMenu.add(copyAllItem);
        
        // 分隔线
        popupMenu.addSeparator();
        
        // 选择全部
        JMenuItem selectAllItem = new JMenuItem("全选");
        selectAllItem.addActionListener(e -> {
            textArea.selectAll();
        });
        popupMenu.add(selectAllItem);
        
        return popupMenu;
    }

    /**
     * 设置消息气泡监听器
     * @param listener 消息气泡监听器
     */
    public void setMessageBubbleListener(MessageBubbleListener listener) {
        this.bubbleListener = listener;
    }
    
    /**
     * 添加自定义菜单项
     * @param menuText 菜单项文本
     * @param actionListener 点击事件监听器
     */
    public void addCustomMenuItem(String menuText, ActionListener actionListener) {
        if (textArea != null && textArea.getComponentPopupMenu() != null) {
            JPopupMenu popupMenu = textArea.getComponentPopupMenu();
            
            // 如果已有菜单项，添加分隔线
            if (popupMenu.getComponentCount() > 0) {
                popupMenu.addSeparator();
            }
            
            JMenuItem customItem = new JMenuItem(menuText);
            customItem.addActionListener(actionListener);
            popupMenu.add(customItem);
        }
    }
    
    /**
     * 获取文本区域组件
     * @return 文本区域组件
     */
    public JTextArea getTextArea() {
        return textArea;
    }

    /**
     * 获取消息内容
     * @return 消息内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 是否是自己发送的消息
     * @return 是否是自己发送的消息
     */
    public boolean isOwnMessage() {
        return isOwnMessage;
    }
    
    /**
     * 获取气泡背景色
     * @return 气泡背景色
     */
    public Color getBubbleColor() {
        return bubbleColor;
    }
    
    /**
     * 获取文本颜色
     * @return 文本颜色
     */
    public Color getTextColor() {
        return textColor;
    }
    
    /**
     * 获取文本字体
     * @return 文本字体
     */
    public Font getTextFont() {
        return textFont;
    }
    
    /**
     * 触发气泡点击事件
     */
    protected void fireBubbleClicked() {
        if (bubbleListener != null) {
            bubbleListener.onBubbleClicked(this);
        }
    }
    
    /**
     * 触发气泡长按事件
     */
    protected void fireBubbleLongPressed() {
        if (bubbleListener != null) {
            bubbleListener.onBubbleLongPressed(this);
        }
    }
    
    /**
     * 触发气泡双击事件
     */
    protected void fireBubbleDoubleClicked() {
        if (bubbleListener != null) {
            bubbleListener.onBubbleDoubleClicked(this);
        }
    }

    // 优化的中文换行算法 - 计算文本换行后的行数
    private int calculateLineCount(String text, FontMetrics fm, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return 1;
        }
        
        int lineCount = 0;
        int startIndex = 0;
        int textLength = text.length();
        
        while (startIndex < textLength) {
            int breakPoint = findBreakPoint(text, startIndex, fm, maxWidth);
            lineCount++;
            startIndex = breakPoint;
        }
        
        return lineCount;
    }
    
    // 优化的中文换行算法 - 找出文本在指定宽度内的断点
    private int findBreakPoint(String text, int startIndex, FontMetrics fm, int availableWidth) {
        int textLength = text.length();
        if (startIndex >= textLength) return textLength;
        
        // 如果整行文本宽度小于可用宽度，直接返回文本长度
        if (fm.stringWidth(text.substring(startIndex)) <= availableWidth) {
            return textLength;
        }
        
        // 二分查找找出最佳断点
        int low = startIndex;
        int high = textLength - 1;
        int best = startIndex;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            String substring = text.substring(startIndex, mid + 1);
            
            if (fm.stringWidth(substring) <= availableWidth) {
                best = mid + 1;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        
        // 优化中文断点 - 避免在标点符号后断行
        if (best > startIndex + 1 && best < textLength) {
            // 检查是否在标点符号后断行
            char charAtBreak = text.charAt(best - 1);
            char nextChar = text.charAt(best);
            
            // 中文标点符号列表
            String chinesePunctuation = "，。！？；：、（）【】《》";
            String westernPunctuation = ",.!?;:\"'()[]{}<>/\\";
            String allPunctuation = chinesePunctuation + westernPunctuation;
            
            // 如果断点前是标点符号，保持当前断点
            if (allPunctuation.indexOf(charAtBreak) >= 0) {
                return best;
            }
            
            // 如果断点处是标点符号，尝试将断点后移
            if (allPunctuation.indexOf(nextChar) >= 0 && best < textLength - 1) {
                // 将断点后移一位，包含标点符号
                return best + 1;
            }
        }
        
        // 如果找不到合适的断点，至少返回一个字符
        return Math.max(startIndex + 1, best);
    }
    
    /**
     * 消息气泡监听器接口
     */
    public interface MessageBubbleListener {
        /**
         * 当气泡被点击时调用
         * @param bubble 被点击的气泡
         */
        default void onBubbleClicked(MessageBubble bubble) {}
        
        /**
         * 当气泡被长按时调用
         * @param bubble 被长按的气泡
         */
        default void onBubbleLongPressed(MessageBubble bubble) {}
        
        /**
         * 当气泡被双击时调用
         * @param bubble 被双击的气泡
         */
        default void onBubbleDoubleClicked(MessageBubble bubble) {}
        
        /**
         * 当气泡内容被选中时调用
         * @param bubble 气泡
         * @param selectedText 选中的文本
         */
        default void onTextSelected(MessageBubble bubble, String selectedText) {}
    }
}

