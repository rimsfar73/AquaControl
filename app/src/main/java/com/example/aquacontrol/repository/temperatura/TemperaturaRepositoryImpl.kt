package com.example.aquacontrol.repository.temperatura

import com.example.aquacontrol.data.temperatura.local.MedicionTemperaturaEntity
import com.example.aquacontrol.data.temperatura.local.TemperaturaLocalDataSource
import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TemperaturaRepositoryImpl(
    private val localDataSource: TemperaturaLocalDataSource,
    private val bebederoRepository: BebederoRepository = BebederoRepository()
) : TemperaturaRepository {

    override suspend fun registrarMedicion(
        lineaId: Int,
        temperatura: Double,
        origen: OrigenMedicion
    ): MedicionTemperatura {
        validarLinea(lineaId)

        require(temperatura.isFinite()) {
            "La temperatura debe ser un número válido."
        }

        val entity = MedicionTemperaturaEntity(
            lineaId = lineaId,
            temperatura = temperatura,
            fechaHora = System.currentTimeMillis(),
            origen = origen.name
        )

        val idGenerado = localDataSource.guardarMedicion(entity)

        return entity.copy(
            id = idGenerado
        ).toModelo()
    }

    override fun observarHistorialPorLinea(
        lineaId: Int
    ): Flow<List<MedicionTemperatura>> {
        validarLinea(lineaId)

        return localDataSource.observarHistorialPorLinea(lineaId)
            .map { mediciones ->
                mediciones.map { it.toModelo() }
            }
    }

    override fun observarUltimaMedicionPorLinea(
        lineaId: Int
    ): Flow<MedicionTemperatura?> {
        validarLinea(lineaId)

        return localDataSource.observarUltimaMedicionPorLinea(lineaId)
            .map { medicion ->
                medicion?.toModelo()
            }
    }

    override fun observarUltimasMediciones():
            Flow<List<MedicionTemperatura>> {
        return localDataSource.observarUltimasMediciones()
            .map { mediciones ->
                mediciones.map { it.toModelo() }
            }
    }

    private fun validarLinea(lineaId: Int) {
        require(lineaId > 0) {
            "Debes seleccionar una línea válida."
        }

        val existe = bebederoRepository.obtenerGranjas().any { granja ->
            bebederoRepository.obtenerGalpones(granja.id).any { galpon ->
                bebederoRepository.obtenerLineas(galpon.id).any { linea ->
                    linea.id == lineaId
                }
            }
        }

        require(existe) {
            "La línea seleccionada no existe."
        }
    }

    private fun MedicionTemperaturaEntity.toModelo():
            MedicionTemperatura {
        return MedicionTemperatura(
            id = id,
            lineaId = lineaId,
            temperatura = temperatura,
            fechaHora = fechaHora,
            origen = OrigenMedicion.valueOf(origen)
        )
    }
}