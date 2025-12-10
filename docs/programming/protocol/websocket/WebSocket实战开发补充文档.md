# WebSocket进阶实战开发 - 从入门到生产级应用完整指南

> **适用对象**: 具有5年Java开发经验的工程师  
> **目标**: 补充WebSocket入门博客与实际生产开发之间的知识缺口  
> **特点**: 企业级方案 + 完整代码 + 踩坑经验 + 实战案例

---

## 📋 目录

1. [现有博客内容总结与知识缺口分析](#1-现有博客内容总结与知识缺口分析)
2. [Spring Boot + WebSocket + STOMP 企业级集成](#2-spring-boot--websocket--stomp-企业级集成)
3. [STOMP协议深入应用](#3-stomp协议深入应用)
4. [集群环境下的WebSocket实现](#4-集群环境下的websocket实现)
5. [心跳检测与断线重连机制](#5-心跳检测与断线重连机制)
6. [安全认证与授权](#6-安全认证与授权)
7. [性能优化策略](#7-性能优化策略)
8. [监控与运维方案](#8-监控与运维方案)
9. [常见问题排查](#9-常见问题排查)
10. [实际项目案例](#10-实际项目案例)

---

## 1. 现有博客内容总结与知识缺口分析

### 1.1 原博客内容评估

**✅ 已覆盖的内容（入门级）：**
- WebSocket协议基础（握手、帧结构、全双工通信）
- 与其他推送方案对比（轮询、长轮询、SSE）
- JavaScript客户端实现（WebSocket API）
- Java服务端实现（JSR 356规范）
- 编程式和注解式两种实现方式
- 简单的消息广播示例

**❌ 缺失的核心实战内容（生产级）：**
- Spring Boot + STOMP企业级集成方案
- 集群环境部署和消息同步
- 心跳检测和断线重连机制
- JWT + Spring Security安全认证
- 性能优化和监控方案
- 生产问题排查经验
- 完整的企业级项目案例

### 1.2 知识缺口评估

当前博客内容占实际开发所需知识的约 **20-25%**，主要停留在：
- ✓ API使用层面
- ✓ 基本概念理解
- ✗ 缺少实战经验（75%的知识缺口）
- ✗ 缺少架构设计能力
- ✗ 缺少问题解决方案

---

## 2. Spring Boot + WebSocket + STOMP 企业级集成

### 2.1 为什么选择STOMP协议？

STOMP（Simple Text Oriented Messaging Protocol）在WebSocket之上提供了更高级的消息模型：

- **结构化消息**：明确的消息头和消息体
- **目的地路由**：支持点对点（`/queue`）和发布订阅（`/topic`）
- **消息代理**：简化多客户端通信
- **简化开发**：Spring提供了完善的支持

### 2.2 核心依赖配置

```xml
<dependencies>
    <!-- Spring Boot WebSocket -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    
    <!-- Spring Messaging -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-messaging</artifactId>
        <version>6.0.13</version>
    </dependency>
    
    <!-- JSON处理 -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.17.2</version>
    </dependency>
    
    <!-- Spring Security (可选，用于安全认证) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
</dependencies>
```

### 2.3 WebSocket配置类

```java
@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP端点，支持SockJS回退
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")  // 生产环境应指定具体域名
                .withSockJS();           // 启用SockJS支持
        
        // 纯WebSocket端点（不使用SockJS）
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单消息代理（内存中）
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{10000, 10000}); // 心跳配置
        
        // 配置应用程序消息前缀
        registry.setApplicationDestinationPrefixes("/app");
        
        // 配置用户目的地前缀（点对点消息）
        registry.setUserDestinationPrefix("/user");
    }
}
```

### 2.4 消息控制器

```java
@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 处理客户端发送的消息
     * 客户端发送到: /app/chat
     * 服务器广播到: /topic/messages
     */
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(@Payload ChatMessage message) {
        log.info("收到消息: {}", message);
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    /**
     * 点对点消息（私聊）
     * 客户端发送到: /app/private
     */
    @MessageMapping("/private")
    public void sendPrivateMessage(@Payload PrivateMessage message, 
                                   Principal principal) {
        log.info("私聊消息: {} -> {}", principal.getName(), message.getTo());
        
        // 发送给特定用户
        messagingTemplate.convertAndSendToUser(
            message.getTo(),
            "/queue/private",
            message
        );
    }

    /**
     * 主动推送消息（服务器端触发）
     */
    public void pushNotification(String userId, String content) {
        NotificationMessage notification = new NotificationMessage(content);
        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/notifications",
            notification
        );
    }
}
```

### 2.5 消息模型

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String from;
    private String content;
    private MessageType type;
    private LocalDateTime timestamp;
    
    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMessage {
    private String from;
    private String to;
    private String content;
    private LocalDateTime timestamp;
}
```

### 2.6 前端集成示例

```html
<!DOCTYPE html>
<html>
<head>
    <title>WebSocket Chat</title>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
</head>
<body>
    <script>
        let stompClient = null;
        let username = null;

        function connect() {
            // 使用SockJS作为传输层
            const socket = new SockJS('/ws');
            stompClient = Stomp.over(socket);
            
            // 连接配置
            const headers = {
                // 如果使用JWT认证，在此添加
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            };

            stompClient.connect(headers, onConnected, onError);
        }

        function onConnected(frame) {
            console.log('Connected: ' + frame);
            
            // 订阅公共频道（广播消息）
            stompClient.subscribe('/topic/messages', onMessageReceived);
            
            // 订阅私人频道（点对点消息）
            stompClient.subscribe('/user/queue/private', onPrivateMessage);
            
            // 订阅通知频道
            stompClient.subscribe('/user/queue/notifications', onNotification);
            
            // 发送加入消息
            sendJoinMessage();
        }

        function onError(error) {
            console.error('STOMP error: ' + error);
            // 实现断线重连逻辑
            setTimeout(() => {
                console.log('尝试重新连接...');
                connect();
            }, 5000);
        }

        function sendMessage() {
            const messageContent = document.getElementById('message').value;
            if (messageContent && stompClient) {
                const chatMessage = {
                    from: username,
                    content: messageContent,
                    type: 'CHAT'
                };
                
                // 发送到 /app/chat
                stompClient.send("/app/chat", {}, JSON.stringify(chatMessage));
                document.getElementById('message').value = '';
            }
        }

        function sendPrivateMessage(toUser) {
            const messageContent = document.getElementById('privateMessage').value;
            if (messageContent && stompClient) {
                const privateMessage = {
                    from: username,
                    to: toUser,
                    content: messageContent
                };
                
                stompClient.send("/app/private", {}, JSON.stringify(privateMessage));
            }
        }

        function onMessageReceived(payload) {
            const message = JSON.parse(payload.body);
            console.log('收到消息:', message);
            // 更新UI显示消息
            displayMessage(message);
        }

        function onPrivateMessage(payload) {
            const message = JSON.parse(payload.body);
            console.log('收到私聊消息:', message);
            displayPrivateMessage(message);
        }

        function onNotification(payload) {
            const notification = JSON.parse(payload.body);
            console.log('收到通知:', notification);
            showNotification(notification);
        }

        function disconnect() {
            if (stompClient !== null) {
                stompClient.disconnect();
            }
            console.log("Disconnected");
        }

        // 页面加载时连接
        window.onload = function() {
            username = prompt("请输入用户名:");
            if (username) {
                connect();
            }
        };
    </script>
</body>
</html>
```

---

## 3. STOMP协议深入应用

### 3.1 消息目的地设计

在STOMP中，目的地（Destination）用于路由消息：

**目的地类型：**
- `/topic/*`：发布-订阅模式（一对多广播）
- `/queue/*`：点对点模式（一对一消息）
- `/user/{username}/queue/*`：用户特定队列

**目的地设计最佳实践：**

```java
// 聊天室广播
/topic/chatroom/{roomId}

// 用户通知
/user/{userId}/queue/notifications

// 系统公告
/topic/announcements

// 私聊消息
/user/{userId}/queue/private

// 特定主题订阅
/topic/stocks/{symbol}
/topic/sports/{gameId}
```

### 3.2 外部消息代理集成

对于生产环境，建议使用外部消息代理（如RabbitMQ、ActiveMQ）：

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置RabbitMQ作为STOMP代理
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost("localhost")
                .setRelayPort(61613)              // RabbitMQ STOMP插件端口
                .setClientLogin("guest")
                .setClientPasscode("guest")
                .setSystemLogin("guest")
                .setSystemPasscode("guest")
                .setSystemHeartbeatSendInterval(10000)
                .setSystemHeartbeatReceiveInterval(10000);
        
        registry.setApplicationDestinationPrefixes("/app");
    }
}
```

### 3.3 消息拦截器

```java
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = 
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 连接时的处理逻辑
            log.info("WebSocket连接建立: {}", accessor.getSessionId());
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            // 断开连接时的处理逻辑
            log.info("WebSocket连接断开: {}", accessor.getSessionId());
        }
        
        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        // 消息发送后的处理
    }
}
```

---

## 4. 集群环境下的WebSocket实现

### 4.1 集群化挑战

在集群环境中部署WebSocket面临以下挑战：

1. **连接粘性**：WebSocket是长连接，客户端需要保持与同一服务器实例的连接
2. **消息广播**：如何将消息发送给连接到不同服务器实例的客户端
3. **会话管理**：如何在集群中共享和管理WebSocket会话

### 4.2 架构方案

```
                    ┌─────────────┐
                    │   Nginx     │
                    │ Load Balance│
                    └──────┬──────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
    ┌────▼────┐      ┌────▼────┐      ┌────▼────┐
    │ WS      │      │ WS      │      │ WS      │
    │ Server1 │      │ Server2 │      │ Server3 │
    └────┬────┘      └────┬────┘      └────┬────┘
         │                │                 │
         └────────────────┼─────────────────┘
                          │
                    ┌─────▼─────┐
                    │   Redis   │
                    │  Pub/Sub  │
                    └───────────┘
```

### 4.3 Nginx负载均衡配置

```nginx
upstream websocket_backend {
    # IP哈希策略，确保同一客户端连接到同一服务器
    ip_hash;
    
    server 127.0.0.1:8080;
    server 127.0.0.1:8081;
    server 127.0.0.1:8082;
}

server {
    listen 80;
    server_name example.com;

    location /ws {
        proxy_pass http://websocket_backend;
        
        # WebSocket协议升级必需的头
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # 传递原始请求头
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时设置（保持长连接）
        proxy_read_timeout 86400s;
        proxy_send_timeout 86400s;
        
        # 禁用缓冲
        proxy_buffering off;
    }
}
```

### 4.4 Redis Pub/Sub实现跨实例通信

**Maven依赖：**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

**Redis配置：**

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // 订阅频道
        container.addMessageListener(
            listenerAdapter, 
            new PatternTopic("websocket.*")
        );
        
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(RedisMessageSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // JSON序列化
        Jackson2JsonRedisSerializer<Object> serializer = 
            new Jackson2JsonRedisSerializer<>(Object.class);
        template.setDefaultSerializer(serializer);
        
        return template;
    }
}
```

**消息发布服务：**

```java
@Service
@RequiredArgsConstructor
public class WebSocketMessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 发布消息到Redis，由所有实例接收
     */
    public void publishMessage(String channel, Object message) {
        redisTemplate.convertAndSend("websocket." + channel, message);
    }

    /**
     * 广播消息到所有连接的客户端
     */
    public void broadcastToAll(ChatMessage message) {
        publishMessage("broadcast", message);
    }

    /**
     * 发送给特定用户（可能在任何实例上）
     */
    public void sendToUser(String userId, Object message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("message", message);
        publishMessage("user", payload);
    }
}
```

**消息订阅处理：**

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMessageSubscriber {

    private final SimpMessagingTemplate messagingTemplate;
    private final SessionManager sessionManager;

    /**
     * 处理从Redis接收的消息
     */
    public void onMessage(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(message);
            
            String type = node.get("type").asText();
            
            switch (type) {
                case "broadcast":
                    // 广播给所有本地连接的客户端
                    handleBroadcast(node.get("data"));
                    break;
                case "user":
                    // 发送给特定用户
                    handleUserMessage(node);
                    break;
                default:
                    log.warn("未知消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理Redis消息失败", e);
        }
    }

    private void handleBroadcast(JsonNode data) {
        // 广播给所有订阅了/topic/messages的客户端
        messagingTemplate.convertAndSend("/topic/messages", data);
    }

    private void handleUserMessage(JsonNode node) {
        String userId = node.get("userId").asText();
        Object message = node.get("message");
        
        // 仅当用户连接在本实例时才发送
        if (sessionManager.isUserConnected(userId)) {
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/private",
                message
            );
        }
    }
}
```

**会话管理器：**

```java
@Component
@Slf4j
public class SessionManager {

    // 本地会话存储 <userId, Set<sessionId>>
    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();
    
    // 会话到用户的映射 <sessionId, userId>
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();

    /**
     * 添加用户会话
     */
    public void addSession(String userId, String sessionId) {
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                    .add(sessionId);
        sessionToUser.put(sessionId, userId);
        log.info("用户 {} 建立连接，会话ID: {}", userId, sessionId);
    }

    /**
     * 移除用户会话
     */
    public void removeSession(String sessionId) {
        String userId = sessionToUser.remove(sessionId);
        if (userId != null) {
            Set<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            log.info("用户 {} 断开连接，会话ID: {}", userId, sessionId);
        }
    }

    /**
     * 检查用户是否在本实例上有连接
     */
    public boolean isUserConnected(String userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    /**
     * 获取在线用户数（仅本实例）
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    /**
     * 获取所有在线用户
     */
    public Set<String> getOnlineUsers() {
        return new HashSet<>(userSessions.keySet());
    }
}
```

### 4.5 WebSocket事件监听

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SessionManager sessionManager;
    private final WebSocketMessagePublisher messagePublisher;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // 从会话属性或JWT中获取用户ID
        String userId = getUserIdFromSession(headerAccessor);
        if (userId != null) {
            sessionManager.addSession(userId, sessionId);
        }
        
        log.info("新的WebSocket连接: sessionId={}", sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        sessionManager.removeSession(sessionId);
        
        log.info("WebSocket连接断开: sessionId={}", sessionId);
    }

    private String getUserIdFromSession(StompHeaderAccessor headerAccessor) {
        // 实现从会话中提取用户ID的逻辑
        // 例如从JWT token中解析
        return headerAccessor.getUser() != null ? 
               headerAccessor.getUser().getName() : null;
    }
}
```

---

## 5. 心跳检测与断线重连机制

### 5.1 服务器端心跳配置

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        int sendInterval = 10000;  // 服务器每10秒发送心跳
        int receiveInterval = 15000; // 期望客户端每15秒发送心跳
        
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{sendInterval, receiveInterval});
        
        registry.setApplicationDestinationPrefixes("/app");
    }

    // 使用外部代理时的心跳配置
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost("localhost")
                .setRelayPort(61613)
                .setSystemHeartbeatSendInterval(10000)
                .setSystemHeartbeatReceiveInterval(15000);
    }
}
```

### 5.2 自定义心跳管理器

```java
@Component
@Slf4j
public class HeartbeatManager {

    // 记录最后心跳时间 <sessionId, lastHeartbeat>
    private final Map<String, Instant> lastHeartbeats = new ConcurrentHashMap<>();
    
    // 心跳超时时长
    private final Duration timeout = Duration.ofMinutes(5);

    /**
     * 记录心跳
     */
    public void recordHeartbeat(String sessionId) {
        lastHeartbeats.put(sessionId, Instant.now());
        log.debug("收到心跳: sessionId={}", sessionId);
    }

    /**
     * 检查会话是否活跃
     */
    public boolean isSessionActive(String sessionId) {
        Instant lastHeartbeat = lastHeartbeats.get(sessionId);
        if (lastHeartbeat == null) {
            return false;
        }
        
        Duration elapsed = Duration.between(lastHeartbeat, Instant.now());
        return elapsed.compareTo(timeout) < 0;
    }

    /**
     * 移除会话心跳记录
     */
    public void removeSession(String sessionId) {
        lastHeartbeats.remove(sessionId);
    }

    /**
     * 获取活跃会话数
     */
    public long getActiveSessionCount() {
        return lastHeartbeats.entrySet().stream()
                .filter(entry -> isSessionActive(entry.getKey()))
                .count();
    }
}
```

### 5.3 连接监控器

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ConnectionMonitor {

    private final HeartbeatManager heartbeatManager;
    private final SessionManager sessionManager;
    private final ScheduledExecutorService scheduler = 
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName("websocket-connection-monitor");
            thread.setDaemon(true);
            return thread;
        });

    @PostConstruct
    public void startMonitoring() {
        // 每1分钟检查一次连接状态
        scheduler.scheduleAtFixedRate(
            this::checkConnections,
            0,
            1,
            TimeUnit.MINUTES
        );
        log.info("WebSocket连接监控已启动");
    }

    @PreDestroy
    public void stopMonitoring() {
        scheduler.shutdown();
        log.info("WebSocket连接监控已停止");
    }

    private void checkConnections() {
        try {
            Set<String> onlineUsers = sessionManager.getOnlineUsers();
            long activeCount = heartbeatManager.getActiveSessionCount();
            
            log.info("连接状态检查 - 在线用户: {}, 活跃连接: {}", 
                    onlineUsers.size(), activeCount);
            
            // 清理不活跃的会话（可选）
            // cleanupInactiveSessions();
        } catch (Exception e) {
            log.error("连接状态检查失败", e);
        }
    }
}
```

### 5.4 客户端心跳和断线重连

```javascript
class WebSocketClient {
    constructor(url, username) {
        this.url = url;
        this.username = username;
        this.stompClient = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.reconnectInterval = 1000; // 初始重连间隔
        this.heartbeatInterval = null;
    }

    /**
     * 建立连接
     */
    connect() {
        const socket = new SockJS(this.url);
        this.stompClient = Stomp.over(socket);
        
        // 配置心跳
        this.stompClient.heartbeat.outgoing = 10000; // 客户端发送心跳间隔
        this.stompClient.heartbeat.incoming = 10000; // 期望服务器心跳间隔
        
        // 设置调试函数（生产环境可禁用）
        this.stompClient.debug = (str) => {
            console.log('[STOMP] ' + str);
        };

        const headers = {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        };

        this.stompClient.connect(
            headers,
            (frame) => this.onConnected(frame),
            (error) => this.onError(error)
        );
    }

    /**
     * 连接成功回调
     */
    onConnected(frame) {
        console.log('WebSocket连接成功:', frame);
        this.reconnectAttempts = 0;
        this.reconnectInterval = 1000;
        
        // 订阅频道
        this.subscribe();
        
        // 发送加入消息
        this.sendJoinMessage();
        
        // 启动应用层心跳（可选，STOMP已有心跳机制）
        // this.startHeartbeat();
    }

    /**
     * 连接错误回调
     */
    onError(error) {
        console.error('WebSocket连接错误:', error);
        
        // 实现指数退避重连策略
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            
            // 计算退避时间（指数增长，最大30秒）
            const backoff = Math.min(
                this.reconnectInterval * Math.pow(2, this.reconnectAttempts - 1),
                30000
            );
            
            console.log(`${backoff/1000}秒后尝试第${this.reconnectAttempts}次重连...`);
            
            setTimeout(() => {
                this.connect();
            }, backoff);
        } else {
            console.error('达到最大重连次数，放弃重连');
            this.showReconnectFailedMessage();
        }
    }

    /**
     * 订阅频道
     */
    subscribe() {
        // 订阅公共频道
        this.stompClient.subscribe('/topic/messages', (message) => {
            this.onMessageReceived(JSON.parse(message.body));
        });
        
        // 订阅私人频道
        this.stompClient.subscribe('/user/queue/private', (message) => {
            this.onPrivateMessage(JSON.parse(message.body));
        });
        
        // 订阅通知频道
        this.stompClient.subscribe('/user/queue/notifications', (message) => {
            this.onNotification(JSON.parse(message.body));
        });
    }

    /**
     * 应用层心跳（可选）
     */
    startHeartbeat() {
        this.heartbeatInterval = setInterval(() => {
            if (this.stompClient && this.stompClient.connected) {
                this.stompClient.send('/app/heartbeat', {}, 
                    JSON.stringify({ username: this.username }));
            }
        }, 30000); // 每30秒发送一次
    }

    /**
     * 停止心跳
     */
    stopHeartbeat() {
        if (this.heartbeatInterval) {
            clearInterval(this.heartbeatInterval);
            this.heartbeatInterval = null;
        }
    }

    /**
     * 发送消息
     */
    sendMessage(content) {
        if (this.stompClient && this.stompClient.connected) {
            const message = {
                from: this.username,
                content: content,
                type: 'CHAT'
            };
            this.stompClient.send('/app/chat', {}, JSON.stringify(message));
        } else {
            console.error('WebSocket未连接，无法发送消息');
            this.showConnectionLostMessage();
        }
    }

    /**
     * 断开连接
     */
    disconnect() {
        this.stopHeartbeat();
        if (this.stompClient !== null) {
            this.stompClient.disconnect(() => {
                console.log('WebSocket已断开');
            });
        }
    }

    // 消息处理回调
    onMessageReceived(message) {
        console.log('收到消息:', message);
        // 更新UI
    }

    onPrivateMessage(message) {
        console.log('收到私聊:', message);
        // 更新UI
    }

    onNotification(notification) {
        console.log('收到通知:', notification);
        // 显示通知
    }

    showConnectionLostMessage() {
        // 显示连接断开提示
    }

    showReconnectFailedMessage() {
        // 显示重连失败提示
    }

    sendJoinMessage() {
        // 发送加入聊天室消息
    }
}

// 使用示例
const wsClient = new WebSocketClient('/ws', 'user123');
wsClient.connect();
```

---

## 6. 安全认证与授权

### 6.1 JWT认证流程

```
┌─────────┐                           ┌──────────────┐
│ Client  │                           │  Auth Server │
└────┬────┘                           └──────┬───────┘
     │  1. POST /api/login                   │
     │    (username + password)              │
     ├──────────────────────────────────────>│
     │                                        │
     │  2. JWT Token                          │
     │<───────────────────────────────────────┤
     │                                        │
     │  3. WebSocket连接 + JWT                │
     ├──────────────────────────────────────>│
     │                                        │
     │  4. 验证JWT并建立连接                   │
     │<───────────────────────────────────────┤
     │                                        │
     │  5. STOMP通信                          │
     │<──────────────────────────────────────>│
```

### 6.2 JWT工具类

```java
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成JWT令牌
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userDetails.getUsername());
        claims.put("authorities", userDetails.getAuthorities());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 验证令牌
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * 判断令牌是否过期
     */
    private boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 获取令牌过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}
```

### 6.3 WebSocket JWT认证拦截器

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = 
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 从连接头中提取JWT令牌
            String authToken = accessor.getFirstNativeHeader("Authorization");
            
            if (authToken != null && authToken.startsWith("Bearer ")) {
                String token = authToken.substring(7);
                
                try {
                    // 验证JWT令牌
                    String username = jwtTokenUtil.getUsernameFromToken(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    if (jwtTokenUtil.validateToken(token, userDetails)) {
                        // 认证成功，设置用户信息到WebSocket会话
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        accessor.setUser(authentication);
                        
                        log.info("WebSocket认证成功: username={}", username);
                    } else {
                        throw new AccessDeniedException("JWT令牌无效");
                    }
                } catch (Exception e) {
                    log.error("WebSocket认证失败", e);
                    throw new AccessDeniedException("WebSocket认证失败: " + e.getMessage());
                }
            } else {
                throw new AccessDeniedException("缺少Authorization头");
            }
        }
        
        return message;
    }
}
```

### 6.4 WebSocket配置集成认证

```java
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor authInterceptor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 添加认证拦截器
        registration.interceptors(authInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
```

### 6.5 Spring Security配置

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 允许认证相关端点
                        .requestMatchers("/api/auth/**", "/ws/**").permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> {
                    ex.authenticationEntryPoint((request, response, authException) -> 
                        response.sendError(401, "Unauthorized"));
                    ex.accessDeniedHandler((request, response, accessDeniedException) -> 
                        response.sendError(403, "Forbidden"));
                })
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
```

### 6.6 消息级别授权

```java
@Controller
@RequiredArgsConstructor
@Slf4j
public class SecureMessageController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 只有管理员可以发送系统公告
     */
    @MessageMapping("/admin/announcement")
    @PreAuthorize("hasRole('ADMIN')")
    public void sendAnnouncement(@Payload AnnouncementMessage message, 
                                 Principal principal) {
        log.info("管理员 {} 发送系统公告", principal.getName());
        messagingTemplate.convertAndSend("/topic/announcements", message);
    }

    /**
     * 检查用户权限后发送私聊消息
     */
    @MessageMapping("/secure/private")
    public void sendSecurePrivateMessage(@Payload PrivateMessage message,
                                         Principal principal) {
        // 验证发送者
        if (!message.getFrom().equals(principal.getName())) {
            throw new AccessDeniedException("无权代表其他用户发送消息");
        }
        
        // 检查是否有权限发送给目标用户
        if (hasPermissionToSendTo(principal.getName(), message.getTo())) {
            messagingTemplate.convertAndSendToUser(
                message.getTo(),
                "/queue/private",
                message
            );
        } else {
            throw new AccessDeniedException("无权向该用户发送消息");
        }
    }

    private boolean hasPermissionToSendTo(String from, String to) {
        // 实现权限检查逻辑
        // 例如：检查是否是好友关系、是否被屏蔽等
        return true;
    }
}
```

---

## 7. 性能优化策略

### 7.1 连接池管理

```java
@Configuration
public class WebSocketPerformanceConfig {

    /**
     * 配置WebSocket任务调度器
     */
    @Bean
    public TaskScheduler messageBrokerTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("wss-heartbeat-");
        scheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.initialize();
        return scheduler;
    }

    /**
     * 配置消息处理线程池
     */
    @Bean
    public ThreadPoolTaskExecutor messageExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("ws-message-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 限制并发连接数
     */
    @Bean
    public ConnectionLimiter connectionLimiter() {
        return new ConnectionLimiter(10000); // 最大10000个并发连接
    }
}

@Component
public class ConnectionLimiter {
    
    private final Semaphore semaphore;

    public ConnectionLimiter(int maxConnections) {
        this.semaphore = new Semaphore(maxConnections);
    }

    public boolean tryAcquire() {
        return semaphore.tryAcquire();
    }

    public void release() {
        semaphore.release();
    }

    public int availablePermits() {
        return semaphore.availablePermits();
    }
}
```

### 7.2 消息压缩

```java
@Configuration
@EnableWebSocket
public class WebSocketCompressionConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(myWebSocketHandler(), "/ws")
                .setAllowedOrigins("*")
                // 启用消息压缩
                .addInterceptors(new CompressionHandshakeInterceptor());
    }

    @Bean
    public WebSocketHandler myWebSocketHandler() {
        return new MyWebSocketHandler();
    }
}

