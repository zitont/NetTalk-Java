package com.example.dao;

import com.example.model.Message;
import com.example.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 消息数据访问对象
 */
public class MessageDAO {

    /**
     * 存储离线消息
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param content 消息内容
     * @return 是否成功存储
     */
    public boolean storeOfflineMessage(Long senderId, Long receiverId, String content) {
        String sql = "INSERT INTO message (sender, receiver, message, ddate, `read`, reserved) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, senderId);
            pstmt.setLong(2, receiverId);
            pstmt.setString(3, content);
            // 使用当前日期
            pstmt.setDate(4, new java.sql.Date(System.currentTimeMillis()));
            pstmt.setInt(5, 2); // 2表示未读
            pstmt.setString(6, null); // 保留字段设为null
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("存储离线消息失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取用户的所有未读消息
     * @param receiverId 接收者ID
     * @return 未读消息列表
     */
    public List<Message> getUnreadMessages(Long receiverId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE receiver = ? AND `read` = 2 ORDER BY ddate ASC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, receiverId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Message message = createMessageFromResultSet(rs);
                messages.add(message);
            }
        } catch (SQLException e) {
            System.err.println("获取未读消息失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return messages;
    }

    /**
     * 获取离线消息统计（按发送者分组）
     * @param receiverId 接收者ID
     * @return 发送者ID -> 消息数量的映射
     */
    public Map<Long, Integer> getOfflineMessageStats(Long receiverId) {
        Map<Long, Integer> stats = new HashMap<>();
        String sql = "SELECT sender, COUNT(*) as count FROM message WHERE receiver = ? AND `read` = 2 GROUP BY sender";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, receiverId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Long senderId = rs.getLong("sender");
                Integer count = rs.getInt("count");
                stats.put(senderId, count);
            }
        } catch (SQLException e) {
            System.err.println("获取离线消息统计失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }

    /**
     * 获取来自特定发送者的离线消息
     * @param receiverId 接收者ID
     * @param senderId 发送者ID
     * @return 消息列表
     */
    public List<Message> getOfflineMessagesFromSender(Long receiverId, Long senderId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE receiver = ? AND sender = ? AND `read` = 2 ORDER BY ddate ASC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, receiverId);
            pstmt.setLong(2, senderId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Message message = createMessageFromResultSet(rs);
                messages.add(message);
            }
        } catch (SQLException e) {
            System.err.println("获取特定发送者的离线消息失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return messages;
    }

    /**
     * 处理离线消息（获取并标记为已读）
     * @param receiverId 接收者ID
     * @return 处理的消息列表
     */
    public List<Message> processOfflineMessages(Long receiverId) {
        List<Message> messages = getUnreadMessages(receiverId);
        
        if (!messages.isEmpty()) {
            List<Long> messageIds = new ArrayList<>();
            for (Message message : messages) {
                messageIds.add(message.getId());
            }
            markMessagesAsRead(messageIds);
        }
        
        return messages;
    }

    /**
     * 标记消息为已读
     * @param messageIds 消息ID列表
     * @return 是否成功
     */
    public boolean markMessagesAsRead(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return true;
        }
        
        StringBuilder sql = new StringBuilder("UPDATE message SET `read` = 1 WHERE _id IN (");
        for (int i = 0; i < messageIds.size(); i++) {
            sql.append("?");
            if (i < messageIds.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < messageIds.size(); i++) {
                pstmt.setLong(i + 1, messageIds.get(i));
            }
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("标记消息为已读失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取两个用户之间的消息历史
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @param limit 限制数量
     * @return 消息历史列表
     */
    public List<Message> getMessageHistory(Long userId1, Long userId2, int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE " +
                    "(sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) " +
                    "ORDER BY ddate DESC LIMIT ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId1);
            pstmt.setLong(2, userId2);
            pstmt.setLong(3, userId2);
            pstmt.setLong(4, userId1);
            pstmt.setInt(5, limit);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Message message = createMessageFromResultSet(rs);
                messages.add(message);
            }
        } catch (SQLException e) {
            System.err.println("获取消息历史失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return messages;
    }

    /**
     * 标记消息为已送达
     * @param messageIds 消息ID列表
     * @return 是否成功
     */
    public boolean markMessagesAsDelivered(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return true;
        }
        
        StringBuilder sql = new StringBuilder("UPDATE message SET reserved = 'delivered' WHERE _id IN (");
        for (int i = 0; i < messageIds.size(); i++) {
            sql.append("?");
            if (i < messageIds.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < messageIds.size(); i++) {
                pstmt.setLong(i + 1, messageIds.get(i));
            }
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("标记消息为已送达失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从ResultSet创建Message对象
     * @param rs ResultSet
     * @return Message对象
     * @throws SQLException SQL异常
     */
    private Message createMessageFromResultSet(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getLong("_id"));
        message.setSenderId(rs.getLong("sender"));
        message.setReceiverId(rs.getLong("receiver"));
        message.setContent(rs.getString("message"));
        
        // 将Date转换为LocalDateTime
        java.sql.Date date = rs.getDate("ddate");
        if (date != null) {
            message.setSentAt(date.toLocalDate().atStartOfDay());
        }
        
        // 根据read字段设置已读状态
        int readStatus = rs.getInt("read");
        message.setRead(readStatus == 1);
        
        // 由于新表没有is_delivered字段，默认设置为false
        message.setDelivered(false);
        
        return message;
    }
}
