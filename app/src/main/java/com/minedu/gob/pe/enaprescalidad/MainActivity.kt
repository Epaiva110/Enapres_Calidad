package com.minedu.gob.pe.enaprescalidad

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi


import com.minedu.gob.pe.enaprescalidad.ui.navigation.AppNavigation
import com.minedu.gob.pe.enaprescalidad.ui.theme.SupNacTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SupNacTheme {
                AppNavigation()
            }
        }
    }
}
