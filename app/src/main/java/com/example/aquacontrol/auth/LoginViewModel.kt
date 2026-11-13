package com.example.aquacontrol.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.auth.dto.LoginResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val cargando: Boolean = false,
    val respuesta: LoginResponse? = null,
    val error: String? = null
)

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> =
        _uiState.asStateFlow()

    fun ingresar(
        username: String,
        password: String
    ) {
        if (_uiState.value.cargando) return

        if (username.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(
                error = "Ingresa usuario y contraseña."
            )
            return
        }

        viewModelScope.launch {

            _uiState.value = LoginUiState(
                cargando = true
            )

            val resultado = repository.ingresar(
                username = username,
                password = password
            )

            resultado.fold(
                onSuccess = { respuesta ->
                    _uiState.value = LoginUiState(
                        respuesta = respuesta
                    )
                },
                onFailure = { exception ->
                    _uiState.value = LoginUiState(
                        error = exception.message
                            ?: "No fue posible iniciar sesión."
                    )
                }
            )
        }
    }

    fun limpiarResultado() {
        _uiState.value = LoginUiState()
    }
}