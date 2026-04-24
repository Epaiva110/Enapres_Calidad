package com.minedu.gob.pe.encuestasatisfaccinenapres

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.minedu.gob.pe.encuestasatisfaccinenapres.navigation.AppNavigation
import com.minedu.gob.pe.encuestasatisfaccinenapres.ui.screens.AddEditTaskScreen
import com.minedu.gob.pe.encuestasatisfaccinenapres.ui.screens.TaskListScreen
import com.minedu.gob.pe.encuestasatisfaccinenapres.viewmodel.TaskViewModel
import com.minedu.gob.pe.pruebaaa.ui.theme.SupNacTheme

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
                //TaskApp()
            }
        }
    }
}


@Composable
fun TaskApp() {
    val navController = rememberNavController()
    val viewModel: TaskViewModel = viewModel()

    NavHost(navController = navController, startDestination = "task_list") {
        composable("task_list") {
            TaskListScreen(
                viewModel = viewModel,
                onNavigateToAdd = { navController.navigate("add_task") },
                onNavigateToEdit = { id -> navController.navigate("edit_task/$id") }
            )
        }
        composable("add_task") {
            AddEditTaskScreen(
                viewModel = viewModel,
                taskId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "edit_task/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId")
            AddEditTaskScreen(
                viewModel = viewModel,
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}



