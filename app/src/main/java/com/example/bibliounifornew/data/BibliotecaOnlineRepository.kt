package com.example.bibliounifornew.data

import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class BibliotecaOnlineRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // Inicializa o serviço do Google Books
    private val googleBooksService = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GoogleBooksService::class.java)

    // Busca online e salva no Firestore
    suspend fun buscarEImportarLivro(
        termoDeBusca: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            // O termoDeBusca pode ser o título, o autor ou o próprio número do ISBN digitado
            val resposta = googleBooksService.buscarLivros(termoDeBusca)
            val primeiroLivroEncontrado = resposta.items?.firstOrNull()

            if (primeiroLivroEncontrado != null) {
                val info = primeiroLivroEncontrado.volumeInfo

                // Tenta capturar o ISBN_13 prioritariamente, senão pega o primeiro identificador que achar
                val isbnEncontrado = info.industryIdentifiers?.find { it.type == "ISBN_13" }?.identifier
                    ?: info.industryIdentifiers?.firstOrNull()?.identifier
                    ?: "Não informado"

                // Monta o mapa completo para o Firestore com os novos campos
                val dadosLivro = hashMapOf(
                    "titulo" to (info.title ?: "Título Desconhecido"),
                    "autor" to (info.authors?.joinToString(", ") ?: "Autor Desconhecido"),
                    "genero" to (info.categories?.joinToString(", ") ?: "Gênero Desconhecido"),
                    "isbn" to isbnEncontrado,
                    "descricao" to (info.description ?: "Sem descrição disponível."),
                    "coverUrl" to (info.imageLinks?.thumbnail?.replace("http://", "https://") ?: "")
                )

                // Grava na coleção 'livros' do Firebase
                firestore.collection("livros")
                    .add(dadosLivro)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e -> onFailure(e) }
            } else {
                onFailure(Exception("Nenhum livro localizado para a busca informada."))
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}