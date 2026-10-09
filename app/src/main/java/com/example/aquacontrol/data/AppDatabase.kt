package com.example.aquacontrol.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.aquacontrol.data.flushing.local.FlushingDao
import com.example.aquacontrol.data.flushing.local.FlushingEntity

@Database(
    entities = [FlushingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun flushingDao(): FlushingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aquacontrol.db"
                ).build().also { database ->
                    INSTANCE = database
                }
            }
        }
    }
}