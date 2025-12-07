/**
 * File: LogTripScreen.kt
 * 
 * Screen for creating new trips. Allows users to input trip details, select locations
 * with autocomplete, view route on map, upload photos, and save trip to Firestore.
 */
package week11.st765512.finalproject.ui.screens.trips

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import week11.st765512.finalproject.data.model.RouteInfo
import week11.st765512.finalproject.data.repository.StorageRepository
import week11.st765512.finalproject.util.DirectionsHelper
import week11.st765512.finalproject.util.LocationHelper
import week11.st765512.finalproject.util.ReverseGeocodingHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import android.util.Log
import week11.st765512.finalproject.data.model.Result
import week11.st765512.finalproject.data.model.TripInput
import week11.st765512.finalproject.ui.components.CustomButton
import week11.st765512.finalproject.ui.components.CustomTextField
import week11.st765512.finalproject.ui.components.ErrorText
import week11.st765512.finalproject.ui.components.UnderlineTextField
import week11.st765512.finalproject.ui.components.GoogleMapView
import week11.st765512.finalproject.ui.components.ScreenStateWrapper
import week11.st765512.finalproject.ui.components.SuccessPill
import week11.st765512.finalproject.ui.components.AutocompleteTextField
import week11.st765512.finalproject.ui.viewmodel.AuthViewModel
import week11.st765512.finalproject.ui.viewmodel.TripViewModel
import week11.st765512.finalproject.util.ApiKeyHelper
import week11.st765512.finalproject.util.GeocodingHelper
import week11.st765512.finalproject.util.PlacesAutocompleteHelper

