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


/**
 * Pantalla Composable para el registro de nuevos usuarios en el sistema.
 *
 * Esta pantalla recopila la información del usuario (nombre, usuario, correo, contraseña),
 * gestiona la validación de campos, y determina el rol del usuario ('admin' para propietarios
 * o 'empleado'/otro para subordinados) basado en la selección y un código de invitación.
 *
 * Utiliza [AuthViewModel] para la lógica de registro con Firebase Auth y Firestore.
 * El proceso incluye:
 * 1. Validación de campos locales.
 * 2. Verificación de disponibilidad de usuario/correo en paralelo.
 * 3. Procesamiento del código de invitación (si aplica) para obtener rol/permisos.
 * 4. Creación de usuario en Firebase Auth.
 * 5. Almacenamiento de datos del usuario (rol, permisos, adminId) en Firestore.
 * 6. Manejo de errores y rollback (eliminación de cuenta de Auth si falla Firestore).
 *
 * @param navController El controlador de navegación para cambiar de pantalla.
 * @param viewModel El [AuthViewModel] que maneja la lógica de autenticación y datos de usuario.
 */
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

/**
 * Componente Composable que agrupa todos los campos de entrada y la lógica de estado del formulario
 * de registro.
 *
 * Se encarga de mostrar la información básica del usuario en una tarjeta y maneja la visibilidad
 * condicional de la sección de rol (Propietario/Empleado) y el campo de código de invitación.
 *
 * @param formulario El estado actual de los datos del formulario (Nombre, Correo, Contraseña, etc.).
 * @param errores El estado actual de los mensajes de error de validación para cada campo.
 * @param mostrarContrasena Estado booleano para controlar la visibilidad del texto de la Contraseña.
 * @param mostrarConfirmarContrasena Estado booleano para controlar la visibilidad del texto de Confirmar Contraseña.
 * @param mostrarCampoCodigo Indica si el campo de código de invitación debe ser visible (si es empleado).
 * @param onFormularioChange Callback para actualizar el estado del formulario principal.
 * @param onErroresChange Callback para actualizar el estado de los errores de validación.
 * @param onMostrarContrasenaChange Callback para alternar el estado de visibilidad de la Contraseña.
 * @param onMostrarConfirmarContrasenaChange Callback para alternar el estado de visibilidad de Confirmar Contraseña.
 */
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


/**
 * Componente Composable que representa el campo de texto para ingresar el **Nombre completo**
 * en el formulario de registro.
 *
 * Se encarga de:
 * 1. Mostrar el valor actual del campo [formulario.nombre].
 * 2. Actualizar el estado del formulario general a través de [onFormularioChange].
 * 3. Limpiar el mensaje de error [errores.nombre] tan pronto como el usuario comienza a escribir
 * después de un error de validación.
 * 4. Mostrar el mensaje de error de validación en el texto de soporte (`supportingText`)
 * si [errores.nombre] no está vacío.
 *
 * @param formulario El estado completo de los datos de registro (contiene el valor actual del nombre).
 * @param errores El estado que contiene los mensajes de error de validación.
 * @param onFormularioChange Callback para actualizar el estado del formulario principal.
 * @param onErroresChange Callback para actualizar/limpiar los mensajes de error de validación.
 */
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


/**
 * Componente Composable que representa el campo de texto para ingresar el **Nombre de Usuario**
 * en el formulario de registro.
 *
 * Se encarga de:
 * 1. Mostrar el valor actual del campo [formulario.username].
 * 2. Actualizar el estado del formulario general a través de [onFormularioChange] cada vez que
 * el usuario escribe.
 * 3. Implementar la "corrección inmediata de errores": si había un mensaje de error previo
 * ([errores.username] no estaba vacío), este se limpia a través de [onErroresChange]
 * tan pronto como el usuario modifica el contenido del campo, permitiendo una validación
 * asíncrona posterior o al guardar.
 * 4. Mostrar visualmente el campo como un error (borde rojo) y el mensaje de error
 * específico en el texto de soporte (`supportingText`) si [errores.username] no está vacío.
 *
 * @param formulario El estado completo de los datos de registro (contiene el valor actual del username).
 * @param errores El estado que contiene los mensajes de error de validación.
 * @param onFormularioChange Callback para actualizar el estado del formulario principal.
 * @param onErroresChange Callback para actualizar/limpiar los mensajes de error de validación.
 */
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

