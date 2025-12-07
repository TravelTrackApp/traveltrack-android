/**
 * File: ReverseGeocodingHelper.kt
 * 
 * Helper class for reverse geocoding (converting coordinates to addresses).
 * Converts LatLng coordinates to readable address strings using Android Geocoder.
 */
package week11.st765512.finalproject.util

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Helper class for reverse geocoding (converting coordinates to addresses)
 */
object ReverseGeocodingHelper {
    private const val TAG = "ReverseGeocodingHelper"
    
    /**
     * Convert LatLng coordinates to address string
     * Uses Android Geocoder (free, but may have rate limits)
     */
    suspend fun getAddressFromLatLng(
        location: LatLng,
        context: Context
    ): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!Geocoder.isPresent()) {
                Log.w(TAG, "Geocoder is not available")
                return@withContext null
            }
            
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                // Format: Street, City, Country
                val addressParts = mutableListOf<String>()
                address.getAddressLine(0)?.let { addressParts.add(it) }
                address.locality?.let { addressParts.add(it) }
                address.countryName?.let { addressParts.add(it) }
                
                val formattedAddress = addressParts.joinToString(", ")
                formattedAddress
            } else {
                Log.w(TAG, "No address found for coordinates")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in reverse geocoding: ${e.message}", e)
            null
        }
    }
    
    /**
     * Get a short address (just street and city)
     */
    suspend fun getShortAddress(
        location: LatLng,
        context: Context
    ): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!Geocoder.isPresent()) {
                return@withContext null
            }
            
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val parts = mutableListOf<String>()
                address.thoroughfare?.let { parts.add(it) } // Street name
                address.subThoroughfare?.let { parts.add(it) } // Street number
                address.locality?.let { parts.add(it) } // City
                
                if (parts.isEmpty()) {
                    address.getAddressLine(0)
                } else {
                    parts.joinToString(", ")
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting short address: ${e.message}", e)
            null
        }
    }
}
