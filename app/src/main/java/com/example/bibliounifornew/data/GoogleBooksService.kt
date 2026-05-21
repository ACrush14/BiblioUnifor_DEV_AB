package com.example.bibliounifornew.data

import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksService {
    @GET("books/v1/volumes")
    suspend fun buscarLivros(
        @Query("q") nomeDoLivro: String,
        @Query("key") apiKey: String =  "AIzaSyAEojGm94sofbQ2ZRnlPjVPiKtrQKeqDE4"// <--- Adicionamos o parâmetro da chave aqui
    ): GoogleBooksResponse
}