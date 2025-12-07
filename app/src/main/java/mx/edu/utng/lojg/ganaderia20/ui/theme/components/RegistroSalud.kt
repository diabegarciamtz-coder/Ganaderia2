package mx.edu.utng.lojg.ganaderia20.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.edu.utng.lojg.ganaderia20.data.entities.RegistroSaludEntity
import mx.edu.utng.lojg.ganaderia20.viewmodel.GanadoViewModel

/**
 * Componente Composable que presenta un registro de salud individual en formato de tarjeta.
 *
 * Muestra los detalles del registro (tipo, tratamiento, observaciones, fecha, responsable)
 * y permite cambiar el estado (Pendiente/Realizado) directamente a través de un [FilterChip].
 * La tarjeta cambia de color según el estado actual del registro.
 *
 * @param registro La entidad [RegistroSaludEntity] que contiene todos los datos del registro de salud.
 * @param viewModel El [GanadoViewModel] necesario para interactuar y actualizar el estado en la base de datos.
 * @param onEstadoCambiado Función lambda que se invoca después de que el estado del registro es actualizado
 * en la base de datos, útil para refrescar la vista. Por defecto es vacía.
 * @param onEditarClick Función lambda que se invoca al pulsar el botón "Editar", pasando la entidad
 * [RegistroSaludEntity] completa para ser modificada. Por defecto es vacía.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaSalud(
    registro: RegistroSaludEntity,
    viewModel: GanadoViewModel,
    onEstadoCambiado: () -> Unit = {},
    onEditarClick: (RegistroSaludEntity) -> Unit = {}
) {
    // Estado interno para manejar el estado del registro en la UI antes de la actualización
    var estadoActual by remember { mutableStateOf(registro.estado) }
    // Estado para controlar la visibilidad del menú de opciones (Editar/Cerrar)
    var mostrarMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            // FIX: Usar clickable con interactionSource y indication de Material 3
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ) {
                mostrarMenu = !mostrarMenu
            },
        colors = CardDefaults.cardColors(
            // El color del contenedor se define según el estado actual
            containerColor = when (estadoActual.lowercase()) {
                "pendiente" -> MaterialTheme.colorScheme.errorContainer // Rojo suave para urgencia
                "realizado" -> MaterialTheme.colorScheme.primaryContainer // Color primario suave para completado
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {

        Column(modifier = Modifier.padding(12.dp)) {
            // Fila superior con tipo y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        registro.tipo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    // ✅ NUEVO: Mostrar arete del animal
                    Text(
                        "Animal: ${registro.areteAnimal}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Componente interactivo para cambiar el estado
                // ✅ MEJORADO: FilterChip con mejor feedback
                FilterChip(
                    selected = true,
                    onClick = {
                        val nuevoEstado = if (estadoActual.lowercase() == "pendiente") "Realizado" else "Pendiente"
                        estadoActual = nuevoEstado
                        // Actualiza el estado en la base de datos
                        viewModel.actualizarEstadoRegistro(registro.id, nuevoEstado)
                        // Llama al callback para notificar a la vista superior
                        onEstadoCambiado()
                    },
                    label = {
                        Text(estadoActual)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        // Los colores del Chip reflejan el estado de forma más prominente
                        selectedContainerColor = when (estadoActual.lowercase()) {
                            "pendiente" -> MaterialTheme.colorScheme.error
                            "realizado" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.secondary
                        },
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            // Información detallada del registro
            // ✅ MEJORADO: Información organizada con emojis
            Column {
                if (registro.tratamiento.isNotBlank()) {
                    Text("💊 Tratamiento: ${registro.tratamiento}")
                }
                if (registro.responsable.isNotBlank()) {
                    Text("👨‍⚕️ Responsable: ${registro.responsable}")
                }
                if (registro.observaciones.isNotBlank()) {
                    Text("📝 Observaciones: ${registro.observaciones}")
                }
                Text(
                    "📅 ${registro.fecha}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Opciones de acción condicionales (Editar/Cerrar)
            // ✅ NUEVO: Menú de opciones al hacer click
            if (mostrarMenu) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            // Llama al callback de edición
                            onEditarClick(registro)
                            mostrarMenu = false
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Editar")
                    }
                    OutlinedButton(
                        onClick = { mostrarMenu = false }
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}