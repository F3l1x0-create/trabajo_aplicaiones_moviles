package com.example.mygossport

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.mygossport.data.DatabaseProvider
import com.example.mygossport.data.EventoEntity
import com.example.mygossport.data.EventoRepository
import com.example.mygossport.viewmodel.EventoViewModel
import com.example.mygossport.viewmodel.EventoViewModelFactory
import com.example.mygossport.viewmodel.UsuarioViewModel
import com.example.mygossport.viewmodel.UsuarioViewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

private val Verde = Color(0xFF176B45)
private val Fondo = Color(0xFFF4F7F4)
private val TextoOscuro = Color(0xFF17251D)

data class UsuarioDemo(
    val id: Int,
    val nombre: String,
    val deporte: String,
    val descripcion: String,
    val interesadoEnMi: Boolean
)

data class MensajeChat(
    val usuarioId: Int,
    val texto: String,
    val enviadoPorMi: Boolean
)

private val usuariosDemo = listOf(
    UsuarioDemo(1, "Carlos", "Fútbol", "Me gusta jugar los fines de semana.", true),
    UsuarioDemo(2, "Camila", "Fútbol", "Busco compañeros para jugar.", true),
    UsuarioDemo(3, "Diego", "Fútbol", "Juego de delantero.", false),
    UsuarioDemo(4, "Valentina", "Básquetbol", "Me gusta entrenar en equipo.", true),
    UsuarioDemo(5, "Matías", "Básquetbol", "Busco partidos recreativos.", true),
    UsuarioDemo(6, "Fernanda", "Vóleibol", "Me gusta el vóleibol mixto.", true),
    UsuarioDemo(7, "Andrés", "Vóleibol", "Juego los fines de semana.", false)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val baseDatos = DatabaseProvider.obtenerBaseDatos(this)

        val eventoRepository = EventoRepository(baseDatos.eventoDao())
        val eventoFactory = EventoViewModelFactory(eventoRepository)
        val eventoViewModel = ViewModelProvider(this, eventoFactory)
            .get(EventoViewModel::class.java)

        val usuarioFactory = UsuarioViewModelFactory(baseDatos.usuarioDao())
        val usuarioViewModel = ViewModelProvider(this, usuarioFactory)
            .get(UsuarioViewModel::class.java)

        setContent {
            MaterialTheme {
                MygosApp(
                    usuarioViewModel = usuarioViewModel,
                    eventoViewModel = eventoViewModel
                )
            }
        }
    }
}

