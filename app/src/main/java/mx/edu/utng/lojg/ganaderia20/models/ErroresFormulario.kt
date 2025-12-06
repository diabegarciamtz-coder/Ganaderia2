package mx.edu.utng.lojg.ganaderia20.models

data class ErroresFormulario(
    val nombre: String = "",
    val username: String = "",
    val correo: String = "",
    val telefono: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val codigoInvitacion: String = ""
)