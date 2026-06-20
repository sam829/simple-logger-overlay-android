package in.sammacwan.logger.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkLogDao {
    @Query("SELECT * FROM network_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getAllPaged(limit: Int): Flow<List<NetworkLogEntity>>
    
    @Query("SELECT * FROM network_logs WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): NetworkLogEntity?
    
    @Query("SELECT * FROM network_logs WHERE response_code BETWEEN 200 AND 299 ORDER BY timestamp DESC")
    fun getSuccessful(): Flow<List<NetworkLogEntity>>
    
    @Query("SELECT * FROM network_logs WHERE response_code < 200 OR response_code >= 300 OR response_code IS NULL ORDER BY timestamp DESC")
    fun getErrors(): Flow<List<NetworkLogEntity>>
    
    @Query("SELECT * FROM network_logs WHERE url LIKE '%' || :query || '%' OR method LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun search(query: String): Flow<List<NetworkLogEntity>>
    
    @Insert
    suspend fun insert(log: NetworkLogEntity)
    
    @Update
    suspend fun update(log: NetworkLogEntity)
    
    @Query("DELETE FROM network_logs WHERE timestamp < :cutoffTime")
    suspend fun deleteOlderThan(cutoffTime: Long)
    
    @Query("DELETE FROM network_logs")
    suspend fun deleteAll()
}
