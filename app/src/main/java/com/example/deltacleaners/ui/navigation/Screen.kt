package com.example.deltacleaners.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object OtpVerification : Screen("otp_verification")
    data object Home : Screen("home")
    data object CleanerDashboard : Screen("cleaner_dashboard")
    data object Notifications : Screen("notifications")
    data object History : Screen("history")
    data object Profile : Screen("profile")
    data object ForceUpdate : Screen("force_update?title={title}&message={message}&url={url}") {
        fun createRoute(title: String, message: String, url: String): String {
            return "force_update?title=$title&message=$message&url=$url"
        }
    }
    
    data object ServiceDetail : Screen("service_detail/{serviceName}") {
        fun createRoute(serviceName: String): String = "service_detail/$serviceName"
    }
    
    data object Booking : Screen("booking/{serviceName}?price={price}") {
        fun createRoute(serviceName: String, price: Double? = null): String {
            return if (price != null) "booking/$serviceName?price=$price" else "booking/$serviceName"
        }
    }
}
