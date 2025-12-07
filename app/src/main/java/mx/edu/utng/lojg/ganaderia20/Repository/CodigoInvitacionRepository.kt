package mx.edu.utng.lojg.ganaderia20.Repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mx.edu.utng.lojg.ganaderia20.models.CodigoInvitacion
import java.util.Calendar

object CodigoInvitacionRepository {

    private val db = FirebaseFirestore.getInstance()

    // Función para generar código único
    private fun generarCodigoUnico(): String {
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..6)
            .map { caracteres.random() }
            .joinToString("")
    }

    // Función para crear código de invitación
    suspend fun crearCodigoInvitacion(
        adminId: String,
        tipo: String = "usuario",
        usosTotales: Int = 1,
        diasExpiracion: Int = 30
    ): CodigoInvitacion {
        val codigo = generarCodigoUnico()

        // Calcular fecha de expiración
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, diasExpiracion)
        val fechaExpiracion = Timestamp(calendar.time)

        val codigoData = hashMapOf(
            "codigo" to codigo,
            "adminId" to adminId,
            "tipo" to tipo,
            "activo" to true,
            "fechaCreacion" to Timestamp.now(),
            "fechaExpiracion" to fechaExpiracion,
            "usadoEl" to null,
            "usadoPor" to null,
            "usosRestantes" to usosTotales,
            "usosTotales" to usosTotales
        )

        val resultado = db.collection("codigos_invitacion").add(codigoData).await()

        return CodigoInvitacion(
            id = resultado.id,
            codigo = codigo,
            adminId = adminId,
            tipo = tipo,
            activo = true,
            fechaCreacion = Timestamp.now(),
            fechaExpiracion = fechaExpiracion,
            usadoEl = null,
            usadoPor = null,
            usosRestantes = usosTotales,
            usosTotales = usosTotales
        )
    }

    // Función para cargar códigos de un admin
    suspend fun cargarCodigosInvitacion(adminId: String): List<CodigoInvitacion> {
        return try {
            val snapshot = db.collection("codigos_invitacion")
                .whereEqualTo("adminId", adminId)
                .get()
                .await()

            snapshot.documents.map { doc ->
                CodigoInvitacion(
                    id = doc.id,
                    codigo = doc.getString("codigo") ?: "",
                    adminId = doc.getString("adminId") ?: "",
                    tipo = doc.getString("tipo") ?: "usuario",
                    activo = doc.getBoolean("activo") ?: true,
                    fechaCreacion = doc.getTimestamp("fechaCreacion") ?: Timestamp.now(),
                    fechaExpiracion = doc.getTimestamp("fechaExpiracion"),
                    usadoEl = doc.getTimestamp("usadoEl"),
                    usadoPor = doc.getString("usadoPor"),
                    usosRestantes = doc.getLong("usosRestantes")?.toInt() ?: 1,
                    usosTotales = doc.getLong("usosTotales")?.toInt() ?: 1
                )
            }.sortedByDescending { it.fechaCreacion }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Función para verificar y usar código de invitación
    // Reemplaza la función existente en CodigoInvitacionRepository.kt

    suspend fun verificarYUsarCodigo(codigo: String, usuarioId: String): CodigoInvitacion? {
        return try {
            // 1. Encontrar el código
            val snapshot = db.collection("codigos_invitacion")
                .whereEqualTo("codigo", codigo)
                .whereEqualTo("activo", true)
                .get()
                .await()

            if (snapshot.isEmpty) return null

            val documento = snapshot.documents[0]
            val codigoId = documento.id
            val usosRestantes = documento.getLong("usosRestantes")?.toInt() ?: 1
            val fechaExpiracion = documento.getTimestamp("fechaExpiracion")

            // Verificar expiración
            if (fechaExpiracion != null && fechaExpiracion < com.google.firebase.Timestamp.now()) {
                return null
            }

            // Verificar usos
            if (usosRestantes <= 0) {
                return null
            }

            // Calcular el estado después del uso
            val nuevoUsosRestantes = usosRestantes - 1
            val activoDespuesUso = nuevoUsosRestantes > 0

            // 2. Preparar objeto a retornar (con datos post-uso)
            val codigoActualizado = CodigoInvitacion(
                id = codigoId,
                codigo = documento.getString("codigo") ?: "",
                adminId = documento.getString("adminId") ?: "",
                tipo = documento.getString("tipo") ?: "usuario",
                activo = activoDespuesUso,
                fechaCreacion = documento.getTimestamp("fechaCreacion") ?: com.google.firebase.Timestamp.now(),
                fechaExpiracion = fechaExpiracion,
                usadoEl = com.google.firebase.Timestamp.now(),
                usadoPor = usuarioId,
                usosRestantes = nuevoUsosRestantes,
                usosTotales = documento.getLong("usosTotales")?.toInt() ?: 1
            )

            // 3. Decisión CLAVE: Eliminar o Actualizar
            if (nuevoUsosRestantes <= 0) {
                // Si ya no quedan usos, ELIMINAR el documento
                db.collection("codigos_invitacion")
                    .document(codigoId)
                    .delete()
                    .await()
            } else {
                // Si todavía quedan usos, solo ACTUALIZAR los campos
                val actualizaciones = hashMapOf<String, Any?>(
                    "usosRestantes" to nuevoUsosRestantes,
                    "usadoEl" to com.google.firebase.Timestamp.now(),
                    "usadoPor" to usuarioId,
                    "activo" to activoDespuesUso // En este caso, sigue siendo true
                )

                db.collection("codigos_invitacion")
                    .document(codigoId)
                    .update(actualizaciones)
                    .await()
            }

            // Retornar el objeto con los datos esenciales (adminId, tipo)
            return codigoActualizado
        } catch (e: Exception) {
            println("Error al verificar y usar código (¿ya fue eliminado?): ${e.message}")
            null
        }
    }


    // Función para verificar si un código es válido (sin usarlo)
    suspend fun verificarCodigoValido(codigo: String): Boolean {
        return try {
            val snapshot = db.collection("codigos_invitacion")
                .whereEqualTo("codigo", codigo)
                .whereEqualTo("activo", true)
                .get()
                .await()

            if (snapshot.isEmpty) return false

            val documento = snapshot.documents[0]
            val usosRestantes = documento.getLong("usosRestantes")?.toInt() ?: 1
            val fechaExpiracion = documento.getTimestamp("fechaExpiracion")

            // Verificar expiración y usos
            (fechaExpiracion == null || fechaExpiracion >= Timestamp.now()) && usosRestantes > 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun verificarCodigoSinUsar(codigo: String): CodigoInvitacion? {
        return try {
            val db = FirebaseFirestore.getInstance()
            val query = db.collection("codigos_invitacion")
                .whereEqualTo("codigo", codigo.uppercase())
                .whereEqualTo("activo", true)
                // Se asume que el campo es 'usosRestantes' y no 'usosDisponibles'
                .whereGreaterThan("usosRestantes", 0)
                .whereGreaterThan("fechaExpiracion", com.google.firebase.Timestamp.now())
                .get()
                .await()

            if (!query.isEmpty && query.documents.isNotEmpty()) {
                val document = query.documents[0]
                document.toObject(CodigoInvitacion::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error al verificar código: ${e.message}")
            null
        }
    }

    // ---------- FUNCIONES INTEGRADAS ----------

    /**
     * Eliminar un código de invitación
     */
    suspend fun eliminarCodigo(codigoId: String): Boolean {
        return try {
            db.collection("codigos_invitacion")
                .document(codigoId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            println("❌ Error eliminando código: ${e.message}")
            false
        }
    }

    /**
     * Generar código con longitud personalizada
     */
    fun generarCodigoPersonalizado(longitud: Int = 8): String {
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..longitud)
            .map { caracteres.random() }
            .joinToString("")
    }

    /**
     * Crear código de invitación con longitud personalizada y verificación de unicidad
     */
    suspend fun crearCodigoInvitacionPersonalizado(
        adminId: String,
        tipo: String,
        usosTotales: Int = 1,
        diasExpiracion: Int = 30,
        longitudCodigo: Int = 8 // NUEVO PARÁMETRO
    ): CodigoInvitacion {
        // Generar código único con longitud personalizada, asegurando que no exista
        var codigoUnico: String
        var existe: Boolean

        do {
            codigoUnico = generarCodigoPersonalizado(longitudCodigo)
            val snapshot = db.collection("codigos_invitacion")
                .whereEqualTo("codigo", codigoUnico)
                .get()
                .await()
            existe = !snapshot.isEmpty
        } while (existe)

        // Calcular fecha de expiración
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, diasExpiracion)
        val fechaExpiracion = Timestamp(calendar.time)

        // Crear el objeto del documento
        val nuevoCodigo = CodigoInvitacion(
            id = "", // Se asignará automáticamente por Firestore
            codigo = codigoUnico,
            adminId = adminId,
            tipo = tipo,
            activo = true,
            fechaCreacion = Timestamp.now(),
            fechaExpiracion = fechaExpiracion,
            usadoEl = null,
            usadoPor = null,
            usosTotales = usosTotales,
            usosRestantes = usosTotales
        )

        val docRef = db.collection("codigos_invitacion").add(nuevoCodigo).await()

        return nuevoCodigo.copy(id = docRef.id)
    }
}
