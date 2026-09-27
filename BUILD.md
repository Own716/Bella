# BellaBox 构建与打包指南 (BUILD.md)

## 1. 软件环境与先决条件

- **操作系统**：Windows 10 / 11, macOS, 或 Linux
- **JDK**：OpenJDK 17 (推荐 Eclipse Adoptium Temurin 17.0.20+)
- **Android SDK**：
  - Compile SDK: `35`
  - Target SDK: `35`
  - Min SDK: `26` (Android 8.0 Oreo)
  - NDK: `r25+`
- **Gradle**：`8.13` (已内嵌 Wrapper 脚本)
- **Go**：Go 1.22+ (可选，用于重新编译 `libbox.aar`)

---

## 2. 编译产物步骤

### 2.1 编译 Debug 版本
```powershell
.\gradlew assembleDebug
```
产物文件路径：
`app/build/outputs/apk/debug/app-debug.apk`

### 2.2 编译 Release 版本
```powershell
.\gradlew assembleRelease
```
产物文件路径：
`app/build/outputs/apk/release/app-release.apk`

### 2.3 执行全量自动化测试
```powershell
.\gradlew test
```

---

## 3. Sing-box 内核编译 (libbox.aar)

若需要从 Sing-box 官方 testing 分支源码重新构建 `libbox.aar`：
1. 检出 Sing-box testing 分支最新源码。
2. 安装 gomobile 工具链：
   ```powershell
   go install github.com/sagernet/gomobile/cmd/gomobile@latest
   go install github.com/sagernet/gomobile/cmd/gobind@latest
   ```
3. 运行构建脚本：
   ```powershell
   go run ./cmd/internal/build_libbox -platform android/arm64
   ```
4. 构建脚本会自动将 `libbox.aar` 分发至 `Bella/singbox-engine/libs/` 与 `Bella/app/libs/`。
