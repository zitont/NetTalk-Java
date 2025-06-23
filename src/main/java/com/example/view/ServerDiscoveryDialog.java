package com.example.view;

import com.example.model.Settings;
import com.example.util.ServerDiscovery;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

public class ServerDiscoveryDialog extends JDialog {
    // 颜色方案
    private static final Color PRIMARY_COLOR = new Color(64, 123, 255);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color BORDER_COLOR = new Color(222, 226, 230);
    
    // 字体
    private static final Font CHINESE_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    
    private DefaultListModel<String> serverListModel;
    private JList<String> serverList;
    private JButton refreshButton;
    private JButton connectButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private boolean isSearching = false;
    
    private String selectedServer = null;
    
    public ServerDiscoveryDialog(Frame owner) {
        super(owner, "局域网服务器列表", true);
        initUI();
        searchServers();
    }
    
    private void initUI() {
        setSize(400, 350);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // 创建标题
        JLabel titleLabel = new JLabel("可用的服务器");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        
        // 创建服务器列表
        serverListModel = new DefaultListModel<>();
        serverList = new JList<>(serverListModel);
        serverList.setFont(CHINESE_FONT);
        serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serverList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(serverList);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        // 创建状态标签
        statusLabel = new JLabel("正在搜索服务器...");
        statusLabel.setFont(CHINESE_FONT);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        refreshButton = new JButton("刷新");
        refreshButton.setFont(CHINESE_FONT);
        refreshButton.addActionListener(e -> searchServers());
        
        connectButton = new JButton("连接");
        connectButton.setFont(CHINESE_FONT);
        connectButton.setEnabled(false);
        connectButton.addActionListener(e -> {
            if (serverList.getSelectedIndex() != -1) {
                selectedServer = serverList.getSelectedValue();
                dispose();
            }
        });
        
        cancelButton = new JButton("取消");
        cancelButton.setFont(CHINESE_FONT);
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(connectButton);
        buttonPanel.add(cancelButton);
        
        // 添加选择监听器
        serverList.addListSelectionListener(e -> {
            connectButton.setEnabled(serverList.getSelectedIndex() != -1);
        });
        
        // 添加组件到主面板
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // 添加到对话框
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void searchServers() {
        if (isSearching) return;
        
        isSearching = true;
        serverListModel.clear();
        statusLabel.setText("正在搜索服务器...");
        refreshButton.setEnabled(false);
        
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return ServerDiscovery.discoverServers();
            }
            
            @Override
            protected void done() {
                try {
                    List<String> servers = get();
                    for (String server : servers) {
                        serverListModel.addElement(server);
                    }
                    
                    if (servers.isEmpty()) {
                        statusLabel.setText("未找到服务器");
                    } else {
                        statusLabel.setText("找到 " + servers.size() + " 个服务器");
                    }
                } catch (Exception e) {
                    statusLabel.setText("搜索出错: " + e.getMessage());
                } finally {
                    isSearching = false;
                    refreshButton.setEnabled(true);
                }
            }
        }.execute();
    }
    
    /**
     * 获取选择的服务器地址
     * @return 服务器地址，格式为 "ip:port"，如果未选择则返回null
     */
    public String getSelectedServer() {
        return selectedServer;
    }
}