# NetTalk-Java 聊天应用程序

一个基于Java Swing的现代化聊天应用程序，支持实时通信、离线消息同步、AI翻译等功能。

## 功能特性

### 核心功能
- 🔐 **用户认证系统** - 安全的注册和登录机制
- 💬 **即时通讯** - 支持群聊和私聊功能
- 📱 **现代UI界面** - 基于Swing的美观设计
- 🌐 **服务器发现** - 自动检测局域网内的服务器
- 📊 **状态显示** - 实时监控用户在线状态

### 离线消息功能 ✨
- 📥 **消息存储** - 离线时自动保存接收的消息
- 🔄 **自动同步** - 登录时立即同步所有离线消息
- 📈 **未读提醒** - 显示未读消息数量和发送者
- ✅ **状态标记** - 消息已读/已送达状态跟踪

### 高级功能
- 🤖 **AI翻译** - 集成AI服务实现消息实时翻译
- 🔄 **消息转发** - 一键转发消息给其他用户
- ⚙️ **动态设置** - 运行时修改服务器配置
- 🔍 **多服务器** - 管理并连接多个聊天服务器

## 技术架构

### 后端技术
- **Java 11+** - 核心开发语言
- **Socket通信** - TCP/UDP网络编程
- **MySQL** - 数据持久化存储
- **JDBC** - 数据库连接与操作
- **多线程** - 并发处理客户端连接

### 前端技术
- **Java Swing** - 图形用户界面
- **自定义组件** - 现代化UI设计
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
    sender BIGINT NOT NULL,
    receiver BIGINT NOT NULL,
    message VARCHAR(200) NOT NULL,
    ddate DATE NOT NULL,
    `read` INT NOT NULL,
    reserved VARCHAR(200),
    FOREIGN KEY (sender) REFERENCES user(_id),
    FOREIGN KEY (receiver) REFERENCES user(_id)
);
```

## 安装与配置

### 环境要求
- Java 11 或更高版本
- MySQL 5.7 或更高版本
- Maven 3.6 或更高版本

### 数据库配置
1. 创建MySQL数据库
2. 执行 `src/main/resources/db_script.sql` 创建表结构
3. 配置数据库连接信息

### 配置文件
编辑 `config/config.properties`：

```properties
# 数据库配置
db.url=jdbc:mysql://localhost:3306/nettalk
db.user=your_username
db.password=your_password

# 服务器配置
server.host=localhost
server.port=8888
server.start=false

# AI翻译配置
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
2. 配置项目SDK为Java 11+
3. 运行 `com.example.App` 主类

## 使用指南

### 服务器模式
1. 在配置文件中设置 `server.start=true`
2. 或在设置界面中手动启动服务器
3. 服务器将在指定端口监听连接请求

### 客户端连接
1. 启动应用程序
2. 注册新账户或使用现有账户登录
3. 应用程序自动连接到可用服务器
4. 连接成功后即可开始聊天

### 离线消息
1. 用户离线时，消息自动存储在数据库
2. 用户登录时，系统自动同步所有离线消息
3. 离线消息在界面中以特殊样式显示
4. 查看后自动标记为已读状态

### 私聊功能
1. 在用户列表中双击用户名
2. 在私聊窗口中发送消息
3. 支持离线消息存储和同步
4. 可转发消息给其他用户

## 项目结构

```
src/main/java/com/example/
├── App.java                    # 应用程序入口
├── component/                  # UI组件
│   ├── ChatClient.java         # 聊天客户端
│   ├── ChatPanel.java          # 聊天面板
│   └── MessageBubble.java      # 消息气泡
├── controller/                 # 控制器
│   └── AuthController.java     # 认证控制器
├── dao/                        # 数据访问层
│   ├── MessageDAO.java         # 消息数据访问
│   └── UserDAO.java            # 用户数据访问
├── model/                      # 数据模型
│   ├── Message.java            # 消息模型
│   ├── Settings.java           # 设置模型
│   └── User.java               # 用户模型
├── service/                    # 服务层
│   ├── AIService.java          # AI翻译服务
│   ├── OfflineMessageService.java # 离线消息服务
│   └── SocketService.java      # Socket通信服务
├── util/                       # 工具类
│   ├── DBUtil.java             # 数据库工具
│   └── ServerDiscovery.java    # 服务器发现
└── view/                       # 视图层
    ├── LoginView.java          # 登录界面
    ├── MainView.java           # 主界面
    ├── PrivateChatView.java    # 私聊界面
    └── SettingsView.java       # 设置界面
```

## 开发指南

### 添加新功能
1. 在相应包中创建新类
2. 遵循MVC架构模式
3. 使用DAO模式进行数据访问
4. 添加相应的测试用例

### 数据库操作
- 使用 `DBUtil.getConnection()` 获取数据库连接
- 所有数据库操作在DAO类中实现
- 使用PreparedStatement防止SQL注入

### UI开发
- 继承现有UI组件风格
- 使用SwingUtilities.invokeLater()更新UI
- 遵循响应式设计原则

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

## 版本历史

### v1.1.0 (当前版本)
- ✅ 离线消息同步功能
- ✅ 消息状态管理
- ✅ UI界面优化
- ✅ AI翻译功能

### v1.0.0 (基础版本)
- ✅ 基础聊天功能
- ✅ 用户注册和登录
- ✅ 服务器发现机制

### 计划功能 (v1.2.0)
- 🔄 文件传输功能
- 🔄 群组聊天管理
- 🔄 消息加密
- 🔄 移动端支持

## 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

如有问题或建议，请通过以下方式联系：
- 提交Issue
- 发送邮件
- 项目讨论区

---

**注意**: 本项目用于学习Java网络编程和Swing GUI开发，生产环境使用前请进行充分的安全性评估。
