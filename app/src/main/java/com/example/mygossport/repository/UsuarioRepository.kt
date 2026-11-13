
package com.example.mygossport.repository

import com.example.mygossport.data.UsuarioDao
import com.example.mygossport.data.UsuarioEntity

class UsuarioRepository(
    private val usuarioDao: UsuarioDao
) {
    suspend fun insertarUsuario(usuario: UsuarioEntity) {
        usuarioDao.insertarUsuario(usuario)
    }

    suspend fun buscarPorCorreo(correo: String): UsuarioEntity? {
        return usuarioDao.buscarPorCorreo(correo)
    }
}
