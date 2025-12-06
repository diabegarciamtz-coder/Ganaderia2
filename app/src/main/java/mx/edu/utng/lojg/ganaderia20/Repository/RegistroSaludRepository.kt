package mx.edu.utng.lojg.ganaderia20.Repository

import mx.edu.utng.lojg.ganaderia20.data.dao.RegistroSaludDao
import mx.edu.utng.lojg.ganaderia20.data.entities.RegistroSaludEntity

class RegistroSaludRepository(private val dao: RegistroSaludDao) {
    suspend fun insertarRegistro(registro: RegistroSaludEntity) {
        dao.insertarRegistroSalud(registro)
    }

    suspend fun obtenerPorArete(arete: String): List<RegistroSaludEntity> {
        return dao.obtenerRegistrosPorArete(arete)
    }

    // ✅ MÉTODO PARA ACTUALIZAR ESTADO (usado por el FilterChip)
    suspend fun actualizarEstado(id: Int, estado: String) {
        dao.actualizarEstado(id, estado)
    }

    // ✅ NUEVO MÉTODO PARA ACTUALIZAR REGISTRO COMPLETO
    suspend fun actualizarRegistro(registro: RegistroSaludEntity) {
        dao.actualizarRegistro(registro)
    }

    suspend fun obtenerPorEstado(estado: String): List<RegistroSaludEntity> {
        return dao.obtenerRegistrosPorEstado(estado)
    }

    suspend fun eliminarRegistro(registro: RegistroSaludEntity) {
        dao.eliminarRegistro(registro)
    }

    suspend fun obtenerTodos(): List<RegistroSaludEntity> {
        return dao.obtenerTodos()
    }

}