package com.minedu.gob.pe.enaprescalidad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.enaprescalidad.ui.domain.model.SidebarItem
import com.minedu.gob.pe.enaprescalidad.ui.domain.usecase.GetSidebarItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

// SidebarUiState.kt
data class SidebarUiState(
    val items: List<SidebarItem> = emptyList(),
    val itemsFiltered: List<SidebarItem> = emptyList(),
    val selectedItemId: String = "home",
    val expandedItemIds: Set<String> = emptySet(),   // para submenús
    val isLoading: Boolean = false
)

// SidebarViewModel.kt
@HiltViewModel
class SidebarViewModel @Inject constructor(
    getSidebarItems: GetSidebarItemsUseCase
) : ViewModel() {

    val uiState: StateFlow<SidebarUiState> = getSidebarItems()
        .map { items -> SidebarUiState(items = items) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SidebarUiState(isLoading = true)
        )

    private val _selectedItemId = MutableStateFlow("home")
    val selectedItemId = _selectedItemId.asStateFlow()

    private val _expandedItemIds = MutableStateFlow<Set<String>>(emptySet())
    val expandedItemIds = _expandedItemIds.asStateFlow()

    fun onItemSelected(id: String) {
        _selectedItemId.value = id
    }

    fun onToggleExpand(id: String) {
        _expandedItemIds.update { current ->
            if (id in current) current - id else current + id
        }
    }
}