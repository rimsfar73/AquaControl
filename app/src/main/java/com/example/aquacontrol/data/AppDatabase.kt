package com.example.aquacontrol.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aquacontrol.data.flushing.local.FlushingDao
import com.example.aquacontrol.data.flushing.local.FlushingEntity
import com.example.aquacontrol.data.flushing.local.FlushingRemotoDao
import com.example.aquacontrol.data.flushing.local.FlushingRemotoEntity
import com.example.aquacontrol.data.temperatura.local.MedicionTemperaturaDao
import com.example.aquacontrol.data.temperatura.local.MedicionTemperaturaEntity

// Conservamos las tablas anteriores y agregamos una copia independiente
// de los registros de flushing descargados del servidor.
@Database(
    entities = [
        FlushingEntity::class,
        MedicionTemperaturaEntity::class,
        FlushingRemotoEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Accedemos a los registros locales originales.
    abstract fun flushingDao(): FlushingDao

    abstract fun medicionTemperaturaDao(): MedicionTemperaturaDao

    // Accedemos a la copia de flushing remoto para consulta offline.
    abstract fun flushingRemotoDao(): FlushingRemotoDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Conservamos las migraciones anteriores para actualizar
        // instalaciones antiguas sin borrar sus registros.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `mediciones_temperatura` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `lineaId` INTEGER NOT NULL,
                        `temperatura` REAL NOT NULL,
                        `fechaHora` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_mediciones_temperatura_lineaId_fechaHora`
                    ON `mediciones_temperatura` (`lineaId`, `fechaHora`)
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE `mediciones_temperatura`
                    ADD COLUMN `origen` TEXT NOT NULL DEFAULT 'MANUAL'
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE `flushing`
                    ADD COLUMN `fotoRuta` TEXT
                    """.trimIndent()
                )
            }
        }

        // Creamos una tabla separada para los datos del servidor.
        // No copiamos ni mezclamos automáticamente el flushing antiguo.
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `flushing_remoto` (
                        `servidor` TEXT NOT NULL,
                        `id` INTEGER NOT NULL,
                        `lineaId` INTEGER NOT NULL,
                        `fechaHora` TEXT NOT NULL,
                        `duracionSegundos` INTEGER NOT NULL,
                        `observacion` TEXT,
                        `temperaturaAntes` REAL,
                        `temperaturaDespues` REAL,
                        `fotoRuta` TEXT,
                        `descargadoEn` INTEGER NOT NULL,
                        PRIMARY KEY (`servidor`, `id`)
                    )
                    """.trimIndent()
                )

                // Facilitamos las consultas del historial por línea y fecha.
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_flushing_remoto_servidor_lineaId_fechaHora`
                    ON `flushing_remoto` (`servidor`, `lineaId`, `fechaHora`)
                    """.trimIndent()
                )
            }
        }

        // Compartimos una sola instancia de la base dentro de la aplicación.
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aquacontrol.db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5
                    )
                    .build()
                    .also { database ->
                        INSTANCE = database
                    }
            }
        }
    }
}