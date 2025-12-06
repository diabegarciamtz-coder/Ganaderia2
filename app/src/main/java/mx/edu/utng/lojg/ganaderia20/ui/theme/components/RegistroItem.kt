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