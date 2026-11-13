package com.example.mygossport.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        Text(
            "MYGOS SPORT",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Verde
        )

        Spacer(Modifier.height(8.dp))
        Text("Encuentra tu próximo encuentro deportivo")
        Spacer(Modifier.height(32.dp))

        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Iniciar sesión",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    correo,
                    onCorreoChange,
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    contrasena,
                    onContrasenaChange,
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
                ) {
                    Text("Ingresar")
                }

                TextButton(
                    onClick = onRegistro,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
        Text(
            "MYGOS SPORT",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Verde
        )

        Spacer(Modifier.height(24.dp))

        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Crear cuenta",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(18.dp))

                OutlinedTextField(
                    nombre,
                    onNombreChange,
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    correo,
                    onCorreoChange,
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    contrasena,
                    onContrasenaChange,
                    label = { Text("Contraseña (mínimo 6 caracteres)") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = onRegistrar,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Verde)
                ) {
                    Text("Registrarme")
                }

                TextButton(
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver al inicio de sesión", color = Verde)
                }
            }
        }
    }
}