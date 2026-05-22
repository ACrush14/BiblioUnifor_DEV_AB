package com.example.bibliounifornew.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bibliounifornew.data.LivroRepository

class LivroViewModelFactory(private val repository: LivroRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LivroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LivroViewModel(repository) as T
        }
        throw IllegalArgumentException("Classe ViewModel desconhecida")
    }
}