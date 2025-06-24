# NetTalk-Java 聊天应用程序

一个基于Java Swing的现代化聊天应用程序，支持实时通信、离线消息同步、AI翻译等功能。

## 功能特性

### 核心功能
- 🔐 **用户注册和登录系统** - 安全的用户认证机制
- 💬 **实时聊天** - 支持群聊和私聊
- 📱 **现代化UI** - 基于Swing的美观界面设计
- 🌐 **服务器发现** - 自动发现局域网内的服务器
- 📊 **在线状态** - 实时显示用户在线状态

### 离线消息功能 ✨
- 📥 **离线消息存储** - 用户离线时自动存储消息
- 🔄 **消息同步** - 用户登录时自动同步离线消息
- 📈 **消息统计** - 显示未读消息数量和发送者信息
- ✅ **已读状态** - 自动标记消息为已读/已送达

### 高级功能
- 🤖 **AI翻译** - 集成AI服务进行消息翻译
- 🔄 **消息转发** - 支持消息转发给其他用户
- ⚙️ **动态配置** - 支持运行时修改服务器设置
- 🔍 **服务器列表** - 管理和连接多个服务器

## 技术架构

### 后端技术
- **Java 8+** - 核心开发语言
- **Socket编程** - TCP/UDP网络通信
- **MySQL数据库** - 数据持久化存储
- **JDBC** - 数据库连接和操作
- **多线程** - 并发处理客户端连接

### 前端技术
- **Java Swing** - 图形用户界面
- **自定义UI组件** - 现代化界面设计
- **事件驱动** - 响应式用户交互

### 数据库设计
```sql
-- 用户表
CREATE TABLE user (
    _id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(40) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    photo LONGBLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 消息表
CREATE TABLE message (
    _id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    is_delivered BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender_id) REFERENCES user(_id),
    FOREIGN KEY (receiver_id) REFERENCES user(_id)
);
```

## 安装和配置

### 环境要求
- Java 8 或更高版本
- MySQL 5.7 或更高版本
- Maven 3.6 或更高版本

### 数据库配置
1. 创建MySQL数据库
2. 执行 `src/main/resources/db_script.sql` 创建表结构
3. 配置数据库连接信息

### 配置文件
编辑 `src/main/resources/config.properties`：

```properties
# 数据库配置
db.url=jdbc:mysql://localhost:3306/nettalk
db.user=your_username
db.password=your_password

# 服务器配置
server.host=localhost
server.port=8888
server.start=false

# AI翻译配置（可选）
ai.api.key=your_api_key
ai.api.url=https://api.example.com/v1/chat/completions
ai.max.tokens=50
```

### 编译和运行

#### 使用Maven
```bash
# 编译项目
mvn clean compile

# 运行应用程序
mvn exec:java -Dexec.mainClass="com.example.App"

# 打包为可执行JAR
mvn clean package
java -jar target/NetTalk-Java-1.0-SNAPSHOT-jar-with-dependencies.jar
```

#### 使用IDE
1. 导入项目到IDE（IntelliJ IDEA、Eclipse等）
2. 配置项目SDK为Java 8+
3. 运行 `com.example.App` 主类

## 使用指南

### 启动服务器
1. 在配置文件中设置 `server.start=true`
2. 或在设置界面中手动启动服务器
3. 服务器将在指定端口启动并开始监听连接

### 客户端连接
1. 启动应用程序
2. 注册新用户或使用现有账户登录
3. 应用程序会自动发现并连接到服务器
4. 连接成功后即可开始聊天

### 离线消息同步
1. 用户离线时，发送给该用户的消息会自动存储在数据库中
2. 用户重新登录时，系统会自动同步所有离线消息
3. 离线消息会在聊天界面中以特殊样式显示
4. 消息同步完成后会自动标记为已读

### 私聊功能
1. 在用户列表中双击用户名
2. 打开私聊窗口
3. 发送私人消息
4. 支持离线消息存储和同步

## 项目结构

```
src/main/java/com/example/
├── App.java                    # 应用程序入口
├── component/                  # UI组件
│   ├── ChatClient.java        # 聊天客户端组件
│   ├── ChatPanel.java         # 聊天面板
│   └── MessageBubble.java     # 消息气泡
├── controller/                 # 控制器
│   └── AuthController.java    # 认证控制器
├── dao/                       # 数据访问层
│   ├── MessageDAO.java        # 消息数据访问
│   └── UserDAO.java           # 用户数据访问
├── model/                     # 数据模型
│   ├── Message.java           # 消息模型
│   ├── Settings.java          # 设置模型
│   └── User.java              # 用户模型
├── service/                   # 服务层
│   ├── AIService.java         # AI翻译服务
│   ├── OfflineMessageService.java # 离线消息服务
│   └── SocketService.java     # Socket通信服务
├── util/                      # 工具类
│   ├── DBUtil.java            # 数据库工具
│   └── ServerDiscovery.java   # 服务器发现
└── view/                      # 视图层
    ├── LoginView.java         # 登录界面
    ├── MainView.java          # 主界面
    ├── PrivateChatView.java   # 私聊界面
    └── SettingsView.java      # 设置界面
```

