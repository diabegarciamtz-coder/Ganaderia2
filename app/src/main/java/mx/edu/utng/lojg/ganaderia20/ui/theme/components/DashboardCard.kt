package mx.edu.utng.lojg.ganaderia20.ui.theme.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// --------------------------------------------------------------------
// TARJETA DE DASHBOARD
// --------------------------------------------------------------------
/**
 * Componente Composable que crea una tarjeta informativa estandarizada para el Dashboard.
 *
 * Muestra un título, una cantidad destacada y un icono asociado,
 * ideal para resumir métricas clave (ej. número de animales, total de ventas, etc.).
 *
 * @param titulo La cadena de texto que actúa como título o descripción de la métrica (ej. "Total de Animales").
 * @param cantidad La cadena de texto que representa el valor numérico o la cantidad a destacar (ej. "150").
 * @param icono El ID del recurso drawable (Int) que se usará como icono ilustrativo en la tarjeta.
 * @param modifier Modificador opcional para personalizar el diseño o el comportamiento del componente.
 */
@Composable
fun DashboardCard(titulo: String, cantidad: String, icono: Int,modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    titulo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    cantidad,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Image(
                painter = painterResource(id = icono),
                contentDescription = titulo, // La descripción de accesibilidad es el título
                modifier = Modifier.size(40.dp)
            )
        }
    }
}