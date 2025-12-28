# Java LLM 开源框架

### 总览对比表

| 维度                                   | **AgentScope Java**                        | **Spring AI Alibaba**        | **LangChain4j**                                    |
| ---------------------------------------- | -------------------------------------------------- | ------------------------------------ | ---------------------------------------------------------- |
| **所属组织**                     | 阿里通义实验室 (`agentscope-ai`)             | 阿里云 Spring 团队 (`alibaba`) | 社区开源（受 LangChain 启发）                            |
| **核心目标**                     | 构建 **多智能体协作系统**                  | **Spring Boot 无缝集成 LLM** | Java 版 **LangChain 全功能移植**                   |
| **是否依赖 Spring**              | ❌ 否（纯 Java）                                 | ✅ 是（需 Spring Boot 3+）         | ❌ 否（可独立使用，也提供 Spring Boot Starter）          |
| **多 Agent 支持**                | ✅ ​**原生核心特性**​（Msg/Role/Memory） | ❌ 不支持                          | ⚠️ 实验性（`ConversationalRetrievalAgent` 等）     |
| **工具调用（Function Calling）** | ✅ `@Tool` 注解 + 自动绑定                   | ✅ `FunctionCallback`          | ✅ `Tool` 接口 + 注解                                |
| **RAG 支持**                     | ❌ 基础（需自研或集成）                          | ⚠️ 有限（依赖向量库）            | ✅ ​**完整支持**​（Chroma, Pinecone, Milvus 等） |
| **模型支持**                     | Qwen（DashScope）、OpenAI 协议                   | **Qwen（DashScope）为主**    | OpenAI、Azure、Anthropic、Ollama、本地模型等             |
| **学习曲线**                     | 中（需理解 Agent 范式）                          | 低（Spring 开发者友好）            | 高（概念多，API 复杂）                                   |
| **成熟度（2025）**               | 实验阶段（快速迭代）                             | Beta（随 Spring AI 生态演进）      | ​**较成熟**​（GitHub 8k+ stars）                 |



### 🔍 详细解析

#### 1. **设计哲学**

* **AgentScope Java**
  > “​**Everything is an Agent**​” —— 强调 ​**角色、消息、协作**​，适合构建模拟人类团队的系统（如：研究员 + 写手 + 审稿人）。
* **Spring AI Alibaba**
  > “​**LLM as a Service Bean**​” —— 把大模型当作 Spring 中的一个 `@Service`，追求 ​**最小侵入、最大一致性**​。
* **LangChain4j**
  > “​**Reproduce LangChain in Java**​” —— 忠实复刻 Python LangChain 的链（Chain）、记忆（Memory）、检索器（Retriever）等概念。

---

#### 2. **多智能体（Multi-Agent）能力**

| 框架                        | 能力                                                                                 |
| ----------------------------- | -------------------------------------------------------------------------------------- |
| **AgentScope Java**   | ✅ ​**最强**​：内置 `Msg` 消息总线、`Agent` 基类、内存隔离、角色扮演 |
| **LangChain4j**       | ⚠️ 有限：可通过多个 `AgentExecutor` 手动编排，但无原生通信协议                 |
| **Spring AI Alibaba** | ❌ 无：仅支持单模型调用                                                              |

> 💡 如果你要做 ​**辩论机器人、自动化工作流、NPC 社会模拟**​，​**AgentScope Java 是目前 Java 生态唯一专注此方向的框架**​。

---

#### 3. **RAG（检索增强生成）支持**

| 框架                        | RAG 能力                                                                                                     |
| ----------------------------- | -------------------------------------------------------------------------------------------------------------- |
| **LangChain4j**       | ✅ ​**最完善**​：内置 `RetrievalQA`、多种 Embedding 模型、向量库集成（Chroma, PGVector, Milvus） |
| **Spring AI Alibaba** | ⚠️ 基础：提供 `VectorStore` 抽象，但生态插件少，需自行实现                                             |
| **AgentScope Java**   | ❌ 无：需手动集成 FAISS 或调用外部 RAG 服务                                                                  |

> 📌 若你的核心场景是 ​**知识库问答、文档摘要**​，​**LangChain4j 是首选**​。

---

#### 4. **与阿里云/Qwen 集成深度**

| 框架                        | Qwen 支持                                                                              |
| ----------------------------- | ---------------------------------------------------------------------------------------- |
| **Spring AI Alibaba** | ✅ ​**官方深度集成**​：自动配置、DashScope API 封装、错误处理优化              |
| **AgentScope Java**   | ✅ 官方支持：`DashScopeChatModel` 开箱即用                                         |
| **LangChain4j**       | ⚠️ 社区支持：通过 `OpenAiClient` 兼容 DashScope（因 DashScope 兼容 OpenAI 协议） |

> ✅ 如果你 ​**重度使用通义千问**​，前两者更可靠；LangChain4j 需注意协议兼容性。

---

#### 5. **部署与运维友好性**

| 框架                        | 优势                                                         |
| ----------------------------- | -------------------------------------------------------------- |
| **Spring AI Alibaba** | ✅ 无缝融入 Spring Boot 监控（Actuator）、配置中心、日志体系 |
| **LangChain4j**       | ✅ 提供 Spring Boot Starter，也可独立运行                    |
| **AgentScope Java**   | ⚠️ 需自行封装为 Web 服务（如结合 Spring WebFlux）          |

---

### 🧪 典型场景推荐

| 场景                                          | 推荐框架                                                                     | 理由                         |
| ----------------------------------------------- | ------------------------------------------------------------------------------ | ------------------------------ |
| **Spring Boot 应用中加一个智能客服**    | Spring AI Alibaba                                                            | 最小改动，自动装配，运维一致 |
| **构建多角色 Agent 系统（如科研助手）** | AgentScope Java                                                              | 唯一原生支持多 Agent 通信    |
| **企业知识库问答（PDF/Word 检索）**     | LangChain4j                                                                  | RAG 生态最成熟               |
| **混合场景：Agent + RAG**               | **LangChain4j + 自定义 Agent** 或 **AgentScope + 外部 RAG 服务** | 需组合使用                   |
| **非 Spring 项目（如 Quarkus）**        | LangChain4j 或 AgentScope Java                                               | 两者均不依赖 Spring          |

---

### ✅ 总结：如何选择？

| 你关心什么？                                   | 选它！                                                                          |
| ------------------------------------------------ | --------------------------------------------------------------------------------- |
| **“我想在 Spring Boot 里快速调 Qwen”** | → **Spring AI Alibaba**                                                  |
| **“我要做多个 AI 角色互相聊天协作”**   | → **AgentScope Java**                                                    |
| **“我需要强大的 RAG 和丰富模型支持”**  | → **LangChain4j**                                                        |
| **“我既要多 Agent 又要 RAG”**          | → ​**LangChain4j（主）+ 自研 Agent 编排**​，或等待 AgentScope 增强 RAG |

> 🌟 ​**未来趋势**​：
>
> * **Spring AI Alibaba** 会随 Spring AI 官方演进，成为 **Java 企业级 LLM 标准接入层**
> * **AgentScope Java** 有望成为 **Java 多智能体事实标准**
> * **LangChain4j** 仍是 **功能最全的通用 LLM 框架**

三者并非互斥，​**在复杂系统中甚至可以分层使用**​（如 Spring AI 做入口，LangChain4j 做 RAG，AgentScope 做决策编排）。
