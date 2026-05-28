package com.example.bibliounifornew.data

import com.example.bibliounifornew.BuildConfig
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BibliotecaOnlineRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private val googleBooksService = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GoogleBooksService::class.java)

    suspend fun buscarEImportarLivro(termoDeBusca: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                // Chave lida de BuildConfig — origem: local.properties (nunca commitado)
                val resposta = googleBooksService.buscarLivros(termoDeBusca, BuildConfig.GOOGLE_BOOKS_API_KEY)
                val listaLivrosApi = resposta.items?.take(5)

                if (!listaLivrosApi.isNullOrEmpty()) {
                    for (livroApi in listaLivrosApi) {
                        val info = livroApi.volumeInfo
                        val tituloEncontrado = info.title ?: continue

                        val isbn13 = info.industryIdentifiers?.find { it.type == "ISBN_13" }?.identifier ?: ""
                        val isbn10 = info.industryIdentifiers?.find { it.type == "ISBN_10" }?.identifier ?: ""

                        // Verifica duplicata por ISBN ou Título
                        val querySnapshot = if (isbn13.isNotEmpty()) {
                            Tasks.await(firestore.collection("livros").whereEqualTo("isbn13", isbn13).get())
                        } else if (isbn10.isNotEmpty()) {
                            Tasks.await(firestore.collection("livros").whereEqualTo("isbn10", isbn10).get())
                        } else {
                            Tasks.await(firestore.collection("livros").whereEqualTo("title", tituloEncontrado).get())
                        }

                        if (querySnapshot.isEmpty) {
                            val dadosLivro = hashMapOf(
                                "title" to tituloEncontrado,
                                "author" to (info.authors?.joinToString(", ") ?: "Autor Desconhecido"),
                                "description" to (info.description ?: "Sem descrição disponível."),
                                "coverUrl" to (info.imageLinks?.thumbnail?.replace("http://", "https://") ?: ""),
                                "isbn10" to isbn10,
                                "isbn13" to isbn13,
                                "category" to (info.categories?.firstOrNull() ?: "Gênero Desconhecido"),
                                "publishDate" to (info.publishedDate ?: ""),
                                "publisher" to (info.publisher ?: ""),
                                "language" to (info.language ?: ""),
                                "totalPages" to (info.pageCount ?: 0),
                                "isAvailable" to true
                            )
                            // Adiciona ao Firestore e espera concluir
                            Tasks.await(firestore.collection("livros").add(dadosLivro))
                        }
                    }
                    onSuccess()
                } else {
                    onFailure(Exception("Nenhum livro encontrado na API."))
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }
}
