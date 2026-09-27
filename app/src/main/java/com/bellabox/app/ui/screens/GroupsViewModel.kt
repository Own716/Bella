package com.bellabox.app.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bellabox.app.BellaApplication
import com.bellabox.core.model.ProxyNode
import com.bellabox.core.model.StrategyGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GroupsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BellaApplication
    private val groupRepo = app.strategyGroupRepository
    private val nodeRepo = app.nodeRepository

    val groups: StateFlow<List<StrategyGroup>> = groupRepo.getAllGroupsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val availableNodes: StateFlow<List<ProxyNode>> = nodeRepo.getAllNodesFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addGroup(group: StrategyGroup) {
        viewModelScope.launch(Dispatchers.IO) {
            groupRepo.insertGroup(group)
        }
    }

    fun updateGroup(group: StrategyGroup) {
        viewModelScope.launch(Dispatchers.IO) {
            groupRepo.updateGroup(group)
        }
    }

    fun deleteGroup(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            groupRepo.deleteGroup(id)
        }
    }

    fun selectNodeInGroup(groupId: Long, nodeId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            groupRepo.updateSelectedNode(groupId, nodeId)
        }
    }
}
