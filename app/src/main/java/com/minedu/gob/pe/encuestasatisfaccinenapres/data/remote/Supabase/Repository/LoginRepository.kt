package com.minedu.gob.pe.encuestasatisfaccinenapres.data.Online.Supabase.Repository

import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Online.Supabase.Database.SupabaseClientProvider
import com.minedu.gob.pe.encuestasatisfaccinenapres.models.LoginResult
import com.minedu.gob.pe.encuestasatisfaccinenapres.models.Usuario
import io.github.jan.supabase.postgrest.from

class LoginRepository {

    // Accedemos directamente a la instancia única
    private val postgrest = SupabaseClientProvider.client.from("usuario")

    suspend fun login(usuarioInput: String, passwordInput: String): LoginResult {
        return try {
            val result = postgrest
                .select {
                    filter {
                        eq("usuario", usuarioInput)
                        eq("password", passwordInput)
                    }
                }
                .decodeList<Usuario>()

            if (result.isEmpty()) {
                LoginResult.Error("Usuario o contraseña incorrectos")
            } else {
                val user = result.first()
                if (!user.activo) LoginResult.Inactive(user)
                else LoginResult.Success(user)
            }
        } catch (e: Exception) {
            LoginResult.Error("Error de conexión: ${e.localizedMessage}")
        }
    }
}