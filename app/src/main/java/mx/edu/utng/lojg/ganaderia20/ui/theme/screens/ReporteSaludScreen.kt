package mx.edu.utng.lojg.ganaderia20.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import mx.edu.utng.lojg.ganaderia20.viewmodel.GanadoViewModel
import mx.edu.utng.lojg.ganaderia20.ui.theme.components.AnimalSaludCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteSaludScreen(
    navController: NavController,
    viewModel: GanadoViewModel
) {
    val animales by viewModel.animales.collectAsState()
    val registrosSalud by viewModel.registrosSalud.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarTodosLosRegistros()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reporte General de Salud") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (animales.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No hay animales registrados",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Animales y su Estado de Salud",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(animales) { animal ->
                    AnimalSaludCard(
                        animal = mx.edu.utng.lojg.ganaderia20.models.Animal(
                            arete = animal.arete,
                            nombre = animal.nombre,
                            tipo = animal.tipo,
                            raza = animal.raza,
                            fechaNacimiento = animal.fechaNacimiento,
                            fecha = "",
                            peso = animal.peso.toDoubleOrNull() ?: 0.0,
                            observacion = animal.observaciones ?: ""
                        ),
                        registrosSalud = registrosSalud,
                        onVerDetalle = { arete ->
                            navController.navigate("historial_salud/$arete")
                        },
                        onAgregarRegistro = { arete ->
                            navController.navigate("registrar_salud/$arete")
                        }
                    )
                }
            }
        }
    }
}