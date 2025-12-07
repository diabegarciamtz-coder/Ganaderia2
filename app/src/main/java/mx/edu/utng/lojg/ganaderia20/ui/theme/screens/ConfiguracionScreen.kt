package mx.edu.utng.lojg.ganaderia20.ui.theme.screens


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mx.edu.utng.lojg.ganaderia20.ui.theme.components.CustomTextField
import androidx.compose.ui.graphics.Brush
import com.google.firebase.Timestamp
import mx.edu.utng.lojg.ganaderia20.Repository.CodigoInvitacionRepository
import java.text.SimpleDateFormat
import java.util.*
import mx.edu.utng.lojg.ganaderia20.viewmodel.AuthViewModel

/**
 * Clase de datos que representa un código de invitación en la capa de interfaz de usuario.
 *
 * Esta clase se utiliza para mapear y mostrar la información relevante de los códigos de invitación
 * generados por el administrador/propietario.
 *
 * @property id El ID único del código de invitación.
 * @property codigo La cadena alfanumérica del código de invitación.
 * @property tipo El rol de usuario que este código permite registrar (ej. "veterinario", "empleado").
 * @property activo Indica si el código está actualmente activo.
 * @property fechaCreacion El timestamp de Firebase que indica cuándo se creó el código.
 * @property usadoEl El timestamp de Firebase que indica cuándo se utilizó el código por última vez (opcional).
 * @property usosRestantes El número de veces que este código puede ser usado antes de agotarse.
 */
data class CodigoInvitacionUI(
    val id: String,
    val codigo: String,
    val tipo: String,
    val activo: Boolean,
    val fechaCreacion: Timestamp,
    val usadoEl: Timestamp? = null,
    val usosRestantes: Int = 1
)

