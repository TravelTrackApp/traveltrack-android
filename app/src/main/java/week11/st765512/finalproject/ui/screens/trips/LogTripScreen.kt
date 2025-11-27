package week11.st765512.finalproject.ui.screens.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlin.math.roundToInt
import week11.st765512.finalproject.data.model.TripInput
import week11.st765512.finalproject.ui.components.CustomButton
import week11.st765512.finalproject.ui.components.CustomTextField
import week11.st765512.finalproject.ui.components.ErrorText
import week11.st765512.finalproject.ui.components.GoogleMapView
import week11.st765512.finalproject.ui.components.MapPlaceholder
import week11.st765512.finalproject.ui.components.ScreenStateWrapper
import week11.st765512.finalproject.ui.components.SuccessPill
import week11.st765512.finalproject.ui.viewmodel.TripViewModel
import week11.st765512.finalproject.util.GeocodingHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogTripScreen(
    tripViewModel: TripViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var uiState by remember { mutableStateOf(tripViewModel.uiState.value) }
    LaunchedEffect(Unit) {
        tripViewModel.uiState.collect { uiState = it }
    }

    var title by rememberSaveable { mutableStateOf("") }
    var startLocation by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }
    var distance by rememberSaveable { mutableStateOf("") }
    var durationHours by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }
    
    // Map coordinates
    var startLatLng by remember { mutableStateOf<LatLng?>(null) }
    var destinationLatLng by remember { mutableStateOf<LatLng?>(null) }
    
    val context = LocalContext.current
    
    // Update map coordinates when locations change
    LaunchedEffect(startLocation) {
        if (startLocation.isNotBlank()) {
            // Check if it's a coordinate string (lat,lng)
            val coordinates = GeocodingHelper.parseCoordinates(startLocation)
            startLatLng = coordinates
            // TODO: If not coordinates, use geocoding API to convert address to LatLng
            // startLatLng = GeocodingHelper.addressToLatLng(startLocation, context)
        } else {
            startLatLng = null
        }
    }
    
    LaunchedEffect(destination) {
        if (destination.isNotBlank()) {
            val coordinates = GeocodingHelper.parseCoordinates(destination)
            destinationLatLng = coordinates
            // TODO: If not coordinates, use geocoding API to convert address to LatLng
            // destinationLatLng = GeocodingHelper.addressToLatLng(destination, context)
        } else {
            destinationLatLng = null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Log Trip") },
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
        ScreenStateWrapper(
            isLoading = uiState.isSubmitting || uiState.isLoading,
            errorMessage = uiState.errorMessage,
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(32.dp),
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Map preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // Always show map, even without coordinates
                    GoogleMapView(
                        startLocation = startLatLng,
                        destination = destinationLatLng,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    
                    // Helper text
                    Text(
                        text = "Tip: Enter coordinates as 'latitude,longitude' or address",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            CustomTextField(
                value = title,
                onValueChange = { title = it },
                label = "Trip Title",
                enabled = !uiState.isSubmitting
            )

            CustomTextField(
                value = startLocation,
                onValueChange = { startLocation = it },
                label = "Starting Point",
                enabled = !uiState.isSubmitting
            )

            CustomTextField(
                value = destination,
                onValueChange = { destination = it },
                label = "Destination Point",
                enabled = !uiState.isSubmitting
            )

            CustomTextField(
                value = distance,
                onValueChange = { distance = it },
                label = "Distance (km)",
                keyboardType = KeyboardType.Decimal,
                enabled = !uiState.isSubmitting
            )

            CustomTextField(
                value = durationHours,
                onValueChange = { durationHours = it },
                label = "Duration (hrs)",
                keyboardType = KeyboardType.Decimal,
                enabled = !uiState.isSubmitting
            )

            CustomTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes",
                singleLine = false,
                maxLines = 4,
                enabled = !uiState.isSubmitting
            )

            ErrorText(message = formError)

            uiState.successMessage?.let { message ->
                SuccessPill(
                    message = message,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            CustomButton(
                text = "Save Trip",
                onClick = {
                    if (startLocation.isBlank() || destination.isBlank() || title.isBlank()) {
                        formError = "Please fill out all required fields."
                        return@CustomButton
                    }
                    val distanceValue = distance.toDoubleOrNull() ?: 0.0
                    val durationMinutes = ((durationHours.toDoubleOrNull() ?: 0.0) * 60).roundToInt()

                    formError = null
                    tripViewModel.saveTrip(
                        TripInput(
                            title = title,
                            startLocation = startLocation,
                            destinationLocation = destination,
                            notes = notes,
                            distanceKm = distanceValue,
                            durationMinutes = durationMinutes
                        )
                    )
                },
                isLoading = uiState.isSubmitting,
                enabled = title.isNotBlank() && startLocation.isNotBlank() && destination.isNotBlank()
            )
        }
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            title = ""
            startLocation = ""
            destination = ""
            distance = ""
            durationHours = ""
            notes = ""
            delay(2500)
            tripViewModel.clearMessage()
        }
    }
}

