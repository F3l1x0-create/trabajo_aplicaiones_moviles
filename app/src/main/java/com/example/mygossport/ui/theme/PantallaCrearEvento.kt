package com.example.mygossport.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

@Composable
fun PantallaCrearEvento(
    onVolver: () -> Unit,
    onGuardar: (String, String, String, String, Int) -> Unit
) {
    val contexto = LocalContext.current

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

        val concedido =
            permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
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
        Text(
            "Crear actividad",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = Verde
        )

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
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(
                    selected = deporte == opcion,
                    onClick = { deporte = opcion }
                )
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
            Text(
                if (buscandoUbicacion) {
                    "Obteniendo ubicación..."
                } else {
                    "Obtener mi ubicación GPS"
                }
            )
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

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
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
    val clienteUbicacion =
        LocationServices.getFusedLocationProviderClient(contexto)

    val tokenCancelacion = CancellationTokenSource()

    try {
        clienteUbicacion.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            tokenCancelacion.token
        ).addOnSuccessListener { ubicacion ->

            if (ubicacion != null) {
                val coordenadas =
                    "Latitud: ${ubicacion.latitude}, Longitud: ${ubicacion.longitude}"

                alObtener(coordenadas)
            } else {
                alFallar(
                    "No se pudo obtener la ubicación. Verifica que el GPS esté activado."
                )
            }

        }.addOnFailureListener {
            alFallar("Ocurrió un error al obtener la ubicación.")
        }

    } catch (e: SecurityException) {
        alFallar("No hay permiso para acceder al GPS.")
    }
}