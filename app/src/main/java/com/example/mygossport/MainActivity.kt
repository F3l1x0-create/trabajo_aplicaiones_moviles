
package com.example.mygossport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.mygossport.ui.theme.MygosSportTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Verde = Color(0xFF176B45)
private val Fondo = Color(0xFFF4F7F4)
private val TextoOscuro = Color(0xFF17251D)
private val RojoError = Color(0xFFB3261E)

data class EventoDeportivo(
    val nombre: String,
    val deporte: String,
    val fecha: String,
    val lugar: String,
    val maxParticipantes: Int,
    val inscritos: Int
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            applicationContext,
            MygosDatabase::class.java,
            "mygos_database"
        )
            .addMigrations(MygosDatabase.MIGRATION_1_2)
            .build()

        val eventoDao = database.eventoDao()
        val usuarioDao = database.usuarioDao()

        setContent {
            MygosSportTheme {
                val eventosGuardados by eventoDao
                    .obtenerEventos()
                    .collectAsState(initial = emptyList())

                val scope = rememberCoroutineScope()

                var pantallaActual by remember {
                    mutableStateOf("cargando")
                }

                var usuarioActual by remember {
                    mutableStateOf<UsuarioEntity?>(null)
                }

                var deporteSeleccionado by remember {
                    mutableStateOf("")
                }

                var eventoSeleccionado by remember {
                    mutableStateOf<EventoDeportivo?>(null)
                }

                var participanteDelChat by remember {
                    mutableStateOf("")
                }

                var mostrarConfirmacion by remember {
                    mutableStateOf(false)
                }

                var mensajeLogin by remember {
                    mutableStateOf("")
                }

                var mensajeRegistro by remember {
                    mutableStateOf("")
                }

                LaunchedEffect(Unit) {
                    if (eventoDao.obtenerEventos().first().isEmpty()) {
                        eventoDao.insertarEvento(
                            EventoEntity(
                                nombre = "Fútbol amistoso",
                                deporte = "Fútbol",
                                fecha = "Sábado, 15:00",
                                lugar = "Cancha deportiva",
                                maxParticipantes = 14,
                                inscritos = 8
                            )
                        )

                        eventoDao.insertarEvento(
                            EventoEntity(
                                nombre = "Partido de básquetbol",
                                deporte = "Básquetbol",
                                fecha = "Domingo, 11:00",
                                lugar = "Gimnasio municipal",
                                maxParticipantes = 10,
                                inscritos = 6
                            )
                        )

                        eventoDao.insertarEvento(
                            EventoEntity(
                                nombre = "Encuentro de vóleibol",
                                deporte = "Vóleibol",
                                fecha = "Sábado, 17:30",
                                lugar = "Centro deportivo",
                                maxParticipantes = 12,
                                inscritos = 5
                            )
                        )
                    }

                    val usuarioGuardado = usuarioDao.obtenerSesionActiva()

                    if (usuarioGuardado != null) {
                        usuarioActual = usuarioGuardado
                        pantallaActual = "inicio"
                    } else {
                        pantallaActual = "login"
                    }
                }

                val eventos = eventosGuardados.map { evento ->
                    EventoDeportivo(
                        nombre = evento.nombre,
                        deporte = evento.deporte,
                        fecha = evento.fecha,
                        lugar = evento.lugar,
                        maxParticipantes = evento.maxParticipantes,
                        inscritos = evento.inscritos
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Fondo
                ) { innerPadding ->

                    when (pantallaActual) {

                        "cargando" -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Verde)
                            }
                        }

                        "login" -> PantallaLogin(
                            modifier = Modifier.padding(innerPadding),
                            mensajeError = mensajeLogin,
                            onLimpiarError = {
                                mensajeLogin = ""
                            },
                            onIrRegistro = {
                                mensajeLogin = ""
                                mensajeRegistro = ""
                                pantallaActual = "registro"
                            },
                            onIniciarSesion = { correo, contrasena ->
                                mensajeLogin = ""

                                if (correo.isBlank() || contrasena.isBlank()) {
                                    mensajeLogin =
                                        "Completa el correo y la contraseña."
                                } else {
                                    scope.launch {
                                        val usuario =
                                            usuarioDao.buscarPorCorreo(correo.trim())

                                        if (
                                            usuario != null &&
                                            usuario.contrasena == contrasena
                                        ) {
                                            usuarioDao.cerrarSesion()
                                            usuarioDao.activarSesion(usuario.id)

                                            usuarioActual = usuario.copy(
                                                sesionActiva = true
                                            )

                                            mensajeLogin = ""
                                            pantallaActual = "inicio"
                                        } else {
                                            mensajeLogin =
                                                "Correo o contraseña incorrectos."
                                        }
                                    }
                                }
                            }
                        )

                        "registro" -> PantallaRegistro(
                            modifier = Modifier.padding(innerPadding),
                            mensajeError = mensajeRegistro,
                            onLimpiarError = {
                                mensajeRegistro = ""
                            },
                            onVolver = {
                                mensajeRegistro = ""
                                pantallaActual = "login"
                            },
                            onRegistrar = { nombre, correo, contrasena ->
                                mensajeRegistro = ""

                                scope.launch {
                                    val usuarioExistente =
                                        usuarioDao.buscarPorCorreo(correo.trim())

                                    if (usuarioExistente != null) {
                                        mensajeRegistro =
                                            "Ese correo ya está registrado."
                                    } else {
                                        usuarioDao.cerrarSesion()

                                        usuarioDao.insertarUsuario(
                                            UsuarioEntity(
                                                nombre = nombre.trim(),
                                                correo = correo.trim(),
                                                contrasena = contrasena,
                                                sesionActiva = true
                                            )
                                        )

                                        usuarioActual =
                                            usuarioDao.buscarPorCorreo(correo.trim())

                                        pantallaActual = "inicio"
                                    }
                                }
                            }
                        )

                        "inicio" -> PantallaInicio(
                            modifier = Modifier.padding(innerPadding),
                            nombreUsuario = usuarioActual?.nombre ?: "",
                            onCerrarSesion = {
                                scope.launch {
                                    usuarioDao.cerrarSesion()
                                    usuarioActual = null
                                    mensajeLogin = ""
                                    pantallaActual = "login"
                                }
                            },
                            onExplorar = {
                                deporteSeleccionado = ""
                                pantallaActual = "eventos"
                            },
                            onDeporteSeleccionado = { deporte ->
                                deporteSeleccionado = deporte
                                pantallaActual = "eventos"
                            },
                            onCrearEvento = {
                                pantallaActual = "crear"
                            }
                        )

                        "eventos" -> PantallaEventos(
                            modifier = Modifier.padding(innerPadding),
                            deporte = deporteSeleccionado,
                            eventos = eventos,
                            onVolver = {
                                pantallaActual = "inicio"
                            },
                            onCrearEvento = {
                                pantallaActual = "crear"
                            },
                            onVerParticipantes = { evento ->
                                eventoSeleccionado = evento
                                pantallaActual = "participantes"
                            }
                        )

                        "participantes" -> {
                            val evento = eventoSeleccionado

                            if (evento != null) {
                                PantallaParticipantes(
                                    modifier = Modifier.padding(innerPadding),
                                    evento = evento,
                                    onVolver = {
                                        pantallaActual = "eventos"
                                    },
                                    onAbrirChat = { participante ->
                                        participanteDelChat = participante
                                        pantallaActual = "chat"
                                    }
                                )
                            }
                        }

                        "chat" -> ChatScreen(
                            participante = participanteDelChat,
                            onVolver = {
                                pantallaActual = "participantes"
                            }
                        )

                        "crear" -> PantallaCrearEvento(
                            modifier = Modifier.padding(innerPadding),
                            onVolver = {
                                pantallaActual = "inicio"
                            },
                            onGuardar = { nuevoEvento ->
                                scope.launch {
                                    eventoDao.insertarEvento(
                                        EventoEntity(
                                            nombre = nuevoEvento.nombre,
                                            deporte = nuevoEvento.deporte,
                                            fecha = nuevoEvento.fecha,
                                            lugar = nuevoEvento.lugar,
                                            maxParticipantes =
                                                nuevoEvento.maxParticipantes,
                                            inscritos = nuevoEvento.inscritos
                                        )
                                    )

                                    deporteSeleccionado = nuevoEvento.deporte
                                    pantallaActual = "eventos"
                                    mostrarConfirmacion = true
                                }
                            }
                        )
                    }
                }

                if (mostrarConfirmacion) {
                    AlertDialog(
                        onDismissRequest = {
                            mostrarConfirmacion = false
                        },
                        title = {
                            Text("Actividad creada")
                        },
                        text = {
                            Text(
                                "Tu actividad se guardó correctamente " +
                                        "en la base de datos."
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    mostrarConfirmacion = false
                                }
                            ) {
                                Text("Aceptar", color = Verde)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PantallaLogin(
    modifier: Modifier = Modifier,
    mensajeError: String,
    onLimpiarError: () -> Unit,
    onIrRegistro: () -> Unit,
    onIniciarSesion: (String, String) -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "MYGOS SPORT",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Verde
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Encuentra tu próximo equipo",
            color = TextoOscuro
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                onLimpiarError()
            },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = {
                contrasena = it
                onLimpiarError()
            },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        if (mensajeError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(mensajeError, color = RojoError)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                onIniciarSesion(correo, contrasena)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Verde
            )
        ) {
            Text("Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onIrRegistro) {
            Text("¿No tienes cuenta? Regístrate", color = Verde)
        }
    }
}

@Composable
fun PantallaRegistro(
    modifier: Modifier = Modifier,
    mensajeError: String,
    onLimpiarError: () -> Unit,
    onVolver: () -> Unit,
    onRegistrar: (String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var repetirContrasena by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Crear cuenta",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Verde
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                onLimpiarError()
            },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                onLimpiarError()
            },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = {
                contrasena = it
                onLimpiarError()
            },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = repetirContrasena,
            onValueChange = {
                repetirContrasena = it
                onLimpiarError()
            },
            label = { Text("Repetir contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (error.isNotEmpty()) {
            Text(error, color = RojoError)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (mensajeError.isNotEmpty()) {
            Text(mensajeError, color = RojoError)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                error = when {
                    nombre.isBlank() ->
                        "Ingresa tu nombre."

                    correo.isBlank() ||
                            !correo.contains("@") ||
                            !correo.substringAfter("@").contains(".") ->
                        "Ingresa un correo válido."

                    contrasena.length < 4 ->
                        "La contraseña debe tener al menos 4 caracteres."

                    contrasena != repetirContrasena ->
                        "Las contraseñas no coinciden."

                    else -> ""
                }

                if (error.isEmpty()) {
                    onRegistrar(nombre, correo, contrasena)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Verde
            )
        ) {
            Text("Registrarme")
        }

        TextButton(onClick = onVolver) {
            Text("Volver al inicio de sesión", color = Verde)
        }
    }
}

@Composable
fun PantallaInicio(
    modifier: Modifier = Modifier,
    nombreUsuario: String,
    onCerrarSesion: () -> Unit,
    onExplorar: () -> Unit,
    onDeporteSeleccionado: (String) -> Unit,
    onCrearEvento: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "MYGOS SPORT",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Verde
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            "¡Bienvenido, $nombreUsuario!",
            fontSize = 16.sp,
            color = TextoOscuro
        )

        TextButton(onClick = onCerrarSesion) {
            Text("Cerrar sesión", color = Verde)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Verde)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    "El deporte se disfruta más en equipo",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "Descubre actividades, conoce participantes y " +
                            "encuentra personas con tus mismos intereses.",
                    fontSize = 15.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onExplorar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Verde
                    )
                ) {
                    Text("Explorar actividades")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Categorías deportivas",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = TextoOscuro,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Actividad("Fútbol", "Partidos y encuentros deportivos") {
            onDeporteSeleccionado("Fútbol")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Actividad("Básquetbol", "Encuentra compañeros de juego") {
            onDeporteSeleccionado("Básquetbol")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Actividad("Vóleibol", "Conoce personas y forma un equipo") {
            onDeporteSeleccionado("Vóleibol")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Encuentra tu ubicación",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = TextoOscuro,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        MiniMapa()

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onCrearEvento,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Verde)
        ) {
            Text("Crear actividad")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun Actividad(
    nombre: String,
    detalle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    nombre,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoOscuro
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(detalle, fontSize = 13.sp, color = Color.DarkGray)
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Verde)
            ) {
                Text("Ver")
            }
        }
    }
}