/**
 * Componente Composable que representa el campo de texto para ingresar el **Correo Electrónico**
 * en el formulario de registro.
 *
 * Se encarga de:
 * 1. Mostrar el valor actual de [formulario.correo].
 * 2. Utilizar [KeyboardType.Email] para optimizar la entrada de datos en dispositivos móviles,
 * mostrando caracteres comunes en correos (como '@' y '.').
 * 3. Actualizar el estado de [formulario.correo] a través de [onFormularioChange] cada vez que
 * el usuario escribe.
 * 4. Implementar la limpieza inmediata del mensaje de error ([errores.correo]) a través de
 * [onErroresChange] al modificar el campo, siguiendo la convención de UI del formulario.
 * 5. Mostrar visualmente el campo como un error (borde y coloración) y el mensaje de error
 * específico en el texto de soporte (`supportingText`) si la validación del correo falla
 * y [errores.correo] no está vacío.
 *
 * @param formulario El estado completo de los datos de registro (contiene el valor actual del correo).
 * @param errores El estado que contiene los mensajes de error de validación.
 * @param onFormularioChange Callback para actualizar el estado del formulario principal.
 * @param onErroresChange Callback para actualizar/limpiar los mensajes de error de validación.
 */
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


/**
 * Componente Composable que representa el campo de texto para ingresar el **Teléfono**
 * en el formulario de registro.
 *
 * Se encarga de:
 * 1. Mostrar el valor actual de [formulario.telefono].
 * 2. Utilizar [KeyboardType.Phone] para optimizar la entrada de dígitos y el signo '+'.
 * 3. Actualizar el estado de [formulario.telefono] a través de [onFormularioChange] con cada cambio.
 * 4. Limpiar el mensaje de error ([errores.telefono]) al modificar el campo, siguiendo la
 * convención de UI del formulario.
 * 5. Mostrar visualmente el campo como un error y el mensaje de error específico en el
 * texto de soporte (`supportingText`) si la validación falla.
 *
 * @param formulario El estado completo de los datos de registro (contiene el valor actual del teléfono).
 * @param errores El estado que contiene los mensajes de error de validación.
 * @param onFormularioChange Callback para actualizar el estado del formulario principal.
 * @param onErroresChange Callback para actualizar/limpiar los mensajes de error de validación.
 */
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


/**
 * Componente Composable que representa el campo de texto para ingresar la **Contraseña**.
 *
 * Este componente es crucial para la seguridad y la experiencia del usuario, ya que incluye:
 * 1. **Visual Transformation:** Oculta el texto por defecto usando [PasswordVisualTransformation],
 * pero permite alternar la visibilidad mediante el ícono [Icons.Default.Visibility/VisibilityOff].
 * 2. **Ícono de Visibilidad (Trailing Icon):** Permite al usuario mostrar u ocultar la contraseña
 * mediante el callback [onMostrarContrasenaChange].
 * 3. **Teclado:** Utiliza [KeyboardType.Password].
 * 4. **Indicador de Fortaleza:** Muestra la fortaleza de la contraseña (utilizando la función
 * externa `IndicadorFortalezaContrasena`) cuando el campo no está vacío ni tiene errores de validación.
 * 5. **Manejo de Errores:** Muestra errores de validación (`isError` y `supportingText`)
 * definidos en [errores.contrasena].
 *
 * @param formulario El estado completo de los datos (contiene el valor actual de la contraseña).
 * @param errores El estado que contiene los mensajes de error de validación.
 * @param mostrarContrasena Estado que indica si la contraseña debe ser visible o estar oculta.
 * @param onFormularioChange Callback para actualizar el estado del formulario principal.
 * @param onErroresChange Callback para actualizar/limpiar los mensajes de error de validación.
 * @param onMostrarContrasenaChange Callback para alternar el estado de visibilidad de la contraseña.
 */
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


/**
 * Componente Composable que representa el campo de texto para **Confirmar Contraseña**.
 *
 * Su propósito principal es asegurar que el usuario ha introducido la contraseña deseada
 * correctamente. Sus funcionalidades clave son:
 * 1. **Visual Transformation:** Oculta el texto por defecto, pero permite alternar la
 * visibilidad mediante el ícono de ojo (utilizando [onMostrarConfirmarContrasenaChange]).
 * 2. **Comparación:** La validación real se realiza externamente (en `validarFormulario`),
 * que compara este valor con el campo de contraseña original.
 * 3. **Manejo de Errores:** Muestra errores de validación (`isError` y `supportingText`)
 * definidos en [errores.confirmarContrasena], generalmente indicando que las contraseñas no
 * coinciden o que el campo está vacío.
 *
 * @param formulario El estado completo de los datos (contiene el valor actual de la confirmación).
 * @param errores El estado que contiene los mensajes de error de validación.
 * @param mostrarConfirmarContrasena Estado que indica si la confirmación debe ser visible o estar oculta.
 * @param onFormularioChange Callback para actualizar el estado del formulario principal.
 * @param onErroresChange Callback para actualizar/limpiar los mensajes de error de validación.
 * @param onMostrarConfirmarContrasenaChange Callback para alternar el estado de visibilidad.
 */
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


