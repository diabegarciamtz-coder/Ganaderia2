package mx.edu.utng.lojg.ganaderia20.Repository

import mx.edu.utng.lojg.ganaderia20.data.dao.RegistroSaludDao
import mx.edu.utng.lojg.ganaderia20.data.entities.RegistroSaludEntity

/**
 * Repositorio de datos para la gestión de registros de salud de los animales.
 *
 * Esta clase actúa como una capa de abstracción entre las fuentes de datos (en este caso, [RegistroSaludDao])
 * y el resto de la aplicación (ViewModel), manejando las operaciones de datos.
 *
 * @property dao La interfaz DAO (Data Access Object) utilizada para acceder a la base de datos de registros de salud.
 */
class RegistroSaludRepository(private val dao: RegistroSaludDao) {

    /**
     * Inserta un nuevo registro de salud en la base de datos.
     *
     * @param registro La entidad [RegistroSaludEntity] a ser insertada.
     */
    suspend fun insertarRegistro(registro: RegistroSaludEntity) {
        dao.insertarRegistroSalud(registro)
    }

    /**
     * Obtiene todos los registros de salud asociados a un animal específico.
     *
     * @param arete El identificador (arete) del animal cuyos registros se desean obtener.
     * @return Una lista de entidades [RegistroSaludEntity] correspondientes al arete especificado.
     */
    suspend fun obtenerPorArete(arete: String): List<RegistroSaludEntity> {
        return dao.obtenerRegistrosPorArete(arete)
    }

    /**
     * Actualiza el estado de un registro de salud específico utilizando su ID.
     *
     * Este método es útil para marcar el estado de seguimiento (e.g., completado, pendiente).
     *
     * @param id El ID único del registro de salud a actualizar.
     * @param estado El nuevo valor del estado (String) para el registro.
     */
    // ✅ MÉTODO PARA ACTUALIZAR ESTADO (usado por el FilterChip)
    suspend fun actualizarEstado(id: Int, estado: String) {
        dao.actualizarEstado(id, estado)
    }

    /**
     * Actualiza todos los campos de un registro de salud existente en la base de datos.
     *
     * @param registro La entidad [RegistroSaludEntity] con los datos actualizados.
     */
    // ✅ NUEVO MÉTODO PARA ACTUALIZAR REGISTRO COMPLETO
    suspend fun actualizarRegistro(registro: RegistroSaludEntity) {
        dao.actualizarRegistro(registro)
    }

    /**
     * Obtiene todos los registros de salud que coinciden con un estado específico.
     *
     * Este método puede ser usado para filtrar registros por estado de seguimiento (e.g., "Pendiente").
     *
     * @param estado El estado (String) por el cual se desean filtrar los registros.
     * @return Una lista de entidades [RegistroSaludEntity] que cumplen con el estado.
     */
    suspend fun obtenerPorEstado(estado: String): List<RegistroSaludEntity> {
        return dao.obtenerRegistrosPorEstado(estado)
    }

    /**
     * Elimina un registro de salud específico de la base de datos.
     *
     * @param registro La entidad [RegistroSaludEntity] a ser eliminada.
     */
    suspend fun eliminarRegistro(registro: RegistroSaludEntity) {
        dao.eliminarRegistro(registro)
    }

    /**
     * Obtiene todos los registros de salud presentes en la base de datos.
     *
     * @return Una lista completa de todas las entidades [RegistroSaludEntity].
     */
    suspend fun obtenerTodos(): List<RegistroSaludEntity> {
        return dao.obtenerTodos()
    }

}