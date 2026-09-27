package com.bellabox.app.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

interface AppStrings {
    // Navigation
    val navHome: String
    val navNodes: String
    val navGroups: String
    val navRules: String
    val navTools: String
    val navSettings: String
    val navAbout: String

    // Pulse Button & Connection Status
    val statusIdle: String
    val statusStarting: String
    val statusConnecting: String
    val statusConnected: String
    val statusReconnecting: String
    val statusStopping: String
    val statusStopped: String
    val statusFailed: String
    val btnConnect: String
    val btnConnected: String
    val btnConnecting: String
    val btnStarting: String
    val btnStopping: String
    val btnReconnect: String
    val btnRetry: String

    // Home Screen
    val homeTestingCore: String
    val homeActiveOutbound: String
    val homeChange: String
    val homeSelect: String
    val homeNoProxyNode: String
    val homeRealtimeTraffic: String
    val homeDownload: String
    val homeUpload: String
    val homeTotalDown: String
    val homeTotalUp: String
    val vpnPermissionDenied: String

    // Nodes & Subscription Screen
    val nodesTabNodes: String
    val nodesTabSubscriptions: String
    val nodesCountFormat: (Int) -> String
    val subsCountFormat: (Int) -> String
    val nodesTestAll: String
    val nodesTesting: String
    val nodesSearchHint: String
    val nodesSortDefault: String
    val nodesSortLatency: String
    val nodesSortName: String
    val nodesFilterAll: String
    val nodesEmptyTitle: String
    val nodesEmptyDesc: String
    val subsEmptyTitle: String
    val subsEmptyDesc: String
    val nodesActionAddNode: String
    val nodesActionAddSub: String
    val nodesActionImportLink: String
    val nodesUpdateNow: String
    val nodesLastUpdate: String
    val nodesNeverUpdated: String
    val nodesManualGroup: String

    // Dialogs & Actions
    val actionCancel: String
    val actionSave: String
    val actionDelete: String
    val actionEdit: String
    val actionConfirm: String
    val dialogConfirmDelete: String
    val dialogDeleteSubConfirm: (String) -> String
    val dialogDeleteNodeConfirm: (String) -> String
    val dialogImportTitle: String
    val dialogImportHint: String
    val dialogImportSuccess: (Int) -> String
    val dialogImportFailed: String
    val dialogAddSubTitle: String
    val dialogEditSubTitle: String
    val dialogSubName: String
    val dialogSubUrl: String
    val dialogAutoUpdate: String
    val dialogUpdateIntervalHours: String
    val dialogAddNodeTitle: String
    val dialogEditNodeTitle: String
    val dialogNodeName: String
    val dialogServerAddress: String
    val dialogServerPort: String
    val dialogProtocol: String

    // Groups Screen
    val groupsTitle: String
    val groupsSubtitle: String
    val groupsNewGroup: String
    val groupsEditGroup: String
    val groupsDeleteGroup: String
    val groupsDeleteConfirm: (String) -> String
    val groupsGroupName: String
    val groupsStrategyType: String
    val groupsStrategyManual: String
    val groupsStrategyUrlTest: String
    val groupsStrategyFallback: String
    val groupsStrategyConsistentHash: String
    val groupsStrategyRoundRobin: String
    val groupsIncludedNodes: String
    val groupsEmptyTitle: String
    val groupsEmptyDesc: String
    val groupsSelectAtLeastOne: String

    // Rules Screen
    val rulesTitle: String
    val rulesSubtitle: String
    val rulesNewRule: String
    val rulesEditRule: String
    val rulesDeleteRule: String
    val rulesDeleteConfirm: (String) -> String
    val rulesResetDefaults: String
    val rulesResetConfirmTitle: String
    val rulesResetConfirmDesc: String
    val rulesRuleType: String
    val rulesAction: String
    val rulesActionProxy: String
    val rulesActionDirect: String
    val rulesActionBlock: String
    val rulesPayload: String
    val rulesPayloadHint: String
    val rulesDescription: String
    val rulesEnabled: String
    val rulesEmptyTitle: String
    val rulesEmptyDesc: String

