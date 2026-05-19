package com.example.bibliounifornew.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class EntidadeLivro(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val author: String,
    val isbn: String = "",
    val category: String = "", // Online, Presencial
    val isAvailable: Boolean = true, // Disponíveis, Alugados
    val publishDate: String = "",
    val content: String = "", // O texto do livro
    val lastPosition: Int = 0, // Onde o usuário parou (o scroll)
    val isFavorite: Boolean = false,
    val totalPages: Int = 0,
    val coverResourceId: Int = 0
)
