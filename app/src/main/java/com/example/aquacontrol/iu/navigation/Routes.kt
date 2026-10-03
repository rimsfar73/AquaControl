package com.example.aquacontrol.iu.navigation

object Routes {

    // --- Nivel 0: Roles ---
    const val ROLE_SELECTION = "roleSelection"
    const val OPERARIO_HOME = "operarioHome"
    const val SUPERVISOR_HOME = "supervisorHome"

    // --- Nivel 1: Granjas ---
    const val GRANJAS = "granjas"

    // --- Nivel 2: Galpones ---
    const val GALPONES = "galpones"
    const val GALPONES_PARAM = "galpones/{granjaId}"

    // --- Nivel 3: Líneas ---
    const val LINEAS = "lineas"
    const val LINEAS_PARAM = "lineas/{galponId}"

    // --- Nivel 4: Detalle de Línea ---
    const val DETALLE_LINEA = "detalleLinea"
    const val DETALLE_LINEA_PARAM = "detalleLinea/{galponId}/{lineaId}"

    // --- Nivel 5: Alertas ---
    const val ALERTAS = "alertas"

    // --- Nivel 6: Flushing ---
    // Flushing general (Operario)
    const val FLUSHING = "flushing"

    // Flushing por línea (Supervisor)
    const val FLUSHING_PARAM = "flushing/{lineaId}"
    const val REGISTRAR_FLUSHING = "registrarFlushing"

}
