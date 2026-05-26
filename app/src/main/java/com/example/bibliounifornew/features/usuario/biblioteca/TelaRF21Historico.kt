package com.example.bibliounifornew.features.usuario.biblioteca

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TelaRF21Historico : AppCompatActivity() {

    private val authRepository    = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private val db                = FirebaseFirestore.getInstance()

    private lateinit var adapter  : HistoricoAdapter
    private val listaHistorico    = mutableListOf<ItemHistorico>()
    private var usuarioId         : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf21_historico)

        // ─── AUTENTICAÇÃO E SESSÃO SEGURA ─────────────────────────────────────
        val textCabecalho = findViewById<TextView>(R.id.textEmailHistorico)
        val usuarioAtual  = authRepository.getUsuarioAtual()

        if (usuarioAtual != null) {
            usuarioId          = usuarioAtual.uid
            textCabecalho?.text = usuarioAtual.email
        } else {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        // ─── BARRA DE NAVEGAÇÃO FIXA ──────────────────────────────────────────
        try {
            // Referência direta ao objeto NavigationHelper no mesmo pacote
            NavigationHelper.configurarBarraNavegacao(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // ─── RECYCLERVIEW DINÂMICO ─────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewHistorico)
        adapter = HistoricoAdapter(listaHistorico) { item, position ->
            showPopupRemover(item.titulo) {
                // Remove fisicamente do Firestore usando o repositório integrado
                usuarioRepository.removerDoHistorico(usuarioId, item.livroId) { sucesso ->
                    if (sucesso) {
                        adapter.removerItem(position)
                        Toast.makeText(this, "Item removido do seu histórico.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Falha ao remover. Tente novamente.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ─── CONSULTA FIRESTORE REAL ───────────────────────────────────────────
        carregarHistorico()
    }

    /**
     * Carrega os dados reais do histórico ordenados por data de adição
     */
    private fun carregarHistorico() {
        db.collection("historico_usuarios")
            .whereEqualTo("usuarioId", usuarioId)
            // .orderBy("adicionadoEm", Query.Direction.DESCENDING) // Comentado para evitar erro de índice ausente no Firestore
            .get()
            .addOnSuccessListener { result ->
                listaHistorico.clear()
                val itens = result.documents.mapNotNull { document ->
                    val livroId  = document.getString("livroId") ?: ""
                    val titulo   = document.getString("titulo") ?: "Título Indisponível"
                    val autor    = document.getString("autor") ?: "Autor Desconhecido"
                    val acao     = document.getString("acao") ?: "Adicionado"
                    val dataLido = document.getLong("adicionadoEm") ?: 0L
                    ItemHistorico(livroId, titulo, autor, acao, dataLido)
                }.sortedByDescending { it.dataLido }
                
                listaHistorico.addAll(itens)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar histórico.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPopupRemover(nomeLivro: String, onConfirm: () -> Unit) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_remover_historico)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val textMensagem = dialog.findViewById<TextView>(R.id.textMensagemPopupRemoverHistorico)
        val btnConfirmar = dialog.findViewById<Button>(R.id.buttonPopupRemoverHistorico)
        val btnCancelar  = dialog.findViewById<TextView>(R.id.textCancelarPopupRemoverHistorico)

        textMensagem?.text = "Tem certeza que deseja remover \"$nomeLivro\" do seu histórico?"

        btnConfirmar?.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        btnCancelar?.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
