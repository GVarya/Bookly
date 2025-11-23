package avito.testtask.bookly.viewmodels


import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import avito.testtask.domain.models.OperationResult
import avito.testtask.domain.models.User
import avito.testtask.domain.usecases.user.GetUserUseCase
import avito.testtask.domain.usecases.user.UpdateAvatarImageUsecase
import avito.testtask.domain.usecases.user.UpdateUserUsecase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUsecase: UpdateUserUsecase,
    private val updateAvatarImageUsecase: UpdateAvatarImageUsecase
) : ViewModel() {

    private val _userState = MutableStateFlow<OperationResult<User>?>(null)
    val userState: StateFlow<OperationResult<User>?> = _userState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _userState.value = getUserUseCase.execute()
            _isLoading.value = false
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = updateUserUsecase.execute(user)) {
                is OperationResult.Success -> {
                    _userState.value = OperationResult.Success(result.data)
                    _isEditing.value = false
                }
                is OperationResult.Error -> {
                    _userState.value = OperationResult.Error(result.message)
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun updateAvatar(imageUri: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = updateAvatarImageUsecase.execute(imageUri.toString())) {
                is OperationResult.Success -> {
                    _selectedImageUri.value = null
                    loadUser()
                }
                is OperationResult.Error -> {
                    _userState.value = OperationResult.Error(result.message)
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun setEditing(editing: Boolean) {
        _isEditing.value = editing
        if (!editing) {
            _selectedImageUri.value = null
        }
    }

    fun clearState() {
        _userState.value = null
        _selectedImageUri.value = null
    }
}