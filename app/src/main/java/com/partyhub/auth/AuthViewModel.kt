package com.partyhub.auth

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona el estado de autenticación de la aplicación.
 */
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // Estado del usuario actual
    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> = _user

    // Estado de carga para mostrar un spinner en la UI
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Mensajes de error para mostrar en la UI (Snackbars/Toasts)
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        // Al iniciar, comprobamos si ya hay una sesión activa
        _user.value = repository.getCurrentUser()
    }

    /**
     * Intenta iniciar sesión con email y contraseña.
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.signIn(email, password)
            result.onSuccess {
                _user.value = it
            }.onFailure {
                _errorMessage.value = it.message ?: "Error desconocido al iniciar sesión"
            }
            
            _isLoading.value = false
        }
    }

    /**
     * Registra un nuevo usuario.
     */
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = repository.signUp(email, password)
            result.onSuccess {
                _user.value = it
            }.onFailure {
                _errorMessage.value = it.message ?: "Error desconocido al registrarse"
            }
            
            _isLoading.value = false
        }
    }

    /**
     * Cierra la sesión activa.
     */
    fun signOut() {
        repository.signOut()
        _user.value = null
    }

    /**
     * Limpia el mensaje de error después de mostrarlo.
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

/**
 * Factory para instanciar el AuthViewModel con su repositorio.
 */
class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
