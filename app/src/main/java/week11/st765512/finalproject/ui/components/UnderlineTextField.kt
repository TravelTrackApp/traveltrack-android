/**
 * File: UnderlineTextField.kt
 * 
 * Custom text field component with underline styling and location icon support.
 * Used for location input fields in trip logging screens.
 */
package week11.st765512.finalproject.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Text field with underline style (no border, just bottom line)
 */
@Composable
fun UnderlineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                label = { 
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    ) 
                },
                placeholder = if (placeholder.isNotEmpty()) { 
                    { 
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    } 
                } else null,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp),
                enabled = enabled,
                singleLine = singleLine,
                maxLines = maxLines,
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    disabledIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            
            trailingIcon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp),
                    tint = if (icon == Icons.Filled.LocationOn) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

