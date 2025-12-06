package mx.edu.utng.lojg.ganaderia20.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animal")
data class AnimalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val arete: String,
    val nombre: String,
    val tipo: String,
    val raza: String,
    val fechaNacimiento: String,
    val peso: String,
    val madre: String?,
    val padre: String?,
    val observaciones: String?,
    val estadoSalud: String,
    val foto: String?,

    val usuarioId: String,
    val adminId: String = usuarioId, // AGREGAR ESTO - por defecto igual a usuarioId
    val registradoPor: String? = null // AGREGAR ESTO - nombre de quien registró
)