package com.example.bibliounifornew.adm

/**
 * Modelo de dados para representar um aluguel (solicitação de empréstimo)
 * na visão do administrador.
 */
data class ItemAluguel(
    val docId: String = "",
    val uidAluno: String = "",
    val idLivro: String = "",
    val dataMs: Long = 0L,
    val status: String = "pendente",
    val nomeUsuario: String = "Usuário",
    val tituloLivro: String = "Título Indisponível",
    val autorLivro: String = "Autor Desconhecido"
)
