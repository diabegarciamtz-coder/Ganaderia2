package mx.edu.utng.lojg.ganaderia20.ui.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.edu.utng.lojg.ganaderia20.data.entities.AnimalEntity
import mx.edu.utng.lojg.ganaderia20.data.entities.RegistroSaludEntity
import mx.edu.utng.lojg.ganaderia20.viewmodel.GanadoViewModel

/**
 * Componente Composable que muestra información básica de un animal en formato de tarjeta.
 *
 * Incluye el arete, nombre, tipo, raza y fecha de nacimiento del animal.
 * Actualmente incluye un botón de "Ver detalles" sin funcionalidad asignada.
 *
 * @param animal La entidad [AnimalEntity] con los datos del animal a mostrar.
 */
@Composable
fun TarjetaAnimal(animal: AnimalEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Arete: ${animal.arete}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text("Nombre: ${animal.nombre}")
            Text("Tipo: ${animal.tipo}")
            Text("Raza: ${animal.raza}")
            Text("Fecha Nac.: ${animal.fechaNacimiento}")
            TextButton(onClick = { /* TODO: Implementar navegación a detalles */ }) {
                Text("Ver detalles")
            }
        }
    }
}

// Si ya tienes RegistroItem.kt, puedes omitir esta función
/**
 * Componente Composable que presenta un registro de salud individual en formato de tarjeta.
 *
 * Permite cambiar el estado del registro (Pendiente/Realizado) a través de un [FilterChip]
 * y actualiza la base de datos mediante el ViewModel. El color de la tarjeta varía
 * según el estado del registro.
 *
 * @param registro La entidad [RegistroSaludEntity] con los datos del registro de salud.
 * @param viewModel El [GanadoViewModel] utilizado para actualizar el estado del registro en la BD.
 * @param onEstadoCambiado Función lambda que se invoca después de que el estado es actualizado
 * en la base de datos, útil para forzar un refresco de la lista. Por defecto es vacía.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaRegistroSalud(
    registro: RegistroSaludEntity,
    viewModel: GanadoViewModel,
    onEstadoCambiado: () -> Unit = {}
) {
    // Estado mutable local para reflejar el estado del registro en la UI
    var estadoActual by remember { mutableStateOf(registro.estado) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            // Asigna color de fondo basado en el estado
            containerColor = when (estadoActual.lowercase()) {
                "pendiente" -> MaterialTheme.colorScheme.errorContainer
                "realizado" -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    registro.tipo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                FilterChip(
                    selected = true,
                    onClick = {
                        // Lógica para alternar el estado
                        val nuevoEstado = if (estadoActual.lowercase() == "pendiente") "Realizado" else "Pendiente"
                        estadoActual = nuevoEstado
                        // Actualiza el estado en el ViewModel (y por ende, en la BD)
                        viewModel.actualizarEstadoRegistro(registro.id, nuevoEstado)
                        // Llama al callback de notificación
                        onEstadoCambiado()
                    },
                    label = {
                        Text(estadoActual)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        // Asigna color del chip basado en el estado
                        selectedContainerColor = when (estadoActual.lowercase()) {
                            "pendiente" -> MaterialTheme.colorScheme.error
                            "realizado" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.secondary
                        },
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            Text("Tratamiento: ${registro.tratamiento}")

            if (registro.observaciones.isNotBlank()) {
                Text("Observaciones: ${registro.observaciones}")
            }

            Text(
                "Fecha: ${registro.fecha}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (registro.responsable.isNotBlank()) {
                Text(
                    "Responsable: ${registro.responsable}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}