    // Tools Screen
    val toolsTitle: String
    val toolsSubtitle: String
    val toolsPublicIpTitle: String
    val toolsPublicIpDesc: String
    val toolsCheckIp: String
    val toolsChecking: String
    val toolsIpAddress: String
    val toolsIsp: String
    val toolsLocation: String
    val toolsProxyStatus: String
    val toolsStatusProxied: String
    val toolsStatusDirect: String
    val toolsTcpPingTitle: String
    val toolsTcpPingDesc: String
    val toolsHost: String
    val toolsPort: String
    val toolsStartPing: String
    val toolsPinging: String
    val toolsPingSuccess: (Long) -> String
    val toolsPingFailed: String
    val toolsDnsTitle: String
    val toolsDnsDesc: String
    val toolsQueryDomain: String
    val toolsStartLookup: String
    val toolsResolving: String
    val toolsQueryResult: String
    val toolsQueryFailed: String

    // Settings Screen
    val settingsTitle: String
    val settingsSubtitle: String
    val settingsAppearance: String
    val settingsThemeMode: String
    val settingsThemeSystem: String
    val settingsThemeLight: String
    val settingsThemeDark: String
    val settingsLanguage: String
    val settingsLangZh: String
    val settingsLangEn: String
    val settingsDynamicColor: String
    val settingsDynamicColorDesc: String
    val settingsConnection: String
    val settingsAutoConnectBoot: String
    val settingsAutoConnectBootDesc: String
    val settingsAutoReconnectNet: String
    val settingsAutoReconnectNetDesc: String
    val settingsTunStack: String
    val settingsTunMtu: String
    val settingsTunMtuDesc: String
    val settingsTunStrictRoute: String
    val settingsTunStrictRouteDesc: String
    val settingsBypassLan: String
    val settingsBypassLanDesc: String
    val settingsDns: String
    val settingsFakeIp: String
    val settingsFakeIpDesc: String
    val settingsDnsDirect: String
    val settingsDnsRemote: String
    val settingsSpeedTest: String
    val settingsSpeedTestUrl: String
    val settingsSpeedTestTimeout: String
    val settingsSpeedTestConcurrency: String
    val settingsAboutKernel: String
    val settingsAboutKernelDesc: String

    // About Screen
    val aboutTitle: String
    val aboutCoreStatus: String
    val aboutAppVersion: String
    val aboutKernelBuild: String
    val aboutArch: String
    val aboutCheckUpdate: String
    val aboutChecking: String
    val aboutUpToDate: String
    val aboutNewVersionAvailable: (String) -> String
    val aboutViewLogs: String
    val aboutKernelLogs: String
    val aboutCopyLogs: String
    val aboutLogsCopied: String
    val aboutClose: String
    val aboutNoLogs: String
}

object ZhStrings : AppStrings {
    override val navHome = "首页"
    override val navNodes = "节点与订阅"
    override val navGroups = "策略组"
    override val navRules = "路由分流"
    override val navTools = "网络工具"
    override val navSettings = "系统设置"
    override val navAbout = "关于"

    override val statusIdle = "未连接"
    override val statusStarting = "正在启动核心…"
    override val statusConnecting = "正在建立隧道…"
    override val statusConnected = "已安全连接"
    override val statusReconnecting = "网络波动重连中…"
    override val statusStopping = "正在断开服务…"
    override val statusStopped = "服务已停止"
    override val statusFailed = "连接发生错误"

    override val btnConnect = "一键连接"
    override val btnConnected = "已连接"
    override val btnConnecting = "正在连接"
    override val btnStarting = "正在启动"
    override val btnStopping = "正在停止"
    override val btnReconnect = "重新连接"
    override val btnRetry = "重试连接"

    override val homeTestingCore = "测试核心"
    override val homeActiveOutbound = "当前选中节点"
    override val homeChange = "切换"
    override val homeSelect = "选择"
    override val homeNoProxyNode = "暂未选择任何代理节点"
    override val homeRealtimeTraffic = "实时网络流量"
    override val homeDownload = "下载速率"
    override val homeUpload = "上传速率"
    override val homeTotalDown = "累计下载"
    override val homeTotalUp = "累计上传"
    override val vpnPermissionDenied = "VPN 权限未授予，无法建立代理通道"

