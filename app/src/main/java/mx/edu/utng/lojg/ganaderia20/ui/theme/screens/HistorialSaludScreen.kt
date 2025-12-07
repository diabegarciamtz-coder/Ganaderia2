package mx.edu.utng.lojg.ganaderia20.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import mx.edu.utng.lojg.ganaderia20.ui.theme.components.TarjetaSalud
import mx.edu.utng.lojg.ganaderia20.viewmodel.GanadoViewModel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Clear
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.foundation.layout.size


/**
 * Pantalla Composable que muestra el historial de registros de salud detallado para un animal específico.
 *
 * Permite al usuario:
 * 1. Ver una lista de todos los registros de salud asociados al [arete] proporcionado.
 * 2. Buscar/filtrar registros por arete, tipo, tratamiento o responsable.
 * 3. Agregar un nuevo registro de salud mediante un diálogo.
 * 4. Volver a la pantalla anterior.
 *
 * Los registros se cargan al inicio usando [viewModel.cargarHistorial].
 *
 * @param navController El controlador de navegación para manejar la pila de pantallas.
 * @param arete El identificador único del animal (arete) cuyo historial se debe mostrar.
 * @param viewModel El [GanadoViewModel] para la gestión de datos de salud y persistencia.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialSaludScreen(
    navController: NavController,
    arete: String,
    viewModel: GanadoViewModel = viewModel()
) {
    var mostrarDialogoNuevoRegistro by remember { mutableStateOf(false) }
    var busqueda by remember { mutableStateOf("") }

    // Obtener los registros de salud del ViewModel (lista filtrada por el ViewModel al cargar)
    // FIX: Agregado initial = emptyList() para evitar crash en emulador nuevo
    val registros = viewModel.registrosSalud.collectAsState(initial = emptyList()).value

    /**
     * Lista de registros de salud filtrados localmente por la cadena [busqueda].
     * Se recalcula si la lista base [registros] o la [busqueda] cambian.
     */
    val registrosFiltrados = remember(registros, busqueda) {
        if (busqueda.isBlank()) {
            registros
        } else {
            registros.filter { registro ->
                registro.areteAnimal.contains(busqueda, ignoreCase = true) ||
                        registro.tipo.contains(busqueda, ignoreCase = true) ||
                        registro.tratamiento.contains(busqueda, ignoreCase = true) ||
                        registro.responsable.contains(busqueda, ignoreCase = true)
            }
        }
    }

    // Cargar el historial específico del animal cuando la pantalla se inicie o cambie el arete
    LaunchedEffect(arete) {
        viewModel.cargarHistorial(arete)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Salud - $arete") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { mostrarDialogoNuevoRegistro = true }
                    ) {
                        Icon(Icons.Default.Add, "Nuevo registro")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            // Mostrar cantidad de resultados si hay búsqueda
            if (busqueda.isNotEmpty()) {
                Text(
                    "${registrosFiltrados.size} resultado(s) encontrado(s)",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- Lógica de Estado Vacío (Empty State) ---

            // Estado vacío: No hay registros Y la búsqueda está vacía
            if (registrosFiltrados.isEmpty() && busqueda.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No hay registros de salud para este animal",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Estado vacío: No hay registros PERO la búsqueda NO está vacía (cero resultados)
            else if (registrosFiltrados.isEmpty() && busqueda.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No se encontraron resultados para \"$busqueda\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
            // Lista de registros
            else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(registrosFiltrados) { registro ->
                        // Componente TarjetaSalud para mostrar cada registro
                        TarjetaSalud(
                            registro = registro,
                            viewModel = viewModel,
                            onEstadoCambiado = {
                                // Recarga el historial del arete actual si se modifica un registro
                                viewModel.cargarHistorial(arete)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            // Botón flotante/inferior para agregar registro (duplicado para accesibilidad)
            Button(
                onClick = { mostrarDialogoNuevoRegistro = true },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar Registro")
            }
        }
    }

    // Muestra el diálogo para crear un nuevo registro
    if (mostrarDialogoNuevoRegistro) {
        DialogoNuevoRegistroSalud(
            onDismiss = { mostrarDialogoNuevoRegistro = false },
            onGuardar = { tipo, descripcion ->
                // Llama al ViewModel para guardar el registro con los datos del diálogo
                viewModel.agregarRegistroSaludSimple(
                    arete = arete,
                    tipo = tipo,
                    descripcion = descripcion
                )
                mostrarDialogoNuevoRegistro = false
                // No se necesita recarga explícita aquí si ViewModel usa StateFlow y la lista se actualiza automáticamente
            }
        )
    }
}


/**
 * Componente Composable que presenta un cuadro de diálogo (AlertDialog) para ingresar
 * un nuevo registro de salud simple (tipo y descripción).
 *
 * @param onDismiss Función lambda que se invoca al cancelar o cerrar el diálogo.
 * @param onGuardar Función lambda que se invoca al confirmar, pasando el tipo y la descripción.
 */
@Composable
fun DialogoNuevoRegistroSalud(
    onDismiss: () -> Unit,
    onGuardar: (tipo: String, descripcion: String) -> Unit
) {
    var tipo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Registro de Salud") },
        text = {
            Column {
                OutlinedTextField(
                    value = tipo,
                    onValueChange = { tipo = it },
                    label = { Text("Tipo (ej: Vacunación, Revisión)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Solo permite guardar si ambos campos están llenos
                    if (tipo.isNotBlank() && descripcion.isNotBlank()) {
                        onGuardar(tipo, descripcion)
                    }
                },
                enabled = tipo.isNotBlank() && descripcion.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}