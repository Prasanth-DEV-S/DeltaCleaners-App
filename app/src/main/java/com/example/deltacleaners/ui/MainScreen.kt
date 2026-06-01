package com.example.deltacleaners.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.deltacleaners.ui.home.HomeScreen
import com.example.deltacleaners.ui.history.BookingHistoryScreen
import com.example.deltacleaners.ui.home.HomeViewModel
import com.example.deltacleaners.ui.profile.ProfileScreen
import com.example.deltacleaners.ui.navigation.Screen
import com.example.deltacleaners.ui.notifications.NotificationViewModel

@Composable
fun MainScreen(
    rootNavController: NavController,
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val bottomNavController = rememberNavController()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val items = listOf(
                    NavigationItem(
                        "Home",
                        Screen.Home.route,
                        Icons.Default.Home,
                        Icons.Outlined.Home
                    ),
                    NavigationItem(
                        "Bookings",
                        Screen.History.route,
                        Icons.Default.History,
                        Icons.Outlined.History
                    ),
                    NavigationItem(
                        "Profile",
                        Screen.Profile.route,
                        Icons.Default.Person,
                        Icons.Outlined.Person
                    )
                )

                items.forEach { item ->
                    val isSelected =
                        currentDestination?.hierarchy?.any { it.route == item.route } == true

                    NavigationBarItem(
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (item.label == "Profile" && unreadCount > 0) {
                                        Badge { Text(unreadCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            }
                        },
                        label = {
                            Text(
                                item.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
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
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(rootNavController)
            }
            composable(Screen.History.route) {
                BookingHistoryScreen(rootNavController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(rootNavController)
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val route: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)

