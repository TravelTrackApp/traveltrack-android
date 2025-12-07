/**
 * File: Trip.kt
 * 
 * Data models for trip-related entities including Trip, RouteInfo, TripSummary, and TripInput.
 * These models represent trip information stored in Firestore and used throughout the app.
 */
package week11.st765512.finalproject.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class RouteInfo(
    val travelMode: String = "", // "DRIVING", "BICYCLING", "WALKING"
    val distanceKm: Double = 0.0,
    val durationMinutes: Int = 0
)

@IgnoreExtraProperties
data class Trip(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val startLocation: String = "",
    val destinationLocation: String = "",
    val startLatLng: String = "", // Format: "lat,lng" for map display
    val destinationLatLng: String = "", // Format: "lat,lng" for map display
    val notes: String = "",
    val distanceKm: Double = 0.0, // Primary distance (driving)
    val durationMinutes: Int = 0, // Primary duration (driving)
    val routeInfo: List<RouteInfo> = emptyList(), // Route info for all travel modes
    val tags: List<String> = emptyList(),
    val photoUrls: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class TripSummary(
    val totalTrips: Int = 0,
    val totalDistanceKm: Double = 0.0,
    val averageDurationHours: Double = 0.0
)

data class TripInput(
    val title: String,
    val startLocation: String,
    val destinationLocation: String,
    val startLatLng: String = "", // Format: "lat,lng"
    val destinationLatLng: String = "", // Format: "lat,lng"
    val notes: String,
    val distanceKm: Double,
    val durationMinutes: Int,
    val routeInfo: List<RouteInfo> = emptyList(),
    val tags: List<String> = emptyList(),
    val photoUrls: List<String> = emptyList()
)

