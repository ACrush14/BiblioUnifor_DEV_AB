package com.example.bibliounifornew.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow

class LivroRepository(
    private val livroDao: LivroDao,
    private val firestore: FirebaseFirestore // Injetamos o Firestore aqui
) {

    // A UI vai observar apenas esta função. Se o Room mudar, a UI atualiza na hora.
    fun buscarTodosLivros(): Flow<List<EntidadeLivro>> {
        return livroDao.buscarTodosLivros()
    }

    // Função de alta performance: Busca na nuvem e atualiza o banco local
    suspend fun sincronizarLivrosDoFirestore() {
        try {
            // Puxa a coleção "livros" do Firestore
            val snapshot = firestore.collection("livros").get().await()

            for (documento in snapshot.documents) {
                // Mapeia o documento da nuvem para a nossa entidade local
                val livro = EntidadeLivro(
                    id = documento.id, // O ID único do Firebase
                    title = documento.getString("title") ?: "",
                    author = documento.getString("author") ?: "",
                    isbn = documento.getString("isbn") ?: "",
                    category = documento.getString("category") ?: "",
                    isAvailable = documento.getBoolean("isAvailable") ?: true,
                    publishDate = documento.getString("publishDate") ?: "",
                    content = documento.getString("content") ?: "",
                    coverUrl = documento.getString("coverUrl") ?: ""
                )

                // O OnConflictStrategy.REPLACE no DAO vai atualizar se já existir ou criar se for novo
                livroDao.inserirLivro(livro)
            }
            Log.d("LivroRepository", "Sincronização concluída com sucesso. ${snapshot.size()} livros processados.")
        } catch (e: Exception) {
            Log.e("LivroRepository", "Falha ao sincronizar com Firestore: ${e.message}")
            // Aqui entra a vantagem da nossa arquitetura: se der erro (ex: sem internet),
            // a UI não quebra, pois ela continua lendo o cache do Room!
        }
    }

    suspend fun buscarLivroPorId(id: String): EntidadeLivro? {
        return livroDao.buscarLivroPorId(id)
    }
}