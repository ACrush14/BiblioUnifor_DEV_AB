package com.example.bibliounifornew.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class EntidadeLivro(
    @PrimaryKey(autoGenerate = false) // Mudamos para false
    val id: String = "", // Agora é String, guardará o ID do Firestore
    val title: String,
    val author: String,
    val isbn: String = "",
    val category: String = "",
    val isAvailable: Boolean = true,
    val publishDate: String = "",
    val content: String = "",
    val lastPosition: Int = 0,
    val isFavorite: Boolean = false,
    val totalPages: Int = 0,
    val coverUrl: String = ""
)