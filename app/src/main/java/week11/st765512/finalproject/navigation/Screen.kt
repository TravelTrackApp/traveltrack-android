/**
 * File: Screen.kt
 * 
 * Screen route definitions for navigation. Sealed class containing all available screen
 * routes including Login, Register, ForgotPassword, Home, LogTrip, TripList, and TripDetail.
 */
package week11.st765512.finalproject.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object LogTrip : Screen("log_trip")
    object TripList : Screen("trip_list")
    object TripDetail : Screen("trip_detail/{tripId}") {
        fun createRoute(tripId: String) = "trip_detail/$tripId"
    }
}

