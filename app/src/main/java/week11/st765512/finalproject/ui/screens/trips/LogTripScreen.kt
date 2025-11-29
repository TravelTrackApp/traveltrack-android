package week11.st765512.finalproject.ui.screens.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import week11.st765512.finalproject.data.model.Result
import week11.st765512.finalproject.data.model.TripInput
import week11.st765512.finalproject.ui.components.CustomButton
import week11.st765512.finalproject.ui.components.CustomTextField
import week11.st765512.finalproject.ui.components.ErrorText
import week11.st765512.finalproject.ui.components.UnderlineTextField
import week11.st765512.finalproject.ui.components.GoogleMapView
import week11.st765512.finalproject.ui.components.ScreenStateWrapper
import week11.st765512.finalproject.ui.components.SuccessPill
import week11.st765512.finalproject.ui.viewmodel.AuthViewModel
import week11.st765512.finalproject.ui.viewmodel.TripViewModel
import week11.st765512.finalproject.util.ApiKeyHelper
import week11.st765512.finalproject.util.DirectionsHelper
import week11.st765512.finalproject.util.GeocodingHelper
import week11.st765512.finalproject.util.ReverseGeocodingHelper

@Composable
fun DrawerContent(
    onHome: () -> Unit,
    onLogTrip: () -> Unit,
    onSavedTrips: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "TravelTrack",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Navigate through the app",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            NavigationDrawerItem(
                label = { Text("Home") },
                selected = false,
                onClick = onHome,
                colors = NavigationDrawerItemDefaults.colors()
            )
            NavigationDrawerItem(
                label = { Text("Log Trip") },
                selected = true,
                onClick = onLogTrip,
                colors = NavigationDrawerItemDefaults.colors()
            )
            NavigationDrawerItem(
                label = { Text("View Trips") },
                selected = false,
                onClick = onSavedTrips,
                colors = NavigationDrawerItemDefaults.colors()
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 1.dp
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
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "See you soon!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = "Logout"
                    )
                }
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
    var uiState by remember { mutableStateOf(tripViewModel.uiState.value) }
    LaunchedEffect(Unit) {
        tripViewModel.uiState.collect { uiState = it }
    }

    var title by rememberSaveable { mutableStateOf("") }
    var startLocation by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }
    
    // Map coordinates and route
    var startLatLng by remember { mutableStateOf<LatLng?>(null) }
    var destinationLatLng by remember { mutableStateOf<LatLng?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var routeInfo by remember { mutableStateOf<DirectionsHelper.RouteInfo?>(null) }
    var calculatedDistance by remember { mutableStateOf(0.0) }
    var calculatedDurationMinutes by remember { mutableStateOf(0) }
    var isCalculatingRoute by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val mapsApiKey = remember { ApiKeyHelper.getMapsApiKey(context) }
    
    // Calculate route between two points
    fun calculateRoute(origin: LatLng, dest: LatLng, apiKey: String) {
        isCalculatingRoute = true
        scope.launch {
            when (val result = DirectionsHelper.getRoute(origin, dest, apiKey, context)) {
                is Result.Success -> {
                    routeInfo = result.data
                    calculatedDistance = result.data.distanceKm
                    calculatedDurationMinutes = result.data.durationMinutes
                    routePoints = result.data.polylinePoints
                }
                is Result.Error -> {
                    val errorMsg = result.exception.message ?: "Unknown error"
                    // Provide user-friendly error message for API key issues
                    formError = if (errorMsg.contains("not authorized") || errorMsg.contains("API key")) {
                        "API key configuration issue. Please check Google Cloud Console settings."
                    } else {
                        "Failed to calculate route: $errorMsg"
                    }
                    routePoints = emptyList()
                    routeInfo = null
                }
                is Result.Loading -> {
                    // Loading state
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
            
            // Calculate route if both points are set
            if (startLatLng != null && destinationLatLng != null && mapsApiKey != null) {
                calculateRoute(startLatLng!!, destinationLatLng!!, mapsApiKey)
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
                // Calculate route if destination is also set
                if (destinationLatLng != null && mapsApiKey != null) {
                    calculateRoute(it, destinationLatLng!!, mapsApiKey!!)
                }
            }
        }
    }
    
    LaunchedEffect(destination) {
        if (destination.isNotBlank() && destinationLatLng == null) {
            val coordinates = GeocodingHelper.parseCoordinates(destination)
            coordinates?.let {
                destinationLatLng = it
                // Calculate route if start is also set
                if (startLatLng != null && mapsApiKey != null) {
                    calculateRoute(startLatLng!!, it, mapsApiKey!!)
                }
            }
        }
    }
    
    // Calculate route when both coordinates are available
    LaunchedEffect(startLatLng, destinationLatLng) {
        if (startLatLng != null && destinationLatLng != null && mapsApiKey != null && routePoints.isEmpty()) {
            calculateRoute(startLatLng!!, destinationLatLng!!, mapsApiKey!!)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
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
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Log Trip") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                tonalElevation = 2.dp
            ) {
                // Map with click-to-select functionality
                GoogleMapView(
                    startLocation = startLatLng,
                    destination = destinationLatLng,
                    onLocationSelected = { latLng ->
                        handleMapLocationSelected(latLng)
                    },
                    routePoints = routePoints,
                    routeInfo = routeInfo,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    UnderlineTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Trip Name",
                        placeholder = "Trip Name",
                        enabled = !uiState.isSubmitting
                    )

                    UnderlineTextField(
                        value = startLocation,
                        onValueChange = { startLocation = it },
                        label = "Starting Point",
                        placeholder = "Starting Point",
                        enabled = !uiState.isSubmitting,
                        trailingIcon = Icons.Default.RadioButtonUnchecked
                    )

                    UnderlineTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = "Destination Point",
                        placeholder = "Destination Point",
                        enabled = !uiState.isSubmitting,
                        trailingIcon = Icons.Default.LocationOn
                    )

                    CustomTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Notes",
                        singleLine = false,
                        maxLines = 4,
                        enabled = !uiState.isSubmitting
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                routeInfo = null
                calculatedDistance = 0.0
                calculatedDurationMinutes = 0
                delay(2500)
                tripViewModel.clearMessage()
            }
        }
    }
}

