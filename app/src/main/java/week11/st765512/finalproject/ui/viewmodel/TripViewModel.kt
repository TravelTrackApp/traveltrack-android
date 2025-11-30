package week11.st765512.finalproject.ui.viewmodel

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

data class TripUiState(
    val trips: List<Trip> = emptyList(),
    val selectedTrip: Trip? = null,
    val summary: TripSummary = TripSummary(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class TripViewModel(
    private val repository: TripRepository = TripRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripUiState())
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null
    private var tripsJob: Job? = null

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

    private fun observeTrips() {
        val userId = currentUserId ?: return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        tripsJob = viewModelScope.launch {
            repository.observeTrips(userId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val trips = result.data
                        _uiState.update {
                            it.copy(
                                trips = trips,
                                summary = buildSummary(trips),
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

    fun clearMessage() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
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