    override val nodesTabNodes = "节点列表"
    override val nodesTabSubscriptions = "订阅管理"
    override val nodesCountFormat: (Int) -> String = { "共 $it 个可用节点" }
    override val subsCountFormat: (Int) -> String = { "共 $it 个托管订阅" }
    override val nodesTestAll = "全部测速"
    override val nodesTesting = "测速中…"
    override val nodesSearchHint = "搜索节点名称、服务器或协议"
    override val nodesSortDefault = "默认排序"
    override val nodesSortLatency = "延迟最低"
    override val nodesSortName = "按名称"
    override val nodesFilterAll = "全部协议"
    override val nodesEmptyTitle = "暂无可用节点"
    override val nodesEmptyDesc = "请导入您的代理订阅链接或手动添加节点。"
    override val subsEmptyTitle = "暂无托管订阅"
    override val subsEmptyDesc = "点击右下角按钮添加订阅链接"
    override val nodesActionAddNode = "添加节点"
    override val nodesActionAddSub = "添加订阅"
    override val nodesActionImportLink = "导入链接"
    override val nodesUpdateNow = "立即更新"
    override val nodesLastUpdate = "最近更新"
    override val nodesNeverUpdated = "从未更新"
    override val nodesManualGroup = "手动添加"

    override val actionCancel = "取消"
    override val actionSave = "保存"
    override val actionDelete = "删除"
    override val actionEdit = "编辑"
    override val actionConfirm = "确认"
    override val dialogConfirmDelete = "确认删除"
    override val dialogDeleteSubConfirm: (String) -> String = { "确定要删除订阅 \"$it\" 吗？相关的节点也将被移除。" }
    override val dialogDeleteNodeConfirm: (String) -> String = { "确定要删除节点 \"$it\" 吗？" }
    override val dialogImportTitle = "导入单条或批量节点链接"
    override val dialogImportHint = "支持粘贴 vless://, vmess://, ss://, trojan://, tuic://, hysteria2:// 链接或 HTTP 订阅地址"
    override val dialogImportSuccess: (Int) -> String = { "成功导入 $it 个节点" }
    override val dialogImportFailed = "未解析到有效节点，请检查链接格式"
    override val dialogAddSubTitle = "添加新订阅"
    override val dialogEditSubTitle = "编辑订阅"
    override val dialogSubName = "订阅名称"
    override val dialogSubUrl = "订阅链接 URL"
    override val dialogAutoUpdate = "自动定时更新"
    override val dialogUpdateIntervalHours = "更新间隔 (小时)"
    override val dialogAddNodeTitle = "手动添加节点"
    override val dialogEditNodeTitle = "编辑节点"
    override val dialogNodeName = "节点名称"
    override val dialogServerAddress = "服务器域名 / IP"
    override val dialogServerPort = "端口"
    override val dialogProtocol = "协议类型"

    override val groupsTitle = "分流策略组"
    override val groupsSubtitle = "自适应故障转移、多线路负载与延迟择优"
    override val groupsNewGroup = "新建策略组"
    override val groupsEditGroup = "编辑策略组"
    override val groupsDeleteGroup = "删除策略组"
    override val groupsDeleteConfirm: (String) -> String = { "确定要删除策略组 \"$it\" 吗？" }
    override val groupsGroupName = "策略组名称"
    override val groupsStrategyType = "策略类型"
    override val groupsStrategyManual = "手动选择 (manual)"
    override val groupsStrategyUrlTest = "自动优选 (urltest)"
    override val groupsStrategyFallback = "故障转移 (fallback)"
    override val groupsStrategyConsistentHash = "一致性哈希 (consistent_hash)"
    override val groupsStrategyRoundRobin = "轮询调度 (round_robin)"
    override val groupsIncludedNodes = "包含的节点"
    override val groupsEmptyTitle = "暂无策略组"
    override val groupsEmptyDesc = "点击右下角按钮创建自定义策略组"
    override val groupsSelectAtLeastOne = "请至少勾选一个节点"

