# Langfuse


**RAGAS 和 Langfuse 是两个互补但定位完全不同的开源工具**​，它们在 LLM 应用（尤其是 RAG 系统）的开发与运维中扮演不同角色：

> ✅ **RAGAS：专注于「自动评估 RAG 系统质量」的指标库**
> ✅ **Langfuse：专注于「全链路追踪、可观测性与人工反馈」的监控平台**

它们​**不是竞争关系，而是可以深度集成、协同工作的“评估 + 观测”组合**​。

---

### 一、核心定位对比

| 工具               | 全称 / 含义              | 核心功能                                                                     | 类比                               |
| -------------------- | -------------------------- | ------------------------------------------------------------------------------ | ------------------------------------ |
| **RAGAS**    | **RAG Assessment** | 自动生成 RAG 系统的**无参考（reference-free）评估指标**                | → LLM 版的 “单元测试框架”       |
| **Langfuse** | —                       | 记录 LLM 应用的​**完整执行链路（trace）**​，支持人工标注、监控、回放 | → LLM 版的 “New Relic / Sentry” |

---

### 二、RAGAS 能做什么？

RAGAS 提供一组 **无需人工标注答案** 的自动化评估指标，特别适合 RAG 场景：

| 指标                        | 说明                                   | 用途           |
| ----------------------------- | ---------------------------------------- | ---------------- |
| **Answer Relevancy**  | 生成答案是否与问题相关                 | 检测答非所问   |
| **Faithfulness**      | 答案是否忠实于检索到的上下文（防幻觉） | 检测模型编造   |
| **Context Relevancy** | 检索到的上下文是否与问题相关           | 评估检索质量   |
| **Context Recall**    | 上下文中是否包含回答所需的关键信息     | 评估召回完整性 |

```
from ragas import evaluate
from ragas.metrics import faithfulness, answer_relevancy

result = evaluate(
    dataset=your_rag_dataset,
    metrics=[faithfulness, answer_relevancy]
)
# 输出: {'faithfulness': 0.82, 'answer_relevancy': 0.91}
```

> 🎯 ​**RAGAS 的价值**​：在没有标准答案的情况下，​**量化 RAG 系统的好坏**​。

---

### 三、Langfuse 能做什么？

Langfuse 关注 ​**运行时可观测性**​：

* 记录每次用户 query 的完整 trace（query → retrieval → prompt → answer）
* 支持人工打分（👍/👎）、添加评论
* 可视化延迟、token 消耗、错误率
* 支持将外部评估结果（如 RAGAS 分数）**写回 trace**

```
from langfuse import Langfuse

langfuse = Langfuse()
trace = langfuse.trace(id="user-123", input="How to reset password?")

# ... 执行 RAG ...

# 将 RAGAS 评估结果关联到 trace
trace.score("faithfulness", value=0.85)
trace.score("answer_relevancy", value=0.92)
```

> 🎯 ​**Langfuse 的价值**​：​**让每一次 LLM 调用可追溯、可分析、可改进**​。

---

### 四、两者如何协同工作？（推荐架构）

#### 具体流程：

1. 用户发起请求，Langfuse 开始记录 trace
2. RAG 系统执行检索 + 生成
3. 请求结束后，**异步调用 RAGAS** 对本次交互进行评估
4. 将 RAGAS 的 `faithfulness=0.6` 等分数作为 **score** 写入 Langfuse trace
5. 在 Langfuse 后台：
   * 按 `faithfulness < 0.7` 筛选高风险样本
   * 查看对应上下文，发现是检索了过期文档
   * 修复向量库 → 回放测试 → 验证分数提升

---

### 五、关键区别总结

| 维度                       | RAGAS                                    | Langfuse                            |
| ---------------------------- | ------------------------------------------ | ------------------------------------- |
| **类型**             | 评估指标库（Python SDK）                 | 可观测性平台（含后端 + UI）         |
| **输入**             | `(question, context, answer)` 三元组 | 完整 LLM 应用执行链路               |
| **输出**             | 数值化指标（0\~1）                       | Trace 日志 + 人工反馈 + 自定义评分  |
| **是否需要部署服务** | ❌ 仅需 Python 包                        | ✅ 需部署 Langfuse 后端（或用云版） |
| **能否替代对方**     | ❌ 不能监控链路                          | ❌ 不能自动计算 faithfulness        |

---

### ✅ 总结

> * ​**RAGAS 是“评估引擎”**​：告诉你 RAG 系统 **好不好**
> * ​**Langfuse 是“黑匣子记录仪”**​：告诉你 **哪里出了问题、为什么出问题**
>
> **最佳实践 = Langfuse（记录 + 人工反馈） + RAGAS（自动评估）**
> 二者结合，形成 **“监控 → 评估 → 优化 → 验证”** 的闭环，是构建高质量 RAG 产品的黄金组合。

> 🔗 相关链接：
>
> * RAGAS: [https://github.com/explodinggradients/ragas]()
> * Langfuse: [https://github.com/langfuse/langfuse]()
> * 官方集成示例：Langfuse 文档中已提供 [RAGAS 评估集成指南]()
