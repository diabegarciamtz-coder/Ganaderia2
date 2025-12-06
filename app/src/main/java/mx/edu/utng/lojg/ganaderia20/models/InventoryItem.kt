package mx.edu.utng.lojg.ganaderia20.models

/**
 * Representa un tipo de animal dentro del inventario (por ejemplo: Vacas, Toros, Becerros)
 */
data class InventoryItem(
    val tipo: String,
    val cantidad: Int
)