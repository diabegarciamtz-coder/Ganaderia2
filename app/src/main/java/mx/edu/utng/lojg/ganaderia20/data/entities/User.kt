package mx.edu.utng.lojg.ganaderia20.data.entities

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val rol: String = "usuario",
    val permisos: List<String> = emptyList(),
    val adminId: String? = null,
    val esDuenoRancho: Boolean = false,
    val fechaRegistro: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val activo: Boolean = true,
    val ultimoAcceso: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val codigoInvitacionUsado: String? = null
)