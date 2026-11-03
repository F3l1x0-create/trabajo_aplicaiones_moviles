
package com.example.mygossport.data

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

    @Query("DELETE FROM eventos WHERE id = :id")
    suspend fun eliminarEvento(id: Int)

    @Query("SELECT COUNT(*) FROM eventos WHERE nombre = :nombre")
    suspend fun contarPorNombre(nombre: String): Int
}
