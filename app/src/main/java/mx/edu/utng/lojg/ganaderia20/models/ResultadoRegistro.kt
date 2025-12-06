package mx.edu.utng.lojg.ganaderia20.models

data class ResultadoRegistro(
    val exitoso: Boolean,
    val mensaje: String,
    val rolAsignado: String = "usuario"
)