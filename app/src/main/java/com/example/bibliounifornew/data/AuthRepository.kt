package com.example.bibliounifornew.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // ----------------------------------------------------
    // EMAIL E SENHA
    // ----------------------------------------------------
    fun getUsuarioAtual(): FirebaseUser? {
        return auth.currentUser
    }

    // REGISTRAR (Usado na Tela de Cadastro)
    fun registrarUsuario(email: String, senha: String, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                        // Desloga imediatamente para evitar redirecionamentos automáticos por outros listeners
                        auth.signOut()

                        if (emailTask.isSuccessful) {
                            onComplete(true, user?.uid)
                        } else {
                            onComplete(false, "Conta criada, mas falha ao enviar o e-mail de verificação.")
                        }
                    }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    // LOGIN REAL (Usado na Tela de Login)
    fun loginUsuario(email: String, senha: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, auth.currentUser?.uid)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }
}