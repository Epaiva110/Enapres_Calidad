package com.minedu.gob.pe.enaprescalidad.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "sync_status",
    primaryKeys = ["userId", "type"]
)
data class SyncEntity(
    val userId: String,
    val type: String,

    val lastSync: Long? = null,
    val isSyncing: Boolean = false,
    val lastError: String? = null
)

enum class SyncType {
    CONGLOMERADO,
    VIVIENDA,
    REENTREVISTA
}