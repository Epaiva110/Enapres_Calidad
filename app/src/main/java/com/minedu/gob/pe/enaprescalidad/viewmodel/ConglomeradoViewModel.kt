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

//data class ConglomeradoUiState(
//
//    // ── Opciones de los combos (se cargan en cascada) ─────────────────────────
//    val anios: List<Int>      = emptyList(),
//    val meses: List<Int>      = emptyList(),
//    val periodos: List<Int>   = emptyList(),
//    val proyectos: List<Int>  = emptyList(),
//
//    // ── Selecciones actuales ──────────────────────────────────────────────────
//    val anioSel: Int?     = null,
//    val mesSel: Int?      = null,
//    val periodoSel: Int?  = null,
//    val proyectoSel: Int? = null,
//
//    // ── Lista resultante ──────────────────────────────────────────────────────
//    val muestras: List<MuestraConglomeradoEntity> = emptyList(),
//
//    // ── Estados de carga y error ──────────────────────────────────────────────
//    val isLoadingCombos: Boolean = false,
//    val isLoadingMuestras: Boolean = false,
//    val isSending: Boolean = false,
//    val error: String? = null,
//    val sendSuccess: Boolean = false,
//    val lastEnvio: String? = null,          // fecha del último envío exitoso
//) {
//    /** true si todos los combos están seleccionados */
//    val filtroCompleto: Boolean
//        get() = anioSel != null && mesSel != null && periodoSel != null && proyectoSel != null
//
//    /** Muestras que aún no han sido enviadas */
//    val pendientesEnvio: Int
//        get() = muestras.count { !it.sincronizado }
//
//    /** Última fecha de sincronización entre todas las muestras de la lista */
//    val ultimaFechaEnvio: String?
//        get() = muestras
//            .mapNotNull { it.fecha_sincronizacion }
//            .maxOrNull()
//}
//
//// ─────────────────────────────────────────────────────────────────────────────
////  VIEW MODEL
//// ─────────────────────────────────────────────────────────────────────────────
//
//@HiltViewModel
//class ConglomeradoViewModel @Inject constructor(
//    private val repo: ConglomeradoListRepository,
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow(ConglomeradoUiState())
//    val uiState: StateFlow<ConglomeradoUiState> = _uiState.asStateFlow()
//
//    // ── Inicialización ────────────────────────────────────────────────────────
//
//    /**
//     * Llamar UNA vez desde LaunchedEffect cuando el userId esté disponible.
//     * Carga los años disponibles y resetea el resto de combos.
//     */
//    fun init(userId: String) {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoadingCombos = true, error = null) }
//            val anios = repo.getAniosDisponibles(userId)
//            _uiState.update {
//                it.copy(
//                    isLoadingCombos = false,
//                    anios = anios,
//                    // Pre-selecciona el primer año si solo hay uno
//                    anioSel = if (anios.size == 1) anios.first() else null,
//                    meses = emptyList(), mesSel = null,
//                    periodos = emptyList(), periodoSel = null,
//                    proyectos = emptyList(), proyectoSel = null,
//                    muestras = emptyList(),
//                )
//            }
//            // Si pre-seleccionamos año, cargamos los meses automáticamente
//            _uiState.value.anioSel?.let { onAnioSelected(userId, it) }
//        }
//    }
//
//    // ── Selección en cascada ──────────────────────────────────────────────────
//
//    fun onAnioSelected(userId: String, anio: Int) {
//        viewModelScope.launch {
//            _uiState.update {
//                it.copy(
//                    anioSel = anio,
//                    meses = emptyList(), mesSel = null,
//                    periodos = emptyList(), periodoSel = null,
//                    proyectos = emptyList(), proyectoSel = null,
//                    muestras = emptyList(),
//                )
//            }
//            val meses = repo.getMesesDisponibles(userId, anio)
//            _uiState.update { it.copy(meses = meses) }
//        }
//    }
//
//    fun onMesSelected(userId: String, mes: Int) {
//        val anio = _uiState.value.anioSel ?: return
//        viewModelScope.launch {
//            _uiState.update {
//                it.copy(
//                    mesSel = mes,
//                    periodos = emptyList(), periodoSel = null,
//                    proyectos = emptyList(), proyectoSel = null,
//                    muestras = emptyList(),
//                )
//            }
//            val periodos = repo.getPeriodosDisponibles(userId, anio, mes)
//            _uiState.update { it.copy(periodos = periodos) }
//        }
//    }
//
//    fun onPeriodoSelected(userId: String, periodo: Int) {
//        val anio = _uiState.value.anioSel ?: return
//        val mes  = _uiState.value.mesSel  ?: return
//        viewModelScope.launch {
//            _uiState.update {
//                it.copy(
//                    periodoSel = periodo,
//                    proyectos = emptyList(), proyectoSel = null,
//                    muestras = emptyList(),
//                )
//            }
//            val proyectos = repo.getProyectosDisponibles(userId, anio, mes, periodo)
//            _uiState.update { it.copy(proyectos = proyectos) }
//        }
//    }
//
//    fun onProyectoSelected(userId: String, proyecto: Int) {
//        val anio    = _uiState.value.anioSel    ?: return
//        val mes     = _uiState.value.mesSel     ?: return
//        val periodo = _uiState.value.periodoSel ?: return
//
//        _uiState.update { it.copy(proyectoSel = proyecto, muestras = emptyList()) }
//
//        // Observa de forma reactiva: cualquier cambio en Room actualiza la lista
//        viewModelScope.launch {
//            repo.getMuestraFiltrada(userId, anio, mes, periodo, proyecto)
//                .catch { e -> _uiState.update { it.copy(error = e.message) } }
//                .collect { lista ->
//                    _uiState.update { it.copy(muestras = lista, isLoadingMuestras = false) }
//                }
//        }
//    }
//
//    // ── Acciones de los botones ───────────────────────────────────────────────
//
//    /**
//     * Enviar la data al servidor.
//     * Aquí conectas tu repositorio remoto; por ahora es un placeholder.
//     */
//    fun onEnviar(userId: String, isOnline: Boolean) {
//        if (_uiState.value.isSending) return
//        viewModelScope.launch {
//            _uiState.update { it.copy(isSending = true, error = null) }
//
//            val result = repo.enviarPendientes(
//                muestras = _uiState.value.muestras,
//                isOnline = isOnline,
//            )
//
//            _uiState.update { state ->
//                when (result) {
//                    is EnvioResult.Success ->
//                        state.copy(isSending = false, sendSuccess = true)
//                    is EnvioResult.Parcial ->
//                        state.copy(isSending = false, sendSuccess = true,
//                            error = "${result.fallidos} muestra(s) no se pudieron enviar")
//                    is EnvioResult.SinPendientes ->
//                        state.copy(isSending = false)
//                    is EnvioResult.Error ->
//                        state.copy(isSending = false, error = result.message)
//                }
//            }
//        }
//    }
//
//    fun clearSendSuccess() = _uiState.update { it.copy(sendSuccess = false) }
//    fun clearError() = _uiState.update { it.copy(error = null) }
//}

