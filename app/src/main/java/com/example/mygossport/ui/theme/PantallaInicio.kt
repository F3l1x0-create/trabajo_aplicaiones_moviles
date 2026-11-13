package com.example.mygossport.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mygossport.R

@Composable
fun PantallaInicio(
    onVerEventos: () -> Unit,
    onCrearEvento: () -> Unit,
    onVerMatches: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
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

        Text(
            "MYGOS SPORT",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Verde
        )

        Spacer(Modifier.height(12.dp))

        Text("¡Bienvenido!", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("Encuentra personas para practicar deporte.")

        Spacer(Modifier.height(30.dp))

        TarjetaDeporte(
            "Fútbol",
            "Encuentra partidos y encuentros recreativos."
        )

        TarjetaDeporte(
            "Básquetbol",
            "Participa en partidos y entrenamientos."
        )

        TarjetaDeporte(
            "Vóleibol",
            "Comparte encuentros deportivos."
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onVerEventos,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Verde)
        ) {
            Text("Explorar actividades")
        }

        OutlinedButton(
            onClick = onCrearEvento,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear actividad")
        }

        OutlinedButton(
            onClick = onVerMatches,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mis Match y chats")
        }

        TextButton(onClick = onCerrarSesion) {
            Text("Cerrar sesión", color = Verde)
        }
    }
}

@Composable
fun TarjetaDeporte(
    titulo: String,
    descripcion: String
) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 7.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                titulo,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Verde
            )

            Text(descripcion, color = TextoOscuro)
        }
    }
}