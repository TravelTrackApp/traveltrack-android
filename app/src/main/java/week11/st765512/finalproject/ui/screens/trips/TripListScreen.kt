/**
 * File: TripListScreen.kt
 * 
 * Screen displaying list of all user trips. Shows trip cards with filtering and
 * search functionality, and allows navigation to individual trip details.
 */
package week11.st765512.finalproject.ui.screens.trips

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.delay
import week11.st765512.finalproject.ui.components.CustomButton
import week11.st765512.finalproject.ui.components.CustomTextField
import week11.st765512.finalproject.ui.components.SuccessPill
import week11.st765512.finalproject.ui.components.TripListCard
import week11.st765512.finalproject.ui.viewmodel.TripUiState
import week11.st765512.finalproject.ui.viewmodel.TripViewModel

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    tripViewModel: TripViewModel,
    onNavigateBack: () -> Unit,
    onTripSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var uiState by remember { mutableStateOf(tripViewModel.uiState.value) }
    LaunchedEffect(Unit) {
        tripViewModel.uiState.collect { uiState = it }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "Saved Trips",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            FilterPanel(
                uiState = uiState,
                onSearchChange = tripViewModel::updateSearchQuery,
                onTagChange = tripViewModel::updateFilterTag,
                onLocationChange = tripViewModel::updateFilterLocation,
                onDateInput = tripViewModel::updateFilterDateFromInput,
                onClear = tripViewModel::clearFilters,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
            )

            if (uiState.trips.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f),
                    shape = RoundedCornerShape(32.dp),
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.7f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Explore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "No trips yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your saved trips will appear here.\nStart logging your adventures!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            } else {
                // Trip count header
                Text(
                    text = "${uiState.trips.size} trip${if (uiState.trips.size != 1) "s" else ""} logged",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.trips, key = { it.id }) { trip ->
                        TripListCard(
                            trip = trip,
                            onDelete = { tripViewModel.deleteTrip(trip.id) },
                            onClick = { onTripSelected(trip.id) }
                        )
                    }
                    // Bottom spacing for FAB
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }

            // Success message at bottom
            uiState.successMessage?.let { message ->
                SuccessPill(
                    message = message,
                    modifier = Modifier
                        .align(Alignment.BottomCenter as Alignment.Horizontal)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            delay(2500)
            tripViewModel.clearMessage()
        }
    }
}

@Composable
fun FilterPanel(
    uiState: TripUiState,
    onSearchChange: (String) -> Unit,
    onTagChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onDateInput: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            CustomTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                label = "Search Trip"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CustomTextField(
                    value = uiState.filterDateInput,
                    onValueChange = onDateInput,
                    label = "Date (yyyy-MM-dd)",
                    modifier = Modifier.weight(1f)
                )

                CustomTextField(
                    value = uiState.filterTag,
                    onValueChange = onTagChange,
                    label = "Tag",
                    modifier = Modifier.weight(1f)
                )

                CustomTextField(
                    value = uiState.filterLocation,
                    onValueChange = onLocationChange,
                    label = "Location",
                    modifier = Modifier.weight(1f)
                )
            }

            CustomButton(
                text = "Clear Filters",
                onClick = onClear
            )
        }
    }
}