    override val rulesTitle = "路由分流规则"
    override val rulesSubtitle = "高优先级规则自上而下匹配执行"
    override val rulesNewRule = "添加自定义规则"
    override val rulesEditRule = "编辑规则"
    override val rulesDeleteRule = "删除规则"
    override val rulesDeleteConfirm: (String) -> String = { "确定要删除规则 \"$it\" 吗？" }
    override val rulesResetDefaults = "恢复默认规则"
    override val rulesResetConfirmTitle = "恢复默认路由规则"
    override val rulesResetConfirmDesc = "此操作将重置所有路由规则为官方默认分流策略，是否继续？"
    override val rulesRuleType = "规则类型"
    override val rulesAction = "目标动作"
    override val rulesActionProxy = "代理流量 (proxy)"
    override val rulesActionDirect = "直连放行 (direct)"
    override val rulesActionBlock = "拦截阻止 (block)"
    override val rulesPayload = "匹配内容 (Payload)"
    override val rulesPayloadHint = "例如: google.com 或 10.0.0.0/8"
    override val rulesDescription = "规则描述"
    override val rulesEnabled = "启用此规则"
    override val rulesEmptyTitle = "暂无分流规则"
    override val rulesEmptyDesc = "点击右上角按钮恢复默认规则或添加自定义规则"

    override val toolsTitle = "网络实用工具"
    override val toolsSubtitle = "真实 TCP Ping、DNS 解析与公网 IP 诊断"
    override val toolsPublicIpTitle = "公网 IP 与地理位置诊断"
    override val toolsPublicIpDesc = "检测当前出口 IP 及代理生效状态"
    override val toolsCheckIp = "查询出口 IP"
    override val toolsChecking = "查询中…"
    override val toolsIpAddress = "IP 地址"
    override val toolsIsp = "网络运营商 (ISP)"
    override val toolsLocation = "地理位置"
    override val toolsProxyStatus = "代理状态"
    override val toolsStatusProxied = "已通过代理通道"
    override val toolsStatusDirect = "本地直连"
    override val toolsTcpPingTitle = "TCP 握手测速"
    override val toolsTcpPingDesc = "真实网络连接建立延迟探测"
    override val toolsHost = "目标地址 (Host)"
    override val toolsPort = "端口"
    override val toolsStartPing = "开始测速"
    override val toolsPinging = "正在测速…"
    override val toolsPingSuccess: (Long) -> String = { "TCP 握手成功，延迟: $it ms" }
    override val toolsPingFailed = "连接超时或拒绝连接"
    override val toolsDnsTitle = "DNS 递归解析"
    override val toolsDnsDesc = "验证系统与虚拟网卡 DNS 查询返回"
    override val toolsQueryDomain = "查询域名"
    override val toolsStartLookup = "开始解析"
    override val toolsResolving = "解析中…"
    override val toolsQueryResult = "解析结果"
    override val toolsQueryFailed = "DNS 查询超时或无法解析域名"

    override val settingsTitle = "系统与个性化"
    override val settingsSubtitle = "Sing-box 1.15 内核参数、网络策略与界面偏好"
    override val settingsAppearance = "界面与外观"
    override val settingsThemeMode = "主题外观"
    override val settingsThemeSystem = "跟随系统"
    override val settingsThemeLight = "浅色模式"
    override val settingsThemeDark = "深色模式"
    override val settingsLanguage = "界面语言"
    override val settingsLangZh = "简体中文 (默认)"
    override val settingsLangEn = "English"
    override val settingsDynamicColor = "动态主题色 (Material You)"
    override val settingsDynamicColorDesc = "提取系统壁纸主题色，自适应全局控件"
    override val settingsConnection = "连接与自动化"
    override val settingsAutoConnectBoot = "开机自启自动连接"
    override val settingsAutoConnectBootDesc = "手机系统启动后自动启动 VPN 代理通道"
    override val settingsAutoReconnectNet = "网络切换自动重连"
    override val settingsAutoReconnectNetDesc = "Wi-Fi 与蜂窝数据切换时无缝保持在线"
    override val settingsTunStack = "内核网络栈 (TUN)"
    override val settingsTunMtu = "TUN 网卡 MTU"
    override val settingsTunMtuDesc = "默认 9000，推荐范围 1280 - 9000"
    override val settingsTunStrictRoute = "严格全局路由 (Strict Route)"
    override val settingsTunStrictRouteDesc = "杜绝一切 DNS 和应用直连泄漏"
    override val settingsBypassLan = "绕过局域网 (Bypass LAN)"
    override val settingsBypassLanDesc = "私有网段流量不进入代理通道"
    override val settingsDns = "DNS 与域名解析"
    override val settingsFakeIp = "启用 FakeIP 增强"
    override val settingsFakeIpDesc = "加快域名解析与连接建立，降低握手延迟"
    override val settingsDnsDirect = "直连 DNS 服务器"
    override val settingsDnsRemote = "远程代理 DNS 服务器"
    override val settingsSpeedTest = "节点测速配置"
    override val settingsSpeedTestUrl = "测速探测 URL"
    override val settingsSpeedTestTimeout = "测速超时时间 (秒)"
    override val settingsSpeedTestConcurrency = "并发测速限制"
    override val settingsAboutKernel = "关于与内核"
    override val settingsAboutKernelDesc = "内核版本与系统信息"

