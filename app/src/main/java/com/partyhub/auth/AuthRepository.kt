package com.partyhub.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Repositorio encargado de gestionar la autenticación con Firebase.
 * Sigue el patrón Singleton para asegurar una única instancia de FirebaseAuth.
 */
class AuthRepository(private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()) {

    /**
     * Obtiene el usuario actual si existe una sesión activa.
     */
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    /**
     * Registra un nuevo usuario con email y contraseña.
     * @return El usuario creado o lanza una excepción en caso de error.
     */
    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Error al crear el usuario: Usuario nulo"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error en el registro de usuario")
            Result.failure(e)
        }
    }

    /**
     * Inicia sesión con email y contraseña.
     */
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Error al iniciar sesión: Usuario nulo"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error en el inicio de sesión")
            Result.failure(e)
        }
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun signOut() {
        firebaseAuth.signOut()
        Timber.i("Sesión cerrada correctamente")
    }

    /**
     * Comprueba si el usuario está autenticado.
     */
    fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null
}