/**
 * WebSocket消息压缩拦截器
 */
public class CompressionHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        // 添加压缩扩展支持
        if (request.getHeaders().containsKey("Sec-WebSocket-Extensions")) {
            response.getHeaders().add("Sec-WebSocket-Extensions", 
                "permessage-deflate; client_max_window_bits");
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                              ServerHttpResponse response,
                              WebSocketHandler wsHandler,
                              Exception exception) {
        // 握手后处理
    }
}
```

### 7.3 消息队列和批处理

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageBatchProcessor {

    private final SimpMessagingTemplate messagingTemplate;
    private final BlockingQueue<QueuedMessage> messageQueue = new LinkedBlockingQueue<>(10000);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void start() {
        // 每100ms批量处理一次消息
        scheduler.scheduleAtFixedRate(this::processBatch, 0, 100, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void stop() {
        scheduler.shutdown();
    }

    /**
     * 将消息加入队列
     */
    public boolean queueMessage(String destination, Object payload) {
        try {
            return messageQueue.offer(
                new QueuedMessage(destination, payload),
                100,
                TimeUnit.MILLISECONDS
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("消息入队被中断", e);
            return false;
        }
    }

    /**
     * 批量处理消息
     */
    private void processBatch() {
        List<QueuedMessage> batch = new ArrayList<>();
        messageQueue.drainTo(batch, 100); // 最多取100条消息
        
        if (!batch.isEmpty()) {
            // 按目的地分组
            Map<String, List<Object>> grouped = batch.stream()
                    .collect(Collectors.groupingBy(
                        QueuedMessage::getDestination,
                        Collectors.mapping(QueuedMessage::getPayload, Collectors.toList())
                    ));
            
            // 批量发送
            grouped.forEach((destination, messages) -> {
                try {
                    if (messages.size() == 1) {
                        messagingTemplate.convertAndSend(destination, messages.get(0));
                    } else {
                        // 发送批量消息
                        messagingTemplate.convertAndSend(destination, new BatchMessage(messages));
                    }
                } catch (Exception e) {
                    log.error("批量发送消息失败: destination={}", destination, e);
                }
            });
            
            log.debug("批量处理 {} 条消息", batch.size());
        }
    }

    @Data
    @AllArgsConstructor
    private static class QueuedMessage {
        private String destination;
        private Object payload;
    }

    @Data
    @AllArgsConstructor
    private static class BatchMessage {
        private List<Object> messages;
    }
}
```

