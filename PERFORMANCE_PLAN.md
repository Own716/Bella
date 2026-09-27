# BellaBox 极致性能优化与功耗控制方案 (PERFORMANCE_PLAN.md)

## 1. 性能指标目标 (Key Performance Indicators)

| 指标维度 | 目标基准 (Benchmark Target) | 业界传统客户端均值 | 优化幅度 |
| :--- | :--- | :--- | :--- |
| **应用冷启动时间** | $\le 450\text{ ms}$ | $\approx 1200\text{ ms}$ | 提升 62% |
| **代理连接握手建立** | $\le 120\text{ ms}$ (优选节点) | $\approx 350\text{ ms}$ | 提升 65% |
| **待机后台内存占用** | $\le 38\text{ MB}$ (RSS) | $\approx 85\text{ MB}$ | 降低 55% |
| **满载代理吞吐 CPU 占用** | $\le 4.5\%$ (100Mbps 持续跑满) | $\approx 14.0\%$ | 降低 67% |
| **息屏长效后台电耗** | $\le 0.4\% / \text{小时}$ | $\approx 1.8\% / \text{小时}$ | 降低 77% |
| **UI 列表帧率 (60/120Hz)**| 0 掉帧 (Jank Rate $< 0.1\%$) | Jank Rate $\approx 2.5\%$ | 极致丝滑 |

---

## 2. TUN 与系统底层优化

1. **直接缓冲区零拷贝 (Zero-Copy Buffer Reuse)**：
   - TUN 虚拟网络设备在处理高并发小包时频繁创建 byte 数组会带来高频 GC 停顿。BellaBox 在 Go 内核与 Kotlin 边界间采用环形缓冲池（RingBuffer Pool），重用数据帧内存。
2. **多队列与单系统调用合并**：
   - 充分利用 Android Linux 内核 TUN 驱动的 `IFF_MULTI_QUEUE` 特性，在多核处理器上均衡数据分发。
3. **针对性 MTU 动态协商**：
   - 默认采用 9000（Jumbo Frame）或自适应网络路径探测（PMTU Discovery），杜绝因分片引发的额外 CPU 损耗。

---

## 3. UI 渲染与内存管理防抖优化

1. **防抖与节流刷新 (Throttled Refresh Rate)**：
   - 严禁每几百毫秒全量重绘界面。网络流速与累计流量收集流在后台汇聚后，固定按照 1000ms 步长下发，并使用低开销浮点线性插值（Linear Interpolation）驱动 UI 动效平滑过渡。
2. **Room 数据库按需分批分页**：
   - 针对超大订阅（上千节点列表），采用 Jetpack Paging 3 分页与 DiffUtil，节点状态变更仅刷新特定卡片，杜绝重建整个数据库视图。
3. **休眠节能调度器 (Device Idle Mode Dispatcher)**：
   - 监听系统 `PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED`，在设备进入 Doze 深度休眠时，自动暂停非必要的心跳探测与后台状态轮询。
