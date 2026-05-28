package com.example.bibliounifornew.features.adm.dashboard

/**
 * Representa um livro com prazo de devolução vencido ou prestes a vencer.
 * Utilizado na tela de Dashboard Financeiro ADM (TelaRF34).
 */
data class LivroVencidoModel(
    val docIdAtual: String,
    val uidAlunoAtual: String,
    val idLivro: String,
    val dataSolicitacaoMs: Long,
    val dataLimiteMs: Long,
    val diasAtraso: Int,
    val valorMulta: Double,
    val nomeAluno: String,
    val tituloLivro: String,
    val autorLivro: String,
    val status: String
)