@Composable
fun DrawerContent(
    onHome: () -> Unit,
    onLogTrip: () -> Unit,
    onSavedTrips: () -> Unit,
    onLogout: () -> Unit,
    selectedItem: String = "Log Trip"
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // App branding section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TravelTrack",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Your journey companion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            NavigationDrawerItem(
                label = { 
                    Text(
                        "Home",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    ) 
                },
                selected = selectedItem == "Home",
                onClick = onHome,
                shape = RoundedCornerShape(14.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
            NavigationDrawerItem(
                label = { 
                    Text(
                        "Log Trip",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    ) 
                },
                selected = selectedItem == "Log Trip",
                onClick = onLogTrip,
                shape = RoundedCornerShape(14.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
            NavigationDrawerItem(
                label = { 
                    Text(
                        "View Trips",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    ) 
                },
                selected = selectedItem == "View Trips",
                onClick = onSavedTrips,
                shape = RoundedCornerShape(14.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }

        // Logout section
        Surface(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "See you soon!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogTripScreen(
    tripViewModel: TripViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToTripList: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var uiState by remember { mutableStateOf(tripViewModel.uiState.value) }
    LaunchedEffect(Unit) {
        tripViewModel.uiState.collect { uiState = it }
    }
    
    // Initialize Places SDK for autocomplete
    LaunchedEffect(Unit) {
        PlacesAutocompleteHelper.initialize(context)
    }

    var title by rememberSaveable { mutableStateOf("") }
    var startLocation by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var tags by rememberSaveable { mutableStateOf("") }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }
    
    // Map coordinates and route
    var startLatLng by remember { mutableStateOf<LatLng?>(null) }
    var destinationLatLng by remember { mutableStateOf<LatLng?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var drivingRouteInfo by remember { mutableStateOf<DirectionsHelper.RouteInfo?>(null) }
    var bicyclingRouteInfo by remember { mutableStateOf<DirectionsHelper.RouteInfo?>(null) }
    var walkingRouteInfo by remember { mutableStateOf<DirectionsHelper.RouteInfo?>(null) }
    var calculatedDistance by remember { mutableStateOf(0.0) }
    var calculatedDurationMinutes by remember { mutableStateOf(0) }
    var isCalculatingRoute by remember { mutableStateOf(false) }
    
    // Selected image URIs (support multiple images)
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    
    val mapsApiKey = remember { ApiKeyHelper.getMapsApiKey(context) }
    
    // Storage repository for uploading images
    val storageRepository = remember { StorageRepository() }
    
    // Image picker launcher - use GetContent which works with file managers
    // This avoids Google Photos login requirement
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUris = selectedImageUris + it
        }
    }
    
    // Function to launch image picker - explicitly request image files
    fun launchImagePicker() {
        imagePickerLauncher.launch("image/*")
    }
    
    // Function to remove image
    fun removeImage(uri: Uri) {
        selectedImageUris = selectedImageUris.filter { it != uri }
    }
    
    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            // Permission granted, get current location
            scope.launch {
                try {
                    val location = LocationHelper.getCurrentLocation(context)
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        startLatLng = latLng
                        val address = ReverseGeocodingHelper.getShortAddress(latLng, context)
                        startLocation = address ?: "${latLng.latitude},${latLng.longitude}"
                    } else {
                        formError = "Unable to get current location. Please check location settings."
                    }
                } catch (e: Exception) {
                    formError = "Error getting location: ${e.message}"
                }
            }
        } else {
            formError = "Location permission is required to get your current location."
        }
    }
    
    // Function to get current location (with permission check)
    fun getCurrentLocationWithPermission() {
        // Check permission status at runtime
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            // Permission already granted, get location directly
            scope.launch {
                try {
                    val location = LocationHelper.getCurrentLocation(context)
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        startLatLng = latLng
                        val address = ReverseGeocodingHelper.getShortAddress(latLng, context)
                        startLocation = address ?: "${latLng.latitude},${latLng.longitude}"
                    } else {
                        formError = "Unable to get current location. Please check location settings."
                    }
                } catch (e: Exception) {
                    formError = "Error getting location: ${e.message}"
                }
            }
        } else {
            // Request permission
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    // Calculate routes for all three travel modes
    fun calculateRoutes(origin: LatLng, dest: LatLng, apiKey: String) {
        isCalculatingRoute = true
        scope.launch {
            // Calculate driving route
            when (val result = DirectionsHelper.getRoute(origin, dest, apiKey, context, DirectionsHelper.TravelMode.DRIVING)) {
                is Result.Success -> {
                    drivingRouteInfo = result.data
                    calculatedDistance = result.data.distanceKm
                    calculatedDurationMinutes = result.data.durationMinutes
                    routePoints = result.data.polylinePoints
                }
                is Result.Error -> {
                    val errorMsg = result.exception.message ?: "Unknown error"
                    formError = if (errorMsg.contains("not authorized") || errorMsg.contains("API key")) {
                        "API key configuration issue. Please check Google Cloud Console settings."
                    } else {
                        "Failed to calculate route: $errorMsg"
                    }
                }
                is Result.Loading -> {}
            }
            
            // Calculate bicycling route
            DirectionsHelper.getRoute(origin, dest, apiKey, context, DirectionsHelper.TravelMode.BICYCLING).let { result ->
                if (result is Result.Success) {
                    bicyclingRouteInfo = result.data
                }
            }
            
            // Calculate walking route
            DirectionsHelper.getRoute(origin, dest, apiKey, context, DirectionsHelper.TravelMode.WALKING).let { result ->
                if (result is Result.Success) {
                    walkingRouteInfo = result.data
                }
            }
            
            isCalculatingRoute = false
        }
    }
    
    // Handle map click to select locations
    fun handleMapLocationSelected(latLng: LatLng) {
        scope.launch {
            // Determine if we should set start or destination based on current state
            val isStart = startLatLng == null
            
            if (isStart) {
                // Set start location
                startLatLng = latLng
                // Get address from coordinates
                val address = ReverseGeocodingHelper.getShortAddress(latLng, context)
                startLocation = address ?: "${latLng.latitude},${latLng.longitude}"
            } else {
                // Set destination location
                destinationLatLng = latLng
                // Get address from coordinates
                val address = ReverseGeocodingHelper.getShortAddress(latLng, context)
                destination = address ?: "${latLng.latitude},${latLng.longitude}"
            }
            
            // Calculate routes if both points are set
            if (startLatLng != null && destinationLatLng != null && mapsApiKey != null) {
                calculateRoutes(startLatLng!!, destinationLatLng!!, mapsApiKey)
            }
        }
    }
    
    // Update map coordinates when text input changes
    LaunchedEffect(startLocation) {
        if (startLocation.isNotBlank() && startLatLng == null) {
            // Check if it's a coordinate string (lat,lng)
            val coordinates = GeocodingHelper.parseCoordinates(startLocation)
            coordinates?.let {
                startLatLng = it
                // Calculate routes if destination is also set
                if (destinationLatLng != null && mapsApiKey != null) {
                    calculateRoutes(it, destinationLatLng!!, mapsApiKey!!)
                }
            }
        }
    }
    
    LaunchedEffect(destination) {
        if (destination.isNotBlank() && destinationLatLng == null) {
            val coordinates = GeocodingHelper.parseCoordinates(destination)
            coordinates?.let {
                destinationLatLng = it
                // Calculate routes if start is also set
                if (startLatLng != null && mapsApiKey != null) {
                    calculateRoutes(startLatLng!!, it, mapsApiKey!!)
                }
            }
        }
    }
    
    // Calculate routes when both coordinates are available
    LaunchedEffect(startLatLng, destinationLatLng) {
        if (startLatLng != null && destinationLatLng != null && mapsApiKey != null && routePoints.isEmpty()) {
            calculateRoutes(startLatLng!!, destinationLatLng!!, mapsApiKey!!)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    onHome = {
                        scope.launch { drawerState.close() }
                        onNavigateToHome()
                    },
                    onLogTrip = {
                        scope.launch { drawerState.close() }
                    },
                    onSavedTrips = {
                        scope.launch { drawerState.close() }
                        onNavigateToTripList()
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        authViewModel.signOut()
                    },
                    selectedItem = "Log Trip"
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Log Trip",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
            // Map section with rounded corners
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                // Map with click-to-select functionality
                GoogleMapView(
                    startLocation = startLatLng,
                    destination = destinationLatLng,
                    onLocationSelected = { latLng ->
                        handleMapLocationSelected(latLng)
                    },
                    routePoints = routePoints,
                    drivingRouteInfo = drivingRouteInfo,
                    bicyclingRouteInfo = bicyclingRouteInfo,
                    walkingRouteInfo = walkingRouteInfo,
                    onGetCurrentLocation = {
                        // Get current location with permission check
                        getCurrentLocationWithPermission()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Form section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UnderlineTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Trip Name",
                        placeholder = "Trip Name",
                        enabled = !uiState.isSubmitting
                    )

                    AutocompleteTextField(
                        value = startLocation,
                        onValueChange = { startLocation = it },
                        onPlaceSelected = { placeDetails ->
                            // Set the coordinates from selected place
                            startLatLng = placeDetails.latLng
                            startLocation = placeDetails.address.ifEmpty { placeDetails.name }
                            
                            // Calculate routes if destination is also set
                            if (destinationLatLng != null && mapsApiKey != null) {
                                calculateRoutes(placeDetails.latLng, destinationLatLng!!, mapsApiKey!!)
                            }
                        },
                        label = "Starting Point",
                        placeholder = "Search or tap map",
                        enabled = !uiState.isSubmitting,
                        trailingIcon = Icons.Default.RadioButtonUnchecked
                    )

                    AutocompleteTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        onPlaceSelected = { placeDetails ->
                            // Set the coordinates from selected place
                            destinationLatLng = placeDetails.latLng
                            destination = placeDetails.address.ifEmpty { placeDetails.name }
                            
                            // Calculate routes if start is also set
                            if (startLatLng != null && mapsApiKey != null) {
                                calculateRoutes(startLatLng!!, placeDetails.latLng, mapsApiKey!!)
                            }
                        },
                        label = "Destination Point",
                        placeholder = "Search or tap map",
                        enabled = !uiState.isSubmitting,
                        trailingIcon = Icons.Default.LocationOn
                    )

                    UnderlineTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Notes",
                        placeholder = "Notes",
                        enabled = !uiState.isSubmitting,
                        singleLine = false,
                        maxLines = 4
                    )
                    
                    UnderlineTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = "Tags",
                        placeholder = "e.g. vacation, business, family",
                        enabled = !uiState.isSubmitting
                    )
                    
                    // Photo selection section
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Photos (Optional)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Display selected images
                            selectedImageUris.forEach { uri ->
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
                                            contentDescription = "Selected photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    // Delete button (minus icon) on top right corner
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.TopEnd)
                                            .padding(2.dp)
                                            .clickable { removeImage(uri) }
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Remove photo",
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .padding(4.dp),
                                                tint = MaterialTheme.colorScheme.onError
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Add photo button
                            Surface(
                                onClick = { launchImagePicker() },
                                enabled = !uiState.isSubmitting,
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .height(76.dp)
                                    .width(76.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add photo",
                                        modifier = Modifier.size(22.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Add",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Submit section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ErrorText(message = formError)

                    uiState.successMessage?.let { message ->
                        SuccessPill(
                            message = message,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    CustomButton(
                        text = "Log Trip",
                        onClick = {
                            if (title.isBlank() || startLocation.isBlank() || destination.isBlank()) {
                                formError = "Please fill out all required fields."
                                return@CustomButton
                            }
                            
                            formError = null
                            
                            // Upload images and save trip
                            scope.launch {
                                try {
                                    var photoUrls = emptyList<String>()
                                    
                                    // Upload all selected images
                                    if (selectedImageUris.isNotEmpty()) {
                                        when (val uploadResult = storageRepository.uploadImages(selectedImageUris, context)) {
                                            is Result.Success -> {
                                                photoUrls = uploadResult.data
                                            }
                                            is Result.Error -> {
                                                formError = "Failed to upload images: ${uploadResult.exception.message}"
                                                return@launch
                                            }
                                            Result.Loading -> Unit
                                        }
                                    }
                                    
                                    // Build route info list from calculated routes
                                    val routeInfoList = mutableListOf<RouteInfo>()
                                    drivingRouteInfo?.let { info ->
                                        routeInfoList.add(
                                            RouteInfo(
                                                travelMode = "DRIVING",
                                                distanceKm = info.distanceKm,
                                                durationMinutes = info.durationMinutes
                                            )
                                        )
                                    }
                                    bicyclingRouteInfo?.let { info ->
                                        routeInfoList.add(
                                            RouteInfo(
                                                travelMode = "BICYCLING",
                                                distanceKm = info.distanceKm,
                                                durationMinutes = info.durationMinutes
                                            )
                                        )
                                    }
                                    walkingRouteInfo?.let { info ->
                                        routeInfoList.add(
                                            RouteInfo(
                                                travelMode = "WALKING",
                                                distanceKm = info.distanceKm,
                                                durationMinutes = info.durationMinutes
                                            )
                                        )
                                    }
                                    
                                    // Use calculated values if available, otherwise use manual input
                                    val distanceValue = if (calculatedDistance > 0) {
                                        calculatedDistance
                                    } else {
                                        0.0
                                    }
                                    
                                    val durationMinutes = if (calculatedDurationMinutes > 0) {
                                        calculatedDurationMinutes
                                    } else {
                                        0
                                    }
                                    
                                    // Parse tags from comma-separated input
                                    val tagsList = tags.split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotBlank() }
                                    
                                    // Save trip with all data
                                    val tripInput = TripInput(
                                        title = title.trim(),
                                        startLocation = startLocation.trim(),
                                        destinationLocation = destination.trim(),
                                        startLatLng = startLatLng?.let { "${it.latitude},${it.longitude}" } ?: "",
                                        destinationLatLng = destinationLatLng?.let { "${it.latitude},${it.longitude}" } ?: "",
                                        notes = notes.trim(),
                                        distanceKm = distanceValue,
                                        durationMinutes = durationMinutes,
                                        routeInfo = routeInfoList,
                                        tags = tagsList,
                                        photoUrls = photoUrls
                                    )
                                    
                                    tripViewModel.saveTrip(tripInput)
                                } catch (e: Exception) {
                                    Log.e("LogTripScreen", "Error saving trip", e)
                                    formError = "Error saving trip: ${e.message ?: "Unknown error"}"
                                }
                            }
                        },
                        isLoading = uiState.isSubmitting || isCalculatingRoute,
                        enabled = title.isNotBlank() && startLocation.isNotBlank() && destination.isNotBlank() && !isCalculatingRoute
                    )
                }
            }
        }
        }
        }
        
        LaunchedEffect(uiState.successMessage) {
            if (uiState.successMessage != null) {
                title = ""
                startLocation = ""
                destination = ""
                notes = ""
                startLatLng = null
                destinationLatLng = null
                routePoints = emptyList()
                drivingRouteInfo = null
                bicyclingRouteInfo = null
                walkingRouteInfo = null
                calculatedDistance = 0.0
                calculatedDurationMinutes = 0
                selectedImageUris = emptyList()
                delay(2500)
                tripViewModel.clearMessage()
            }
        }
    }
}

