package week11.st765512.finalproject.util

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Data class representing a place suggestion from autocomplete
 */
data class PlaceSuggestion(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
    val fullText: String
)

/**
 * Data class representing a place with coordinates
 */
data class PlaceDetails(
    val placeId: String,
    val name: String,
    val address: String,
    val latLng: LatLng
)

/**
 * Helper class for Google Places Autocomplete functionality
 */
object PlacesAutocompleteHelper {
    
    private const val TAG = "PlacesAutocomplete"
    private var placesClient: PlacesClient? = null
    
    /**
     * Initialize the Places SDK. Should be called once when the app starts.
     */
    fun initialize(context: Context) {
        if (!Places.isInitialized()) {
            val apiKey = ApiKeyHelper.getMapsApiKey(context)
            if (apiKey != null) {
                Places.initialize(context.applicationContext, apiKey)
                Log.d(TAG, "Places SDK initialized successfully")
            } else {
                Log.e(TAG, "Failed to get API key for Places SDK")
            }
        }
        placesClient = Places.createClient(context)
    }
    
    /**
     * Get autocomplete predictions for a query string
     */
    suspend fun getAutocompletePredictions(
        query: String,
        context: Context
    ): List<PlaceSuggestion> = suspendCancellableCoroutine { continuation ->
        if (query.isBlank()) {
            continuation.resume(emptyList())
            return@suspendCancellableCoroutine
        }
        
        // Ensure Places is initialized
        if (placesClient == null) {
            initialize(context)
        }
        
        val client = placesClient
        if (client == null) {
            Log.e(TAG, "Places client not initialized")
            continuation.resume(emptyList())
            return@suspendCancellableCoroutine
        }
        
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()
        
        client.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val suggestions = response.autocompletePredictions.map { prediction ->
                    PlaceSuggestion(
                        placeId = prediction.placeId,
                        primaryText = prediction.getPrimaryText(null).toString(),
                        secondaryText = prediction.getSecondaryText(null).toString(),
                        fullText = prediction.getFullText(null).toString()
                    )
                }
                Log.d(TAG, "Found ${suggestions.size} predictions for query: $query")
                continuation.resume(suggestions)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting predictions: ${exception.message}")
                continuation.resume(emptyList())
            }
    }
    
    /**
     * Get place details including coordinates for a place ID
     */
    suspend fun getPlaceDetails(
        placeId: String,
        context: Context
    ): PlaceDetails? = suspendCancellableCoroutine { continuation ->
        // Ensure Places is initialized
        if (placesClient == null) {
            initialize(context)
        }
        
        val client = placesClient
        if (client == null) {
            Log.e(TAG, "Places client not initialized")
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )
        
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()
        
        client.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                val latLng = place.latLng
                
                if (latLng != null) {
                    val details = PlaceDetails(
                        placeId = placeId,
                        name = place.name ?: "",
                        address = place.address ?: "",
                        latLng = latLng
                    )
                    Log.d(TAG, "Got place details: ${details.address} at ${latLng.latitude}, ${latLng.longitude}")
                    continuation.resume(details)
                } else {
                    Log.e(TAG, "Place has no coordinates")
                    continuation.resume(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching place details: ${exception.message}")
                continuation.resume(null)
            }
    }
}

