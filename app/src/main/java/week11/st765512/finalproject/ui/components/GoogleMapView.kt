package week11.st765512.finalproject.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import week11.st765512.finalproject.util.DirectionsHelper
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
 * @param routeInfo Route information to display in info window
 * @param modifier Modifier for the map
 */
@Composable
fun GoogleMapView(
    startLocation: LatLng? = null,
    destination: LatLng? = null,
    onLocationSelected: ((LatLng) -> Unit)? = null, // LatLng - will determine start/destination based on current state
    routePoints: List<LatLng> = emptyList(),
    routeInfo: DirectionsHelper.RouteInfo? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Initialize Maps SDK
    LaunchedEffect(Unit) {
        try {
            MapsInitializer.initialize(context)
            Log.d("GoogleMaps", "Maps SDK initialized successfully")
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

    // Update camera when locations change
    androidx.compose.runtime.LaunchedEffect(startLocation, destination) {
        when {
            startLocation != null && destination != null -> {
                // Center between start and destination
                val centerLat = (startLocation.latitude + destination.latitude) / 2
                val centerLng = (startLocation.longitude + destination.longitude) / 2
                val center = LatLng(centerLat, centerLng)
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.fromLatLngZoom(center, 10f)
                    )
                )
            }
            startLocation != null -> {
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.fromLatLngZoom(startLocation, 12f)
                    )
                )
            }
            destination != null -> {
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.fromLatLngZoom(destination, 12f)
                    )
                )
            }
        }
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            compassEnabled = true,
            mapToolbarEnabled = false
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
        
        // Show route info window if route is calculated
        routeInfo?.let { info ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${info.durationMinutes} min ${String.format("%.1f", info.distanceKm)} km",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
        
        // Show hint if no coordinates
        if (startLocation == null && destination == null && routeInfo == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Text(
                        text = "Enter coordinates (lat,lng) to see markers",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurface
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

