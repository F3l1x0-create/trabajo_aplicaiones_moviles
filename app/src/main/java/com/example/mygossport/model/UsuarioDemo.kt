
package com.example.mygossport.model

data class UsuarioDemo(
    val id: Int,
    val nombre: String,
    val deporte: String,
    val descripcion: String,
    val interesadoEnMi: Boolean
)

val usuariosDemo = listOf(
    UsuarioDemo(1, "Carlos", "Fútbol", "Me gusta jugar los fines de semana.", true),
    UsuarioDemo(2, "Camila", "Fútbol", "Busco compañeros para jugar.", true),
    UsuarioDemo(3, "Diego", "Fútbol", "Juego de delantero.", false),
    UsuarioDemo(4, "Valentina", "Básquetbol", "Me gusta entrenar en equipo.", true),
    UsuarioDemo(5, "Matías", "Básquetbol", "Busco partidos recreativos.", true),
    UsuarioDemo(6, "Fernanda", "Vóleibol", "Me gusta el vóleibol mixto.", true),
    UsuarioDemo(7, "Andrés", "Vóleibol", "Juego los fines de semana.", false)
)
