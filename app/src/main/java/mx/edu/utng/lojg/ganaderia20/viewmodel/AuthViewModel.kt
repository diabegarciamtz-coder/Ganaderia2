package mx.edu.utng.lojg.ganaderia20.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.lojg.ganaderia20.Repository.AuthRepository
import mx.edu.utng.lojg.ganaderia20.data.entities.User
import kotlinx.coroutines.tasks.await

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

// Variable para mantener la referencia al listener de Firestore
private var permisosListener: ListenerRegistration? = null

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> get() = _loginState

    // StateFlows para roles y permisos
    private val _permisosActuales = MutableStateFlow<List<String>>(emptyList())
    val permisosActuales: StateFlow<List<String>> = _permisosActuales.asStateFlow()

    private val _rolActual = MutableStateFlow<String>("usuario")
    val rolActual: StateFlow<String> = _rolActual.asStateFlow()


    fun login(userOrEmail: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val userFromDb = authRepository.getUserByUsernameOrEmail(userOrEmail)
                if (userFromDb == null) {
                    _loginState.value = LoginState.Error("Usuario no encontrado")
                    return@launch
                }
                val email = userFromDb.email
                val result = authRepository.loginWithEmailAndPassword(email, password)

                if (result.isSuccess) {
                    val firebaseUser = result.getOrThrow()
                    val user = authRepository.getUserByUid(firebaseUser.uid)
                    if (user != null) {
                        _loginState.value = LoginState.Success(user)
                        // Iniciar escucha de cambios en tiempo real
                        escucharCambiosPermisos()
                    } else {
                        _loginState.value = LoginState.Error("Error al cargar datos del usuario")
                    }
                } else {
                    val error = result.exceptionOrNull()
                    _loginState.value = LoginState.Error(traducirErrorFirebase(error))
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Error: ${e.message ?: "Error desconocido"}")
            }
        }
    }

    fun register(email: String, password: String, userData: User) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val result = authRepository.registerUser(email, password, userData)
                if (result.isSuccess) {
                    val user = result.getOrThrow()
                    _loginState.value = LoginState.Success(user)
                    // Iniciar escucha de cambios en tiempo real
                    escucharCambiosPermisos()
                } else {
                    _loginState.value = LoginState.Error(traducirErrorFirebase(result.exceptionOrNull()))
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Error en registro: ${e.message ?: "Error desconocido"}")
            }
        }
    }

    /**
     * Verifica y carga los permisos del usuario actual desde Firestore
     */
    fun verificarPermisos() {
        viewModelScope.launch {
            try {
                val uid = authRepository.getCurrentUser()?.uid
                if (uid == null) {
                    println("⚠️ No hay usuario autenticado")
                    return@launch
                }

                val db = FirebaseFirestore.getInstance()
                val documento = db.collection("usuarios").document(uid).get().await()

                if (documento.exists()) {
                    val permisos = documento.get("permisos") as? List<String> ?: emptyList()
                    val rol = documento.getString("rol") ?: "usuario"
                    val activo = documento.getBoolean("activo") ?: true

                    // Si la cuenta está inactiva, cerrar sesión
                    if (!activo) {
                        println("⚠️ Cuenta desactivada")
                        logout()
                        return@launch
                    }

                    // Actualizar los StateFlows
                    _permisosActuales.value = permisos
                    _rolActual.value = rol

                    println("✅ Permisos verificados: Rol=$rol, Permisos=$permisos")

                    // Iniciar listener para cambios en tiempo real
                    escucharCambiosPermisos()
                } else {
                    println("⚠️ Documento de usuario no encontrado")
                }
            } catch (e: Exception) {
                println("❌ Error verificando permisos: ${e.message}")
            }
        }
    }

    fun logout() {
        // Detener el listener antes de cerrar sesión
        detenerListenerPermisos()
        authRepository.logout()
        _loginState.value = LoginState.Idle
        _permisosActuales.value = emptyList()
        _rolActual.value = "usuario"
    }

    fun getCurrentUser(): User? {
        val firebaseUser = authRepository.getCurrentUser()
        return if (firebaseUser != null) {
            User(uid = firebaseUser.uid, email = firebaseUser.email ?: "")
        } else {
            null
        }
    }

    private fun traducirErrorFirebase(error: Throwable?): String {
        return when {
            error?.message?.contains("invalid-email") == true -> "Formato de correo inválido"
            error?.message?.contains("user-not-found") == true -> "Usuario no encontrado"
            error?.message?.contains("wrong-password") == true -> "Contraseña incorrecta"
            error?.message?.contains("network") == true -> "Error de conexión a internet"
            error?.message?.contains("invalid-credential") == true -> "Credenciales incorrectas"
            error?.message?.contains("user-disabled") == true -> "Cuenta deshabilitada"
            error?.message?.contains("too-many-requests") == true -> "Demasiados intentos. Intenta más tarde"
            error?.message?.contains("email-already-in-use") == true -> "El correo ya está registrado"
            error?.message?.contains("weak-password") == true -> "La contraseña es demasiado débil"
            error?.message?.contains("operation-not-allowed") == true -> "Operación no permitida"
            else -> error?.message ?: "Error desconocido"
        }
    }

    // ============================================================
    // GESTIÓN DE ROLES Y PERMISOS (CON TIEMPO REAL)
    // ============================================================

    /**
     * Escuchar cambios en los permisos y rol del usuario en tiempo real.
     */
    private fun escucharCambiosPermisos() {
        viewModelScope.launch {
            try {
                val uid = authRepository.getCurrentUser()?.uid ?: return@launch
                val db = FirebaseFirestore.getInstance()

                // Cancelar cualquier listener anterior para evitar duplicados
                detenerListenerPermisos()

                // Crear un nuevo listener para el documento del usuario
                permisosListener = db.collection("usuarios")
                    .document(uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            println("❌ Error escuchando cambios de permisos: ${error.message}")
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.exists()) {
                            val activo = snapshot.getBoolean("activo") ?: true
                            // Si la cuenta ha sido desactivada, cerrar sesión inmediatamente
                            if (!activo) {
                                println("⚠️ Cuenta desactivada. Cerrando sesión.")
                                logout()
                                return@addSnapshotListener
                            }

                            val permisos = snapshot.get("permisos") as? List<String> ?: emptyList()
                            val rol = snapshot.getString("rol") ?: "usuario"

                            // Actualizar los StateFlows, lo que refrescará la UI
                            _permisosActuales.value = permisos
                            _rolActual.value = rol

                            println("🔄 Permisos y rol actualizados en tiempo real: Rol=$rol, Permisos=$permisos")
                        }
                    }
            } catch (e: Exception) {
                println("❌ Error al configurar el listener de permisos: ${e.message}")
            }
        }
    }

    /**
     * Detiene la escucha de cambios de permisos para evitar fugas de memoria.
     */
    private fun detenerListenerPermisos() {
        permisosListener?.remove()
        permisosListener = null
        println("🚫 Listener de permisos detenido.")
    }

    fun tienePermiso(permiso: String): Boolean {
        return _permisosActuales.value.contains(permiso)
    }

    fun tieneAlgunPermiso(permisos: List<String>): Boolean {
        return permisos.any { _permisosActuales.value.contains(it) }
    }
}