### 7.4 内存管理和GC优化

```yaml
# application.yml
spring:
  websocket:
    # 消息大小限制
    message-size-limit: 128KB
    # 发送缓冲区大小
    send-buffer-size: 512KB
    # 接收缓冲区大小
    receive-buffer-size: 512KB
    
server:
  tomcat:
    # 最大连接数
    max-connections: 10000
    # 接受线程数
    accept-count: 100
    # 最大工作线程数
    threads:
      max: 200
      min-spare: 10
```

**JVM参数优化：**

```bash
java -jar app.jar \
  -Xms2g -Xmx2g \                    # 堆内存
  -XX:+UseG1GC \                     # 使用G1垃圾收集器
  -XX:MaxGCPauseMillis=200 \         # 最大GC暂停时间
  -XX:+ParallelRefProcEnabled \      # 并行处理引用
  -XX:+UseStringDeduplication \      # 字符串去重
  -Dio.netty.allocator.type=pooled \ # Netty使用池化内存
  -Dio.netty.leakDetectionLevel=simple
```

### 7.5 二进制消息传输

```java
/**
 * 使用二进制消息传输大数据
 */
@Controller
public class BinaryMessageController {

    @MessageMapping("/binary/data")
    public void handleBinaryData(@Payload byte[] data, Principal principal) {
        // 处理二进制数据
        log.info("收到二进制数据: {} bytes from {}", data.length, principal.getName());
        
        // 解析二进制协议
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int messageType = buffer.getInt();
        // ... 处理不同类型的二进制消息
    }

    /**
     * 发送二进制数据
     */
    public void sendBinaryData(String userId, byte[] data) {
        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/binary",
            data
        );
    }
}
```

