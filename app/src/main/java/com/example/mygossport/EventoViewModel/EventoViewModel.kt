
package com.example.mygossport.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mygossport.data.EventoEntity
import com.example.mygossport.data.EventoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventoViewModel(
    private val repository: EventoRepository
) : ViewModel() {

    val eventos = repository.obtenerEventos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    init {
        cargarActividadesDeEjemplo()
    }

    private fun cargarActividadesDeEjemplo() {
        viewModelScope.launch {

            val actividades = listOf(
                EventoEntity(
                    nombre = "Fútbol 7 recreativo",
                    deporte = "Fútbol",
                    fecha = "Sábado 16:00",
                    lugar = "Cancha deportiva",
                    maxParticipantes = 14
                ),
                EventoEntity(
                    nombre = "Fútbol amistoso",
                    deporte = "Fútbol",
                    fecha = "Domingo 11:00",
                    lugar = "Complejo deportivo",
                    maxParticipantes = 14
                ),
                EventoEntity(
                    nombre = "Básquetbol mixto",
                    deporte = "Básquetbol",
                    fecha = "Sábado 17:00",
                    lugar = "Gimnasio municipal",
                    maxParticipantes = 10
                ),
                EventoEntity(
                    nombre = "Entrenamiento grupal",
                    deporte = "Básquetbol",
                    fecha = "Domingo 10:00",
                    lugar = "Cancha techada",
                    maxParticipantes = 12
                ),
                EventoEntity(
                    nombre = "Vóleibol mixto",
                    deporte = "Vóleibol",
                    fecha = "Sábado 15:00",
                    lugar = "Centro deportivo",
                    maxParticipantes = 12
                ),
                EventoEntity(
                    nombre = "Encuentro recreativo",
                    deporte = "Vóleibol",
                    fecha = "Domingo 16:00",
                    lugar = "Gimnasio municipal",
                    maxParticipantes = 10
                )
            )

            actividades.forEach { actividad ->
                if (repository.contarPorNombre(actividad.nombre) == 0) {
                    repository.insertarEvento(actividad)
                }
            }
        }
    }

    fun agregarEvento(
        nombre: String,
        deporte: String,
        fecha: String,
        lugar: String,
        maxParticipantes: Int
    ) {
        viewModelScope.launch {
            repository.insertarEvento(
                EventoEntity(
                    nombre = nombre,
                    deporte = deporte,
                    fecha = fecha,
                    lugar = lugar,
                    maxParticipantes = maxParticipantes
                )
            )
        }
    }

    fun eliminarEvento(id: Int) {
        viewModelScope.launch {
            repository.eliminarEvento(id)
        }
    }
}

class EventoViewModelFactory(
    private val repository: EventoRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventoViewModel(repository) as T
        }

        throw IllegalArgumentException("ViewModel desconocido")
    }
}
