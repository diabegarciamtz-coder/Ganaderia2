package mx.edu.utng.lojg.ganaderia20.ui.theme.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mx.edu.utng.lojg.ganaderia20.R
import mx.edu.utng.lojg.ganaderia20.viewmodel.AuthViewModel
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import mx.edu.utng.lojg.ganaderia20.viewmodel.LoginState

/**
 * Pantalla Composable de inicio de sesión del sistema Ganadero.
 *
 * Esta pantalla gestiona la autenticación del usuario, observando el estado
 * de [AuthViewModel.loginState] para mostrar indicadores de carga, mensajes de error,
 * y manejar la navegación a [DashboardScreen] tras un inicio de sesión exitoso.
 *
 * @param navController El controlador de navegación para gestionar el flujo de la aplicación.
 * @param viewModel El [AuthViewModel] para manejar la lógica de autenticación (login).
 */
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel,
) {
    var usuarioOCorreo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    // Observar el estado del ViewModel (Idle, Loading, Success, Error)
    val loginState by viewModel.loginState.collectAsState()

    // Manejar el estado de la UI basado en el ViewModel
    val isLoading = loginState is LoginState.Loading
    var errorMessage by remember { mutableStateOf("") }

    // Efecto para manejar el resultado del login
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.Success -> {
                // Navegar al dashboard cuando el login es exitoso
                navController.navigate("dashboard/${state.user.uid}/${state.user.rol}") {
                    // Limpiar la pila de navegación para que el usuario no pueda volver al login
                    popUpTo("login") { inclusive = true }
                }
            }
            is LoginState.Error -> {
                // Mostrar el mensaje de error del ViewModel
                errorMessage = state.message
            }
            else -> {
                // No hacer nada en Idle o Loading
            }
        }
    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo de la aplicación
                Image(
                    painter = painterResource(id = R.drawable.logo_toro),
                    contentDescription = "Logo toro",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 16.dp)
                )

                Text(
                    "Sistema Ganadero",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Gestión y registro de ganado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(32.dp))

                // Campo de texto para Usuario o Correo
                OutlinedTextField(
                    value = usuarioOCorreo,
                    onValueChange = {
                        usuarioOCorreo = it
                        errorMessage = "" // Limpiar error al escribir
                    },
                    label = { Text("Usuario o correo electrónico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(Modifier.height(16.dp))

                // Campo de texto para Contraseña
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = {
                        contrasena = it
                        errorMessage = "" // Limpiar error al escribir
                    },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    // Oculta el texto
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(Modifier.height(24.dp))

                // Botón de Iniciar Sesión o Indicador de Carga
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            if (usuarioOCorreo.isNotEmpty() && contrasena.isNotEmpty()) {
                                // Llama al método de login del ViewModel
                                viewModel.login(usuarioOCorreo, contrasena)
                            } else {
                                errorMessage = "Por favor completa todos los campos"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Iniciar Sesión")
                    }
                }

                // Mensaje de Error
                if (errorMessage.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Enlace a Registro
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(
                        "¿No tienes cuenta?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "Registrarse como nuevo usuario",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // Sin efecto visual (ripple)
                            onClick = {
                                // Navega a la pantalla de registro
                                navController.navigate("registro")
                            }
                        )
                    )
                }
            }
        }
    }
}