### 7.6 缓存策略

```java
@Service
@RequiredArgsConstructor
public class CachedDataService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DataRepository dataRepository;

    /**
     * 获取缓存数据，不存在则从数据库加载
     */
    @Cacheable(value = "websocket:data", key = "#dataId", unless = "#result == null")
    public DataDTO getData(String dataId) {
        return dataRepository.findById(dataId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * 更新缓存
     */
    @CachePut(value = "websocket:data", key = "#dataId")
    public DataDTO updateData(String dataId, DataDTO data) {
        // 更新数据库
        dataRepository.save(convertToEntity(data));
        return data;
    }

    /**
     * 清除缓存
     */
    @CacheEvict(value = "websocket:data", key = "#dataId")
    public void invalidateCache(String dataId) {
        // 缓存失效
    }

    /**
     * 预加载热点数据到缓存
     */
    @Scheduled(fixedRate = 300000) // 每5分钟
    public void preloadHotData() {
        List<String> hotDataIds = getHotDataIds();
        hotDataIds.forEach(this::getData);
    }

    private List<String> getHotDataIds() {
        // 获取热点数据ID列表
        return Collections.emptyList();
    }

    private DataDTO convertToDTO(Object entity) {
        // 转换逻辑
        return new DataDTO();
    }

    private Object convertToEntity(DataDTO dto) {
        // 转换逻辑
        return new Object();
    }
}
```

