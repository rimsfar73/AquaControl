package com.example.aquacontrol

import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.model.temperatura.FiltroHistorialTemperatura
import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.repository.temperatura.TemperaturaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class FakeTemperaturaRepository : TemperaturaRepository {
    val mediciones = MutableStateFlow<List<MedicionTemperatura>>(emptyList())
    val conteo = MutableStateFlow(0)
    val pagina = MutableStateFlow<List<MedicionTemperatura>>(emptyList())

    var onRegistrar: suspend (Int, Double, OrigenMedicion) -> MedicionTemperatura =
        { linea, temperatura, origen ->
            MedicionTemperatura(99L, linea, temperatura, 1_000L, origen)
        }
    var onObtener: suspend (Int, Long) -> MedicionTemperatura? = { _, _ -> null }
    var onActualizar: suspend (Int, Long, Double, PerfilUsuario) -> MedicionTemperatura =
        { linea, id, temperatura, _ ->
            MedicionTemperatura(id, linea, temperatura, 1_000L, OrigenMedicion.MANUAL)
        }
    var onEliminar: suspend (Int, Long, PerfilUsuario) -> Unit = { _, _, _ -> }
    var errorHistorial: Throwable? = null

    override suspend fun registrarMedicion(
        lineaId: Int,
        temperatura: Double,
        origen: OrigenMedicion
    ) = onRegistrar(lineaId, temperatura, origen)

    override fun observarHistorialPorLinea(lineaId: Int): Flow<List<MedicionTemperatura>> =
        mediciones.asStateFlow()

    override fun observarPaginaHistorial(
        lineaId: Int,
        filtro: FiltroHistorialTemperatura,
        limite: Int,
        desplazamiento: Int
    ): Flow<List<MedicionTemperatura>> {
        errorHistorial?.let { throw it }
        return pagina.asStateFlow()
    }

    override fun observarCantidadHistorial(
        lineaId: Int,
        filtro: FiltroHistorialTemperatura
    ): Flow<Int> {
        errorHistorial?.let { throw it }
        return conteo.asStateFlow()
    }

    override fun observarUltimaMedicionPorLinea(lineaId: Int): Flow<MedicionTemperatura?> =
        MutableStateFlow(mediciones.value.filter { it.lineaId == lineaId }.maxByOrNull { it.fechaHora })

    override fun observarUltimasMediciones(): Flow<List<MedicionTemperatura>> =
        mediciones.asStateFlow()

    override suspend fun obtenerMedicion(lineaId: Int, medicionId: Long) =
        onObtener(lineaId, medicionId)

    override suspend fun actualizarTemperatura(
        lineaId: Int,
        medicionId: Long,
        temperatura: Double,
        perfil: PerfilUsuario
    ) = onActualizar(lineaId, medicionId, temperatura, perfil)

    override suspend fun eliminarMedicion(
        lineaId: Int,
        medicionId: Long,
        perfil: PerfilUsuario
    ) = onEliminar(lineaId, medicionId, perfil)

    override fun observarMedicionesEnIntervalo(
        lineaId: Int,
        desdeInclusive: Long,
        hastaInclusive: Long
    ): Flow<List<MedicionTemperatura>> = mediciones.asStateFlow()
}
