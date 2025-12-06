package mx.edu.utng.lojg.ganaderia20.data.entities

data class Permission(
    val id: String = "",
    val rol: String = "",
    val permiso: String = "",
    val acceso: Boolean = true,
    val descripcion: String = ""
)