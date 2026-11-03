package com.example.micomposito

import androidx.lifecycle.ViewModel
import com.example.micomposito.domain.Persona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PersonaViewModel : ViewModel() {

    private val _state = MutableStateFlow(PersonaState())
    val state: StateFlow<PersonaState> = _state.asStateFlow()

    fun onNombreChange(nombre: String) {
        _state.update { it.copy(nombre = nombre) }
    }

    fun onApellidoChange(apellido: String) {
        _state.update { it.copy(apellido = apellido) }
    }

    fun onAddPersona() {
        val state = _state.value
        if (state.nombre.isBlank() || state.apellido.isBlank()) return

        _state.update {
            it.copy(
                nombre = "",
                apellido = "",
                personas = it.personas + Persona(state.nombre.trim(), state.apellido.trim()),
            )
        }
    }
}
