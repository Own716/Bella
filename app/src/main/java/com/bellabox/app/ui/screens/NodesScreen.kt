package com.bellabox.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bellabox.app.ui.components.NodeCard
import com.bellabox.app.ui.theme.BellaShapes
import com.bellabox.app.ui.theme.StatusConnected
import com.bellabox.core.model.ProtocolType
import com.bellabox.core.model.ProxyNode
import com.bellabox.core.model.Subscription
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodesScreen(
    viewModel: NodesViewModel,
    activeNodeId: Long?,
    onSelectNode: (ProxyNode) -> Unit,
    modifier: Modifier = Modifier
) {
    val nodes by viewModel.nodes.collectAsState()
    val subscriptions by viewModel.subscriptions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()
    val selectedProtocol by viewModel.selectedProtocol.collectAsState()
    val isTesting by viewModel.isTesting.collectAsState()
    val updatingSubId by viewModel.updatingSubId.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Dialog States
    var showImportUrlDialog by remember { mutableStateOf(false) }
    var showAddSubDialog by remember { mutableStateOf(false) }
    var showAddManualNodeDialog by remember { mutableStateOf(false) }
    var editingNode by remember { mutableStateOf<ProxyNode?>(null) }
    var deletingNode by remember { mutableStateOf<ProxyNode?>(null) }
    var editingSub by remember { mutableStateOf<Subscription?>(null) }
    var deletingSub by remember { mutableStateOf<Subscription?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header with Tab Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (selectedTabIndex == 0) "节点列表" else "订阅管理",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (selectedTabIndex == 0) "共 ${nodes.size} 个可用节点" else "共 ${subscriptions.size} 个托管订阅",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (selectedTabIndex == 0) {
                    Button(
                        onClick = { viewModel.testAllNodes() },
                        shape = BellaShapes.small,
                        enabled = !isTesting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("测速中&#8230;", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Rounded.Speed, contentDescription = "Test", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("全部测速", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tabs: [节点列表 | 订阅管理]
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(BellaShapes.medium)
                    .padding(vertical = 4.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("节点列表 (${nodes.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("订阅管理 (${subscriptions.size})", fontWeight = FontWeight.Bold) }
                )
            }

            // Operation Message Banner
            if (operationMessage != null) {
                Card(
                    shape = BellaShapes.small,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = operationMessage!!,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearOperationMessage() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (selectedTabIndex == 0) {
                // TAB 0: NODES LIST
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = BellaShapes.medium,
                    placeholder = { Text("搜索节点名称、服务器或协议", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search", modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(imageVector = Icons.Rounded.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true
                )

                // Protocol & Sort Filter Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedProtocol == null,
                            onClick = { viewModel.setProtocolFilter(null) },
                            label = { Text("全部协议", fontSize = 12.sp) },
                            shape = BellaShapes.small
                        )
                    }
                    items(ProtocolType.entries) { proto ->
                        FilterChip(
                            selected = selectedProtocol.equals(proto.name, ignoreCase = true),
                            onClick = {
                                if (selectedProtocol.equals(proto.name, ignoreCase = true)) {
                                    viewModel.setProtocolFilter(null)
                                } else {
                                    viewModel.setProtocolFilter(proto.name)
                                }
                            },
                            label = { Text(proto.label, fontSize = 12.sp) },
                            shape = BellaShapes.small
                        )
                    }
                }

                if (nodes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = BellaShapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CloudDownload,
                                    contentDescription = "Empty",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "暂无可用节点",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "导入订阅或添加分享链接即可快速畅联",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showAddSubDialog = true },
                                    shape = BellaShapes.medium,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("添加订阅")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { showImportUrlDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("导入分享链接")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(nodes, key = { it.id }) { node ->
                            NodeCard(
                                node = node,
                                isSelected = node.id == activeNodeId,
                                onSelect = { onSelectNode(node) },
                                onFavoriteToggle = { viewModel.toggleFavorite(node) },
                                onTestLatency = { viewModel.testSingleNode(node) },
                                onEdit = { editingNode = node },
                                onDelete = { deletingNode = node }
                            )
                        }
                    }
                }
            } else {
                // TAB 1: SUBSCRIPTION MANAGEMENT
                if (subscriptions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = BellaShapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Sync,
                                    contentDescription = "No Subs",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "暂无托管订阅",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "添加订阅链接后，系统将自动定时更新并智能保留节点收藏与测速记录。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showAddSubDialog = true },
                                    shape = BellaShapes.medium,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("新建订阅")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(subscriptions, key = { it.id }) { sub ->
                            Card(
                                shape = BellaShapes.large,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = sub.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = sub.url,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(BellaShapes.small)
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${sub.nodeCount} 节点",
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Last update time & status
                                    val timeStr = if (sub.lastUpdate > 0) {
                                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                        sdf.format(Date(sub.lastUpdate))
                                    } else "未曾更新"
                                    Text(
                                        text = "上次更新: $timeStr",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )

                                    if (sub.lastStatusMessage.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = sub.lastStatusMessage,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Action buttons row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val isUpdating = updatingSubId == sub.id
                                        Button(
                                            onClick = { viewModel.updateSubscriptionNodes(sub.id) },
                                            enabled = !isUpdating,
                                            shape = BellaShapes.small
                                        ) {
                                            if (isUpdating) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(14.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("更新中&#8230;", fontSize = 12.sp)
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Rounded.Refresh,
                                                    contentDescription = "Update",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("立即更新", fontSize = 12.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        IconButton(onClick = { editingSub = sub }) {
                                            Icon(
                                                imageVector = Icons.Rounded.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        IconButton(onClick = { deletingSub = sub }) {
                                            Icon(
                                                imageVector = Icons.Rounded.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
                if (selectedTabIndex == 0) {
                    showImportUrlDialog = true
                } else {
                    showAddSubDialog = true
                }
            },
            shape = BellaShapes.large,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add")
        }
    }

    // Dialog: Add Subscription
    if (showAddSubDialog) {
        var subName by remember { mutableStateOf("") }
        var subUrl by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddSubDialog = false },
            title = { Text("添加订阅", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = subName,
                        onValueChange = { subName = it },
                        label = { Text("订阅名称") },
                        placeholder = { Text("例如：极速节点订阅") },
                        shape = BellaShapes.small,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = subUrl,
                        onValueChange = { subUrl = it },
                        label = { Text("订阅链接 URL") },
                        placeholder = { Text("https://example.com/sub") },
                        shape = BellaShapes.small,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subUrl.isNotBlank()) {
                            val name = subName.ifBlank { "订阅 ${System.currentTimeMillis() % 1000}" }
                            viewModel.addSubscription(name, subUrl.trim())
                            showAddSubDialog = false
                        }
                    },
                    shape = BellaShapes.small
                ) {
                    Text("导入并解析")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubDialog = false }) {
                    Text("取消")
                }
            },
            shape = BellaShapes.extraLarge
        )
    }

    // Dialog: Edit Subscription
    val currentSubEdit = editingSub
    if (currentSubEdit != null) {
        var name by remember { mutableStateOf(currentSubEdit.name) }
        var url by remember { mutableStateOf(currentSubEdit.url) }

        AlertDialog(
            onDismissRequest = { editingSub = null },
            title = { Text("编辑订阅配置", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("订阅名称") },
                        shape = BellaShapes.small,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("订阅链接 URL") },
                        shape = BellaShapes.small,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && url.isNotBlank()) {
                            viewModel.updateSubscription(currentSubEdit.copy(name = name.trim(), url = url.trim()))
                            editingSub = null
                        }
                    },
                    shape = BellaShapes.small
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSub = null }) {
                    Text("取消")
                }
            },
            shape = BellaShapes.extraLarge
        )
    }

    // Dialog: Delete Subscription
    val currentSubDelete = deletingSub
    if (currentSubDelete != null) {
        var deleteNodesToo by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { deletingSub = null },
            title = { Text("确认删除订阅", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("确定要删除订阅 \"${currentSubDelete.name}\" 吗？")
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = deleteNodesToo, onCheckedChange = { deleteNodesToo = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("同时删除属于该订阅的所有节点", fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubscription(currentSubDelete.id, deleteNodesToo)
                        deletingSub = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = BellaShapes.small
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingSub = null }) {
                    Text("取消")
                }
            },
            shape = BellaShapes.extraLarge
        )
    }

    // Dialog: Import Node from Share URL
    if (showImportUrlDialog) {
        var shareLink by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showImportUrlDialog = false },
            title = { Text("导入代理分享链接", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = shareLink,
                        onValueChange = {
                            shareLink = it
                            isError = false
                        },
                        label = { Text("分享链接 (vless://, vmess://, trojan://, ss://, hy2://, tuic://, wg://)") },
                        shape = BellaShapes.small,
                        isError = isError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isError) {
                        Text(
                            text = "不支持的链接格式或格式损坏",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.importNodeFromUrl(shareLink.trim())
                        if (success) {
                            showImportUrlDialog = false
                        } else {
                            isError = true
                        }
                    },
                    shape = BellaShapes.small
                ) {
                    Text("导入")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportUrlDialog = false }) {
                    Text("取消")
                }
            },
            shape = BellaShapes.extraLarge
        )
    }

    // Dialog: Edit Node
    val currentEditingNode = editingNode
    if (currentEditingNode != null) {
        var name by remember { mutableStateOf(currentEditingNode.name) }
        var server by remember { mutableStateOf(currentEditingNode.server) }
        var port by remember { mutableStateOf(currentEditingNode.port.toString()) }
        var uuid by remember { mutableStateOf(currentEditingNode.uuid) }
        var password by remember { mutableStateOf(currentEditingNode.password) }
        var sni by remember { mutableStateOf(currentEditingNode.sni) }

        val scrollState = rememberScrollState()

        AlertDialog(
            onDismissRequest = { editingNode = null },
            title = { Text("编辑节点参数", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("节点名称") },
                        shape = BellaShapes.small,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = server,
                        onValueChange = { server = it },
                        label = { Text("服务器地址") },
                        shape = BellaShapes.small,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("端口号") },
                        shape = BellaShapes.small,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (currentEditingNode.protocol == ProtocolType.VLESS || currentEditingNode.protocol == ProtocolType.VMESS || currentEditingNode.protocol == ProtocolType.TUIC) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uuid,
                            onValueChange = { uuid = it },
                            label = { Text("UUID") },
                            shape = BellaShapes.small,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    if (currentEditingNode.protocol == ProtocolType.TROJAN || currentEditingNode.protocol == ProtocolType.SHADOWSOCKS || currentEditingNode.protocol == ProtocolType.HYSTERIA2 || currentEditingNode.protocol == ProtocolType.TUIC) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("密码 / 密钥") },
                            shape = BellaShapes.small,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sni,
                        onValueChange = { sni = it },
                        label = { Text("SNI (Server Name)") },
                        shape = BellaShapes.small,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = port.toIntOrNull() ?: currentEditingNode.port
                        val updated = currentEditingNode.copy(
                            name = name.trim(),
                            server = server.trim(),
                            port = p,
                            uuid = uuid.trim(),
                            password = password.trim(),
                            sni = sni.trim()
                        )
                        viewModel.updateNode(updated)
                        editingNode = null
                    },
                    shape = BellaShapes.small
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingNode = null }) {
                    Text("取消")
                }
            },
            shape = BellaShapes.extraLarge
        )
    }

    // Dialog: Delete Node
    val currentDeletingNode = deletingNode
    if (currentDeletingNode != null) {
        AlertDialog(
            onDismissRequest = { deletingNode = null },
            title = { Text("确认删除节点", fontWeight = FontWeight.Bold) },
            text = { Text("确定要删除节点 \"${currentDeletingNode.name}\" 吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteNode(currentDeletingNode)
                        deletingNode = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = BellaShapes.small
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingNode = null }) {
                    Text("取消")
                }
            },
            shape = BellaShapes.extraLarge
        )
    }
}
