package com.example.aquacontrol.model.estado

enum class EstadoLinea {
    NORMAL,
    ADVERTENCIA,
    CRITICO;

    companion object {
        fun desdeTemperatura(temperatura: Double): EstadoLinea {
            require(temperatura.isFinite()) {
                "La temperatura debe ser un número válido"
            }

            return when {
                temperatura < 5.0 || temperatura >= 30.0 -> CRITICO
                temperatura in 18.0..21.0 -> NORMAL
                else -> ADVERTENCIA
            }
        }
    }
}
