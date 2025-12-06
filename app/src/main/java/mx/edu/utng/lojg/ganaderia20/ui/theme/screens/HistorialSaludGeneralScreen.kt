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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialSaludGeneralScreen(
    navController: NavController,
    viewModel: GanadoViewModel
) {
    val registrosSalud by viewModel.registrosSalud.collectAsState()
    var busqueda by remember { mutableStateOf("") }

    // FILTRAR registros
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
            // CAMPO DE BÚSQUEDA
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar por arete, tipo o responsable") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                },
                trailingIcon = {
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

            if (registrosFiltrados.isEmpty() && busqueda.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Favorite,
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
            } else if (registrosFiltrados.isEmpty() && busqueda.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
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
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(registrosFiltrados) { registro ->
                        TarjetaSalud(
                            registro = registro,
                            viewModel = viewModel,
                            onEstadoCambiado = {
                                viewModel.cargarTodosLosRegistros()
                            }
                        )
                    }
                }
            }
        }
    }
}