package com.example.bibliounifornew.usuario

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF15MinhaLivrariaActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var adapter: LivrariaAdapter
    private val listaLivraria = mutableListOf<ItemLivraria>()
    private var usuarioId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf15_minha_livraria)

        // ─── AUTENTICAÇÃO E SESSÃO SEGURA ─────────────────────────────────────
        val usuarioAtual = authRepository.getUsuarioAtual()
        if (usuarioAtual != null) {
            usuarioId = usuarioAtual.uid
            findViewById<TextView>(R.id.textEmailLivraria)?.text = usuarioAtual.email
        } else {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        // ─── BARRA DE NAVEGAÇÃO FIXA (Brena) ──────────────────────────────────
        // Configura a barra inferior unificada para evitar crashes de navegação
        try {
            com.example.bibliounifornew.util.NavigationHelper.configurarBarraNavegacao(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // ─── FILTRO ────────────────────────────────────────────────────────────
        findViewById<ImageView>(R.id.imgFiltroStatus)?.setOnClickListener {
            abrirPopupFiltro()
        }

        // ─── RECYCLERVIEW DINÂMICO ─────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewLivraria)
        adapter = LivrariaAdapter(listaLivraria) { item, position ->
            removerLivro(item, position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ─── CONSULTA FIRESTORE REAL ───────────────────────────────────────────
        carregarLivraria()
    }

    /**
     * Busca os livros do usuário na coleção biblioteca_usuarios no Firestore
     */
    private fun carregarLivraria() {
        db.collection("biblioteca_usuarios")
            .whereEqualTo("usuarioId", usuarioId)
            .get()
            .addOnSuccessListener { result ->
                listaLivraria.clear()
                for (doc in result) {
                    val livroId = doc.getString("livroId") ?: ""
                    val titulo = doc.getString("titulo") ?: "Título Indisponível"
                    val autor = doc.getString("autor") ?: "Autor Desconhecido"
                    val statusLeitura = doc.getString("statusLeitura") ?: "Não Lido"

                    // Instancia o objeto dinâmico do item
                    listaLivraria.add(ItemLivraria(livroId, titulo, autor, statusLeitura))
                }
                adapter.notifyDataSetChanged()

                if (listaLivraria.isEmpty()) {
                    Toast.makeText(this, "Sua livraria está vazia.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar livraria: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Remove o livro de biblioteca_usuarios e limpa o item da lista visual
     */
    private fun removerLivro(item: ItemLivraria, position: Int) {
        val docId = "${usuarioId}_${item.livroId}"
        db.collection("biblioteca_usuarios").document(docId)
            .delete()
            .addOnSuccessListener {
                adapter.removerItem(position)
                Toast.makeText(this, "\"${item.titulo}\" removido com sucesso.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Falha ao remover item do servidor.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun abrirPopupFiltro() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_filtrar_midia)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.findViewById<MaterialButton>(R.id.btnLimparFiltro)?.setOnClickListener { dialog.dismiss() }
        dialog.findViewById<MaterialButton>(R.id.btnSalvarFiltro)?.setOnClickListener {
            Toast.makeText(this, "Filtro aplicado.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}