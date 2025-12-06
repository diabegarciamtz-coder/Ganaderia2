package mx.edu.utng.lojg.ganaderia20.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class CodigoInvitacion(
    val id: String = "",
    val codigo: String = "",
    val adminId: String = "",
    val tipo: String = "usuario", // "usuario", "admin", "veterinario"
    val activo: Boolean = true,
    val fechaCreacion: Timestamp = Timestamp.now(),
    val fechaExpiracion: Timestamp? = null,
    val usadoEl: Timestamp? = null,
    val usadoPor: String? = null,
    val usosRestantes: Int = 1,
    val usosTotales: Int = 1
){
    // Constructor sin parámetros para Firestore
    // FIX: Reordered arguments to match the primary constructor
    constructor() : this(
        id = "",
        codigo = "",
        adminId = "",
        tipo = "usuario",
        activo = true, // 'activo' is a Boolean, should be before Timestamp
        fechaCreacion = Timestamp.now(),
        fechaExpiracion = null,
        usadoEl = null,
        usadoPor = null,
        usosRestantes = 1,
        usosTotales = 1
    )
}

// Dentro de CodigoInvitacionRepository.kt (ejemplo)

suspend fun marcarCodigoComoUsado(codigo: String, empleadoUid: String) {
    val db = FirebaseFirestore.getInstance()
    val codigoSnapshot = db.collection("codigosInvitacion")
        .whereEqualTo("codigo", codigo)
        .limit(1)
        .get()
        .await()

    if (codigoSnapshot.isEmpty) return // No existe, ignorar

    val codigoDoc = codigoSnapshot.documents[0]
    val usosRestantesActual = codigoDoc.getLong("usosRestantes") ?: 0L

    // Solo actualiza si aún tiene usos
    if (usosRestantesActual > 0) {
        val nuevoUsosRestantes = usosRestantesActual - 1

        db.collection("codigosInvitacion").document(codigoDoc.id)
            .update(
                "usosRestantes", nuevoUsosRestantes,
                "usadoEl", Timestamp.now(),
                "usadoPor", empleadoUid,
                "activo", nuevoUsosRestantes > 0
            )
            .await()
    }
}
