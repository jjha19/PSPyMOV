package com.example.micomposito.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.micomposito.domain.Persona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PersonaViewModel : ViewModel() {

    private val _state = MutableLiveData(PersonaState())
    val state: LiveData<PersonaState> = _state

    init {
        loadPersonas()
    }

    fun loadPersonas() {
//        _state.value = PersonaState(
//            nombre = "Juan",
//            apellido = "Pérez",
//            personas = listOf(Persona("María", "Gómez"))
//        )
        _state.value = state.value?.copy(personas = personaRepository.getAllPersonas())
    }

    fun onNombreChange(nombre: String) {
        _state.value = _state.value?.copy(nombre = nombre)
    }

    fun onApellidoChange(apellido: String) {
        _state.value = _state.value?.copy(apellido = apellido)
    }

    fun onAddPersona() {
        val state = _state.value
        if (state == null) return
        if (state.nombre.isBlank() || state.apellido.isBlank()) return

        _state.value = _state.value?.copy(
            nombre = "",
            apellido = "",
            personas = _state.value?.personas.orEmpty() + Persona(state.nombre.trim(), state.apellido.trim()),
        )
    }
}
