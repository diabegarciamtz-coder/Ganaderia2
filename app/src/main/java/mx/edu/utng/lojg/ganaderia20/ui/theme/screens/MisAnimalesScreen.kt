package mx.edu.utng.lojg.ganaderia20.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mx.edu.utng.lojg.ganaderia20.data.entities.AnimalEntity
import mx.edu.utng.lojg.ganaderia20.viewmodel.GanadoViewModel
import androidx.compose.material.icons.filled.MedicalServices
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.Pets
import androidx.compose.ui.unit.sp
import mx.edu.utng.lojg.ganaderia20.R

/**
 * Pantalla principal que muestra la lista de animales del usuario o rancho.
 *
 * Permite al usuario:
 * 1. Ver la lista de animales cargados desde el ViewModel, filtrada por el [uid] y [rol].
 * 2. Buscar/filtrar animales por nombre o arete.
 * 3. Interactuar con cada animal a través de [TarjetaAnimalExpandible].
 * 4. Navegar a la pantalla de registro de cría.
 *
 * @param navController El controlador de navegación para el flujo de la aplicación.
 * @param viewModel El [GanadoViewModel] para gestionar la carga y el estado de los animales.
 * @param uid El ID del usuario actual.
 * @param rol El rol del usuario actual, usado para determinar qué animales cargar.
 */
@Composable
fun PantallaMisAnimales(
    navController: NavController,
    viewModel: GanadoViewModel,
    uid: String,
    rol: String
) {
    // Llamar a cargarAnimales cuando se abre esta pantalla
    LaunchedEffect(uid, rol) {
        viewModel.cargarAnimales(uid, rol)
    }

    // Obtener la lista de animales (StateFlow)
    val animales by viewModel.animales.collectAsState()

    // Estado del buscador
    var busqueda by remember { mutableStateOf("") }

    // Filtrar la lista de animales
    val filtrados = if (busqueda.isBlank()) {
        animales
    } else {
        animales.filter { animal ->
            animal.nombre.contains(busqueda, ignoreCase = true) ||
                    animal.arete.contains(busqueda, ignoreCase = true)
        }
    }

    Scaffold(
        /*floatingActionButton = { ... } */ // FAB comentado en el código original
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text(
                " Mis Animales 🐄",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            // Campo de Búsqueda
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar por arete o nombre") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(Modifier.height(16.dp))

            // --- Lógica de Estado Vacío y Lista ---

            if (animales.isEmpty()) {
                // Estado vacío general (no hay registros en total)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No hay animales registrados")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { navController.navigate("registrar_cria") }) {
                        Text("Registrar primer animal")
                    }
                }
            } else if (filtrados.isEmpty() && busqueda.isNotBlank()) {
                // Estado de "sin resultados" en la búsqueda
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No se encontraron animales con '$busqueda'")
                }
            } else {
                // Lista de animales filtrados
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtrados) { animal ->
                        TarjetaAnimalExpandible(
                            animal = animal,
                            onVerHistorial = {
                                // Navega al reporte de salud del animal (incluye la gráfica)
                                navController.navigate("health_report/${animal.arete}")
                            },
                            onActualizarPeso = {
                                // Navega a la pantalla para actualizar peso
                                navController.navigate("actualizar_peso/${animal.arete}")
                            },
                            navController = navController
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Botón de registro al final
            Button(
                onClick = { navController.navigate("registrar_cria") },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Registrar Nueva Cría")
            }
        }
    }
}

/**
 * Componente de tarjeta de animal que puede expandirse para mostrar detalles y acciones.
 *
 * Muestra el nombre, arete, tipo, raza, foto, peso y estado de salud.
 * Al expandirse, muestra detalles adicionales y botones de acción:
 * - [onActualizarPeso]: Navega a la pantalla de actualización de peso.
 * - [onVerHistorial]: Navega a la pantalla de reporte de salud.
 * - Registro de evento de salud.
 *
 * @param animal La entidad [AnimalEntity] a mostrar.
 * @param onVerHistorial Lambda para la acción de ver el reporte/historial.
 * @param onActualizarPeso Lambda para la acción de actualizar el peso.
 * @param navController El controlador de navegación necesario para el botón de 'Registrar Evento de Salud'.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaAnimalExpandible(
    animal: AnimalEntity,
    onVerHistorial: () -> Unit,
    onActualizarPeso: () -> Unit,
    navController: NavController
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { expanded = !expanded } // Toggle de expansión al hacer clic en la tarjeta
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header de la tarjeta CON FOTO/ÍCONO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // FOTO DEL ANIMAL o Ícono por defecto
                if (animal.foto != null) {
                    AsyncImage(
                        model = animal.foto,
                        contentDescription = "Foto de ${animal.nombre}",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.vaca_logo) // Imagen por defecto si falla
                    )
                } else {
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = "Sin foto",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Información del animal
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        animal.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Arete: ${animal.arete}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${animal.tipo} • ${animal.raza}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Ícono de expansión
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Contraer" else "Expandir",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Información básica siempre visible (Peso y Estado)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                InfoItem("Peso", "${animal.peso} kg")
                InfoItem("Estado", animal.estadoSalud)
            }

            // Información expandida (detalles y botones)
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Detalles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoItemDetalle("Fecha Nac.", animal.fechaNacimiento)
                        InfoItemDetalle("Edad", calcularEdad(animal.fechaNacimiento))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoItemDetalle("Madre", animal.madre ?: "Desconocida")
                        InfoItemDetalle("Padre", animal.padre ?: "Desconocido")
                    }
                    InfoItemDetalle(
                        "Observaciones",
                        animal.observaciones?.ifEmpty { "Sin observaciones" } ?: "Sin observaciones"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botones de acción principales
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onActualizarPeso,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Actualizar Peso", fontSize = 12.sp)
                        }
                        Button(
                            onClick = onVerHistorial,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ver Reporte", fontSize = 12.sp)
                        }
                    }

                    // Botón para registrar evento de salud
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            navController.navigate("registrar_salud/${animal.arete}")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registrar Evento de Salud")
                    }
                }
            }
        }
    }
}


/**
 * Componente para mostrar un par título/valor en una columna, centrado.
 * Usado para datos clave siempre visibles (Peso, Estado).
 *
 * @param titulo La etiqueta (ej. "Peso").
 * @param valor El dato (ej. "500 kg").
 */
