package com.example.mygossport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material3.MaterialTheme
import com.example.mygossport.data.DatabaseProvider
import com.example.mygossport.repository.EventoRepository
import com.example.mygossport.repository.UsuarioRepository
import com.example.mygossport.ui.MygosApp
import com.example.mygossport.viewmodel.EventoViewModel
import com.example.mygossport.viewmodel.EventoViewModelFactory
import com.example.mygossport.viewmodel.UsuarioViewModel
import com.example.mygossport.viewmodel.UsuarioViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val baseDatos = DatabaseProvider.obtenerBaseDatos(this)

        val eventoRepository = EventoRepository(baseDatos.eventoDao())
        val eventoFactory = EventoViewModelFactory(eventoRepository)

        val eventoViewModel = ViewModelProvider(this, eventoFactory)
            .get(EventoViewModel::class.java)

        val usuarioRepository = UsuarioRepository(baseDatos.usuarioDao())
        val usuarioFactory = UsuarioViewModelFactory(usuarioRepository)

        val usuarioViewModel = ViewModelProvider(this, usuarioFactory)
            .get(UsuarioViewModel::class.java)

        setContent {
            MaterialTheme {
                MygosApp(
                    usuarioViewModel = usuarioViewModel,
                    eventoViewModel = eventoViewModel
                )
            }
        }
    }
}