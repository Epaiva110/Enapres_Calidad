package com.minedu.gob.pe.enaprescalidad.ui.domain.model

data class SyncStateMuestra(
    val loadingConglomerado: Boolean = false,
    val loadingViviendas: Boolean = false,
    val loadingReentrevistas: Boolean = false,

    val lastSyncConglomerado: Long? = null,
    val lastSyncViviendas: Long? = null,
    val lastSyncReentrevistas: Long? = null,

    val successMessage: String? = null,
    val error: String? = null
)