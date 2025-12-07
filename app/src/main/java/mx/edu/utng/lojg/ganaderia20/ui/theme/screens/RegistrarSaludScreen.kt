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
/**
 * Pantalla Composable para registrar un nuevo evento o historial de salud para un animal específico.
 *
 * Permite al usuario ingresar la fecha, tipo de evento, tratamiento aplicado, el responsable
 * y observaciones, validando que los campos obligatorios (Fecha, Tipo, Responsable) estén llenos.
 *
 * @param navController El controlador de navegación para volver a la pantalla anterior.
 * @param arete El identificador único del animal (arete) al que se le registrará el evento.
 * @param viewModel El [GanadoViewModel] para ejecutar la lógica de guardado de datos de salud.
 */
@Composable
fun RegistrarSaludScreen(
    navController: NavController,
    arete: String,
    viewModel: GanadoViewModel
) {
    // Estados locales para los campos del formulario
    var fecha by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var tratamiento by remember { mutableStateOf("") }
    var responsable by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var mostrarError by remember { mutableStateOf(false) }

    // Lista de tipos de tratamiento para sugerencias
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

            // Campo Fecha
            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha (dd/mm/aaaa)") },
                placeholder = {  },
                modifier = Modifier.fillMaxWidth(),
                isError = mostrarError && fecha.isBlank()
            )
            Spacer(Modifier.height(8.dp))

            // Campo Tipo de Tratamiento (Obligatorio)
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
                    // Muestra mensaje de error o sugerencia de autocompletado
                    if (mostrarError && tipo.isBlank()) {
                        Text("Este campo es obligatorio")
                    } else if (tipo.isNotBlank()) {
                        tiposTratamiento.find { it.contains(tipo, true) }?.let { sugerencia ->
                            Text("Sugerencia: $sugerencia")
                        }
                    }
                }
            )
            Spacer(Modifier.height(8.dp))

            // Campo Tratamiento Aplicado (Opcional)
            OutlinedTextField(
                value = tratamiento,
                onValueChange = { tratamiento = it },
                label = { Text("Tratamiento aplicado") },
                placeholder = { Text("Describe el tratamiento o medicamento") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // Campo Veterinario / Responsable (Obligatorio)
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

            // Campo Observaciones (Opcional)
            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Mensaje de error general de validación
            if (mostrarError) {
                Text(
                    "⚠️ Complete los campos obligatorios",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { navController.popBackStack() }) {
                    Text("Cancelar")
                }

                Button(onClick = {
                    // Validación de campos obligatorios
                    val camposObligatorios = listOf(fecha, tipo, responsable)
                    val hayCamposVacios = camposObligatorios.any { it.isBlank() }

                    if (hayCamposVacios) {
                        mostrarError = true // Muestra los indicadores de error en los campos
                    } else {
                        // Oculta errores previos
                        mostrarError = false

                        // Llama al ViewModel para guardar el registro
                        viewModel.agregarRegistroSalud(
                            arete = arete,
                            fecha = fecha,
                            tipo = tipo,
                            tratamiento = tratamiento,
                            responsable = responsable,
                            observaciones = observaciones
                        )

                        // Limpiar campos (opcional, ya que se vuelve atrás)
                        fecha = ""
                        tipo = ""
                        tratamiento = ""
                        responsable = ""
                        observaciones = ""

                        // Regresar a la pantalla anterior
                        navController.popBackStack()
                    }
                }) {
                    Text("Guardar Registro")
                }
            }
        }
    }
}