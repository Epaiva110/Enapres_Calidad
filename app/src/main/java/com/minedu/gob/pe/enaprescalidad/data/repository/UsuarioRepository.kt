package com.minedu.gob.pe.enaprescalidad.data.repository

import android.util.Log
import com.minedu.gob.pe.enaprescalidad.data.domain.Usuario
import com.minedu.gob.pe.enaprescalidad.data.local.database.datasource.UsuarioLocalDataSource
import com.minedu.gob.pe.enaprescalidad.data.local.entity.UsuarioEntity
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.datasource.UsuarioRemoteDataSource
import com.minedu.gob.pe.enaprescalidad.data.repository.mapper.toDomain
import com.minedu.gob.pe.enaprescalidad.utils.CryptoManager

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsuarioRepository @Inject constructor(
    private val remote: UsuarioRemoteDataSource,
    private val local: UsuarioLocalDataSource,
    private val crypto: CryptoManager
) {

    suspend fun login(usuario: String, password: String, isOnline: Boolean): LoginResult {
        return if (isOnline) {
            loginOnline(usuario, password)
        } else {
            loginOffline(usuario, password)
        }
    }

    private suspend fun loginOnline(usuario: String, password: String): LoginResult {

        Log.d("Error_Save", "Iniciando proceso de login...")

        return try {

            val dto = remote.login(usuario, password)
                ?: return LoginResult.Error("Usuario o contraseña incorrectos")

            Log.d("Error_Save", "PASO 01")

            val domain = dto.toDomain()

            Log.d("Error_Save", "PASO 02")

            try {
                saveUser(domain, password)
            } catch (e: Exception) {
                Log.e("Error_Save", "Error guardando en Room", e)
            }

            Log.d("Error_Save", "PASO 03")

            if (!domain.activo) {
                LoginResult.Inactive(domain)
            } else {
                LoginResult.Success(domain)
            }

        } catch (e: Exception) {
            Log.e("Error_Save", "Error en login remoto", e)
            LoginResult.Error("Error de red o servidor")
        }
    }

    private suspend fun loginOffline(usuario: String, password: String): LoginResult {

        val localUser = local.get(usuario)
            ?: return LoginResult.Error("Sin datos offline")

        val decrypted = crypto.decrypt(localUser.password)
        val activo = localUser.activo

        // 1000 ms = 1 segundo - 60 s = 1 minuto - 60 min = 1 hora - 24 h = 1 día

        val tiempo = (System.currentTimeMillis() - localUser.lastUpdated) / (1000 * 60 * 60 * 24)
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

    private suspend fun saveUser(user: Usuario, password: String) {

        val entity = UsuarioEntity(
            usuario = user.usuario,
            password = crypto.encrypt(password),
            activo = user.activo,
            lastUpdated = System.currentTimeMillis()
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