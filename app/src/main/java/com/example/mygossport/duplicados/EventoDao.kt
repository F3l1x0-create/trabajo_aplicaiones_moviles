package com.example.mygossport.duplicados

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {

    @Query("SELECT * FROM eventos ORDER BY id DESC")
    fun obtenerEventos(): Flow<List<EventoEntity>>

    @Insert
    suspend fun insertarEvento(evento: EventoEntity)

    @Query("DELETE FROM eventos WHERE id = :eventoId")
    suspend fun eliminarEvento(eventoId: Int)
}