/**
 * Componente Composable que permite al usuario seleccionar su rol inicial: **Propietario del Rancho**
 * o **Empleado**.
 *
 * Esta selección es fundamental ya que determina:
 * 1. **Rol de Base de Datos:** Los propietarios se registran con rol 'admin', los empleados con rol 'empleado'
 * (o rol específico asociado a un código).
 * 2. **Visibilidad del Código de Invitación:** Si el usuario selecciona "No, soy empleado"
 * ([formulario.propietarioRancho] es `false`), se habilita el campo [CampoCodigoInvitacion].
 * 3. **Lógica de Limpieza:** Al seleccionar "Sí, soy el propietario", se asegura que el campo
 * `codigoInvitacion` se limpie y se borre su error asociado.
 *
 * @param formulario El estado completo de los datos (se utiliza para leer/actualizar `propietarioRancho` y `codigoInvitacion`).
 * @param errores El estado que contiene los mensajes de error de validación.
 * @param mostrarCampoCodigo Indica si el campo de código debería ser visible (propiedad derivada).
 * @param onFormularioChange Callback para actualizar el estado del formulario principal.
 * @param onErroresChange Callback para actualizar/limpiar los mensajes de error de validación.
 */
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

/**
 * Componente Composable que permite al usuario (empleado) ingresar un **Código de Invitación**.
 *
 * Este campo es condicional y solo es visible si el usuario se ha identificado como "Empleado"
 * en [SeccionPropietario].
 *
 * Funcionalidades:
 * 1. **Contenedor Visual:** Se presenta dentro de una Card con un color distintivo (amarillo pálido)
 * para destacar su importancia y naturaleza opcional/secundaria.
 * 2. **Actualización de Estado:** Actualiza el valor de [formulario.codigoInvitacion] con cada cambio.
 * 3. **Manejo de Errores:** Muestra errores de validación (`isError` y `supportingText`) definidos
 * en [errores.codigoInvitacion], típicamente relacionados con la longitud o el formato del código.
 * 4. **Texto de Ayuda:** Proporciona un texto explicativo sobre el propósito del código de invitación.
 *
 * @param formulario El estado completo de los datos (contiene el valor actual del código).
 * @param errores El estado que contiene los mensajes de error de validación.
 * @param onFormularioChange Callback para actualizar el estado del formulario principal.
 * @param onErroresChange Callback para actualizar/limpiar los mensajes de error de validación.
 */
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


/**
 * Componente Composable que agrupa los botones principales de acción del formulario de registro:
 * Registrar, Cancelar y Enlace a Iniciar Sesión.
 *
 * Funcionalidades clave:
 * 1. **Registro:** El botón principal ("Crear Cuenta") ejecuta [onRegistroClick].
 * 2. **Estado de Carga (Loading):** Cuando [isLoading] es `true`, el botón de registro
 * se deshabilita, se muestra un [CircularProgressIndicator] y el texto "Creando cuenta...",
 * previniendo múltiples envíos.
 * 3. **Navegación:** Permite [onCancelarClick] (volver atrás) y [onLoginClick] (navegar a la pantalla de inicio de sesión).
 * 4. **Estilo:** El enlace a Iniciar Sesión se presenta como texto clickeable.
 *
 * @param isLoading Indica si una operación asíncrona (registro) está en curso.
 * @param onRegistroClick Callback ejecutado al presionar el botón de "Crear Cuenta".
 * @param onCancelarClick Callback ejecutado al presionar el botón "Cancelar" (normalmente popBackStack).
 * @param onLoginClick Callback ejecutado al presionar el enlace "Inicia sesión".
 */
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

/**
 * Función central de validación del formulario de registro.
 *
 * Ejecuta todas las funciones de validación individuales sobre los campos del [formulario]
 * y recopila todos los mensajes de error resultantes en un único objeto [ErroresFormulario].
 *
 * @param formulario El estado completo de los datos ingresados por el usuario ([FormularioRegistro]).
 * @return Un objeto [ErroresFormulario] donde cada propiedad contiene el mensaje de error
 * correspondiente si el campo es inválido, o una cadena vacía si es válido.
 */
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


