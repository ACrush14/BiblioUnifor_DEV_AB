package com.example.bibliounifornew.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class EntidadeLivro(
    @PrimaryKey
    val id: String = "",
    val title: String,
    val author: String,
    val description: String = "", // Substitui o 'content' para ficar mais semântico
    val category: String = "",
    val coverUrl: String = "",
    val stockQuantity: Int = 0,
    val rating: Float = 0f,
    val language: String = "",
    val publisher: String = "",
    val dimensions: String = "",
    val isbn10: String = "",
    val isbn13: String = "",
    val asin: String = "",
    val publishDate: String = "",
    val totalPages: Int = 0,
    val hasPdf: Boolean = false,
    val hasBraille: Boolean = false,
    val hasAudiobook: Boolean = false,
    val librarySector: String = "", // Ex: 75H.102B
    val isAvailable: Boolean = true,
    val isFavorite: Boolean = false
)
