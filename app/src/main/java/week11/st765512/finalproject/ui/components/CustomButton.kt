/**
 * File: CustomButton.kt
 * 
 * Custom button component with loading state and animation support. Used throughout
 * the app for consistent button styling and behavior.
 */
package week11.st765512.finalproject.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isSecondary: Boolean = false
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled || isLoading -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            isSecondary -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(200),
        label = "buttonColor"
    )
    
    val contentColor = when {
        isSecondary -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onPrimary
    }
    
    val elevation by animateDpAsState(
        targetValue = if (enabled && !isLoading) 4.dp else 0.dp,
        animationSpec = tween(200),
        label = "buttonElevation"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            ),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = contentColor,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

