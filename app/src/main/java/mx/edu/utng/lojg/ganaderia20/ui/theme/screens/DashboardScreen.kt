package mx.edu.utng.lojg.ganaderia20.ui.theme.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mx.edu.utng.lojg.ganaderia20.R
import mx.edu.utng.lojg.ganaderia20.models.Registro
import mx.edu.utng.lojg.ganaderia20.ui.theme.components.DashboardCard
import mx.edu.utng.lojg.ganaderia20.ui.theme.components.RegistroItem
import mx.edu.utng.lojg.ganaderia20.viewmodel.AuthViewModel
import mx.edu.utng.lojg.ganaderia20.viewmodel.GanadoViewModel

/**
 * Pantalla principal del Dashboard que sirve como punto de entrada de la aplicación.
 *
 * Muestra un resumen de las métricas clave (total de animales por tipo), los últimos registros
 * de animales y proporciona un menú lateral de navegación ([AnimatedVisibility]) dinámico
 * que se ajusta a los permisos del usuario.
 *
 * Se encarga de:
 * 1. Verificar los permisos y el rol del usuario mediante [AuthViewModel].
 * 2. Cargar la lista de animales mediante [GanadoViewModel].
 * 3. Mostrar las métricas en tarjetas [DashboardCard].
 * 4. Listar los registros de animales recientes o un estado vacío con una opción de registro.
 *
 * @param uid El ID único del usuario actual logueado.
 * @param rol El rol inicial del usuario obtenido durante el login.
 * @param navController El controlador de navegación para cambiar entre pantallas.
 * @param viewModel El [GanadoViewModel] para acceder y gestionar los datos de los animales.
 * @param authViewModel El [AuthViewModel] para gestionar la autenticación, permisos y el rol actual.
 */
