package com.example.service;

import com.example.dao.MessageDAO;
import com.example.dao.UserDAO;
import com.example.model.Message;
import com.example.model.User;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * 离线消息服务类
 * 负责处理离线消息的同步、通知等功能
 */
public class OfflineMessageService {
    
    private final MessageDAO messageDAO;
    private final UserDAO userDAO;
    
    public OfflineMessageService() {
        this.messageDAO = new MessageDAO();
        this.userDAO = new UserDAO();
    }
    
    /**
     * 用户登录时同步离线消息
     * @param userId 用户ID
     * @return 离线消息同步结果
     */
    public OfflineMessageSyncResult syncOfflineMessages(Long userId) {
        try {
            // 获取所有未读消息
            List<Message> unreadMessages = messageDAO.getUnreadMessages(userId);
            
            // 获取离线消息统计
            Map<Long, Integer> messageStats = messageDAO.getOfflineMessageStats(userId);
            
            // 创建同步结果
            OfflineMessageSyncResult result = new OfflineMessageSyncResult();
            result.setUserId(userId);
            result.setUnreadMessages(unreadMessages);
            result.setMessageStats(messageStats);
            result.setTotalUnreadCount(unreadMessages.size());
            result.setSuccess(true);
            
            System.out.println("用户 " + userId + " 离线消息同步完成，共 " + unreadMessages.size() + " 条未读消息");
            
            return result;
        } catch (Exception e) {
            System.err.println("离线消息同步失败: " + e.getMessage());
            e.printStackTrace();
            
            OfflineMessageSyncResult result = new OfflineMessageSyncResult();
            result.setUserId(userId);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            return result;
        }
    }
    
    /**
     * 存储离线消息
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param content 消息内容
     * @return 是否成功
     */
    public boolean storeOfflineMessage(Long senderId, Long receiverId, String content) {
        try {
            boolean success = messageDAO.storeOfflineMessage(senderId, receiverId, content);
            if (success) {
                System.out.println("离线消息已存储: 从用户 " + senderId + " 到用户 " + receiverId);
            }
            return success;
        } catch (Exception e) {
            System.err.println("存储离线消息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 标记消息为已读
     * @param messageIds 消息ID列表
     * @return 是否成功
     */
    public boolean markMessagesAsRead(List<Long> messageIds) {
        try {
            boolean success = messageDAO.markMessagesAsRead(messageIds);
            if (success) {
                System.out.println("已将 " + messageIds.size() + " 条消息标记为已读");
            }
            return success;
        } catch (Exception e) {
            System.err.println("标记消息为已读失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取来自特定发送者的离线消息
     * @param receiverId 接收者ID
     * @param senderId 发送者ID
     * @return 离线消息列表
     */
    public List<Message> getOfflineMessagesFromSender(Long receiverId, Long senderId) {
        try {
            // 获取来自特定发送者的未读消息
            List<Message> messages = messageDAO.getOfflineMessagesFromSender(receiverId, senderId);
            
            System.out.println("获取用户 " + receiverId + " 来自用户 " + senderId + " 的离线消息，共 " + messages.size() + " 条");
            
            return messages;
        } catch (Exception e) {
            System.err.println("获取特定发送者的离线消息失败: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取用户名
     * @param userId 用户ID
     * @return 用户名
     */
    public String getUserName(Long userId) {
        try {
            // 使用UserDAO获取用户名
            String userName = userDAO.getUserNameById(userId);
            if (userName != null && !userName.isEmpty()) {
                return userName;
            }
            // 如果找不到，返回默认格式
            return "User" + userId;
        } catch (Exception e) {
            System.err.println("获取用户名失败: " + e.getMessage());
            return "User" + userId;
        }
    }
    
    /**
     * 离线消息同步结果类
     */
    public static class OfflineMessageSyncResult {
        private Long userId;
        private List<Message> unreadMessages;
        private Map<Long, Integer> messageStats;
        private int totalUnreadCount;
        private boolean success;
        private String errorMessage;
        
        // Getters and Setters
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public List<Message> getUnreadMessages() {
            return unreadMessages;
        }
        
        public void setUnreadMessages(List<Message> unreadMessages) {
            this.unreadMessages = unreadMessages;
        }
        
        public Map<Long, Integer> getMessageStats() {
            return messageStats;
        }
        
        public void setMessageStats(Map<Long, Integer> messageStats) {
            this.messageStats = messageStats;
        }
        
        public int getTotalUnreadCount() {
            return totalUnreadCount;
        }
        
        public void setTotalUnreadCount(int totalUnreadCount) {
            this.totalUnreadCount = totalUnreadCount;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        @Override
        public String toString() {
            return "OfflineMessageSyncResult{" +
                    "userId=" + userId +
                    ", totalUnreadCount=" + totalUnreadCount +
                    ", success=" + success +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        }
    }
}
