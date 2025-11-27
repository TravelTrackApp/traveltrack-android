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

