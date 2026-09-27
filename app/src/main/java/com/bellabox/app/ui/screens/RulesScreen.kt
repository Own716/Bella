package com.bellabox.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bellabox.app.ui.theme.BellaShapes
import com.bellabox.app.ui.theme.StatusConnected
import com.bellabox.app.ui.theme.StatusFailed
import com.bellabox.core.model.RouteRule
import com.bellabox.core.model.RuleActionType
import com.bellabox.core.model.RuleType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    viewModel: RulesViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val rules by viewModel.rules.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<RouteRule?>(null) }
    var deletingRule by remember { mutableStateOf<RouteRule?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Route,
                        contentDescription = "Rules",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "路由分流规则",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "自上而下逐条匹配，首条命中立即执行",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Reset to default button
                IconButton(onClick = { showResetDialog = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Reset Rules",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (rules.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = BellaShapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth(0.9f).padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "暂无路由规则",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.resetToDefaultRules() },
                                shape = BellaShapes.small
                            ) {
                                Text("加载官方默认分流规则")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 96.dp)
                ) {
                    itemsIndexed(rules, key = { _, rule -> rule.id }) { index, rule ->
                        Card(
                            shape = BellaShapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = if (rule.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Priority ordering controls
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = { viewModel.moveRuleUp(rule.id) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ArrowUpward,
                                            contentDescription = "Move Up",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = "#${index + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    IconButton(
                                        onClick = { viewModel.moveRuleDown(rule.id) },
                                        enabled = index < rules.size - 1,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ArrowDownward,
                                            contentDescription = "Move Down",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Rule Info
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = rule.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val (actionColor, actionLabel) = when (rule.action) {
                                            RuleActionType.DIRECT -> StatusConnected to "直连 (DIRECT)"
                                            RuleActionType.PROXY -> MaterialTheme.colorScheme.primary to "代理 (PROXY)"
                                            RuleActionType.BLOCK -> StatusFailed to "阻止 (BLOCK)"
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(BellaShapes.small)
                                                .background(actionColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = actionLabel,
                                                color = actionColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${rule.ruleType.name}: ${rule.values.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Edit button
                                IconButton(
                                    onClick = { editingRule = rule },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Delete button
                                IconButton(
                                    onClick = { deletingRule = rule },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Enable switch
                                Switch(
                                    checked = rule.isEnabled,
                                    onCheckedChange = { viewModel.toggleRuleEnabled(rule.id, it) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            shape = BellaShapes.large,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Rule")
        }
    }

    // Add / Edit Rule Dialog
    val currentEdit = editingRule
    if (showAddDialog || currentEdit != null) {
        val isEditing = currentEdit != null
        var name by remember { mutableStateOf(currentEdit?.name ?: "") }
        var ruleType by remember { mutableStateOf(currentEdit?.ruleType ?: RuleType.DOMAIN_SUFFIX) }
        var valuesText by remember { mutableStateOf(currentEdit?.values?.joinToString(", ") ?: "") }
        var action by remember { mutableStateOf(currentEdit?.action ?: RuleActionType.DIRECT) }
        var outboundTag by remember { mutableStateOf(currentEdit?.targetOutboundTag ?: "") }
        var ruleTypeDropdownExpanded by remember { mutableStateOf(false) }
        var actionDropdownExpanded by remember { mutableStateOf(false) }

        val scrollState = rememberScrollState()

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingRule = null
            },
            title = {
                Text(
                    text = if (isEditing) "编辑分流规则" else "新建分流规则",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("规则名称") },
                        placeholder = { Text("例如：国内直连") },
                        shape = BellaShapes.small,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = ruleTypeDropdownExpanded,
                        onExpandedChange = { ruleTypeDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = ruleType.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("匹配类型") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ruleTypeDropdownExpanded) },
                            shape = BellaShapes.small,
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = ruleTypeDropdownExpanded,
                            onDismissRequest = { ruleTypeDropdownExpanded = false }
                        ) {
                            RuleType.entries.forEach { rt ->
                                DropdownMenuItem(
                                    text = { Text(rt.name) },
                                    onClick = {
                                        ruleType = rt
                                        ruleTypeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = valuesText,
                        onValueChange = { valuesText = it },
                        label = { Text("匹配内容 (逗号分隔)") },
                        placeholder = { Text("例如：cn 或 google.com, youtube.com") },
                        shape = BellaShapes.small,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = actionDropdownExpanded,
                        onExpandedChange = { actionDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = action.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("执行动作") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionDropdownExpanded) },
                            shape = BellaShapes.small,
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = actionDropdownExpanded,
                            onDismissRequest = { actionDropdownExpanded = false }
                        ) {
                            RuleActionType.entries.forEach { at ->
                                DropdownMenuItem(
                                    text = { Text(at.name) },
                                    onClick = {
                                        action = at
                                        actionDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (action == RuleActionType.PROXY) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = outboundTag,
                            onValueChange = { outboundTag = it },
                            label = { Text("目标出站标识 (可选，默认proxy)") },
                            shape = BellaShapes.small,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val vals = valuesText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            val rule = RouteRule(
                                id = currentEdit?.id ?: 0,
                                name = name.trim(),
                                ruleType = ruleType,
                                values = vals,
                                action = action,
                                targetOutboundTag = outboundTag.trim(),
                                isEnabled = currentEdit?.isEnabled ?: true,
                                priority = currentEdit?.priority ?: (rules.size + 1)
                            )
                            if (isEditing) {
                                viewModel.updateRule(rule)
                            } else {
                                viewModel.addRule(rule)
                            }
                            showAddDialog = false
                            editingRule = null
                        }
                    },
                    shape = BellaShapes.small
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    editingRule = null
                }) {
                    Text("取消")
                }
            },
            shape = BellaShapes.extraLarge
        )
    }

    // Delete Confirmation Dialog
    val toDelete = deletingRule
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { deletingRule = null },
            title = { Text("确认删除规则", fontWeight = FontWeight.Bold) },
            text = { Text("确定要删除规则 \"${toDelete.name}\" 吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRule(toDelete.id)
                        deletingRule = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = BellaShapes.small
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingRule = null }) {
                    Text("取消")
                }
            },
            shape = BellaShapes.extraLarge
        )
    }

    // Reset Defaults Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("恢复默认规则", fontWeight = FontWeight.Bold) },
            text = { Text("此操作将清空当前自定义路由，并重新初始化系统官方推荐的分流规则（DNS劫持、局域网直连、国内IP与域名直连、广告拦截等）。") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToDefaultRules()
                        showResetDialog = false
                    },
                    shape = BellaShapes.small
                ) {
                    Text("确认重置")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("取消")
                }
            },
            shape = BellaShapes.extraLarge
        )
    }
}
