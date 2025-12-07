package mx.edu.utng.lojg.ganaderia20.ui.theme.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Componente Composable que define un campo de texto personalizado (TextField)
 * con un estilo específico para la aplicación.
 *
 * Incluye una etiqueta (label) y soporta la funcionalidad de campo de contraseña.
 *
 * @param label La etiqueta de texto que se mostrará encima del campo de entrada.
 * @param value Un [MutableState<String>] que contiene y actualiza el valor del texto introducido por el usuario.
 * @param isPassword Un booleano que, si es verdadero (true), oculta el texto con puntos
 * utilizando [PasswordVisualTransformation]. Por defecto es falso (false).
 */
// --- Campo de texto personalizado ---
@Composable
fun CustomTextField(label: String, value: MutableState<String>, isPassword: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value.value,
            onValueChange = { value.value = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            // Determina la transformación visual: ocultar si es contraseña, o mostrar normalmente.
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            colors = TextFieldDefaults.colors(
                // Define colores de fondo e indicadores para un diseño limpio y consistente.
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}