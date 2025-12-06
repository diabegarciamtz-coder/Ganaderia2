package mx.edu.utng.lojg.ganaderia20.Repository

import kotlinx.coroutines.flow.Flow
import mx.edu.utng.lojg.ganaderia20.data.dao.AnimalDao
import mx.edu.utng.lojg.ganaderia20.data.entities.AnimalEntity

class AnimalRepository(private val dao: AnimalDao) {

    // Flujo reactivo de animales (todos)
    val animales: Flow<List<AnimalEntity>> = dao.obtenerTodosLosAnimales()

    // Insertar animal
    suspend fun insertAnimal(animal: AnimalEntity) {
        dao.insertarAnimal(animal)
    }

    // Eliminar animal
    suspend fun deleteAnimal(animal: AnimalEntity) {
        dao.eliminarAnimal(animal)
    }

    // Lista por usuario (flujo)
    fun getAnimalesByUsuario(usuarioId: String): Flow<List<AnimalEntity>> {
        return dao.obtenerAnimalesPorUsuario(usuarioId)
    }

    // Actualizar animal completo
    suspend fun actualizarAnimal(animal: AnimalEntity) {
        // Como no tienes @Update en el DAO, eliminamos y reinsertamos
        dao.eliminarAnimal(animal)
        dao.insertarAnimal(animal)
    }

    // Obtener animal por arete
    suspend fun obtenerAnimalPorArete(arete: String): AnimalEntity? {
        return dao.obtenerAnimalPorArete(arete)
    }

    // Obtener todos los animales (sin Flow)
    suspend fun obtenerTodos(): List<AnimalEntity> {
        return dao.obtenerAnimalesPorAdminId("") // Esto no funcionará correctamente
    }

    // Obtener animales por adminId (para ver animales del mismo rancho)
    suspend fun obtenerAnimalesPorAdminId(adminId: String): List<AnimalEntity> {
        return dao.obtenerAnimalesPorAdminId(adminId)
    }
}