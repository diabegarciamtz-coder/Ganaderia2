package mx.edu.utng.lojg.ganaderia20.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.entryModelOf
import mx.edu.utng.lojg.ganaderia20.viewmodel.GanadoViewModel
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.component.text.textComponent


/**
 * Pantalla Composable que muestra un reporte detallado de salud para un animal específico.
 *
 * Muestra información básica del animal, estadísticas clave de salud (peso actual, promedio),
 * una gráfica de evolución de peso usando la librería Vico, y el historial de pesajes registrados.
 *
 * @param navController El controlador de navegación para volver a la pantalla anterior o navegar a la actualización de peso.
 * @param arete El identificador único del animal (arete) cuyos datos se van a mostrar.
 * @param viewModel El [GanadoViewModel] que contiene y gestiona los datos de salud del animal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthReportScreen(
    navController: NavController,
    arete: String,
    viewModel: GanadoViewModel
) {
    // Estados observados del ViewModel
    val registrosPeso by viewModel.registrosPeso.collectAsState()
    val estadisticas by viewModel.estadisticasSalud.collectAsState()
    val animalSeleccionado by viewModel.animalSeleccionado

    // Cargar datos al iniciar la pantalla
    LaunchedEffect(arete) {
        viewModel.cargarAnimalPorArete(arete)
        viewModel.cargarHistorialPeso(arete)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reporte de Salud - $arete") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Sección 1: Información del animal ---
            item {
                animalSeleccionado?.let { animal ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        animal.nombre,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "${animal.tipo} - ${animal.raza}",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- Sección 2: Estadísticas generales de salud ---
            item {
                estadisticas?.let { stats ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonitorHeart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Estado de Salud", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatCard("Peso Actual", "%.1f kg".format(stats.pesoActual))
                                StatCard("Peso Promedio", "%.1f kg".format(stats.pesoPromedio))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Última revisión: ${stats.ultimaRevision}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // --- Sección 3: Gráfica de evolución de peso (Vico Chart) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ShowChart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Evolución de Peso", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (registrosPeso.isEmpty()) {
                            // Mensaje de estado vacío para la gráfica
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "No hay datos de peso suficientes",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Actualiza el peso del animal para ver la gráfica",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            // Preparar datos para la gráfica Vico
                            val chartEntryModel = entryModelOf(
                                registrosPeso.mapIndexed { index, registro ->
                                    // Mapea cada registro a un FloatEntry (x=índice, y=peso)
                                    FloatEntry(index.toFloat(), registro.peso.toFloat())
                                }
                            )

                            // Componente Chart de Vico
                            ProvideChartStyle {
                                Chart(
                                    chart = lineChart(
                                        lines = listOf(
                                            LineChart.LineSpec(
                                                lineColor = MaterialTheme.colorScheme.primary.hashCode(),
                                                lineBackgroundShader = null // Shader opcional
                                            )
                                        )
                                    ),
                                    model = chartEntryModel,

                                    // Eje Vertical (Start Axis)
                                    startAxis = rememberStartAxis(
                                        title = "Peso (kg)",
                                        titleComponent = textComponent(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            background = shapeComponent(Shapes.pillShape, MaterialTheme.colorScheme.surfaceVariant),
                                            padding = dimensionsOf(horizontal = 8.dp, vertical = 2.dp)
                                        ),
                                        label = textComponent {
                                            color = MaterialTheme.colorScheme.onSurface.hashCode()
                                        }
                                    ),

                                    // Eje Horizontal (Bottom Axis) con formateador de fecha
                                    bottomAxis = rememberBottomAxis(
                                        title = "Fecha",
                                        titleComponent = textComponent(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            background = shapeComponent(Shapes.pillShape, MaterialTheme.colorScheme.surfaceVariant),
                                            padding = dimensionsOf(horizontal = 8.dp, vertical = 2.dp)
                                        ),
                                        label = textComponent {
                                            color = MaterialTheme.colorScheme.onSurface.hashCode()
                                        },
                                        // Formatea el valor del índice X al inicio de la fecha (ej. "DD/MM")
                                        valueFormatter = { value, _ ->
                                            if (value.toInt() in registrosPeso.indices) {
                                                val fecha = registrosPeso[value.toInt()].fecha
                                                fecha.take(5) // Toma los primeros 5 caracteres (ej. "01/12")
                                            } else ""
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Leyenda
                            Text(
                                "📈 Registros: ${registrosPeso.size}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // --- Sección 4: Historial de registros de peso detallado ---
            if (registrosPeso.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Historial de Pesajes", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Muestra los registros en orden inverso (más reciente primero)
                            registrosPeso.reversed().forEach { registro ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            registro.fecha,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (registro.observacion.isNotEmpty()) {
                                            Text(
                                                registro.observacion,
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    Text(
                                        "%.1f kg".format(registro.peso),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                // Añade un divisor entre elementos, excepto después del último
                                if (registro != registrosPeso.last()) {
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }

            // --- Sección 5: Botón para actualizar peso ---
            item {
                Button(
                    onClick = {
                        // Navega a la pantalla de actualización de peso, pasando el arete
                        navController.navigate("actualizar_peso/$arete")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Scale, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Actualizar Peso")
                }
            }
        }
    }
}

/**
 * Componente Composable auxiliar para mostrar una tarjeta de estadística simple.
 *
 * Muestra un valor grande y una etiqueta debajo.
 *
 * @param label La etiqueta descriptiva de la estadística (ej. "Peso Actual").
 * @param value El valor de la estadística formateado como String (ej. "500.5 kg").
 */
@Composable
fun StatCard(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}