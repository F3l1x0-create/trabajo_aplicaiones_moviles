
package com.example.mygossport.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Esta clase representa un evento deportivo en la base de datos.
@Entity(tableName = "eventos")
data class EventoEntity(

    // Identificador único de cada evento.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,
    val deporte: String,
    val fecha: String,
    val lugar: String,
    val maxParticipantes: Int
)
