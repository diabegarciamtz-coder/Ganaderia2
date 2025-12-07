package mx.edu.utng.lojg.ganaderia20.ui.theme.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.edu.utng.lojg.ganaderia20.models.Animal
import mx.edu.utng.lojg.ganaderia20.data.entities.RegistroSaludEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Componente Composable que representa una tarjeta de resumen para el estado de salud de un animal.
 *
 * Muestra información básica del animal, el último registro de salud y botones de acción.
 *
 * @param animal El objeto [Animal] cuyos datos se van a mostrar.
 * @param registrosSalud La lista completa de [RegistroSaludEntity] disponibles en la vista. La función filtra los registros solo para este animal.
 * @param onVerDetalle Función lambda que se invoca al pulsar el botón "Ver Historial", pasando el arete del animal.
 * @param onAgregarRegistro Función lambda que se invoca al pulsar el botón "Nuevo Registro", pasando el arete del animal.
 */
@Composable
fun AnimalSaludCard(
    animal: Animal,
    registrosSalud: List<RegistroSaludEntity>,
    onVerDetalle: (String) -> Unit,
    onAgregarRegistro: (String) -> Unit
) {
    // Lógica interna para filtrar y encontrar el último registro del animal específico
    val registrosDelAnimal = registrosSalud.filter { it.areteAnimal == animal.arete }
    val ultimoRegistro = registrosDelAnimal.sortedByDescending { it.fecha }.firstOrNull()
    val totalRegistros = registrosDelAnimal.size

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header con información básica del animal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        animal.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Arete: ${animal.arete} • ${animal.tipo}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Indicador visual del estado de salud
                // ✅ CORREGIDO: Pasar solo los registros de este animal
                HealthStatusIndicator(registrosDelAnimal)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información de salud
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Registros: $totalRegistros",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (ultimoRegistro != null) {
                        Text(
                            "Último: ${formatearFecha(ultimoRegistro.fecha)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${ultimoRegistro.tipo}: ${ultimoRegistro.observaciones.take(30)}...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            "Sin registros de salud",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onAgregarRegistro(animal.arete) }
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Agregar registro",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo Registro")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { onVerDetalle(animal.arete) }
                ) {
                    Text("Ver Historial")
                }
            }
        }
    }
}

/**
 * Componente Composable que muestra un indicador visual del estado de salud de un animal.
 *
 * El estado se determina basado en la presencia de registros de tipo "Enfermedad" o registros "Pendientes".
 *
 * @param registros La lista de [RegistroSaludEntity] de un animal específico.
 */
@Composable
private fun HealthStatusIndicator(registros: List<RegistroSaludEntity>) {
    val tienePendientes = registros.any { it.estado == "Pendiente" }
    val tieneEnfermedades = registros.any {
        // Se considera enfermo si tiene un registro de tipo "Enfermedad" que no ha sido "Resuelto"
        it.tipo == "Enfermedad" && it.estado != "Resuelto"
    }

    val (color, icon, descripcion) = when {
        tieneEnfermedades -> Triple(
            MaterialTheme.colorScheme.error,
            Icons.Filled.Warning,
            "Enfermo"
        )
        tienePendientes -> Triple(
            MaterialTheme.colorScheme.onSurfaceVariant, // Color alternativo (e.g., gris/naranja)
            Icons.Filled.Schedule,
            "Pendiente"
        )
        registros.isNotEmpty() -> Triple(
            MaterialTheme.colorScheme.primary,
            Icons.Filled.CheckCircle,
            "Saludable"
        )
        else -> Triple(
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Filled.Help,
            "Sin datos"
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = descripcion,
            tint = color
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = descripcion,
            color = color,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Función auxiliar para formatear una cadena de fecha de 'yyyy-MM-dd' a 'dd/MM/yyyy'.
 *
 * Si el formato es inválido, devuelve la cadena original.
 *
 * @param fecha La cadena de fecha en formato 'yyyy-MM-dd'.
 * @return La cadena de fecha formateada a 'dd/MM/yyyy' o la cadena original en caso de error.
 */
private fun formatearFecha(fecha: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(fecha)
        outputFormat.format(date ?: fecha)
    } catch (e: Exception) {
        // En caso de error de parseo, devolver la fecha original
        fecha
    }
}