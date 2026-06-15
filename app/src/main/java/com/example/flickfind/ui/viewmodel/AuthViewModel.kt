package com.example.flickfind.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flickfind.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthException
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

    private fun translateError(exception: Throwable): String {
        val message = exception.message ?: ""
        val msg = message.lowercase()
        
        // 1. Kiểm tra mã lỗi Firebase trước (Chính xác nhất)
        if (exception is FirebaseAuthException) {
            val code = exception.errorCode
            return when (code) {
                "ERROR_INVALID_EMAIL", "invalid-email" -> "Địa chỉ email không hợp lệ."
                "ERROR_WRONG_PASSWORD", "wrong-password" -> "Mật khẩu không chính xác."
                "ERROR_USER_NOT_FOUND", "user-not-found" -> "Tài khoản không tồn tại."
                "ERROR_USER_DISABLED", "user-disabled" -> "Tài khoản này đã bị vô hiệu hóa."
                "ERROR_TOO_MANY_REQUESTS", "too-many-requests" -> "Quá nhiều yêu cầu. Vui lòng thử lại sau ít phút."
                "ERROR_EMAIL_ALREADY_IN_USE", "email-already-in-use" -> "Email này đã được đăng ký bởi một tài khoản khác."
                "ERROR_WEAK_PASSWORD", "weak-password" -> "Mật khẩu quá yếu (tối thiểu 6 ký tự)."
                "ERROR_NETWORK_REQUEST_FAILED", "network-request-failed" -> "Lỗi kết nối mạng. Vui lòng kiểm tra lại."
                "ERROR_USER_MISMATCH" -> "Thông tin người dùng không khớp."
                "ERROR_REQUIRES_RECENT_LOGIN", "requires-recent-login" -> "Vui lòng đăng nhập lại để thực hiện thao tác này."
                "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "Tài khoản đã tồn tại với phương thức đăng nhập khác."
                "ERROR_INVALID_USER_TOKEN" -> "Phiên làm việc hết hạn. Vui lòng đăng nhập lại."
                "ERROR_NULL_USER" -> "Không tìm thấy thông tin người dùng."
                "channel-error" -> "Vui lòng nhập đầy đủ thông tin."
                else -> "Lỗi hệ thống (${code}). Vui lòng thử lại."
            }
        }

        // 2. Kiểm tra dựa trên nội dung text (Dự phòng cho các lỗi khác hoặc khi mã lỗi không khớp)
        return when {
            msg.contains("invalid-email") || msg.contains("invalid email") || msg.contains("badly formatted") -> "Địa chỉ email không hợp lệ."
            msg.contains("user-not-found") || msg.contains("no user record") || msg.contains("identifier") -> "Tài khoản không tồn tại."
            msg.contains("wrong-password") || msg.contains("invalid password") || msg.contains("incorrect") -> "Mật khẩu không chính xác."
            msg.contains("email-already-in-use") || msg.contains("already in use") || msg.contains("already exists") -> "Email này đã được đăng ký bởi một tài khoản khác."
            msg.contains("network-request-failed") || msg.contains("network error") || msg.contains("timeout") -> "Lỗi kết nối mạng. Vui lòng thử lại."
            msg.contains("too-many-requests") || msg.contains("too many requests") -> "Quá nhiều yêu cầu. Vui lòng thử lại sau ít phút."
            msg.contains("user-disabled") || msg.contains("has been disabled") -> "Tài khoản này đã bị vô hiệu hóa."
            msg.contains("weak-password") || msg.contains("weak password") -> "Mật khẩu quá yếu (tối thiểu 6 ký tự)."
            msg.contains("credential") && msg.contains("invalid") -> "Thông tin xác thực không hợp lệ."
            msg.contains("empty") || msg.contains("null") -> "Vui lòng điền đầy đủ thông tin."
            else -> "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin."
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.login(email, password)
                .onSuccess {
                    _user.value = it
                }
                .onFailure {
                    _error.value = translateError(it)
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
                    _registrationSuccess.value = true
                }
                .onFailure {
                    _error.value = translateError(it)
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
                    _error.value = translateError(it)
                }
            _isLoading.value = false
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.sendPasswordResetEmail(email)
                .onSuccess {
                    _error.value = "Liên kết đặt lại mật khẩu đã được gửi đến email của bạn."
                }
                .onFailure {
                    _error.value = translateError(it)
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