@Composable
fun PantallaEventos(
    modifier: Modifier = Modifier,
    deporte: String,
    eventos: List<EventoDeportivo>,
    onVolver: () -> Unit,
    onCrearEvento: () -> Unit,
    onVerParticipantes: (EventoDeportivo) -> Unit
) {
    val eventosFiltrados = if (deporte.isEmpty()) {
        eventos
    } else {
        eventos.filter { it.deporte == deporte }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "MYGOS SPORT",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Verde
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            if (deporte.isEmpty()) "Todos los eventos" else "Eventos de $deporte",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = TextoOscuro
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            "Encuentra una actividad que te interese.",
            fontSize = 15.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (eventosFiltrados.isEmpty()) {
            Text("Todavía no hay actividades en esta categoría.")
        }

        eventosFiltrados.forEach { evento ->
            EventoCard(evento = evento) {
                onVerParticipantes(evento)
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        Button(
            onClick = onCrearEvento,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Verde)
        ) {
            Text("Crear actividad")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE1EAE3),
                contentColor = Verde
            )
        ) {
            Text("Volver al inicio")
        }
    }
}

@Composable
fun EventoCard(
    evento: EventoDeportivo,
    onVerParticipantes: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                evento.nombre,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Verde
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text("Deporte: ${evento.deporte}", color = TextoOscuro)
            Spacer(modifier = Modifier.height(5.dp))
            Text("Fecha y hora: ${evento.fecha}", color = TextoOscuro)
            Spacer(modifier = Modifier.height(5.dp))
            Text("Lugar: ${evento.lugar}", color = TextoOscuro)
            Spacer(modifier = Modifier.height(5.dp))

            Text(
                "${evento.inscritos} de ${evento.maxParticipantes} participantes",
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onVerParticipantes,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Verde)
            ) {
                Text("Ver participantes")
            }
        }
    }
}

