/**
 * File: AutocompleteTextField.kt
 * 
 * Text field component with Google Places autocomplete functionality. Provides location
 * suggestions as user types and converts selected addresses to coordinates.
 */
package week11.st765512.finalproject.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import week11.st765512.finalproject.util.PlaceDetails
import week11.st765512.finalproject.util.PlaceSuggestion
import week11.st765512.finalproject.util.PlacesAutocompleteHelper

/**
 * Text field with autocomplete dropdown for place suggestions
 * Works alongside manual map marker placement
 */
@Composable
fun AutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onPlaceSelected: (PlaceDetails) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null,
    placeholder: String = "",
    singleLine: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    
    var suggestions by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showDropdown by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    // Track if user just selected a place to avoid re-triggering search
    var justSelected by remember { mutableStateOf(false) }
    
    // Initialize Places SDK
    LaunchedEffect(Unit) {
        PlacesAutocompleteHelper.initialize(context)
    }
    
    // Debounced search when value changes
    LaunchedEffect(value, isFocused) {
        if (justSelected) {
            justSelected = false
            return@LaunchedEffect
        }
        
        searchJob?.cancel()
        
        if (value.isBlank() || value.length < 2 || !isFocused) {
            suggestions = emptyList()
            showDropdown = false
            return@LaunchedEffect
        }
        
        searchJob = scope.launch {
            // Debounce: wait 300ms before searching
            delay(300)
            isLoading = true
            
            val results = PlacesAutocompleteHelper.getAutocompletePredictions(value, context)
            suggestions = results.take(5) // Limit to 5 suggestions
            showDropdown = results.isNotEmpty()
            isLoading = false
        }
    }
    
    Box(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Text field with underline style
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = value,
                    onValueChange = { newValue ->
                        onValueChange(newValue)
                    },
                    label = { Text(label) },
                    placeholder = if (placeholder.isNotEmpty()) { { Text(placeholder) } } else null,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                        .onFocusChanged { focusState ->
                            isFocused = focusState.isFocused
                            if (!focusState.isFocused) {
                                // Delay hiding dropdown to allow click to register
                                scope.launch {
                                    delay(200)
                                    showDropdown = false
                                }
                            }
                        },
                    enabled = enabled,
                    singleLine = singleLine,
                    trailingIcon = if (isLoading) {
                        {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    } else null,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                        disabledIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                
                trailingIcon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 8.dp),
                        tint = if (icon == Icons.Filled.LocationOn) {
                            Color(0xFFD32F2F) // Red color for location icon
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            // Dropdown with suggestions
            AnimatedVisibility(
                visible = showDropdown && suggestions.isNotEmpty(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .zIndex(10f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 250.dp)
                    ) {
                        items(suggestions) { suggestion ->
                            SuggestionItem(
                                suggestion = suggestion,
                                onClick = {
                                    scope.launch {
                                        // Fetch place details with coordinates
                                        isLoading = true
                                        val details = PlacesAutocompleteHelper.getPlaceDetails(
                                            suggestion.placeId,
                                            context
                                        )
                                        isLoading = false
                                        
                                        if (details != null) {
                                            justSelected = true
                                            onValueChange(suggestion.fullText)
                                            onPlaceSelected(details)
                                            showDropdown = false
                                            suggestions = emptyList()
                                            focusManager.clearFocus()
                                        }
                                    }
                                }
                            )
                            
                            if (suggestion != suggestions.last()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    suggestion: PlaceSuggestion,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 0.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = suggestion.primaryText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (suggestion.secondaryText.isNotEmpty()) {
                Text(
                    text = suggestion.secondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

