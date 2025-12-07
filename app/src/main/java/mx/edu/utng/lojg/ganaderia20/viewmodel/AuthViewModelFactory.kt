package mx.edu.utng.lojg.ganaderia20.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.edu.utng.lojg.ganaderia20.Repository.AuthRepository

/**
 * Factory para la creación de instancias de [AuthViewModel].
 *
 * Esta clase es esencial para permitir que el [AuthViewModel] reciba dependencias
 * (como el [AuthRepository]) en su constructor. El marco de trabajo de Android
 * utiliza esta factoría para instanciar el ViewModel de forma segura y asociada
 * al ciclo de vida del componente (ej. una Activity o un Composable).
 *
 * @param repo La instancia del [AuthRepository] que se inyectará en el [AuthViewModel].
 */
class AuthViewModelFactory(
    private val repo: AuthRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
