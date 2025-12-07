/**
 * File: AuthScreenContainer.kt
 * 
 * Container component for authentication screens with background image and consistent
 * layout structure for login, register, and forgot password screens.
 */
package week11.st765512.finalproject.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import week11.st765512.finalproject.R

@Composable
fun AuthScreenContainer(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Background image with overlay
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .align(Alignment.TopCenter),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay for better text readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.5f)
                        )
                    )
                )
        )

        // App name with subtle shadow
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TravelTrack",
                style = MaterialTheme.typography.displaySmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Your journey starts here",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.85f)
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Main content card
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 200.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                ),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            ScreenStateWrapper(
                isLoading = isLoading,
                errorMessage = errorMessage,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 28.dp, vertical = 36.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    content()
                }
            }
        }
    }
}

