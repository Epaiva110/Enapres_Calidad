package com.minedu.gob.pe.encuestasatisfaccinenapres.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

//sealed interface Screen {
//    object Splash : Screen
//    object Login : Screen
//    object Main : Screen
//}

@Serializable
sealed class Routes : NavKey {
    @Serializable data object Splash : Routes()
    @Serializable data object Login : Routes()
    @Serializable data class Main (val supervisorId:String): Routes()

    @Serializable data class Welcome (val supervisorId:String): Routes()


    @Serializable data object Home : Routes()
    @Serializable data object Map : Routes()
}




