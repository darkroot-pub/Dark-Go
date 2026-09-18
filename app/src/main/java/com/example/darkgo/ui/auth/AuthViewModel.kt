package com.example.darkgo.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.darkgo.data.models.UserProfile
import com.example.darkgo.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: FirebaseUser? = null,
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(currentUser = authRepository.currentUser))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authStateFlow().collect { user ->
                _uiState.value = _uiState.value.copy(currentUser = user)
                if (user != null) {
                    loadUserProfile(user.uid)
                } else {
                    _uiState.value = _uiState.value.copy(userProfile = null)
                }
            }
        }
    }

    private fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            val result = authRepository.fetchUserProfile(uid)
            result.onSuccess { profile ->
                _uiState.value = _uiState.value.copy(userProfile = profile)
            }
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter both email and password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.loginWithEmail(email, pass)
            result.onSuccess { user ->
                _uiState.value = _uiState.value.copy(isLoading = false, currentUser = user)
                loadUserProfile(user.uid)
                onSuccess()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message ?: "Authentication failed")
            }
        }
    }

    fun register(email: String, pass: String, displayName: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank() || displayName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "All fields are required")
            return
        }
        if (pass.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.registerWithEmail(email, pass, displayName)
            result.onSuccess { profile ->
                _uiState.value = _uiState.value.copy(isLoading = false, userProfile = profile)
                onSuccess()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message ?: "Registration failed")
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your email to reset password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.sendPasswordReset(email)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Password reset email sent!")
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message ?: "Failed to send reset email")
            }
        }
    }

    fun updateProfile(displayName: String, bio: String, avatarId: String, onSuccess: () -> Unit) {
        if (displayName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Display name cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = authRepository.updateUserProfile(displayName, bio, avatarId)
            result.onSuccess { updated ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userProfile = updated,
                    successMessage = "Profile updated successfully!"
                )
                onSuccess()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to update profile"
                )
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        authRepository.signOut()
        _uiState.value = AuthUiState()
        onSignedOut()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
