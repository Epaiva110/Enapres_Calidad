package com.minedu.gob.pe.enaprescalidad.data.repository
//
//import com.minedu.gob.pe.enaprescalidad.data.local.dao.SyncDao
//import com.minedu.gob.pe.enaprescalidad.data.local.entity.SyncEntity
//import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.api.MuestraConnglomeradoApi
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class SyncRepository @Inject constructor(
//    private val dao: SyncDao,
//    private val apiCM: MuestraConnglomeradoApi
//)
//{
//
//    suspend fun syncViviendas(
//        userId: String,
//        lastSync: Long,
//        onProgress: (SyncEntity) -> Unit
//    ) {
//        var page = 1
//        var current = 0
//        var total = 0
//
//        do {
//            val response = api.getViviendas(page, lastSync)
//
//            total = response.totalItems
//
//            dao.save(
//                SyncStatusEntity(
//                    userId = userId,
//                    type = "viviendas",
//                    status = "LOADING",
//                    total = total,
//                    current = current
//                )
//            )
//
//            dao.save(
//                SyncStatusEntity(
//                    userId = userId,
//                    type = "viviendas",
//                    status = "LOADING",
//                    total = total,
//                    current = current
//                )
//            )
//
//            current += response.data.size
//
//            dao.save(
//                SyncStatusEntity(
//                    userId = userId,
//                    type = "viviendas",
//                    status = "LOADING",
//                    total = total,
//                    current = current
//                )
//            )
//
//            onProgress(
//                SyncStatusEntity(
//                    userId = userId,
//                    type = "viviendas",
//                    status = "LOADING",
//                    total = total,
//                    current = current
//                )
//            )
//
//            page++
//
//        } while (page <= response.totalPages)
//
//        dao.save(
//            SyncStatusEntity(
//                userId = userId,
//                type = "viviendas",
//                status = "SUCCESS",
//                lastSync = System.currentTimeMillis(),
//                total = total,
//                current = total
//            )
//        )
//    }
//}