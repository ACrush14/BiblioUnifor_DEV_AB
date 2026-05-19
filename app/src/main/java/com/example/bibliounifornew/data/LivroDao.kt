package com.example.bibliounifornew.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LivroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirLivro(livro: EntidadeLivro)

    // Dentro de LivroDao.kt
    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun buscarLivroPorId(id: String): EntidadeLivro? // Mudou para String

    @Query("SELECT * FROM books")
    fun buscarTodosLivros(): Flow<List<EntidadeLivro>>

    @Query("SELECT * FROM books WHERE title LIKE :query OR author LIKE :query OR isbn LIKE :query")
    fun pesquisarLivros(query: String): Flow<List<EntidadeLivro>>

    @Update
    suspend fun atualizarProgresso(livro: EntidadeLivro)

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getCount(): Int
}
