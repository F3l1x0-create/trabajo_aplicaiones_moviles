package com.example.mygossport.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.mygossport.data.EventoEntity
import com.example.mygossport.model.MensajeChat
import com.example.mygossport.model.UsuarioDemo
import com.example.mygossport.model.usuariosDemo
import com.example.mygossport.viewmodel.EventoViewModel
import com.example.mygossport.viewmodel.UsuarioViewModel

@Composable
fun MygosApp(
    usuarioViewModel: UsuarioViewModel,
    eventoViewModel: EventoViewModel
) {
    val contexto = LocalContext.current

    val preferencias = remember {
        contexto.getSharedPreferences("mygos_chat", Context.MODE_PRIVATE)
    }

    var pantalla by remember { mutableStateOf("login") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var mensajeValidacion by remember { mutableStateOf("") }

    var eventoSeleccionado by remember {
        mutableStateOf<EventoEntity?>(null)
    }

    var usuarioChat by remember {
        mutableStateOf<UsuarioDemo?>(null)
    }

    var usuariosConInteres by remember {
        mutableStateOf(
            preferencias.getStringSet("matches", emptySet())!!
                .mapNotNull { it.toIntOrNull() }
                .toSet()
        )
    }

    var textoMensaje by remember { mutableStateOf("") }

    var mensajes by remember {
        mutableStateOf(
            usuariosDemo.flatMap { usuario ->
                val guardados =
                    preferencias.getString("chat_${usuario.id}", "") ?: ""

                guardados.split("\n")
                    .filter { it.isNotBlank() }
                    .mapNotNull { linea ->
                        val partes = linea.split("|", limit = 2)

                        if (partes.size == 2) {
                            MensajeChat(
                                usuario.id,
                                partes[1],
                                partes[0] == "yo"
                            )
                        } else {
                            null
                        }
                    }
            }
        )
    }

    val mensaje by usuarioViewModel.mensaje.collectAsState()
    val usuarioIngresado by usuarioViewModel.usuarioIngresado.collectAsState()
    val eventos by eventoViewModel.eventos.collectAsState()

    LaunchedEffect(usuarioIngresado) {
        if (usuarioIngresado) {
            pantalla = "inicio"
        }
    }

    LaunchedEffect(mensaje) {
        if (mensaje.isNotBlank()) {
            mensajeValidacion = mensaje
        }
    }

    fun guardarMatches(nuevos: Set<Int>) {
        usuariosConInteres = nuevos

        preferencias.edit()
            .putStringSet(
                "matches",
                nuevos.map { it.toString() }.toSet()
            )
            .apply()
    }

    fun guardarMensaje(
        usuarioId: Int,
        texto: String,
        mio: Boolean
    ) {
        val nuevo = MensajeChat(usuarioId, texto, mio)
        mensajes = mensajes + nuevo

        val contenido = mensajes
            .filter { it.usuarioId == usuarioId }
            .joinToString("\n") {
                (if (it.enviadoPorMi) "yo" else "otro") + "|" + it.texto
            }

        preferencias.edit()
            .putString("chat_$usuarioId", contenido)
            .apply()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
    ) {
        when (pantalla) {

            "login" -> PantallaLogin(
                correo,
                contrasena,
                { correo = it },
                { contrasena = it },
                {
                    if (correo.isBlank() || contrasena.isBlank()) {
                        mensajeValidacion = "Completa todos los campos."
                    } else {
                        usuarioViewModel.iniciarSesion(
                            correo.trim(),
                            contrasena
                        )
                    }
                },
                {
                    correo = ""
                    contrasena = ""
                    nombre = ""
                    pantalla = "registro"
                }
            )

            "registro" -> PantallaRegistro(
                nombre,
                correo,
                contrasena,
                { nombre = it },
                { correo = it },
                { contrasena = it },
                {
                    if (
                        nombre.isBlank() ||
                        correo.isBlank() ||
                        contrasena.isBlank()
                    ) {
                        mensajeValidacion = "Completa todos los campos."
                    } else if (
                        !correo.contains("@") ||
                        !correo.contains(".")
                    ) {
                        mensajeValidacion = "Ingresa un correo válido."
                    } else if (contrasena.length < 6) {
                        mensajeValidacion =
                            "La contraseña debe tener al menos 6 caracteres."
                    } else {
                        usuarioViewModel.registrarUsuario(
                            nombre.trim(),
                            correo.trim(),
                            contrasena
                        )
                    }
                },
                {
                    pantalla = "login"
                }
            )

            "inicio" -> PantallaInicio(
                onVerEventos = {
                    pantalla = "eventos"
                },
                onCrearEvento = {
                    pantalla = "crear"
                },
                onVerMatches = {
                    pantalla = "matches"
                },
                onCerrarSesion = {
                    pantalla = "login"
                    correo = ""
                    contrasena = ""
                }
            )

            "eventos" -> PantallaEventos(
                eventos = eventos,
                onVolver = {
                    pantalla = "inicio"
                },
                onCrear = {
                    pantalla = "crear"
                },
                onEliminar = {
                    eventoViewModel.eliminarEvento(it)
                },
                onSeleccionar = {
                    eventoSeleccionado = it
                    pantalla = "participantes"
                }
            )

            "participantes" -> eventoSeleccionado?.let { evento ->
                PantallaPersonasInteresadas(
                    evento = evento,
                    usuarios = usuariosDemo.filter {
                        it.deporte.equals(
                            evento.deporte,
                            ignoreCase = true
                        )
                    },
                    usuariosConInteres = usuariosConInteres,
                    onVolver = {
                        pantalla = "eventos"
                    },
                    onInteres = { usuario ->
                        guardarMatches(usuariosConInteres + usuario.id)

                        if (usuario.interesadoEnMi) {
                            mensajeValidacion =
                                "¡Tienen un Match! Ahora puedes conversar con ${usuario.nombre}."
                        }
                    }
                )
            }

            "matches" -> PantallaMatches(
                matches = usuariosDemo.filter {
                    it.id in usuariosConInteres
                },
                onVolver = {
                    pantalla = "inicio"
                },
                onAbrirChat = {
                    usuarioChat = it
                    pantalla = "chat"
                }
            )

            "chat" -> usuarioChat?.let { usuario ->
                PantallaChat(
                    usuario = usuario,
                    mensajes = mensajes.filter {
                        it.usuarioId == usuario.id
                    },
                    texto = textoMensaje,
                    onTextoChange = {
                        textoMensaje = it
                    },
                    onEnviar = {
                        if (textoMensaje.isNotBlank()) {
                            guardarMensaje(
                                usuario.id,
                                textoMensaje.trim(),
                                true
                            )

                            textoMensaje = ""
                        }
                    },
                    onVolver = {
                        pantalla = "matches"
                    }
                )
            }

            "crear" -> PantallaCrearEvento(
                onVolver = {
                    pantalla = "eventos"
                },
                onGuardar = {
                        nombreEvento,
                        deporte,
                        fecha,
                        lugar,
                        maximo ->

                    eventoViewModel.agregarEvento(
                        nombreEvento,
                        deporte,
                        fecha,
                        lugar,
                        maximo
                    )

                    pantalla = "eventos"
                    mensajeValidacion = "Actividad creada correctamente."
                }
            )
        }

        if (mensajeValidacion.isNotBlank()) {
            AlertDialog(
                onDismissRequest = {
                    mensajeValidacion = ""
                    usuarioViewModel.cerrarMensaje()
                },
                title = {
                    Text("Mygos Sport")
                },
                text = {
                    Text(mensajeValidacion)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            mensajeValidacion = ""
                            usuarioViewModel.cerrarMensaje()

                            if (mensaje == "Registro exitoso.") {
                                pantalla = "login"
                            }
                        }
                    ) {
                        Text("Aceptar", color = Verde)
                    }
                }
            )
        }
    }
}