import androidx.lifecycle.SavedStateHandle


// ─────────────────────────────────────────────────────────────────────────────
//  UI STATE
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class ConglomeradoViewModel @Inject constructor(
    private val repo: ConglomeradoListRepository,
    private val savedState: SavedStateHandle,
) : ViewModel(), ConglomeradoActions {

    private val _uiState = MutableStateFlow(ConglomeradoUiState())
    val uiState: StateFlow<ConglomeradoUiState> = _uiState.asStateFlow()

    private companion object {
        const val KEY_ANIO = "cong_anio"; const val KEY_MES = "cong_mes"
        const val KEY_PERIODO = "cong_per"; const val KEY_PROYECTO = "cong_pro"
        const val KEY_USER = "cong_user"
    }

    fun init(userId: String) {
        if (savedState.get<String>(KEY_USER) == userId && savedState.contains(KEY_ANIO)) {
            restoreState(userId)
            return
        }
        viewModelScope.launch {
            savedState[KEY_USER] = userId
            val anios = repo.getAniosDisponibles(userId)
            _uiState.update { it.copy(anios = anios) }
            if (anios.size == 1) onAnioSelected(userId, anios.first())
        }
    }

    private fun restoreState(userId: String) {
        viewModelScope.launch {
            val a = savedState.get<Int>(KEY_ANIO) ?: return@launch
            val m = savedState.get<Int>(KEY_MES)
            val p = savedState.get<Int>(KEY_PERIODO)
            val pr = savedState.get<Int>(KEY_PROYECTO)

            _uiState.update { it.copy(anios = repo.getAniosDisponibles(userId), anioSel = a) }
            m?.let { mes ->
                _uiState.update { it.copy(meses = repo.getMesesDisponibles(userId, a), mesSel = mes) }
                p?.let { per ->
                    _uiState.update { it.copy(periodos = repo.getPeriodosDisponibles(userId, a, mes), periodoSel = per) }
                    pr?.let { pro ->
                        _uiState.update { it.copy(proyectos = repo.getProyectosDisponibles(userId, a, mes, per), proyectoSel = pro) }
                        observarMuestras(userId, a, mes, per, pro)
                    }
                }
            }
        }
    }

    override fun onAnioSelected(userId: String, anio: Int) {
        resetDesde(1); savedState[KEY_ANIO] = anio
        viewModelScope.launch {
            val meses = repo.getMesesDisponibles(userId, anio)
            _uiState.update { it.copy(anioSel = anio, meses = meses) }
        }
    }

    override fun onMesSelected(userId: String, mes: Int) {
        resetDesde(2); savedState[KEY_MES] = mes
        viewModelScope.launch {
            val periodos = repo.getPeriodosDisponibles(userId, _uiState.value.anioSel!!, mes)
            _uiState.update { it.copy(mesSel = mes, periodos = periodos) }
        }
    }

    override fun onPeriodoSelected(userId: String, periodo: Int) {
        resetDesde(3); savedState[KEY_PERIODO] = periodo
        viewModelScope.launch {
            val pros = repo.getProyectosDisponibles(userId, _uiState.value.anioSel!!, _uiState.value.mesSel!!, periodo)
            _uiState.update { it.copy(periodoSel = periodo, proyectos = pros) }
        }
    }

    override fun onProyectoSelected(userId: String, proyecto: Int) {
        savedState[KEY_PROYECTO] = proyecto
        _uiState.update { it.copy(proyectoSel = proyecto, isLoadingMuestras = true) }
        observarMuestras(userId, _uiState.value.anioSel!!, _uiState.value.mesSel!!, _uiState.value.periodoSel!!, proyecto)
    }

    private fun observarMuestras(u: String, a: Int, m: Int, p: Int, pr: Int) {
        viewModelScope.launch {
            repo.getMuestraFiltrada(u, a, m, p, pr)
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoadingMuestras = false) } }
                .collect { list -> _uiState.update { it.copy(muestras = list, isLoadingMuestras = false) } }
        }
    }

    private fun resetDesde(nivel: Int) {
        _uiState.update { it.copy(
            mesSel = if (nivel <= 1) null else it.mesSel,
            periodoSel = if (nivel <= 2) null else it.periodoSel,
            proyectoSel = if (nivel <= 3) null else it.proyectoSel,
            muestras = emptyList(), seleccionados = emptySet()
        ) }
    }

    override fun onEnviarTodas(online: Boolean) = enviar(_uiState.value.muestras.filter { !it.sincronizado }, online)
    override fun onEnviarSeleccionadas(online: Boolean) = enviar(_uiState.value.muestras.filter { it.id in _uiState.value.seleccionados && !it.sincronizado }, online)

    private fun enviar(muestras: List<MuestraConglomeradoEntity>, online: Boolean) {
        if (_uiState.value.isSending || muestras.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            val result = repo.enviarPendientes(muestras, online)
            _uiState.update { it.copy(isSending = false, sendSuccess = result is EnvioResult.Success || result is EnvioResult.Parcial,
                error = (result as? EnvioResult.Error)?.message ?: (result as? EnvioResult.Parcial)?.let { "Error en algunos envíos" },
                modoSeleccion = false, seleccionados = emptySet()) }
        }
    }

    override fun toggleModoSeleccion() = _uiState.update { it.copy(modoSeleccion = !it.modoSeleccion, seleccionados = emptySet()) }
    override fun toggleSeleccion(id: Int) = _uiState.update { it.copy(seleccionados = if (id in it.seleccionados) it.seleccionados - id else it.seleccionados + id) }
    override fun seleccionarTodosPendientes() = _uiState.update { s -> s.copy(seleccionados = s.muestras.filter { !it.sincronizado }.map { it.id }.toSet()) }
    override fun deseleccionarTodos() = _uiState.update { it.copy(seleccionados = emptySet()) }
    override fun clearSendSuccess() = _uiState.update { it.copy(sendSuccess = false) }
    override fun clearError() = _uiState.update { it.copy(error = null) }
}

