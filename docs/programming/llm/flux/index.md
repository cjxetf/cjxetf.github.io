# Flux vs Mono

> 均来自 **Project Reactor**，是 Spring WebFlux 的核心响应式类型，遵循 **Reactive Streams 规范**。

| 维度 | `Mono<T>` | `Flux<T>` |
|------|----------|----------|
| **语义** | 表示 **0 或 1 个** 异步结果 | 表示 **0 到 N 个** 异步数据流（可无限） |
| **类比** | `Optional<T>` + 异步 | `Stream<T>` / 事件流 + 异步 |
| **典型场景** | - 查询单条记录<br>- 调用一次 LLM API<br>- 保存/删除操作（`Mono<Void>`） | - 查询列表<br>- 流式输出（SSE）<br>- 实时事件推送（如 Kafka 消费） |
| **完成行为** | 发出最多 1 个 `onNext` → `onComplete` | 可发出多个 `onNext` → `onComplete` |
| **背压支持** | ❌（单值无需流量控制） | ✅（支持背压，防生产者过快） |
| **WebFlux 返回示例** | `public Mono<User> getUser(String id)` → JSON 对象 | `public Flux<User> listUsers()` → JSON 数组<br>`public Flux<SseEvent> stream()` → `text/event-stream` |
| **互相转换** | `flux.next()` / `flux.single()` → `Mono` | `mono.flux()` → `Flux` |
| **选择原则** | **业务语义为“单值”时使用** | **业务语义为“多值”或“流”时使用** |

## ✅ 总结
- “`Mono` 是单值异步容器，`Flux` 是多值响应式流，二者互补，非替代关系。”
- “选 `Mono` 还是 `Flux`，取决于**业务语义**，而非技术偏好。”
- “在 RAG 系统中：LLM 一次性回答用 `Mono<String>`，流式生成用 `Flux<String>`。”

## ❌ 常见误区
- 误认为 `Flux` 更“强大”而滥用 → 导致 API 语义不清。
- 在应返回单对象的接口中返回 `Flux` → 客户端困惑（是否可能多条？）。