    override val aboutTitle = "关于 BellaBox"
    override val aboutCoreStatus = "核心组件状态"
    override val aboutAppVersion = "客户端版本"
    override val aboutKernelBuild = "内核版本"
    override val aboutArch = "芯片架构"
    override val aboutCheckUpdate = "检查内核更新"
    override val aboutChecking = "检查中…"
    override val aboutUpToDate = "当前内核已是最新版本"
    override val aboutNewVersionAvailable: (String) -> String = { "发现内核新版本: $it" }
    override val aboutViewLogs = "查看实时内核运行日志"
    override val aboutKernelLogs = "实时内核运行日志"
    override val aboutCopyLogs = "复制日志"
    override val aboutLogsCopied = "日志已复制到剪贴板"
    override val aboutClose = "关闭"
    override val aboutNoLogs = "暂无内核日志输出"
}

object EnStrings : AppStrings {
    override val navHome = "Home"
    override val navNodes = "Nodes & Subs"
    override val navGroups = "Groups"
    override val navRules = "Rules"
    override val navTools = "Tools"
    override val navSettings = "Settings"
    override val navAbout = "About"

    override val statusIdle = "Disconnected"
    override val statusStarting = "Starting core…"
    override val statusConnecting = "Connecting tunnel…"
    override val statusConnected = "Connected"
    override val statusReconnecting = "Reconnecting…"
    override val statusStopping = "Stopping service…"
    override val statusStopped = "Stopped"
    override val statusFailed = "Connection Failed"

    override val btnConnect = "CONNECT"
    override val btnConnected = "CONNECTED"
    override val btnConnecting = "CONNECTING"
    override val btnStarting = "STARTING"
    override val btnStopping = "STOPPING"
    override val btnReconnect = "RECONNECT"
    override val btnRetry = "RETRY"

    override val homeTestingCore = "TESTING CORE"
    override val homeActiveOutbound = "ACTIVE OUTBOUND"
    override val homeChange = "Change"
    override val homeSelect = "Select"
    override val homeNoProxyNode = "No proxy node selected"
    override val homeRealtimeTraffic = "REAL-TIME TRAFFIC FLOW"
    override val homeDownload = "Download"
    override val homeUpload = "Upload"
    override val homeTotalDown = "Total Down"
    override val homeTotalUp = "Total Up"
    override val vpnPermissionDenied = "VPN permission denied, unable to establish tunnel"

    override val nodesTabNodes = "Nodes"
    override val nodesTabSubscriptions = "Subscriptions"
    override val nodesCountFormat: (Int) -> String = { "Total $it available nodes" }
    override val subsCountFormat: (Int) -> String = { "Total $it subscriptions" }
    override val nodesTestAll = "Test All"
    override val nodesTesting = "Testing…"
    override val nodesSearchHint = "Search by name, server or protocol"
    override val nodesSortDefault = "Default"
    override val nodesSortLatency = "Lowest Latency"
    override val nodesSortName = "By Name"
    override val nodesFilterAll = "All Protocols"
    override val nodesEmptyTitle = "No Nodes Found"
    override val nodesEmptyDesc = "Import your subscription URL or paste a proxy share link to get started."
    override val subsEmptyTitle = "No Subscriptions Added"
    override val subsEmptyDesc = "Click the bottom right button to add a subscription URL."
    override val nodesActionAddNode = "Add Node"
    override val nodesActionAddSub = "Add Subscription"
    override val nodesActionImportLink = "Import Link"
    override val nodesUpdateNow = "Update"
    override val nodesLastUpdate = "Last updated"
    override val nodesNeverUpdated = "Never updated"
    override val nodesManualGroup = "Manual"

