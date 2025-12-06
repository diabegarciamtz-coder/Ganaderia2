package mx.edu.utng.lojg.ganaderia20.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import mx.edu.utng.lojg.ganaderia20.data.entities.AnimalEntity
import mx.edu.utng.lojg.ganaderia20.data.entities.RegistroSaludEntity

@Dao
interface AnimalDao {

    // ==================== OPERACIONES DE ANIMALES ====================

    // ✅ Insertar animal
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAnimal(animal: AnimalEntity)

    // ✅ AGREGAR: Actualizar animal
    @Update
    suspend fun updateAnimal(animal: AnimalEntity)

    // ✅ Eliminar animal
    @Delete
    suspend fun eliminarAnimal(animal: AnimalEntity)

    // ✅ Obtener todos los animales (Flow)
    @Query("SELECT * FROM animal ORDER BY fechaNacimiento DESC")
    fun obtenerTodosLosAnimales(): Flow<List<AnimalEntity>>

    // ✅ AGREGAR: Alias para compatibilidad
    @Query("SELECT * FROM animal ORDER BY fechaNacimiento DESC")
    fun getAllAnimalesFlow(): Flow<List<AnimalEntity>>

    // ✅ AGREGAR: Obtener todos sin Flow
    @Query("SELECT * FROM animal ORDER BY fechaNacimiento DESC")
    suspend fun getAllAnimales(): List<AnimalEntity>

    // ✅ Obtener animales por usuario (usa usuarioId)
    @Query("SELECT * FROM animal WHERE usuarioId = :usuarioId ORDER BY fechaNacimiento DESC")
    fun obtenerAnimalesPorUsuario(usuarioId: String): Flow<List<AnimalEntity>>

    // ✅ AGREGAR: Alias para compatibilidad
    @Query("SELECT * FROM animal WHERE usuarioId = :usuarioId ORDER BY fechaNacimiento DESC")
    fun getAnimalesByUsuario(usuarioId: String): Flow<List<AnimalEntity>>

    // ✅ Obtener animales por adminId (para compartir entre empleados)
    @Query("SELECT * FROM animal WHERE adminId = :adminId ORDER BY fechaNacimiento DESC")
    suspend fun obtenerAnimalesPorAdminId(adminId: String): List<AnimalEntity>

    // ✅ Obtener animal por arete
    @Query("SELECT * FROM animal WHERE arete = :arete LIMIT 1")
    suspend fun obtenerAnimalPorArete(arete: String): AnimalEntity?

    // ✅ Actualizar peso
    @Query("UPDATE animal SET peso = :nuevoPeso WHERE arete = :arete")
    suspend fun actualizarPeso(arete: String, nuevoPeso: String)

    // ==================== REGISTROS DE SALUD ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRegistroSalud(registro: RegistroSaludEntity)

    @Query("SELECT * FROM registro_salud ORDER BY fecha DESC")
    fun obtenerTodosLosRegistros(): Flow<List<RegistroSaludEntity>>

    @Query("SELECT * FROM registro_salud WHERE areteAnimal = :arete ORDER BY fecha DESC")
    fun obtenerRegistrosPorAreteFlow(arete: String): Flow<List<RegistroSaludEntity>>

    @Query("SELECT * FROM registro_salud WHERE areteAnimal = :arete ORDER BY fecha ASC")
    suspend fun obtenerRegistrosPorArete(arete: String): List<RegistroSaludEntity>

    @Query("UPDATE registro_salud SET estado = :nuevoEstado WHERE id = :registroId")
    suspend fun actualizarEstadoRegistro(registroId: Int, nuevoEstado: String)

    @Delete
    suspend fun eliminarRegistroSalud(registro: RegistroSaludEntity)
}