/**
 * File: DirectionsHelper.kt
 * 
 * Helper class for Google Directions API. Calculates route, distance, and duration
 * between two points for different travel modes (driving, bicycling, walking).
 */
package week11.st765512.finalproject.util

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import week11.st765512.finalproject.data.model.Result

/**
 * Helper class for Google Directions API
 * Calculates route, distance, and duration between two points
 */
object DirectionsHelper {
    private const val TAG = "DirectionsHelper"
    
    enum class TravelMode {
        DRIVING,
        WALKING,
        TRANSIT,
        BICYCLING
    }
    
    data class RouteInfo(
        val distanceKm: Double,
        val durationMinutes: Int,
        val polylinePoints: List<LatLng>,
        val travelMode: TravelMode = TravelMode.DRIVING
    )
    
    /**
     * Get route information between two points using Google Directions API
     * Note: Requires Directions API to be enabled in Google Cloud Console
     * 
     * @param travelMode Default is DRIVING. Can be WALKING, TRANSIT, or BICYCLING
     */
    suspend fun getRoute(
        origin: LatLng,
        destination: LatLng,
        apiKey: String,
        context: Context,
        travelMode: TravelMode = TravelMode.DRIVING
    ): Result<RouteInfo> = withContext(Dispatchers.IO) {
        return@withContext try {
            val mode = travelMode.name.lowercase()
            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${origin.latitude},${origin.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&mode=$mode" +
                    "&key=$apiKey"
            
            val response = URL(url).readText()
            val json = JSONObject(response)
            
            if (json.getString("status") != "OK") {
                val errorMsg = json.optString("error_message", "Unknown error")
                Log.e(TAG, "Directions API error: $errorMsg")
                return@withContext Result.Error(Exception("Directions API error: $errorMsg"))
            }
            
            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) {
                return@withContext Result.Error(Exception("No routes found"))
            }
            
            val route = routes.getJSONObject(0)
            val legs = route.getJSONArray("legs")
            val leg = legs.getJSONObject(0)
            
            // Extract distance (in meters)
            val distance = leg.getJSONObject("distance").getDouble("value") / 1000.0 // Convert to km
            
            // Extract duration (in seconds)
            val duration = leg.getJSONObject("duration").getInt("value") / 60 // Convert to minutes
            
            // Extract polyline points for drawing route
            val overviewPolyline = route.getJSONObject("overview_polyline")
            val encodedPoints = overviewPolyline.getString("points")
            val polylinePoints = decodePolyline(encodedPoints)
            
            Result.Success(RouteInfo(
                distanceKm = distance, 
                durationMinutes = duration, 
                polylinePoints = polylinePoints,
                travelMode = travelMode
            ))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting route: ${e.message}", e)
            Result.Error(e)
        }
    }
    
    /**
     * Decode polyline string to list of LatLng points
     */
    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            
            poly.add(LatLng(lat / 1e5, lng / 1e5))
        }
        
        return poly
    }
}

