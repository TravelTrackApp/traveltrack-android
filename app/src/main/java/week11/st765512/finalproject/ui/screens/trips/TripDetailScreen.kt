package week11.st765512.finalproject.ui.screens.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import week11.st765512.finalproject.ui.components.CustomButton
import week11.st765512.finalproject.ui.components.InfoChip
import week11.st765512.finalproject.ui.viewmodel.TripViewModel
import week11.st765512.finalproject.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: String,
    tripViewModel: TripViewModel,
    onNavigateBack: () -> Unit,
    onTripDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var uiState by remember { mutableStateOf(tripViewModel.uiState.value) }
    LaunchedEffect(Unit) {
        tripViewModel.uiState.collect { uiState = it }
    }

    LaunchedEffect(tripId) {
        tripViewModel.refreshSelectedTrip(tripId)
    }

    val trip = uiState.trips.find { it.id == tripId } ?: uiState.selectedTrip

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(trip?.title ?: "Trip Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        tripViewModel.deleteTrip(tripId) {
                            tripViewModel.clearSelectedTrip()
                            onTripDeleted()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete trip"
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
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (trip == null) {
                Text(
                    text = "Loading trip details...",
                    style = MaterialTheme.typography.bodyLarge
                )
                return@Column
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${trip.startLocation} → ${trip.destinationLocation}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = trip.notes.ifBlank { "No notes added yet." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    RowInfoChips(
                        distance = trip.distanceKm,
                        durationMinutes = trip.durationMinutes
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Trip Metadata",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    InfoChip(
                        label = "Logged On",
                        value = uiState.trips.firstOrNull { it.id == trip.id }?.createdAt?.let {
                            TimeFormatter.formatDate(it)
                        } ?: "—"
                    )
                    InfoChip(
                        label = "Tags",
                        value = if (trip.tags.isEmpty()) "Not tagged" else trip.tags.joinToString()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            CustomButton(
                text = "Back to Trips",
                onClick = onNavigateBack
            )
        }
    }
}

@Composable
private fun RowInfoChips(distance: Double, durationMinutes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoChip(label = "Distance", value = "${"%.1f".format(distance)} km", modifier = Modifier.weight(1f))
        InfoChip(
            label = "Duration",
            value = if (durationMinutes > 0) "${"%.1f".format(durationMinutes / 60f)} hrs" else "N/A",
            modifier = Modifier.weight(1f)
        )
    }
}

