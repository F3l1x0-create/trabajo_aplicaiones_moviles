
package com.example.mygossport.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        EventoEntity::class,
        UsuarioEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MygosDatabase : RoomDatabase() {

    abstract fun eventoDao(): EventoDao

    abstract fun usuarioDao(): UsuarioDao
}
