package com.example.micomposito

import com.example.micomposito.domain.Persona

data class PersonaState(
    val nombre: String = "",
    val apellido: String = "",
    val personas: List<Persona> = emptyList(),
)
