package mx.edu.utng.lojg.ganaderia20.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import mx.edu.utng.lojg.ganaderia20.data.entities.RegistroSaludEntity
import mx.edu.utng.lojg.ganaderia20.ui.theme.components.TarjetaSalud
import mx.edu.utng.lojg.ganaderia20.viewmodel.GanadoViewModel
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Clear
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.foundation.layout.size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialSaludScreen(
    navController: NavController,
    arete: String,
    viewModel: GanadoViewModel = viewModel()
) {
    var mostrarDialogoNuevoRegistro by remember { mutableStateOf(false) }
    var busqueda by remember { mutableStateOf("") }

    // Obtener los registros de salud del ViewModel
    val registros = viewModel.registrosSalud.collectAsState().value

    // FILTRAR registros por arete o descripción
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

    // Cargar el historial cuando la pantalla se inicie
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
            } else if (registrosFiltrados.isEmpty() && busqueda.isNotEmpty()) {
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
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    items(registrosFiltrados) { registro ->
                        TarjetaSalud(
                            registro = registro,
                            viewModel = viewModel,
                            onEstadoCambiado = {
                                viewModel.cargarHistorial(arete)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
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

    // Diálogo para nuevo registro
    if (mostrarDialogoNuevoRegistro) {
        DialogoNuevoRegistroSalud(
            onDismiss = { mostrarDialogoNuevoRegistro = false },
            onGuardar = { tipo, descripcion ->
                viewModel.agregarRegistroSaludSimple(
                    arete = arete,
                    tipo = tipo,
                    descripcion = descripcion
                )
                mostrarDialogoNuevoRegistro = false
            }
        )
    }
}

// ---------- CÓDIGO AÑADIDO ----------
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
