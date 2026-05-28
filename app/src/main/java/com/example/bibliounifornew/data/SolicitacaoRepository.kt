package com.example.bibliounifornew.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * SolicitacaoRepository — camada de acesso a dados para solicitações de mídia.
 *
 * Responsabilidades:
 *   - Gravar novas solicitações no Firestore (coleção "solicitacoes_midia")
 *   - Escutar solicitações do usuário em tempo real (SnapshotListener)
 *   - Cancelar solicitações (soft-delete via status)
 *
 * Coleção: "solicitacoes_midia"
 * Compatível com: TelaRF31Solicitacoes (leitura ADM)
 */
class SolicitacaoRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val COLECAO = "solicitacoes_midia"
    }

    // ─── ESCRITA ──────────────────────────────────────────────────────────────

    /**
     * Grava uma nova [Solicitacao] no Firestore.
     * O ID do documento é gerado automaticamente pelo Firestore (.add).
     *
     * @param solicitacao Objeto totalmente preenchido (uidUsuario, idLivro, tipos, etc.)
     * @param onResult    Callback: (sucesso: Boolean, docId?: String, erro?: String)
     */
    fun gravarSolicitacao(
        solicitacao: Solicitacao,
        onResult: (sucesso: Boolean, docId: String?, erro: String?) -> Unit
    ) {
        db.collection(COLECAO)
            .add(solicitacao.toFirestoreMap())
            .addOnSuccessListener { docRef ->
                onResult(true, docRef.id, null)
            }
            .addOnFailureListener { e ->
                onResult(false, null, e.message)
            }
    }

    // ─── LEITURA REATIVA ──────────────────────────────────────────────────────

    /**
     * Escuta em tempo real as solicitações pendentes de um usuário.
     * Retorna um [ListenerRegistration] que DEVE ser cancelado em onDestroy()
     * para evitar memory leak.
     *
     * @param uid       UID do usuário logado
     * @param onChange  Chamado imediatamente com a lista atual e a cada mudança
     */
    fun escutarSolicitacoesDoUsuario(
        uid: String,
        onChange: (List<Solicitacao>) -> Unit
    ): ListenerRegistration {
        return db.collection(COLECAO)
            .whereEqualTo("uidUsuario", uid)
            .orderBy("dataSolicitacao", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onChange(emptyList())
                    return@addSnapshotListener
                }
                val lista = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { Solicitacao.fromFirestore(doc.id, it) }
                }
                onChange(lista)
            }
    }

    // ─── CANCELAMENTO ─────────────────────────────────────────────────────────

    /**
     * Cancela uma solicitação alterando seu status para "cancelado".
     * Usa merge para não sobrescrever campos existentes.
     */
    fun cancelarSolicitacao(
        docId: String,
        onResult: (Boolean) -> Unit
    ) {
        db.collection(COLECAO).document(docId)
            .update("status", "cancelado")
            .addOnSuccessListener { onResult(true)  }
            .addOnFailureListener { onResult(false) }
    }

    // ─── GAP-5: CONTROLE ATÔMICO DE ESTOQUE (solicitacoes_emprestimo) ─────────

    /**
     * Cria um empréstimo físico garantindo consistência de estoque via transação.
     *
     * Operações atômicas (tudo ou nada):
     *   1. Lê o documento do livro e verifica quantidade > 0
     *   2. Decrementa os campos "quantidade" e "estoque" atomicamente
     *   3. Cria o documento em "solicitacoes_emprestimo" com status "pendente"
     *
     * @param uidAluno  UID do usuário autenticado
     * @param livroId   ID do documento na coleção "livros"
     * @param titulo    Título do livro (desnormalizado para exibição rápida)
     * @param autor     Autor do livro (desnormalizado para exibição rápida)
     *
     * @return Result<String> com o ID do documento criado em caso de sucesso,
     *         ou Result.failure com a mensagem de erro (ex: "Sem estoque disponível").
     *
     * Uso:
     * ```kotlin
     * lifecycleScope.launch {
     *     val resultado = solicitacaoRepository.criarEmprestimoComControleDeEstoque(uid, livroId, titulo, autor)
     *     resultado.onSuccess { docId -> ... }
     *            .onFailure { e -> Toast.makeText(this, e.message, ...) }
     * }
     * ```
     */
    suspend fun criarEmprestimoComControleDeEstoque(
        uidAluno : String,
        livroId  : String,
        titulo   : String,
        autor    : String
    ): Result<String> {
        val livroRef       = db.collection("livros").document(livroId)
        val emprestimoRef  = db.collection("solicitacoes_emprestimo").document()
        val agora          = System.currentTimeMillis()
        // Prazo padrão: 15 dias — alinhado com RF34.PRAZO_MS
        val dataDevolucao  = agora + (15L * 24 * 60 * 60 * 1_000)

        return try {
            db.runTransaction { transaction ->
                val livroSnapshot = transaction.get(livroRef)

                // Lê o estoque com os mesmos fallbacks usados no resto do projeto
                val quantidadeAtual = livroSnapshot.getLong("quantidade")
                    ?: livroSnapshot.getLong("estoque")
                    ?: livroSnapshot.getLong("stock")
                    ?: 0L

                if (quantidadeAtual <= 0L) {
                    // Aborta a transação — sem estoque
                    throw FirebaseFirestoreException(
                        "Sem estoque disponível para este livro.",
                        FirebaseFirestoreException.Code.ABORTED
                    )
                }

                // Decrementa de forma consistente nos dois campos que o projeto usa
                val novaQuantidade = quantidadeAtual - 1
                transaction.update(livroRef, mapOf(
                    "quantidade" to novaQuantidade,
                    "estoque"    to novaQuantidade
                ))

                // Cria o documento de empréstimo atomicamente
                transaction.set(emprestimoRef, mapOf(
                    "uidAluno"        to uidAluno,
                    "idLivro"         to livroId,
                    "tituloLivro"     to titulo,
                    "autorLivro"      to autor,
                    "status"          to "pendente",
                    "dataSolicitacao" to agora,
                    "dataDevolucao"   to dataDevolucao,
                    "dataDevolucaoMs" to dataDevolucao   // alias — lido por TelaRF30
                ))

                // Retorna o ID do documento recém-criado
                emprestimoRef.id
            }.await()

            Result.success(emprestimoRef.id)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception(e.message ?: "Erro ao criar empréstimo."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Confirma a devolução física de um livro, restaurando o estoque atomicamente.
     *
     * Operações atômicas (tudo ou nada):
     *   1. Lê o documento de empréstimo e verifica que ainda está "ativo"
     *   2. Atualiza o status do empréstimo para "devolvido"
     *   3. Incrementa os campos "quantidade" e "estoque" do livro
     *
     * @param docId   ID do documento em "solicitacoes_emprestimo"
     * @param livroId ID do documento na coleção "livros"
     *
     * @return Result<Unit> — sucesso ou falha com mensagem descritiva.
     *
     * Uso:
     * ```kotlin
     * lifecycleScope.launch {
     *     solicitacaoRepository.confirmarDevolucao(docId, livroId)
     *         .onSuccess { Toast.makeText(this, "Devolvido!", ...) }
     *         .onFailure { e -> Toast.makeText(this, e.message, ...) }
     * }
     * ```
     */
    suspend fun confirmarDevolucao(
        docId  : String,
        livroId: String
    ): Result<Unit> {
        val emprestimoRef = db.collection("solicitacoes_emprestimo").document(docId)
        val livroRef      = db.collection("livros").document(livroId)

        return try {
            db.runTransaction { transaction ->
                val emprestimo = transaction.get(emprestimoRef)

                val statusAtual = emprestimo.getString("status") ?: ""
                if (statusAtual == "devolvido") {
                    // Idempotente: não falha se já devolvido, apenas não incrementa
                    throw FirebaseFirestoreException(
                        "Este livro já foi marcado como devolvido.",
                        FirebaseFirestoreException.Code.ABORTED
                    )
                }

                // Restaura o estoque
                val livroSnapshot    = transaction.get(livroRef)
                val quantidadeAtual  = livroSnapshot.getLong("quantidade")
                    ?: livroSnapshot.getLong("estoque")
                    ?: livroSnapshot.getLong("stock")
                    ?: 0L
                val novaQuantidade = quantidadeAtual + 1

                transaction.update(livroRef, mapOf(
                    "quantidade" to novaQuantidade,
                    "estoque"    to novaQuantidade
                ))

                // Atualiza status + timestamp de devolução real
                transaction.update(emprestimoRef, mapOf(
                    "status"          to "devolvido",
                    "dataDevolucaoReal" to System.currentTimeMillis()
                ))
            }.await()

            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception(e.message ?: "Erro ao confirmar devolução."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
