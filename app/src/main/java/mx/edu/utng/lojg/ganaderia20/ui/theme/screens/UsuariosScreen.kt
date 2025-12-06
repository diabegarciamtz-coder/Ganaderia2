package mx.edu.utng.lojg.ganaderia20.ui.theme.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.edu.utng.lojg.ganaderia20.Repository.CodigoInvitacionRepository
import mx.edu.utng.lojg.ganaderia20.viewmodel.GanadoViewModel
import java.text.SimpleDateFormat
import java.util.*

// Data class para usuarios
data class Usuario(
    val id: String,
    val nombre: String,
    val email: String,
    val rol: String,
    val activo: Boolean,
    val fechaRegistro: Timestamp,
    val permisos: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuariosScreen(
    navController: NavController,
    viewModel: GanadoViewModel,
    currentUserId: String,
    userRole: String = "admin",
    propietarioRancho: Boolean = true
) {
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var mostrarDialogoGenerarCodigo by remember { mutableStateOf(false) }
    var mostrarDialogoEditarUsuario by remember { mutableStateOf(false) }
    var mostrarDialogoCodigoGenerado by remember { mutableStateOf(false) }
    var codigoGenerado by remember { mutableStateOf("") }
    var usuarioSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Cargar usuarios desde Firestore
    LaunchedEffect(currentUserId) {
        try {
            usuarios = cargarUsuariosDesdeFirestore(currentUserId)
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar("Error al cargar usuarios: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrar Usuarios") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { mostrarDialogoGenerarCodigo = true }
                    ) {
                        Icon(Icons.Default.PersonAdd, "Generar código de invitación")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Estadísticas rápidas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            usuarios.size.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color(0xFF0369A1)
                        )
                        Text("Total usuarios", fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            usuarios.count { it.activo }.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color(0xFF16A34A)
                        )
                        Text("Activos", fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            usuarios.count { !it.activo }.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color(0xFFDC2626)
                        )
                        Text("Inactivos", fontSize = 12.sp)
                    }
                }
            }

            // Lista de usuarios
            if (usuarios.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(usuarios) { usuario ->
                        TarjetaUsuario(
                            usuario = usuario,
                            onEditarClick = {
                                usuarioSeleccionado = usuario
                                mostrarDialogoEditarUsuario = true
                            },
                            onActivarDesactivarClick = {
                                scope.launch {
                                    try {
                                        toggleUsuarioActivo(usuario.id, !usuario.activo)
                                        usuarios = usuarios.map {
                                            if (it.id == usuario.id) it.copy(activo = !it.activo)
                                            else it
                                        }
                                        snackbarHostState.showSnackbar(
                                            if (usuario.activo) "Usuario desactivado"
                                            else "Usuario activado"
                                        )
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Error: ${e.message}")
                                    }
                                }
                            }
                        )
                    }
                }
            } else {
                // Estado vacío
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = "Sin usuarios",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No hay usuarios registrados",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Genera un código de invitación para agregar nuevos usuarios",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
        }
    }

    // Diálogo para generar código de invitación
    if (mostrarDialogoGenerarCodigo) {
        DialogoGenerarCodigo(
            onDismiss = { mostrarDialogoGenerarCodigo = false },
            onGenerarCodigo = { tipo, usos ->
                scope.launch {
                    try {
                        val nuevoCodigo = CodigoInvitacionRepository.crearCodigoInvitacion(
                            adminId = currentUserId,
                            tipo = tipo,
                            usosTotales = usos,
                            diasExpiracion = 30
                        )
                        codigoGenerado = nuevoCodigo.codigo
                        mostrarDialogoGenerarCodigo = false
                        mostrarDialogoCodigoGenerado = true
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("❌ Error al generar código: ${e.message}")
                    }
                }
            }
        )
    }

    // Diálogo para mostrar código generado
    if (mostrarDialogoCodigoGenerado) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCodigoGenerado = false },
            title = {
                Text("✅ Código Generado", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Copia este código y compártelo con la persona que quieras invitar:")

                    Spacer(modifier = Modifier.height(16.dp))

                    // Código destacado
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                codigoGenerado,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1),
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Código de invitación",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "El usuario deberá ingresar este código al registrarse para obtener los permisos correspondientes.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Código de invitación", codigoGenerado)
                            clipboard.setPrimaryClip(clip)

                            scope.launch {
                                mostrarDialogoCodigoGenerado = false
                                snackbarHostState.showSnackbar("Código copiado al portapapeles")
                            }
                        } catch (e: Exception) {
                            scope.launch {
                                mostrarDialogoCodigoGenerado = false
                                snackbarHostState.showSnackbar("Error al copiar: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copiar Código")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCodigoGenerado = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Diálogo para editar usuario
    if (mostrarDialogoEditarUsuario && usuarioSeleccionado != null) {
        DialogoEditarUsuario(
            usuario = usuarioSeleccionado!!,
            onDismiss = {
                mostrarDialogoEditarUsuario = false
                usuarioSeleccionado = null
            },
            // ---------- LÓGICA ACTUALIZADA ----------
            onGuardarCambios = { nuevosPermisos, nuevoRol ->
                scope.launch {
                    try {
                        actualizarUsuario(usuarioSeleccionado!!.id, nuevosPermisos, nuevoRol)

                        // Notificar al usuario si está conectado para que sus permisos se actualicen en tiempo real
                        notificarCambioPermisos(usuarioSeleccionado!!.id)

                        // Actualizar la lista localmente
                        usuarios = usuarios.map {
                            if (it.id == usuarioSeleccionado!!.id)
                                it.copy(permisos = nuevosPermisos, rol = nuevoRol)
                            else it
                        }
                        snackbarHostState.showSnackbar("✅ Usuario actualizado correctamente")
                        mostrarDialogoEditarUsuario = false
                        usuarioSeleccionado = null
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("❌ Error al actualizar usuario: ${e.message}")
                    }
                }
            }
            // ---------- FIN DE LA ACTUALIZACIÓN ----------
        )
    }
}

@Composable
fun TarjetaUsuario(
    usuario: Usuario,
    onEditarClick: () -> Unit,
    onActivarDesactivarClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (usuario.activo) Color.White else Color(0xFFFEF2F2)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        usuario.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        usuario.email,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                if (!usuario.activo) {
                    Text(
                        "INACTIVO",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge de rol
                Text(
                    text = usuario.rol.uppercase(),
                    modifier = Modifier
                        .background(
                            color = when (usuario.rol) {
                                "admin" -> Color(0xFFDC2626)
                                "veterinario" -> Color(0xFF059669)
                                "supervisor" -> Color(0xFF2563EB)
                                else -> Color(0xFF6B7280)
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    "Registrado: ${formatearFecha(usuario.fechaRegistro)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Permisos (solo mostrar los primeros 3)
            if (usuario.permisos.isNotEmpty()) {
                Text(
                    "Permisos: ${usuario.permisos.take(3).joinToString(", ")}" +
                            if (usuario.permisos.size > 3) "..." else "",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón activar/desactivar
                TextButton(
                    onClick = onActivarDesactivarClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (usuario.activo) Color(0xFFDC2626) else Color(0xFF16A34A)
                    )
                ) {
                    Icon(
                        if (usuario.activo) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (usuario.activo) "Desactivar" else "Activar")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón editar
                Button(
                    onClick = onEditarClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoGenerarCodigo(
    onDismiss: () -> Unit,
    onGenerarCodigo: (String, Int) -> Unit
) {
    var tipoSeleccionado by remember { mutableStateOf("veterinario") }
    var usosSeleccionados by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Generar Código de Invitación", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Selecciona el tipo de usuario:")
                Spacer(modifier = Modifier.height(12.dp))

                listOf(
                    "veterinario" to "Veterinario",
                    "empleado" to "Empleado General",
                    "supervisor" to "Supervisor"
                ).forEach { (tipo, nombre) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { tipoSeleccionado = tipo }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tipoSeleccionado == tipo,
                            onClick = { tipoSeleccionado = tipo }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(nombre)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Número de usos permitidos:")
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(1, 5, 10).forEach { usos ->
                        FilterChip(
                            selected = usosSeleccionados == usos,
                            onClick = { usosSeleccionados = usos },
                            label = { Text("$usos") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGenerarCodigo(tipoSeleccionado, usosSeleccionados) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
            ) {
                Text("Generar Código")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DialogoEditarUsuario(
    usuario: Usuario,
    onDismiss: () -> Unit,
    onGuardarCambios: (List<String>, String) -> Unit
) {
    var rolSeleccionado by remember { mutableStateOf(usuario.rol) }
    val permisosSeleccionados = remember { mutableStateOf(usuario.permisos.toMutableSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Editar Usuario: ${usuario.nombre}", fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn { // Usar LazyColumn para que sea scrollable si hay muchos permisos
                item {
                    Text("Rol del usuario:")
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf("empleado", "veterinario", "supervisor").forEach { rol ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { rolSeleccionado = rol }
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = rolSeleccionado == rol,
                                onClick = { rolSeleccionado = rol }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(rol.replaceFirstChar { it.uppercase() })
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Permisos:")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val todosLosPermisos = listOf(
                    "leer" to "Ver información",
                    "crear" to "Crear registros",
                    "actualizar" to "Editar registros",
                    "eliminar" to "Eliminar registros",
                    "gestionar_animales" to "Gestionar animales",
                    "ver_reportes" to "Ver reportes"
                )

                items(todosLosPermisos) { (permiso, descripcion) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    val currentPerms = permisosSeleccionados.value.toMutableSet()
                                    if (currentPerms.contains(permiso)) {
                                        currentPerms.remove(permiso)
                                    } else {
                                        currentPerms.add(permiso)
                                    }
                                    permisosSeleccionados.value = currentPerms
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = permisosSeleccionados.value.contains(permiso),
                            onCheckedChange = { checked ->
                                val currentPerms = permisosSeleccionados.value.toMutableSet()
                                if (checked) {
                                    currentPerms.add(permiso)
                                } else {
                                    currentPerms.remove(permiso)
                                }
                                permisosSeleccionados.value = currentPerms
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(descripcion)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardarCambios(permisosSeleccionados.value.toList(), rolSeleccionado) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


// --- FUNCIONES DE UTILIDAD ---

private suspend fun cargarUsuariosDesdeFirestore(adminId: String): List<Usuario> {
    return try {
        val db = FirebaseFirestore.getInstance()
        val snapshot = db.collection("usuarios")
            .whereNotEqualTo("rol", "superadmin")
            .get()
            .await()

        snapshot.documents.mapNotNull { document ->
            try {
                Usuario(
                    id = document.id,
                    nombre = document.getString("nombre") ?: "Sin nombre",
                    email = document.getString("email") ?: "Sin email",
                    rol = document.getString("rol") ?: "usuario",
                    activo = document.getBoolean("activo") ?: true,
                    fechaRegistro = document.getTimestamp("fechaRegistro") ?: Timestamp.now(),
                    permisos = document.get("permisos") as? List<String> ?: emptyList()
                )
            } catch (e: Exception) {
                null
            }
        }
    } catch (e: Exception) {
        emptyList()
    }
}

private suspend fun toggleUsuarioActivo(usuarioId: String, activo: Boolean) {
    val db = FirebaseFirestore.getInstance()
    db.collection("usuarios")
        .document(usuarioId)
        .update("activo", activo)
        .await()
}

private suspend fun actualizarUsuario(usuarioId: String, permisos: List<String>, rol: String) {
    val db = FirebaseFirestore.getInstance()
    val updates = hashMapOf<String, Any>(
        "rol" to rol,
        "permisos" to permisos,
    )
    db.collection("usuarios")
        .document(usuarioId)
        .update(updates)
        .await()
}

/**
 * Notifica un cambio de permisos para que el cliente pueda reaccionar en tiempo real.
 * Crea o actualiza un documento con un timestamp.
 */
private suspend fun notificarCambioPermisos(usuarioId: String) {
    try {
        val db = FirebaseFirestore.getInstance()
        val notificacion = hashMapOf(
            "ultimoCambio" to FieldValue.serverTimestamp()
        )
        db.collection("notificaciones_permisos")
            .document(usuarioId)
            .set(notificacion) // .set() crea o sobrescribe el documento
            .await()
        println("✅ Notificación de cambio de permisos enviada para $usuarioId")
    } catch (e: Exception) {
        println("❌ Error al notificar cambio de permisos: ${e.message}")
    }
}

private fun formatearFecha(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(date)
}
