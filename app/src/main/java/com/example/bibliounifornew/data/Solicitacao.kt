package com.example.bibliounifornew.data

/**
 * Modelo de uma solicitação de mídia gravada no Firestore.
 *
 * Coleção: "solicitacoes_midia"
 * Documento: gerado automaticamente por .add()
 *
 * Compatibilidade com TelaRF31Solicitacoes (ADM):
 *   - [uidUsuario] / [uidAluno] → lidos por `doc.getString("uidUsuario") ?: doc.getString("uidAluno")`
 *   - [idLivro]   → lido por `doc.getString("idLivro")`
 *   - [tipos]     → lido por `doc.getString("tipos")`
 *   - [status]    → filtrado com `whereEqualTo("status", "pendente")`
 *   - [dataSolicitacao] → campo de ordenação / timestamp
 *
 * Tipos válidos para [tipos]: "PDF" | "Braille" | "Audiobook" | "Reserva"
 */
data class Solicitacao(
    val id              : String = "",
    val uidUsuario      : String = "",
    val uidAluno        : String = "",  // alias para compatibilidade legada
    val idLivro         : String = "",
    val tipos           : String = "",  // "PDF" | "Braille" | "Audiobook" | "Reserva"
    val status          : String = "pendente",
    val dataSolicitacao : Long   = System.currentTimeMillis()
) {
    /**
     * Converte para Map<String, Any> pronto para gravar no Firestore.
     * Todos os campos são escritos para garantir compatibilidade total
     * com as leituras do ADM em TelaRF31Solicitacoes.
     * O ID não é enviado no corpo do map pois é a chave do documento.
     */
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "uidUsuario"       to uidUsuario,
        "uidAluno"         to uidAluno,
        "idLivro"          to idLivro,
        "tipos"            to tipos,
        "status"           to status,
        "dataSolicitacao"  to dataSolicitacao
    )

    companion object {
        /**
         * Reconstrói um [Solicitacao] a partir de um documento Firestore.
         * Útil para leituras do lado do ADM ou histórico do usuário.
         */
        fun fromFirestore(docId: String, data: Map<String, Any?>): Solicitacao {
            return Solicitacao(
                id              = docId,
                uidUsuario      = data["uidUsuario"]      as? String ?: "",
                uidAluno        = data["uidAluno"]        as? String ?: "",
                idLivro         = data["idLivro"]         as? String ?: "",
                tipos           = data["tipos"]           as? String ?: "",
                status          = data["status"]          as? String ?: "pendente",
                dataSolicitacao = data["dataSolicitacao"] as? Long   ?: 0L
            )
        }
    }
}