---

## 8. 监控与运维方案

### 8.1 Prometheus指标暴露

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
```

### 8.2 自定义指标收集

```java
@Component
@RequiredArgsConstructor
public class WebSocketMetrics {

    private final MeterRegistry meterRegistry;
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicLong totalMessages = new AtomicLong(0);

    @PostConstruct
    public void init() {
        // 活跃连接数
        Gauge.builder("websocket.connections.active", activeConnections, AtomicInteger::get)
                .description("当前活跃的WebSocket连接数")
                .register(meterRegistry);
    }

    /**
     * 记录连接建立
     */
    public void recordConnection() {
        activeConnections.incrementAndGet();
        meterRegistry.counter("websocket.connections.total",
                "type", "opened").increment();
    }

    /**
     * 记录连接断开
     */
    public void recordDisconnection() {
        activeConnections.decrementAndGet();
        meterRegistry.counter("websocket.connections.total",
                "type", "closed").increment();
    }

    /**
     * 记录消息发送
     */
    public void recordMessageSent(String messageType) {
        totalMessages.incrementAndGet();
        meterRegistry.counter("websocket.messages.sent",
                "type", messageType).increment();
    }

    /**
     * 记录消息接收
     */
    public void recordMessageReceived(String messageType) {
        meterRegistry.counter("websocket.messages.received",
                "type", messageType).increment();
    }

