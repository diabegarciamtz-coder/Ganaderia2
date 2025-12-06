package mx.edu.utng.lojg.ganaderia20.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import mx.edu.utng.lojg.ganaderia20.ui.theme.screens.ActualizarPesoScreen
import mx.edu.utng.lojg.ganaderia20.ui.theme.screens.ConfiguracionScreen
import mx.edu.utng.lojg.ganaderia20.ui.theme.screens.DashboardScreen
import mx.edu.utng.lojg.ganaderia20.ui.theme.screens.HealthReportScreen
import mx.edu.utng.lojg.ganaderia20.ui.theme.screens.HistorialSaludGeneralScreen
import mx.edu.utng.lojg.ganaderia20.ui.theme.screens.HistorialSaludScreen
import mx.edu.utng.lojg.ganaderia20.ui.theme.screens.LoginScreen
import mx.edu.utng.lojg.ganaderia20.ui.theme.screens.PantallaMisAnimales
import mx.edu.utng.lojg.ganaderia20.ui.theme.screens.PantallaRegistrarCria
import mx.edu.utng.lojg.ganaderia20.ui.theme.screens.RegistrarSaludScreen
import mx.edu.utng.lojg.ganaderia20.ui.theme.screens.RegistroScreen
import mx.edu.utng.lojg.ganaderia20.ui.theme.screens.ReporteSaludScreen
import mx.edu.utng.lojg.ganaderia20.ui.theme.screens.ReportsScreen
import mx.edu.utng.lojg.ganaderia20.ui.theme.screens.UsuariosScreen
import mx.edu.utng.lojg.ganaderia20.viewmodel.AuthViewModel
import mx.edu.utng.lojg.ganaderia20.viewmodel.GanadoViewModel


@Composable
fun NavGraph(
    navController: NavHostController,
    ganadoViewModel: GanadoViewModel, // Renombrado para claridad
    authViewModel: AuthViewModel
) {
    NavHost(navController, startDestination = "login") {

        composable("login") {
            // Correcto: LoginScreen usa el AuthViewModel que viene de MainActivity
            LoginScreen(navController = navController, viewModel = authViewModel)
        }

        composable("registro") {
            // Correcto: RegistroScreen usa el AuthViewModel que viene de MainActivity
            RegistroScreen(navController = navController, viewModel = authViewModel)
        }

        composable("dashboard/{uid}/{rol}") { backStack ->
            val uid = backStack.arguments?.getString("uid") ?: ""
            val rol = backStack.arguments?.getString("rol") ?: "usuario"
            // Correcto: Dashboard usa el GanadoViewModel
            DashboardScreen(uid = uid, rol = rol, navController = navController, viewModel = ganadoViewModel,authViewModel = authViewModel)
        }

        composable("usuarios/{currentUserId}") { backStack ->
            val currentUserId = backStack.arguments?.getString("currentUserId") ?: ""
            // Corregido: UsuariosScreen debe usar el AuthViewModel para gestionar usuarios
            UsuariosScreen(navController = navController, viewModel = ganadoViewModel, currentUserId = currentUserId)
        }

        composable("mis_animales/{uid}/{rol}") { backStack ->
            val uid = backStack.arguments?.getString("uid") ?: ""
            val rol = backStack.arguments?.getString("rol") ?: "usuario"
            // Correcto: MisAnimales usa el GanadoViewModel
            PantallaMisAnimales(navController, ganadoViewModel, uid, rol)
        }

        composable("registrar_cria") {
            // Correcto: RegistrarCria usa el GanadoViewModel
            PantallaRegistrarCria(navController, ganadoViewModel)
        }

        composable("historial_salud_general") { // Ruta renombrada para claridad
            // Correcto: HistorialSaludGeneral usa el GanadoViewModel
            HistorialSaludGeneralScreen(navController, ganadoViewModel)
        }

        composable("historial_salud/{arete}") { backStack ->
            val arete = backStack.arguments?.getString("arete") ?: ""
            // Correcto: HistoricalSaludScreen usa el GanadoViewModel
            HistorialSaludScreen(navController, arete, ganadoViewModel)
        }

        composable("registrar_salud/{arete}") { backStack ->
            val arete = backStack.arguments?.getString("arete") ?: ""
            // Correcto: RegistrarSaludScreen usa el GanadoViewModel
            RegistrarSaludScreen(navController, arete, ganadoViewModel)
        }

        composable("reports") {
            // Le pasamos el ganadoViewModel que necesita para generar el reporte
            ReportsScreen(
                navController = navController, viewModel = ganadoViewModel
            )
        }

        composable("configuracion/{userId}/{userRole}") { backStack ->
            val userId = backStack.arguments?.getString("userId") ?: ""
            val userRole = backStack.arguments?.getString("userRole") ?: "usuario"
            // Correcto: ConfiguracionScreen usa el AuthViewModel para gestionar la cuenta
            ConfiguracionScreen(
                navController = navController,
                authViewModel = authViewModel,
                userId = userId,
                userRole = userRole
            )
        }

        // Se elimina la ruta "configuracion" sin parámetros para evitar ambigüedad,
        // ya que la pantalla de configuración casi siempre necesita el contexto del usuario.
        // Si fuera necesaria, se debe asegurar que se obtienen los datos del usuario de otra forma.


        composable("reporte_salud") {
            ReporteSaludScreen(navController = navController, viewModel = ganadoViewModel)
        }

        composable("health_report/{arete}") { backStack ->
            val arete = backStack.arguments?.getString("arete") ?: ""
            HealthReportScreen(
                navController = navController,
                viewModel = ganadoViewModel,
                arete = arete
            )
        }
        composable("actualizar_peso/{arete}") { backStack ->
            val arete = backStack.arguments?.getString("arete") ?: ""
            // Correcto: ActualizarPesoScreen usa el GanadoViewModel
            ActualizarPesoScreen(
                navController = navController,
                viewModel = ganadoViewModel,
                arete = arete
            )
        }

    }
}
