package com.minedu.gob.pe.enaprescalidad.data.repository

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.minedu.gob.pe.enaprescalidad.data.domain.Usuario
import com.minedu.gob.pe.enaprescalidad.data.local.database.datasource.UsuarioLocalDataSource
import com.minedu.gob.pe.enaprescalidad.data.local.entity.UsuarioEntity
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.datasource.UsuarioRemoteDataSource
import com.minedu.gob.pe.enaprescalidad.data.repository.mapper.toDomain
import com.minedu.gob.pe.enaprescalidad.utils.CryptoManager
import com.minedu.gob.pe.enaprescalidad.utils.formatDate
import com.minedu.gob.pe.enaprescalidad.utils.parseDate

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlin.String

@Singleton
class UsuarioRepository @Inject constructor(
    private val remote: UsuarioRemoteDataSource,
    private val local: UsuarioLocalDataSource,
    private val crypto: CryptoManager
) {

    suspend fun login(user: String, password: String, isOnline: Boolean, lastconnection: Long): LoginResult {
        return if (isOnline) {
            loginOnline(user, password, lastconnection)
        } else {
            loginOffline(user, password)
        }
    }

    private suspend fun loginOnline(user: String, password: String, lastconnection: Long): LoginResult {

        return try {

            val dto = remote.login(user, password)
                ?: return LoginResult.Error("Usuario o contraseña incorrectos")

            Log.i("Error000000000001", "dto: $dto")

            val domain = dto.toDomain()

            Log.i("Error000000000001", "domain: $domain")

            remote.update(user, lastconnection)

            try {
                saveUser(domain, password, lastconnection)
            } catch (e: Exception) {
                Log.e("Error_Save", "Error guardando en Room", e)
            }

            if (!domain.active) {
                LoginResult.Inactive(domain)
            } else {
                LoginResult.Success(domain)
            }

        } catch (e: Exception) {
            Log.e("Error_Save", "Error en login remoto", e)
            LoginResult.Error("Error de red o servidor")
        }
    }

    private suspend fun loginOffline(user: String, password: String): LoginResult {

        val localUser = local.get(user)
            ?: return LoginResult.Error("Sin datos offline")

        val decrypted = crypto.decrypt(localUser.password)
        val activo = localUser.active

        // 1000 ms = 1 segundo - 60 s = 1 minuto - 60 min = 1 hora - 24 h = 1 día

        val tiempo = (System.currentTimeMillis() - localUser.last_connection) / (1000 * 60 * 60 * 24)
        //val tiempo = (System.currentTimeMillis() - localUser.lastUpdated) / (1000 * 60)
        Log.i("Tiempo", "Tiempo: $tiempo")

        return if (tiempo > 30) {
            LoginResult.Error("Sesión expirada (30 días)")
        } else if (decrypted == password) {
            if (activo) {
                LoginResult.Success(localUser.toDomain())
            } else {
                LoginResult.Inactive(localUser.toDomain())
            }
        } else {
            LoginResult.Error("Credenciales incorrectas")
        }
    }

    private suspend fun saveUser(user: Usuario, password: String, lastconnection: Long) {
        val entity = UsuarioEntity(
            id = user.id,
            user = user.user,
            password = crypto.encrypt(password),
            active = user.active,
            user_name = user.user_name,
            role = user.role,
            last_connection = lastconnection
        )
        local.save(entity)
    }
    suspend fun logout() {
        local.logout()
    }
}

sealed class LoginResult {
    data class Success(val user: Usuario) : LoginResult()
    data class Inactive(val user: Usuario) : LoginResult()
    data class Error(val message: String) : LoginResult()
}