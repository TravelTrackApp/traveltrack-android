/**
 * File: GeocodingHelper.kt
 * 
 * Helper class for geocoding (converting addresses to coordinates).
 * Converts address strings to LatLng coordinates for map display.
 */
package week11.st765512.finalproject.util

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper class for geocoding (converting addresses to coordinates)
 * Note: For production, use Google Geocoding API or Android Geocoder
 */
object GeocodingHelper {
    
    /**
     * Convert address string to LatLng coordinates
     * This is a placeholder - in production, use:
     * 1. Android Geocoder (free but limited)
     * 2. Google Geocoding API (requires API key)
     * 
     * For now, returns null - you'll need to implement actual geocoding
     */
    suspend fun addressToLatLng(
        address: String,
        context: Context
    ): LatLng? = withContext(Dispatchers.IO) {
        if (address.isBlank()) return@withContext null
        
        // Note: Geocoding is not implemented - returns null
        // To implement, use Android Geocoder or Google Geocoding API
        null
    }
    
    /**
     * Check if a string might be coordinates (for manual input)
     */
    fun isCoordinateString(input: String): Boolean {
        val parts = input.split(",")
        if (parts.size != 2) return false
        
        return try {
            parts[0].trim().toDouble()
            parts[1].trim().toDouble()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * Parse coordinate string "lat,lng" to LatLng
     */
    fun parseCoordinates(input: String): LatLng? {
        if (!isCoordinateString(input)) return null
        
        val parts = input.split(",")
        return try {
            LatLng(parts[0].trim().toDouble(), parts[1].trim().toDouble())
        } catch (e: Exception) {
            null
        }
    }
}
