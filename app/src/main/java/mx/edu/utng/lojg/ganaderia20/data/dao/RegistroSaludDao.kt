package mx.edu.utng.lojg.ganaderia20.data.dao

import androidx.room.*
import mx.edu.utng.lojg.ganaderia20.data.entities.RegistroSaludEntity

@Dao
interface RegistroSaludDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRegistroSalud(registro: RegistroSaludEntity)

    @Query("SELECT * FROM registro_salud WHERE areteAnimal = :arete")
    suspend fun obtenerRegistrosPorArete(arete: String): List<RegistroSaludEntity>

    // ✅ NUEVO: Actualizar el estado de un registro
    @Query("UPDATE registro_salud SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: String)

    // ✅ NUEVO: Obtener registros por estado
    @Query("SELECT * FROM registro_salud WHERE estado = :estado")
    suspend fun obtenerRegistrosPorEstado(estado: String): List<RegistroSaludEntity>

    // ✅ NUEVO: Eliminar registro
    @Delete
    suspend fun eliminarRegistro(registro: RegistroSaludEntity)

    @Query("SELECT * FROM registro_salud ORDER BY fecha DESC")
    suspend fun obtenerTodos(): List<RegistroSaludEntity>


    @Update
    suspend fun actualizarRegistro(registro: RegistroSaludEntity)
}