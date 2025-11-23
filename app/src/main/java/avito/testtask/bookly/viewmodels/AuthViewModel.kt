package avito.testtask.bookly.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import avito.testtask.domain.models.AuthState
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.User
import avito.testtask.domain.usecases.auth.GetCurrentUserUseCase
import avito.testtask.domain.usecases.auth.LoginUsecase
import avito.testtask.domain.usecases.auth.LogOutUsecase
import avito.testtask.domain.usecases.auth.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUsecase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogOutUsecase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkCurrentUser()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading
            when (val result = loginUseCase(email, password)) {
                is OperationResult.Success -> {
                    _currentUser.value = result.data
                    _authState.value = AuthState.Authenticated(result.data)
                }
                is OperationResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading
            when (val result = registerUseCase.execute(name, email, password)) {
                is OperationResult.Success -> {
                    _currentUser.value = result.data
                    _authState.value = AuthState.Authenticated(result.data)
                }
                is OperationResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            when (val result = logoutUseCase.execute()) {
                is OperationResult.Success -> {
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated
                }
                is OperationResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = getCurrentUserUseCase.execute()) {
                is OperationResult.Success -> {
                    result.data?.let { user ->
                        _currentUser.value = user
                        _authState.value = AuthState.Authenticated(user)
                    } ?: run {
                        _authState.value = AuthState.Unauthenticated
                    }
                }
                is OperationResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}