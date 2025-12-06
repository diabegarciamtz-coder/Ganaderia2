package mx.edu.utng.lojg.ganaderia20.ui.theme.components

import androidx.compose.foundation.clickable
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaSalud(
    registro: RegistroSaludEntity,
    viewModel: GanadoViewModel,
    onEstadoCambiado: () -> Unit = {},
    onEditarClick: (RegistroSaludEntity) -> Unit = {} // ✅ NUEVO: Callback para editar
) {
    var estadoActual by remember { mutableStateOf(registro.estado) }
    var mostrarMenu by remember { mutableStateOf(false) }

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

                // ✅ MEJORADO: FilterChip con mejor feedback
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

            Spacer(Modifier.height(8.dp))

            // ✅ MEJORADO: Información organizada
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

            // ✅ NUEVO: Menú de opciones al hacer click
            if (mostrarMenu) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
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