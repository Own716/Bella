package com.bellabox.app

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bellabox.app.ui.components.FloatingBottomBar
import com.bellabox.app.ui.components.NavigationTab
import com.bellabox.app.ui.screens.AboutScreen
import com.bellabox.app.ui.screens.GroupsScreen
import com.bellabox.app.ui.screens.HomeScreen
import com.bellabox.app.ui.screens.HomeViewModel
import com.bellabox.app.ui.screens.NodesScreen
import com.bellabox.app.ui.screens.NodesViewModel
import com.bellabox.app.ui.screens.RulesScreen
import com.bellabox.app.ui.screens.SettingsScreen
import com.bellabox.app.ui.screens.ToolsScreen
import com.bellabox.app.ui.theme.BellaBoxTheme

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val nodesViewModel: NodesViewModel by viewModels()

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            homeViewModel.connect()
        } else {
            Toast.makeText(this, "VPN permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BellaBoxTheme {
                var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
                var showingAbout by remember { mutableStateOf(false) }

                val activeNode by homeViewModel.activeNode.collectAsState()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    bottomBar = {
                        if (!showingAbout) {
                            FloatingBottomBar(
                                currentTab = currentTab,
                                onTabSelected = {
                                    currentTab = it
                                    showingAbout = false
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        if (showingAbout) {
                            AboutScreen(
                                onBack = { showingAbout = false }
                            )
                        } else {
                            when (currentTab) {
                                NavigationTab.HOME -> {
                                    HomeScreen(
                                        viewModel = homeViewModel,
                                        onNavigateToNodes = { currentTab = NavigationTab.NODES }
                                    )
                                }
                                NavigationTab.NODES -> {
                                    NodesScreen(
                                        viewModel = nodesViewModel,
                                        activeNodeId = activeNode?.id,
                                        onSelectNode = { node ->
                                            homeViewModel.selectNode(node)
                                            currentTab = NavigationTab.HOME
                                        }
                                    )
                                }
                                NavigationTab.GROUPS -> {
                                    GroupsScreen()
                                }
                                NavigationTab.RULES -> {
                                    RulesScreen()
                                }
                                NavigationTab.TOOLS -> {
                                    ToolsScreen()
                                }
                                NavigationTab.SETTINGS -> {
                                    SettingsScreen(
                                        onNavigateToAbout = { showingAbout = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun requestVpnPermissionAndConnect() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            homeViewModel.connect()
        }
    }
}