## 开发指南

### 添加新功能
1. 在相应的包中创建新类
2. 遵循MVC架构模式
3. 使用DAO模式进行数据访问
4. 添加相应的测试用例

### 数据库操作
- 使用 `DBUtil.getConnection()` 获取数据库连接
- 所有数据库操作都应该在DAO类中实现
- 使用PreparedStatement防止SQL注入

### UI开发
- 继承现有的UI组件风格
- 使用SwingUtilities.invokeLater()更新UI
- 遵循响应式设计原则

### 离线消息开发指南

#### 核心组件
- **MessageDAO**: 处理消息的数据库操作
- **OfflineMessageService**: 离线消息业务逻辑
- **SocketService**: 服务器端消息处理
- **MainView**: 客户端消息显示

#### 消息流程
1. **发送消息**: 客户端 → SocketService → 在线用户/数据库存储
2. **离线存储**: SocketService → MessageDAO → 数据库
3. **登录同步**: 用户登录 → OfflineMessageService → 获取离线消息
4. **消息显示**: 离线消息 → MainView → 用户界面

#### 扩展离线消息功能
```java
// 添加新的消息类型
public class MessageDAO {
    public boolean storeMultimediaMessage(Long senderId, Long receiverId,
                                        String content, String mediaType) {
        // 实现多媒体消息存储
    }
}

// 扩展同步服务
public class OfflineMessageService {
    public void syncMessagesWithPagination(Long userId, int page, int size) {
        // 实现分页同步
    }
}
```

## 测试

### 运行测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=MessageDAOTest
```

### 测试覆盖
- 单元测试：DAO层和Service层
- 集成测试：数据库操作
- UI测试：手动测试

## 故障排除

### 常见问题

**连接数据库失败**
- 检查数据库服务是否启动
- 验证配置文件中的连接信息
- 确认数据库用户权限

**服务器启动失败**
- 检查端口是否被占用
- 验证防火墙设置
- 查看控制台错误信息

**离线消息不同步**
- 检查数据库连接
- 验证MessageDAO实现
- 查看服务器日志

### 性能优化建议

**数据库优化**
- 为message表的receiver_id和sender_id字段添加索引
- 定期清理过期的已读消息
- 使用连接池管理数据库连接

**内存优化**
- 限制在线用户数量
- 实现消息缓存机制
- 及时释放断开连接的资源

**网络优化**
- 使用消息压缩
- 实现心跳检测机制
- 优化消息传输协议

### 安全考虑

**数据安全**
- 密码使用哈希存储
- 实现SQL注入防护
- 添加输入验证和过滤

**网络安全**
- 考虑使用SSL/TLS加密
- 实现访问频率限制
- 添加用户权限管理

**隐私保护**
- 实现消息加密
- 添加消息自动删除功能
- 用户数据匿名化选项

## 版本历史

### v1.1.0 (当前版本)
- ✅ 添加离线消息同步功能
- ✅ 实现MessageDAO和OfflineMessageService
- ✅ 优化用户界面和消息显示
- ✅ 添加消息状态管理（已读/未读/已送达）

### v1.0.0 (基础版本)
- ✅ 基础聊天功能
- ✅ 用户注册和登录
- ✅ 服务器发现机制
- ✅ AI翻译功能

### 计划功能 (v1.2.0)
- 🔄 文件传输功能
- 🔄 群组聊天管理
- 🔄 消息加密
- 🔄 移动端支持

## 贡献指南

### 开发流程
1. Fork项目到个人仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

### 代码规范
- 使用Java标准命名规范
- 添加适当的注释和文档
- 遵循现有的代码风格
- 确保所有测试通过

### 提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 代码重构
- test: 测试相关
- chore: 构建过程或辅助工具的变动

## 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

如有问题或建议，请通过以下方式联系：
- 提交Issue
- 发送邮件
- 项目讨论区

---

**注意**: 这是一个教育项目，用于学习Java网络编程和Swing GUI开发。在生产环境中使用前请进行充分的安全性评估。
