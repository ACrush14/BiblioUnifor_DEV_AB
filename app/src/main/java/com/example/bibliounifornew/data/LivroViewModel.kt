package com.example.bibliounifornew.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bibliounifornew.data.EntidadeLivro
import com.example.bibliounifornew.data.LivroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LivroViewModel(private val repository: LivroRepository) : ViewModel() {

    init {
        // Ao iniciar o ViewModel, já tentamos sincronizar
        sincronizarComNuvem()
    }

    // A UI vai observar essa lista. Qualquer mudança no Room atualiza a tela automaticamente.
    val todosOsLivros: Flow<List<EntidadeLivro>> = repository.buscarTodosLivros()

    // Função para engatilhar a sincronização com o Firebase quando o app abrir
    fun sincronizarComNuvem() {
        viewModelScope.launch {
            repository.sincronizarLivrosDoFirestore()
        }
    }



    // Busca um livro específico (útil para a TelaRF12 e TelaRF13)
    // Retornamos o objeto diretamente, ou nulo se não achar.
    suspend fun buscarLivroPorId(id: String): EntidadeLivro? {
        return repository.buscarLivroPorId(id)
    }


    fun pesquisarLivros(query: String): Flow<List<EntidadeLivro>> {
        // Opcional: Chame a sincronização aqui também para garantir dados frescos
        // viewModelScope.launch { repository.sincronizarLivrosDoFirestore() }

        return repository.pesquisarLivrosLocais(query)
    }
}