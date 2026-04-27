package com.example.sidebarapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.enaprescalidad.ui.domain.usecase.GetSidebarItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getSidebarItems: GetSidebarItemsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadSidebarItems()
    }

    private fun loadSidebarItems() {
        viewModelScope.launch {
            getSidebarItems().collect { items ->
                _uiState.update { state ->
                    state.copy(
                        sidebarItems = items,
                        // Selecciona el primero por defecto
                        selectedItemId = items.firstOrNull()?.id ?: "",
                        //isLoading = false
                    )
                }
            }
        }
    }

    fun onItemSelected(itemId: String) {
        _uiState.update { it.copy(selectedItemId = itemId) }
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

    // MainViewModel
    fun onLogout() {
        _uiState.update { it.copy(
            selectedItemId = "home",
            expandedItemIds = emptySet()
        )
        }
    }
}
