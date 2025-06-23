package com.example.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import com.example.service.AIService;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingWorker;

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

    /**
     * 创建文本右键菜单
     * @param textArea 文本区域
     * @return 右键菜单
     */
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
        
        // 翻译文本
        JMenuItem translateItem = new JMenuItem("翻译");
        translateItem.addActionListener(e -> {
            translateBubbleContent();
        });
        popupMenu.add(translateItem);
        
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
     * 翻译气泡内容
     */
    public void translateBubbleContent() {
        // 如果已经触发了翻译，不再重复翻译
        if (getClientProperty("translating") != null && (boolean) getClientProperty("translating")) {
            return;
        }
        
        // 标记正在翻译
        putClientProperty("translating", true);
        
        // 创建一个SwingWorker来执行翻译，避免阻塞UI线程
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // 使用AIService翻译文本
                AIService aiService = new AIService();
                return aiService.translateText(content, "zh-CN");
            }
            
            @Override
            protected void done() {
                try {
                    String translatedText = get();
                    if (translatedText != null && !translatedText.isEmpty()) {
                        // 创建翻译结果气泡
                        createTranslationBubble(translatedText);
                    }
                } catch (Exception e) {
                    System.err.println("翻译出错: " + e.getMessage());
                } finally {
                    // 标记翻译完成
                    putClientProperty("translating", false);
                }
            }
        }.execute();
    }

    /**
     * 创建翻译结果气泡
     * @param translatedText 翻译后的文本
     */
    private void createTranslationBubble(String translatedText) {
        // 获取当前气泡所在的容器
        Container parent = getParent();
        if (parent == null) return;
        
        // 创建翻译气泡 - 使用浅色调
        Color translationBubbleColor = isOwnMessage ? 
                new Color(bubbleColor.getRed(), bubbleColor.getGreen(), bubbleColor.getBlue(), 200) : 
                new Color(230, 230, 230);
        
        MessageBubble translationBubble = new MessageBubble(
                translatedText,
                isOwnMessage,
                translationBubbleColor,
                textColor,
                new Font(textFont.getName(), Font.ITALIC, textFont.getSize() - 1)
        );
        
        // 添加标识，防止翻译气泡再次被翻译
        translationBubble.putClientProperty("isTranslation", true);
        
        // 获取当前气泡在父容器中的索引
        int index = -1;
        Component[] components = parent.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] == this) {
                index = i;
                break;
            }
        }
        
        // 如果找到索引，在当前气泡后添加翻译气泡
        if (index != -1 && parent instanceof JPanel) {
            JPanel panel = (JPanel) parent;
            
            // 检查是否已经有翻译气泡
            boolean hasTranslation = false;
            if (index + 1 < components.length) {
                Component nextComponent = components[index + 1];
                if (nextComponent instanceof MessageBubble && 
                    ((MessageBubble) nextComponent).getClientProperty("isTranslation") != null) {
                    // 替换现有的翻译气泡
                    panel.remove(nextComponent);
                    panel.add(translationBubble, index + 1);
                    hasTranslation = true;
                }
            }
            
            // 如果没有现有的翻译气泡，添加新的
            if (!hasTranslation) {
                panel.add(translationBubble, index + 1);
            }
            
            // 重新布局
            panel.revalidate();
            panel.repaint();
            
            // 如果在ChatPanel中，滚动到底部
            Container topContainer = SwingUtilities.getAncestorOfClass(ChatPanel.class, panel);
            if (topContainer instanceof ChatPanel) {
                ((ChatPanel) topContainer).scrollToBottom();
            }
        }
    }

    /**
     * 设置消息气泡监听器
     * @param listener 消息气泡监听器
     */
    public void setMessageBubbleListener(MessageBubbleListener listener) {
        this.bubbleListener = listener;
        
        // 添加鼠标监听器来触发事件
        this.addMouseListener(new MouseAdapter() {
            private long pressStartTime;
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    fireBubbleClicked();
                } else if (e.getClickCount() == 2) {
                    fireBubbleDoubleClicked();
                }
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                pressStartTime = System.currentTimeMillis();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                long pressDuration = System.currentTimeMillis() - pressStartTime;
                if (pressDuration > 500) { // 长按超过500毫秒
                    fireBubbleLongPressed();
                }
            }
        });
        
        // 监听文本选择
        if (textArea != null) {
            textArea.addCaretListener(e -> {
                String selectedText = textArea.getSelectedText();
                if (selectedText != null && !selectedText.isEmpty() && bubbleListener != null) {
                    bubbleListener.onTextSelected(MessageBubble.this, selectedText);
                }
            });
        }
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

