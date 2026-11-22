package com.example.appmovilesmvvm.data

import com.example.appmovilesmvvm.domain.model.Politico

object Politicos {
    private val politicos = mutableListOf(
        Politico("Perro S.", "Partido de los Perros", 0),
        Politico("Juan P.", "Partido de los Gatos", 0),
        Politico("Maria L.", "Partido de los Pájaros", 0),
        )

    fun damePresidente() = politicos[0]
}
