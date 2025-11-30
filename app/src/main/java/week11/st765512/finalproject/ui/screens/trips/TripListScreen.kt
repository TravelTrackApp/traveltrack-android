package week11.st765512.finalproject.ui.screens.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.delay
import week11.st765512.finalproject.ui.components.CustomButton
import week11.st765512.finalproject.ui.components.CustomTextField
import week11.st765512.finalproject.ui.components.SuccessPill
import week11.st765512.finalproject.ui.components.TripListCard
import week11.st765512.finalproject.ui.viewmodel.TripUiState
import week11.st765512.finalproject.ui.viewmodel.TripViewModel

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
                title = { Text("Trips Saved") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            FilterPanel(
                uiState = uiState,
                onSearchChange = tripViewModel::updateSearchQuery,
                onTagChange = tripViewModel::updateFilterTag,
                onLocationChange = tripViewModel::updateFilterLocation,
                onDateChange = tripViewModel::updateFilterDate,
                onClear = tripViewModel::clearFilters
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
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No trips yet",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Trips you save will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
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
                }
            }

            uiState.successMessage?.let { message ->
                SuccessPill(
                    message = message,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
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
    onDateChange: (Long?) -> Unit,
    onClear: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
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

            CustomTextField(
                value = uiState.filterTag,
                onValueChange = onTagChange,
                label = "Tag"
            )

            CustomTextField(
                value = uiState.filterLocation,
                onValueChange = onLocationChange,
                label = "Location"
            )

            CustomButton(
                text = "Clear Filters",
                onClick = onClear
            )
        }
    }
}

