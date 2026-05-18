package com.minedu.gob.pe.enaprescalidad.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.enaprescalidad.ui.domain.model.SidebarItem
import com.minedu.gob.pe.enaprescalidad.ui.domain.usecase.GetSidebarItemsUseCase
import com.minedu.gob.pe.enaprescalidad.ui.navigation.NavigationManager
import com.minedu.gob.pe.enaprescalidad.ui.screens.login.sesion.SessionManager
import com.minedu.gob.pe.enaprescalidad.viewmodel.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import jakarta.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    private val getSidebarItems: GetSidebarItemsUseCase,

    ) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val currentTitle: StateFlow<String> = _uiState.map { state ->
        findTitleById(state.sidebarItems, state.selectedItemId)
            ?: "Control de Calidad de Datos"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = "Control de Calidad de Datos"
    )

    private fun findTitleById(items: List<SidebarItem>, id: String): String? {
        for (item in items) {
            if (item.id == id) return item.titleMenu
            val found = findTitleById(item.children, id)
            if (found != null) return found
        }
        return null
    }

    init {
        loadSidebarItems()
    }

    private fun loadSidebarItems() {
        viewModelScope.launch {
            getSidebarItems().collect { items ->
                _uiState.update { state ->
                    state.copy(
                        sidebarItems = items,
                        selectedItemId = items.firstOrNull()?.id ?: "",
                    )
                }
            }
        }
    }

    fun onLogout() {
        _uiState.update { it.copy(
            selectedItemId = "home",
            expandedItemIds = emptySet()
        )
        }
    }

    fun onItemSelected(itemId: String) {

        if (_uiState.value.selectedItemId == itemId) return

        _uiState.update { state ->
            // Expande todos los ancestros del item seleccionado
            val ancestors = findAncestorIds(state.sidebarItems, itemId)
            state.copy(
                selectedItemId = itemId,
                expandedItemIds = state.expandedItemIds + ancestors
            )
        }

        navigationManager.navigateTo(itemId)
    }

    // Busca recursivamente los ids padre del item seleccionado
    private fun findAncestorIds(
        items: List<SidebarItem>,
        targetId: String
    ): Set<String> {
        for (item in items) {
            if (item.id == targetId) return emptySet()
            val childResult = findAncestorIds(item.children, targetId)
            if (childResult != null || item.children.any { it.id == targetId }) {
                return (childResult ?: emptySet()) + item.id
            }
        }
        return emptySet()
    }

    fun onToggleExpand(id: String) {
        _uiState.update { current ->
            val newExpanded = if (id in current.expandedItemIds)
                current.expandedItemIds - id
            else
                current.expandedItemIds + id
            current.copy(expandedItemIds = newExpanded)
        }
    }

    fun onDrawerClosed() {
        _uiState.update { current ->
            current.copy(expandedItemIds = emptySet())
        }
    }
}