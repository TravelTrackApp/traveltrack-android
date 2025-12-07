/**
 * File: TripViewModel.kt
 * 
 * ViewModel for managing trip-related UI state and business logic. Handles trip creation,
 * updates, deletion, and real-time trip data observation from Firestore.
 */
package week11.st765512.finalproject.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import week11.st765512.finalproject.data.model.Result
import week11.st765512.finalproject.data.model.Trip
import week11.st765512.finalproject.data.model.TripInput
import week11.st765512.finalproject.data.model.TripSummary
import week11.st765512.finalproject.data.repository.TripRepository
import week11.st765512.finalproject.util.TimeFormatter
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class TripUiState(
    val fullTrips: List<Trip> = emptyList(), // Firestore data display
    val trips: List<Trip> = emptyList(), // filtered results shown
    val filteredTrips: List<Trip> = emptyList(),
    val filterDateInput: String = "",
    val selectedTrip: Trip? = null,
    val summary: TripSummary = TripSummary(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Filter Search Query
    val searchQuery: String = "",
    val filterDate: Long? = null,
    val filterTag: String = "",
    val filterLocation: String = "",
    )

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class TripViewModel(
    private val repository: TripRepository = TripRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripUiState())
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null
    private var tripsJob: Job? = null

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun onAuthStateChanged(userId: String?) {
        if (currentUserId == userId) return
        currentUserId = userId
        tripsJob?.cancel()

        if (userId == null) {
            _uiState.value = TripUiState()
        } else {
            observeTrips()
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun observeTrips() {
        val userId = currentUserId ?: return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        tripsJob = viewModelScope.launch {
            repository.observeTrips(userId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val trips = result.data

                        val filtered = applyFilters(trips)

                        _uiState.update {
                            it.copy(
                                fullTrips = trips, // show unfiltered trips
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.exception.message ?: "Unable to load trips."
                            )
                        }
                    }

                    Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun refreshSelectedTrip(tripId: String) {
        val cached = _uiState.value.trips.find { it.id == tripId }
        if (cached != null) {
            _uiState.update { it.copy(selectedTrip = cached) }
            return
        }

        val userId = currentUserId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.getTrip(userId, tripId)) {
                is Result.Success -> _uiState.update {
                    it.copy(selectedTrip = result.data, isLoading = false)
                }

                is Result.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exception.message ?: "Unable to load trip."
                    )
                }

                Result.Loading -> Unit
            }
        }
    }

    fun clearSelectedTrip() {
        _uiState.update { it.copy(selectedTrip = null) }
    }

    fun saveTrip(input: TripInput) {
        val userId = currentUserId ?: run {
            _uiState.update { it.copy(errorMessage = "Please sign in to save trips.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, successMessage = null, errorMessage = null) }
            val trip = Trip(
                title = input.title,
                startLocation = input.startLocation,
                destinationLocation = input.destinationLocation,
                startLatLng = input.startLatLng,
                destinationLatLng = input.destinationLatLng,
                notes = input.notes,
                distanceKm = input.distanceKm,
                durationMinutes = input.durationMinutes,
                routeInfo = input.routeInfo,
                tags = input.tags,
                photoUrls = input.photoUrls,
                userId = userId
            )

            when (val result = repository.createTrip(userId, trip)) {
                is Result.Success -> _uiState.update {
                    it.copy(isSubmitting = false, successMessage = "Trip saved successfully!")
                }

                is Result.Error -> _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = result.exception.message ?: "Unable to save trip."
                    )
                }

                Result.Loading -> Unit
            }
        }
    }

    fun updateTrip(tripId: String, updates: Map<String, Any>, onComplete: (() -> Unit)? = null) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }

            when (val result = repository.updateTrip(userId, tripId, updates)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = "Trip updated successfully!"
                        )
                    }
                    onComplete?.invoke()
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = result.exception.message ?: "Failed to update trip."
                        )
                    }
                }

                Result.Loading -> Unit
            }
        }
    }

    fun deleteTrip(tripId: String, onComplete: (() -> Unit)? = null) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            when (val result = repository.deleteTrip(userId, tripId)) {
                is Result.Success -> {
                    _uiState.update {
                        val shouldClearSelection = it.selectedTrip?.id == tripId
                        it.copy(
                            successMessage = "Trip deleted successfully.",
                            selectedTrip = if (shouldClearSelection) null else it.selectedTrip
                        )
                    }
                    onComplete?.invoke()
                }

                is Result.Error -> _uiState.update {
                    it.copy(errorMessage = result.exception.message ?: "Unable to delete trip.")
                }

                Result.Loading -> Unit
            }
        }
    }

    // YYYY-MM-DD conversion
    fun updateFilterDateFromInput(input: String) {
        // Always update the raw text
        _uiState.update { it.copy(filterDateInput = input) }

        // Try parsing ONLY when valid full date entered
        if (input.length == 10) {  // yyyy-MM-dd = 10 chars
            try {
                val parsed = LocalDate.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val epoch = parsed.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                _uiState.update { it.copy(filterDate = epoch) }

            } catch (_: Exception) { /* ignore invalid */ }
        } else {
            _uiState.update { it.copy(filterDate = null) }
        }
    }

    // Filter Search Query Functions
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun updateFilterDate(date: Long?) {
        _uiState.update { it.copy(filterDate = date) }
    }

    fun updateFilterTag(tag: String) {
        _uiState.update { it.copy(filterTag = tag) }
    }

    fun updateFilterLocation(location: String) {
        _uiState.update { it.copy(filterLocation = location) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                filterDate = null,
                filterTag = "",
                filterLocation = ""
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) // required for isSameDay call
    private fun applyFilters(trips: List<Trip>): List<Trip> {
        val state = _uiState.value

        return trips.filter { trip ->
            val matchesSearch = state.searchQuery.isBlank() ||
                    trip.title.contains(state.searchQuery, ignoreCase = true)

            val matchesDate =
                state.filterDate == null ||
                        isSameDay(trip.createdAt, state.filterDate!!)

            val matchesTag = state.filterTag.isBlank() ||
                    trip.tags.any { it.contains(state.filterTag, ignoreCase = true) }

            val matchesLocation = state.filterLocation.isBlank() ||
                    trip.startLocation.contains(state.filterLocation, ignoreCase = true) ||
                    trip.destinationLocation.contains(state.filterLocation, ignoreCase = true)

            matchesSearch && matchesDate && matchesTag && matchesLocation
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE) // required for LocalDate.ofInstant
    private fun isSameDay(tripTimestamp: Long, filterTimestamp: Long): Boolean {
        val zone = ZoneId.systemDefault()

        val tripDay = LocalDate.ofInstant(java.time.Instant.ofEpochMilli(tripTimestamp), zone)
        val filterDay = LocalDate.ofInstant(java.time.Instant.ofEpochMilli(filterTimestamp), zone)

        return tripDay == filterDay
    }

    fun clearMessage() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    // Update list when typing in filter search

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                val currentTrips = state.fullTrips
                val filtered = applyFilters(currentTrips)

                _uiState.update {
                    it.copy(
                        trips = filtered,
                        summary = buildSummary(filtered)
                    )
                }
            }
        }
    }

    private fun buildSummary(trips: List<Trip>): TripSummary {
        if (trips.isEmpty()) return TripSummary()

        val totalDistance = trips.sumOf { it.distanceKm }
        val averageDurationHours = trips.map { it.durationMinutes }.average() / 60.0

        return TripSummary(
            totalTrips = trips.size,
            totalDistanceKm = totalDistance,
            averageDurationHours = averageDurationHours
        )
    }
}

