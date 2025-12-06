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
            TextButton(onClick = { }) {
                Text("Ver detalles")
            }
        }
    }
}

// Si ya tienes RegistroItem.kt, puedes omitir esta función
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaRegistroSalud(
    registro: RegistroSaludEntity,
    viewModel: GanadoViewModel,
    onEstadoCambiado: () -> Unit = {}
) {
    var estadoActual by remember { mutableStateOf(registro.estado) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
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
                        val nuevoEstado = if (estadoActual.lowercase() == "pendiente") "Realizado" else "Pendiente"
                        estadoActual = nuevoEstado
                        viewModel.actualizarEstadoRegistro(registro.id, nuevoEstado)
                        onEstadoCambiado()
                    },
                    label = {
                        Text(estadoActual)
                    },
                    colors = FilterChipDefaults.filterChipColors(
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