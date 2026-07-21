package com.example.calculadoralysifix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculadoralysifix.ui.theme.CalculadoraLysifixTheme
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculadoraLysifixTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212),
                ) {
                    CalculadoraApp()
                }
            }
        }
    }
}

@Composable
fun CalculadoraApp() {
    var operacionPrevia by remember { mutableStateOf("") }
    var expresion by remember { mutableStateOf("0") }
    var primerOperando by remember { mutableStateOf<Double?>(null) }
    var operador by remember { mutableStateOf<String?>(null) }
    var nuevoNumero by remember { mutableStateOf(value = true) }

    val emeraldGreen = Color(0xFF2F604F)
    val coralRed = Color(0xFFF64343)
    val darkGrey = Color(0xFF2C2C2C)
    val brightOrange = Color(0xFFFF9800)
    val limeGreen = Color(0xFF11A64E)
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Lysifix",
            color = Color(0xFFB09500), // Amarillo ocre / dorado suave, menos chillante
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 27.dp, bottom = 8.dp), // Añadido padding superior para bajarlo
            textAlign = TextAlign.Center
        )

        // Pantallas superiores
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp, alignment = androidx.compose.ui.Alignment.Bottom)
        ) {
            // Pantalla de operación (pequeña)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                color = emeraldGreen,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
            ) {
                Text(
                    text = operacionPrevia,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 18.sp,
                    textAlign = TextAlign.End,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            // Pantalla de resultado (grande)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                color = emeraldGreen,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
            ) {
                Text(
                    text = if (operacionPrevia.endsWith("=")) {
                        if (expresion == "Creo que es..." || expresion == "¡Ni idea!") expresion else "¡Le atiné! $expresion"
                    } else expresion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    fontSize = if (operacionPrevia.endsWith("=")) 28.sp else 30.sp,
                    textAlign = TextAlign.End,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Definición de los botones
        val botones = listOf(
            listOf("C", "()", "%", "÷"),
            listOf("7", "8", "9", "x"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "⌫", "=")
        )

        // Generar la cuadrícula
        Column(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            botones.forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    fila.forEach { texto ->
                        val colorBoton = when (texto) {
                            "C" -> coralRed
                            "÷", "x", "-", "+" -> brightOrange
                            "=" -> limeGreen
                            else -> darkGrey
                        }
                        val colorTexto = if (texto == "=") Color.Black else Color.White

                        BotonCalculadora(
                            texto = texto,
                            colorContenedor = colorBoton,
                            colorTexto = colorTexto,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.2f),
                            onClick = {
                                when (texto) {
                                    "C" -> {
                                        expresion = "0"
                                        operacionPrevia = ""
                                        primerOperando = null
                                        operador = null
                                        nuevoNumero = true
                                    }
                                    "⌫" -> {
                                        if (expresion.length > 1) {
                                            expresion = expresion.dropLast(1)
                                        } else {
                                            expresion = "0"
                                            nuevoNumero = true
                                        }
                                    }
                                    "+", "-", "x", "÷" -> {
                                        operacionPrevia = "$expresion $texto"
                                        val valorActual = expresion.toDoubleOrNull() ?: 0.0

                                        if (operador != null && !nuevoNumero) {
                                            val resultado = when (operador) {
                                                "+" -> (primerOperando ?: 0.0) + valorActual
                                                "-" -> (primerOperando ?: 0.0) - valorActual
                                                "x" -> (primerOperando ?: 0.0) * valorActual
                                                "÷" -> if (valorActual != 0.0) (primerOperando ?: 0.0) / valorActual else Double.NaN
                                                else -> valorActual
                                            }
                                            primerOperando = if (resultado.isNaN()) null else resultado
                                            expresion = formatResult(resultado)
                                        } else {
                                            primerOperando = valorActual
                                        }

                                        operador = texto
                                        operacionPrevia = "${formatResult(primerOperando ?: 0.0)} $texto"
                                        nuevoNumero = true
                                    }
                                    "=" -> {
                                        if (operador != null && primerOperando != null) {
                                            val segundoNumero = expresion
                                            val valorActual = expresion.toDoubleOrNull() ?: 0.0

                                            operacionPrevia = "$operacionPrevia $segundoNumero ="
                                            expresion = "Creo que es..."

                                            coroutineScope.launch {
                                                delay(1000L.milliseconds)
                                                val resultado = when (operador) {
                                                    "+" -> primerOperando!! + valorActual
                                                    "-" -> primerOperando!! - valorActual
                                                    "x" -> primerOperando!! * valorActual
                                                    "÷" -> if (valorActual != 0.0) primerOperando!! / valorActual else Double.NaN
                                                    else -> valorActual
                                                }

                                                val resultadoFormateado = formatResult(resultado)

                                                expresion = resultadoFormateado
                                                primerOperando = null
                                                operador = null
                                            }
                                        }
                                    }
                                    "()" -> {
                                        // Lógica para alternar paréntesis ( )
                                        if (nuevoNumero || expresion == "0") {
                                            expresion = "("
                                            nuevoNumero = false
                                        } else {
                                            val abiertos = expresion.count { it == '(' }
                                            val cerrados = expresion.count { it == ')' }
                                            
                                            // Si hay paréntesis abiertos y el último caracter es un número o un paréntesis de cierre, cerramos.
                                            expresion += if (abiertos > cerrados && (expresion.last().isDigit() || expresion.last() == ')')) {
                                                ")"
                                            } else {
                                                "("
                                            }
                                        }
                                    }
                                    "%" -> {
                                        val valorActual = expresion.toDoubleOrNull() ?: 0.0
                                        expresion = formatResult(valorActual / 100.0)
                                        nuevoNumero = true
                                    }
                                    else -> {
                                        if (nuevoNumero) {
                                            expresion = texto
                                            nuevoNumero = false
                                            if (operacionPrevia.endsWith("=")) {
                                                operacionPrevia = ""
                                            }
                                        } else {
                                            expresion += texto
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatResult(value: Double): String {
    return if (value.isNaN() || value.isInfinite()) "¡Ni idea!"
    else if (value % 1 == 0.0) value.toLong().toString()
    else value.toString()
}

@Composable
fun BotonCalculadora(
    texto: String,
    colorContenedor: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val blueColor = Color(0xFF2196F3) // Azul brillante para el efecto de presión

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPressed) blueColor else colorContenedor,
            contentColor = if (isPressed) Color.White else colorTexto
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        if (texto == "=") {
            Text(
                text = "=",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = texto,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
