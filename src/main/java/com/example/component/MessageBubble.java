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
        
        // 减小文本区域的内边距，避免额外的高度 - 调整为更小的内边距
        textArea.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        
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
        
        // 添加鼠标事件监听器，用于点击和长按
        addMouseListeners();
    }

    /**
     * 添加鼠标事件监听器
     */
    private void addMouseListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            private Timer longPressTimer;
            private boolean isDragging = false;
            
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                    return;
                }
                
                isDragging = false;
                
                // 创建长按定时器
                longPressTimer = new Timer(600, evt -> {
                    longPressTimer.stop();
                    if (!isDragging) {
                        fireBubbleLongPressed();
                    }
                });
                longPressTimer.setRepeats(false);
                longPressTimer.start();
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                isDragging = true;
                if (longPressTimer != null) {
                    longPressTimer.stop();
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                    return;
                }
                
                if (longPressTimer != null) {
                    longPressTimer.stop();
                }
                
                if (!isDragging) {
                    // 检查是否是双击
                    if (e.getClickCount() == 2) {
                        fireBubbleDoubleClicked();
                    } else if (e.getClickCount() == 1) {
                        fireBubbleClicked();
                    }
                }
            }
            
            private void showPopupMenu(MouseEvent e) {
                JPopupMenu popupMenu = textArea.getComponentPopupMenu();
                if (popupMenu != null) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
        
        // 添加鼠标监听器
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        
        // 确保文本区域也有相同的鼠标监听器
        textArea.addMouseListener(mouseAdapter);
        textArea.addMouseMotionListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 设置气泡颜色
        g2.setColor(bubbleColor);
        
        // 创建圆角矩形
        int radius = 16;
        RoundRectangle2D roundRect = new RoundRectangle2D.Float(
            0, 0, getWidth(), getHeight(), radius, radius
        );
        g2.fill(roundRect);
        
        // 如果是翻译气泡，添加一个微妙的边框
        if (getClientProperty("isTranslation") != null) {
            g2.setColor(new Color(0, 0, 0, 20));
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(roundRect);
        }
        
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
        
        // 转发消息
        JMenuItem forwardItem = new JMenuItem("转发");
        forwardItem.addActionListener(e -> {
            forwardBubbleContent();
        });
        popupMenu.add(forwardItem);
        
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
        
        // 如果是翻译气泡，不再翻译
        if (getClientProperty("isTranslation") != null && (boolean) getClientProperty("isTranslation")) {
            return;
        }
        
        // 标记正在翻译
        putClientProperty("translating", true);
        
        // 添加加载指示器或提示
        JLabel loadingLabel = new JLabel("正在翻译...");
        loadingLabel.setFont(new Font(textFont.getName(), Font.ITALIC, textFont.getSize() - 1));
        loadingLabel.setForeground(Color.GRAY);
        
        // 获取当前气泡所在的容器
        Container parent = getParent();
        if (parent != null) {
            parent.add(loadingLabel);
            parent.revalidate();
            parent.repaint();
        }
        
        // 创建一个SwingWorker来执行翻译，避免阻塞UI线程
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    // 使用AIService翻译文本
                    AIService aiService = new AIService();
                    System.out.println("开始翻译文本: " + content);
                    String result = aiService.translateText(content, "zh-CN");
                    System.out.println("翻译结果: " + result);
                    return result;
                } catch (Exception e) {
                    System.err.println("翻译出错: " + e.getMessage());
                    e.printStackTrace();
                    return "翻译失败: " + e.getMessage();
                }
            }
            
            @Override
            protected void done() {
                try {
                    // 移除加载指示器
                    if (parent != null) {
                        parent.remove(loadingLabel);
                        parent.revalidate();
                        parent.repaint();
                    }
                    
                    String translatedText = get();
                    if (translatedText != null && !translatedText.isEmpty()) {
                        // 创建翻译结果气泡
                        System.out.println("显示翻译结果: " + translatedText);
                        createTranslationBubble(translatedText);
                    } else {
                        System.err.println("翻译结果为空");
                        JOptionPane.showMessageDialog(null, "翻译失败: 结果为空", "翻译错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    System.err.println("处理翻译结果时出错: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "翻译失败: " + e.getMessage(), "翻译错误", JOptionPane.ERROR_MESSAGE);
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
        System.out.println("创建翻译气泡: " + translatedText);
        
        // 获取当前气泡所在的容器
        Container parent = getParent();
        if (parent == null) {
            System.err.println("无法获取父容器");
            return;
        }
        
        // 创建翻译气泡 - 使用更精致的样式
        Color translationBubbleColor;
        Color translationTextColor;
        
        if (isOwnMessage) {
            // 自己发送的消息的翻译 - 使用原气泡颜色的浅色版本
            translationBubbleColor = new Color(
                Math.min(bubbleColor.getRed() + 40, 255),
                Math.min(bubbleColor.getGreen() + 40, 255),
                Math.min(bubbleColor.getBlue() + 40, 255),
                220
            );
            translationTextColor = textColor;
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
        
        // 尝试直接在父容器中添加翻译气泡
        if (parent instanceof JPanel) {
            JPanel panel = (JPanel) parent;
            
            // 获取当前气泡在父容器中的索引
            int index = -1;
            Component[] components = panel.getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i] == this) {
                    index = i;
                    break;
                }
            }
            
            if (index != -1) {
                System.out.println("在父容器中找到当前气泡，索引: " + index);
                
                // 检查是否已经有翻译气泡
                boolean hasTranslation = false;
                if (index + 1 < components.length) {
                    Component nextComponent = components[index + 1];
                    if (nextComponent instanceof MessageBubble && 
                        ((MessageBubble) nextComponent).getClientProperty("isTranslation") != null) {
                        // 替换现有的翻译气泡
                        System.out.println("替换现有翻译气泡");
                        panel.remove(nextComponent);
                        panel.add(translationBubble, index + 1);
                        hasTranslation = true;
                    }
                }
                
                // 如果没有现有的翻译气泡，添加新的
                if (!hasTranslation) {
                    System.out.println("添加新翻译气泡");
                    
                    // 创建一个包装面板，添加一些间距
                    JPanel wrapperPanel = new JPanel(new BorderLayout());
                    wrapperPanel.setOpaque(false);
                    wrapperPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
                    wrapperPanel.add(translationBubble, BorderLayout.CENTER);
                    
                    panel.add(wrapperPanel, index + 1);
                }
                
                // 重新布局
                panel.revalidate();
                panel.repaint();
                
                // 尝试找到最顶层的容器并滚动到底部
                Container topContainer = panel;
                while (topContainer.getParent() != null) {
                    topContainer = topContainer.getParent();
                    if (topContainer instanceof JScrollPane) {
                        System.out.println("找到滚动面板，滚动到底部");
                        JScrollPane scrollPane = (JScrollPane) topContainer;
                        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                        SwingUtilities.invokeLater(() -> {
                            verticalBar.setValue(verticalBar.getMaximum());
                        });
                        break;
                    }
                }
                
                return;
            }
        }
        
        // 如果直接添加失败，尝试找到消息行并添加
        Container messageRow = findMessageRow(parent);
        if (messageRow != null) {
            System.out.println("找到消息行");
            
            // 获取消息行所在的容器
            Container contentPanel = messageRow.getParent();
            if (contentPanel != null) {
                System.out.println("找到内容面板");
                
                // 获取消息行在内容面板中的索引
                int rowIndex = -1;
                Component[] components = contentPanel.getComponents();
                for (int i = 0; i < components.length; i++) {
                    if (components[i] == messageRow) {
                        rowIndex = i;
                        break;
                    }
                }
                
                if (rowIndex != -1) {
                    System.out.println("找到消息行索引: " + rowIndex);
                    
                    // 创建新的消息行，用于放置翻译气泡
                    JPanel translationRow = new JPanel(new BorderLayout());
                    translationRow.setOpaque(false);
                    
                    // 创建内部面板，用于控制气泡位置
                    JPanel innerPanel = new JPanel(new FlowLayout(isOwnMessage ? FlowLayout.RIGHT : FlowLayout.LEFT, 5, 0));
                    innerPanel.setOpaque(false);
                    innerPanel.add(translationBubble);
                    
                    // 添加到消息行
                    if (isOwnMessage) {
                        translationRow.add(innerPanel, BorderLayout.EAST);
                    } else {
                        translationRow.add(innerPanel, BorderLayout.WEST);
                    }
                    
                    // 检查是否已经有翻译气泡
                    boolean hasTranslation = false;
                    if (rowIndex + 1 < components.length) {
                        Component nextComponent = components[rowIndex + 1];
                        if (isTranslationContainer(nextComponent)) {
                            hasTranslation = true;
                            contentPanel.remove(rowIndex + 1);
                        }
                    }
                    
                    // 添加翻译行
                    contentPanel.add(translationRow, rowIndex + 1);
                    contentPanel.revalidate();
                    contentPanel.repaint();
                    
                    // 尝试滚动到底部
                    scrollToBottom(contentPanel);
                    
                    return;
                }
            }
        }
        
        System.err.println("无法添加翻译气泡，所有尝试都失败了");
    }

    /**
     * 查找消息行
     * @param component 起始组件
     * @return 消息行容器，如果找不到则返回null
     */
    private Container findMessageRow(Container component) {
        // 检查当前组件是否是消息行
        if (component.getLayout() instanceof FlowLayout || component.getLayout() instanceof BorderLayout) {
            // 检查是否包含头像和消息气泡
            boolean hasAvatar = false;
            boolean hasBubble = false;
            
            Component[] components = component.getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    hasAvatar = true;
                } else if (comp instanceof JPanel) {
                    // 检查是否包含消息气泡
                    Component[] subComps = ((JPanel) comp).getComponents();
                    for (Component subComp : subComps) {
                        if (subComp instanceof MessageBubble) {
                            hasBubble = true;
                            break;
                        }
                    }
                }
            }
            
            if (hasAvatar || hasBubble) {
                return component;
            }
        }
        
        // 递归检查父容器
        if (component.getParent() != null) {
            return findMessageRow(component.getParent());
        }
        
        return null;
    }

    /**
     * 检查组件是否包含翻译气泡
     * @param component 要检查的组件
     * @return 是否包含翻译气泡
     */
    private boolean isTranslationContainer(Component component) {
        if (!(component instanceof Container)) {
            return false;
        }
        
        if (component instanceof MessageBubble && 
            ((MessageBubble) component).getClientProperty("isTranslation") != null) {
            return true;
        }
        
        // 递归检查所有子组件
        Component[] components = ((Container) component).getComponents();
        for (Component comp : components) {
            if (isTranslationContainer(comp)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 尝试滚动到底部
     * @param component 起始组件
     */
    private void scrollToBottom(Container component) {
        // 尝试找到滚动面板
        Container container = component;
        while (container != null) {
            if (container instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) container;
                JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
                SwingUtilities.invokeLater(() -> {
                    verticalBar.setValue(verticalBar.getMaximum());
                });
                System.out.println("找到滚动面板，滚动到底部");
                return;
            }
            container = container.getParent();
        }
        
        System.out.println("未找到滚动面板");
    }

    /**
     * 打印组件层次结构，用于调试
     * @param component 要打印的组件
     * @param indent 缩进
     */
    private void printComponentHierarchy(Component component, String indent) {
        System.out.println(indent + component.getClass().getSimpleName() + 
                          " [" + component.getWidth() + "x" + component.getHeight() + "]");
        
        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                printComponentHierarchy(child, indent + "  ");
            }
        }
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
        
        /**
         * 当气泡内容被转发时调用
         * @param bubble 气泡
         * @param content 要转发的内容
         */
        default void onBubbleForwarded(MessageBubble bubble, String content) {}
    }

    /**
     * 添加翻译指示器
     */
    public void addTranslationIndicator() {
        // 创建一个小图标，表示这是翻译结果
        JLabel translationIcon = new JLabel("\uD83D\uDD24"); // 使用放大镜Unicode表情
        translationIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        translationIcon.setForeground(new Color(120, 120, 120));
        translationIcon.setToolTipText("翻译结果");
        
        // 创建一个面板，包含图标和"翻译"文本
        JPanel indicatorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        indicatorPanel.setOpaque(false);
        indicatorPanel.add(translationIcon);
        
        JLabel translationLabel = new JLabel("翻译");
        translationLabel.setFont(new Font(textFont.getName(), Font.ITALIC, 10));
        translationLabel.setForeground(new Color(120, 120, 120));
        indicatorPanel.add(translationLabel);
        
        // 将指示器添加到气泡顶部
        add(indicatorPanel, BorderLayout.NORTH);
    }

    /**
     * 转发气泡内容
     */
    public void forwardBubbleContent() {
        if (bubbleListener != null) {
            bubbleListener.onBubbleForwarded(this, content);
        } else {
            // 如果没有设置监听器，显示转发对话框
            showForwardDialog();
        }
    }

    /**
     * 显示转发对话框
     */
    private void showForwardDialog() {
        // 创建用户选择对话框
        JDialog forwardDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "转发消息", true);
        forwardDialog.setLayout(new BorderLayout());
        forwardDialog.setSize(300, 400);
        forwardDialog.setLocationRelativeTo(this);
        
        // 创建用户列表面板
        JPanel userListPanel = new JPanel();
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));
        userListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("选择转发对象:");
        titleLabel.setFont(new Font(textFont.getName(), Font.BOLD, textFont.getSize()));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        userListPanel.add(titleLabel);
        userListPanel.add(Box.createVerticalStrut(10));
        
        // 这里应该添加用户列表，但由于我们没有直接访问用户列表，
        // 所以这部分需要通过监听器来实现
        JLabel placeholderLabel = new JLabel("请设置监听器以获取用户列表");
        placeholderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        userListPanel.add(placeholderLabel);
        
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
}

