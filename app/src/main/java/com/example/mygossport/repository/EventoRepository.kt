
package com.example.mygossport.repository

import com.example.mygossport.data.EventoDao
import com.example.mygossport.data.EventoEntity

class EventoRepository(
    private val eventoDao: EventoDao
) {
    fun obtenerEventos() = eventoDao.obtenerEventos()

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
