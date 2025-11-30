package week11.st765512.finalproject.ui.components

import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.awaitCancellation
import week11.st765512.finalproject.util.DirectionsHelper
import week11.st765512.finalproject.util.DirectionsHelper.TravelMode
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Google Maps component for displaying trip route with click-to-select functionality
 * 
 * @param startLocation Starting point coordinates (LatLng)
 * @param destination Destination coordinates (LatLng)
 * @param onLocationSelected Callback when user clicks on map to select location
 * @param routePoints Points for drawing route polyline
 * @param drivingRouteInfo Route information for driving mode
 * @param bicyclingRouteInfo Route information for bicycling mode
 * @param walkingRouteInfo Route information for walking mode
 * @param modifier Modifier for the map
 */
@Composable
fun GoogleMapView(
    startLocation: LatLng? = null,
    destination: LatLng? = null,
    onLocationSelected: ((LatLng) -> Unit)? = null, // LatLng - will determine start/destination based on current state
    routePoints: List<LatLng> = emptyList(),
    drivingRouteInfo: DirectionsHelper.RouteInfo? = null,
    bicyclingRouteInfo: DirectionsHelper.RouteInfo? = null,
    walkingRouteInfo: DirectionsHelper.RouteInfo? = null,
    onGetCurrentLocation: (() -> Unit)? = null, // Callback to get current location
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Initialize Maps SDK
    LaunchedEffect(Unit) {
        try {
            MapsInitializer.initialize(context)
        } catch (e: Exception) {
            Log.e("GoogleMaps", "Failed to initialize Maps SDK: ${e.message}", e)
        }
    }
    
    // Default location (can be changed to user's current location)
    val defaultLocation = LatLng(37.7749, -122.4194) // San Francisco
    
    // Determine camera position
    val cameraPositionState = rememberCameraPositionState {
        position = when {
            startLocation != null -> CameraPosition.fromLatLngZoom(startLocation, 12f)
            destination != null -> CameraPosition.fromLatLngZoom(destination, 12f)
            else -> CameraPosition.fromLatLngZoom(defaultLocation, 10f)
        }
    }

    // Track if we've already animated to avoid interfering with user dragging
    var hasAnimatedToBothPoints by remember { mutableStateOf(false) }
    
    // Update camera only once when both points are first set, then let user drag freely
    androidx.compose.runtime.LaunchedEffect(startLocation, destination) {
        when {
            startLocation != null && destination != null && !hasAnimatedToBothPoints -> {
                // Center between start and destination and zoom in (only once)
                val centerLat = (startLocation.latitude + destination.latitude) / 2
                val centerLng = (startLocation.longitude + destination.longitude) / 2
                val center = LatLng(centerLat, centerLng)
                
                // Calculate zoom level based on distance - less zoom for better overview
                val distance = calculateDistance(startLocation, destination)
                val zoomLevel = when {
                    distance < 5.0 -> 13f  // Very close
                    distance < 20.0 -> 11f  // Close
                    distance < 50.0 -> 10f  // Medium distance
                    distance < 100.0 -> 9f  // Medium-far distance
                    else -> 8f  // Far
                }
                
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.fromLatLngZoom(center, zoomLevel)
                    )
                )
                hasAnimatedToBothPoints = true
            }
            startLocation != null && destination == null && !hasAnimatedToBothPoints -> {
                // Only animate to start location if destination is not set yet
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.fromLatLngZoom(startLocation, 12f)
                    )
                )
            }
            destination != null && startLocation == null && !hasAnimatedToBothPoints -> {
                // Only animate to destination if start location is not set yet
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.fromLatLngZoom(destination, 12f)
                    )
                )
            }
        }
        
        // Reset flag if both points are cleared
        if (startLocation == null && destination == null) {
            hasAnimatedToBothPoints = false
        }
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            mapToolbarEnabled = false,
            scrollGesturesEnabled = true,  // Enable map dragging/panning
            zoomGesturesEnabled = true,    // Enable pinch to zoom
            rotationGesturesEnabled = true, // Enable rotation gestures
            tiltGesturesEnabled = true,    // Enable tilt gestures
            scrollGesturesEnabledDuringRotateOrZoom = true // Allow scrolling during zoom/rotate
        )
    }

    val mapProperties = remember {
        MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = false // Set to true if you want to show user location
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            properties = mapProperties,
            onMapClick = { latLng ->
                // When user clicks on map, pass the location to parent
                // Parent will determine if it's start or destination based on current state
                onLocationSelected?.invoke(latLng)
            }
        ) {
            // Draw route polyline if route points are available
            if (routePoints.isNotEmpty()) {
                Polyline(
                    points = routePoints,
                    color = Color(0xFF49C5C1) // TealPrimary color
                )
            }
            
            // Start location marker
            startLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Starting Point",
                    draggable = true
                )
            }

            // Destination marker
            destination?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Destination",
                    draggable = true
                )
            }
        }
        
        // Show route info window if routes are calculated - display at top right to avoid blocking route
        // Use absolute positioning instead of fillMaxSize Box to avoid blocking map interactions
        if (startLocation != null && destination != null && (drivingRouteInfo != null || bicyclingRouteInfo != null || walkingRouteInfo != null)) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .wrapContentSize()
            ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Driving route
                        drivingRouteInfo?.let { info ->
                            RouteInfoRow(
                                icon = Icons.Default.DirectionsCar,
                                durationMinutes = info.durationMinutes,
                                distanceKm = info.distanceKm
                            )
                        }
                        
                        // Bicycling route
                        bicyclingRouteInfo?.let { info ->
                            RouteInfoRow(
                                icon = Icons.Default.DirectionsBike,
                                durationMinutes = info.durationMinutes,
                                distanceKm = info.distanceKm
                            )
                        }
                        
                        // Walking route
                        walkingRouteInfo?.let { info ->
                            RouteInfoRow(
                                icon = Icons.Default.DirectionsWalk,
                                durationMinutes = info.durationMinutes,
                                distanceKm = info.distanceKm
                            )
                        }
                    }
                }
        }
        
        // Show "Get Current Location" button at bottom left (avoid interfering with zoom controls)
        onGetCurrentLocation?.let {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                FloatingActionButton(
                    onClick = it,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Get Current Location",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        
    }
}

/**
 * Placeholder map view when locations are not available
 */
@Composable
fun MapPlaceholder(
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "Enter starting point and destination to see map",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Calculate distance between two LatLng points in kilometers
 */
private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(
        point1.latitude,
        point1.longitude,
        point2.latitude,
        point2.longitude,
        results
    )
    return results[0] / 1000.0 // Convert meters to kilometers
}

/**
 * Route info row component showing icon and route details
 */
@Composable
private fun RouteInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    durationMinutes: Int,
    distanceKm: Double
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "${durationMinutes} min ${String.format("%.1f", distanceKm)} km",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

