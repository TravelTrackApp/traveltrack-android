package week11.st765512.finalproject.ui.common

/**
 * Generic UI State wrapper for consistent error and loading state handling
 * This provides a standard way to represent UI states across the app
 */
sealed class UiState<out T> {
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    object Idle : UiState<Nothing>()

    val isLoading: Boolean
        get() = this is Loading

    val isError: Boolean
        get() = this is Error

    val isSuccess: Boolean
        get() = this is Success

    val errorMessage: String?
        get() = when (this) {
            is Error -> message
            else -> null
        }
}

