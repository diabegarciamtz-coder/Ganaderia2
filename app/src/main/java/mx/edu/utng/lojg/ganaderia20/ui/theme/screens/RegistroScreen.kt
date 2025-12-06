package mx.edu.utng.lojg.ganaderia20.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.edu.utng.lojg.ganaderia20.viewmodel.AuthViewModel
import androidx.compose.foundation.interaction.MutableInteractionSource
import mx.edu.utng.lojg.ganaderia20.Repository.CodigoInvitacionRepository

// Data classes para mejor organización de datos
data class FormularioRegistro(
    val nombre: String = "",
    val username: String = "",
    val correo: String = "",
    val telefono: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val propietarioRancho: Boolean = false,
    val codigoInvitacion: String = ""
)

data class ErroresFormulario(
    val nombre: String = "",
    val username: String = "",
    val correo: String = "",
    val telefono: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val codigoInvitacion: String = ""
)

data class ResultadoRegistro(
    val exitoso: Boolean,
    val mensaje: String,
    val rolAsignado: String = "usuario"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(navController: NavController, viewModel: AuthViewModel) {
    // Estado único para el formulario
    var formulario by remember { mutableStateOf(FormularioRegistro()) }
    var errores by remember { mutableStateOf(ErroresFormulario()) }

    // Estados para UI
    var isLoading by remember { mutableStateOf(false) }
    var mostrarContrasena by remember { mutableStateOf(false) }
    var mostrarConfirmarContrasena by remember { mutableStateOf(false) }
    val mostrarCampoCodigo by remember {
        derivedStateOf { !formulario.propietarioRancho }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Crear Cuenta",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Formulario de registro
                FormularioRegistro(
                    formulario = formulario,
                    errores = errores,
                    mostrarContrasena = mostrarContrasena,
                    mostrarConfirmarContrasena = mostrarConfirmarContrasena,
                    mostrarCampoCodigo = mostrarCampoCodigo,
                    onFormularioChange = { formulario = it },
                    onErroresChange = { errores = it },
                    onMostrarContrasenaChange = { mostrarContrasena = it },
                    onMostrarConfirmarContrasenaChange = { mostrarConfirmarContrasena = it }
                )

                // Botones de acción
                BotonesAccion(
                    isLoading = isLoading,
                    onRegistroClick = {
                        scope.launch {
                            val nuevosErrores = validarFormulario(formulario)
                            if (nuevosErrores.tieneErrores()) {
                                // Actualizar errores en la UI
                                errores = nuevosErrores
                                snackbarHostState.showSnackbar("Por favor, corrige los errores en el formulario")
                            } else {
                                isLoading = true
                                val resultado = procesarRegistro(formulario)
                                isLoading = false

                                snackbarHostState.showSnackbar(resultado.mensaje)
                                if (resultado.exitoso) {
                                    // CERRAR SESIÓN después de registro exitoso
                                    FirebaseAuth.getInstance().signOut()

                                    navController.navigate("login?registroExitoso=true") {
                                        popUpTo("registro") { inclusive = true }
                                    }
                                }
                            }
                        }
                    },
                    onCancelarClick = { navController.popBackStack() },
                    onLoginClick = {
                        navController.navigate("login") {
                            popUpTo("registro") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FormularioRegistro(
    formulario: FormularioRegistro,
    errores: ErroresFormulario,
    mostrarContrasena: Boolean,
    mostrarConfirmarContrasena: Boolean,
    mostrarCampoCodigo: Boolean,
    onFormularioChange: (FormularioRegistro) -> Unit,
    onErroresChange: (ErroresFormulario) -> Unit,
    onMostrarContrasenaChange: (Boolean) -> Unit,
    onMostrarConfirmarContrasenaChange: (Boolean) -> Unit
) {
    // Tarjeta del formulario
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CampoNombre(formulario, errores, onFormularioChange, onErroresChange)
            CampoUsername(formulario, errores, onFormularioChange, onErroresChange)
            CampoCorreo(formulario, errores, onFormularioChange, onErroresChange)
            CampoTelefono(formulario, errores, onFormularioChange, onErroresChange)
            CampoContrasena(
                formulario,
                errores,
                mostrarContrasena,
                onFormularioChange,
                onErroresChange,
                onMostrarContrasenaChange
            )
            CampoConfirmarContrasena(
                formulario,
                errores,
                mostrarConfirmarContrasena,
                onFormularioChange,
                onErroresChange,
                onMostrarConfirmarContrasenaChange
            )
        }
    }

    // Sección de propietario/empleado
    SeccionPropietario(
        formulario = formulario,
        errores = errores,
        mostrarCampoCodigo = mostrarCampoCodigo,
        onFormularioChange = onFormularioChange,
        onErroresChange = onErroresChange
    )

    // Campo de código si es empleado
    if (mostrarCampoCodigo) {
        CampoCodigoInvitacion(
            formulario = formulario,
            errores = errores,
            onFormularioChange = onFormularioChange,
            onErroresChange = onErroresChange
        )
    }
}

@Composable
private fun CampoNombre(
    formulario: FormularioRegistro,
    errores: ErroresFormulario,
    onFormularioChange: (FormularioRegistro) -> Unit,
    onErroresChange: (ErroresFormulario) -> Unit
) {
    OutlinedTextField(
        value = formulario.nombre,
        onValueChange = {
            onFormularioChange(formulario.copy(nombre = it))
            if (errores.nombre.isNotEmpty()) {
                onErroresChange(errores.copy(nombre = ""))
            }
        },
        label = { Text("Nombre completo") },
        modifier = Modifier.fillMaxWidth(),
        isError = errores.nombre.isNotEmpty(),
        supportingText = {
            if (errores.nombre.isNotEmpty()) {
                Text(errores.nombre)
            }
        }
    )
}

@Composable
private fun CampoUsername(
    formulario: FormularioRegistro,
    errores: ErroresFormulario,
    onFormularioChange: (FormularioRegistro) -> Unit,
    onErroresChange: (ErroresFormulario) -> Unit
) {
    OutlinedTextField(
        value = formulario.username,
        onValueChange = {
            onFormularioChange(formulario.copy(username = it))
            if (errores.username.isNotEmpty()) {
                onErroresChange(errores.copy(username = ""))
            }
        },
        label = { Text("Nombre de usuario") },
        modifier = Modifier.fillMaxWidth(),
        isError = errores.username.isNotEmpty(),
        supportingText = {
            if (errores.username.isNotEmpty()) {
                Text(errores.username)
            }
        }
    )
}

@Composable
private fun CampoCorreo(
    formulario: FormularioRegistro,
    errores: ErroresFormulario,
    onFormularioChange: (FormularioRegistro) -> Unit,
    onErroresChange: (ErroresFormulario) -> Unit
) {
    OutlinedTextField(
        value = formulario.correo,
        onValueChange = {
            onFormularioChange(formulario.copy(correo = it))
            if (errores.correo.isNotEmpty()) {
                onErroresChange(errores.copy(correo = ""))
            }
        },
        label = { Text("Correo electrónico") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        isError = errores.correo.isNotEmpty(),
        supportingText = {
            if (errores.correo.isNotEmpty()) {
                Text(errores.correo)
            }
        }
    )
}

@Composable
private fun CampoTelefono(
    formulario: FormularioRegistro,
    errores: ErroresFormulario,
    onFormularioChange: (FormularioRegistro) -> Unit,
    onErroresChange: (ErroresFormulario) -> Unit
) {
    OutlinedTextField(
        value = formulario.telefono,
        onValueChange = {
            onFormularioChange(formulario.copy(telefono = it))
            if (errores.telefono.isNotEmpty()) {
                onErroresChange(errores.copy(telefono = ""))
            }
        },
        label = { Text("Teléfono") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth(),
        isError = errores.telefono.isNotEmpty(),
        supportingText = {
            if (errores.telefono.isNotEmpty()) {
                Text(errores.telefono)
            }
        }
    )
}

@Composable
private fun CampoContrasena(
    formulario: FormularioRegistro,
    errores: ErroresFormulario,
    mostrarContrasena: Boolean,
    onFormularioChange: (FormularioRegistro) -> Unit,
    onErroresChange: (ErroresFormulario) -> Unit,
    onMostrarContrasenaChange: (Boolean) -> Unit
) {
    OutlinedTextField(
        value = formulario.contrasena,
        onValueChange = {
            onFormularioChange(formulario.copy(contrasena = it))
            if (errores.contrasena.isNotEmpty()) {
                onErroresChange(errores.copy(contrasena = ""))
            }
        },
        label = { Text("Contraseña") },
        visualTransformation = if (mostrarContrasena) VisualTransformation.None
        else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { onMostrarContrasenaChange(!mostrarContrasena) }) {
                Icon(
                    imageVector = if (mostrarContrasena) Icons.Default.Visibility
                    else Icons.Default.VisibilityOff,
                    contentDescription = if (mostrarContrasena) "Ocultar contraseña"
                    else "Mostrar contraseña"
                )
            }
        },
        isError = errores.contrasena.isNotEmpty(),
        supportingText = {
            if (errores.contrasena.isNotEmpty()) {
                Text(errores.contrasena)
            } else if (formulario.contrasena.isNotEmpty()) {
                IndicadorFortalezaContrasena(formulario.contrasena)
            } else {
                Text("Mínimo 6 caracteres")
            }
        }
    )
}

@Composable
private fun CampoConfirmarContrasena(
    formulario: FormularioRegistro,
    errores: ErroresFormulario,
    mostrarConfirmarContrasena: Boolean,
    onFormularioChange: (FormularioRegistro) -> Unit,
    onErroresChange: (ErroresFormulario) -> Unit,
    onMostrarConfirmarContrasenaChange: (Boolean) -> Unit
) {
    OutlinedTextField(
        value = formulario.confirmarContrasena,
        onValueChange = {
            onFormularioChange(formulario.copy(confirmarContrasena = it))
            if (errores.confirmarContrasena.isNotEmpty()) {
                onErroresChange(errores.copy(confirmarContrasena = ""))
            }
        },
        label = { Text("Confirmar contraseña") },
        visualTransformation = if (mostrarConfirmarContrasena) VisualTransformation.None
        else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { onMostrarConfirmarContrasenaChange(!mostrarConfirmarContrasena) }) {
                Icon(
                    imageVector = if (mostrarConfirmarContrasena) Icons.Default.Visibility
                    else Icons.Default.VisibilityOff,
                    contentDescription = if (mostrarConfirmarContrasena) "Ocultar contraseña"
                    else "Mostrar contraseña"
                )
            }
        },
        isError = errores.confirmarContrasena.isNotEmpty(),
        supportingText = {
            if (errores.confirmarContrasena.isNotEmpty()) {
                Text(errores.confirmarContrasena)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeccionPropietario(
    formulario: FormularioRegistro,
    errores: ErroresFormulario,
    mostrarCampoCodigo: Boolean,
    onFormularioChange: (FormularioRegistro) -> Unit,
    onErroresChange: (ErroresFormulario) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "¿Eres el propietario del rancho?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0369A1)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Como dueño tendrás acceso completo para gestionar empleados, animales y reportes.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF475569)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = formulario.propietarioRancho,
                    onClick = {
                        onFormularioChange(
                            formulario.copy(
                                propietarioRancho = true,
                                codigoInvitacion = ""
                            )
                        )
                        // CORRECCIÓN: Cambiar errores.com por errores.copy
                        onErroresChange(errores.copy(codigoInvitacion = ""))
                    },
                    label = { Text("Sí, soy el propietario") },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = !formulario.propietarioRancho,
                    onClick = {
                        onFormularioChange(formulario.copy(propietarioRancho = false))
                    },
                    label = { Text("No, soy empleado") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
@Composable
private fun CampoCodigoInvitacion(
    formulario: FormularioRegistro,
    errores: ErroresFormulario,
    onFormularioChange: (FormularioRegistro) -> Unit,
    onErroresChange: (ErroresFormulario) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = formulario.codigoInvitacion,
                onValueChange = {
                    onFormularioChange(formulario.copy(codigoInvitacion = it))
                    if (errores.codigoInvitacion.isNotEmpty()) {
                        onErroresChange(errores.copy(codigoInvitacion = ""))
                    }
                },
                label = { Text("Código de invitación") },
                placeholder = { Text("Ingresa el código que te dio tu empleador") },
                modifier = Modifier.fillMaxWidth(),
                isError = errores.codigoInvitacion.isNotEmpty(),
                supportingText = {
                    if (errores.codigoInvitacion.isNotEmpty()) {
                        Text(errores.codigoInvitacion)
                    } else {
                        Text("Opcional - 6 caracteres alfanuméricos")
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Si tu empleador te dio un código, ingrésalo aquí para obtener permisos específicos.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF92400E)
            )
        }
    }
}

@Composable
private fun BotonesAccion(
    isLoading: Boolean,
    onRegistroClick: () -> Unit,
    onCancelarClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onRegistroClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Creando cuenta...")
                }
            } else {
                Text("Crear Cuenta")
            }
        }

        OutlinedButton(
            onClick = onCancelarClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading
        ) {
            Text("Cancelar")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {

            Text("¿Ya tienes cuenta? ")

            Text(
                "Inicia sesión",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onLoginClick()
                }
            )
        }
    }
}

// Función de validación optimizada
private fun validarFormulario(formulario: FormularioRegistro): ErroresFormulario {
    return ErroresFormulario(
        nombre = validarNombre(formulario.nombre),
        username = validarUsername(formulario.username),
        correo = validarCorreo(formulario.correo),
        telefono = validarTelefono(formulario.telefono),
        contrasena = validarContrasena(formulario.contrasena),
        confirmarContrasena = validarConfirmarContrasena(
            formulario.contrasena,
            formulario.confirmarContrasena
        ),
        codigoInvitacion = validarCodigoInvitacion(
            formulario.codigoInvitacion,
            formulario.propietarioRancho
        )
    )
}

private fun ErroresFormulario.tieneErrores(): Boolean {
    return nombre.isNotEmpty() || username.isNotEmpty() || correo.isNotEmpty() ||
            telefono.isNotEmpty() || contrasena.isNotEmpty() ||
            confirmarContrasena.isNotEmpty() || codigoInvitacion.isNotEmpty()
}

// Funciones de validación individuales (más mantenibles)
private fun validarNombre(nombre: String): String = when {
    nombre.isBlank() -> "El nombre es obligatorio"
    nombre.length < 2 -> "El nombre debe tener al menos 2 caracteres"
    else -> ""
}

private fun validarUsername(username: String): String = when {
    username.isBlank() -> "El nombre de usuario es obligatorio"
    username.length < 3 -> "El usuario debe tener al menos 3 caracteres"
    !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> "Solo letras, números y guiones bajos"
    else -> ""
}

private fun validarCorreo(correo: String): String = when {
    correo.isBlank() -> "El correo es obligatorio"
    !isValidEmail(correo) -> "Formato de correo inválido"
    else -> ""
}

private fun validarTelefono(telefono: String): String = when {
    telefono.isBlank() -> "El teléfono es obligatorio"
    telefono.length < 10 -> "El teléfono debe tener al menos 10 dígitos"
    !telefono.matches(Regex("^[0-9+]+\$")) -> "Solo números y el signo +"
    else -> ""
}

private fun validarContrasena(contrasena: String): String = when {
    contrasena.isBlank() -> "La contraseña es obligatoria"
    contrasena.length < 6 -> "Mínimo 6 caracteres"
    else -> ""
}

private fun validarConfirmarContrasena(contrasena: String, confirmarContrasena: String): String = when {
    confirmarContrasena.isBlank() -> "Confirma tu contraseña"
    contrasena != confirmarContrasena -> "Las contraseñas no coinciden"
    else -> ""
}

private fun validarCodigoInvitacion(codigo: String, esPropietario: Boolean): String = when {
    esPropietario -> "" // No se requiere código para propietarios
    codigo.isBlank() -> "El código de invitación es obligatorio para empleados."
    codigo.length  != 6 -> "El código debe tener al menos 6 caracteres"
    !codigo.matches(Regex("^[A-Za-z0-9]+\$")) -> "Solo letras y números"
    else -> ""
}

@Composable
private fun IndicadorFortalezaContrasena(contrasena: String) {
    val (fortaleza, color) = calcularFortalezaContrasena(contrasena)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Fortaleza:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(
            fortaleza,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun calcularFortalezaContrasena(contrasena: String): Pair<String, Color> {
    return when {
        contrasena.length < 6 -> Pair("Débil", Color(0xFFDC2626))
        contrasena.length < 8 -> Pair("Media", Color(0xFFD97706))
        contrasena.matches(Regex(".*[A-Z].*")) &&
                contrasena.matches(Regex(".*[0-9].*")) &&
                contrasena.matches(Regex(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) ->
            Pair("Muy Fuerte", Color(0xFF059669))
        contrasena.matches(Regex(".*[A-Z].*")) &&
                contrasena.matches(Regex(".*[0-9].*")) ->
            Pair("Fuerte", Color(0xFF16A34A))
        else -> Pair("Media", Color(0xFFD97706))
    }
}

private fun isValidEmail(email: String): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
    return email.matches(emailRegex)
}

// Función principal de procesamiento de registro (optimizada)
private suspend fun procesarRegistro(formulario: FormularioRegistro): ResultadoRegistro {
    var userCreated = false
    val auth = FirebaseAuth.getInstance()

    return try {
        // Verificaciones en paralelo
        val (usuarioOcupado, correoOcupado) = verificarDisponibilidadParallel(formulario.username, formulario.correo)

        if (usuarioOcupado) return ResultadoRegistro(false, "El nombre de usuario ya está en uso")
        if (correoOcupado) return ResultadoRegistro(false, "El correo electrónico ya está registrado")

        // Determinar rol ANTES de crear usuario
        val (rol, permisos, adminIdDelCodigo) = if (formulario.propietarioRancho) {
            Triple(
                "admin",
                listOf("leer", "crear", "actualizar", "eliminar", "gestionar_usuarios", "generar_codigos"),
                null // propietarios no necesitan adminId del código
            )
        } else {
            // Retorna: rol, permisos y adminId del dueño asociado al código
            procesarCodigoInvitacionAntes(formulario.codigoInvitacion)
        }

        // Crear usuario en Firebase Auth
        val authResult = auth.createUserWithEmailAndPassword(formulario.correo, formulario.contrasena).await()
        val user = authResult.user ?: return ResultadoRegistro(false, "Error al crear usuario")
        userCreated = true

        // Operaciones en paralelo después del registro
        // Se pasa el adminIdDelCodigo y el código de invitación para que la función resuelva el adminIdFinal
        realizarOperacionesParalelas(
            user = user,
            formulario = formulario,
            rol = rol,
            permisos = permisos,
            adminIdDelCodigo = adminIdDelCodigo,
            codigoInvitacion = formulario.codigoInvitacion
        )

        // Mensaje de éxito
        val mensaje = when {
            formulario.propietarioRancho -> "✅ Propietario registrado exitosamente. Puedes generar códigos para tus empleados."
            rol == "veterinario" -> "✅ Veterinario registrado exitosamente"
            rol == "admin" -> "✅ Administrador registrado exitosamente"
            else -> "✅ Empleado registrado exitosamente"
        }

        ResultadoRegistro(true, mensaje, rol)

    } catch (e: Exception) {
        // Rollback si es necesario
        if (userCreated) {
            try {
                auth.currentUser?.delete()?.await()
            } catch (deleteError: Exception) {
                println("Error al eliminar usuario después de fallo: ${deleteError.message}")
            }
        }
        manejarErrorRegistro(e)
    }
}

// Funciones auxiliares optimizadas
private suspend fun verificarDisponibilidadParallel(username: String, email: String): Pair<Boolean, Boolean> =
    kotlinx.coroutines.coroutineScope {
        val usuarioDeferred = async {
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .whereEqualTo("username", username)
                .get()
                .await()
                .isEmpty.not()
        }
        val correoDeferred = async {
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .await()
                .isEmpty.not()
        }
        Pair(usuarioDeferred.await(), correoDeferred.await())
    }

private suspend fun procesarCodigoInvitacionAntes(codigo: String): Triple<String, List<String>, String?> {
    if (codigo.isBlank()) {
        return Triple("empleado", listOf("leer", "crear", "actualizar"), null)
    }
    return try {
        // Usando el repository para verificar el código
        val codigoUsado = CodigoInvitacionRepository.verificarCodigoSinUsar(codigo)
        if (codigoUsado != null) {
            val permisos = when (codigoUsado.tipo) {
                "admin" -> listOf("leer", "crear", "actualizar", "eliminar", "gestionar_usuarios", "generar_codigos")
                "veterinario" -> listOf("leer", "crear", "actualizar", "gestionar_animales", "registrar_salud", "ver_reportes_medicos")
                "supervisor" -> listOf("leer", "crear", "actualizar", "gestionar_animales", "ver_reportes")
                else -> listOf("leer", "crear", "actualizar")
            }
            Triple(codigoUsado.tipo, permisos, codigoUsado.adminId)
        } else {
            Triple("empleado", listOf("leer", "crear", "actualizar"), null)
        }
    } catch (e: Exception) {
        Triple("empleado", listOf("leer", "crear", "actualizar"), null)
    }
}

// UBICACIÓN: Función realizarOperacionesParalelas (Reemplazo completo)
private suspend fun realizarOperacionesParalelas(
    user: com.google.firebase.auth.FirebaseUser,
    formulario: FormularioRegistro,
    rol: String,
    permisos: List<String>,
    // Cambiamos el nombre del parámetro para reflejar que es el ID potencial del dueño
    adminIdDelCodigo: String?,
    codigoInvitacion: String // El código que se usará
) = kotlinx.coroutines.coroutineScope {
    val db = FirebaseFirestore.getInstance()

    // --- Lógica para determinar el adminId FINAL ---
    var adminIdFinal: String = user.uid // Valor por defecto: el UID del propio usuario

    // Tarea para usar el código de invitación y determinar el adminId final
    val procesarVinculacion = async {
        if (formulario.propietarioRancho) {
            // Propietario: Su rancho es él mismo.
            adminIdFinal = user.uid
        } else if (codigoInvitacion.isNotBlank()) {
            // Empleado con código: Intentar usar el código de forma atómica.
            val codigoUsado = CodigoInvitacionRepository.verificarYUsarCodigo(
                codigoInvitacion,
                user.uid // Pasamos el UID real del nuevo usuario
            )

            if (codigoUsado != null) {
                // Éxito: Código usado y obtenemos el ID del rancho.
                adminIdFinal = codigoUsado.adminId
            } else {
                // Falla crítica: El código se invalidó entre la validación inicial y este punto.
                // Lanzar excepción para forzar el rollback y eliminar la cuenta recién creada.
                throw Exception("El código de invitación no se pudo usar o ha caducado/agotado sus usos.")
            }
        } else {
            // Esto solo ocurriría si la validación falla, pero por seguridad, usamos el ID del código
            // que fue validado previamente, o el fallback si todo falla.
            adminIdFinal = adminIdDelCodigo ?: user.uid
        }
        adminIdFinal // Retorna el adminIdFinal calculado
    }

    val actualizarPerfil = async {
        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(formulario.nombre)
                .build()
        ).await()
    }

    // Esperamos a que la vinculación se resuelva para obtener el adminIdFinal
    val finalId = procesarVinculacion.await() // Se resuelve el adminIdFinal o lanza excepción

    val guardarFirestore = async {
        db.collection("usuarios").document(user.uid).set(
            hashMapOf(
                "uid" to user.uid,
                "username" to formulario.username,
                "email" to formulario.correo,
                "nombre" to formulario.nombre,
                "telefono" to formulario.telefono,
                "rol" to rol,
                "permisos" to permisos,
                "adminId" to finalId, // <--- ¡USAMOS EL ID FINAL CORRECTO!
                "propietarioRancho" to formulario.propietarioRancho,
                "fechaRegistro" to com.google.firebase.Timestamp.now(),
                "activo" to true,
                "ultimoAcceso" to com.google.firebase.Timestamp.now(),
                "codigoInvitacionUsado" to formulario.codigoInvitacion.ifBlank { null }
            )
        ).await()
    }

    val generarCodigo = async {
        if (formulario.propietarioRancho) {
            // ... (Tu lógica existente que usa user.uid)
            try {
                CodigoInvitacionRepository.crearCodigoInvitacion(
                    adminId = user.uid,
                    tipo = "admin",
                    usosTotales = 10,
                    diasExpiracion = 365
                )
                true
            } catch (e: Exception) {
                false
            }
        } else false
    }

    // Ejecutar tareas:
    actualizarPerfil.await()
    guardarFirestore.await()
    generarCodigo.await()
}
private fun manejarErrorRegistro(e: Exception): ResultadoRegistro {
    return when {
        e.message?.contains("email address is already in use") == true ->
            ResultadoRegistro(false, "El correo electrónico ya está registrado")
        e.message?.contains("network error") == true || e.message?.contains("TIMEOUT") == true ->
            ResultadoRegistro(false, "Error de conexión. Verifica tu internet")
        e.message?.contains("invalid email") == true ->
            ResultadoRegistro(false, "Formato de correo electrónico inválido")
        e.message?.contains("password is too weak") == true || e.message?.contains("WEAK_PASSWORD") == true ->
            ResultadoRegistro(false, "La contraseña es demasiado débil. Use al menos 6 caracteres")
        else -> ResultadoRegistro(false, "Error al registrar: ${e.localizedMessage ?: "Intenta nuevamente"}")
    }
}