package com.example.bibliounifornew.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow

class LivroRepository(
    private val livroDao: LivroDao,
    private val firestore: FirebaseFirestore
) {

    fun buscarTodosLivros(): Flow<List<EntidadeLivro>> {
        return livroDao.buscarTodosLivros()
    }

    suspend fun sincronizarLivrosDoFirestore() {
        try {
            Log.d("LivroRepository", "Iniciando busca no Firestore...")
            val snapshot = firestore.collection("livros").get().await()
            Log.d("LivroRepository", "Documentos encontrados: ${snapshot.size()}")

            for (documento in snapshot.documents) {
                val livro = EntidadeLivro(
                    id = documento.id,
                    title = documento.getString("title") ?: "",
                    author = documento.getString("author") ?: "",
                    description = documento.getString("description") ?: "",
                    category = documento.getString("category") ?: "",
                    isAvailable = documento.getBoolean("isAvailable") ?: true,
                    publishDate = documento.getString("publishDate") ?: "",
                    coverUrl = documento.getString("coverUrl") ?: "",
                    isbn10 = documento.getString("isbn10") ?: "",
                    isbn13 = documento.getString("isbn13") ?: "",
                    publisher = documento.getString("publisher") ?: "",
                    language = documento.getString("language") ?: ""
                )
                livroDao.inserirLivro(livro)
            }
            Log.d("LivroRepository", "Sincronização concluída.")
        } catch (e: Exception) {
            Log.e("LivroRepository", "Erro ao sincronizar: ", e)
        }
    }

    suspend fun buscarLivroPorId(id: String): EntidadeLivro? {
        return livroDao.buscarLivroPorId(id)
    }

    fun pesquisarLivrosLocais(query: String): Flow<List<EntidadeLivro>> {
        // Chamada direta ao DAO com o padrão correto
        return livroDao.pesquisarLivros("%$query%")
    }
}