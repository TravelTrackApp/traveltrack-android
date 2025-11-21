package week11.st765512.finalproject.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.collect
import week11.st765512.finalproject.ui.screens.auth.ForgotPasswordScreen
import week11.st765512.finalproject.ui.screens.auth.LoginScreen
import week11.st765512.finalproject.ui.screens.auth.RegisterScreen
import week11.st765512.finalproject.ui.screens.home.HomeScreen
import week11.st765512.finalproject.ui.screens.trips.LogTripScreen
import week11.st765512.finalproject.ui.screens.trips.TripDetailScreen
import week11.st765512.finalproject.ui.screens.trips.TripListScreen
import week11.st765512.finalproject.ui.viewmodel.AuthViewModel
import week11.st765512.finalproject.ui.viewmodel.TripViewModel

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route,
    authViewModel: AuthViewModel,
    tripViewModel: TripViewModel
) {
    var uiState by remember { mutableStateOf(authViewModel.uiState.value) }
    LaunchedEffect(Unit) {
        authViewModel.uiState.collect { uiState = it }
    }

    LaunchedEffect(uiState.user?.uid) {
        tripViewModel.onAuthStateChanged(uiState.user?.uid)
    }

    // Navigate based on auth state
    LaunchedEffect(uiState.isSignedIn) {
        if (uiState.isSignedIn) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        } else if (!uiState.isSignedIn && navController.currentDestination?.route == Screen.Home.route) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                tripViewModel = tripViewModel,
                onNavigateToLogTrip = { navController.navigate(Screen.LogTrip.route) },
                onNavigateToTripList = { navController.navigate(Screen.TripList.route) },
                onNavigateToTripDetail = { tripId ->
                    navController.navigate(Screen.TripDetail.createRoute(tripId))
                }
            )
        }

        composable(Screen.LogTrip.route) {
            LogTripScreen(
                tripViewModel = tripViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TripList.route) {
            TripListScreen(
                tripViewModel = tripViewModel,
                onNavigateBack = { navController.popBackStack() },
                onTripSelected = { tripId ->
                    navController.navigate(Screen.TripDetail.createRoute(tripId))
                }
            )
        }

        composable(
            route = Screen.TripDetail.route,
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
            TripDetailScreen(
                tripId = tripId,
                tripViewModel = tripViewModel,
                onNavigateBack = { navController.popBackStack() },
                onTripDeleted = {
                    navController.popBackStack(Screen.TripList.route, inclusive = false)
                }
            )
        }
    }
}

