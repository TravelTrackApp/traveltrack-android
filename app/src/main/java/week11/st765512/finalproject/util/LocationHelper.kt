package week11.st765512.finalproject.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

object LocationHelper {
    private const val TAG = "LocationHelper"
    
    /**
     * Get current location using FusedLocationProviderClient
     * Requires ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission
     */
    suspend fun getCurrentLocation(context: Context): Location? {
        return try {
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)

            // Check if permission is granted
            val hasFineLocation = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            
            val hasCoarseLocation = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasFineLocation && !hasCoarseLocation) {
                Log.w(TAG, "Location permission not granted")
                return null
            }

            Log.d(TAG, "Requesting current location...")
            
            // Get current location with timeout
            val cancellationTokenSource = CancellationTokenSource()
            val locationResult = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()

            if (locationResult != null) {
                Log.d(TAG, "Location obtained: ${locationResult.latitude}, ${locationResult.longitude}")
            } else {
                Log.w(TAG, "Location result is null - location may not be available")
            }

            locationResult
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location: ${e.message}", e)
            null
        }
    }
}

