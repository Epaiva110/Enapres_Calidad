package com.minedu.gob.pe.enaprescalidad.data.repository

import android.util.Log
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraConglomeradoDao
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.api.MuestraApi
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────────────────────
//  ConglomeradoListRepository
//
//  LECTURA  → solo Room (sin tocar la red)
//  ENVÍO    → UPDATE en Supabase (sincronizado=true + fecha),
//             luego refleja el cambio en Room para que la UI se actualice sola
// ─────────────────────────────────────────────────────────────────────────────

@Singleton
class ConglomeradoListRepository @Inject constructor(
    private val dao: MuestraConglomeradoDao,
    private val api: MuestraApi,
) {

    // ── LECTURA LOCAL (Room) ──────────────────────────────────────────────────

    fun getMuestraFiltrada(
        user: String, anio: Int, mes: Int, periodo: Int, proyecto: Int,
    ): Flow<List<MuestraConglomeradoEntity>> =
        dao.getMuestraFiltrada(user, anio, mes, periodo, proyecto)

    suspend fun getAniosDisponibles(user: String): List<Int> =
        dao.getAniosDisponibles(user)

    suspend fun getMesesDisponibles(user: String, anio: Int): List<Int> =
        dao.getMesesDisponibles(user, anio)

    suspend fun getPeriodosDisponibles(user: String, anio: Int, mes: Int): List<Int> =
        dao.getPeriodosDisponibles(user, anio, mes)

    suspend fun getProyectosDisponibles(user: String, anio: Int, mes: Int, periodo: Int): List<Int> =
        dao.getProyectosDisponibles(user, anio, mes, periodo)

    // ── ENVÍO AL SERVIDOR ─────────────────────────────────────────────────────

    /**
     * Para cada muestra pendiente:
     *  1. Hace UPDATE en Supabase (sincronizado = true, fecha_sincronizacion = ahora)
     *  2. Si el servidor responde OK, actualiza Room → el Flow reactive
     *     refresca la UI automáticamente sin recargar nada.
     *
     * Sigue intentando con las demás aunque una falle (no corta al primer error).
     */
    suspend fun enviarPendientes(
        muestras: List<MuestraConglomeradoEntity>,
        isOnline: Boolean,
    ): EnvioResult {

        if (!isOnline) return EnvioResult.Error("Sin conexión a internet")

        val pendientes = muestras.filter { !it.sincronizado }
        if (pendientes.isEmpty()) return EnvioResult.SinPendientes

        val ahora = System.currentTimeMillis().toString()
        var enviados = 0
        var fallidos = 0

        for (muestra in pendientes) {
            try {
                // 1. UPDATE en Supabase
                api.marcarSincronizadoC(id = muestra.id, fecha = ahora)

                // 2. Refleja en Room → el Flow de getMuestraFiltrada actualiza la UI
                dao.marcarSincronizada(id = muestra.id, fecha = ahora)

                enviados++
                Log.i("ConglomeradoRepo", "Enviado id=${muestra.id}")

            } catch (e: Exception) {
                fallidos++
                Log.e("ConglomeradoRepo", "Error id=${muestra.id}: ${e.message}")
            }
        }

        return when {
            fallidos == 0 -> EnvioResult.Success(count = enviados)
            enviados == 0 -> EnvioResult.Error("No se pudo enviar ninguna muestra")
            else          -> EnvioResult.Parcial(enviados = enviados, fallidos = fallidos)
        }
    }
}

// ── Resultado del envío ────────────────────────────────────────────────────────
sealed class EnvioResult {
    data class Success(val count: Int) : EnvioResult()
    data object SinPendientes : EnvioResult()
    data class Parcial(val enviados: Int, val fallidos: Int) : EnvioResult()
    data class Error(val message: String) : EnvioResult()
}