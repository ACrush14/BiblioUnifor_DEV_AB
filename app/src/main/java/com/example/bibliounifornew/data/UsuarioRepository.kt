package com.example.bibliounifornew.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

class UsuarioRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun salvarUsuarioFirestore(
        uid: String,
        nome: String,
        usuario: String,
        email: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        // Mapeando os dados para o padrão NoSQL do Firestore
        val userMap = hashMapOf(
            "uid" to uid,
            "nome" to nome,
            "usuario" to usuario,
            "email" to email,
            "tipoPerfil" to "estudante", // Define como estudante por padrão
            "statusCadastro" to "pendente"
        )

        // Salva na coleção "usuarios" usando o UID como nome do documento
        db.collection("usuarios").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    // Atualiza um campo específico do perfil no banco de dados
    fun atualizarCampoPerfil(uid: String, campo: String, valor: String, onComplete: (Boolean, String?) -> Unit) {
        db.collection("usuarios").document(uid)
            .update(campo, valor)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    fun buscarPerfilUsuario(uid: String, onComplete: (Boolean, Map<String, Any>?, String?) -> Unit) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    onComplete(true, document.data, null)
                } else {
                    onComplete(false, null, "Usuário não encontrado no banco de dados.")
                }
            }
            .addOnFailureListener { exception ->
                onComplete(false, null, exception.message)
            }
    }

    /**
     * Observa o perfil do usuário em tempo real com SnapshotListener.
     * Retorna um ListenerRegistration que DEVE ser cancelado em onDestroy() da Activity/Fragment.
     * Uso: snapshotListener = usuarioRepository.observarPerfilUsuario(uid) { dados -> ... }
     *      snapshotListener?.remove() // no onDestroy
     */
    fun observarPerfilUsuario(uid: String, onChange: (Map<String, Any>?) -> Unit): ListenerRegistration {
        return db.collection("usuarios").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    onChange(null)
                    return@addSnapshotListener
                }
                onChange(snapshot.data)
            }
    }

    /**
     * Salva múltiplos campos do perfil de uma só vez com merge,
     * preservando campos que não foram alterados.
     */
    fun salvarPerfilCompleto(
        uid: String,
        campos: Map<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        db.collection("usuarios").document(uid)
            .set(campos, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }

    /**
     * Remove um item do histórico do usuário no Firestore.
     * Busca por usuarioId e livroId e deleta todos os registros correspondentes.
     */
    fun removerDoHistorico(uid: String, livroId: String, onComplete: (Boolean) -> Unit) {
        db.collection("historico_usuarios")
            .whereEqualTo("usuarioId", uid)
            .whereEqualTo("livroId", livroId)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Registra uma ação (adição/remoção) no histórico do usuário.
     * RF15.8: Manter registro de livros adicionados ou removidos.
     */
    fun registrarNoHistorico(
        uid: String,
        livroId: String,
        titulo: String,
        autor: String,
        acao: String, // "Adicionado", "Removido" ou "Solicitado"
        coverUrl: String = "",
        onComplete: (Boolean) -> Unit = {}
    ) {
        val historicoData = hashMapOf(
            "usuarioId" to uid,
            "livroId" to livroId,
            "titulo" to titulo,
            "autor" to autor,
            "coverUrl" to coverUrl,
            "acao" to acao,
            "adicionadoEm" to System.currentTimeMillis()
        )
        
        // Usamos um ID gerado automaticamente para permitir múltiplos registros do mesmo livro (ex: add, remove, add)
        db.collection("historico_usuarios")
            .add(historicoData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Salva ou atualiza o item na lista de desejos no Firestore com segurança.
     * Padrão do ID do Documento: {uid}_{livroId} para evitar duplicidade.
     */
    fun salvarListaDesejos(
        uid: String,
        livroId: String,
        dados: Map<String, Any>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        if (uid.isBlank() || livroId.isBlank()) {
            onComplete(false, "ID de usuário ou livro inválido.")
            return
        }

        val documentoId = "${uid}_${livroId}"
        db.collection("lista_desejos").document(documentoId)
            .set(dados, SetOptions.merge())
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.localizedMessage) }
    }

    /**
     * Remove item da lista de desejos no Firestore.
     */
    fun removerDaListaDesejos(uid: String, livroId: String, onComplete: (Boolean) -> Unit) {
        val documentoId = "${uid}_${livroId}"
        db.collection("lista_desejos").document(documentoId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun verificarECriarUsuarioGoogle(
        uid: String,
        nome: String?,
        email: String?,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val docRef = db.collection("usuarios").document(uid)

        docRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                // É o primeiro acesso! Vamos criar o perfil dele no Firestore.
                val userMap = hashMapOf(
                    "uid" to uid,
                    "nome" to (nome ?: "Usuário Google"),
                    "email" to email,
                    "tipoPerfil" to "estudante",
                    "statusCadastro" to "aprovado" // Contas Google já são validadas nativamente
                )

                docRef.set(userMap)
                    .addOnSuccessListener { onComplete(true, null) }
                    .addOnFailureListener { e -> onComplete(false, e.message) }
            } else {
                // O usuário já existe no banco, apenas liberamos o login.
                onComplete(true, null)
            }
        }.addOnFailureListener { e ->
            onComplete(false, e.message)
        }
    }

    // ─── NOTIFICAÇÕES ─────────────────────────────────────────────────────────

    /**
     * Escuta a subcoleção de notificações do usuário em tempo real.
     *
     * Caminho Firestore: usuarios/{uid}/notificacoes
     * Ordenação: por "data" decrescente (mais recente primeiro).
     *
     * @param uid      UID do usuário logado (de FirebaseAuth.currentUser.uid)
     * @param onChange Chamado imediatamente com a lista atual e a cada nova
     *                 notificação criada pelo ADM.
     * @return [ListenerRegistration] — DEVE ser cancelado em onDestroy() da
     *         Activity/Fragment: `snapshotListener?.remove()`
     *
     * Exemplo de uso:
     *   snapshotListener = usuarioRepository.escutarNotificacoes(uid) { lista ->
     *       adapter.atualizarLista(lista)
     *   }
     */
    fun escutarNotificacoes(
        uid     : String,
        onChange: (List<Notificacao>) -> Unit
    ): ListenerRegistration {
        return db.collection("usuarios")
            .document(uid)
            .collection("notificacoes")
            .orderBy("data", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onChange(emptyList())
                    return@addSnapshotListener
                }
                val lista = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { Notificacao.fromFirestore(doc.id, it) }
                }
                onChange(lista)
            }
    }

    /**
     * Upload de foto de perfil (Storage) + Atualização do campo fotoUrl (Firestore).
     * RF08.4: Consolidado para uso no Dashboard e Configurações.
     */
    fun uploadFotoPerfil(
        uid: String,
        imageBytes: ByteArray,
        colecao: String = "usuarios",
        onComplete: (Boolean, String?, String?) -> Unit
    ) {
        val storageRef = storage.reference.child("profile_images/$uid.jpg")

        storageRef.putBytes(imageBytes)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception!!
                storageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                val url = downloadUri.toString()
                db.collection(colecao).document(uid)
                    .update("fotoUrl", url)
                    .addOnSuccessListener {
                        onComplete(true, url, null)
                    }
                    .addOnFailureListener { e ->
                        onComplete(false, null, e.message)
                    }
            }
            .addOnFailureListener { e ->
                onComplete(false, null, e.message)
            }
    }
}