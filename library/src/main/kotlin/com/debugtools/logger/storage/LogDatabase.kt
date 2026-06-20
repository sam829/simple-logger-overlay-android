package com.debugtools.logger.storage

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LogEntity::class, NetworkLogEntity::class],
    version = 3,
    exportSchema = false
)
abstract class LogDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
    abstract fun networkLogDao(): NetworkLogDao
}
