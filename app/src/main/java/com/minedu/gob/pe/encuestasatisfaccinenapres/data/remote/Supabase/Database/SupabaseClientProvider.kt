package com.minedu.gob.pe.encuestasatisfaccinenapres.data.Online.Supabase.Database

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.createSupabaseClient

object SupabaseClientProvider {

    private const val SUPABASE_URL = "https://vofuwtljegyjajwjzlll.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_wWNTLpcXWobt0Bh7IMeopw_pJbxUGVi"

    // 'by lazy' significa que el cliente solo se creará la primera vez que se use
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Postgrest)
            // Aquí puedes instalar otros módulos como Auth o Storage en el futuro
        }
    }
}