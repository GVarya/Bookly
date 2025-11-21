package avito.testtask.domain.models

sealed class OperationResult<out T> {
    object Loading : OperationResult<Nothing>()
    data class Success<T>(val data: T) : OperationResult<T>()
    data class Error(val message: String) : OperationResult<Nothing>()
}