    override val actionCancel = "Cancel"
    override val actionSave = "Save"
    override val actionDelete = "Delete"
    override val actionEdit = "Edit"
    override val actionConfirm = "Confirm"
    override val dialogConfirmDelete = "Confirm Delete"
    override val dialogDeleteSubConfirm: (String) -> String = { "Are you sure you want to delete subscription \"$it\"? Associated nodes will also be removed." }
    override val dialogDeleteNodeConfirm: (String) -> String = { "Are you sure you want to delete node \"$it\"?" }
    override val dialogImportTitle = "Import Single or Batch Proxy Links"
    override val dialogImportHint = "Supports vless://, vmess://, ss://, trojan://, tuic://, hysteria2:// or HTTP subscription URLs"
    override val dialogImportSuccess: (Int) -> String = { "Successfully imported $it nodes" }
    override val dialogImportFailed = "No valid nodes found, please check link format"
    override val dialogAddSubTitle = "Add Subscription"
    override val dialogEditSubTitle = "Edit Subscription"
    override val dialogSubName = "Subscription Name"
    override val dialogSubUrl = "Subscription URL"
    override val dialogAutoUpdate = "Auto-update"
    override val dialogUpdateIntervalHours = "Interval (Hours)"
    override val dialogAddNodeTitle = "Add Node"
    override val dialogEditNodeTitle = "Edit Node"
    override val dialogNodeName = "Node Name"
    override val dialogServerAddress = "Server Address / IP"
    override val dialogServerPort = "Port"
    override val dialogProtocol = "Protocol"

    override val groupsTitle = "Strategy Groups"
    override val groupsSubtitle = "Adaptive failover, load balancing and latency selection"
    override val groupsNewGroup = "New Group"
    override val groupsEditGroup = "Edit Group"
    override val groupsDeleteGroup = "Delete Group"
    override val groupsDeleteConfirm: (String) -> String = { "Are you sure you want to delete group \"$it\"?" }
    override val groupsGroupName = "Group Name"
    override val groupsStrategyType = "Strategy Type"
    override val groupsStrategyManual = "Manual (selector)"
    override val groupsStrategyUrlTest = "URL Test (Auto Latency)"
    override val groupsStrategyFallback = "Fallback"
    override val groupsStrategyConsistentHash = "Consistent Hash"
    override val groupsStrategyRoundRobin = "Round Robin"
    override val groupsIncludedNodes = "Included Nodes"
    override val groupsEmptyTitle = "No Strategy Groups"
    override val groupsEmptyDesc = "Click the bottom right button to create a custom group"
    override val groupsSelectAtLeastOne = "Please select at least one node"

    override val rulesTitle = "Routing Rules"
    override val rulesSubtitle = "Top-to-bottom rule match priority evaluation"
    override val rulesNewRule = "Add Custom Rule"
    override val rulesEditRule = "Edit Rule"
    override val rulesDeleteRule = "Delete Rule"
    override val rulesDeleteConfirm: (String) -> String = { "Are you sure you want to delete rule \"$it\"?" }
    override val rulesResetDefaults = "Reset to Defaults"
    override val rulesResetConfirmTitle = "Reset Routing Rules"
    override val rulesResetConfirmDesc = "This will reset all rules to the official default routing policy. Continue?"
    override val rulesRuleType = "Rule Type"
    override val rulesAction = "Action"
    override val rulesActionProxy = "Proxy"
    override val rulesActionDirect = "Direct"
    override val rulesActionBlock = "Block"
    override val rulesPayload = "Payload"
    override val rulesPayloadHint = "e.g. google.com or 10.0.0.0/8"
    override val rulesDescription = "Description"
    override val rulesEnabled = "Enable this rule"
    override val rulesEmptyTitle = "No Routing Rules"
    override val rulesEmptyDesc = "Click the top right button to reset to defaults or add custom rules"