@Composable
fun MygosApp(
    usuarioViewModel: UsuarioViewModel,
    eventoViewModel: EventoViewModel
) {
    val contexto = androidx.compose.ui.platform.LocalContext.current
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
                .mapNotNull { it.toIntOrNull() }.toSet()
        )
    }

    var textoMensaje by remember { mutableStateOf("") }

    var mensajes by remember {
        mutableStateOf(
            usuariosDemo.flatMap { usuario ->
                val guardados = preferencias.getString("chat_${usuario.id}", "") ?: ""
                guardados.split("\n").filter { it.isNotBlank() }.mapNotNull { linea ->
                    val partes = linea.split("|", limit = 2)
                    if (partes.size == 2) {
                        MensajeChat(usuario.id, partes[1], partes[0] == "yo")
                    } else null
                }
            }
        )
    }

    val mensaje by usuarioViewModel.mensaje.collectAsState()
    val usuarioIngresado by usuarioViewModel.usuarioIngresado.collectAsState()
    val eventos by eventoViewModel.eventos.collectAsState()

    LaunchedEffect(usuarioIngresado) {
        if (usuarioIngresado) pantalla = "inicio"
    }

    LaunchedEffect(mensaje) {
        if (mensaje.isNotBlank()) mensajeValidacion = mensaje
    }

    fun guardarMatches(nuevos: Set<Int>) {
        usuariosConInteres = nuevos
        preferencias.edit()
            .putStringSet("matches", nuevos.map { it.toString() }.toSet())
            .apply()
    }

    fun guardarMensaje(usuarioId: Int, texto: String, mio: Boolean) {
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
        modifier = Modifier.fillMaxSize().background(Fondo)
    ) {
        when (pantalla) {
            "login" -> PantallaLogin(
                correo, contrasena,
                { correo = it }, { contrasena = it },
                {
                    if (correo.isBlank() || contrasena.isBlank()) {
                        mensajeValidacion = "Completa todos los campos."
                    } else {
                        usuarioViewModel.iniciarSesion(correo.trim(), contrasena)
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
                nombre, correo, contrasena,
                { nombre = it }, { correo = it }, { contrasena = it },
                {
                    if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
                        mensajeValidacion = "Completa todos los campos."
                    } else if (!correo.contains("@") || !correo.contains(".")) {
                        mensajeValidacion = "Ingresa un correo válido."
                    } else if (contrasena.length < 6) {
                        mensajeValidacion = "La contraseña debe tener al menos 6 caracteres."
                    } else {
                        usuarioViewModel.registrarUsuario(nombre.trim(), correo.trim(), contrasena)
                    }
                },
                { pantalla = "login" }
            )

            "inicio" -> PantallaInicio(
                onVerEventos = { pantalla = "eventos" },
                onCrearEvento = { pantalla = "crear" },
                onVerMatches = { pantalla = "matches" },
                onCerrarSesion = {
                    pantalla = "login"
                    correo = ""
                    contrasena = ""
                }
            )

            "eventos" -> PantallaEventos(
                eventos = eventos,
                onVolver = { pantalla = "inicio" },
                onCrear = { pantalla = "crear" },
                onEliminar = { eventoViewModel.eliminarEvento(it) },
                onSeleccionar = {
                    eventoSeleccionado = it
                    pantalla = "participantes"
                }
            )

            "participantes" -> eventoSeleccionado?.let { evento ->
                PantallaPersonasInteresadas(
                    evento = evento,
                    usuarios = usuariosDemo.filter {
                        it.deporte.equals(evento.deporte, ignoreCase = true)
                    },
                    usuariosConInteres = usuariosConInteres,
                    onVolver = { pantalla = "eventos" },
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
                matches = usuariosDemo.filter { it.id in usuariosConInteres },
                onVolver = { pantalla = "inicio" },
                onAbrirChat = {
                    usuarioChat = it
                    pantalla = "chat"
                }
            )

            "chat" -> usuarioChat?.let { usuario ->
                PantallaChat(
                    usuario = usuario,
                    mensajes = mensajes.filter { it.usuarioId == usuario.id },
                    texto = textoMensaje,
                    onTextoChange = { textoMensaje = it },
                    onEnviar = {
                        if (textoMensaje.isNotBlank()) {
                            guardarMensaje(usuario.id, textoMensaje.trim(), true)
                            textoMensaje = ""
                        }
                    },
                    onVolver = { pantalla = "matches" }
                )
            }

            "crear" -> PantallaCrearEvento(
                onVolver = { pantalla = "eventos" },
                onGuardar = { nombreEvento, deporte, fecha, lugar, maximo ->
                    eventoViewModel.agregarEvento(
                        nombreEvento, deporte, fecha, lugar, maximo
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
                title = { Text("Mygos Sport") },
                text = { Text(mensajeValidacion) },
                confirmButton = {
                    TextButton(onClick = {
                        mensajeValidacion = ""
                        usuarioViewModel.cerrarMensaje()
                        if (mensaje == "Registro exitoso.") pantalla = "login"
                    }) {
                        Text("Aceptar", color = Verde)
                    }
                }
            )
        }
    }
}

@Composable
fun PantallaLogin(
    correo: String,
    contrasena: String,
    onCorreoChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onIngresar: () -> Unit,
    onRegistro: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("MYGOS SPORT", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Verde)
        Spacer(Modifier.height(8.dp))
        Text("Encuentra tu próximo encuentro deportivo")
        Spacer(Modifier.height(32.dp))

        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Iniciar sesión", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    correo, onCorreoChange,
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    contrasena, onContrasenaChange,
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(22.dp))
                Button(
                    onClick = onIngresar,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Verde)
                ) { Text("Ingresar") }
                TextButton(onClick = onRegistro, modifier = Modifier.fillMaxWidth()) {
                    Text("¿No tienes cuenta? Regístrate", color = Verde)
                }
            }
        }
    }
}

@Composable
fun PantallaRegistro(
    nombre: String,
    correo: String,
    contrasena: String,
    onNombreChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onRegistrar: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("MYGOS SPORT", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Verde)
        Spacer(Modifier.height(24.dp))
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Crear cuenta", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(18.dp))
                OutlinedTextField(nombre, onNombreChange, label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(correo, onCorreoChange, label = { Text("Correo electrónico") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    contrasena, onContrasenaChange,
                    label = { Text("Contraseña (mínimo 6 caracteres)") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onRegistrar,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Verde)
                ) { Text("Registrarme") }
                TextButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver al inicio de sesión", color = Verde)
                }
            }
        }
    }
}

