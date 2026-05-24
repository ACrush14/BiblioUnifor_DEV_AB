package com.example.bibliounifornew.usuario

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class TelaRF16ListaDesejosActivity : AppCompatActivity() {

    private val authRepository    = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private var usuarioId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf16_lista_desejos)

        // ─── CABEÇALHO ────────────────────────────────────────────────────────
        val textNomeUsuario = findViewById<TextView>(R.id.textNomeUsuarioDesejos)
        val usuarioAtual    = authRepository.getUsuarioAtual()

        if (usuarioAtual != null) {
            usuarioId = usuarioAtual.uid
            textNomeUsuario?.text = "Carregando..."

            usuarioRepository.buscarPerfilUsuario(usuarioAtual.uid) { sucesso, dados, erro ->
                if (sucesso && dados != null) {
                    textNomeUsuario?.text = dados["nome"] as? String ?: "Usuário"
                } else {
                    textNomeUsuario?.text = "Usuário"
                }
            }
        } else {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        // ─── CONFIGURAR LIVROS ────────────────────────────────────────────────
        // Livro 1: Vidas Secas — disponível
        configurarLivro(
            cardId         = R.id.cardD1,
            btnSuaLivrariaId = R.id.btnSuaLivraria1,
            btnAlugarId    = R.id.btnAlugar1,
            btnExcluirId   = R.id.btnExcluir1,
            menuIconId     = R.id.menuD1,
            livroId        = "vidas_secas",
            titulo         = "Vidas Secas",
            autor          = "Graciliano Ramos",
            disponivel     = true
        )

        // Livro 2: O Ceifador — indisponível
        configurarLivro(
            cardId         = R.id.cardD2,
            btnSuaLivrariaId = R.id.btnSuaLivraria2,
            btnAlugarId    = R.id.btnAlugar2,
            btnExcluirId   = R.id.btnExcluir2,
            menuIconId     = R.id.menuD2,
            livroId        = "o_ceifador",
            titulo         = "O Ceifador",
            autor          = "Neal Shusterman",
            disponivel     = false
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
        livroId: String,
        titulo: String,
        autor: String,
        disponivel: Boolean
    ) {
        val card          = findViewById<MaterialCardView>(cardId)
        val btnSuaLivraria = findViewById<MaterialButton>(btnSuaLivrariaId)
        val btnAlugar     = findViewById<MaterialButton>(btnAlugarId)
        val btnExcluir    = findViewById<MaterialButton>(btnExcluirId)
        val menuIcon      = findViewById<View>(menuIconId)

        // 1) "Minha Livraria" → salva na collection biblioteca_usuarios
        btnSuaLivraria?.setOnClickListener {
            val dados = hashMapOf(
                "usuarioId" to usuarioId,
                "livroId"   to livroId,
                "titulo"    to titulo,
                "autor"     to autor,
                "origem"    to "lista_desejos",
                "adicionadoEm" to System.currentTimeMillis()
            )
            usuarioRepository.salvarListaDesejos(usuarioId, livroId, dados) { sucesso, _ ->
                val msg = if (sucesso) "\"$titulo\" adicionado à sua livraria!"
                          else "Erro ao salvar. Tente novamente."
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }

        // 2) "Alugar" → popup de aluguel (lógica existente)
        btnAlugar?.setOnClickListener {
            if (disponivel) showPopupAlugar(livroId)
            else Toast.makeText(this, "\"$titulo\" está indisponível no momento.", Toast.LENGTH_SHORT).show()
        }

        // 3) "Excluir" → remove do Firestore E esconde o card
        btnExcluir?.setOnClickListener {
            usuarioRepository.removerDaListaDesejos(usuarioId, livroId) { sucesso ->
                card?.visibility = View.GONE
                val msg = if (sucesso) "\"$titulo\" removido da lista de desejos."
                          else "Removido localmente (falha no servidor)."
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }

        // 4) Menu (3 pontos) → abre detalhes do livro
        menuIcon?.setOnClickListener {
            val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            intent.putExtra("LIVRO_ID", livroId)
            startActivity(intent)
        }
    }

    private fun showPopupAlugar(livroId: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_alugar_livro)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnAlugar  = dialog.findViewById<MaterialButton>(R.id.buttonAdicionarLivro)
        val btnCancelar = dialog.findViewById<TextView>(R.id.textCancelarPopup)

        btnAlugar?.setOnClickListener {
            dialog.dismiss()
            showPopupLivroAdicionado()
        }
        btnCancelar?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showPopupLivroAdicionado() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_livro_adicionado)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnVerMeusLivros = dialog.findViewById<MaterialButton>(R.id.buttonVerMeusLivros)
        btnVerMeusLivros?.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, TelaRF18StatusAluguel::class.java))
        }
        dialog.show()
    }
}