@Composable
fun DashboardScreen(
    uid: String,
    rol: String,
    navController: NavController,
    viewModel: GanadoViewModel,
    authViewModel: AuthViewModel
) {
    // Estados observados del ViewModel
    val permisosActuales by authViewModel.permisosActuales.collectAsState()
    val rolActual by authViewModel.rolActual.collectAsState()
    val animales by viewModel.animales.collectAsState()

    // Estados locales para la UI
    var menuAbierto by remember { mutableStateOf(false) }
    var nombreUsuario by remember { mutableStateOf("Usuario") }

    // Efecto para verificar permisos al iniciar o si el UID cambia
    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            authViewModel.verificarPermisos()
        }
    }

    // Efecto para cargar animales cuando el rol actual esté disponible/actualizado
    LaunchedEffect(rolActual) {
        if (uid.isNotEmpty() && rolActual.isNotEmpty()) {
            viewModel.cargarAnimales(uid, rolActual)
        }
    }

    // Efecto para cargar el nombre del usuario desde Firestore
    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            nombreUsuario = obtenerNombreUsuario(uid)
        }
    }

    // Construye la lista de opciones del menú dinámicamente según el rol y los permisos
    val opciones = remember(rolActual, permisosActuales) {
        buildList {
            add(OpcionMenu("Dashboard", Icons.Filled.Home, "dashboard/$uid/${rolActual.ifEmpty { rol }}"))
            add(OpcionMenu("Mis Animales", Icons.Filled.Pets, "mis_animales/$uid/${rolActual.ifEmpty { rol }}"))

            if (authViewModel.tienePermiso("crear") || authViewModel.tienePermiso("gestionar_animales")) {
                add(OpcionMenu("Registrar Cría", Icons.Filled.AddCircle, "registrar_cria"))
            }

            add(OpcionMenu("Historial de Salud", Icons.Filled.History, "historial_salud_general"))

            if (authViewModel.tieneAlgunPermiso(listOf("ver_reportes", "admin", "gestionar_usuarios"))) {
                add(OpcionMenu("Informe General", Icons.Filled.ContentPaste, "reports"))
            }

            if (rolActual == "admin" || authViewModel.tienePermiso("gestionar_usuarios")) {
                add(OpcionMenu("Usuarios", Icons.Filled.Group, "usuarios/$uid"))
            }

            add(OpcionMenu("Configuración", Icons.Filled.Settings, "configuracion/$uid/${rolActual.ifEmpty { rol }}"))
        }
    }

    // Prepara una lista de los 3 animales más recientes para mostrar en la sección de registros
    val registrosRecientes = remember(animales) {
        animales.take(3).map {
            Registro(
                nombre = it.nombre,
                arete = it.arete,
                fecha = it.fechaNacimiento,
                imagen = R.drawable.vaca_logo // Asume un drawable por defecto
            )
        }
    }

    Scaffold { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {

                // Header de la pantalla con botón de menú, título e información de usuario/rol
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menú",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        "Dashboard",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            nombreUsuario,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Rol: ${rolActual.ifEmpty { rol }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Contenido principal: Métricas y Registros
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        // Sección de Tarjetas de Métricas
                        Column(modifier = Modifier.fillMaxWidth()) {
                            DashboardCard(
                                "Total Animales",
                                animales.size.toString(),
                                R.drawable.loga_torosyvacas,
                                Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(8.dp))

                            DashboardCard(
                                "Vacas",
                                animales.count { it.tipo.equals("Vaca", true) }.toString(),
                                R.drawable.vaca_logo,
                                Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(8.dp))

                            DashboardCard(
                                "Toros",
                                animales.count { it.tipo.equals("Toro", true) }.toString(),
                                R.drawable.logo_toro2,
                                Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(8.dp))

                            DashboardCard(
                                "Becerros",
                                animales.count { it.tipo.equals("Becerro", true) }.toString(),
                                R.drawable.logo_becerro,
                                Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        // SECCIÓN DE REGISTROS RECIENTES O ESTADO VACÍO
                        if (registrosRecientes.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(3.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        "Registros Recientes",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    // Muestra los registros usando el componente RegistroItem
                                    registrosRecientes.forEach {
                                        RegistroItem(it)
                                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        } else {
                            // Estado de la lista vacía
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "No hay animales registrados aún",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Botón de navegación al registro de cría
                                Row(
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            navController.navigate("registrar_cria")
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AddCircle,
                                        contentDescription = "Registrar",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Registrar primer animal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Menú lateral de navegación (Drawer)
            if (menuAbierto) {

                // Overlay oscuro (Scrim)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                        // Cierra el menú al hacer clic fuera
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { menuAbierto = false }
                        )
                )

                // Contenido del Menú Lateral con animación de visibilidad
                AnimatedVisibility(
                    visible = menuAbierto,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(280.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(
                            topEnd = 16.dp,
                            bottomEnd = 16.dp
                        ),
                        shadowElevation = 8.dp
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {

                            // Cabecera del menú con título y botón de cierre
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Sistema Ganadero",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                IconButton(onClick = { menuAbierto = false }) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Cerrar menú",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Rol: $rolActual",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(Modifier.height(24.dp))

                            // Lista de opciones navegables
                            LazyColumn {
                                items(opciones.size) { index ->
                                    val opcion = opciones[index]
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp)),
                                        color = Color.Transparent,
                                        onClick = {
                                            menuAbierto = false
                                            navController.navigate(opcion.ruta)
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                opcion.icono,
                                                contentDescription = opcion.titulo,
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                opcion.titulo,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clase de datos que representa un elemento navegable en el menú lateral.
 *
 * Se utiliza para definir las opciones de navegación que se muestran en el menú del Dashboard.
 *
 * @property titulo El texto que se muestra en la opción del menú (ej. "Mis Animales").
 * @property icono El [ImageVector] que representa el icono a mostrar junto al título.
 * @property ruta La ruta de navegación que se invoca al seleccionar la opción.
 */
data class OpcionMenu(
    val titulo: String,
    val icono: ImageVector,
    val ruta: String
)

/**
 * Función suspendida para obtener el nombre de un usuario a partir de su UID desde Firestore.
 *
 * Consulta la colección "usuarios" con el ID del usuario y busca el campo "nombre" o "username".
 *
 * @param uid El ID del usuario cuyo nombre se desea obtener.
 * @return El nombre del usuario si se encuentra ("nombre" o "username"), de lo contrario, devuelve "Usuario".
 */
private suspend fun obtenerNombreUsuario(uid: String): String {
    return try {
        val db = FirebaseFirestore.getInstance()
        val doc = db.collection("usuarios").document(uid).get().await()
        doc.getString("nombre")
            ?: doc.getString("username")
            ?: "Usuario"
    } catch (e: Exception) {
        "Usuario"
    }
}