package com.minedu.gob.pe.enaprescalidad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.data.repository.ConglomeradoListRepository
import com.minedu.gob.pe.enaprescalidad.data.repository.EnvioResult
import com.minedu.gob.pe.enaprescalidad.utils.hasInternet
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
//  UI STATE
// ─────────────────────────────────────────────────────────────────────────────

data class ConglomeradoUiState(

    // ── Opciones de los combos (se cargan en cascada) ─────────────────────────
    val anios: List<Int>      = emptyList(),
    val meses: List<Int>      = emptyList(),
    val periodos: List<Int>   = emptyList(),
    val proyectos: List<Int>  = emptyList(),

    // ── Selecciones actuales ──────────────────────────────────────────────────
    val anioSel: Int?     = null,
    val mesSel: Int?      = null,
    val periodoSel: Int?  = null,
    val proyectoSel: Int? = null,

    // ── Lista resultante ──────────────────────────────────────────────────────
    val muestras: List<MuestraConglomeradoEntity> = emptyList(),

    // ── Estados de carga y error ──────────────────────────────────────────────
    val isLoadingCombos: Boolean = false,
    val isLoadingMuestras: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val sendSuccess: Boolean = false,
    val lastEnvio: String? = null,          // fecha del último envío exitoso
) {
    /** true si todos los combos están seleccionados */
    val filtroCompleto: Boolean
        get() = anioSel != null && mesSel != null && periodoSel != null && proyectoSel != null

    /** Muestras que aún no han sido enviadas */
    val pendientesEnvio: Int
        get() = muestras.count { !it.sincronizado }

    /** Última fecha de sincronización entre todas las muestras de la lista */
    val ultimaFechaEnvio: String?
        get() = muestras
            .mapNotNull { it.fecha_sincronizacion }
            .maxOrNull()
}

// ─────────────────────────────────────────────────────────────────────────────
//  VIEW MODEL
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class ConglomeradoViewModel @Inject constructor(
    private val repo: ConglomeradoListRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConglomeradoUiState())
    val uiState: StateFlow<ConglomeradoUiState> = _uiState.asStateFlow()

    // ── Inicialización ────────────────────────────────────────────────────────

    /**
     * Llamar UNA vez desde LaunchedEffect cuando el userId esté disponible.
     * Carga los años disponibles y resetea el resto de combos.
     */
    fun init(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCombos = true, error = null) }
            val anios = repo.getAniosDisponibles(userId)
            _uiState.update {
                it.copy(
                    isLoadingCombos = false,
                    anios = anios,
                    // Pre-selecciona el primer año si solo hay uno
                    anioSel = if (anios.size == 1) anios.first() else null,
                    meses = emptyList(), mesSel = null,
                    periodos = emptyList(), periodoSel = null,
                    proyectos = emptyList(), proyectoSel = null,
                    muestras = emptyList(),
                )
            }
            // Si pre-seleccionamos año, cargamos los meses automáticamente
            _uiState.value.anioSel?.let { onAnioSelected(userId, it) }
        }
    }

    // ── Selección en cascada ──────────────────────────────────────────────────

    fun onAnioSelected(userId: String, anio: Int) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    anioSel = anio,
                    meses = emptyList(), mesSel = null,
                    periodos = emptyList(), periodoSel = null,
                    proyectos = emptyList(), proyectoSel = null,
                    muestras = emptyList(),
                )
            }
            val meses = repo.getMesesDisponibles(userId, anio)
            _uiState.update { it.copy(meses = meses) }
        }
    }

    fun onMesSelected(userId: String, mes: Int) {
        val anio = _uiState.value.anioSel ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    mesSel = mes,
                    periodos = emptyList(), periodoSel = null,
                    proyectos = emptyList(), proyectoSel = null,
                    muestras = emptyList(),
                )
            }
            val periodos = repo.getPeriodosDisponibles(userId, anio, mes)
            _uiState.update { it.copy(periodos = periodos) }
        }
    }

    fun onPeriodoSelected(userId: String, periodo: Int) {
        val anio = _uiState.value.anioSel ?: return
        val mes  = _uiState.value.mesSel  ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    periodoSel = periodo,
                    proyectos = emptyList(), proyectoSel = null,
                    muestras = emptyList(),
                )
            }
            val proyectos = repo.getProyectosDisponibles(userId, anio, mes, periodo)
            _uiState.update { it.copy(proyectos = proyectos) }
        }
    }

    fun onProyectoSelected(userId: String, proyecto: Int) {
        val anio    = _uiState.value.anioSel    ?: return
        val mes     = _uiState.value.mesSel     ?: return
        val periodo = _uiState.value.periodoSel ?: return

        _uiState.update { it.copy(proyectoSel = proyecto, muestras = emptyList()) }

        // Observa de forma reactiva: cualquier cambio en Room actualiza la lista
        viewModelScope.launch {
            repo.getMuestraFiltrada(userId, anio, mes, periodo, proyecto)
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { lista ->
                    _uiState.update { it.copy(muestras = lista, isLoadingMuestras = false) }
                }
        }
    }

    // ── Acciones de los botones ───────────────────────────────────────────────

    /**
     * Enviar la data al servidor.
     * Aquí conectas tu repositorio remoto; por ahora es un placeholder.
     */
    fun onEnviar(userId: String, isOnline: Boolean) {
        if (_uiState.value.isSending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }

            val result = repo.enviarPendientes(
                muestras = _uiState.value.muestras,
                isOnline = isOnline,
            )

            _uiState.update { state ->
                when (result) {
                    is EnvioResult.Success ->
                        state.copy(isSending = false, sendSuccess = true)
                    is EnvioResult.Parcial ->
                        state.copy(isSending = false, sendSuccess = true,
                            error = "${result.fallidos} muestra(s) no se pudieron enviar")
                    is EnvioResult.SinPendientes ->
                        state.copy(isSending = false)
                    is EnvioResult.Error ->
                        state.copy(isSending = false, error = result.message)
                }
            }
        }
    }

    fun clearSendSuccess() = _uiState.update { it.copy(sendSuccess = false) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}