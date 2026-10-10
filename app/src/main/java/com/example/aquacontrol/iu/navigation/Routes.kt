package com.example.aquacontrol.iu.navigation

object Routes {

    // Roles
    const val ROLE_SELECTION = "roleSelection"
    const val OPERARIO_HOME = "operarioHome"
    const val SUPERVISOR_HOME = "supervisorHome"

    // Granjas
    const val GRANJAS = "granjas"

    // Galpones
    const val GALPONES = "galpones"
    const val GALPONES_PARAM = "galpones/{granjaId}"

    // Líneas
    const val LINEAS = "lineas"
    const val LINEAS_PARAM = "lineas/{galponId}"

    // Detalle de línea
    const val DETALLE_LINEA = "detalleLinea"
    const val DETALLE_LINEA_PARAM = "detalleLinea/{lineaId}"

    // Alertas
    const val ALERTAS = "alertas"

    // Flushing
    const val FLUSHING = "flushing"
    const val FLUSHING_PARAM = "flushing/{lineaId}"
    const val REGISTRAR_FLUSHING = "registrarFlushing"

    // Simulación de temperaturas
    const val SIMULACION = "simulacion"
}