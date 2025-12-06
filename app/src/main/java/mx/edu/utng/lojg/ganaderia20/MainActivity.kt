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

class MainActivity : ComponentActivity() {

    // ✅ CORREGIDO: Agregar Factory para AuthViewModel
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