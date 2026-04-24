package com.minedu.gob.pe.encuestasatisfaccinenapres.viewmodel

import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Entity.Task

data class UsuarioUiState(
    val tasks: List<Task> = emptyList(),
)


