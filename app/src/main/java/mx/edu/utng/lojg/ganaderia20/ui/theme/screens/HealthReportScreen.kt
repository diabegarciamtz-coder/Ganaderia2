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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthReportScreen(
    navController: NavController,
    arete: String,
    viewModel: GanadoViewModel
) {
    val registrosPeso by viewModel.registrosPeso.collectAsState()
    val estadisticas by viewModel.estadisticasSalud.collectAsState()
    val animalSeleccionado by viewModel.animalSeleccionado

    // Cargar datos
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
            // Información del animal
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

            // Estado general con estadísticas
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

            // Gráfica de evolución de peso
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
                            // Preparar datos para la gráfica
                            // CÓDIGO CORREGIDO
                            val chartEntryModel = entryModelOf(
                                registrosPeso.mapIndexed { index, registro ->
                                    // Se crea un objeto FloatEntry para cada punto de datos
                                    FloatEntry(index.toFloat(), registro.peso.toFloat())
                                }
                            )


                            ProvideChartStyle {
                                // ... justo después de `ProvideChartStyle {`

                                Chart(
                                    chart = lineChart(
                                        lines = listOf(
                                            LineChart.LineSpec(
                                                lineColor = MaterialTheme.colorScheme.primary.hashCode(),                    lineBackgroundShader = null
                                            )
                                        )
                                    ),
                                    model = chartEntryModel,

                                    // Todos los parámetros del eje Y están dentro de `rememberStartAxis`
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

                                    // El eje X (bottomAxis) ya estaba bien
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
                                        valueFormatter = { value, _ ->
                                            if (value.toInt() in registrosPeso.indices) {
                                                val fecha = registrosPeso[value.toInt()].fecha
                                                fecha.take(5)
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

            // Historial de registros de peso
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
                                if (registro != registrosPeso.last()) {
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }

            // Botón para actualizar peso
            item {
                Button(
                    onClick = {
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