    /**
     * 记录消息处理延迟
     */
    public void recordMessageLatency(long latencyMs, String messageType) {
        meterRegistry.timer("websocket.message.latency",
                "type", messageType)
                .record(latencyMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录错误
     */
    public void recordError(String errorType) {
        meterRegistry.counter("websocket.errors",
                "type", errorType).increment();
    }
}
```

### 8.3 监控拦截器

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsChannelInterceptor implements ChannelInterceptor {

    private final WebSocketMetrics metrics;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = 
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            
            if (StompCommand.CONNECT.equals(command)) {
                metrics.recordConnection();
            } else if (StompCommand.DISCONNECT.equals(command)) {
                metrics.recordDisconnection();
            } else if (StompCommand.SEND.equals(command)) {
                metrics.recordMessageSent(getMessageType(accessor));
            } else if (StompCommand.SUBSCRIBE.equals(command)) {
                log.info("订阅: destination={}", accessor.getDestination());
            }
        }
        
        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        if (!sent) {
            metrics.recordError("message_send_failed");
        }
    }

    private String getMessageType(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination != null) {
            if (destination.startsWith("/topic/")) {
                return "broadcast";
            } else if (destination.startsWith("/queue/")) {
                return "p2p";
            }
        }
        return "unknown";
    }
}
```

### 8.4 健康检查端点

```java
@Component
public class WebSocketHealthIndicator implements HealthIndicator {

    @Autowired
    private SessionManager sessionManager;
    
    @Autowired
    private HeartbeatManager heartbeatManager;

    @Override
    public Health health() {
        try {
            int totalSessions = sessionManager.getOnlineUserCount();
            long activeSessions = heartbeatManager.getActiveSessionCount();
            
            if (totalSessions == 0) {
                return Health.up()
                        .withDetail("sessions", 0)
                        .withDetail("status", "no_connections")
                        .build();
            }
            
            double activeRatio = (double) activeSessions / totalSessions;
            
            if (activeRatio < 0.5) {
                // 活跃连接比例过低，可能有问题
                return Health.down()
                        .withDetail("total_sessions", totalSessions)
                        .withDetail("active_sessions", activeSessions)
                        .withDetail("active_ratio", activeRatio)
                        .withDetail("status", "low_activity")
                        .build();
            }
            
            return Health.up()
                    .withDetail("total_sessions", totalSessions)
                    .withDetail("active_sessions", activeSessions)
                    .withDetail("active_ratio", activeRatio)
                    .withDetail("status", "healthy")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .build();
        }
    }
}
```

### 8.5 Grafana仪表板配置

**Prometheus查询示例：**

```promql
# 活跃连接数
websocket_connections_active{application="chat-service"}

# 每秒消息发送率
rate(websocket_messages_sent_total[1m])

# 消息延迟P95
histogram_quantile(0.95, rate(websocket_message_latency_seconds_bucket[5m]))

# 错误率
rate(websocket_errors_total[1m])

# 连接成功率
rate(websocket_connections_total{type="opened"}[5m]) / 
(rate(websocket_connections_total{type="opened"}[5m]) + 
 rate(websocket_errors_total{type="connection_failed"}[5m]))
```

### 8.6 日志配置

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="WEBSOCKET_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/websocket.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/websocket.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- WebSocket专用日志 -->
    <logger name="com.example.websocket" level="INFO" additivity="false">
        <appender-ref ref="WEBSOCKET_FILE"/>
        <appender-ref ref="CONSOLE"/>
    </logger>

    <!-- STOMP消息日志（开发环境） -->
    <logger name="org.springframework.messaging" level="DEBUG" additivity="false">
        <appender-ref ref="WEBSOCKET_FILE"/>
    </logger>

    <!-- WebSocket连接日志 -->
    <logger name="org.springframework.web.socket" level="INFO" additivity="false">
        <appender-ref ref="WEBSOCKET_FILE"/>
    </logger>
</configuration>
```

---

## 9. 常见问题排查

### 9.1 连接问题

#### 问题1: WebSocket握手失败（HTTP 400/403）

**现象：**
```
Failed to load resource: the server responded with a status of 403 (Forbidden)
Error during WebSocket handshake: Unexpected response code: 403
```

**排查步骤：**
1. 检查CORS配置
2. 检查Spring Security配置是否放行WebSocket端点
3. 检查JWT令牌是否正确传递

**解决方案：**

```java
// 1. 确保WebSocket端点允许跨域
@Override
public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")  // 或指定具体域名
            .withSockJS();
}

// 2. Spring Security放行WebSocket端点
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/ws/**").permitAll()
                    .anyRequest().authenticated()
            )
            .build();
}
```

#### 问题2: 连接建立后立即断开

**现象：**
```
WebSocket connection to 'ws://localhost:8080/ws' failed: Connection closed before receiving a handshake response
```

**可能原因：**
- JWT认证失败
- 服务器主动关闭连接
- 网络不稳定

**排查：**
```java
@Component
@Slf4j
public class ConnectionDebugListener {
    
    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        log.info("连接建立: sessionId={}, user={}", 
                sha.getSessionId(), sha.getUser());
    }
    
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        CloseStatus closeStatus = (CloseStatus) sha.getHeader("closeStatus");
        log.warn("连接断开: sessionId={}, status={}, reason={}", 
                sha.getSessionId(), 
                closeStatus != null ? closeStatus.getCode() : "unknown",
                closeStatus != null ? closeStatus.getReason() : "unknown");
    }
}
```

### 9.2 消息问题

#### 问题3: 消息丢失

**现象：** 客户端发送消息，但服务器未收到或未广播

**排查步骤：**

1. 检查消息目的地是否正确
2. 检查@MessageMapping路径是否匹配
3. 检查是否有异常抛出

**调试代码：**

```java
@Controller
@Slf4j
public class DebugMessageController {
    
    @MessageMapping("/**")
    public void catchAll(Message<?> message, StompHeaderAccessor accessor) {
        log.info("收到消息: destination={}, payload={}", 
                accessor.getDestination(), message.getPayload());
    }
    
    @MessageExceptionHandler
    public void handleException(Exception e, Message<?> message) {
        log.error("消息处理异常: {}", message.getPayload(), e);
    }
}
```

#### 问题4: 消息延迟过高

**排查工具：**

```java
@Aspect
@Component
@Slf4j
public class MessageLatencyAspect {
    
    @Around("@annotation(org.springframework.messaging.handler.annotation.MessageMapping)")
    public Object measureLatency(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long latency = System.currentTimeMillis() - startTime;
            if (latency > 100) {  // 超过100ms记录
                log.warn("消息处理延迟: {}ms, method={}", 
                        latency, joinPoint.getSignature().getName());
            }
        }
    }
}
```

### 9.3 性能问题

#### 问题5: 内存泄漏

**排查步骤：**

1. 使用jmap生成堆转储
```bash
jmap -dump:live,format=b,file=heapdump.hprof <pid>
```

2. 使用MAT或JProfiler分析
3. 检查是否有未关闭的连接
4. 检查缓存是否正确清理

**常见内存泄漏点：**

```java
// ❌ 错误：会话未清理
private Map<String, WebSocketSession> sessions = new HashMap<>();

public void addSession(WebSocketSession session) {
    sessions.put(session.getId(), session);
    // 忘记在断开时清理
}

// ✅ 正确：使用弱引用或及时清理
private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

@EventListener
public void handleDisconnect(SessionDisconnectEvent event) {
    sessions.remove(event.getSessionId());
}
```

#### 问题6: CPU飙升

**排查命令：**

```bash
# 查找CPU占用高的线程
top -Hp <pid>

# 生成线程转储
jstack <pid> > thread_dump.txt

# 或使用jcmd
jcmd <pid> Thread.print > thread_dump.txt
```

**常见原因：**
- 消息处理逻辑耗时过长
- 频繁的GC
- 死循环或死锁

### 9.4 集群问题

#### 问题7: 集群消息不同步

**现象：** 消息只能发送给连接到同一服务器的客户端

**检查清单：**

1. Redis连接是否正常
```java
@Component
@RequiredArgsConstructor
public class RedisHealthCheck {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Scheduled(fixedRate = 60000)
    public void checkRedis() {
        try {
            redisTemplate.opsForValue().set("health:check", "ok", 10, TimeUnit.SECONDS);
            log.info("Redis连接正常");
        } catch (Exception e) {
            log.error("Redis连接异常", e);
        }
    }
}
```

2. Pub/Sub是否正确配置
3. 检查消息是否正确发布到Redis

**调试日志：**

```java
@Service
@Slf4j
public class DebugMessagePublisher {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public void publish(String channel, Object message) {
        log.info("发布消息到Redis: channel={}, message={}", channel, message);
        Long receivers = redisTemplate.convertAndSend(channel, message);
        log.info("消息接收者数量: {}", receivers);
    }
}
```

### 9.5 排查工具箱

```java
@RestController
@RequestMapping("/api/debug/websocket")
@RequiredArgsConstructor
public class WebSocketDebugController {
    
    private final SessionManager sessionManager;
    private final HeartbeatManager heartbeatManager;
    private final WebSocketMetrics metrics;
    
    /**
     * 获取当前连接状态
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("onlineUsers", sessionManager.getOnlineUserCount());
        status.put("activeSessions", heartbeatManager.getActiveSessionCount());
        status.put("totalMessages", metrics.getTotalMessages());
        return status;
    }
    
    /**
     * 获取在线用户列表
     */
    @GetMapping("/users")
    public Set<String> getOnlineUsers() {
        return sessionManager.getOnlineUsers();
    }
    
    /**
     * 强制断开指定用户
     */
    @PostMapping("/disconnect/{userId}")
    public void forceDisconnect(@PathVariable String userId) {
        // 实现强制断开逻辑
    }
    
    /**
     * 发送测试消息
     */
    @PostMapping("/test-message")
    public void sendTestMessage(@RequestParam String userId, 
                                @RequestParam String content) {
        // 发送测试消息
    }
}
```

---

## 10. 实际项目案例

### 10.1 案例一：企业级实时IM系统

#### 业务需求
- 支持10万+并发用户
- 消息必达（离线消息）
- 群聊、私聊
- 文件传输
- 消息已读回执

#### 技术架构

```
┌──────────────────────────────────────────────────┐
│                   Client Layer                    │
│  Web(Vue.js) / iOS(Swift) / Android(Kotlin)     │
└─────────────────────┬────────────────────────────┘
                      │
