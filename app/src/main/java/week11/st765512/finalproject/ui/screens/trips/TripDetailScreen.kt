/**
 * File: TripDetailScreen.kt
 * 
 * Screen displaying detailed information for a single trip. Shows trip details,
 * route map, photos, and allows editing and deletion of trips.
 */
package week11.st765512.finalproject.ui.screens.trips

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.google.android.gms.maps.model.LatLng
import week11.st765512.finalproject.data.model.Result
import week11.st765512.finalproject.data.model.RouteInfo
import week11.st765512.finalproject.data.model.Trip
import week11.st765512.finalproject.data.repository.StorageRepository
import week11.st765512.finalproject.ui.components.AutocompleteTextField
import week11.st765512.finalproject.ui.components.CustomButton
import week11.st765512.finalproject.ui.components.CustomTextField
import week11.st765512.finalproject.ui.components.GoogleMapView
import week11.st765512.finalproject.ui.components.InfoChip
import week11.st765512.finalproject.ui.viewmodel.TripViewModel
import week11.st765512.finalproject.util.ApiKeyHelper
import week11.st765512.finalproject.util.DirectionsHelper
import week11.st765512.finalproject.util.GeocodingHelper
import week11.st765512.finalproject.util.PlacesAutocompleteHelper
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storageRepository = remember { StorageRepository() }

    var title by remember { mutableStateOf("") }
    var startLocation by remember { mutableStateOf("") }
    var destinationLocation by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var durationHours by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    
    // Map coordinates for editing
    var editStartLatLng by remember { mutableStateOf<LatLng?>(null) }
    var editDestinationLatLng by remember { mutableStateOf<LatLng?>(null) }
    var editRoutePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var editDrivingRouteInfo by remember { mutableStateOf<DirectionsHelper.RouteInfo?>(null) }
    var editBicyclingRouteInfo by remember { mutableStateOf<DirectionsHelper.RouteInfo?>(null) }
    var editWalkingRouteInfo by remember { mutableStateOf<DirectionsHelper.RouteInfo?>(null) }
    var isCalculatingRoute by remember { mutableStateOf(false) }
    
    val mapsApiKey = remember { ApiKeyHelper.getMapsApiKey(context) }
    
    // Initialize Places SDK for autocomplete
    LaunchedEffect(Unit) {
        PlacesAutocompleteHelper.initialize(context)
    }
    
    // Photo editing state
    var existingPhotoUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var newImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploadingPhotos by remember { mutableStateOf(false) }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            newImageUris = newImageUris + it
        }
    }
    
    // Function to calculate routes for all travel modes
    fun calculateRoutes(origin: LatLng, dest: LatLng, apiKey: String) {
        isCalculatingRoute = true
        scope.launch {
            // Calculate driving route
            when (val result = DirectionsHelper.getRoute(origin, dest, apiKey, context, DirectionsHelper.TravelMode.DRIVING)) {
                is Result.Success -> {
                    editDrivingRouteInfo = result.data
                    distance = "%.3f".format(result.data.distanceKm)
                    durationHours = "%.8f".format(result.data.durationMinutes / 60f)
                    editRoutePoints = result.data.polylinePoints
                }
                is Result.Error -> {
                    Log.e("TripDetailScreen", "Failed to calculate route: ${result.exception.message}")
                }
                is Result.Loading -> {}
            }
            
            // Calculate bicycling route
            DirectionsHelper.getRoute(origin, dest, apiKey, context, DirectionsHelper.TravelMode.BICYCLING).let { result ->
                if (result is Result.Success) {
                    editBicyclingRouteInfo = result.data
                }
            }
            
            // Calculate walking route
            DirectionsHelper.getRoute(origin, dest, apiKey, context, DirectionsHelper.TravelMode.WALKING).let { result ->
                if (result is Result.Success) {
                    editWalkingRouteInfo = result.data
                }
            }
            
            isCalculatingRoute = false
        }
    }
    
    LaunchedEffect(trip) {
        if (trip != null) {
            title = trip.title
            startLocation = trip.startLocation
            destinationLocation = trip.destinationLocation
            notes = trip.notes
            distance = trip.distanceKm.toString()
            durationHours = (trip.durationMinutes / 60f).toString()
            tags = trip.tags.joinToString(", ")
            existingPhotoUrls = trip.photoUrls
            newImageUris = emptyList()
            
            // Initialize coordinates from trip data
            editStartLatLng = if (trip.startLatLng.isNotBlank()) {
                GeocodingHelper.parseCoordinates(trip.startLatLng)
            } else null
            
            editDestinationLatLng = if (trip.destinationLatLng.isNotBlank()) {
                GeocodingHelper.parseCoordinates(trip.destinationLatLng)
            } else null
            
            // Reset route info
            editRoutePoints = emptyList()
            editDrivingRouteInfo = null
            editBicyclingRouteInfo = null
            editWalkingRouteInfo = null
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
                    onStartPlaceSelected = { placeDetails ->
                        editStartLatLng = placeDetails.latLng
                        startLocation = placeDetails.address.ifEmpty { placeDetails.name }
                        
                        // Calculate routes if destination is also set
                        if (editDestinationLatLng != null && mapsApiKey != null) {
                            calculateRoutes(placeDetails.latLng, editDestinationLatLng!!, mapsApiKey)
                        }
                    },
                    destinationLocation = destinationLocation,
                    onDestinationChange = { destinationLocation = it },
                    onDestinationPlaceSelected = { placeDetails ->
                        editDestinationLatLng = placeDetails.latLng
                        destinationLocation = placeDetails.address.ifEmpty { placeDetails.name }
                        
                        // Calculate routes if start is also set
                        if (editStartLatLng != null && mapsApiKey != null) {
                            calculateRoutes(editStartLatLng!!, placeDetails.latLng, mapsApiKey)
                        }
                    },
                    startLatLng = editStartLatLng,
                    destinationLatLng = editDestinationLatLng,
                    routePoints = editRoutePoints,
                    drivingRouteInfo = editDrivingRouteInfo,
                    bicyclingRouteInfo = editBicyclingRouteInfo,
                    walkingRouteInfo = editWalkingRouteInfo,
                    notes = notes,
                    onNotesChange = { notes = it },
                    distance = distance,
                    onDistanceChange = { distance = it },
                    durationHours = durationHours,
                    onDurationChange = { durationHours = it },
                    tags = tags,
                    onTagsChange = { tags = it },
                    existingPhotoUrls = existingPhotoUrls,
                    onRemoveExistingPhoto = { url -> existingPhotoUrls = existingPhotoUrls - url },
                    newImageUris = newImageUris,
                    onRemoveNewImage = { uri -> newImageUris = newImageUris - uri },
                    onAddPhoto = { imagePickerLauncher.launch("image/*") },
                    isUploadingPhotos = isUploadingPhotos || isCalculatingRoute,
                    onApply = {
                        scope.launch {
                            isUploadingPhotos = true
                            
                            // Upload new images
                            var uploadedUrls = emptyList<String>()
                            if (newImageUris.isNotEmpty()) {
                                when (val uploadResult = storageRepository.uploadImages(newImageUris, context)) {
                                    is Result.Success -> {
                                        uploadedUrls = uploadResult.data
                                    }
                                    is Result.Error -> {
                                        isUploadingPhotos = false
                                        return@launch
                                    }
                                    Result.Loading -> Unit
                                }
                            }
                            
                            // Combine existing (non-removed) photos with newly uploaded ones
                            val finalPhotoUrls = existingPhotoUrls + uploadedUrls
                            
                            val tagsList = tags.split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                            
                            // Build route info list from calculated routes
                            val routeInfoList = mutableListOf<RouteInfo>()
                            editDrivingRouteInfo?.let { info ->
                                routeInfoList.add(
                                    RouteInfo(
                                        travelMode = "DRIVING",
                                        distanceKm = info.distanceKm,
                                        durationMinutes = info.durationMinutes
                                    )
                                )
                            }
                            editBicyclingRouteInfo?.let { info ->
                                routeInfoList.add(
                                    RouteInfo(
                                        travelMode = "BICYCLING",
                                        distanceKm = info.distanceKm,
                                        durationMinutes = info.durationMinutes
                                    )
                                )
                            }
                            editWalkingRouteInfo?.let { info ->
                                routeInfoList.add(
                                    RouteInfo(
                                        travelMode = "WALKING",
                                        distanceKm = info.distanceKm,
                                        durationMinutes = info.durationMinutes
                                    )
                                )
                            }
                            
                            val updates = mutableMapOf<String, Any>(
                                "title" to title,
                                "startLocation" to startLocation,
                                "destinationLocation" to destinationLocation,
                                "notes" to notes,
                                "distanceKm" to (distance.toDoubleOrNull() ?: 0.0),
                                "durationMinutes" to ((durationHours.toDoubleOrNull()
                                    ?: 0.0) * 60).toInt(),
                                "tags" to tagsList,
                                "photoUrls" to finalPhotoUrls
                            )
                            
                            // Add updated coordinates if they've been set
                            editStartLatLng?.let { latLng ->
                                updates["startLatLng"] = "${latLng.latitude},${latLng.longitude}"
                            }
                            editDestinationLatLng?.let { latLng ->
                                updates["destinationLatLng"] = "${latLng.latitude},${latLng.longitude}"
                            }
                            
                            // Add route info if recalculated
                            if (routeInfoList.isNotEmpty()) {
                                updates["routeInfo"] = routeInfoList
                            }

                            tripViewModel.updateTrip(tripId, updates) {
                                isUploadingPhotos = false
                                isEditing = false
                            }
                        }
                    },
                    onCancel = { 
                        // Reset photo state when canceling
                        existingPhotoUrls = trip.photoUrls
                        newImageUris = emptyList()
                        // Reset coordinate state
                        editStartLatLng = if (trip.startLatLng.isNotBlank()) {
                            GeocodingHelper.parseCoordinates(trip.startLatLng)
                        } else null
                        editDestinationLatLng = if (trip.destinationLatLng.isNotBlank()) {
                            GeocodingHelper.parseCoordinates(trip.destinationLatLng)
                        } else null
                        editRoutePoints = emptyList()
                        editDrivingRouteInfo = null
                        editBicyclingRouteInfo = null
                        editWalkingRouteInfo = null
                        isEditing = false 
                    }
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
    val context = LocalContext.current
    
    // Parse coordinates from stored strings
    val startLatLng = remember(trip.startLatLng) {
        if (trip.startLatLng.isNotBlank()) {
            GeocodingHelper.parseCoordinates(trip.startLatLng)
        } else null
    }
    
    val destinationLatLng = remember(trip.destinationLatLng) {
        if (trip.destinationLatLng.isNotBlank()) {
            GeocodingHelper.parseCoordinates(trip.destinationLatLng)
        } else null
    }
    
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

    // Map Display (if coordinates are available)
    if (startLatLng != null || destinationLatLng != null) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            GoogleMapView(
                startLocation = startLatLng,
                destination = destinationLatLng,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(18.dp))
    }

    // Photos Display
    if (trip.photoUrls.isNotEmpty()) {
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
                    text = "Photos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (trip.photoUrls.size == 1) {
                    // Single photo - full width
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(trip.photoUrls[0])
                                .build()
                        ),
                        contentDescription = "Trip photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Multiple photos - grid layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        trip.photoUrls.take(3).forEach { photoUrl ->
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(context)
                                        .data(photoUrl)
                                        .build()
                                ),
                                contentDescription = "Trip photo",
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    if (trip.photoUrls.size > 3) {
                        Text(
                            text = "+${trip.photoUrls.size - 3} more photos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(18.dp))
    }

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
    onStartPlaceSelected: (week11.st765512.finalproject.util.PlaceDetails) -> Unit,
    destinationLocation: String,
    onDestinationChange: (String) -> Unit,
    onDestinationPlaceSelected: (week11.st765512.finalproject.util.PlaceDetails) -> Unit,
    startLatLng: LatLng?,
    destinationLatLng: LatLng?,
    routePoints: List<LatLng>,
    drivingRouteInfo: DirectionsHelper.RouteInfo?,
    bicyclingRouteInfo: DirectionsHelper.RouteInfo?,
    walkingRouteInfo: DirectionsHelper.RouteInfo?,
    notes: String,
    onNotesChange: (String) -> Unit,
    distance: String,
    onDistanceChange: (String) -> Unit,
    durationHours: String,
    onDurationChange: (String) -> Unit,
    tags: String,
    onTagsChange: (String) -> Unit,
    existingPhotoUrls: List<String>,
    onRemoveExistingPhoto: (String) -> Unit,
    newImageUris: List<Uri>,
    onRemoveNewImage: (Uri) -> Unit,
    onAddPhoto: () -> Unit,
    isUploadingPhotos: Boolean,
    onApply: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CustomTextField(value = title, onValueChange = onTitleChange, label = "Title")
        
        // Start Location with autocomplete
        AutocompleteTextField(
            value = startLocation,
            onValueChange = onStartChange,
            onPlaceSelected = onStartPlaceSelected,
            label = "Start Location",
            placeholder = "Search location",
            trailingIcon = Icons.Default.RadioButtonUnchecked
        )
        
        // Destination with autocomplete
        AutocompleteTextField(
            value = destinationLocation,
            onValueChange = onDestinationChange,
            onPlaceSelected = onDestinationPlaceSelected,
            label = "Destination",
            placeholder = "Search location",
            trailingIcon = Icons.Default.LocationOn
        )
        
        // Map preview showing current/updated locations
        if (startLatLng != null || destinationLatLng != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                GoogleMapView(
                    startLocation = startLatLng,
                    destination = destinationLatLng,
                    routePoints = routePoints,
                    drivingRouteInfo = drivingRouteInfo,
                    bicyclingRouteInfo = bicyclingRouteInfo,
                    walkingRouteInfo = walkingRouteInfo,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        CustomTextField(value = distance, onValueChange = onDistanceChange, label = "Distance (km)")
        CustomTextField(value = durationHours, onValueChange = onDurationChange, label = "Duration (hours)")
        CustomTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = "Notes",
            singleLine = false,
            maxLines = 4
        )
        CustomTextField(
            value = tags,
            onValueChange = onTagsChange,
            label = "Tags (separated by comma)"
        )
        
        // Photos section
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Photos",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Display existing photos
                existingPhotoUrls.forEach { photoUrl ->
                    Box(modifier = Modifier.size(76.dp)) {
                        Surface(
                            modifier = Modifier.size(76.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(context)
                                        .data(photoUrl)
                                        .build()
                                ),
                                contentDescription = "Existing photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        // Delete button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(22.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = CircleShape
                                )
                                .clickable { onRemoveExistingPhoto(photoUrl) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove photo",
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                
                // Display newly selected images
                newImageUris.forEach { uri ->
                    Box(modifier = Modifier.size(76.dp)) {
                        Surface(
                            modifier = Modifier.size(76.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(context)
                                        .data(uri)
                                        .build()
                                ),
                                contentDescription = "New photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        // Delete button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(22.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.error,
                                    shape = CircleShape
                                )
                                .clickable { onRemoveNewImage(uri) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove photo",
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                
                // Add photo button
                if (existingPhotoUrls.size + newImageUris.size < 5) {
                    Surface(
                        modifier = Modifier
                            .size(76.dp)
                            .clickable { onAddPhoto() },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add photo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
            
            if (existingPhotoUrls.isEmpty() && newImageUris.isEmpty()) {
                Text(
                    text = "No photos added",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        CustomButton(
            text = if (isUploadingPhotos) "Uploading..." else "Apply Changes",
            onClick = onApply,
            enabled = !isUploadingPhotos
        )
        CustomButton(
            text = "Cancel", 
            onClick = onCancel,
            enabled = !isUploadingPhotos
        )
    }
}


