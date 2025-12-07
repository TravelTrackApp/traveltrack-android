/**
 * File: ApiKeyHelper.kt
 * 
 * Helper class for retrieving Google Maps API key from AndroidManifest.xml.
 * Provides secure access to API keys stored in app metadata.
 */
package week11.st765512.finalproject.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

object ApiKeyHelper {
    /**
     * Get Google Maps API Key from AndroidManifest.xml
     */
    fun getMapsApiKey(context: Context): String? {
        return try {
            val ai: ApplicationInfo = context.packageManager
                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val bundle = ai.metaData
            bundle?.getString("com.google.android.geo.API_KEY")
        } catch (e: Exception) {
            null
        }
    }
}



