package mx.edu.utng.lojg.ganaderia20.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mx.edu.utng.lojg.ganaderia20.data.dao.AnimalDao
import mx.edu.utng.lojg.ganaderia20.data.dao.RegistroSaludDao
import mx.edu.utng.lojg.ganaderia20.data.entities.AnimalEntity
import mx.edu.utng.lojg.ganaderia20.data.entities.RegistroSaludEntity

@Database(
    entities = [AnimalEntity::class, RegistroSaludEntity::class],
    version = 6, // ✅ INCREMENTADA para forzar recreación
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animalDao(): AnimalDao
    abstract fun registroSaludDao(): RegistroSaludDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ganaderia_database"
                )
                    // ✅ Esto elimina la BD antigua y crea una nueva automáticamente
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}