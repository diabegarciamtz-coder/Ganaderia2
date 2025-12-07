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


/**
 * Define y configura el grafo de navegación (NavGraph) de la aplicación.
 *
 * Esta función composable centraliza la definición de todas las rutas y las pantallas
 * asociadas, utilizando la biblioteca de navegación de Compose. Gestiona la inyección
 * de dependencias (View Models) necesarios para cada pantalla.
 *
 * @param navController El controlador de navegación que gestiona el estado de navegación.
 * @param ganadoViewModel El ViewModel para la gestión de datos del ganado (animales, salud, peso).
 * @param authViewModel El ViewModel para la gestión de la autenticación de usuarios (login, registro).
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    ganadoViewModel: GanadoViewModel,
    authViewModel: AuthViewModel
) {
    // Inicializa el NavHost, que es el contenedor de navegación.
    // La ruta de inicio (startDestination) es "login".
    NavHost(navController, startDestination = "login") {

        // --- Rutas de Autenticación ---

        composable("login") {
            // Pantalla de inicio de sesión.
            // Correcto: LoginScreen usa el AuthViewModel.
            LoginScreen(navController = navController, viewModel = authViewModel)
        }

        composable("registro") {
            // Pantalla de registro de nuevos usuarios.
            // Correcto: RegistroScreen usa el AuthViewModel.
            RegistroScreen(navController = navController, viewModel = authViewModel)
        }

        // --- Rutas Principales y de Dashboard ---

        composable("dashboard/{uid}/{rol}") { backStack ->
            // Pantalla principal (Dashboard) que requiere el ID de usuario (uid) y su rol.
            val uid = backStack.arguments?.getString("uid") ?: ""
            val rol = backStack.arguments?.getString("rol") ?: "usuario"
            // Correcto: Dashboard usa el GanadoViewModel y el AuthViewModel.
            DashboardScreen(uid = uid, rol = rol, navController = navController, viewModel = ganadoViewModel,authViewModel = authViewModel)
        }

        composable("usuarios/{currentUserId}") { backStack ->
            // Pantalla de gestión de usuarios (típicamente para administradores).
            // Requiere el ID del usuario actual (currentUserId) para permisos.
            val currentUserId = backStack.arguments?.getString("currentUserId") ?: ""
            // Corregido: UsuariosScreen usa el GanadoViewModel para listar usuarios y el currentUserId.
            UsuariosScreen(navController = navController, viewModel = ganadoViewModel, currentUserId = currentUserId)
        }

        composable("mis_animales/{uid}/{rol}") { backStack ->
            // Pantalla que muestra el listado de animales del usuario.
            // Requiere el ID de usuario (uid) y su rol.
            val uid = backStack.arguments?.getString("uid") ?: ""
            val rol = backStack.arguments?.getString("rol") ?: "usuario"
            // Correcto: MisAnimales usa el GanadoViewModel.
            PantallaMisAnimales(navController, ganadoViewModel, uid, rol)
        }

        composable("registrar_cria") {
            // Pantalla para registrar un nuevo animal (cría).
            // Correcto: RegistrarCria usa el GanadoViewModel.
            PantallaRegistrarCria(navController, ganadoViewModel)
        }

        // --- Rutas de Salud y Reportes ---

        composable("historial_salud_general") {
            // Pantalla para ver el historial de salud de todos los animales (vista general).
            // Correcto: HistorialSaludGeneral usa el GanadoViewModel.
            HistorialSaludGeneralScreen(navController, ganadoViewModel)
        }

        composable("historial_salud/{arete}") { backStack ->
            // Pantalla para ver el historial de salud de un animal específico.
            // Requiere el número de arete del animal.
            val arete = backStack.arguments?.getString("arete") ?: ""
            // Correcto: HistorialSaludScreen usa el GanadoViewModel.
            HistorialSaludScreen(navController, arete, ganadoViewModel)
        }

        composable("registrar_salud/{arete}") { backStack ->
            // Pantalla para registrar un nuevo evento de salud para un animal específico.
            // Requiere el número de arete del animal.
            val arete = backStack.arguments?.getString("arete") ?: ""
            // Correcto: RegistrarSaludScreen usa el GanadoViewModel.
            RegistrarSaludScreen(navController, arete, ganadoViewModel)
        }

        composable("reports") {
            // Pantalla de informes y estadísticas generales.
            // Le pasamos el ganadoViewModel que necesita para generar el reporte.
            ReportsScreen(
                navController = navController, viewModel = ganadoViewModel
            )
        }

        composable("reporte_salud") {
            // Pantalla de reportes específicos de salud (quizás un resumen o filtros).
            ReporteSaludScreen(navController = navController, viewModel = ganadoViewModel)
        }

        composable("health_report/{arete}") { backStack ->
            // Pantalla que muestra un reporte detallado de salud para un animal.
            // Requiere el número de arete del animal.
            val arete = backStack.arguments?.getString("arete") ?: ""
            HealthReportScreen(
                navController = navController,
                viewModel = ganadoViewModel,
                arete = arete
            )
        }

        // --- Ruta de Configuración y Peso ---

        composable("configuracion/{userId}/{userRole}") { backStack ->
            // Pantalla de configuración del usuario actual.
            // Requiere el ID de usuario (userId) y su rol (userRole).
            val userId = backStack.arguments?.getString("userId") ?: ""
            val userRole = backStack.arguments?.getString("userRole") ?: "usuario"
            // Correcto: ConfiguracionScreen usa el AuthViewModel.
            ConfiguracionScreen(
                navController = navController,
                authViewModel = authViewModel,
                userId = userId,
                userRole = userRole
            )
        }

        composable("actualizar_peso/{arete}") { backStack ->
            // Pantalla para registrar o actualizar el peso de un animal.
            // Requiere el número de arete del animal.
            val arete = backStack.arguments?.getString("arete") ?: ""
            // Correcto: ActualizarPesoScreen usa el GanadoViewModel.
            ActualizarPesoScreen(
                navController = navController,
                viewModel = ganadoViewModel,
                arete = arete
            )
        }

    }
}