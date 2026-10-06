package com.example.appmovilesmvvm.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appmovilesmvvm.domain.usecases.DamePresidenteUseCase

class MainViewModel(val damePresidenteUseCase: DamePresidenteUseCase) : ViewModel() {
    private val _state = MutableLiveData<MainState>(MainState())
    val state: MutableLiveData<MainState> = _state

    fun handleDamePresidente(): Unit {
        var presi = damePresidenteUseCase.damePresidente()
        _state.value = _state.value?.copy(presidente = presi.nombre)
    }

    class MainViewModelFactory(private val damePresidenteUseCase: DamePresidenteUseCase) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(damePresidenteUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}