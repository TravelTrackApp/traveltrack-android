package week11.st765512.finalproject.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.collect
import week11.st765512.finalproject.ui.components.CustomButton
import week11.st765512.finalproject.ui.components.CustomTextField
import week11.st765512.finalproject.ui.components.ErrorText
import week11.st765512.finalproject.ui.viewmodel.AuthViewModel
import week11.st765512.finalproject.util.Validation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    var uiState by remember { mutableStateOf(viewModel.uiState.value) }
    LaunchedEffect(Unit) {
        viewModel.uiState.collect { uiState = it }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }

    // Navigate on successful login
    LaunchedEffect(uiState.isSignedIn) {
        if (uiState.isSignedIn) {
            onLoginSuccess()
        }
    }

    // Real-time validation when user types
    LaunchedEffect(email) {
        if (emailTouched || email.isNotBlank()) {
            emailError = Validation.getEmailError(email)
        }
    }

    LaunchedEffect(password) {
        if (passwordTouched || password.isNotBlank()) {
            passwordError = Validation.getPasswordError(password)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Sign in to continue",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        CustomTextField(
            value = email,
            onValueChange = { 
                email = it
                emailTouched = true
            },
            label = "Email",
            keyboardType = KeyboardType.Email,
            isError = emailError != null && (emailTouched || email.isNotBlank()),
            errorMessage = if (emailError != null && (emailTouched || email.isNotBlank())) emailError else null,
            enabled = !uiState.isLoading
        )

        CustomTextField(
            value = password,
            onValueChange = { 
                password = it
                passwordTouched = true
            },
            label = "Password",
            isPassword = true,
            isError = passwordError != null && (passwordTouched || password.isNotBlank()),
            errorMessage = if (passwordError != null && (passwordTouched || password.isNotBlank())) passwordError else null,
            enabled = !uiState.isLoading
        )

        TextButton(
            onClick = onNavigateToForgotPassword,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Forgot Password?")
        }

        ErrorText(message = uiState.errorMessage)

        CustomButton(
            text = "Sign In",
            onClick = {
                emailTouched = true
                passwordTouched = true
                emailError = Validation.getEmailError(email)
                passwordError = Validation.getPasswordError(password)

                if (emailError == null && passwordError == null) {
                    viewModel.signInWithEmailAndPassword(email, password)
                }
            },
            isLoading = uiState.isLoading,
            enabled = email.isNotBlank() && password.isNotBlank()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account? ")
            TextButton(onClick = onNavigateToRegister) {
                Text("Sign Up")
            }
        }
    }
}

