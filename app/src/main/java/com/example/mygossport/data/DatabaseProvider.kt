package com.example.mygossport.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: MygosDatabase? = null

    fun obtenerBaseDatos(context: Context): MygosDatabase {
        return INSTANCE ?: synchronized(this) {

            val instancia = Room.databaseBuilder(
                context.applicationContext,
                MygosDatabase::class.java,
                "mygos_simple_database"
            )
                .fallbackToDestructiveMigration()
                .build()

            INSTANCE = instancia
            instancia
        }
    }
}