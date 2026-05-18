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
import androidx.lifecycle.SavedStateHandle
import com.minedu.gob.pe.enaprescalidad.ui.navigation.NavigationManager
import kotlinx.coroutines.flow.drop
import kotlinx.serialization.descriptors.StructureKind
import java.util.Objects


// ─────────────────────────────────────────────────────────────────────────────
//  UI STATE
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class ConglomeradoViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    private val repo: ConglomeradoListRepository,
    private val savedState: SavedStateHandle,
) : ViewModel(), ConglomeradoActions {

    private val _uiState = MutableStateFlow(ConglomeradoUiState())
    val uiState: StateFlow<ConglomeradoUiState> = _uiState.asStateFlow()

    private companion object {
        const val KEY_ANIO = "cong_anio"; const val KEY_MES = "cong_mes"
        const val KEY_PERIODO = "cong_per"; const val KEY_PROYECTO = "cong_pro"
        const val KEY_USER = "cong_user"; const val PANTALLA_ID  = "verificacionConglomerado"
    }

    init {
        // Observa cambios de pantalla desde el NavigationManager.
        // drop(1) ignora el valor inicial (el que había antes de suscribirse).
        viewModelScope.launch {
            navigationManager.currentScreen
                .drop(1)
                .collect { nuevoId ->
                    val userId = savedState.get<String>(KEY_USER) ?: return@collect

                    if (nuevoId != PANTALLA_ID) {
                        // Salimos de conglomerado → limpiar todo
                        resetFiltros()
                    } else {
                        init(userId)
                    }
                }
        }
    }

    fun init(userId: String) {
        // Si hay estado guardado en SavedStateHandle (viene de rotación) → restaurar
        if (savedState.get<String>(KEY_USER) == userId && savedState.contains(KEY_ANIO)) {
            restoreState(userId)
            return
        }
        // Primera carga o post-reset → carga fresca
        viewModelScope.launch {
            savedState[KEY_USER] = userId
            val anios = repo.getAniosDisponibles(userId)
            _uiState.update { it.copy(anios = anios) }
            if (anios.size == 1) onAnioSelected(userId, anios.first())
        }
    }

    override fun resetFiltros() {
        // Limpiar SavedStateHandle para que init() no restaure estado viejo
        savedState.remove<Int>(KEY_ANIO)
        savedState.remove<Int>(KEY_MES)
        savedState.remove<Int>(KEY_PERIODO)
        savedState.remove<Int>(KEY_PROYECTO)
        // Limpiar UI inmediatamente
        _uiState.update {
            ConglomeradoUiState()   // estado inicial limpio
        }
    }

    private fun restoreState(userId: String) {
        viewModelScope.launch {
            val a  = savedState.get<Int>(KEY_ANIO)     ?: return@launch
            val m  = savedState.get<Int>(KEY_MES)
            val p  = savedState.get<Int>(KEY_PERIODO)
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
        // 1. Limpiamos de forma segura el SavedStateHandle según el nivel alterado
        if (nivel <= 1) { savedState.remove<Int>(KEY_MES) }
        if (nivel <= 2) { savedState.remove<Int>(KEY_PERIODO) }
        if (nivel <= 3) {savedState.remove<Int>(KEY_PROYECTO) }

        // 2. Sincronizamos el UI State
        _uiState.update { it.copy(
            mesSel = if (nivel <= 1) null else it.mesSel,
            periodoSel = if (nivel <= 2) null else it.periodoSel,
            proyectoSel = if (nivel <= 3) null else it.proyectoSel,
            muestras = emptyList(),
            seleccionados = emptySet()
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
    override fun resetModoSeleccion() = _uiState.update { it.copy(modoSeleccion = false, seleccionados = emptySet(), anioSel = null, mesSel = null, periodoSel = null, proyectoSel = null) }
}

interface ConglomeradoActions {
    fun resetFiltros()
    fun resetModoSeleccion()
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