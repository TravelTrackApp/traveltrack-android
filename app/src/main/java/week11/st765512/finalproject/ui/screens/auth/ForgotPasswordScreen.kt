/**
 * File: ForgotPasswordScreen.kt
 * 
 * Password reset screen. Allows users to request a password reset email
 * and navigate back to login screen.
 */
package week11.st765512.finalproject.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import week11.st765512.finalproject.ui.components.CustomButton
import week11.st765512.finalproject.ui.components.CustomTextField
import week11.st765512.finalproject.ui.components.AuthScreenContainer
import week11.st765512.finalproject.ui.viewmodel.AuthViewModel
import week11.st765512.finalproject.util.Validation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var uiState by remember { mutableStateOf(viewModel.uiState.value) }
    LaunchedEffect(Unit) {
        viewModel.uiState.collect { uiState = it }
    }

    var email by rememberSaveable { mutableStateOf("") }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailSent by rememberSaveable { mutableStateOf(false) }
    var emailTouched by rememberSaveable { mutableStateOf(false) }

    // Real-time validation when user types
    LaunchedEffect(email) {
        if (emailTouched || email.isNotBlank()) {
            emailError = Validation.getEmailError(email)
        }
    }

    // Show success message when email is sent
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && uiState.errorMessage == null && email.isNotBlank()) {
            emailSent = true
        }
    }

    AuthScreenContainer(
        title = "Forgot Password",
        subtitle = "Enter your email to receive a reset link",
        modifier = modifier,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage
    ) {

        if (emailSent) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "Password reset email sent! Please check your inbox.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        CustomTextField(
            value = email,
            onValueChange = { 
                email = it
                emailTouched = true
                emailSent = false
            },
            label = "Email",
            keyboardType = KeyboardType.Email,
            isError = emailError != null && (emailTouched || email.isNotBlank()),
            errorMessage = if (emailError != null && (emailTouched || email.isNotBlank())) emailError else null,
            enabled = !uiState.isLoading && !emailSent
        )

        CustomButton(
            text = "Send Reset Link",
            onClick = {
                emailTouched = true
                emailError = Validation.getEmailError(email)
                if (emailError == null) {
                    viewModel.sendPasswordResetEmail(email)
                }
            },
            isLoading = uiState.isLoading,
            enabled = email.isNotBlank() && !emailSent
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Back to Sign In")
        }
    }
}

