
package com.example.mygossport

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE sesionActiva = 1 LIMIT 1")
    suspend fun obtenerSesionActiva(): UsuarioEntity?

    @Query("UPDATE usuarios SET sesionActiva = 0")
    suspend fun cerrarSesion()

    @Query("UPDATE usuarios SET sesionActiva = 1 WHERE id = :usuarioId")
    suspend fun activarSesion(usuarioId: Int)
}
