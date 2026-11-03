package com.example.mygossport.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mygossport.data.UsuarioDao
import com.example.mygossport.data.UsuarioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UsuarioViewModel(
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    private val _mensaje = MutableStateFlow("")
    val mensaje: StateFlow<String> = _mensaje

    private val _usuarioIngresado = MutableStateFlow(false)
    val usuarioIngresado: StateFlow<Boolean> = _usuarioIngresado

    fun registrarUsuario(
        nombre: String,
        correo: String,
        contrasena: String
    ) {
        viewModelScope.launch {
            try {
                val usuarioExistente = usuarioDao.buscarPorCorreo(correo)

                if (usuarioExistente != null) {
                    _mensaje.value = "Este correo ya está registrado."
                } else {
                    usuarioDao.insertarUsuario(
                        UsuarioEntity(
                            nombre = nombre,
                            correo = correo,
                            contrasena = contrasena
                        )
                    )

                    _mensaje.value = "Registro exitoso."
                }
            } catch (e: Exception) {
                _mensaje.value = "No se pudo completar el registro."
            }
        }
    }

    fun iniciarSesion(correo: String, contrasena: String) {
        viewModelScope.launch {
            try {
                val usuario = usuarioDao.buscarPorCorreo(correo)

                if (usuario != null && usuario.contrasena == contrasena) {
                    _usuarioIngresado.value = true
                    _mensaje.value = "Bienvenido a Mygos Sport."
                } else {
                    _mensaje.value = "Correo o contraseña incorrectos."
                }
            } catch (e: Exception) {
                _mensaje.value = "No se pudo iniciar sesión."
            }
        }
    }

    fun cerrarMensaje() {
        _mensaje.value = ""
    }
}

class UsuarioViewModelFactory(
    private val usuarioDao: UsuarioDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsuarioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UsuarioViewModel(usuarioDao) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}