
package com.example.mygossport

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.tasks.await

private val VerdeMapa = Color(0xFF176B45)

@Composable
fun PantallaMapa(
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val clienteUbicacion = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var ubicacion by remember {
        mutableStateOf<LatLng?>(null)
    }

    var mensaje by remember {
        mutableStateOf("Permite el acceso a tu ubicación para encontrar tu posición.")
    }

    val posicionInicial = LatLng(-33.4489, -70.6693)

    val posicionCamara = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(posicionInicial, 13f)
    }

    val solicitarPermiso =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permisos ->
            permisoConcedido =
                permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (permisoConcedido) {
                mensaje = "Permiso concedido. Buscando ubicación..."
            } else {
                mensaje = "No se concedió el permiso de ubicación."
            }
        }

    LaunchedEffect(permisoConcedido) {
        if (permisoConcedido) {
            try {
                @Suppress("MissingPermission")
                val ultimaUbicacion: Location? =
                    clienteUbicacion.lastLocation.await()

                if (ultimaUbicacion != null) {
                    val nuevaPosicion = LatLng(
                        ultimaUbicacion.latitude,
                        ultimaUbicacion.longitude
                    )

                    ubicacion = nuevaPosicion
                    posicionCamara.position =
                        CameraPosition.fromLatLngZoom(nuevaPosicion, 16f)

                    mensaje = "Ubicación encontrada."
                } else {
                    mensaje =
                        "No hay ubicación disponible. Configúrala en el emulador."
                }
            } catch (e: Exception) {
                mensaje = "No se pudo obtener la ubicación. Inténtalo de nuevo."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Mi ubicación",
            fontSize = 26.sp,
            color = VerdeMapa
        )

        Text(
            text = mensaje,
            fontSize = 14.sp
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = posicionCamara,
                properties = MapProperties(
                    isMyLocationEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false
                )
            ) {
                Marker(
                    state = com.google.maps.android.compose.rememberMarkerState(
                        position = ubicacion ?: posicionInicial
                    ),
                    title = if (ubicacion != null) {
                        "Mi ubicación"
                    } else {
                        "Ubicación inicial de referencia"
                    }
                )
            }
        }

        if (!permisoConcedido) {
            Button(
                onClick = {
                    solicitarPermiso.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeMapa
                )
            ) {
                Text("Permitir acceso al GPS")
            }
        }

        Button(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE1EAE3),
                contentColor = VerdeMapa
            )
        ) {
            Text("Volver al inicio")
        }
    }
}
