package mx.edu.utng.lojg.ganaderia20.Repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mx.edu.utng.lojg.ganaderia20.data.entities.User
import java.lang.Exception

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Función para login con email y contraseña
    suspend fun loginWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener usuario por username o email
    suspend fun getUserByUsernameOrEmail(usernameOrEmail: String): User? {
        return try {
            // Buscar por email
            val emailQuery = db.collection("usuarios")
                .whereEqualTo("email", usernameOrEmail)
                .get()
                .await()

            if (!emailQuery.isEmpty) {
                val document = emailQuery.documents[0]
                return User(
                    uid = document.id,
                    email = document.getString("email") ?: "",
                    username = document.getString("username") ?: "",
                    nombre = document.getString("nombre") ?: "",
                    rol = document.getString("rol") ?: "usuario"
                )
            }

            // Buscar por username
            val usernameQuery = db.collection("usuarios")
                .whereEqualTo("username", usernameOrEmail)
                .get()
                .await()

            if (!usernameQuery.isEmpty) {
                val document = usernameQuery.documents[0]
                return User(
                    uid = document.id,
                    email = document.getString("email") ?: "",
                    username = document.getString("username") ?: "",
                    nombre = document.getString("nombre") ?: "",
                    rol = document.getString("rol") ?: "usuario"
                )
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    // Obtener usuario por UID
    suspend fun getUserByUid(uid: String): User? {
        return try {
            val document = db.collection("usuarios").document(uid).get().await()
            if (document.exists()) {
                User(
                    uid = document.id,
                    email = document.getString("email") ?: "",
                    username = document.getString("username") ?: "",
                    nombre = document.getString("nombre") ?: "",
                    rol = document.getString("rol") ?: "usuario"
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Función para registro
    suspend fun registerUser(email: String, password: String, userData: User): Result<User> {
        return try {
            // Crear usuario en Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user!!.uid

            // Guardar datos adicionales en Firestore
            val userMap = mapOf(
                "email" to email,
                "username" to userData.username,
                "nombre" to userData.nombre,
                "rol" to userData.rol
            )

            db.collection("usuarios").document(uid).set(userMap).await()

            Result.success(userData.copy(uid = uid))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cerrar sesión
    fun logout() {
        auth.signOut()
    }

    // Obtener usuario actual
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}