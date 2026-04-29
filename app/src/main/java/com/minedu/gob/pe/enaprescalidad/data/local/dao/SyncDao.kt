package com.minedu.gob.pe.enaprescalidad.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SyncEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncDao {

    @Query("""
        SELECT * FROM sync_status 
        WHERE userId = :userId
    """)
    fun observeByUser(userId: String): Flow<List<SyncEntity>>

    @Query("""
        SELECT * FROM sync_status 
        WHERE userId = :userId AND type = :type
    """)
    fun observeOne(userId: String, type: String): Flow<SyncEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SyncEntity)

    @Query("""
        UPDATE sync_status 
        SET lastSync = :time, lastError = NULL, isSyncing = 0
        WHERE userId = :userId AND type = :type
    """)
    suspend fun markSuccess(userId: String, type: String, time: Long)

    @Query("""
        UPDATE sync_status 
        SET lastError = :error, isSyncing = 0
        WHERE userId = :userId AND type = :type
    """)
    suspend fun markError(userId: String, type: String, error: String)

    @Query("""
        UPDATE sync_status 
        SET isSyncing = 1
        WHERE userId = :userId AND type = :type
    """)
    suspend fun markSyncing(userId: String, type: String)
}