package com.example.aquacontrol.viewmodel.temperatura

import com.example.aquacontrol.model.temperatura.MedicionTemperatura

data class RegistrarTemperaturaUiState(
    val lineaId: Int? = null,
    val temperatura: String = "",

    val errorLinea: String? = null,
    val errorTemperatura: String? = null,

    val guardando: Boolean = false,
    val errorGuardado: String? = null,
    val medicionGuardada: MedicionTemperatura? = null
)