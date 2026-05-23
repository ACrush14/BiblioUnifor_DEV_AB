package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository

class TelaRF17Amigos : AppCompatActivity() {

    // 1. Instanciando o Repositório de Autenticação
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf17_amigos)

        // ----------------------------------------------------
        // 2. VERIFICAÇÃO DE SEGURANÇA (Sessão Ativa)
        // ----------------------------------------------------
        val usuarioAtual = authRepository.getUsuarioAtual()
        if (usuarioAtual == null) {
            // Rota Segura: Sem usuário logado, manda de volta pro Login.
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        // ----------------------------------------------------
        // 3. LÓGICA DO BOTÃO "ADICIONAR AMIGOS"
        // ----------------------------------------------------
        val layoutAdicionarAmigos = findViewById<View>(R.id.layoutAdicionarAmigos)
        layoutAdicionarAmigos.setOnClickListener {
            val intent = Intent(this, TelaRF17_3_BuscaAmigos::class.java)
            startActivity(intent)
        }

        // ----------------------------------------------------
        // 4. LÓGICA DA LISTA DE AMIGOS (Cards Estáticos)
        // ----------------------------------------------------
        // No futuro da aplicação, assim como os livros, os amigos aqui listados
        // sairão do XML fixo e virão de um RecyclerView consultando o Firestore.
        configurarCliquePerfil(R.id.fotoAmigo1, R.id.nomeAmigo1, R.id.menuAmigo1)
        configurarCliquePerfil(R.id.fotoAmigo2, R.id.nomeAmigo2, R.id.menuAmigo2)
        configurarCliquePerfil(R.id.fotoAmigo3, R.id.nomeAmigo3, R.id.menuAmigo3)
        configurarCliquePerfil(R.id.fotoAmigo4, R.id.nomeAmigo4, R.id.menuAmigo4)
        configurarCliquePerfil(R.id.fotoAmigo5, R.id.nomeAmigo5, R.id.menuAmigo5)
    }

    // ----------------------------------------------------
    // MÉTODOS DE APOIO
    // ----------------------------------------------------

    private fun configurarCliquePerfil(fotoId: Int, nomeId: Int, menuId: Int) {
        val clickListener = View.OnClickListener {
            val intent = Intent(this, TelaRF17_5_PerfilAmigo::class.java)
            startActivity(intent)
        }

        findViewById<View>(fotoId).setOnClickListener(clickListener)
        findViewById<View>(nomeId).setOnClickListener(clickListener)
        findViewById<View>(menuId).setOnClickListener(clickListener)
    }
}