# 变更日志 (CHANGELOG.md)

所有针对 BellaBox 项目的重大改进与版本迭代均记录于此。

## [1.0.0-alpha.1] - 2026-09-27

### 新增 (Added)
- 初始化基于 Sing-box 官方最新 testing 分支（`v1.15.0-alpha.9`，Commit `af60b5e`）的内核集成方案。
- 编译并植入匹配的官方最新 `libbox.aar` 二进制包。
- 落地 Clean Architecture 架构体系，构建 `core-common`, `core-model`, `core-database`, `core-network`, `singbox-engine`, `vpn-service`, `feature-*` 模块结构。
- 引入统一连接状态机 `ConnectionStateMachine`（8 大生命周期状态）。
- 实现 2026 年现代美学 UI 设计系统：悬浮底栏、圆角设计（14dp~28dp）、平滑速率实时曲线图、呼吸式核心连接控制盘。
- 支持真实多阶段测速引擎（TCP Connect, TLS Handshake, HTTP 端到端时延）。
- 实现节点指纹识别引擎与去重机制。
- 完善 VLESS (Reality), VMess, Trojan, Shadowsocks, Hysteria 2, TUIC 配置支持。
