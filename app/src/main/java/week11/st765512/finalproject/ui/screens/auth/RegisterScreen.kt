package week11.st765512.finalproject.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var uiState by remember { mutableStateOf(viewModel.uiState.value) }
    LaunchedEffect(Unit) {
        viewModel.uiState.collect { uiState = it }
    }

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmPasswordError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailTouched by rememberSaveable { mutableStateOf(false) }
    var passwordTouched by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordTouched by rememberSaveable { mutableStateOf(false) }

    // Navigate on successful registration
    LaunchedEffect(uiState.isSignedIn) {
        if (uiState.isSignedIn) {
            onRegisterSuccess()
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
        if (confirmPasswordTouched || confirmPassword.isNotBlank()) {
            confirmPasswordError = Validation.getConfirmPasswordError(password, confirmPassword)
        }
    }

    LaunchedEffect(confirmPassword) {
        if (confirmPasswordTouched || confirmPassword.isNotBlank()) {
            confirmPasswordError = Validation.getConfirmPasswordError(password, confirmPassword)
        }
    }

    AuthScreenContainer(
        title = "Create Account",
        subtitle = "Register to start logging your trips",
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

        CustomTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                confirmPasswordTouched = true
            },
            label = "Confirm Password",
            isPassword = true,
            isError = confirmPasswordError != null && (confirmPasswordTouched || confirmPassword.isNotBlank()),
            errorMessage = if (confirmPasswordError != null && (confirmPasswordTouched || confirmPassword.isNotBlank())) confirmPasswordError else null,
            enabled = !uiState.isLoading
        )

        CustomButton(
            text = "Sign Up",
            onClick = {
                emailTouched = true
                passwordTouched = true
                confirmPasswordTouched = true
                emailError = Validation.getEmailError(email)
                passwordError = Validation.getPasswordError(password)
                confirmPasswordError = Validation.getConfirmPasswordError(password, confirmPassword)

                if (emailError == null && passwordError == null && confirmPasswordError == null) {
                    viewModel.registerWithEmailAndPassword(email, password)
                }
            },
            isLoading = uiState.isLoading,
            enabled = email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account? ")
            TextButton(onClick = onNavigateToLogin) {
                Text("Sign In")
            }
        }
    }
}

