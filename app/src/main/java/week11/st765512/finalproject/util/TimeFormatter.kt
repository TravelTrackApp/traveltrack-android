/**
 * File: TimeFormatter.kt
 * 
 * Helper class for formatting timestamps to readable date strings.
 * Converts Long timestamps to formatted date/time strings for display.
 */
package week11.st765512.finalproject.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatter {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
}

