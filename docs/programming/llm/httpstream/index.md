# Streamable HTTP

## ✅ 什么是 Streamable HTTP？

**Streamable HTTP** 是 MCP 协议（[2025-03-26 规范]()）中定义的一种 ​**标准流式通信模式**​：

> 它允许客户端通过 ​**单个 HTTP POST 请求**​，接收一个 ​**可选的流式响应（如 SSE）或普通 JSON 响应**​，所有交互发生在 ​**同一个 HTTP 事务中**​。

---

## 🔑 核心设计原则

| 原则                      | 说明                                                                      |
| --------------------------- | --------------------------------------------------------------------------- |
| **请求-响应一体化** | 请求与流式结果在同一个连接完成，无需分离的监听端点                        |
| **内容协商驱动**    | 客户端通过 `Accept: application/json, text/event-stream` 声明支持能力 |
| **无状态友好**      | 每个请求自包含，服务端无需维护跨请求状态来实现流                          |
| **向后兼容**        | 同一接口可同时支持流式和非流式调用                                        |

---

## 🆚 与传统 SSE 模式的对比

| 特性                 | 传统 SSE（旧）                               | Streamable HTTP（新）                  |
| ---------------------- | ---------------------------------------------- | ---------------------------------------- |
| **端点数量**   | 2 个（`POST /call` + `GET /events`） | **1 个（`POST /mcp`）**      |
| **连接模型**   | 分离式（请求 ≠ 流）                         | **一体化（请求 = 流）**          |
| **多节点部署** | 需 sticky session（会话亲和）                | **天然支持（无状态）**           |
| **客户端 API** | `.submit()` + `.getEvents(id)`       | **`.execute()`（一次调用）** |
| **错误处理**   | 复杂（跨连接协调）                           | **简单（标准 HTTP 错误）**       |
| **可重试性**   | 差（流中断难恢复）                           | **好（重试 = 新请求）**          |

---

## ✅ 为什么推荐使用 Streamable HTTP？

### 1. **云原生友好**

* 无需 sticky session
* 支持任意负载均衡（Nginx、ALB、K8s Service）
* 节点扩缩容无缝

### 2. **开发体验更佳**

* Server 端：直接返回 `Flux<ServerSentEvent<T>>`
* Client 端：一次调用，自动处理流或 JSON
* 无需维护全局连接映射或 requestId

### 3. **协议语义清晰**

* 符合 RESTful 原则：一个操作 = 一个资源交互
* 请求携带完整上下文，响应即结果（流或非流）

### 4. **可观测性更强**

* 单个 trace ID 覆盖整个请求-响应链路
* 日志、监控、调试更简单

---

## 💡 典型使用示例（MCP）

### 客户端请求

```
POST /mcp HTTP/1.1
Accept: application/json, text/event-stream
Content-Type: application/json

{
  "method": "call_tool",
  "params": { "tool_name": "search_web", "query": "AI" }
}
```

### 服务端响应（流式）

```
HTTP/1.1 200 OK
Content-Type: text/event-stream

data: {"delta":"正在搜索 AI..."}
data: {"delta":"\n- 结果1: ...", "is_final":true}
```

### 服务端响应（非流式）

```
HTTP/1.1 200 OK
Content-Type: application/json

{"result": "天气晴，25°C"}
```

---

## 🛠 开发建议

* ​**服务端（Spring WebFlux）**​：
  ```
  @PostMapping("/mcp")
  public Flux<ServerSentEvent<Result>> handle(@RequestBody Request req) {
      return toolService.executeAsStream(req)
          .map(r -> ServerSentEvent.builder().data(r).build());
  }
  ```
* ​**客户端**​：
    * 发送 `Accept: text/event-stream`
    * 根据响应 `Content-Type` 决定解析方式
    * 使用 `fetch` + `ReadableStream` 或专用 SSE 库（支持 POST 的）

---

## ✅ 总结一句话

> **Streamable HTTP 是 MCP 推荐的现代流式通信方式：它将请求与流式响应合并到单个 HTTP 事务中，消除了传统 SSE 的状态耦合问题，使系统更简单、可靠、可扩展。**

它是 **“一次调用、自包含流”** 的最佳实践，特别适合 AI Agent、LLM 工具调用等需要高可用、弹性部署的场景。
