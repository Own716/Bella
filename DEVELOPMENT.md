# BellaBox 开发者开发规范与指南 (DEVELOPMENT.md)

## 1. 代码规范与架构约束

- **严禁 God Class**：Activity 只负责挂载 Compose 视图容器；严禁在 Activity 或 Service 中堆砌过千行业务代码。
- **状态单一事实源**：连接状态全部收归于 `ConnectionStateMachine`，UI 仅以只读方式订阅其 `StateFlow<ConnectionState>`。
- **配置编译与预检分离**：所有 Sing-box 配置必须经由 `SingboxConfigBuilder` 生产，并经由 `SingboxConfigValidator` 校验无误后方可下发给内核。
- **禁止硬编码多语言文本**：所有面向用户的字符串必须在 `strings.xml` 中统一定义，并同步维护 `values/`, `values-zh-rCN/`, `values-zh-rTW/`, `values-ja/`。
- **安全第一**：密钥、密码、UUID 等敏感字段绝不能明文记录在普通日志中，UI 默认以掩码形式展示。