/**
 * Función de extensión para la clase [ErroresFormulario] que verifica rápidamente
 * si existe algún error de validación en el formulario.
 *
 * Esta se utiliza en el botón de "Crear Cuenta" para determinar si debe detenerse
 * y mostrar los errores, o proceder con el proceso de registro asíncrono.
 *
 * @return `true` si al menos uno de los campos de error ([nombre], [username], [correo], etc.)
 * contiene un mensaje (es decir, no está vacío), `false` en caso contrario.
 */
private fun ErroresFormulario.tieneErrores(): Boolean {
    return nombre.isNotEmpty() || username.isNotEmpty() || correo.isNotEmpty() ||
            telefono.isNotEmpty() || contrasena.isNotEmpty() ||
            confirmarContrasena.isNotEmpty() || codigoInvitacion.isNotEmpty()
}

/**
 * Valida el campo de Nombre Completo.
 *
 * Requisitos:
 * 1. No debe estar vacío.
 * 2. Debe tener una longitud mínima de 2 caracteres.
 *
 * @param nombre El valor del campo nombre.
 * @return Mensaje de error o cadena vacía ("").
 */
private fun validarNombre(nombre: String): String = when {
    nombre.isBlank() -> "El nombre es obligatorio"
    nombre.length < 2 -> "El nombre debe tener al menos 2 caracteres"
    else -> ""
}

/**
 * Valida el campo de Nombre de Usuario.
 *
 * Requisitos:
 * 1. No debe estar vacío.
 * 2. Debe tener una longitud mínima de 3 caracteres.
 * 3. Solo debe contener letras, números y guiones bajos (alfanumérico con underscore).
 *
 * @param username El valor del campo nombre de usuario.
 * @return Mensaje de error o cadena vacía ("").
 */
private fun validarUsername(username: String): String = when {
    username.isBlank() -> "El nombre de usuario es obligatorio"
    username.length < 3 -> "El usuario debe tener al menos 3 caracteres"
    !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> "Solo letras, números y guiones bajos"
    else -> ""
}

/**
 * Valida el campo de Correo Electrónico.
 *
 * Requisitos:
 * 1. No debe estar vacío.
 * 2. Debe tener un formato de correo electrónico válido (utiliza la función externa [isValidEmail]).
 *
 * @param correo El valor del campo correo electrónico.
 * @return Mensaje de error o cadena vacía ("").
 */
private fun validarCorreo(correo: String): String = when {
    correo.isBlank() -> "El correo es obligatorio"
    !isValidEmail(correo) -> "Formato de correo inválido"
    else -> ""
}

/**
 * Valida el campo de Teléfono.
 *
 * Requisitos:
 * 1. No debe estar vacío.
 * 2. Debe tener una longitud mínima de 10 dígitos.
 * 3. Solo debe contener números del 0 al 9 y opcionalmente el signo de más (+).
 *
 * @param telefono El valor del campo teléfono.
 * @return Mensaje de error o cadena vacía ("").
 */
private fun validarTelefono(telefono: String): String = when {
    telefono.isBlank() -> "El teléfono es obligatorio"
    telefono.length < 10 -> "El teléfono debe tener al menos 10 dígitos"
    !telefono.matches(Regex("^[0-9+]+\$")) -> "Solo números y el signo +"
    else -> ""
}

/**
 * Valida el campo de Contraseña.
 *
 * Requisitos:
 * 1. No debe estar vacío.
 * 2. Debe tener una longitud mínima de 6 caracteres (requisito mínimo de Firebase Auth).
 *
 * @param contrasena El valor del campo contraseña.
 * @return Mensaje de error o cadena vacía ("").
 */
private fun validarContrasena(contrasena: String): String = when {
    contrasena.isBlank() -> "La contraseña es obligatoria"
    contrasena.length < 6 -> "Mínimo 6 caracteres"
    else -> ""
}

/**
 * Valida el campo de Confirmar Contraseña.
 *
 * Requisitos:
 * 1. No debe estar vacío.
 * 2. Debe ser exactamente igual al valor de la contraseña original ([contrasena]).
 *
 * @param contrasena El valor de la contraseña original.
 * @param confirmarContrasena El valor del campo de confirmación.
 * @return Mensaje de error o cadena vacía ("").
 */
