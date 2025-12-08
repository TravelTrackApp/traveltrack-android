/**
 * File: MainActivity.kt
 * 
 * Main entry point of the application. Initializes ViewModels, applies theme,
 * and sets up navigation graph with authentication-based start destination.
 */
package week11.st765512.finalproject

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import week11.st765512.finalproject.data.repository.AuthRepository
import week11.st765512.finalproject.navigation.NavGraph
import week11.st765512.finalproject.navigation.Screen
import week11.st765512.finalproject.ui.theme.TravelTrackAppTheme
import week11.st765512.finalproject.ui.viewmodel.AuthViewModel
import week11.st765512.finalproject.ui.viewmodel.TripViewModel

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelTrackAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authRepository = AuthRepository()
                    val authViewModel: AuthViewModel = viewModel { AuthViewModel(authRepository) }
                    val tripViewModel: TripViewModel = viewModel { TripViewModel() }

                    // Determine start destination based on auth state
                    val startDestination = if (authRepository.isUserSignedIn) {
                        Screen.Home.route
                    } else {
                        Screen.Login.route
                    }

                    NavGraph(
                        startDestination = startDestination,
                        authViewModel = authViewModel,
                        tripViewModel = tripViewModel
                    )
                }
            }
        }
    }
}