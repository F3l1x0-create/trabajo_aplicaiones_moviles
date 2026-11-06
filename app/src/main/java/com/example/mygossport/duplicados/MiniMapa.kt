package com.example.mygossport.duplicados

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MiniMapa() {
    val fondoMapa = Color(0xFFE8EDE5)
    val calle = Color(0xFFFFFFFF)
    val verde = Color(0xFF176B45)
    val azul = Color(0xFF2878E5)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = fondoMapa
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val ancho = size.width
                val alto = size.height

                // Fondo del mapa
                drawRect(color = fondoMapa)

                // Áreas verdes
                drawRect(
                    color = Color(0xFFC7E2BC),
                    topLeft = Offset(ancho * 0.04f, alto * 0.08f),
                    size = Size(
                        ancho * 0.24f,
                        alto * 0.25f
                    )
                )

                drawRect(
                    color = Color(0xFFC7E2BC),
                    topLeft = Offset(ancho * 0.67f, alto * 0.58f),
                    size = Size(
                        ancho * 0.27f,
                        alto * 0.30f
                    )
                )

                // Calles horizontales
                for (i in 1..4) {
                    val y = alto * i / 5f

                    drawLine(
                        color = calle,
                        start = Offset(0f, y),
                        end = Offset(ancho, y),
                        strokeWidth = 20f
                    )
                }

                // Calles verticales
                for (i in 1..4) {
                    val x = ancho * i / 5f

                    drawLine(
                        color = calle,
                        start = Offset(x, 0f),
                        end = Offset(x, alto),
                        strokeWidth = 18f
                    )
                }

                // Calle diagonal
                val camino = Path().apply {
                    moveTo(0f, alto * 0.78f)
                    lineTo(ancho * 0.42f, alto * 0.56f)
                    lineTo(ancho, alto * 0.34f)
                }

                drawPath(
                    path = camino,
                    color = calle,
                    style = Stroke(
                        width = 17f
                    )
                )

                // Punto azul de ubicación ilustrativa
                val ubicacion = Offset(
                    x = ancho * 0.50f,
                    y = alto * 0.50f
                )

                drawCircle(
                    color = Color.White,
                    radius = 17f,
                    center = ubicacion
                )

                drawCircle(
                    color = azul,
                    radius = 12f,
                    center = ubicacion
                )

                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = ubicacion
                )
            }

            Text(
                text = "MI UBICACIÓN",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .background(
                        Color.White,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 6.dp
                    ),
                color = verde,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