@Composable
fun PantallaParticipantes(
    modifier: Modifier = Modifier,
    evento: EventoDeportivo,
    onVolver: () -> Unit,
    onAbrirChat: (String) -> Unit
) {
    var participanteSeleccionado by remember { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }

    val participantes = when (evento.deporte) {
        "Fútbol" -> listOf(
            "Diego Morales" to "Interesado en jugar de delantero",
            "Camila Torres" to "Le gusta jugar en equipo",
            "Matías Rojas" to "Prefiere jugar de defensa"
        )

        "Básquetbol" -> listOf(
            "Valentina Soto" to "Interesada en jugar de base",
            "Benjamín Castro" to "Le gusta el juego en equipo",
            "Javiera Silva" to "Prefiere jugar de alera"
        )

        else -> listOf(
            "Sofía Herrera" to "Interesada en jugar de armadora",
            "Tomás Fuentes" to "Le gusta jugar en equipo",
            "Antonia Reyes" to "Prefiere jugar de central"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "MYGOS SPORT",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Verde
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Participantes",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = TextoOscuro
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            evento.nombre,
            fontSize = 18.sp,
            color = Verde,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Conoce a las personas inscritas en esta actividad.",
            fontSize = 14.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        participantes.forEach { participante ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        participante.first,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoOscuro
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        participante.second,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            participanteSeleccionado = participante.first
                            mostrarDialogo = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Verde)
                    ) {
                        Text("Me interesa")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Verde)
        ) {
            Text("Volver a los eventos")
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogo = false
            },
            title = {
                Text("¡Hicieron match!")
            },
            text = {
                Text(
                    "Has mostrado interés en $participanteSeleccionado. " +
                            "Puedes abrir el chat de prueba para coordinar la actividad."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogo = false
                        onAbrirChat(participanteSeleccionado)
                    }
                ) {
                    Text("Abrir chat", color = Verde)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarDialogo = false
                    }
                ) {
                    Text("Cancelar", color = Verde)
                }
            }
        )
    }
}

