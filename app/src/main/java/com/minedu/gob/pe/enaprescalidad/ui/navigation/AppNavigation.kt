package com.minedu.gob.pe.enaprescalidad.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.minedu.gob.pe.enaprescalidad.ui.navigation.core.ex.back
import com.minedu.gob.pe.enaprescalidad.ui.navigation.core.ex.navigateTo
import com.minedu.gob.pe.enaprescalidad.ui.screens.login.LoginScreen
import com.minedu.gob.pe.enaprescalidad.ui.screens.splash.SplashScreen
import com.minedu.gob.pe.enaprescalidad.ui.navigation.core.ex.replace
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.MainDynamicScreen


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun AppNavigation() {

    val backStack = rememberNavBackStack(Routes.Splash)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.back() },
        entryProvider = entryProvider {

            entry<Routes.Splash> {
                SplashScreen { nextRoute ->
                    backStack.replace(nextRoute)
                }
            }

            entry<Routes.Login> {
                LoginScreen(
                    onLoginSuccess = {
                        backStack.clear()
                        backStack.navigateTo(
                            Routes.MainDynamic
                        )
                    }
                )
            }

            entry<Routes.MainDynamic> { route ->
                MainDynamicScreen(
                    backStack = backStack,
                )
            }
        }
    )
}