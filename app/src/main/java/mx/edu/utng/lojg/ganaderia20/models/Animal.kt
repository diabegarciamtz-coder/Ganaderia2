package mx.edu.utng.lojg.ganaderia20.models

data class Animal(
    val arete: String,
    val nombre: String,
    val tipo: String,
    val raza: String,
    val fechaNacimiento: String,
    val fecha: String, //registrar las fechas de cuando se toma el peso
    val peso: Double,
    val observacion: String = ""
)

data class EstadisticasSalud(
    val pesoActual: Double,
    val pesoPromedio: Double,
    val totalRegistros: Int,
    val ultimaRevision: String
)