@Composable
fun PantallaCrearEvento(
    modifier: Modifier = Modifier,
    onVolver: () -> Unit,
    onGuardar: (EventoDeportivo) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var deporte by remember { mutableStateOf("Fútbol") }
    var fecha by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var maxParticipantes by remember { mutableStateOf("10") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Fondo)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "MYGOS SPORT",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Verde
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Crear actividad",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = TextoOscuro
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Completa los datos para agregar un evento deportivo.",
            fontSize = 14.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre de la actividad") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Selecciona el deporte",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextoOscuro
        )

        Spacer(modifier = Modifier.height(8.dp))

        listOf("Fútbol", "Básquetbol", "Vóleibol").forEach { opcion ->
            Button(
                onClick = { deporte = opcion },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (deporte == opcion) Verde
                    else Color(0xFFE1EAE3),
                    contentColor = if (deporte == opcion) Color.White
                    else Verde
                )
            ) {
                Text(opcion)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = fecha,
            onValueChange = { fecha = it },
            label = { Text("Fecha y hora") },
            placeholder = { Text("Ej.: Sábado, 16:00") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = lugar,
            onValueChange = { lugar = it },
            label = { Text("Lugar de la actividad") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = maxParticipantes,
            onValueChange = { valor ->
                if (valor.all { it.isDigit() }) {
                    maxParticipantes = valor
                }
            },
            label = { Text("Máximo de participantes") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (error.isNotEmpty()) {
            Text(error, color = RojoError, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val cantidad = maxParticipantes.toIntOrNull()

                error = when {
                    nombre.isBlank() ->
                        "Escribe el nombre de la actividad."

                    fecha.isBlank() ->
                        "Escribe la fecha y la hora."

                    lugar.isBlank() ->
                        "Escribe el lugar de la actividad."

                    cantidad == null || cantidad !in 2..100 ->
                        "El máximo debe estar entre 2 y 100 participantes."

                    else -> ""
                }

                if (error.isEmpty() && cantidad != null) {
                    onGuardar(
                        EventoDeportivo(
                            nombre = nombre.trim(),
                            deporte = deporte,
                            fecha = fecha.trim(),
                            lugar = lugar.trim(),
                            maxParticipantes = cantidad,
                            inscritos = 1
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Verde)
        ) {
            Text("Guardar actividad")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE1EAE3),
                contentColor = Verde
            )
        ) {
            Text("Cancelar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Las actividades se guardan en la base de datos del teléfono.",
            fontSize = 12.sp,
            color = Color.DarkGray
        )
    }
}
