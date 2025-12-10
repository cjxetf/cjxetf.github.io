# Spring 集成 WebSocket

在 [WebSocket](/programming/protocol/websocket/#_4-2-java) 协议介绍中讲解了在 JSR 356 规范下如何使用 WebSocket。实际上 Spring 框架对 WebSocket 也有自己的支持方式，下面将进行介绍。

## 1. 引入依赖

```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

## 2.方式

Spring 对 WebSocket 的支持主要有三种方式：

- 标准 Java WebSocket（JSR 356）`@ServerEndpoint`（介绍过了）
- 使用 Spring 的低层 API：`WebSocketHandler + WebSocketConfigurer`
- 基于 `Spring Messaging` 的 `STOMP`（推荐：发布/订阅场景）
