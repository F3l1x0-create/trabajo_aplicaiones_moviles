
package com.example.mygossport.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eventos")
data class EventoEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,
    val deporte: String,
    val fecha: String,
    val lugar: String,
    val maxParticipantes: Int
)