private fun validarConfirmarContrasena(contrasena: String, confirmarContrasena: String): String = when {
    confirmarContrasena.isBlank() -> "Confirma tu contraseña"
    contrasena != confirmarContrasena -> "Las contraseñas no coinciden"
    else -> ""
}

/**
 * Valida el campo de Código de Invitación.
 *
 * La validación es condicional:
 * 1. Si [esPropietario] es `true`, no se requiere el código y siempre retorna éxito.
 * 2. Si es empleado, el código es obligatorio y debe cumplir con las siguientes reglas:
 * a. No debe estar vacío.
 * b. Debe tener una longitud exacta de 6 caracteres.
 * c. Solo debe contener caracteres alfanuméricos (letras y números).
 *
 * @param codigo El valor del código de invitación.
 * @param esPropietario Bandera que indica si el usuario se ha seleccionado como propietario.
 * @return Mensaje de error o cadena vacía ("").
 */
private fun validarCodigoInvitacion(codigo: String, esPropietario: Boolean): String = when {
    esPropietario -> "" // No se requiere código para propietarios
    codigo.isBlank() -> "El código de invitación es obligatorio para empleados."
    codigo.length  != 6 -> "El código debe tener al menos 6 caracteres"
    !codigo.matches(Regex("^[A-Za-z0-9]+\$")) -> "Solo letras y números"
    else -> ""
}

/**
 * Componente Composable que muestra visualmente el nivel de **fortaleza de la contraseña**
 * actual, utilizando una etiqueta de texto y un color asociado.
 *
 * Delega la lógica de cálculo a la función auxiliar [calcularFortalezaContrasena].
 * Se muestra dentro del `supportingText` del campo de contraseña.
 *
 * @param contrasena La cadena de la contraseña a evaluar.
 */
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

/**
 * Lógica para calcular la fortaleza de una contraseña basada en su longitud y la presencia
 * de diferentes tipos de caracteres.
 *
 * Reglas de Fortaleza:
 * 1. **Débil:** Menos de 6 caracteres.
 * 2. **Media:** Entre 6 y 7 caracteres, o no cumple criterios de Fuerte/Muy Fuerte.
 * 3. **Fuerte:** 8 o más caracteres Y contiene mayúsculas Y números.
 * 4. **Muy Fuerte:** 8 o más caracteres Y contiene mayúsculas, números Y símbolos/caracteres especiales.
 *
 * @param contrasena La cadena de la contraseña a evaluar.
 * @return Un [Pair] que contiene el nivel de fortaleza como [String] ("Débil", "Media", etc.)
 * y el [Color] asociado para la UI.
 */
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

/**
 * Función auxiliar para verificar si una cadena de texto coincide con un patrón básico
 * de correo electrónico.
 *
 * Utiliza una expresión regular (`emailRegex`) para validar el formato estándar.
 *
 * @param email La cadena de correo electrónico a validar.
 * @return `true` si el formato del email es válido según la Regex, `false` en caso contrario.
 */
private fun isValidEmail(email: String): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
    return email.matches(emailRegex)
}

/**
 * Función principal y suspendida que gestiona la lógica de negocio completa para el registro
 * de un nuevo usuario.
 *
 * El proceso sigue varios pasos cruciales, la mayoría ejecutados de forma asíncrona:
 *
 * ## Flujo de Ejecución
 * 1. **Verificaciones en Paralelo:** Llama a [verificarDisponibilidadParallel] para comprobar
 * si el `username` o el `correo` ya están en uso en Firestore, abortando si hay duplicados.
 * 2. **Determinación de Rol Inicial:** Usa [procesarCodigoInvitacionAntes] (o la selección de propietario)
 * para obtener el rol, permisos y el ID del rancho (`adminId`) **antes** de crear la cuenta.
 * 3. **Creación de Auth:** Crea el usuario en **Firebase Authentication**.
 * 4. **Operaciones Post-Registro en Paralelo:** Llama a [realizarOperacionesParalelas]
 * para completar el perfil (Display Name) y guardar el documento de usuario (con rol, permisos y `adminId`)
 * en Firestore, además de usar el código de invitación de forma atómica.
 * 5. **Manejo de Transacciones y Rollback:** Utiliza un bloque `try-catch` y la bandera `userCreated`
 * para intentar eliminar el usuario de Firebase Auth si cualquier paso posterior (Firestore/código) falla.
 *
 * @param formulario Los datos validados del formulario [FormularioRegistro].
 * @return Un objeto [ResultadoRegistro] que indica el éxito o fracaso de la operación, junto con un mensaje.
 */
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

