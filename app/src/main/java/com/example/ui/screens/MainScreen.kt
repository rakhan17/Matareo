package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.DashboardViewModel
import kotlinx.coroutines.launch
import com.example.utils.ShellUtils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Rounded.Dashboard)
    object Hardware : Screen("hardware", "Hardware", Icons.Rounded.Memory)
    object Processes : Screen("processes", "Processes", Icons.Rounded.List)
    object Tools : Screen("tools", "Tools", Icons.Rounded.Build)
    object Settings : Screen("settings", "Settings", Icons.Rounded.Settings)
    object ScreenTest : Screen("screentest", "Screen Test", Icons.Rounded.Monitor)
    object Benchmark : Screen("benchmark", "Benchmark", Icons.Rounded.Speed)
    object Terminal : Screen("terminal", "Terminal", Icons.Rounded.Terminal)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Hardware,
    Screen.Processes,
    Screen.Tools,
    Screen.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: DashboardViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stats = uiState.stats
    var showBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isFullScreen = currentRoute == Screen.ScreenTest.route || currentRoute == Screen.Terminal.route || currentRoute == Screen.Benchmark.route

    Scaffold(
        topBar = {
            if (!isFullScreen) {
                TopAppBar(
                    title = {
                        Text("Matareo", fontWeight = FontWeight.Black, fontSize = 28.sp)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    ),
                    actions = {
                        IconButton(onClick = { showBottomSheet = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "More Actions")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!isFullScreen) {
                NavigationBar(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { androidx.compose.material3.Text(screen.title, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, softWrap = false) },
                            alwaysShowLabel = false,
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (stats == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardContent(uiState, stats)
                }
                composable(Screen.Hardware.route) {
                    HardwareScreen(stats)
                }
                composable(Screen.Processes.route) {
                    ProcessesScreen()
                }
                composable(Screen.Tools.route) {
                    ToolsScreen(navController)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen()
                }
                composable(Screen.ScreenTest.route) {
                    ScreenTestScreen(navController)
                }
                composable(Screen.Terminal.route) {
                    TerminalScreen(navController)
                }
                composable(Screen.Benchmark.route) {
                    BenchmarkScreen(navController)
                }
            }
        }
        
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false }
            ) {
                Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                    Text("Quick Actions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                    
                    ListItem(
                        headlineContent = { Text("Battery Saver Mode") },
                        supportingContent = { Text("Limit background activities") },
                        leadingContent = { Icon(Icons.Rounded.BatterySaver, contentDescription = null) },
                        modifier = Modifier.clickable { 
                            showBottomSheet = false
                            try {
                                context.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open Battery Settings", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Clear All RAM") },
                        supportingContent = { Text("Force stop non-essential apps") },
                        leadingContent = { Icon(Icons.Rounded.Memory, contentDescription = null) },
                        modifier = Modifier.clickable { 
                            showBottomSheet = false
                            coroutineScope.launch {
                                Toast.makeText(context, "Clearing Background Processes...", Toast.LENGTH_SHORT).show()
                                ShellUtils.executeCommand("am kill-all")
                                Toast.makeText(context, "RAM Cleared", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Copy System Report") },
                        supportingContent = { Text("Copy specs to clipboard") },
                        leadingContent = { Icon(Icons.Rounded.ContentCopy, contentDescription = null) },
                        modifier = Modifier.clickable { 
                            showBottomSheet = false
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val report = "Matareo System Report\n\nDevice: ${stats?.deviceName}\nOS: ${stats?.osVersion}\nCPU Load: ${stats?.cpuUsagePercent}%\nRAM: ${stats?.ramUsedMb} / ${stats?.ramTotalMb} MB\nStorage Free: ${stats?.storageFreeGb} GB"
                            val clip = ClipData.newPlainText("System Report", report)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Report copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}
