package week11.st765512.finalproject.data.model

data class Trip(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val startLocation: String = "",
    val destinationLocation: String = "",
    val notes: String = "",
    val distanceKm: Double = 0.0,
    val durationMinutes: Int = 0,
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
    val notes: String,
    val distanceKm: Double,
    val durationMinutes: Int,
    val tags: List<String> = emptyList()
)