interface ConglomeradoActions {
    fun onAnioSelected(userId: String, anio: Int)
    fun onMesSelected(userId: String, mes: Int)
    fun onPeriodoSelected(userId: String, periodo: Int)
    fun onProyectoSelected(userId: String, proyecto: Int)
    fun onEnviarTodas(isOnline: Boolean)
    fun onEnviarSeleccionadas(isOnline: Boolean)
    fun toggleModoSeleccion()
    fun toggleSeleccion(id: Int)
    fun seleccionarTodosPendientes()
    fun deseleccionarTodos()
    fun clearError()
    fun clearSendSuccess()
}

data class ConglomeradoUiState(
    val anios: List<Int> = emptyList(),
    val meses: List<Int> = emptyList(),
    val periodos: List<Int> = emptyList(),
    val proyectos: List<Int> = emptyList(),
    val anioSel: Int? = null,
    val mesSel: Int? = null,
    val periodoSel: Int? = null,
    val proyectoSel: Int? = null,
    val muestras: List<MuestraConglomeradoEntity> = emptyList(),
    val seleccionados: Set<Int> = emptySet(),
    val modoSeleccion: Boolean = false,
    val isLoadingCombos: Boolean = false,
    val isLoadingMuestras: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val sendSuccess: Boolean = false
) {
    val filtroCompleto: Boolean get() = anioSel != null && mesSel != null && periodoSel != null && proyectoSel != null
    val pendientesTotal: Int get() = muestras.count { !it.sincronizado }
    val ultimaFechaEnvio: String? get() = muestras.mapNotNull { it.fecha_sincronizacion }.maxOrNull()
}