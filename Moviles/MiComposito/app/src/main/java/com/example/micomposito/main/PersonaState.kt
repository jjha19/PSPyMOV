package com.example.micomposito.main

import com.example.micomposito.domain.Persona

data class PersonaState(
    val nombre: String = "",
    val apellido: String = "",
    val personas: List<Persona> = ArrayList(),
    val error: String? = null,
)
