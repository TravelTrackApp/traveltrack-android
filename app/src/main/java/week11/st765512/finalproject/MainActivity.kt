package week11.st765512.finalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import week11.st765512.finalproject.data.repository.AuthRepository
import week11.st765512.finalproject.navigation.NavGraph
import week11.st765512.finalproject.navigation.Screen
import week11.st765512.finalproject.ui.theme.TravelTrackAppTheme
import week11.st765512.finalproject.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
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
                    val authViewModel: AuthViewModel = viewModel {
                        AuthViewModel(authRepository)
                    }

                    // Determine start destination based on auth state
                    val startDestination = if (authRepository.isUserSignedIn) {
                        Screen.Home.route
                    } else {
                        Screen.Login.route
                    }

                    NavGraph(
                        startDestination = startDestination,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}