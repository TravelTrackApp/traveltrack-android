/**
 * File: MapDebugHelper.kt
 * 
 * Helper class for logging map-related debug information and errors.
 * Provides centralized logging for Google Maps operations.
 */
package week11.st765512.finalproject.util

import android.util.Log

object MapDebugHelper {
    private const val TAG = "GoogleMapsDebug"
    
    fun logMapError(error: String) {
        Log.e(TAG, "Map Error: $error")
    }
    
    fun logMapInfo(message: String) {
        Log.d(TAG, "Map Info: $message")
    }
}