/**
 * Pantalla Composable principal para la configuración de la cuenta y administración de la aplicación.
 *
 * Permite al usuario ver la información de su cuenta, cambiar la contraseña y, si es administrador
 * o propietario, gestionar y generar códigos de invitación.
 *
 * @param navController El controlador de navegación para cambiar de pantalla.
 * @param authViewModel El ViewModel de autenticación para manejar las operaciones de sesión (ej. cerrar sesión).
 * @param userId El ID del usuario actual.
 * @param userRole El rol del usuario actual (ej. "admin", "usuario").
 * @param propietarioRancho Bandera que indica si el usuario es el propietario del rancho.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    userId: String = "user123",
    userRole: String = "usuario",
    propietarioRancho: Boolean = false
) {

    // --- Estados para Campos de Formulario ---
    val nombre = remember { mutableStateOf("Administrador Sistema") }
    val correo = remember { mutableStateOf("correo@ejemplo.com") }
    val telefono = remember { mutableStateOf("123-456-7890") }
    val contrasenaActual = remember { mutableStateOf("") }
    val nuevaContrasena = remember { mutableStateOf("") }

    // --- Estados para Control de UI y Lógica Admin ---
    var mostrarGenerarCodigo by remember { mutableStateOf(false) }
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }
    var codigosGenerados by remember { mutableStateOf<List<CodigoInvitacionUI>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Determina si se deben mostrar las funciones de administrador/propietario
    val mostrarFuncionesAdmin = userRole == "admin" || propietarioRancho

    // Carga los códigos de invitación al inicializar si el usuario tiene permisos
    LaunchedEffect(userId, userRole, propietarioRancho) {
        if (mostrarFuncionesAdmin) {
            try {
                val codigosFirebase = CodigoInvitacionRepository.cargarCodigosInvitacion(userId)
                codigosGenerados = codigosFirebase.map { firebaseCodigo ->
                    CodigoInvitacionUI(
                        id = firebaseCodigo.id,
                        codigo = firebaseCodigo.codigo,
                        tipo = firebaseCodigo.tipo,
                        activo = firebaseCodigo.activo,
                        fechaCreacion = firebaseCodigo.fechaCreacion,
                        usadoEl = firebaseCodigo.usadoEl,
                        usosRestantes = firebaseCodigo.usosRestantes
                    )
                }
            } catch (e: Exception) {
                println("Error al cargar códigos: ${e.message}")
            }
        }
    }

    /**
     * Cierra la sesión del usuario actual y navega a la pantalla de login.
     *
     * Utiliza [authViewModel.logout] y limpia la pila de navegación.
     */
    fun cerrarSesion() {
        authViewModel.logout()
        navController.navigate("login") {
            // Limpia la pila de navegación para que el usuario no pueda volver atrás.
            popUpTo(0) { inclusive = true }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Configuración",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
                .padding(24.dp)
        ) {
            // --- Tarjeta de Resumen de Cuenta ---
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Tu Cuenta",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Rol actual: $userRole", fontSize = 14.sp)
                        Text("ID: $userId", fontSize = 12.sp, color = Color.Gray)
                        if (propietarioRancho) {
                            Text("Propietario del rancho", fontSize = 12.sp, color = Color(0xFF16A34A))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- Sección de Información Personal ---
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = if (propietarioRancho) "Información del Dueño" else "Información Personal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        CustomTextField("Nombre Completo", nombre)
                        Spacer(modifier = Modifier.height(12.dp))
                        CustomTextField("Correo Electrónico", correo)
                        Spacer(modifier = Modifier.height(12.dp))
                        CustomTextField("Teléfono", telefono)

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { /* Lógica para guardar cambios */ },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                            ) {
                                Text("Guardar Cambios")
                            }

                            OutlinedButton(onClick = { /* Lógica para cancelar */ }) {
                                Text("Cancelar")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- Sección de Cambiar Contraseña ---
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Cambiar Contraseña",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        CustomTextField("Contraseña Actual", contrasenaActual, isPassword = true)
                        Spacer(modifier = Modifier.height(12.dp))
                        CustomTextField("Nueva Contraseña", nuevaContrasena, isPassword = true)

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { /* Lógica para actualizar contraseña */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Text("Actualizar Contraseña")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- Sección de Seguridad y Cerrar Sesión ---
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Seguridad",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Gestiona la seguridad de tu cuenta",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // BOTÓN DE CERRAR SESIÓN
                        OutlinedButton(
                            onClick = { mostrarDialogoCerrarSesion = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFDC2626)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFDC2626), Color(0xFFB91C1C))
                                )
                            )
                        ) {
                            Icon(
                                Icons.Filled.Logout,
                                contentDescription = "Cerrar sesión",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cerrar Sesión")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- Sección de Administración (solo si es admin/propietario) ---
            if (mostrarFuncionesAdmin) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (propietarioRancho) Color(0xFFF0F9FF) else Color(0xFFE0F2FE)
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                if (propietarioRancho) "Panel del Dueño" else "Panel de Administrador",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (propietarioRancho) Color(0xFF0369A1) else Color(0xFF075985)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                if (propietarioRancho)
                                    "Genera códigos de invitación para que tus empleados se registren en el sistema"
                                else
                                    "Genera códigos de invitación para que otros usuarios se registren en el sistema",
                                fontSize = 14.sp,
                                color = Color(0xFF374151)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { mostrarGenerarCodigo = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                            ) {
                                Icon(
                                    if (propietarioRancho) Icons.Filled.PersonAdd else Icons.Filled.Share,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (propietarioRancho) "Generar Código para Empleados" else "Generar Nuevo Código"
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- Listado de Códigos Generados ---
                if (codigosGenerados.isNotEmpty()) {
                    item {
                        Text(
                            "Tus Códigos de Invitación",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // ---------- BLOQUE ACTUALIZADO ----------
                    items(codigosGenerados) { codigo ->
                        TarjetaCodigoInvitacion(
                            codigo = codigo,
                            onEliminar = { codigoId ->
                                scope.launch {
                                    try {
                                        val eliminado = CodigoInvitacionRepository.eliminarCodigo(codigoId)
                                        if (eliminado) {
                                            codigosGenerados = codigosGenerados.filter { it.id != codigoId }
                                            snackbarHostState.showSnackbar("✅ Código eliminado")
                                        } else {
                                            snackbarHostState.showSnackbar("❌ Error al eliminar código")
                                        }
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("❌ Error: ${e.message}")
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    // ---------- FIN DE LA ACTUALIZACIÓN ----------
                }
            }
        }
    }

    // --- Diálogo de Confirmación de Cerrar Sesión ---
    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            title = {
                Text(
                    "¿Cerrar Sesión?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            },
            text = {
                Column {
                    Text("Estás a punto de cerrar tu sesión actual.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tendrás que volver a iniciar sesión para acceder a la aplicación.",
                        color = Color(0xFF6B7280)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        cerrarSesion()
                        mostrarDialogoCerrarSesion = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Sí, Cerrar Sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // --- Diálogo de Generación de Código de Invitación ---
    if (mostrarGenerarCodigo) {
        var tipoSeleccionado by remember { mutableStateOf("veterinario") }
        var usosSeleccionados by remember { mutableStateOf(1) }
        var longitudSeleccionada by remember { mutableStateOf(8) }

        AlertDialog(
            onDismissRequest = { mostrarGenerarCodigo = false },
            title = {
                Text(
                    "Generar Código de Invitación",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Selecciona el tipo de código:")
                    Spacer(modifier = Modifier.height(12.dp))

                    val opciones = if (propietarioRancho) {
                        listOf("veterinario" to "Veterinario", "empleado" to "Empleado General", "supervisor" to "Supervisor")
                    } else {
                        listOf("admin" to "Administrador", "veterinario" to "Veterinario", "supervisor" to "Supervisor")
                    }

                    opciones.forEach { (tipo, nombre) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
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
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(1, 5, 10).forEach { usos ->
                            FilterChip(
                                selected = usosSeleccionados == usos,
                                onClick = { usosSeleccionados = usos },
                                label = { Text("$usos") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Longitud del código:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(6, 8, 10, 12).forEach { longitud ->
                            FilterChip(
                                selected = longitudSeleccionada == longitud,
                                onClick = { longitudSeleccionada = longitud },
                                label = { Text("$longitud") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        // Muestra un ejemplo dinámico del código
                        "Ejemplo: ${CodigoInvitacionRepository.generarCodigoPersonalizado(longitudSeleccionada)}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                // Llama al repositorio para crear y guardar el código en Firebase
                                val nuevoCodigoFirebase = CodigoInvitacionRepository.crearCodigoInvitacionPersonalizado(
                                    adminId = userId,
                                    tipo = tipoSeleccionado,
                                    usosTotales = usosSeleccionados,
                                    diasExpiracion = 30,
                                    longitudCodigo = longitudSeleccionada
                                )

                                // Mapea el resultado a la clase de UI
                                val nuevoCodigoUI = CodigoInvitacionUI(
                                    id = nuevoCodigoFirebase.id,
                                    codigo = nuevoCodigoFirebase.codigo,
                                    tipo = nuevoCodigoFirebase.tipo,
                                    activo = true,
                                    fechaCreacion = nuevoCodigoFirebase.fechaCreacion,
                                    usosRestantes = nuevoCodigoFirebase.usosRestantes
                                )

                                // Añade el nuevo código al inicio de la lista
                                codigosGenerados = listOf(nuevoCodigoUI) + codigosGenerados
                                mostrarGenerarCodigo = false
                                snackbarHostState.showSnackbar("Código generado exitosamente")
                            } catch (e: Exception) {
                                println("Error al generar código: ${e.message}")
                                snackbarHostState.showSnackbar("Error al generar el código")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text("Generar Código")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarGenerarCodigo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Componente Composable que presenta un código de invitación generado en formato de tarjeta.
 *
 * Permite al usuario ver el estado, tipo, usos restantes del código y copiarlo al portapapeles.
 * También permite eliminar el código si está inactivo o agotado.
 *
 * @param codigo El objeto [CodigoInvitacionUI] con los datos del código a mostrar.
 * @param onEliminar Función lambda que se invoca para eliminar el código, pasando el ID del código.
 */
@Composable
fun TarjetaCodigoInvitacion(
    codigo: CodigoInvitacionUI,
    onEliminar: (String) -> Unit
) {
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = codigo.codigo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E40AF),
                    letterSpacing = 2.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (codigo.activo && codigo.usosRestantes > 0) "✓ Activo"
                        else if (codigo.usosRestantes == 0) "✓ Agotado"
                        else "✗ Usado",
                        fontSize = 12.sp,
                        color = if (codigo.activo && codigo.usosRestantes > 0) Color(0xFF16A34A)
                        else Color.Gray,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    // Muestra el botón de eliminar solo si está inactivo o agotado
                    if (!codigo.activo || codigo.usosRestantes == 0) {
                        IconButton(
                            onClick = { mostrarDialogoEliminar = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Tipo: ${codigo.tipo}", fontSize = 14.sp)
            Text("Longitud: ${codigo.codigo.length} caracteres", fontSize = 12.sp, color = Color.Gray)
            Text("Creado: ${formatearFecha(codigo.fechaCreacion)}", fontSize = 12.sp, color = Color.Gray)
            Text("Usos restantes: ${codigo.usosRestantes}", fontSize = 12.sp, color = Color.Gray)

            if (!codigo.activo && codigo.usadoEl != null) {
                Text("Usado el: ${formatearFecha(codigo.usadoEl)}", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    // Copia el código al portapapeles
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Código de invitación", codigo.codigo)
                    clipboard.setPrimaryClip(clip)
                    // Opcional: Mostrar un Snackbar
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = codigo.activo && codigo.usosRestantes > 0, // Solo activo si está activo y tiene usos
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (codigo.activo && codigo.usosRestantes > 0)
                        Color(0xFF3B82F6) else Color.LightGray
                )
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copiar Código")
            }
        }
    }

    // Diálogo de confirmación de eliminación
    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("¿Eliminar código?") },
            text = {
                Text("¿Estás seguro de que quieres eliminar el código ${codigo.codigo}? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEliminar(codigo.id)
                        mostrarDialogoEliminar = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Función auxiliar para formatear un [Timestamp] de Firebase a una cadena de fecha y hora legible.
 *
 * El formato utilizado es "dd/MM/yyyy HH:mm".
 *
 * @param timestamp El objeto [Timestamp] a formatear.
 * @return La cadena de fecha y hora formateada.
 */
private fun formatearFecha(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}