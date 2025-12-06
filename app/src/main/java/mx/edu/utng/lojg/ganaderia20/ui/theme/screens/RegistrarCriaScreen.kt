package mx.edu.utng.lojg.ganaderia20.ui.theme.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.edu.utng.lojg.ganaderia20.data.entities.AnimalEntity
import mx.edu.utng.lojg.ganaderia20.viewmodel.GanadoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistrarCria(navController: NavController, viewModel: GanadoViewModel) {

    // Obtenemos la lista de animales desde el ViewModel
    val animales by viewModel.animales.collectAsState(initial = emptyList<AnimalEntity>())

    // Lista de razas comunes de ganado
    val razasComunes = listOf(
        "Angus", "Hereford", "Brahman", "Charolais", "Limousin", "Simmental",
        "Holstein", "Jersey", "Guernsey", "Pardo Suizo", "Cebú", "Nelore",
        "Gyr", "Sahiwal", "Brangus", "Beefmaster", "Santa Gertrudis",
        "Texas Longhorn", "Highland", "Wagyu", "Criollo", "Indobrasil",
        "Tabapuan", "Guzerat"
    )

    // Generar arete automático
    val siguienteArete by remember(animales) {
        derivedStateOf {
            if (animales.isEmpty()) {
                "A-001"
            } else {
                val numeros = animales.mapNotNull { animal ->
                    val regex = """[A-Za-z]-?(\d+)""".toRegex()
                    val match = regex.find(animal.arete)
                    match?.groups?.get(1)?.value?.toIntOrNull()
                }
                val maxNumero = numeros.maxOrNull() ?: 0
                val nuevoNumero = maxNumero + 1
                "A-%03d".format(nuevoNumero)
            }
        }
    }

    var arete by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var madre by remember { mutableStateOf("") }
    var padre by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var mostrarError by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    // Para manejar el Snackbar y las corrutinas
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        fotoUri = uri
    }

    // Inicializa con el arete calculado
    LaunchedEffect(siguienteArete) {
        arete = siguienteArete
    }

    // Estado para el dropdown de razas
    var razasExpandidas by remember { mutableStateOf(false) }

    // Filtrar razas basado en lo que el usuario escribe
    val razasFiltradas = if (raza.isBlank()) {
        razasComunes
    } else {
        razasComunes.filter { it.contains(raza, ignoreCase = true) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            item {
                Text(
                    "Registrar Nueva Cría",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                OutlinedTextField(
                    value = arete,
                    onValueChange = { arete = it },
                    label = { Text("Número de Arete") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ejemplo: $siguienteArete") },
                    supportingText = {
                        Text("Arete generado automáticamente. Puedes editarlo si es necesario.")
                    },
                    isError = mostrarError // Muestra el campo en rojo si hay error
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del animal") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
                OutlinedTextField(
                    value = tipo,
                    onValueChange = { tipo = it },
                    label = { Text("Tipo (Vaca, Toro, etc.)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
                // CAMPO DE RAZA AUTOCOMPLETABLE
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = raza,
                        onValueChange = {
                            raza = it
                            razasExpandidas = true
                        },
                        label = { Text("Raza") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(
                                onClick = { razasExpandidas = !razasExpandidas }
                            ) {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Mostrar razas"
                                )
                            }
                        }
                    )

                    // Dropdown de razas
                    if (razasExpandidas && razasFiltradas.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 56.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .heightIn(max = 200.dp)
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                items(razasFiltradas) { razaItem ->
                                    Text(
                                        text = razaItem,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = {
                                                    raza = razaItem
                                                    razasExpandidas = false
                                                }
                                            )
                                            .padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Divider(modifier = Modifier.padding(horizontal = 8.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            item {
                OutlinedTextField(
                    value = fechaNacimiento,
                    onValueChange = { fechaNacimiento = it },
                    label = { Text("Fecha de Nacimiento (dd/mm/aaaa)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
                OutlinedTextField(
                    value = peso,
                    onValueChange = { peso = it },
                    label = { Text("Peso al Nacer (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
                OutlinedTextField(
                    value = madre,
                    onValueChange = { madre = it },
                    label = { Text("Madre (Arete)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
                OutlinedTextField(
                    value = padre,
                    onValueChange = { padre = it },
                    label = { Text("Padre (Arete)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
                // Campo de foto
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Foto del animal (opcional)", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (fotoUri != null) {
                            AsyncImage(
                                model = fotoUri,
                                contentDescription = "Foto del animal",
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { fotoUri = null }) {
                                Text("Eliminar foto")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Seleccionar foto")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = { navController.popBackStack() }) {
                        Text("Cancelar")
                    }
                    // ---------- BOTÓN DE GUARDADO ACTUALIZADO ----------
                    Button(onClick = {
                        val auth = FirebaseAuth.getInstance()
                        val uid = auth.currentUser?.uid ?: ""

                        if (uid.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Error: No se pudo verificar el usuario.")
                            }
                            return@Button
                        }

                        val areteFinal = if (arete.isBlank()) siguienteArete else arete

                        // Verificar que el arete no exista ya
                        val areteExiste = animales.any { it.arete == areteFinal }
                        if (areteExiste) {
                            mostrarError = true
                            mensajeError = "⚠ El arete $areteFinal ya está registrado"
                            scope.launch {
                                snackbarHostState.showSnackbar(mensajeError)
                            }
                            return@Button
                        }

                        scope.launch {
                            try {
                                val db = FirebaseFirestore.getInstance()
                                val userDoc = db.collection("usuarios").document(uid).get().await()
                                val adminId = userDoc.getString("adminId") ?: uid
                                val nombreUsuario = userDoc.getString("nombre") ?: "Usuario"

                                // Crear el objeto del nuevo animal
                                val nuevoAnimal = AnimalEntity(
                                    arete = areteFinal,
                                    nombre = nombre,
                                    tipo = tipo,
                                    raza = raza,
                                    fechaNacimiento = fechaNacimiento,
                                    peso = peso,
                                    madre = madre.ifBlank { null },
                                    padre = padre.ifBlank { null },
                                    observaciones = observaciones.ifBlank { null },
                                    estadoSalud = "Saludable",
                                    foto = fotoUri?.toString(),
                                    usuarioId = uid,
                                    adminId = adminId,
                                    registradoPor = nombreUsuario
                                )

                                // GUARDAR EN FIREBASE PRIMERO
                                val animalMap = hashMapOf(
                                    "arete" to nuevoAnimal.arete,
                                    "nombre" to nuevoAnimal.nombre,
                                    "tipo" to nuevoAnimal.tipo,
                                    "raza" to nuevoAnimal.raza,
                                    "fechaNacimiento" to nuevoAnimal.fechaNacimiento,
                                    "peso" to nuevoAnimal.peso,
                                    "madre" to nuevoAnimal.madre,
                                    "padre" to nuevoAnimal.padre,
                                    "observaciones" to nuevoAnimal.observaciones,
                                    "estadoSalud" to nuevoAnimal.estadoSalud,
                                    "foto" to nuevoAnimal.foto,
                                    "usuarioId" to nuevoAnimal.usuarioId,
                                    "adminId" to nuevoAnimal.adminId,
                                    "registradoPor" to nuevoAnimal.registradoPor,
                                    "fechaRegistro" to com.google.firebase.Timestamp.now()
                                )

                                db.collection("animales")
                                    .document(areteFinal) // Usar arete como ID del documento
                                    .set(animalMap)
                                    .await()

                                // LUEGO GUARDAR EN ROOM
                                viewModel.insertarAnimal(nuevoAnimal)

                                println("✅ Animal guardado en Firebase y Room: ${nuevoAnimal.arete}")

                                // Volver a la lista
                                navController.popBackStack()

                            } catch (e: Exception) {
                                println("❌ Error al registrar: ${e.message}")
                                e.printStackTrace()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                }
                            }
                        }
                    }) {
                        Text("Registrar Cría")
                    }
                    // ---------- FIN DE LA LÓGICA ACTUALIZADA ----------
                }
            }
        }
    }
}
