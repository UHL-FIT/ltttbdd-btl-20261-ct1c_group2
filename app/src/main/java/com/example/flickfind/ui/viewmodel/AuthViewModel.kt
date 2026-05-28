package com.example.flickfind.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flickfind.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(repository.currentUser)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess = _registrationSuccess.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess = _updateSuccess.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.login(email, password)
                .onSuccess {
                    _user.value = it
                }
                .onFailure {
                    _error.value = it.message ?: "Đăng nhập thất bại"
                }
            _isLoading.value = false
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.register(email, password)
                .onSuccess {
                    // Firebase tự động đăng nhập sau khi đăng ký, 
                    // nhưng chúng ta muốn người dùng tự đăng nhập lại để xác nhận.
                    repository.logout() 
                    _registrationSuccess.value = true
                }
                .onFailure {
                    _error.value = it.message ?: "Đăng ký thất bại"
                }
            _isLoading.value = false
        }
    }

    fun updateProfile(displayName: String?, photoUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.updateProfile(displayName, photoUri)
                .onSuccess {
                    _user.value = repository.currentUser
                    _updateSuccess.value = true
                }
                .onFailure {
                    _error.value = it.message ?: "Cập nhật thông tin thất bại"
                }
            _isLoading.value = false
        }
    }

    fun resetUpdateSuccess() {
        _updateSuccess.value = false
    }

    fun resetRegistrationSuccess() {
        _registrationSuccess.value = false
    }

    fun logout() {
        repository.logout()
        _user.value = null
    }

    fun clearError() {
        _error.value = null
    }
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
