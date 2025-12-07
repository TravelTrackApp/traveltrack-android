/**
 * File: CustomTextField.kt
 * 
 * Custom text field component with location icon and rounded corner styling.
 * Used for location input in various screens.
 */
package week11.st765512.finalproject.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    trailingIcon: ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { 
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            ) 
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { 
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                ) 
            }
        } else null,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        enabled = enabled,
        singleLine = singleLine,
        maxLines = maxLines,
        shape = RoundedCornerShape(14.dp),
        trailingIcon = trailingIcon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (icon == Icons.Default.LocationOn) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error
        )
    )
}