@Composable
fun InfoItem(titulo: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            titulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Componente para mostrar un par título/valor, alineado a la izquierda.
 * Usado en la sección expandida para detalles.
 *
 * @param titulo La etiqueta (ej. "Fecha Nac.").
 * @param valor El dato (ej. "01/01/2023").
 */
@Composable
fun InfoItemDetalle(titulo: String, valor: String) {
    Column {
        Text(
            titulo,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Función privada para calcular la edad aproximada de un animal
 * a partir de una fecha de nacimiento en formato "dd/MM/yyyy".
 *
 * Devuelve la edad en días, meses o años.
 *
 * @param fechaNacimiento La fecha de nacimiento como String.
 * @return Una cadena de texto con la edad aproximada (ej. "2 años").
 */
private fun calcularEdad(fechaNacimiento: String): String {
    return try {
        val formato = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val fechaNac = formato.parse(fechaNacimiento)
        val hoy = java.util.Calendar.getInstance().time

        val diff = hoy.time - (fechaNac?.time ?: 0)
        val dias = diff / (24 * 60 * 60 * 1000)

        when {
            dias < 30 -> "$dias días"
            dias < 365 -> "${dias / 30} meses"
            else -> "${dias / 365} años"
        }
    } catch (e: Exception) {
        "Desconocida"
    }
}


/**
 * Pantalla Composable para registrar el nuevo peso de un animal y guardar un registro de salud asociado.
 *
 * @param navController El controlador de navegación para volver a la pantalla anterior.
 * @param viewModel El [GanadoViewModel] para cargar la información del animal y ejecutar las actualizaciones.
 * @param arete El arete del animal a actualizar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActualizarPesoScreen(
    navController: NavController,
    viewModel: GanadoViewModel,
    arete: String
) {
    var nuevoPeso by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val animal by viewModel.animalSeleccionado

    // Carga la información del animal al iniciar la pantalla
    LaunchedEffect(arete) {
        viewModel.cargarAnimalPorArete(arete)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actualizar Peso") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn( // Usamos LazyColumn para evitar desbordes en pantallas pequeñas
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                // Información del animal
                animal?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                it.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Arete: ${it.arete}")
                            Text("Peso actual: ${it.peso} kg")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                // Formulario para actualizar peso
                OutlinedTextField(
                    value = nuevoPeso,
                    onValueChange = { nuevoPeso = it },
                    label = { Text("Nuevo peso (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones (opcional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de Actualizar
                Button(
                    onClick = {
                        if (nuevoPeso.isNotBlank()) {
                            isLoading = true
                            val pesoDouble = nuevoPeso.toDoubleOrNull() ?: 0.0

                            // 1. Registrar un evento de salud con la actualización de peso
                            viewModel.agregarRegistroSaludSimple(
                                arete = arete,
                                tipo = "Actualización de peso",
                                descripcion = "Nuevo peso: $nuevoPeso kg. ${if (observaciones.isNotEmpty()) "Obs: $observaciones" else ""}"
                            )

                            // 2. Actualizar el campo 'peso' en la entidad del animal
                            viewModel.actualizarPesoAnimal(
                                arete = arete,
                                nuevoPeso = pesoDouble,
                                observaciones = observaciones
                            ) { exito ->
                                isLoading = false
                                if (exito) {
                                    // Vuelve a la pantalla anterior (Mis Animales o Reporte de Salud)
                                    navController.popBackStack()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = nuevoPeso.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Actualizar Peso")
                    }
                }
            }
        }
    }
}