/**
 * Realiza una verificación asíncrona y en paralelo en Firestore para determinar si
 * el nombre de usuario o el correo electrónico ya están en uso.
 *
 * Utiliza [kotlinx.coroutines.coroutineScope] y [async] para ejecutar ambas consultas
 * de Firestore simultáneamente, optimizando el tiempo de espera.
 *
 * @param username El nombre de usuario propuesto.
 * @param email El correo electrónico propuesto.
 * @return Un [Pair] donde:
 * - El primer valor ([Boolean]) es `true` si el nombre de usuario ya existe.
 * - El segundo valor ([Boolean]) es `true` si el correo electrónico ya existe.
 */
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

/**
 * Determina el rol, permisos base y el ID del administrador asociado (rancho dueño)
 * a partir de un código de invitación, **antes** de que se cree la cuenta en Firebase Auth.
 *
 * Esta verificación es preliminar. La confirmación atómica del uso del código ocurre
 * posteriormente en [realizarOperacionesParalelas].
 *
 * Lógica:
 * - Si el código está vacío, asigna el rol de "empleado" con permisos básicos.
 * - Si el código es válido (verificado por [CodigoInvitacionRepository]), asigna el
 * rol y permisos basados en el tipo de código ("admin", "veterinario", "supervisor", etc.)
 * y devuelve el ID del administrador ([adminId]) al que pertenece el rancho.
 *
 * @param codigo El código de invitación ingresado por el usuario.
 * @return Un [Triple] que contiene: (rol: String, permisos: List<String>, adminId: String? o null).
 */
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

/**
 * Ejecuta de forma asíncrona y paralela todas las operaciones post-registro
 * cruciales en Firebase, utilizando el UID del usuario recién creado.
 *
 * **Pasos clave ejecutados en paralelo:**
 * 1. **Procesar Vinculación (async 'procesarVinculacion'):** Determina el [adminIdFinal] del rancho.
 * - Si es propietario, [adminIdFinal] es su propio UID.
 * - Si es empleado con código, llama a `verificarYUsarCodigo` del Repository para **marcar el código como usado**
 * de forma atómica y obtiene el [adminId] del dueño. Lanza una excepción si el uso falla, forzando un rollback.
 * 2. **Actualizar Perfil (async 'actualizarPerfil'):** Establece el `displayName` de Firebase Auth al nombre del usuario.
 * 3. **Guardar en Firestore (async 'guardarFirestore'):** Crea el documento de usuario en la colección "usuarios",
 * asignando el [rol], [permisos], y, crucialmente, el **[adminIdFinal]** resuelto en el paso 1.
 * 4. **Generar Código (async 'generarCodigo'):** Si el usuario es propietario, genera automáticamente un código de
 * invitación inicial para que pueda empezar a registrar a sus empleados.
 *
 * Esta función garantiza la coherencia de los datos después de la creación inicial del usuario en Auth.
 *
 * @param user El [FirebaseUser] recién creado en la autenticación.
 * @param formulario Los datos finales del formulario [FormularioRegistro].
 * @param rol El rol asignado al usuario (ej. "admin", "veterinario").
 * @param permisos La lista de permisos asignados.
 * @param adminIdDelCodigo El ID de administrador potencial obtenido de la verificación previa del código.
 * @param codigoInvitacion El código de invitación utilizado (si aplica).
 */
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

/**
 * Función auxiliar utilizada dentro de [procesarRegistro] para interpretar las excepciones
 * de registro lanzadas por Firebase Authentication o durante las operaciones asíncronas
 * y convertirlas en un objeto [ResultadoRegistro] con un mensaje de error legible
 * y amigable para el usuario.
 *
 * ## Errores Manejados:
 * 1. **Correo ya en uso:** Mapea errores de "email address is already in use".
 * 2. **Red/Timeout:** Maneja errores de conexión o tiempo de espera.
 * 3. **Correo Inválido:** Mapea errores de "invalid email".
 * 4. **Contraseña Débil:** Mapea errores de "password is too weak" (aunque la validación
 * de longitud se hace localmente, esto cubre posibles requisitos adicionales de Firebase).
 * 5. **General:** Cualquier otra excepción se devuelve con un mensaje genérico.
 *
 * @param e La excepción lanzada durante el proceso de registro (generalmente un `FirebaseAuthException`).
 * @return Un objeto [ResultadoRegistro] con `exitoso = false` y el mensaje de error traducido.
 */
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