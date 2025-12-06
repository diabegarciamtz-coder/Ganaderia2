package mx.edu.utng.lojg.ganaderia20.models

/**
 * Representa la cantidad de animales agrupados por raza
 */
data class BreedDistribution(
    val raza: String,
    val cantidad: Int
)