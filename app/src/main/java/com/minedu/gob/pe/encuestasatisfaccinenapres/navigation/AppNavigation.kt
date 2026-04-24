package com.minedu.gob.pe.encuestasatisfaccinenapres.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.minedu.gob.pe.encuestasatisfaccinenapres.navigation.core.ex.back
import com.minedu.gob.pe.encuestasatisfaccinenapres.ui.screens.LoginScreen
import com.minedu.gob.pe.encuestasatisfaccinenapres.ui.screens.SplashScreen
import com.minedu.gob.pe.encuestasatisfaccinenapres.navigation.core.ex.replace
import com.minedu.gob.pe.encuestasatisfaccinenapres.ui.screens.HomeScreen
import com.minedu.gob.pe.encuestasatisfaccinenapres.ui.screens.MainScreen
import com.minedu.gob.pe.encuestasatisfaccinenapres.ui.screens.MapVisor

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AppNavigation() {
    // Usamos tu función de inicialización
    val backStack = rememberNavBackStack(Routes.Splash)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.back() },
        entryProvider = entryProvider {

            // 1. SPLASH
            entry<Routes.Splash> {
                SplashScreen { nextRoute ->
                    backStack.replace(nextRoute)
                }
            }

            // 2. LOGIN
            entry<Routes.Login> {
                LoginScreen(onLoginSuccess = { codigo ->
                    backStack.replace(Routes.Main(supervisorId = codigo))
                    //backStack.navigateTo(Routes.Main(supervisorId))
                })
            }

            // 3. MAIN (Contenedor con SideBar)

            entry<Routes.Main> { key ->
                MainScreen(
                    id = key.supervisorId,
                    backStack = backStack, // El backstack que viene del NavDisplay
                    navigateBack = { backStack.back() }
                )
            }



            entry<Routes.Map> {
                MapVisor(backStack = backStack)
            }

            // 4. SUB-PANTALLAS (Para el contenido del Main)
            entry<Routes.Home> { HomeScreen() }

        }
    )
}