package mx.edu.utng.lojg.ganaderia20.ui.theme.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import mx.edu.utng.lojg.ganaderia20.models.Registro

/**
 * Componente Composable que representa un elemento individual en una lista de registros.
 *
 * Muestra información clave del registro (nombre, arete y fecha) junto con una imagen
 * en formato de tarjeta (Card) para mejorar la visualización y separación en listas.
 *
 * @param registro El objeto [Registro] que contiene los datos a mostrar (nombre, arete, fecha, imagen).
 */
@Composable
fun RegistroItem(registro: Registro) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Image(
                painter = painterResource(id = registro.imagen),
                contentDescription = "Imagen animal",
                modifier = Modifier.size(50.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(registro.nombre, style = MaterialTheme.typography.titleMedium)
                Text(registro.arete, style = MaterialTheme.typography.bodyMedium)
                Text("Fecha: ${registro.fecha}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}