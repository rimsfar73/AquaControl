package com.example.aquacontrol.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.aquacontrol.data.flushing.local.FlushingDao
import com.example.aquacontrol.data.flushing.local.FlushingEntity

@Database(
    entities = [
        FlushingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun flushingDao(): FlushingDao
}
