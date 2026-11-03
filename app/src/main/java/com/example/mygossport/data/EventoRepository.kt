
package com.example.mygossport.data

import kotlinx.coroutines.flow.Flow

class EventoRepository(
    private val eventoDao: EventoDao
) {

    fun obtenerEventos(): Flow<List<EventoEntity>> {
        return eventoDao.obtenerEventos()
    }

    suspend fun insertarEvento(evento: EventoEntity) {
        eventoDao.insertarEvento(evento)
    }

    suspend fun eliminarEvento(id: Int) {
        eventoDao.eliminarEvento(id)
    }

    suspend fun contarPorNombre(nombre: String): Int {
        return eventoDao.contarPorNombre(nombre)
    }
}
