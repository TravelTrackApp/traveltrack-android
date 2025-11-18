package week11.st765512.finalproject.data.model

/**
 * A generic class that holds a value or an exception
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