@Composable
fun PantallaInicio(
    onVerEventos: () -> Unit,
    onCrearEvento: () -> Unit,
    onVerMatches: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        Image(
            painter = painterResource(id = R.drawable.img),
            contentDescription = "Imagen deportiva de Mygos Sport",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(20.dp))

        Text("MYGOS SPORT", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Verde)
        Spacer(Modifier.height(12.dp))
        Text("¡Bienvenido!", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("Encuentra personas para practicar deporte.")
        Spacer(Modifier.height(30.dp))
        TarjetaDeporte("Fútbol", "Encuentra partidos y encuentros recreativos.")
        TarjetaDeporte("Básquetbol", "Participa en partidos y entrenamientos.")
        TarjetaDeporte("Vóleibol", "Comparte encuentros deportivos.")
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onVerEventos,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Verde)
        ) { Text("Explorar actividades") }

        OutlinedButton(onClick = onCrearEvento, modifier = Modifier.fillMaxWidth()) {
            Text("Crear actividad")
        }

        OutlinedButton(onClick = onVerMatches, modifier = Modifier.fillMaxWidth()) {
            Text("Mis Match y chats")
        }

        TextButton(onClick = onCerrarSesion) {
            Text("Cerrar sesión", color = Verde)
        }
    }
}

@Composable
fun TarjetaDeporte(titulo: String, descripcion: String) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 7.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(titulo, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Verde)
            Text(descripcion, color = TextoOscuro)
        }
    }
}

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
        Text("Actividades deportivas", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Verde)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onCrear,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Verde)
        ) { Text("Crear actividad") }
        Spacer(Modifier.height(16.dp))

        if (eventos.isEmpty()) Text("Todavía no hay actividades creadas.")

        eventos.forEach { evento ->
            AnimatedVisibility(visible = true, enter = fadeIn() + expandVertically()) {
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 7.dp).clickable {
                        onSeleccionar(evento)
                    },
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(evento.nombre, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Verde)
                        Text("Deporte: ${evento.deporte}")
                        Text("Fecha: ${evento.fecha}")
                        Text("Lugar: ${evento.lugar}")
                        Text("Máximo de participantes: ${evento.maxParticipantes}")
                        Spacer(Modifier.height(8.dp))
                        Text("Ver personas interesadas", color = Verde, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { onEliminar(evento.id) }) {
                            Text("Eliminar actividad", color = Color.Red)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
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
        Text("Personas interesadas", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Verde)
        Spacer(Modifier.height(8.dp))
        Text("Actividad: ${evento.nombre}", fontWeight = FontWeight.Bold)
        Text("Deporte: ${evento.deporte}")
        Text("Estas personas buscan compañeros para jugar.")
        Spacer(Modifier.height(20.dp))

        if (usuarios.isEmpty()) Text("Todavía no hay personas interesadas en este deporte.")

        usuarios.forEach { usuario ->
            Card(
                Modifier.fillMaxWidth().padding(vertical = 7.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(usuario.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Verde)
                    Text(usuario.descripcion)
                    Spacer(Modifier.height(12.dp))

                    if (usuario.id in usuariosConInteres) {
                        Text(
                            if (usuario.interesadoEnMi) "¡Tienen un Match!" else "Ya mostraste interés",
                            color = Verde,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Button(
                            onClick = { onInteres(usuario) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Verde)
                        ) { Text("Me interesa") }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text("Volver a actividades")
        }
    }
}

@Composable
fun PantallaMatches(
    matches: List<UsuarioDemo>,
    onVolver: () -> Unit,
    onAbrirChat: (UsuarioDemo) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Mis Match", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Verde)
        Spacer(Modifier.height(8.dp))
        Text("Aquí puedes revisar tus conexiones y abrir una conversación.")
        Spacer(Modifier.height(16.dp))

        if (matches.isEmpty()) {
            Text("Todavía no tienes Match. Explora actividades y muestra interés en otros perfiles.")
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(matches) { usuario ->
                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 7.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(usuario.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Verde)
                            Text(usuario.deporte)
                            Text(usuario.descripcion)
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { onAbrirChat(usuario) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Verde)
                            ) { Text("Abrir chat") }
                        }
                    }
                }
            }
        }

        OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
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
        Text("Chat con ${usuario.nombre}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Verde)
        Text("Conversación de demostración local.")
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = false
        ) {
            items(mensajes) { mensaje ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = if (mensaje.enviadoPorMi) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        Modifier.padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (mensaje.enviadoPorMi) Color(0xFFD8EFE1) else Color.White
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
            ) { Text("Enviar") }
        }

        OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text("Volver a mis Match")
        }
    }
}

