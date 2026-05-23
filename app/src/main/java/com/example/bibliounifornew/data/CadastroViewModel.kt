package com.example.bibliounifornew.data

import androidx.lifecycle.ViewModel

class CadastroViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val usuarioRepository = UsuarioRepository() // Instanciamos o novo repositório

    fun cadastrarUsuario(
        email: String,
        senha: String,
        nome: String,
        usuario: String,
        onResultado: (Boolean, String?) -> Unit
    ) {
        // 1. Cria a credencial de login
        authRepository.registrarUsuario(email, senha) { sucessoAuth, resultadoAuth ->

            if (sucessoAuth && resultadoAuth != null) {
                // Se o sucesso for true, o resultadoAuth contém o UID gerado
                val uid = resultadoAuth

                // 2. Salva o perfil no Firestore
                usuarioRepository.salvarUsuarioFirestore(uid, nome, usuario, email) { sucessoDb, erroDb ->
                    if (sucessoDb) {
                        onResultado(true, "Conta criada com sucesso!")
                    } else {
                        onResultado(false, "Conta criada, mas falha ao salvar perfil: $erroDb")
                    }
                }
            } else {
                // Falha ao criar a conta (ex: senha fraca, email já existe)
                onResultado(false, resultadoAuth)
            }
        }
    }
}