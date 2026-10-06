package com.example.appmovilesmvvm.di

import com.example.appmovilesmvvm.domain.usecases.DamePresidenteUseCase
import com.example.appmovilesmvvm.ui.MainViewModel

object AppModule {
    val damePresidenteUseCase : DamePresidenteUseCase = DamePresidenteUseCase()
    fun provideMainViewModel() : MainViewModel = MainViewModel(damePresidenteUseCase)
}