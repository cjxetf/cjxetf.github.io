# TensorFlow Serving的优化方案


#### ✅ 一、**模型层优化 —— 从源头减负**

1. **图优化 + XLA 编译**
   * 使用 `@tf.function` 导出 `SavedModel`，启用算子融合
   * 启用 XLA：`config.graph_options.optimizer_options.global_jit_level = tf.OptimizerOptions.ON_1`
     → **推理速度提升 20%\~50%**
2. **量化压缩（Quantization）**
   * 训练后量化（PTQ）生成 INT8 模型
   * 模型体积 ↓75%，CPU 推理速度 ↑2\~3x
     → **适合 ResNet、BERT 等大模型**
3. **避免运行时字符串操作**
   * 禁用 `tf.as_string()`、`tf.strings.*` 等昂贵 Op
   * 特征 ID 直接用整数，哈希用 `tf.math.floormod` 替代字符串哈希
     → **消除 Profiler 中高频 toString 热点**

> 🔥 ​**亮点**​：通过 ​**Profiler 自动分析 trace**​，定位 `ParseExample`/`toString` 瓶颈，推动算法团队前置特征工程。

---

#### ✅ 二、**服务层优化 —— 榨干 TF Serving 能力**

1. **动态批处理（Dynamic Batching）** ← **最关键！**

   ```
   max_batch_size: 32
   batch_timeout_micros: 1000  # 1ms
   ```

   * GPU 利用率从 30% → 85%，QPS 提升 5\~10x
   * 适用于图像、Embedding 等可批场景
2. **线程池调优**

   * `intra_op_parallelism = CPU 核数`
   * `inter_op_parallelism = 1~2`
     → 避免线程竞争，降低上下文切换开销
3. **多实例 + NUMA 绑定**

   * 单机部署多个 TF Serving 实例（非多线程）
   * 用 `numactl` 绑定 CPU 与内存
     → 提升缓存局部性，减少跨 NUMA 访问延迟

---

#### ✅ 三、**系统与部署优化 —— 底层加速**

1. **编译优化**
   * Bazel 编译时启用 AVX2/AVX512：`--copt=-mavx2 --copt=-mfma`
     → 矩阵运算加速 30%
2. **内存分配器**
   * 使用 `tcmalloc` 替代 glibc malloc
   * 减少内存碎片，提升高并发稳定性
3. **GPU 加速（如适用）**
   * 模型转换为 TensorRT 格式
   * 启用 GPU 内存增长限制，避免 OOM
4. **预热（Warm-up）**
   * 启动后自动发送 dummy 请求
     → **避免首次请求冷启动抖动，保障发布 SLA**

---

#### ✅ 四、**架构与平台级优化 —— 工程体系化**

1. **绕过 tf.Example，直传 Tensor**
   * 客户端（Java/Go）直接构造 `TensorProto`
   * 模型签名接收原始数值输入（`user_id: int64[]`）
     → **跳过 ParseExample，P99 延迟 ↓30%**
2. **混合部署策略**
   * 小模型 → CPU 实例（JPMML/TF Serving）
   * 大模型 → GPU + TensorRT
     → **资源成本降低 40%**
3. **可观测 + 自愈**
   * 监控：batch queue length、GPU util、P99
   * 告警：当 `toString` 耗时 > 5% 时触发模型 review
   * 自动扩缩容：基于 QPS / GPU 利用率 HPA

---

### 🏆 **总结**

> ​**Situation**​：我们在广告 CTR 预估场景中，TF Serving P99 延迟达 60ms，QPS 仅 200，无法支撑大促流量。
> ​**Task**​：需在 2 周内将 P99 降至 20ms 以内，QPS 提升至 800+。
> ​**Action**​：
>
> * 用 Profiler 定位到 `ParseExample` 和 `toString` 占比超 50%；
> * 推动特征工程前置，客户端直传整数 ID；
> * 启用 Dynamic Batching + INT8 量化 + tcmalloc；
> * 改造模型输入为 direct Tensor，绕过 Example 解析。
    >   ​**Result**​：
> * ​**P99 降至 18ms，QPS 达 850**​，单 GPU 成本下降 60%；
> * 方案沉淀为平台规范，新模型上线自动校验 Profiler 报告。

---

### 💡 **总结**

> “**TF Serving 的性能瓶颈，80% 不在模型本身，而在特征处理与部署方式。**
> 我们坚持：​**能不在图里做的字符串操作，就不要做；能不在服务端做的特征工程，就不要做。**​”
