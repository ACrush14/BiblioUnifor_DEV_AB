package com.example.bibliounifornew.data

/**
 * Modelo de dados para uma notificação do usuário.
 *
 * Subcoleção Firestore: usuarios/{uid}/notificacoes
 * Documento: gerado automaticamente pelo ADM ao aprovar um livro/solicitação.
 *
 * Campos gravados pelo ADM (ex: TelaRF31Solicitacoes.criarNotificacaoMidia):
 *   "titulo"   → título do livro ou assunto da notificação
 *   "mensagem" → corpo da notificação (mapeado para [descricao])
 *   "lida"     → Boolean — false quando criada
 *   "data"     → Long (epoch ms) — timestamp de criação
 *
 * Campos opcionais que o ADM pode enriquecer:
 *   "autor"    → autor do livro relacionado
 *   "coverUrl" → URL da capa do livro (carregada via Coil)
 *   "livroId"  → ID do livro para navegação direta
 */
data class Notificacao(
    val id       : String  = "",
    val titulo   : String  = "",
    val autor    : String  = "",
    val descricao: String  = "",  // mapeado de "mensagem" no Firestore
    val coverUrl : String  = "",  // substituiu capaResId (drawable estático)
    val livroId  : String  = "",  // permite navegar para TelaRF12 ao tocar
    val timestamp: Long    = 0L,  // epoch ms — usado para ordenar e formatar tempo
    var lida     : Boolean = false
) {
    companion object {
        /**
         * Reconstrói um [Notificacao] a partir de um documento Firestore.
         * Trata campos legados e protege contra a divergência de tipos de data
         * (Long vs com.google.firebase.Timestamp).
         */
        fun fromFirestore(docId: String, data: Map<String, Any?>): Notificacao {

            // Tratamento robusto para a data:
            // "data"      → campo padrão (docs antigos e RF31)
            // "timestamp" → alias legado
            // "criadoEm"  → campo gravado pelo ADM no GAP-3 (RF35)
            val rawDate = data["data"] ?: data["timestamp"] ?: data["criadoEm"]
            val timestampResolvido = when (rawDate) {
                is Long -> rawDate
                is com.google.firebase.Timestamp -> rawDate.toDate().time
                else -> 0L
            }

            return Notificacao(
                id        = docId,
                titulo    = data["titulo"]    as? String  ?: "",
                autor     = data["autor"]     as? String  ?: "",
                descricao = data["descricao"] as? String
                    ?: data["mensagem"]  as? String  ?: "",
                coverUrl  = data["coverUrl"]  as? String  ?: "",
                livroId   = data["livroId"]   as? String  ?: "",
                timestamp = timestampResolvido,
                lida      = data["lida"]      as? Boolean ?: false
            )
        }
    }
}
