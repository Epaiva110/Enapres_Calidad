package com.minedu.gob.pe.enaprescalidad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SyncType
import com.minedu.gob.pe.enaprescalidad.data.repository.MarcoTrabajoRepository
import com.minedu.gob.pe.enaprescalidad.data.repository.MarcoTrabajoResultLocal
import com.minedu.gob.pe.enaprescalidad.data.repository.MarcoTrabajoResultRemote
import com.minedu.gob.pe.enaprescalidad.data.repository.MuestraConglomeradoRepository
import com.minedu.gob.pe.enaprescalidad.data.repository.MuestraResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.minedu.gob.pe.enaprescalidad.data.domain.MarcoTrabajo


/**
 * ViewModel exclusivo de UpdateScreen.
 *
 * Responsabilidades:
 *  - Observar los marcos de trabajo locales (Room → Flow).
 *  - Disparar la búsqueda remota de marcos.
 *  - Coordinar la sincronización de muestras (un tipo a la vez).
 *  - Exponer un único [UpdateUiState] inmutable a la UI.
 *
 * No conoce nada de Compose ni de Context.
 */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val marcoRepo: MarcoTrabajoRepository,
    private val muestraRepo: MuestraConglomeradoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    // ── Observación reactiva de marcos ────────────────────────────────────────

    /**
     * Llama esto UNA vez desde LaunchedEffect cuando el usuario está disponible.
     * Inicia la observación reactiva: cada cambio en Room actualiza la UI automáticamente.
     */
    fun observeMarcos(userId: String) {
        viewModelScope.launch {
            marcoRepo.getMarcoTrabajoLocal(userId).collect { result ->
                _uiState.update { state ->
                    when (result) {
                        is MarcoTrabajoResultLocal.Success ->
                            state.copy(marcos = result.data)
                        is MarcoTrabajoResultLocal.Empty ->
                            state.copy(marcos = emptyList())
                        is MarcoTrabajoResultLocal.Error ->
                            state.copy(marcos = emptyList(), marcoError = result.message)
                    }
                }
            }
        }
    }

    // ── Búsqueda de marcos (Card 1) ───────────────────────────────────────────

    /**
     * Descarga los marcos de trabajo del servidor y los guarda en Room.
     * El Flow de [observeMarcos] actualiza la UI automáticamente al terminar.
     */
    fun fetchMarcos(userId: String, isOnline: Boolean) {
        if (_uiState.value.isAnyLoading) return // evita doble tap

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMarcos = true, marcoError = null) }

            val result = marcoRepo.getMarcoTrabajo(userId, isOnline)

            _uiState.update { state ->
                when (result) {
                    is MarcoTrabajoResultRemote.Success ->
                        state.copy(isLoadingMarcos = false, marcoSuccess = true, lastSyncType = null )
                    is MarcoTrabajoResultRemote.Empty ->
                        state.copy(isLoadingMarcos = false, marcoError = result.message)
                    is MarcoTrabajoResultRemote.Error ->
                        state.copy(isLoadingMarcos = false, marcoError = result.message)
                }
            }
        }
    }

    // ── Sincronización de Cargas ────────────────────────────────────────────

    /**
     * Sincroniza un tipo específico de Cargas.
     * Bloquea otros syncs mientras está en curso para evitar condiciones de carrera en Room.
     */
    fun syncType(type: SyncType, userId: String, isOnline: Boolean) {
        if (_uiState.value.isAnyLoading) return // evita solapamiento

        viewModelScope.launch {
            _uiState.update { it.copy(syncingType = type, syncError = null) }

            val result = when (type) {
                SyncType.CONGLOMERADO -> marcoRepo.getMarcoTrabajoTipo(userId,"Conglomerado", isOnline)
                SyncType.VIVIENDA     -> marcoRepo.getMarcoTrabajoTipo(userId,"Vivienda", isOnline)
                SyncType.REENTREVISTA -> marcoRepo.getMarcoTrabajoTipo(userId,"Reentrevista", isOnline)
            }
            _uiState.update { state ->
                when (result) {
                    is MarcoTrabajoResultRemote.Success ->
                        state.copy(syncingType = null, syncSuccess = true, lastSyncType = type)

                    is MarcoTrabajoResultRemote.Empty ->
                        state.copy(syncingType = null, syncError = result.message)

                    is MarcoTrabajoResultRemote.Error ->
                        state.copy(syncingType = null, syncError = result.message)
                }
            }
        }
    }

    /**
     * Actualiza los datos de UNA carga individual (por su id).
     * Llama al endpoint que recarga solo esa carga concreta.
     *
     * Se mapea al botón Refresh (🔄) de cada fila de la tabla.
     */
    fun syncIndividual(cargaId: String, userId: String, isOnline: Boolean) {
        if (_uiState.value.isAnyLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(syncingIndividualId = cargaId, syncError = null) }

//            val result = muestraRepo.syncMuestraIndividual(cargaId, userId, isOnline)
//
//            _uiState.update { state ->
//                when (result) {
//                    is MuestraResult.Success ->
//                        state.copy(syncingIndividualId = null, syncSuccess = true)
//                    is MuestraResult.Empty ->
//                        state.copy(syncingIndividualId = null, syncError = result.message)
//                    is MuestraResult.Error ->
//                        state.copy(syncingIndividualId = null, syncError = result.message)
//                }
//            }
        }
    }

    /**
     * Sincroniza todos los tipos secuencialmente (CONGLOMERADO → VIVIENDA → REENTREVISTA).
     * Espera a que cada uno termine antes de iniciar el siguiente.
     */
    fun syncAll(userId: String, isOnline: Boolean) {
        if (_uiState.value.isAnyLoading) return

        viewModelScope.launch {
            for (type in SyncType.entries) {
                _uiState.update { it.copy(syncingType = type, syncError = null) }

                val result = when (type) {
                    SyncType.CONGLOMERADO -> muestraRepo.syncMuestraConglomerado(userId, isOnline)
                    SyncType.VIVIENDA     -> muestraRepo.syncMuestraVivienda(userId, isOnline)
                    SyncType.REENTREVISTA -> muestraRepo.syncReentrevista(userId, isOnline)
                }

                // Si un tipo falla, detenemos el proceso
                if (result is MuestraResult.Error) {
                    _uiState.update { it.copy(syncingType = null, syncError = result.message) }
                    return@launch
                }
            }
            _uiState.update { it.copy(syncingType = null, syncSuccess = true) }
        }
    }

    // ── Limpieza de mensajes transitorios ─────────────────────────────────────

    fun clearSyncSuccess()    = _uiState.update { it.copy(syncSuccess = false) }
    fun clearSyncError()      = _uiState.update { it.copy(syncError = null) }
    fun clearMarcoSuccess()   = _uiState.update { it.copy(marcoSuccess = false) }
    fun clearMarcoError()     = _uiState.update { it.copy(marcoError = null) }
}




