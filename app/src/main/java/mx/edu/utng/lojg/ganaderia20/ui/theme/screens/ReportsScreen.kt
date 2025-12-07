package mx.edu.utng.lojg.ganaderia20.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mx.edu.utng.lojg.ganaderia20.models.BreedDistribution
import mx.edu.utng.lojg.ganaderia20.models.InventoryItem
import kotlin.collections.forEach
import androidx.compose.ui.platform.LocalContext
import mx.edu.utng.lojg.ganaderia20.utils.ExportUtils
import mx.edu.utng.lojg.ganaderia20.viewmodel.GanadoViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width

/**
 * Pantalla Composable que proporciona una visión consolidada de los datos del inventario
 * ganadero, incluyendo estadísticas resumidas y opciones para exportar la información.
 *
 * ## Funcionalidades Principales
 * 1. **Cálculo de Estadísticas:** Calcula y muestra en tiempo real el inventario por tipo
 * (Vacas, Toros, Becerros) y la distribución por raza, utilizando la lista reactiva de [viewModel.animales].
 * 2. **Exportación de Datos:** Ofrece botones (actualmente solo PDF) para generar archivos
 * exportables de la lista de animales utilizando la utilidad [ExportUtils].
 * 3. **Feedback:** Muestra el estado de la exportación (cargando, éxito, error) mediante
 * mensajes condicionales y un indicador de progreso circular.
 * 4. **Resumen General:** Proporciona métricas rápidas como el total de animales y el peso promedio.
 *
 * @param navController El controlador de navegación (actualmente no utilizado para navegar).
 * @param viewModel La instancia del [GanadoViewModel] que proporciona la lista reactiva de animales.
 */

@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: GanadoViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Debug: Log para verificar que la pantalla se está cargando
    android.util.Log.d("ReportsScreen", "Pantalla iniciada")
    android.util.Log.d("ReportsScreen", "ViewModel: $viewModel")

    // Obtener animales del ViewModel
    val animales by viewModel.animales.collectAsState(initial = emptyList())

    android.util.Log.d("ReportsScreen", "Animales cargados: ${animales.size}")

    // Estados de carga/feedback
    var exporting by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    var tipoExportacion by remember { mutableStateOf("") }

    // Calcular estadísticas de forma segura
    val inventory = remember(animales) {
        listOf(
            InventoryItem("Vacas", animales.count { it.tipo.equals("Vaca", true) }),
            InventoryItem("Toros", animales.count { it.tipo.equals("Toro", true) }),
            InventoryItem("Becerros", animales.count { it.tipo.equals("Becerro", true) })
        )
    }

    val distribution = remember(animales) {
        animales.groupBy { it.raza }
            .map { (raza, lista) -> BreedDistribution(raza, lista.size) }
            .sortedByDescending { it.cantidad }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(modifier = Modifier.height(56.dp))
        Text("Reportes", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))

        // Inventario por tipo
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Inventario por Tipo", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                /*
                // Botón Descargar Excel
                Button(
                    onClick = {
                        if (animales.isEmpty()) {
                            exportMessage = "⚠️ No hay animales para exportar"
                            return@Button
                        }

                        exporting = true
                        tipoExportacion = "Excel"
                        exportMessage = null

                        coroutineScope.launch {
                            ExportUtils.generarExcelAnimales(
                                context = context,
                                animales = animales,
                                onSuccess = { file ->
                                    exporting = false
                                    exportMessage = "✅ Excel generado: ${file.name}"
                                    // Abrir el archivo
                                    ExportUtils.abrirArchivo(context, file)
                                },
                                onError = { error ->
                                    exporting = false
                                    exportMessage = "❌ $error"
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !exporting
                ) {
                    if (exporting && tipoExportacion == "Excel") {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (exporting && tipoExportacion == "Excel") "Generando..." else "Descargar Excel")
                }*/

                Spacer(modifier = Modifier.height(8.dp))
                inventory.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.tipo)
                        Text("${item.cantidad}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Distribución por raza
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Distribución por Raza", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (distribution.isNotEmpty()) {
                    distribution.forEach { d ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(d.raza)
                            Text("${d.cantidad}")
                        }
                    }
                } else {
                    Text("Sin datos", color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones de exportación
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Exportar Datos", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // Botón Descargar PDF
                Button(
                    onClick = {
                        if (animales.isEmpty()) {
                            exportMessage = "⚠️ No hay animales para exportar"
                            return@Button
                        }

                        exporting = true
                        tipoExportacion = "PDF"
                        exportMessage = null

                        coroutineScope.launch {
                            ExportUtils.generarPDFAnimales(
                                context = context,
                                animales = animales,
                                onSuccess = { file ->
                                    exporting = false
                                    exportMessage = "✅ PDF generado: ${file.name}"
                                    ExportUtils.abrirArchivo(context, file)
                                },
                                onError = { error ->
                                    exporting = false
                                    exportMessage = "❌ $error"
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !exporting
                ) {
                    if (exporting && tipoExportacion == "PDF") {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (exporting && tipoExportacion == "PDF") "Generando..." else "Descargar PDF")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mensaje de estado
                exportMessage?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                it.contains("✅") -> Color(0xFFDCFCE7)
                                it.contains("⚠️") -> Color(0xFFFEF3C7)
                                else -> Color(0xFFFEE2E2)
                            }
                        )
                    ) {
                        Text(
                            it,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resumen general - FIX: Cálculo seguro del peso promedio
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Resumen General", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Total de Animales: ${animales.size}")
                Spacer(modifier = Modifier.height(4.dp))

                // Cálculo seguro del peso promedio
                val pesoPromedio = remember(animales) {
                    val pesos = animales.mapNotNull { it.peso.toDoubleOrNull() }
                    if (pesos.isNotEmpty()) {
                        "%.2f kg".format(pesos.average())
                    } else {
                        "N/A"
                    }
                }
                Text("Peso promedio: $pesoPromedio")
            }
        }
    }
}