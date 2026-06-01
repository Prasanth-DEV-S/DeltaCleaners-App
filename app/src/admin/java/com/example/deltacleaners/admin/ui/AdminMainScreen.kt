package com.example.deltacleaners.admin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.deltacleaners.admin.navigation.AdminScreen
import com.example.deltacleaners.admin.ui.bookings.BookingListScreen
import com.example.deltacleaners.admin.ui.bookings.BookingDetailsScreen
import com.example.deltacleaners.admin.ui.cleaners.CleanerManagementScreen
import com.example.deltacleaners.admin.ui.dashboard.DashboardScreen
import com.example.deltacleaners.admin.ui.services.ServiceManagementScreen

@Composable
fun AdminMainScreen(rootNavController: NavController) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            AdminBottomNavigation(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AdminScreen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AdminScreen.Dashboard.route) {
                DashboardScreen(onBookingClick = { id -> navController.navigate(AdminScreen.BookingDetails.createRoute(id)) })
            }
            composable(AdminScreen.Bookings.route) {
                BookingListScreen(onBookingClick = { id -> navController.navigate(AdminScreen.BookingDetails.createRoute(id)) })
            }
            composable(
                route = AdminScreen.BookingDetails.route,
                arguments = listOf(androidx.navigation.navArgument("bookingId") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                BookingDetailsScreen(bookingId = bookingId, onBack = { navController.popBackStack() })
            }
            composable(AdminScreen.Services.route) {
                ServiceManagementScreen()
            }
            composable(AdminScreen.Cleaners.route) {
                CleanerManagementScreen()
            }
            composable(AdminScreen.Notifications.route) {
                com.example.deltacleaners.admin.ui.notifications.AdminNotificationScreen()
            }
            composable(AdminScreen.Profile.route) {
                // Profile/Logout screen
                AdminProfileScreen(onLogout = { rootNavController.navigate(AdminScreen.Login.route) })
            }
        }
    }
}

@Composable
fun AdminBottomNavigation(navController: NavController) {
    val items = listOf(
        AdminNavItem("Dashboard", AdminScreen.Dashboard.route, Icons.Default.Dashboard, Icons.Outlined.Dashboard),
        AdminNavItem("Bookings", AdminScreen.Bookings.route, Icons.Default.Assignment, Icons.Outlined.Assignment),
        AdminNavItem("Services", AdminScreen.Services.route, Icons.Default.CleaningServices, Icons.Outlined.CleaningServices),
        AdminNavItem("Cleaners", AdminScreen.Cleaners.route, Icons.Default.Groups, Icons.Outlined.Groups),
        AdminNavItem("Profile", AdminScreen.Profile.route, Icons.Default.Person, Icons.Outlined.Person)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = { Icon(if (isSelected) item.selectedIcon else item.unselectedIcon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
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

data class AdminNavItem(val label: String, val route: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector)

@Composable
fun AdminProfileScreen(onLogout: () -> Unit, viewModel: com.example.deltacleaners.admin.ui.auth.AdminAuthViewModel = androidx.hilt.navigation.compose.hiltViewModel()) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Admin Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { 
                viewModel.logout()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Logout")
        }
    }
}