┌─────────────────────▼────────────────────────────┐
│                  Gateway Layer                    │
│        Nginx (Load Balancer + SSL Termination)   │
└─────────────────────┬────────────────────────────┘
                      │
┌─────────────────────▼────────────────────────────┐
│              WebSocket Server Cluster             │
│   Spring Boot + STOMP + JWT (4 instances)        │
└──┬────────────────────────────────────────────┬──┘
   │                                            │
┌──▼──────────┐              ┌────────────────▼───┐
│   Redis     │              │   RabbitMQ (STOMP) │
│  Pub/Sub    │              │   Message Broker   │
└──┬──────────┘              └────────────────┬───┘
   │                                          │
┌──▼──────────────────────────────────────────▼───┐
│              Business Service Layer              │
│  User Service / Message Service / File Service  │
└──────────────────────┬───────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────┐
│                Database Layer                     │
│   MySQL (User/Group) + MongoDB (Message History) │
└──────────────────────────────────────────────────┘
```

#### 核心代码实现

**1. 消息模型：**

```java
@Data
@Document(collection = "messages")
public class ChatMessage {
    @Id
    private String id;
    private String conversationId;  // 会话ID（群聊或私聊）
    private MessageType type;        // TEXT, IMAGE, FILE, SYSTEM
    private String senderId;
    private List<String> receiverIds;
    private String content;
    private String fileUrl;
    private Long timestamp;
    private MessageStatus status;    // SENT, DELIVERED, READ
    private Map<String, Long> readBy; // userId -> readTimestamp
}

@Data
public class Conversation {
    private String id;
    private ConversationType type;  // PRIVATE, GROUP
    private List<String> participants;
    private String lastMessage;
    private Long lastMessageTime;
    private Map<String, Integer> unreadCount;
}
```

**2. 消息服务：**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {
    
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final WebSocketMessagePublisher messagePublisher;
    private final OfflineMessageQueue offlineMessageQueue;
    
    /**
     * 发送消息
     */
    @Transactional
    public ChatMessage sendMessage(SendMessageRequest request) {
        // 1. 验证权限
        validatePermission(request.getSenderId(), request.getConversationId());
        
        // 2. 保存消息到数据库
        ChatMessage message = new ChatMessage();
        message.setId(UUID.randomUUID().toString());
        message.setConversationId(request.getConversationId());
        message.setType(request.getType());
        message.setSenderId(request.getSenderId());
        message.setContent(request.getContent());
        message.setTimestamp(System.currentTimeMillis());
        message.setStatus(MessageStatus.SENT);
        
        messageRepository.save(message);
        
        // 3. 更新会话信息
        updateConversation(message);
        
        // 4. 发送实时消息
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("会话不存在"));
        
        for (String participantId : conversation.getParticipants()) {
            if (!participantId.equals(request.getSenderId())) {
                // 尝试实时推送
                boolean delivered = messagePublisher.sendToUser(participantId, message);
                
                if (!delivered) {
                    // 用户离线，加入离线消息队列
                    offlineMessageQueue.add(participantId, message);
                }
            }
        }
        
        return message;
    }
    
    /**
     * 标记消息已读
     */
    public void markAsRead(String userId, String messageId) {
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));
        
        if (message.getReadBy() == null) {
            message.setReadBy(new HashMap<>());
        }
        message.getReadBy().put(userId, System.currentTimeMillis());
        
        // 检查是否所有人都已读
        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElseThrow();
        
        if (message.getReadBy().size() == conversation.getParticipants().size() - 1) {
            message.setStatus(MessageStatus.READ);
        }
        
        messageRepository.save(message);
        
        // 通知发送者消息已读
        ReadReceipt receipt = new ReadReceipt(messageId, userId, System.currentTimeMillis());
        messagePublisher.sendToUser(message.getSenderId(), receipt);
    }
    
    /**
     * 获取离线消息
     */
    public List<ChatMessage> getOfflineMessages(String userId) {
        return offlineMessageQueue.getAndClear(userId);
    }
    
    private void updateConversation(ChatMessage message) {
        Conversation conversation = conversationRepository.findById(message.getConversationId())
                .orElseThrow();
        
        conversation.setLastMessage(message.getContent());
        conversation.setLastMessageTime(message.getTimestamp());
        
        // 更新未读计数
        for (String participantId : conversation.getParticipants()) {
            if (!participantId.equals(message.getSenderId())) {
                conversation.getUnreadCount().merge(participantId, 1, Integer::sum);
            }
        }
        
        conversationRepository.save(conversation);
    }
    
    private void validatePermission(String userId, String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));
        
        if (!conversation.getParticipants().contains(userId)) {
            throw new AccessDeniedException("无权限访问该会话");
        }
    }
}
```

**3. 离线消息队列：**

```java
@Service
@RequiredArgsConstructor
public class OfflineMessageQueue {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String OFFLINE_QUEUE_PREFIX = "offline:messages:";
    
    /**
     * 添加离线消息
     */
    public void add(String userId, ChatMessage message) {
        String key = OFFLINE_QUEUE_PREFIX + userId;
        redisTemplate.opsForList().rightPush(key, message);
        // 设置过期时间7天
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }
    
    /**
     * 获取并清空离线消息
     */
    public List<ChatMessage> getAndClear(String userId) {
        String key = OFFLINE_QUEUE_PREFIX + userId;
        List<Object> messages = redisTemplate.opsForList().range(key, 0, -1);
        redisTemplate.delete(key);
        
        return messages.stream()
                .map(obj -> (ChatMessage) obj)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取离线消息数量
     */
    public long getCount(String userId) {
        String key = OFFLINE_QUEUE_PREFIX + userId;
        Long size = redisTemplate.opsForList().size(key);
        return size != null ? size : 0;
    }
}
```

**4. WebSocket控制器：**

```java
@Controller
@RequiredArgsConstructor
@Slf4j
public class IMWebSocketController {
    
    private final ChatMessageService messageService;
    private final SessionManager sessionManager;
    
    /**
     * 发送聊天消息
     */
    @MessageMapping("/im/send")
    public void sendMessage(@Payload SendMessageRequest request, Principal principal) {
        request.setSenderId(principal.getName());
        messageService.sendMessage(request);
    }
    
    /**
     * 标记消息已读
     */
    @MessageMapping("/im/read")
    public void markAsRead(@Payload ReadMessageRequest request, Principal principal) {
        messageService.markAsRead(principal.getName(), request.getMessageId());
    }
    
    /**
     * 用户上线，推送离线消息
     */
    @EventListener
    public void handleUserOnline(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = accessor.getUser().getName();
        
        log.info("用户上线: {}", userId);
        
        // 获取离线消息
        List<ChatMessage> offlineMessages = messageService.getOfflineMessages(userId);
        
        if (!offlineMessages.isEmpty()) {
            log.info("推送 {} 条离线消息给用户: {}", offlineMessages.size(), userId);
            offlineMessages.forEach(message -> {
                messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/offline",
                    message
                );
            });
        }
    }
}
```

#### 性能指标
- 单服务器支持25,000并发连接
- 集群总计100,000+并发连接
- 消息延迟P95 < 50ms
- 消息送达率 99.99%

### 10.2 案例二：实时数据监控大屏

#### 业务场景
- 实时显示业务指标
- 多指标同时更新
- 历史数据回放
- 告警推送

