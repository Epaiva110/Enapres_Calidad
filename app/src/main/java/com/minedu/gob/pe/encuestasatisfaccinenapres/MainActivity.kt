package com.minedu.gob.pe.encuestasatisfaccinenapres

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.room.Room

import com.minedu.gob.pe.encuestasatisfaccinenapres.navigation.AppNavigation
import com.minedu.gob.pe.pruebaaa.ui.theme.SupNacTheme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val supabase = createSupabaseClient(
        //        supabaseUrl = "https://vofuwtljegyjajwjzlll.supabase.co",
        //        supabaseKey = "sb_publishable_wWNTLpcXWobt0Bh7IMeopw_pJbxUGVi"
        //      ) {
        //        install(Postgrest)
        //    }

        setContent {
            SupNacTheme {
                AppNavigation()
            }
        }
    }
}


