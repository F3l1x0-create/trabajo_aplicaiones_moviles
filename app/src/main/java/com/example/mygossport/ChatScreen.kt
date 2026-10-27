
package com.example.mygossport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val VerdeChat = Color(0xFF176B45)
private val FondoChat = Color(0xFFF4F7F4)

@Composable
fun ChatScreen(
    participante: String,
    onVolver: () -> Unit
) {
    val mensajes = remember {
        mutableStateListOf(
            "¡Hola! También me interesa participar en esta actividad."
        )
    }

    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoChat)
            .padding(16.dp)
    ) {
        Text(
            text = "¡Hicieron match!",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = VerdeChat
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Chat con $participante",
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mensajes) { texto ->
                    Text(
                        text = texto,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Color(0xFFE1EAE3),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(12.dp),
                        color = Color(0xFF17251D)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = mensaje,
            onValueChange = { mensaje = it },
            label = { Text("Escribe un mensaje") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (mensaje.isNotBlank()) {
                    mensajes.add(mensaje.trim())
                    mensaje = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeChat
            )
        ) {
            Text("Enviar mensaje")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE1EAE3),
                contentColor = VerdeChat
            )
        ) {
            Text("← Volver a participantes")
        }
    }
}
