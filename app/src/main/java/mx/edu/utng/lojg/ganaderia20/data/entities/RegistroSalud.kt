package mx.edu.utng.lojg.ganaderia20.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registro_salud")
data class RegistroSaludEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val areteAnimal: String,
    val fecha: String,
    val tipo: String,
    val tratamiento: String,
    val responsable: String,
    val observaciones: String,
    val estado: String = "Pendiente"
)