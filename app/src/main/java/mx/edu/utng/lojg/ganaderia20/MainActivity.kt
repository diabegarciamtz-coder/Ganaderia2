package mx.edu.utng.lojg.ganaderia20

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import mx.edu.utng.lojg.ganaderia20.navigation.NavGraph
import mx.edu.utng.lojg.ganaderia20.ui.theme.GanaderoTheme
import androidx.room.Room
import mx.edu.utng.lojg.ganaderia20.Repository.AnimalRepository
import mx.edu.utng.lojg.ganaderia20.Repository.AuthRepository
import mx.edu.utng.lojg.ganaderia20.Repository.RegistroSaludRepository
import mx.edu.utng.lojg.ganaderia20.data.AppDatabase
import mx.edu.utng.lojg.ganaderia20.viewmodel.GanadoViewModel
import mx.edu.utng.lojg.ganaderia20.viewmodel.AuthViewModel

/**
 * [MainActivity] es el punto de entrada principal de la aplicación.
 *
 * Esta actividad configura el entorno Compose, inicializa la base de datos Room,
 * y crea las instancias de los ViewModels (`AuthViewModel` y `GanadoViewModel`)
 * utilizando Factorys para inyectar sus dependencias (Repositorios y DAOs).
 */
class MainActivity : ComponentActivity() {

    // --------------------------------------------------------------------------
    // 1. Inicialización de AuthViewModel (Manejo de Autenticación)
    // --------------------------------------------------------------------------

    /**
     * Inicializa el [AuthViewModel] utilizando `viewModels` delegado.
     * Se proporciona una [ViewModelProvider.Factory] personalizada para crear e inyectar
     * el [AuthRepository] necesario para la gestión de Firebase Auth.
     */
    private val authViewModel: AuthViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // Crear AuthRepository para AuthViewModel
                val authRepository = AuthRepository()
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(authRepository) as T
            }
        }
    }

    // --------------------------------------------------------------------------
    // 2. Inicialización de GanadoViewModel (Manejo de Datos de Ganado)
    // --------------------------------------------------------------------------

    /**
     * Inicializa el [GanadoViewModel] utilizando `viewModels` delegado.
     * Se proporciona una [ViewModelProvider.Factory] que se encarga de:
     * 1. Construir la base de datos Room ([AppDatabase]).
     * 2. Crear las instancias de [AnimalRepository] y [RegistroSaludRepository]
     * inyectando sus respectivos DAOs.
     * 3. Crear el [GanadoViewModel] con los repositorios de Room.
     */
    private val ganadoViewModel: GanadoViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, "ganaderia.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                val animalRepository = AnimalRepository(db.animalDao())
                val registroSaludRepository = RegistroSaludRepository(db.registroSaludDao())

                @Suppress("UNCHECKED_CAST")
                return GanadoViewModel(animalRepository, registroSaludRepository) as T
            }
        }
    }

    // --------------------------------------------------------------------------
    // 3. Ciclo de Vida: onCreate
    // --------------------------------------------------------------------------

    /**
     * Método llamado al crearse la actividad.
     * Configura el contenido de la interfaz de usuario utilizando Jetpack Compose.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GanaderoTheme {
                val navController = rememberNavController()
                // ✅ CORREGIDO: Pasar ambos ViewModels
                NavGraph(
                    navController = navController,
                    ganadoViewModel = ganadoViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}