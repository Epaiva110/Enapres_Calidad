package com.minedu.gob.pe.enaprescalidad.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.benchmark.perfetto.UiState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.MainViewModel
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
                    onLoginSuccess = { codigo ->
                        backStack.clear()
                        backStack.navigateTo(Routes.MainDynamic(codigo))
                    }
                )
            }

//            entry<Routes.MainDynamic> { key ->
//                MainScreenDinamic(
//                    id = key.supervisorId,
//                    backStack = backStack
//                )
//            }

            entry<Routes.MainDynamic> {
                MainDynamicScreen(
                    backStack = backStack
                )
            }
        }
    )
}