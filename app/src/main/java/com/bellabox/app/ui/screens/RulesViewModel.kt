package com.bellabox.app.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bellabox.app.BellaApplication
import com.bellabox.core.model.RouteRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RulesViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BellaApplication
    private val ruleRepo = app.ruleRepository

    val rules: StateFlow<List<RouteRule>> = ruleRepo.getAllRulesFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun toggleRuleEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            ruleRepo.toggleRuleEnabled(id, enabled)
        }
    }

    fun addRule(rule: RouteRule) {
        viewModelScope.launch(Dispatchers.IO) {
            ruleRepo.insertRule(rule)
        }
    }

    fun updateRule(rule: RouteRule) {
        viewModelScope.launch(Dispatchers.IO) {
            ruleRepo.updateRule(rule)
        }
    }

    fun deleteRule(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            ruleRepo.deleteRule(id)
        }
    }

    fun moveRuleUp(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = rules.value
            val index = list.indexOfFirst { it.id == id }
            if (index > 0) {
                val current = list[index]
                val prev = list[index - 1]
                ruleRepo.updateRulePriority(current.id, prev.priority)
                ruleRepo.updateRulePriority(prev.id, current.priority)
            }
        }
    }

    fun moveRuleDown(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = rules.value
            val index = list.indexOfFirst { it.id == id }
            if (index >= 0 && index < list.size - 1) {
                val current = list[index]
                val next = list[index + 1]
                ruleRepo.updateRulePriority(current.id, next.priority)
                ruleRepo.updateRulePriority(next.id, current.priority)
            }
        }
    }

    fun resetToDefaultRules() {
        viewModelScope.launch(Dispatchers.IO) {
            ruleRepo.resetToDefaultRules()
        }
    }
}
