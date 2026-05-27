package com.example.bibliounifornew.data

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
                val minhaChaveApi = "AIzaSyAEojGm94sofbQ2ZRnlPjVPiKtrQKeqDE4"
                val resposta = googleBooksService.buscarLivros(termoDeBusca, minhaChaveApi)
                val listaLivrosApi = resposta.items?.take(5)

                if (!listaLivrosApi.isNullOrEmpty()) {
                    for (livroApi in listaLivrosApi) {
                        val info = livroApi.volumeInfo
                        val tituloEncontrado = info.title ?: continue

                        // Verifica duplicata de forma síncrona (na thread IO)
                        val querySnapshot = Tasks.await(
                            firestore.collection("livros")
                                .whereEqualTo("titulo", tituloEncontrado)
                                .get()
                        )

                        if (querySnapshot.isEmpty) {
                            val isbn = info.industryIdentifiers?.find { it.type == "ISBN_13" }?.identifier
                                ?: info.industryIdentifiers?.firstOrNull()?.identifier
                                ?: "0000000000000"

                            val dadosLivro = hashMapOf(
                                "titulo" to tituloEncontrado,
                                "autor" to (info.authors?.joinToString(", ") ?: "Autor Desconhecido"),
                                "descricao" to (info.description ?: "Sem descrição disponível."),
                                "coverUrl" to (info.imageLinks?.thumbnail?.replace("http://", "https://") ?: ""),
                                "isbn" to isbn,
                                "genero" to (info.categories?.joinToString(", ") ?: "Gênero Desconhecido")
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