#### 核心实现

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsPushService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final MetricsRepository metricsRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 推送实时指标
     */
    @Scheduled(fixedRate = 1000) // 每秒推送一次
    public void pushRealtimeMetrics() {
        try {
            // 收集各类指标
            Map<String, Object> metrics = collectMetrics();
            
            // 推送到所有订阅的客户端
            messagingTemplate.convertAndSend("/topic/metrics/realtime", metrics);
            
            // 检查告警
            checkAlerts(metrics);
        } catch (Exception e) {
            log.error("推送实时指标失败", e);
        }
    }
    
    /**
     * 推送特定指标给特定用户
     */
    public void pushCustomMetrics(String userId, List<String> metricNames) {
        Map<String, Object> customMetrics = new HashMap<>();
        
        for (String metricName : metricNames) {
            Object value = getMetricValue(metricName);
            customMetrics.put(metricName, value);
        }
        
        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/metrics/custom",
            customMetrics
        );
    }
    
    /**
     * 历史数据回放
     */
    public void replayHistory(String userId, long startTime, long endTime) {
        List<MetricSnapshot> history = metricsRepository.findByTimeBetween(startTime, endTime);
        
        // 按时间顺序推送
        history.forEach(snapshot -> {
            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/metrics/replay",
                snapshot
            );
            
            // 模拟实时速度
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    private Map<String, Object> collectMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // 从不同数据源收集指标
        metrics.put("activeUsers", getActiveUserCount());
        metrics.put("qps", getQPS());
        metrics.put("errorRate", getErrorRate());
        metrics.put("avgResponseTime", getAvgResponseTime());
        metrics.put("cpuUsage", getCPUUsage());
        metrics.put("memoryUsage", getMemoryUsage());
        
        return metrics;
    }
    
    private void checkAlerts(Map<String, Object> metrics) {
        // 实现告警逻辑
        double errorRate = (double) metrics.get("errorRate");
        if (errorRate > 0.05) { // 错误率超过5%
            Alert alert = new Alert("HIGH_ERROR_RATE", "错误率过高: " + errorRate);
            messagingTemplate.convertAndSend("/topic/alerts", alert);
        }
    }
    
    // 各种指标获取方法...
    private long getActiveUserCount() { return 0; }
    private double getQPS() { return 0.0; }
    private double getErrorRate() { return 0.0; }
    private double getAvgResponseTime() { return 0.0; }
    private double getCPUUsage() { return 0.0; }
    private double getMemoryUsage() { return 0.0; }
    private Object getMetricValue(String metricName) { return null; }
}
```

### 10.3 案例三：在线协同编辑

#### 核心挑战
- 多用户同时编辑
- 冲突检测与解决
- 操作同步
- 撤销/重做

#### OT算法实现

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CollaborativeEditingService {
    
    private final DocumentRepository documentRepository;
    private final OperationTransformer operationTransformer;
    private final SimpMessagingTemplate messagingTemplate;
    
    // 文档版本号 <docId, version>
    private final Map<String, AtomicInteger> documentVersions = new ConcurrentHashMap<>();
    
    /**
     * 处理编辑操作
     */
    public void handleOperation(String docId, String userId, Operation operation) {
        // 1. 加锁防止并发冲突
        synchronized (getDocumentLock(docId)) {
            // 2. 获取当前文档版本
            int currentVersion = documentVersions
                    .computeIfAbsent(docId, k -> new AtomicInteger(0))
                    .get();
            
            // 3. 如果操作基于旧版本，需要进行转换
            if (operation.getBaseVersion() < currentVersion) {
                List<Operation> intermediateOps = getOperationsBetween(
                        docId, operation.getBaseVersion(), currentVersion);
                
                operation = operationTransformer.transform(operation, intermediateOps);
            }
            
            // 4. 应用操作到文档
            applyOperation(docId, operation);
            
            // 5. 版本号递增
            int newVersion = documentVersions.get(docId).incrementAndGet();
            operation.setVersion(newVersion);
            
            // 6. 广播给其他用户
            broadcastOperation(docId, userId, operation);
            
            // 7. 持久化操作
            saveOperation(docId, operation);
        }
    }
    
    private void applyOperation(String docId, Operation operation) {
        Document doc = documentRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        
        switch (operation.getType()) {
            case INSERT:
                doc.insertText(operation.getPosition(), operation.getText());
                break;
            case DELETE:
                doc.deleteText(operation.getPosition(), operation.getLength());
                break;
            case FORMAT:
                doc.applyFormat(operation.getPosition(), operation.getLength(), operation.getFormat());
                break;
        }
        
        documentRepository.save(doc);
    }
    
    private void broadcastOperation(String docId, String userId, Operation operation) {
        OperationMessage message = new OperationMessage(
                docId, userId, operation, System.currentTimeMillis());
        
        messagingTemplate.convertAndSend(
                "/topic/doc/" + docId + "/operations",
                message
        );
    }
    
    private Object getDocumentLock(String docId) {
        // 实现文档级别的锁
        return ("lock_" + docId).intern();
    }
    
    private List<Operation> getOperationsBetween(String docId, int startVersion, int endVersion) {
        // 从数据库或缓存获取中间操作
        return Collections.emptyList();
    }
    
    private void saveOperation(String docId, Operation operation) {
        // 持久化操作历史
    }
}

/**
 * 操作转换器（OT算法核心）
 */
@Component
public class OperationTransformer {
    
    public Operation transform(Operation op, List<Operation> concurrentOps) {
        Operation transformed = op;
        
        for (Operation concurrentOp : concurrentOps) {
            transformed = transformPair(transformed, concurrentOp);
        }
        
        return transformed;
    }
    
    private Operation transformPair(Operation op1, Operation op2) {
        // 实现OT算法的两个操作转换
        if (op1.getType() == OperationType.INSERT && op2.getType() == OperationType.INSERT) {
            if (op1.getPosition() < op2.getPosition()) {
                return op1;
            } else {
                return op1.withPosition(op1.getPosition() + op2.getText().length());
            }
        }
        // ... 其他转换情况
        return op1;
    }
}
```

---

## 总结与最佳实践

### 关键要点回顾

1. **Spring Boot + STOMP** 是企业级WebSocket开发的标准组合
2. **外部消息代理** 是实现集群扩展的关键
3. **心跳检测和断线重连** 保障连接稳定性
4. **JWT认证** 是WebSocket安全的首选方案
5. **性能优化** 需要从连接管理、消息处理、内存管理多方面入手
6. **监控运维** 是生产环境不可或缺的环节

### 生产环境检查清单

- [ ] 使用WSS加密连接
- [ ] 实现JWT认证和授权
- [ ] 配置心跳检测
- [ ] 实现断线重连
- [ ] 集群部署（Nginx + Redis）
- [ ] 配置连接池和线程池
- [ ] 启用消息压缩
- [ ] 实现消息队列
- [ ] 集成Prometheus监控
- [ ] 配置日志记录
- [ ] 编写健康检查
- [ ] 压力测试验证
- [ ] 制定应急预案

### 进阶学习路径

1. **深入STOMP协议** - 理解协议细节和扩展
2. **学习操作转换算法** - 协同编辑核心技术
3. **研究Netty底层** - 理解WebSocket底层实现
4. **掌握分布式追踪** - 集群环境下的问题定位
5. **实践高可用架构** - 多机房、容灾、降级

---

**文档版本**: v1.0  
**更新时间**: 2025-01-19  
**适用人群**: 5年+ Java开发经验  
**技术栈**: Spring Boot 3.x + WebSocket + STOMP + Redis + RabbitMQ

---

希望这份文档能帮助你从WebSocket入门快速成长为能够独立设计和实现生产级WebSocket系统的高级工程师！🚀