package com.example.mygossport.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mygossport.data.EventoEntity
import com.example.mygossport.model.UsuarioDemo

@Composable
fun PantallaEventos(
    eventos: List<EventoEntity>,
    onVolver: () -> Unit,
    onCrear: () -> Unit,
    onEliminar: (Int) -> Unit,
    onSeleccionar: (EventoEntity) -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Text(
            "Actividades deportivas",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Verde
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onCrear,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Verde)
        ) {
            Text("Crear actividad")
        }

        Spacer(Modifier.height(16.dp))

        if (eventos.isEmpty()) {
            Text("Todavía no hay actividades creadas.")
        }

        eventos.forEach { evento ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically()
            ) {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp)
                        .clickable { onSeleccionar(evento) },
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            evento.nombre,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Verde
                        )

                        Text("Deporte: ${evento.deporte}")
                        Text("Fecha: ${evento.fecha}")
                        Text("Lugar: ${evento.lugar}")
                        Text("Máximo de participantes: ${evento.maxParticipantes}")

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Ver personas interesadas",
                            color = Verde,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(onClick = { onEliminar(evento.id) }) {
                            Text("Eliminar actividad", color = Color.Red)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

@Composable
fun PantallaPersonasInteresadas(
    evento: EventoEntity,
    usuarios: List<UsuarioDemo>,
    usuariosConInteres: Set<Int>,
    onVolver: () -> Unit,
    onInteres: (UsuarioDemo) -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Text(
            "Personas interesadas",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Verde
        )

        Spacer(Modifier.height(8.dp))

        Text("Actividad: ${evento.nombre}", fontWeight = FontWeight.Bold)
        Text("Deporte: ${evento.deporte}")
        Text("Estas personas buscan compañeros para jugar.")

        Spacer(Modifier.height(20.dp))

        if (usuarios.isEmpty()) {
            Text("Todavía no hay personas interesadas en este deporte.")
        }

        usuarios.forEach { usuario ->
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

                    Text(usuario.descripcion)

                    Spacer(Modifier.height(12.dp))

                    if (usuario.id in usuariosConInteres) {
                        Text(
                            if (usuario.interesadoEnMi) {
                                "¡Tienen un Match!"
                            } else {
                                "Ya mostraste interés"
                            },
                            color = Verde,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Button(
                            onClick = { onInteres(usuario) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Verde)
                        ) {
                            Text("Me interesa")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver a actividades")
        }
    }
}