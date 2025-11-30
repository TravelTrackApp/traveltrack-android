package week11.st765512.finalproject.ui.screens.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import week11.st765512.finalproject.data.model.Trip
import week11.st765512.finalproject.ui.components.CustomButton
import week11.st765512.finalproject.ui.components.CustomTextField
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

    var isEditing by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var startLocation by remember { mutableStateOf("") }
    var destinationLocation by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var durationHours by remember { mutableStateOf("") }
    LaunchedEffect(trip) {
        if (trip != null) {
            title = trip.title
            startLocation = trip.startLocation
            destinationLocation = trip.destinationLocation
            notes = trip.notes
            distance = trip.distanceKm.toString()
            durationHours = (trip.durationMinutes / 60f).toString()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = trip?.title ?: "Trip Details",
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
                actions = {
                    if (!isEditing) {
                        Surface(
                            onClick = { isEditing = true },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit, 
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    } else {
                        Surface(
                            onClick = { isEditing = false },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close, 
                                contentDescription = "Cancel edit",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.size(8.dp))

                    Surface(
                        onClick = {
                            tripViewModel.deleteTrip(tripId) {
                                tripViewModel.clearSelectedTrip()
                                onTripDeleted()
                            }
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete trip",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.size(8.dp))
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

            if (!isEditing) {
                ViewModeContent(trip)
            } else {
                EditModeContent(
                    title = title,
                    onTitleChange = { title = it },
                    startLocation = startLocation,
                    onStartChange = { startLocation = it },
                    destinationLocation = destinationLocation,
                    onDestinationChange = { destinationLocation = it },
                    notes = notes,
                    onNotesChange = { notes = it },
                    distance = distance,
                    onDistanceChange = { distance = it },
                    durationHours = durationHours,
                    onDurationChange = { durationHours = it },
                    onApply = {
                        val updates = mapOf(
                            "title" to title,
                            "startLocation" to startLocation,
                            "destinationLocation" to destinationLocation,
                            "notes" to notes,
                            "distanceKm" to (distance.toDoubleOrNull() ?: 0.0),
                            "durationMinutes" to ((durationHours.toDoubleOrNull()
                                ?: 0.0) * 60).toInt()
                        )

                        tripViewModel.updateTrip(tripId, updates) {
                            isEditing = false
                        }
                    },
                    onCancel = { isEditing = false }
                )
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
fun ViewModeContent(trip: Trip) {
    // Route card
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Route header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(8.dp)
                ) {}
                Text(
                    text = trip.startLocation,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Text(
                text = "↓",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 0.dp)
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(8.dp)
                ) {}
                Text(
                    text = trip.destinationLocation,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Notes
            if (trip.notes.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = trip.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Text(
                    text = "No notes added",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // Stats row
            RowInfoChips(
                distance = trip.distanceKm,
                durationMinutes = trip.durationMinutes
            )
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Metadata card
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Trip Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoChip(
                    label = "Logged On",
                    value = TimeFormatter.formatDate(trip.createdAt),
                    modifier = Modifier.weight(1f)
                )
                InfoChip(
                    label = "Tags",
                    value = if (trip.tags.isEmpty()) "None" else trip.tags.joinToString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
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

@Composable
fun EditModeContent(
    title: String,
    onTitleChange: (String) -> Unit,
    startLocation: String,
    onStartChange: (String) -> Unit,
    destinationLocation: String,
    onDestinationChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    distance: String,
    onDistanceChange: (String) -> Unit,
    durationHours: String,
    onDurationChange: (String) -> Unit,
    onApply: () -> Unit,
    onCancel: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CustomTextField(value = title, onValueChange = onTitleChange, label = "Title")
        CustomTextField(value = startLocation, onValueChange = onStartChange, label = "Start Location")
        CustomTextField(value = destinationLocation, onValueChange = onDestinationChange, label = "Destination")
        CustomTextField(value = distance, onValueChange = onDistanceChange, label = "Distance (km)")
        CustomTextField(value = durationHours, onValueChange = onDurationChange, label = "Duration (hours)")
        CustomTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = "Notes",
            singleLine = false,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(24.dp))

        CustomButton(text = "Apply Changes", onClick = onApply)
        CustomButton(text = "Cancel", onClick = onCancel)
    }
}


