package com.example.bibliounifornew.data

import com.google.firebase.firestore.FirebaseFirestore

class UsuarioRepository {

    private val db = FirebaseFirestore.getInstance()

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
}