package com.minedu.gob.pe.enaprescalidad.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Routes : NavKey {

    @Serializable data object Splash : Routes()
    @Serializable data object Login : Routes()
    @Serializable data class Welcome(val supervisorId: String) : Routes()
    @Serializable data class MainDynamic(val supervisorId: String) : Routes()

}


