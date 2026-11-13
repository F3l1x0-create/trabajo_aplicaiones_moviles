package com.example.mygossport.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mygossport.model.MensajeChat
import com.example.mygossport.model.UsuarioDemo

@Composable
fun PantallaMatches(
    matches: List<UsuarioDemo>,
    onVolver: () -> Unit,
    onAbrirChat: (UsuarioDemo) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(
            "Mis Match",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Verde
        )

        Spacer(Modifier.height(8.dp))

        Text("Aquí puedes revisar tus conexiones y abrir una conversación.")

        Spacer(Modifier.height(16.dp))

        if (matches.isEmpty()) {
            Text(
                "Todavía no tienes Match. Explora actividades y muestra interés en otros perfiles."
            )
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(matches) { usuario ->
                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 7.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                usuario.nombre,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Verde
                            )

                            Text(usuario.deporte)
                            Text(usuario.descripcion)

                            Spacer(Modifier.height(10.dp))

                            Button(
                                onClick = { onAbrirChat(usuario) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Verde)
                            ) {
                                Text("Abrir chat")
                            }
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al inicio")
        }
    }
}

@Composable
fun PantallaChat(
    usuario: UsuarioDemo,
    mensajes: List<MensajeChat>,
    texto: String,
    onTextoChange: (String) -> Unit,
    onEnviar: () -> Unit,
    onVolver: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Chat con ${usuario.nombre}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Verde
        )

        Text("Conversación de demostración local.")

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = false
        ) {
            items(mensajes) { mensaje ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = if (mensaje.enviadoPorMi) {
                        Arrangement.End
                    } else {
                        Arrangement.Start
                    }
                ) {
                    Card(
                        Modifier.padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (mensaje.enviadoPorMi) {
                                Color(0xFFD8EFE1)
                            } else {
                                Color.White
                            }
                        )
                    ) {
                        Text(mensaje.texto, Modifier.padding(12.dp))
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = texto,
                onValueChange = onTextoChange,
                label = { Text("Escribe un mensaje") },
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = onEnviar,
                colors = ButtonDefaults.buttonColors(containerColor = Verde)
            ) {
                Text("Enviar")
            }
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver a mis Match")
        }
    }
}