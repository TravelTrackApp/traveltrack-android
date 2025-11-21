package week11.st765512.finalproject.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import week11.st765512.finalproject.ui.components.CustomButton
import week11.st765512.finalproject.ui.components.CustomTextField
import week11.st765512.finalproject.ui.components.AuthScreenContainer
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

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailTouched by rememberSaveable { mutableStateOf(false) }
    var passwordTouched by rememberSaveable { mutableStateOf(false) }

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

    AuthScreenContainer(
        title = "Sign In",
        subtitle = "Enter your credentials to view your trips",
        modifier = modifier,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage
    ) {

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

