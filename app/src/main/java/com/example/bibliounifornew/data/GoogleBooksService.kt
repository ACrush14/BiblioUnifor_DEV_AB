package com.example.bibliounifornew.data

import com.example.bibliounifornew.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksService {
    @GET("books/v1/volumes")
    suspend fun buscarLivros(
        @Query("q")   nomeDoLivro: String,
        // Chave injetada via BuildConfig — origem: local.properties (não commitado)
        @Query("key") apiKey: String = BuildConfig.GOOGLE_BOOKS_API_KEY
    ): GoogleBooksResponse
}