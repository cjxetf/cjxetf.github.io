# MLOps vs LLMOps

### 总览对比表

| 维度               | **MLOps（传统机器学习）**                | **LLMOps（大语言模型）**                                 |
| -------------------- | ------------------------------------------------ | ---------------------------------------------------------------- |
| **模型特性** | 小模型（<1GB），结构固定（如 XGBoost、ResNet） | 超大模型（7B\~70B+ 参数），可微调/提示工程                     |
| **数据重心** | **特征工程 + 标注数据质量**              | **提示（Prompt） + 上下文（Context） + 知识库**          |
| **评估方式** | 固定指标（Accuracy, AUC, F1）                  | ​**主观性高**​：相关性、流畅性、无幻觉、安全性         |
| **部署形态** | 批处理 or 实时 API（输入→输出确定）           | **交互式、多轮对话、流式生成**                           |
| **监控重点** | 数据漂移、模型衰减、预测偏差                   | **提示注入、越狱攻击、上下文泄露、Token 消耗**           |
| **迭代方式** | 重训练（天/周级）                              | ​**轻量更新**​：Prompt 优化、RAG 知识库刷新、LoRA 微调 |
| **工具链**   | MLflow, Kubeflow, SageMaker                    | **LangChain, LlamaIndex, Langfuse, Weaviate, vLLM**      |
| **成本结构** | 训练成本为主                                   | ​**推理成本为主**​（Token 量 × 模型单价）             |

---

### 🔍 详细解析各维度差异

#### 1. **模型管理（Model Management）**

* ​**MLOps**​：
    * 模型 = `.pkl` / `.onnx` 文件，版本清晰
    * 注册、部署、回滚简单
* ​**LLMOps**​：
    * 模型可能包含：**基础模型 + LoRA 适配器 + Prompt 模板 + 向量库**
    * 需管理 ​**多组件协同版本**​（如 Qwen-Max + KB-v2 + Prompt-v3）

> 💡 LLMOps 引入了 **“模型即服务栈”（Model Stack）** 概念，而非单一模型文件。

---

#### 2. **数据与输入（Data & Input）**

* ​**MLOps**​：
    * 输入是结构化特征向量（如 `[age=30, income=50k]`）
    * 关注特征一致性、缺失值
* ​**LLMOps**​：
    * 输入是 **自然语言 + 可能的检索上下文**
    * 关注：
        * Prompt 是否被污染（如用户注入恶意指令）
        * RAG 检索结果是否相关/过时
        * 上下文长度是否超限（导致关键信息截断）

> 🌟 **LLMOps 的“数据” = Prompt + Context + Knowledge Base**

---

#### 3. **评估与验证（Evaluation）**

* ​**MLOps**​：
    * 有明确 label → 计算准确率等客观指标
    * 测试集固定
* ​**LLMOps**​：
    * ​**多数场景无标准答案**​（如“写一篇营销文案”）
    * 依赖：
        * ​**人工评估**​（成本高）
        * ​**LLM-as-a-Judge**​（用 GPT-4 评质量）
        * ​**专用指标**​（如 RAGAS 的 Faithfulness、Answer Relevancy）
        * ​**安全护栏测试**​（是否拒绝有害请求）

> ⚠️ LLMOps 评估更像“产品体验测试”，而非“统计性能测试”。

---

#### 4. **监控与可观测性（Monitoring）**

| 监控项             | MLOps                | LLMOps                                         |
| -------------------- | ---------------------- | ------------------------------------------------ |
| **输入监控** | 特征分布偏移         | Prompt 长度、敏感词、异常模式                  |
| **输出监控** | 预测置信度、类别分布 | 幻觉率、毒性内容、重复生成                     |
| **系统监控** | 延迟、错误率         | **Token 使用量、缓存命中率、流式中断率** |
| **业务监控** | 转化率、ROI          | 用户满意度（👍/👎）、重问率、会话深度          |

> ✅ LLMOps 必须集成 **Langfuse / Phoenix / PromptLayer** 等 LLM 专属可观测工具。

---

#### 5. **部署与推理（Deployment & Inference）**

* ​**MLOps**​：
    * 模型小 → 可嵌入边缘设备或微服务
    * 延迟要求高（毫秒级）
* ​**LLMOps**​：
    * 模型大 → 需 GPU 推理服务器（vLLM, TGI）
    * 支持 **流式输出（SSE/WebSocket）**
    * 需优化 **KV Cache、PagedAttention** 以降低显存
    * 成本敏感：**每 token 都要计费**

> 💡 LLMOps 更关注 ​**吞吐量（tokens/sec）与成本效率**​，而非单纯延迟。

---

#### 6. **安全与合规（Security & Compliance）**

* ​**MLOps**​：
    * 主要防数据泄露、模型窃取
* ​**LLMOps**​：
    * ​**新风险突出**​：
        * Prompt 注入（如“忽略之前指令…”）
        * 越狱（Jailbreak）
        * 上下文泄露（多租户场景）
        * 训练数据记忆（隐私泄露）
    * 需部署 **输入过滤、输出审查、沙箱执行**

> 🔒 LLMOps 必须内置 ​**安全护栏（Safety Guardrails）**​。

---

#### 7. **迭代与实验（Iteration & Experimentation）**

* ​**MLOps**​：
    * A/B 测试：模型 v1 vs v2
    * 实验周期：数天
* ​**LLMOps**​：
    * A/B 测试对象更多样：
        * Prompt A vs Prompt B
        * RAG with KB-v1 vs KB-v2
        * Model: Qwen-Max vs GPT-4
    * 支持 ​**实时灰度发布**​（因微调成本低）

> 🧪 LLMOps 实验单元从“模型”扩展到“整个提示-检索-生成链路”。

---

#### 8. **工具生态（Tooling Ecosystem）**

| 功能     | MLOps 工具               | LLMOps 工具                                      |
| ---------- | -------------------------- | -------------------------------------------------- |
| 实验跟踪 | MLflow, Weights & Biases | **Langfuse, PromptHub, Braintrust**        |
| 向量存储 | —                       | **Weaviate, Milvus, Pinecone**             |
| 编排框架 | Airflow, Kubeflow        | **LangChain, LlamaIndex, Semantic Kernel** |
| 推理引擎 | TorchServe, Triton       | **vLLM, Text Generation Interface (TGI)**  |
| 安全防护 | —                       | **Llama Guard, NeMo Guardrails**           |

---

### ✅ 总结：LLMOps 是 MLOps 的“超集 + 变形”

> * ​**LLMOps 继承了 MLOps 的核心思想**​：版本控制、自动化、监控、CI/CD
> * ​**但 LLMOps 新增了三大支柱**​：
    >   1. **Prompt 与上下文工程**
>   2. **RAG 与知识管理**
>   3. **交互式安全与体验监控**

| 如果你熟悉 MLOps，那么 LLMOps =

> **MLOps + NLP 工程 + 对话系统运维 + 大模型推理优化 + AI 安全**

---

### 🚀 实践建议

* **不要直接套用 MLOps 流程到 LLM 项目**
* ​**必须引入 LLM 专属工具链**​（如 Langfuse + RAGAS + vLLM）
* **建立“Prompt + Knowledge + Model”三位一体的版本管理**
* **将“用户体验指标”纳入核心监控体系**