@Composable
fun PantallaCrearEvento(
    onVolver: () -> Unit,
    onGuardar: (String, String, String, String, Int) -> Unit
) {
    val contexto = androidx.compose.ui.platform.LocalContext.current

    var nombre by remember { mutableStateOf("") }
    var deporte by remember { mutableStateOf("Fútbol") }
    var fecha by remember { mutableStateOf("") }
    var lugar by remember { mutableStateOf("") }
    var maximo by remember { mutableStateOf("10") }
    var error by remember { mutableStateOf("") }
    var buscandoUbicacion by remember { mutableStateOf(false) }

    val permisosUbicacion = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (concedido) {
            obtenerUbicacionActual(
                contexto = contexto,
                alObtener = { coordenadas ->
                    lugar = coordenadas
                    buscandoUbicacion = false
                },
                alFallar = {
                    error = it
                    buscandoUbicacion = false
                }
            )
        } else {
            error = "Debes permitir el acceso a la ubicación para utilizar el GPS."
            buscandoUbicacion = false
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Text("Crear actividad", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Verde)
        Spacer(Modifier.height(18.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre de la actividad") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        Text("Selecciona un deporte", fontWeight = FontWeight.Bold)

        listOf("Fútbol", "Básquetbol", "Vóleibol").forEach { opcion ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = deporte == opcion, onClick = { deporte = opcion })
                Text(opcion)
            }
        }

        OutlinedTextField(
            value = fecha,
            onValueChange = { fecha = it },
            label = { Text("Fecha y hora") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = lugar,
            onValueChange = { lugar = it },
            label = { Text("Lugar de encuentro o coordenadas GPS") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                buscandoUbicacion = true
                error = ""

                val permisoFino = ContextCompat.checkSelfPermission(
                    contexto,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                val permisoAproximado = ContextCompat.checkSelfPermission(
                    contexto,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (permisoFino || permisoAproximado) {
                    obtenerUbicacionActual(
                        contexto = contexto,
                        alObtener = { coordenadas ->
                            lugar = coordenadas
                            buscandoUbicacion = false
                        },
                        alFallar = {
                            error = it
                            buscandoUbicacion = false
                        }
                    )
                } else {
                    permisosUbicacion.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Verde),
            enabled = !buscandoUbicacion
        ) {
            Text(if (buscandoUbicacion) "Obteniendo ubicación..." else "Obtener mi ubicación GPS")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = maximo,
            onValueChange = { maximo = it },
            label = { Text("Máximo de participantes") },
            modifier = Modifier.fillMaxWidth()
        )

        if (error.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = Color.Red)
        }

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = {
                val cantidad = maximo.toIntOrNull()

                if (nombre.isBlank() || fecha.isBlank() || lugar.isBlank()) {
                    error = "Completa todos los campos."
                } else if (cantidad == null || cantidad <= 0) {
                    error = "Ingresa un máximo de participantes válido."
                } else {
                    onGuardar(nombre, deporte, fecha, lugar, cantidad)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Verde)
        ) {
            Text("Guardar actividad")
        }

        OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}

@Suppress("MissingPermission")
private fun obtenerUbicacionActual(
    contexto: Context,
    alObtener: (String) -> Unit,
    alFallar: (String) -> Unit
) {
    val clienteUbicacion = LocationServices.getFusedLocationProviderClient(contexto)
    val tokenCancelacion = CancellationTokenSource()

    try {
        clienteUbicacion.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            tokenCancelacion.token
        ).addOnSuccessListener { ubicacion ->
            if (ubicacion != null) {
                val coordenadas = "Latitud: ${ubicacion.latitude}, Longitud: ${ubicacion.longitude}"
                alObtener(coordenadas)
            } else {
                alFallar("No se pudo obtener la ubicación. Verifica que el GPS esté activado.")
            }
        }.addOnFailureListener {
            alFallar("Ocurrió un error al obtener la ubicación.")
        }
    } catch (e: SecurityException) {
        alFallar("No hay permiso para acceder al GPS.")
    }
}