/**
 * Estado inmutable de la pantalla UpdateScreen.
 *
 * La UI solo lee este objeto — nunca lo muta directamente.
 * Toda mutación pasa por UpdateViewModel.
 *
 * Separamos el estado de carga de marcos (búsqueda remota)
 * del estado de sincronización de muestras (descarga).
 */

data class UpdateUiState(

    // ── Datos ────────────────────────────────────────────────────────────────
    /** Todos los marcos de trabajo del usuario cargados desde Room. */
    val marcos: List<MarcoTrabajo> = emptyList(),

    // ── Estado de carga de marcos (Card 1) ───────────────────────────────────
    /** true mientras se buscan nuevos marcos desde Supabase. */
    val isLoadingMarcos: Boolean = false,

    val lastSyncType: SyncType? = null,

    /** Mensaje de error al buscar marcos (null = sin error). */
    val marcoError: String? = null,

    /** true si la búsqueda de marcos terminó con éxito (para mostrar snackbar). */
    val marcoSuccess: Boolean = false,

    // ── Estado de sincronización de muestras (Card 2 + Secciones) ────────────
    /**
     * Qué tipo está sincronizando actualmente (null = nadie sincronizando).
     * Un solo tipo a la vez para evitar condiciones de carrera en Room.
     */
    val syncingType: SyncType? = null,

    /** Error de la última sincronización (null = sin error). */
    val syncError: String? = null,

    /** true si el último sync terminó exitosamente (para mostrar snackbar). */
    val syncSuccess: Boolean = false,

    /**
     * ID de la carga individual que está actualizándose ahora mismo.
     * null = nadie en curso. La tabla usa esto para mostrar el spinner en la fila correcta.
     */
    val syncingIndividualId: String? = null
) {
    // ── Derivados ─────────────────────────────────────────────────────────────

    val conglomerados: List<MarcoTrabajo>
        get() = marcos.filter { it.tipo.equals("Conglomerado", ignoreCase = true) }

    val reentrevistas: List<MarcoTrabajo>
        get() = marcos.filter { it.tipo.equals("Reentrevista", ignoreCase = true) }

    val viviendas: List<MarcoTrabajo>
        get() = marcos.filter { it.tipo.equals("Vivienda", ignoreCase = true) }

    /** Total de marcos pendientes de actualizar. */
    val pendingTotal: Int
        get() = marcos.count { !it.estaAlDia }

    /** true si cualquier operación de fondo está en curso. */
    val isAnyLoading: Boolean
        get() = isLoadingMarcos || syncingType != null || syncingIndividualId != null
}
