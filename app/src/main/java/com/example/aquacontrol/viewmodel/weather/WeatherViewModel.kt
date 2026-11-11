package com.example.aquacontrol.viewmodel.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.data.weather.WeatherRepository
import com.example.aquacontrol.data.weather.remote.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    data object Loading : WeatherUiState

    data class Success(
        val clima: WeatherResponse
    ) : WeatherUiState

    data class Error(
        val mensaje: String
    ) : WeatherUiState
}

class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)

    val uiState: StateFlow<WeatherUiState> = _uiState

    init {
        cargarClima()
    }

    fun cargarClima() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            try {
                val respuesta = repository.obtenerClima(
                    latitude = -33.7,
                    longitude = -71.2
                )

                _uiState.value = WeatherUiState.Success(respuesta)

            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(
                    "No fue posible consultar el servicio meteorológico."
                )
            }
        }
    }
}