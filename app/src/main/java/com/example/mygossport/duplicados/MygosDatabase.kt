package com.example.mygossport.duplicados

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS usuarios (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre TEXT NOT NULL,
                        correo TEXT NOT NULL,
                        contrasena TEXT NOT NULL,
                        sesionActiva INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
