package com.debugtools.logger.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM logs ORDER BY timestamp DESC LIMIT :limit")
    fun getAllPaged(limit: Int): Flow<List<LogEntity>>
    
    @Query("SELECT * FROM logs WHERE level IN (:levels) ORDER BY timestamp DESC")
    fun getByLevels(levels: List<String>): Flow<List<LogEntity>>
    
    @Query("SELECT * FROM logs WHERE message LIKE '%' || :query || '%' OR tag LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun search(query: String): Flow<List<LogEntity>>
    
    @Insert
    suspend fun insert(log: LogEntity)
    
    @Query("DELETE FROM logs WHERE timestamp < :cutoffTime")
    suspend fun deleteOlderThan(cutoffTime: Long)
    
    @Query("DELETE FROM logs")
    suspend fun deleteAll()
}
