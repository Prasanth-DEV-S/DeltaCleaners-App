package com.example.deltacleaners.admin.navigation

sealed class AdminScreen(val route: String) {
    data object Login : AdminScreen("login")
    data object Register : AdminScreen("register")
    data object Dashboard : AdminScreen("dashboard")
    data object Bookings : AdminScreen("bookings")
    data object BookingDetails : AdminScreen("booking_details/{bookingId}") {
        fun createRoute(bookingId: String) = "booking_details/$bookingId"
    }
    data object Services : AdminScreen("services")
    data object Cleaners : AdminScreen("cleaners")
    data object Notifications : AdminScreen("notifications")
    data object Profile : AdminScreen("profile")
}
