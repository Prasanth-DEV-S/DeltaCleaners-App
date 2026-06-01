package com.example.deltacleaners.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.deltacleaners.ui.auth.LoginScreen
import com.example.deltacleaners.ui.auth.OtpVerificationScreen
import com.example.deltacleaners.ui.MainScreen
import com.example.deltacleaners.ui.splash.SplashScreen
import com.example.deltacleaners.ui.booking.BookingScreen
import com.example.deltacleaners.ui.notifications.NotificationScreen
import com.example.deltacleaners.ui.service_detail.ServiceDetailScreen
import com.example.deltacleaners.ui.cleaner.CleanerDashboardScreen
import com.example.deltacleaners.ui.auth.AuthViewModel

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import com.example.deltacleaners.ui.force_update.ForceUpdateScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }

        composable(
            route = Screen.ForceUpdate.route,
            arguments = listOf(
                androidx.navigation.navArgument("title") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("message") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("url") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val title = URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "", StandardCharsets.UTF_8.toString())
            val message = URLDecoder.decode(backStackEntry.arguments?.getString("message") ?: "", StandardCharsets.UTF_8.toString())
            val url = URLDecoder.decode(backStackEntry.arguments?.getString("url") ?: "", StandardCharsets.UTF_8.toString())
            
            ForceUpdateScreen(title, message, url)
        }

        navigation(
            startDestination = Screen.Login.route,
            route = "auth_graph"
        ) {
            composable(Screen.Login.route) { entry ->
                val viewModel = hiltViewModel<AuthViewModel>(
                    remember(entry) { navController.getBackStackEntry("auth_graph") }
                )
                LoginScreen(navController, viewModel)
            }

            composable(Screen.OtpVerification.route) { entry ->
                val viewModel = hiltViewModel<AuthViewModel>(
                    remember(entry) { navController.getBackStackEntry("auth_graph") }
                )
                OtpVerificationScreen(navController, viewModel)
            }
        }

        composable(Screen.Home.route) {
            MainScreen(navController)
        }

        composable(Screen.CleanerDashboard.route) {
            CleanerDashboardScreen(navController)
        }

        composable(Screen.Notifications.route) {
            NotificationScreen(navController)
        }

        composable(route = Screen.ServiceDetail.route) { backStackEntry ->
            val serviceName = backStackEntry.arguments?.getString("serviceName") ?: ""
            ServiceDetailScreen(
                navController = navController,
                serviceName = serviceName
            )
        }

        composable(
            route = Screen.Booking.route,
            arguments = listOf(
                androidx.navigation.navArgument("serviceName") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("price") { 
                    type = androidx.navigation.NavType.StringType
                    defaultValue = "999"
                }
            )
        ) { backStackEntry ->
            val serviceName = backStackEntry.arguments?.getString("serviceName") ?: ""
            val price = backStackEntry.arguments?.getString("price") ?: "999"
            BookingScreen(
                navController = navController,
                serviceName = serviceName,
                price = price
            )
        }
    }
}