    override val toolsTitle = "Network Diagnostics"
    override val toolsSubtitle = "Real TCP Ping, DNS Query, and Public IP inspection"
    override val toolsPublicIpTitle = "Public IP & Geo Location"
    override val toolsPublicIpDesc = "Inspect current outbound IP and proxy routing state"
    override val toolsCheckIp = "Check Outbound IP"
    override val toolsChecking = "Checking…"
    override val toolsIpAddress = "IP Address"
    override val toolsIsp = "ISP"
    override val toolsLocation = "Location"
    override val toolsProxyStatus = "Proxy Status"
    override val toolsStatusProxied = "Proxied via Tunnel"
    override val toolsStatusDirect = "Direct Outbound"
    override val toolsTcpPingTitle = "TCP Handshake Ping"
    override val toolsTcpPingDesc = "Real network handshake latency probe"
    override val toolsHost = "Host"
    override val toolsPort = "Port"
    override val toolsStartPing = "Start Ping"
    override val toolsPinging = "Pinging…"
    override val toolsPingSuccess: (Long) -> String = { "Handshake successful, latency: $it ms" }
    override val toolsPingFailed = "Connection timed out or refused"
    override val toolsDnsTitle = "DNS Query"
    override val toolsDnsDesc = "Verify DNS resolution via local and virtual interface"
    override val toolsQueryDomain = "Domain"
    override val toolsStartLookup = "Lookup"
    override val toolsResolving = "Resolving…"
    override val toolsQueryResult = "Result"
    override val toolsQueryFailed = "DNS query failed or timed out"

    override val settingsTitle = "Settings & Preferences"
    override val settingsSubtitle = "Sing-box 1.15 core parameters, network policy & preferences"
    override val settingsAppearance = "Appearance & UI"
    override val settingsThemeMode = "Theme Mode"
    override val settingsThemeSystem = "Follow System"
    override val settingsThemeLight = "Light Mode"
    override val settingsThemeDark = "Dark Mode"
    override val settingsLanguage = "Language"
    override val settingsLangZh = "简体中文"
    override val settingsLangEn = "English"
    override val settingsDynamicColor = "Dynamic Color (Material You)"
    override val settingsDynamicColorDesc = "Extract color scheme from system wallpaper"
    override val settingsConnection = "Connection & Automation"
    override val settingsAutoConnectBoot = "Connect on Boot"
    override val settingsAutoConnectBootDesc = "Automatically start proxy tunnel on system boot"
    override val settingsAutoReconnectNet = "Auto-reconnect on Network Change"
    override val settingsAutoReconnectNetDesc = "Maintain active connection across Wi-Fi and mobile data"
    override val settingsTunStack = "Kernel Network Stack (TUN)"
    override val settingsTunMtu = "TUN MTU"
    override val settingsTunMtuDesc = "Default 9000, recommended 1280 - 9000"
    override val settingsTunStrictRoute = "Strict Route"
    override val settingsTunStrictRouteDesc = "Prevent DNS and connection leaks"
    override val settingsBypassLan = "Bypass LAN"
    override val settingsBypassLanDesc = "Private LAN subnets bypass proxy tunnel"
    override val settingsDns = "DNS & Domain Resolution"
    override val settingsFakeIp = "Enable FakeIP"
    override val settingsFakeIpDesc = "Accelerate DNS resolution and lower connection latency"
    override val settingsDnsDirect = "Direct DNS Server"
    override val settingsDnsRemote = "Remote DNS Server"
    override val settingsSpeedTest = "Speed Test Configuration"
    override val settingsSpeedTestUrl = "Speed Test Probe URL"
    override val settingsSpeedTestTimeout = "Timeout (Seconds)"
    override val settingsSpeedTestConcurrency = "Concurrency Limit"
    override val settingsAboutKernel = "About & Kernel"
    override val settingsAboutKernelDesc = "Kernel version and system diagnostics"

    override val aboutTitle = "About BellaBox"
    override val aboutCoreStatus = "Core Components"
    override val aboutAppVersion = "Client Version"
    override val aboutKernelBuild = "Kernel Version"
    override val aboutArch = "Architecture"
    override val aboutCheckUpdate = "Check Kernel Update"
    override val aboutChecking = "Checking…"
    override val aboutUpToDate = "Kernel is up to date"
    override val aboutNewVersionAvailable: (String) -> String = { "New kernel version found: $it" }
    override val aboutViewLogs = "View Real-Time Kernel Logs"
    override val aboutKernelLogs = "Real-Time Kernel Logs"
    override val aboutCopyLogs = "Copy Logs"
    override val aboutLogsCopied = "Logs copied to clipboard"
    override val aboutClose = "Close"
    override val aboutNoLogs = "No kernel log output"
}

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { ZhStrings }

@Composable
fun ProvideAppStrings(
    language: String,
    content: @Composable () -> Unit
) {
    val strings = when (language) {
        "en" -> EnStrings
        else -> ZhStrings
    }
    CompositionLocalProvider(
        LocalAppStrings provides strings,
        content = content
    )
}
