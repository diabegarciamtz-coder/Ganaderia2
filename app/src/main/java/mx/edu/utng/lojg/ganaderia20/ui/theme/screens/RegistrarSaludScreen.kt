package mx.edu.utng.lojg.ganaderia20.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import mx.edu.utng.lojg.ganaderia20.viewmodel.GanadoViewModel

// ---------------------------------------------------------
// PANTALLA: REGISTRAR NUEVO EVENTO DE SALUD
// ---------------------------------------------------------
@Composable
fun RegistrarSaludScreen(
    navController: NavController,
    arete: String,
    viewModel: GanadoViewModel
) {
    // Estados locales para los campos
    var fecha by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var tratamiento by remember { mutableStateOf("") }
    var responsable by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var mostrarError by remember { mutableStateOf(false) }

    // Lista de tipos de tratamiento para autocompletar
    val tiposTratamiento = listOf(
        "Vacunación", "Desparasitación", "Vitaminización", "Curacion",
        "Revisión General", "Parto", "Celo", "Enfermedad", "Cirugía", "Otro"
    )

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "Nuevo Registro de Salud 🩺 - $arete",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha (dd/mm/aaaa)") },
                placeholder = {  },
                modifier = Modifier.fillMaxWidth(),
                isError = mostrarError && fecha.isBlank()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = tipo,
                onValueChange = {
                    tipo = it
                    mostrarError = false
                },
                label = { Text("Tipo de tratamiento") },
                placeholder = { Text("Ej: Vacunación, Desparasitación...") },
                modifier = Modifier.fillMaxWidth(),
                isError = mostrarError && tipo.isBlank(),
                supportingText = {
                    if (mostrarError && tipo.isBlank()) {
                        Text("Este campo es obligatorio")
                    } else if (tipo.isNotBlank() && tiposTratamiento.any { it.contains(tipo, true) }) {
                        Text("Sugerencia: ${tiposTratamiento.find { it.contains(tipo, true) }}")
                    }
                }
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = tratamiento,
                onValueChange = { tratamiento = it },
                label = { Text("Tratamiento aplicado") },
                placeholder = { Text("Describe el tratamiento o medicamento") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = responsable,
                onValueChange = {
                    responsable = it
                    mostrarError = false
                },
                label = { Text("Veterinario / Responsable *") },
                placeholder = { Text("Nombre del veterinario o responsable") },
                modifier = Modifier.fillMaxWidth(),
                isError = mostrarError && responsable.isBlank(),
                supportingText = {
                    if (mostrarError && responsable.isBlank()) {
                        Text("Este campo es obligatorio")
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Mostrar mensaje de error
            if (mostrarError) {
                Text(
                    "⚠️ Complete los campos obligatorios",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(20.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { navController.popBackStack() }) {
                    Text("Cancelar")
                }

                Button(onClick = {
                    // Validación más estricta
                    val camposObligatorios = listOf(fecha, tipo, responsable)
                    val hayCamposVacios = camposObligatorios.any { it.isBlank() }

                    if (hayCamposVacios) {
                        mostrarError = true
                    } else {
                        // Creamos el nuevo registro de salud
                        viewModel.agregarRegistroSalud(
                            arete = arete,
                            fecha = fecha,
                            tipo = tipo,
                            tratamiento = tratamiento,
                            responsable = responsable,
                            observaciones = observaciones
                        )

                        // Limpiar campos
                        fecha = ""
                        tipo = ""
                        tratamiento = ""
                        responsable = ""
                        observaciones = ""

                        // Regresar atrás
                        navController.popBackStack()
                    }
                }) {
                    Text("Guardar Registro")
                }
            }
        }
    }
}



