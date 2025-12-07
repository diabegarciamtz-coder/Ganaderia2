package mx.edu.utng.lojg.ganaderia20.ui.theme.screens

// screens/HistorialSaludGeneralScreen.kt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mx.edu.utng.lojg.ganaderia20.ui.theme.components.TarjetaSalud
import mx.edu.utng.lojg.ganaderia20.viewmodel.GanadoViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.runtime.setValue

/**
 * Pantalla Composable que muestra el historial completo de registros de salud de todo el ganado.
 *
 * Esta pantalla incluye una funcionalidad de búsqueda y filtrado de los registros de salud
 * (vacunaciones, tratamientos, diagnósticos) cargados desde [GanadoViewModel].
 *
 * @param navController El controlador de navegación para manejar la acción de volver.
 * @param viewModel El [GanadoViewModel] para acceder y gestionar la lista de [registrosSalud].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialSaludGeneralScreen(
    navController: NavController,
    viewModel: GanadoViewModel
) {
    // Estado que contiene la lista completa de registros de salud
    // FIX: Agregado initial = emptyList() para evitar crash en emulador nuevo
    val registrosSalud by viewModel.registrosSalud.collectAsState(initial = emptyList())
    // Estado local para almacenar la cadena de búsqueda del usuario
    var busqueda by remember { mutableStateOf("") }

    /**
     * Lista de registros de salud filtrados.
     * Se recalcula automáticamente cada vez que [registrosSalud] o [busqueda] cambian.
     * El filtro busca coincidencias (ignorando mayúsculas/minúsculas) en:
     * - Arete del animal (areteAnimal)
     * - Tipo de registro/enfermedad (tipo)
     * - Responsable (responsable)
     * - Tratamiento (tratamiento)
     */
    val registrosFiltrados = remember(registrosSalud, busqueda) {
        if (busqueda.isBlank()) {
            registrosSalud
        } else {
            registrosSalud.filter { registro ->
                registro.areteAnimal.contains(busqueda, ignoreCase = true) ||
                        registro.tipo.contains(busqueda, ignoreCase = true) ||
                        registro.responsable.contains(busqueda, ignoreCase = true) ||
                        registro.tratamiento.contains(busqueda, ignoreCase = true)
            }
        }
    }

    // Efecto para cargar todos los registros de salud la primera vez que se compone la pantalla
    LaunchedEffect(Unit) {
        viewModel.cargarTodosLosRegistros()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Historial General de Salud",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // CAMPO DE BÚSQUEDA (OutlinedTextField)
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar por arete, tipo o responsable") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                },
                trailingIcon = {
                    // Muestra el icono de limpiar solo si hay texto en la búsqueda
                    if (busqueda.isNotEmpty()) {
                        IconButton(onClick = { busqueda = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Indicador de resultados de búsqueda
            if (busqueda.isNotEmpty()) {
                Text(
                    "${registrosFiltrados.size} resultado(s) encontrado(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "Todos los registros de salud del ganado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Lógica de Estado Vacío (Empty State) ---

            // 1. No hay registros y la búsqueda está vacía (lista general vacía)
            if (registrosFiltrados.isEmpty() && busqueda.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Favorite, // Ícono genérico de salud/bienestar
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay registros de salud",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // 2. No hay registros, pero la búsqueda NO está vacía (cero resultados)
            else if (registrosFiltrados.isEmpty() && busqueda.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff, // Ícono de búsqueda sin resultados
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No se encontraron resultados",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // 3. Hay registros filtrados o la lista completa
            else {
                // Lista perezosa (LazyColumn) para mostrar los registros
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(registrosFiltrados) { registro ->
                        // Componente para mostrar la información de un registro de salud
                        TarjetaSalud(
                            registro = registro,
                            viewModel = viewModel,
                            onEstadoCambiado = {
                                // Recarga la lista completa si el estado de un registro cambia
                                viewModel.cargarTodosLosRegistros()
                            }
                        )
                    }
                }
            }
        }
    }
}