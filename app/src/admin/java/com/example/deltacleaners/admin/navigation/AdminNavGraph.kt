package com.example.deltacleaners.admin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.deltacleaners.admin.ui.AdminMainScreen
import com.example.deltacleaners.admin.ui.auth.AdminLoginScreen
import com.example.deltacleaners.admin.ui.auth.AdminRegisterScreen

@Composable
fun AdminNavGraph(startDestination: String = AdminScreen.Login.route) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AdminScreen.Login.route) {
            AdminLoginScreen(
                onLoginSuccess = {
                    navController.navigate(AdminScreen.Dashboard.route) {
                        popUpTo(AdminScreen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AdminScreen.Register.route)
                }
            )
        }

        composable(AdminScreen.Register.route) {
            AdminRegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(AdminScreen.Dashboard.route) {
                        popUpTo(AdminScreen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(AdminScreen.Dashboard.route) {
            AdminMainScreen(navController)
        }
    }
}
