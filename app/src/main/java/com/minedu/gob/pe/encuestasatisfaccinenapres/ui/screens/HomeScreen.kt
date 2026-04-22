package com.minedu.gob.pe.encuestasatisfaccinenapres.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.minedu.gob.pe.encuestasatisfaccinenapres.surveys.ui.SurveyScreen

//import com.minedu.gob.pe.encuestasatisfaccinenapres.ui.components.SideBar


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun HomeScreen() {
    val context = LocalContext.current

    // Usamos remember para que no lea el archivo cada vez que se recompone la pantalla
    val jsonFromAssets = remember {
        context.assets.open("encuesta_ejemplo.json").bufferedReader().use { it.readText() }
    }

    SurveyScreen(jsonString = jsonFromAssets)
}

@Preview
@Composable
fun pantalla (){

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()

            .background(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
        contentAlignment = Alignment.Center,
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = statusBarHeight,
                    bottom = statusBarHeight,
                    start = statusBarHeight,
                    end = navBarHeight
                )
                .background(
                    color = MaterialTheme.colorScheme.onPrimary
                ),
            contentAlignment = Alignment.Center,
        ) {

        }

    }
}