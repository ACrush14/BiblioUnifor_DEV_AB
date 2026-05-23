package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.google.android.material.button.MaterialButton

class TelaRF18StatusAluguel : AppCompatActivity() {

    // 1. Instanciando os Repositórios
    private val authRepository = AuthRepository()
    private val usuarioRepository = UsuarioRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf18_status_aluguel)

        // ----------------------------------------------------
        // 2. ATUALIZANDO O CABEÇALHO (Tchau, João Bobo!)
        // ----------------------------------------------------
        val textNomeUsuario = findViewById<TextView>(R.id.textNomeUsuarioAlugados)
        val usuarioAtual = authRepository.getUsuarioAtual()

        if (usuarioAtual != null) {
            textNomeUsuario?.text = "Carregando..."

            usuarioRepository.buscarPerfilUsuario(usuarioAtual.uid) { sucesso, dados, erro ->
                if (sucesso && dados != null) {
                    val nomeBanco = dados["nome"] as? String ?: "Usuário"
                    textNomeUsuario?.text = nomeBanco
                } else {
                    Toast.makeText(this, "Erro ao carregar perfil: $erro", Toast.LENGTH_SHORT).show()
                    textNomeUsuario?.text = "Erro"
                }
            }
        } else {
            // Se a sessão caiu, manda fazer login novamente
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        // ----------------------------------------------------
        // 3. LÓGICA DOS BOTÕES DE RENOVAR
        // ----------------------------------------------------
        val btnRenovar1 = findViewById<MaterialButton>(R.id.btnRenovarLivro1)
        val btnRenovar2 = findViewById<MaterialButton>(R.id.btnRenovarLivro2)
        val btnRenovar3 = findViewById<MaterialButton>(R.id.btnRenovarLivro3)

        // Livro 1 (Dom Casmurro) - Disponível para renovação -> Abre o seu Calendário
        btnRenovar1?.setOnClickListener {
            val intent = Intent(this, TelaRF18CalendarioRenovacao::class.java)
            // Você pode até passar o nome do livro para o calendário depois usando putExtra
            startActivity(intent)
        }

        // Livro 2 (O Alienista) - Limite atingido (Botão Cinza no XML)
        btnRenovar2?.setOnClickListener {
            Toast.makeText(this, "Limite de renovação atingido para este livro.", Toast.LENGTH_SHORT).show()
        }

        // Livro 3 (Vidas Secas) - Prazo Expirado (Botão Cinza no XML)
        btnRenovar3?.setOnClickListener {
            Toast.makeText(this, "Prazo expirado. Por favor, devolva o livro na biblioteca.", Toast.LENGTH_LONG).show()
        }
    }
}