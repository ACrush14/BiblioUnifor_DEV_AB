package com.example.bibliounifornew.usuario

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class TelaRF16ListaDesejosActivity : AppCompatActivity() {

    // 1. Instanciando os repositórios de Nuvem
    private val authRepository = AuthRepository()
    private val usuarioRepository = UsuarioRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf16_lista_desejos)

        // ----------------------------------------------------
        // 2. ATUALIZANDO O CABEÇALHO (Adeus, João Bobo!)
        // ----------------------------------------------------
        // ATENÇÃO: Confirme no seu XML telarf16_lista_desejos.xml se o ID do TextView do nome é 'textNomeUsuario'
        val textNomeUsuario = findViewById<TextView>(R.id.textNomeUsuarioDesejos)
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
            // Proteção de rota: Se não estiver logado, volta pro Login
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        // ----------------------------------------------------
        // 3. LÓGICA DOS LIVROS (Cards Estáticos)
        // ----------------------------------------------------
        // Configuração do Livro 1: Vidas Secas (ID: 1)
        configurarLivro(
            cardId = R.id.cardD1,
            btnSuaLivrariaId = R.id.btnSuaLivraria1,
            btnAlugarId = R.id.btnAlugar1,
            btnExcluirId = R.id.btnExcluir1,
            menuIconId = R.id.menuD1,
            livroId = 1,
            disponivel = true
        )

        // Configuração do Livro 2: O Ceifador (ID: 2)
        configurarLivro(
            cardId = R.id.cardD2,
            btnSuaLivrariaId = R.id.btnSuaLivraria2,
            btnAlugarId = R.id.btnAlugar2,
            btnExcluirId = R.id.btnExcluir2,
            menuIconId = R.id.menuD2,
            livroId = 2,
            disponivel = false
        )

        // Configurar Barra de Navegação
        NavigationHelper.configurarBarraNavegacao(this)
    }

    private fun configurarLivro(
        cardId: Int,
        btnSuaLivrariaId: Int,
        btnAlugarId: Int,
        btnExcluirId: Int,
        menuIconId: Int,
        livroId: Int,
        disponivel: Boolean
    ) {
        val card = findViewById<MaterialCardView>(cardId)
        val btnSuaLivraria = findViewById<MaterialButton>(btnSuaLivrariaId)
        val btnAlugar = findViewById<MaterialButton>(btnAlugarId)
        val btnExcluir = findViewById<MaterialButton>(btnExcluirId)
        val menuIcon = findViewById<View>(menuIconId)

        // 1) BOTÃO "Minha Livraria"
        btnSuaLivraria.setOnClickListener {
            Toast.makeText(this, "Livro adicionado à sua livraria", Toast.LENGTH_SHORT).show()
        }

        // 2) BOTÃO "Alugar Livro"
        btnAlugar.setOnClickListener {
            if (disponivel) {
                showPopupAlugar(livroId)
            } else {
                Toast.makeText(this, "Livro indisponível no momento", Toast.LENGTH_SHORT).show()
            }
        }

        // 3) BOTÃO EXCLUIR
        btnExcluir.setOnClickListener {
            card.visibility = View.GONE
            Toast.makeText(this, "Livro removido da lista de desejos", Toast.LENGTH_SHORT).show()
        }

        // 4) CLIQUE NOS 3 PONTINHOS
        menuIcon.setOnClickListener {
            val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            intent.putExtra("LIVRO_ID", livroId.toString())
            startActivity(intent)
        }
    }

    private fun showPopupAlugar(livroId: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_alugar_livro)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnAlugar = dialog.findViewById<MaterialButton>(R.id.buttonAdicionarLivro)
        val btnCancelar = dialog.findViewById<TextView>(R.id.textCancelarPopup)

        btnAlugar.setOnClickListener {
            // TODO: Integrar com Banco de Dados para salvar o aluguel do livroId
            dialog.dismiss()
            showPopupLivroAdicionado()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showPopupLivroAdicionado() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_livro_adicionado)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnVerMeusLivros = dialog.findViewById<MaterialButton>(R.id.buttonVerMeusLivros)

        btnVerMeusLivros.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, TelaRF18StatusAluguel::class.java)
            startActivity(intent)
        }

        dialog.show()
    }
}