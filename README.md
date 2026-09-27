# BellaBox (Bella)

> 基于 Sing-box 官方最新 Alpha / Testing 内核（v1.15.0-alpha.9）的高性能、现代美学 Android 代理客户端。

[![Sing-box Core](https://img.shields.io/badge/Sing--box%20Core-1.15.0--alpha.9-blue.svg)](https://github.com/SagerNet/sing-box)
[![Channel](https://img.shields.io/badge/Channel-Testing%20%2F%20Pre--release-orange.svg)](https://github.com/SagerNet/sing-box/tree/testing)
[![Android](https://img.shields.io/badge/Android-8.0%2B%20(API%2026%2B)-green.svg)](https://developer.android.com)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVI%20%2B%20Compose-purple.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-GPLv3-red.svg)](./LICENSE)

---

## 📖 项目简介

**BellaBox**（代码库名：**Bella**）是一款全新打造的现代化 Android 代理客户端。项目立足于 2026 年现代移动端应用美学与极致性能规范，抛弃传统代理客户端厚重表格式、生硬方块直角与繁杂设置面板的陈旧模式，采用**现代大圆角（16~28dp）**、**悬浮底栏（Floating Navigation Bar）**、**轻度毛玻璃质感**与**统一设计系统**，将核心网络控制与可视化仪表盘无缝融合。

BellaBox 深度集成 **Sing-box 官方最新 Alpha / Testing 内核（v1.15.0-alpha.9）**，坚持真实网络实现，拒绝任何虚假假数据与假测速。通过高效的 TUN 堆栈、智能故障转移与一致性哈希策略组、动态规则分流、多协议 DNS 解析，以及多维真实网络质量评估，为用户提供低 CPU、低内存占用、低功耗且稳定可靠的长效网络加速体验。

---

## 🚀 核心特性与技术亮点

### 1. 官方最新 Testing 内核深度集成
- **核心版本**：Sing-box `1.15.0-alpha.9`（基于官方 testing 分支，commit `af60b5e`）。
- **动态版本演进**：集成 `CoreVersionChecker`，实时检测官方 GitHub Releases 与 testing 分支，保证随时跟踪官方最新功能与协议修复。
- **协议覆盖**：全面支持 VLESS (Reality, Vision), VMess, Trojan, Shadowsocks, Hysteria 2, TUIC v5, WireGuard, AnyTLS, Naive, SOCKS5, HTTP 等主流协议。

### 2. 极致性能与低开销 TUN 架构
- **高性能 TUN**：结合 Android `VpnService` 与 sing-tun 驱动，全面支持 TCP/UDP/DNS 全局代理与分流。
- **内存与功耗优化**：优化数据包缓冲区复用与 Goroutine 调度，杜绝内存泄漏；针对休眠与息屏场景实现智能省电模式。
- **网络自愈与重连机制**：完善的 `NetworkCallback` 状态机，平滑处理 Wi-Fi 与移动网络无缝切换，防止假死与悬挂连接。

### 3. 2026 现代美学设计系统 (Design System)
- **悬浮导航底栏**：圆角悬浮形态，自适应安全边距与系统手势交互。
- **流体式视觉中心**：首页中央动态呼吸连接态、实时平滑速率曲线（防高频抖动刷新）、延迟雷达式指示与累计流量卡片。
- **自适应响应式布局**：完美兼容折叠屏、横竖屏、平板以及主流打孔/刘海屏。
- **无感深浅主题切换**：支持深色、浅色及跟随系统，切换无需重启 Activity。

### 4. 真实多维度测速与质量评估系统
- **拒绝假测速**：独立隔离测速引擎，严防并发测速污染主 VPN 隧道。
- **分段延迟分析**：细分 TCP 建连、TLS 握手、代理协议握手与 HTTP 端到端总时延。
- **可解释网络质量评分（Quality Score）**：综合延迟基准、抖动方差、丢包率、历史成功率及持续可用时长计算透明评分，点击即可查看评分细则。

### 5. 智能策略组与规则路由
- **高级策略**：手动选择、最低延迟（URLTest）、最低真实负载、随机、轮询、一致性哈希（Consistent Hashing）、故障转移（Failover，具备冷却时间与抖动保护）。
- **全能路由**：支持 Domain, IP-CIDR, GeoIP, Geosite, Rule-Set, 端口、应用包名等多维条件分流。
- **应用级分流**：支持黑名单/白名单模式与按策略分流。

### 6. 健壮的后台长期驻留与安全防护
- **前台保活服务**：规范的前台通知，提供实时连接态与一键切换/断开操作。
- **敏感信息安全**：配置中密钥、UUID、节点信息与订阅链接使用 Android Keystore 加密存储；日志展示自动进行脱敏处理。

---

## 🛠 技术栈与工程架构

| 模块 / 层面 | 技术方案 |
| :--- | :--- |
| **开发语言** | Kotlin 2.0.21 / Go 1.25+ / C++ |
| **UI 框架** | Jetpack Compose (Material 3) |
| **异步框架** | Kotlin Coroutines & StateFlow / SharedFlow |
| **本地数据库** | Room 2.6.1 + SQLite |
| **持久化配置** | Jetpack DataStore Preferences |
| **网络引擎** | Sing-box Core (v1.15.0-alpha.9) via Android libbox.aar |
| **网络底层** | Android VpnService, Tun2Socks, Sing-tun |
| **网络客户端** | OkHttp 4.12+ (用于订阅与工具链) |
| **构建系统** | Gradle 8.13 + AGP 8.8.1 |
| **最低系统要求** | Android 8.0 (API 26) 及以上 |
| **目标系统版本** | Android 15 / 16 (API 35+) |

---

## 📁 模块化项目结构

```
Bella/
├── app/                        # 应用主入口、Application 实例、DI 注册
├── core-common/                # 基础工具类、协程调度器、日志组件、基类定义
├── core-model/                 # 核心数据模型 (Node, Strategy, Route, DNS, Traffic)
├── core-database/              # Room 数据库实体、DAO 接口及数据库升级迁移
├── core-network/               # HTTP 客户端、订阅拉取解析、分段测速引擎、IP 查询
├── singbox-engine/             # Sing-box 配置生成器、校验器、libbox 适配器与状态机
├── vpn-service/                # Android VpnService 实现、TUN 接口保护、前台服务通知
├── feature-home/               # 首页控制器：连接中心、实时速率曲面图、状态卡片
├── feature-nodes/              # 节点管理：搜索、多条件筛选、排序、批量测速与详情
├── feature-subscriptions/      # 订阅中心：订阅管理、智能去重指纹、自动更新
├── feature-groups/             # 策略组管理：URLTest、故障转移、一致性哈希配置
├── feature-rules/              # 分流规则中心：Direct/Proxy/Block、Rule-Set 管理
├── feature-dns/                # DNS 配置：DoH/DoT/FakeIP/直连与代理双解析配置
├── feature-speedtest/          # 测速中心：节点测速、分段耗时分析、质量评分看板
├── feature-tools/              # 网络工具包：Ping, TCP Ping, DNS 诊断, 公网 IP 详情
├── feature-settings/           # 分组设置：TUN、分流、通知、后台保护、主题设置
└── feature-about/              # 关于页面：内核版本、Commit、构建时间、ABI、导出日志
```

---

## 🔨 构建与开发指南

### 环境准备
1. **JDK**：OpenJDK 17（推荐 Eclipse Adoptium Temurin 17）
2. **Android SDK**：API 35 平台及 Build-Tools 35.0.0+
3. **Android NDK**：NDK r25+ (例如 25.0.8775105)
4. **Go**：Go 1.22+（用于构建或验证 libbox）
5. **Gradle**：已内置 Gradle Wrapper (Gradle 8.13)

### 编译构建
- **Debug APK**：
  ```powershell
  .\gradlew assembleDebug
  ```
  输出路径：`app/build/outputs/apk/debug/app-debug.apk`

- **Release APK**：
  ```powershell
  .\gradlew assembleRelease
  ```
  输出路径：`app/build/outputs/apk/release/app-release.apk`

- **运行单元测试**：
  ```powershell
  .\gradlew test
  ```

---

## 📄 许可证说明

本项目遵循 **GPLv3 (GNU General Public License v3.0)** 许可证开源。所包含与引用的第三方库及 Sing-box 官方组件均已完整核实许可证兼容性，详见 [THIRD_PARTY_LICENSES.md](./THIRD_PARTY_LICENSES.md)。
