package com.bellabox.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bellabox.app.ui.i18n.LocalAppStrings
import com.bellabox.app.ui.theme.BellaShapes
import com.bellabox.core.model.StrategyGroup
import com.bellabox.core.model.StrategyType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    viewModel: GroupsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current

    val groups by viewModel.groups.collectAsState()
    val availableNodes by viewModel.availableNodes.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<StrategyGroup?>(null) }
    var deletingGroup by remember { mutableStateOf<StrategyGroup?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Hub,
                    contentDescription = s.groupsTitle,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = s.groupsTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = s.groupsSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (groups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = s.groupsEmptyTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = s.groupsEmptyDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 96.dp)
                ) {
                    items(groups, key = { it.id }) { group ->
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
                                            text = group.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Tag: ${group.tag}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Type Badge
                                    val typeLabel = when (group.type) {
                                        StrategyType.MANUAL -> s.groupsStrategyManual
                                        StrategyType.URLTEST -> s.groupsStrategyUrlTest
                                        StrategyType.FALLBACK -> s.groupsStrategyFallback
                                        StrategyType.CONSISTENT_HASH -> s.groupsStrategyConsistentHash
                                        StrategyType.ROUND_ROBIN -> s.groupsStrategyRoundRobin
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(BellaShapes.small)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = typeLabel,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    IconButton(
                                        onClick = { editingGroup = group },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Edit,
                                            contentDescription = s.actionEdit,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { deletingGroup = group },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = s.actionDelete,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val memberCount = if (group.nodeIds.isEmpty()) availableNodes.size else group.nodeIds.size
                                Text(
                                    text = "${s.groupsIncludedNodes}: $memberCount" + if (group.nodeIds.isEmpty()) " (${if (s.groupsTitle.contains("策略")) "全部可用节点" else "All available nodes"})" else "",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (group.type == StrategyType.URLTEST) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Probe: ${group.urlTestUrl} (${group.urlTestIntervalMinutes}m)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
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
            Icon(imageVector = Icons.Rounded.Add, contentDescription = s.groupsNewGroup)
        }
    }

    // Add / Edit Group Dialog
    val currentEdit = editingGroup
    if (showAddDialog || currentEdit != null) {
        val isEditing = currentEdit != null
        var name by remember { mutableStateOf(currentEdit?.name ?: "") }
        var tag by remember { mutableStateOf(currentEdit?.tag ?: "") }
        var type by remember { mutableStateOf(currentEdit?.type ?: StrategyType.MANUAL) }
        var url by remember { mutableStateOf(currentEdit?.urlTestUrl ?: "https://www.gstatic.com/generate_204") }
        var interval by remember { mutableStateOf((currentEdit?.urlTestIntervalMinutes ?: 10).toString()) }
        var tolerance by remember { mutableStateOf((currentEdit?.urlTestToleranceMs ?: 50).toString()) }
        var selectedNodeIds by remember { mutableStateOf(currentEdit?.nodeIds ?: emptyList()) }
        var typeDropdownExpanded by remember { mutableStateOf(false) }

        val scrollState = rememberScrollState()

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingGroup = null
            },
            title = {
                Text(
                    text = if (isEditing) s.groupsEditGroup else s.groupsNewGroup,
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
                        onValueChange = {
                            name = it
                            if (!isEditing && tag.isBlank()) {
                                tag = it.lowercase().replace(" ", "-")
                            }
                        },
                        label = { Text(s.groupsGroupName) },
                        shape = BellaShapes.small,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = tag,
                        onValueChange = { tag = it },
                        label = { Text("Tag") },
                        shape = BellaShapes.small,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = typeDropdownExpanded,
                        onExpandedChange = { typeDropdownExpanded = it }
                    ) {
                        val currentTypeLabel = when (type) {
                            StrategyType.MANUAL -> s.groupsStrategyManual
                            StrategyType.URLTEST -> s.groupsStrategyUrlTest
                            StrategyType.FALLBACK -> s.groupsStrategyFallback
                            StrategyType.CONSISTENT_HASH -> s.groupsStrategyConsistentHash
                            StrategyType.ROUND_ROBIN -> s.groupsStrategyRoundRobin
                        }

                        OutlinedTextField(
                            value = currentTypeLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(s.groupsStrategyType) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                            shape = BellaShapes.small,
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = typeDropdownExpanded,
                            onDismissRequest = { typeDropdownExpanded = false }
                        ) {
                            StrategyType.entries.forEach { st ->
                                val stLabel = when (st) {
                                    StrategyType.MANUAL -> s.groupsStrategyManual
                                    StrategyType.URLTEST -> s.groupsStrategyUrlTest
                                    StrategyType.FALLBACK -> s.groupsStrategyFallback
                                    StrategyType.CONSISTENT_HASH -> s.groupsStrategyConsistentHash
                                    StrategyType.ROUND_ROBIN -> s.groupsStrategyRoundRobin
                                }
                                DropdownMenuItem(
                                    text = {
                                        Text(stLabel, fontWeight = FontWeight.Bold)
                                    },
                                    onClick = {
                                        type = st
                                        typeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (type == StrategyType.URLTEST) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("Probe URL") },
                            shape = BellaShapes.small,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = interval,
                                onValueChange = { interval = it },
                                label = { Text("Interval (min)") },
                                shape = BellaShapes.small,
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = tolerance,
                                onValueChange = { tolerance = it },
                                label = { Text("Tolerance (ms)") },
                                shape = BellaShapes.small,
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${s.groupsIncludedNodes}:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    availableNodes.forEach { node ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedNodeIds = if (selectedNodeIds.contains(node.id)) {
                                        selectedNodeIds - node.id
                                    } else {
                                        selectedNodeIds + node.id
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = selectedNodeIds.contains(node.id),
                                onCheckedChange = { checked ->
                                    selectedNodeIds = if (checked == true) {
                                        selectedNodeIds + node.id
                                    } else {
                                        selectedNodeIds - node.id
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${node.name} (${node.protocol.label})",
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && tag.isNotBlank()) {
                            val grp = StrategyGroup(
                                id = currentEdit?.id ?: 0,
                                tag = tag.trim(),
                                name = name.trim(),
                                type = type,
                                nodeIds = selectedNodeIds,
                                urlTestUrl = url.trim(),
                                urlTestIntervalMinutes = interval.toIntOrNull() ?: 10,
                                urlTestToleranceMs = tolerance.toIntOrNull() ?: 50
                            )
                            if (isEditing) {
                                viewModel.updateGroup(grp)
                            } else {
                                viewModel.addGroup(grp)
                            }
                            showAddDialog = false
                            editingGroup = null
                        }
                    },
                    shape = BellaShapes.small
                ) {
                    Text(s.actionSave)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    editingGroup = null
                }) {
                    Text(s.actionCancel)
                }
            },
            shape = BellaShapes.extraLarge
        )
    }

    // Delete Confirmation Dialog
    val toDelete = deletingGroup
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { deletingGroup = null },
            title = { Text(s.groupsDeleteGroup, fontWeight = FontWeight.Bold) },
            text = { Text(s.groupsDeleteConfirm(toDelete.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGroup(toDelete.id)
                        deletingGroup = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = BellaShapes.small
                ) {
                    Text(s.actionDelete)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingGroup = null }) {
                    Text(s.actionCancel)
                }
            },
            shape = BellaShapes.extraLarge
        )
    }
}
