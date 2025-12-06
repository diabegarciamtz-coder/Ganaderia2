package mx.edu.utng.lojg.ganaderia20.viewmodel

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import mx.edu.utng.lojg.ganaderia20.Repository.AnimalRepository
import mx.edu.utng.lojg.ganaderia20.Repository.RegistroSaludRepository
import mx.edu.utng.lojg.ganaderia20.data.entities.AnimalEntity
import mx.edu.utng.lojg.ganaderia20.data.entities.RegistroSaludEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.text.RegexOption
import kotlinx.coroutines.flow.collect // <-- Agrega esta línea

class GanadoViewModel(
    private val repository: AnimalRepository,
    private val registroSaludRepository: RegistroSaludRepository
) : ViewModel() {

    // ============================================================
    // STATEFLOW: Animales (Room)
    // ============================================================
    private val _animales = MutableStateFlow<List<AnimalEntity>>(emptyList())
    val animales: StateFlow<List<AnimalEntity>> = _animales.asStateFlow()

    // ============================================================
    // STATEFLOW: Registros de salud
    // ============================================================
    private val _registrosSalud = MutableStateFlow<List<RegistroSaludEntity>>(emptyList())
    val registrosSalud: StateFlow<List<RegistroSaludEntity>> = _registrosSalud.asStateFlow()

    // ============================================================
    // NUEVOS STATEFLOWS PARA HISTORIAL DE PESO Y ESTADÍSTICAS
    // ============================================================
    private val _registrosPeso = MutableStateFlow<List<RegistroPeso>>(emptyList())
    val registrosPeso: StateFlow<List<RegistroPeso>> = _registrosPeso.asStateFlow()

    private val _estadisticasSalud = MutableStateFlow<EstadisticasSalud?>(null)
    val estadisticasSalud: StateFlow<EstadisticasSalud?> = _estadisticasSalud.asStateFlow()

    // ============================================================
    // ESTADOS ADICIONALES
    // ============================================================
    private val _animalSeleccionado = mutableStateOf<AnimalEntity?>(null)
    val animalSeleccionado: State<AnimalEntity?> get() = _animalSeleccionado

    private val _animalesFirebase = mutableStateOf<List<AnimalEntity>>(emptyList())
    val animalesFirebase: State<List<AnimalEntity>> get() = _animalesFirebase

    // ============================================================
    // UI STATE para mensajes y errores
    // ============================================================
    private val _uiState = mutableStateOf(UiState())
    val uiState: State<UiState> get() = _uiState

    data class UiState(
        val mensaje: String = "",
        val error: String = ""
    )

    // ============================================================
    // ANIMALES (Room)
    // ============================================================
    fun insertarAnimal(animal: AnimalEntity) {
        viewModelScope.launch {
            repository.insertAnimal(animal)
        }
    }

    fun eliminarAnimal(animal: AnimalEntity) {
        viewModelScope.launch {
            repository.deleteAnimal(animal)
        }
    }


    //... (código anterior del ViewModel)

    /**
     * Cargar animales según el rol del usuario
     * - Propietario/Admin: ve todos los animales del rancho (suyos y de sus empleados).
     * - Empleados: ven todos los animales del mismo rancho (mismo adminId).
     */
    fun cargarAnimales(uid: String, rol: String) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()

                // 1. Obtener la información del usuario actual
                val userDoc = db.collection("usuarios").document(uid).get().await()
                val adminIdVinculado = userDoc.getString("adminId")

                // 2. Determinar el ID de rancho a usar para el filtro (CLAVE)
                val idRanchoParaFiltro = when (rol) {
                    "admin", "superadmin" -> {
                        // Admins y Superadmins: siempre se filtran por su propio UID
                        println("ROL: ADMIN/SUPERADMIN. Filtrando por UID: $uid")
                        uid
                    }
                    else -> {
                        // Empleados y otros: deben usar el ID del admin al que están vinculados.
                        if (adminIdVinculado.isNullOrEmpty()) {
                            println("⚠️ ERROR DE CONFIGURACIÓN DE DATA: El empleado $uid no tiene 'adminId' vinculado. Filtrando por su propio UID.")
                            uid // Fallback: usa su propio UID (verá 0 animales si no registró ninguno)
                        } else {
                            println("ROL: EMPLEADO. Filtrando por adminId: $adminIdVinculado")
                            adminIdVinculado // Usa el ID del administrador vinculado
                        }
                    }
                }

                // 3. CONSULTA A FIREBASE: Usar el ID de rancho determinado
                val snapshot = db.collection("animales")
                    .whereEqualTo("adminId", idRanchoParaFiltro) // <--- Filtro principal
                    .get()
                    .await()

                // ... (Resto del código para mapeo y actualización de Room)
                val animalesFirebase = snapshot.documents.mapNotNull { doc ->
                    try {
                        AnimalEntity(
                            id = 0,
                            arete = doc.getString("arete") ?: "",
                            nombre = doc.getString("nombre") ?: "",
                            tipo = doc.getString("tipo") ?: "",
                            raza = doc.getString("raza") ?: "",
                            fechaNacimiento = doc.getString("fechaNacimiento") ?: "",
                            peso = doc.getString("peso") ?: "0",
                            madre = doc.getString("madre"),
                            padre = doc.getString("padre"),
                            observaciones = doc.getString("observaciones"),
                            estadoSalud = doc.getString("estadoSalud") ?: "Saludable",
                            foto = doc.getString("foto"),
                            usuarioId = doc.getString("usuarioId") ?: uid,
                            adminId = doc.getString("adminId") ?: idRanchoParaFiltro,
                            registradoPor = doc.getString("registradoPor")
                        )
                    } catch (e: Exception) {
                        println("❌ Error parseando animal: ${e.message}")
                        null
                    }
                }

                // Actualizar Room
                animalesFirebase.forEach { animal ->
                    repository.insertAnimal(animal)
                }

                _animales.value = animalesFirebase

                println("✅ Cargados ${animalesFirebase.size} animales desde Firebase con el filtro: $idRanchoParaFiltro")

            } catch (e: Exception) {
                println("❌ Error cargando animales: ${e.message}")
                e.printStackTrace()
                _animales.value = emptyList()
            }
        }
    }

    // ============================================================
    // REGISTROS DE SALUD (Room) - FUNCIONES EXISTENTES
    // ============================================================
    fun cargarHistorial(arete: String) {
        viewModelScope.launch {
            _registrosSalud.value = registroSaludRepository.obtenerPorArete(arete)
        }
    }

    fun agregarRegistroSalud(registro: RegistroSaludEntity) {
        viewModelScope.launch {
            registroSaludRepository.insertarRegistro(registro)
            _registrosSalud.value = registroSaludRepository.obtenerPorArete(registro.areteAnimal)
        }
    }

    fun agregarRegistroSalud(
        arete: String,
        fecha: String,
        tipo: String,
        tratamiento: String,
        responsable: String,
        observaciones: String,
        estado: String = "Pendiente"
    ) {
        viewModelScope.launch {
            val registro = RegistroSaludEntity(
                areteAnimal = arete,
                fecha = fecha,
                tipo = tipo,
                tratamiento = tratamiento,
                responsable = responsable,
                observaciones = observaciones,
                estado = estado
            )
            registroSaludRepository.insertarRegistro(registro)
            _registrosSalud.value = registroSaludRepository.obtenerPorArete(arete)
        }
    }

    fun obtenerRegistrosPorEstado(estado: String) {
        viewModelScope.launch {
            val filtrados = registroSaludRepository.obtenerPorEstado(estado)
            _registrosSalud.value = filtrados
        }
    }

    fun eliminarRegistroSalud(registro: RegistroSaludEntity) {
        viewModelScope.launch {
            registroSaludRepository.eliminarRegistro(registro)
            _registrosSalud.value = registroSaludRepository.obtenerPorArete(registro.areteAnimal)
        }
    }

    fun cargarTodosLosRegistros() {
        viewModelScope.launch {
            _registrosSalud.value = registroSaludRepository.obtenerTodos()
        }
    }

    // ============================================================
    // NUEVAS FUNCIONES PARA EL FILTERCHIP - CORREGIDAS
    // ============================================================
    fun actualizarEstadoRegistro(registroId: Int, nuevoEstado: String) {
        viewModelScope.launch {
            try {
                registroSaludRepository.actualizarEstado(registroId, nuevoEstado)
                val registrosActualizados = _registrosSalud.value.map { registro ->
                    if (registro.id == registroId) registro.copy(estado = nuevoEstado) else registro
                }
                _registrosSalud.value = registrosActualizados
                _uiState.value = UiState(mensaje = "Estado actualizado a ${nuevoEstado.replaceFirstChar { it.uppercase() }}")
            } catch (e: Exception) {
                _uiState.value = UiState(error = "Error al actualizar estado: ${e.message}")
            }
        }
    }

    fun agregarRegistroSaludSimple(arete: String, tipo: String, descripcion: String) {
        viewModelScope.launch {
            try {
                val nuevoRegistro = RegistroSaludEntity(
                    areteAnimal = arete,
                    fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    tipo = tipo,
                    tratamiento = descripcion,
                    responsable = "",
                    observaciones = "",
                    estado = "Pendiente"
                )
                registroSaludRepository.insertarRegistro(nuevoRegistro)
                cargarHistorial(arete)
                _uiState.value = UiState(mensaje = "Registro de salud agregado correctamente")
            } catch (e: Exception) {
                _uiState.value = UiState(error = "Error al agregar registro: ${e.message}")
            }
        }
    }

    fun cargarAnimalPorArete(arete: String) {
        viewModelScope.launch {
            try {
                val animal = repository.obtenerAnimalPorArete(arete)
                _animalSeleccionado.value = animal
                if (animal == null) {
                    _uiState.value = UiState(error = "No se encontró el animal con arete: $arete")
                }
            } catch (e: Exception) {
                _uiState.value = UiState(error = "Error al cargar animal: ${e.message}")
            }
        }
    }

    fun actualizarPesoAnimal(
        arete: String,
        nuevoPeso: Double,
        observaciones: String,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val animal = repository.obtenerAnimalPorArete(arete)
                if (animal != null) {
                    val actualizado = animal.copy(
                        peso = nuevoPeso.toString(),
                        observaciones = observaciones
                    )
                    repository.actualizarAnimal(actualizado)
                    callback(true)
                } else {
                    callback(false)
                }
            } catch (e: Exception) {
                callback(false)
            }
        }
    }

    // ============================================================
    // CÓDIGO INTEGRADO: HISTORIAL DE PESO Y ESTADÍSTICAS
    // ============================================================

    /**
     * Cargar historial de peso de un animal
     */
    fun cargarHistorialPeso(arete: String) {
        viewModelScope.launch {
            try {
                // Obtener animal actual desde el repositorio
                val animal = withContext(Dispatchers.IO) {
                    repository.obtenerAnimalPorArete(arete)
                }

                // Obtener registros de salud que incluyan peso desde el repositorio
                val registrosSalud = withContext(Dispatchers.IO) {
                    registroSaludRepository.obtenerPorArete(arete)
                }

                // Construir lista de registros de peso
                val registros = mutableListOf<RegistroPeso>()

                // Agregar peso al nacer (fecha de nacimiento)
                animal?.let {
                    val pesoInicial = it.peso.toDoubleOrNull() ?: 0.0
                    if (pesoInicial > 0) {
                        registros.add(
                            RegistroPeso(
                                fecha = it.fechaNacimiento,
                                peso = pesoInicial,
                                observacion = "Peso al nacer"
                            )
                        )
                    }
                }

                // Agregar pesos de actualizaciones
                registrosSalud
                    .filter { it.tipo.contains("peso", ignoreCase = true) ||
                            it.tratamiento.contains("peso", ignoreCase = true) }
                    .forEach { registro ->
                        // Extraer peso del tratamiento u observaciones
                        val pesoStr = extractPesoFromText(registro.tratamiento + " " + registro.observaciones)
                        pesoStr?.toDoubleOrNull()?.let { peso ->
                            registros.add(
                                RegistroPeso(
                                    fecha = registro.fecha,
                                    peso = peso,
                                    observacion = registro.tipo
                                )
                            )
                        }
                    }

                // Ordenar por fecha
                val registrosOrdenados = registros.sortedBy { parseDate(it.fecha) }

                _registrosPeso.value = registrosOrdenados

                // Calcular estadísticas
                if (registrosOrdenados.isNotEmpty()) {
                    val pesoActual = registrosOrdenados.last().peso
                    val pesoPromedio = registrosOrdenados.map { it.peso }.average()
                    val ultimaFecha = registrosOrdenados.last().fecha

                    _estadisticasSalud.value = EstadisticasSalud(
                        pesoActual = pesoActual,
                        pesoPromedio = pesoPromedio,
                        totalRegistros = registrosOrdenados.size,
                        ultimaRevision = ultimaFecha
                    )
                }

            } catch (e: Exception) {
                println("Error cargando historial de peso: ${e.message}")
                _registrosPeso.value = emptyList()
            }
        }
    }

    /**
     * Extrae peso de un texto (busca números seguidos de "kg")
     */
    private fun extractPesoFromText(text: String): String? {
        val regex = """(\d+(?:\.\d+)?)\s*kg""".toRegex(RegexOption.IGNORE_CASE)
        return regex.find(text)?.groupValues?.get(1)
    }

    /**
     * Parsea fecha en formato dd/MM/yyyy o yyyy-MM-dd
     */
    private fun parseDate(dateStr: String): Long {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            // Intenta con otro formato si falla, por ejemplo, yyyy-MM-dd
            try {
                val altFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                altFormat.parse(dateStr)?.time ?: 0L
            } catch (e2: Exception) {
                0L
            }
        }
    }
}

// ============================================================
// DATA CLASSES NECESARIAS PARA EL NUEVO CÓDIGO
// ============================================================

data class RegistroPeso(
    val fecha: String,
    val peso: Double,
    val observacion: String
)

data class EstadisticasSalud(
    val pesoActual: Double,
    val pesoPromedio: Double,
    val totalRegistros: Int,
    val ultimaRevision: String
)
