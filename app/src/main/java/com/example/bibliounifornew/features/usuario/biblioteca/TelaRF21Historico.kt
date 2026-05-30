package com.example.bibliounifornew.features.usuario.biblioteca

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
        // Adapter usa lista própria — listaHistorico é a fonte de verdade para o filtro
        adapter = HistoricoAdapter(mutableListOf()) { item, position ->
            showPopupRemover(item.titulo) {
                usuarioRepository.removerDoHistorico(usuarioId, item.livroId) { sucesso ->
                    if (sucesso) {
                        adapter.removerItem(position)
                        // Mantém listaHistorico sincronizada para que o filtro não reexiba o item
                        listaHistorico.removeAll { it.livroId == item.livroId }
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

        // ─── FILTRO EM MEMÓRIA (RF21.01) ──────────────────────────────────────
        // listaHistorico é a fonte de verdade; adapter exibe uma cópia filtrada.
        val tvVazioRef = lazy { findViewById<TextView>(R.id.tvHistoricoVazio) }
        findViewById<EditText>(R.id.editPesquisaHistorico)
            ?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val termo = s?.toString()?.trim() ?: ""
                    val filtrados = if (termo.isEmpty()) {
                        listaHistorico.toList()
                    } else {
                        listaHistorico.filter {
                            it.titulo.contains(termo, ignoreCase = true) ||
                            it.autor.contains(termo, ignoreCase = true)
                        }
                    }
                    adapter.atualizarLista(filtrados)
                    tvVazioRef.value?.visibility =
                        if (filtrados.isEmpty() && listaHistorico.isNotEmpty()) View.VISIBLE
                        else View.GONE
                }
            })
    }

    /**
     * PERF-2: Carrega o histórico sem N+1 — todos os campos (incluindo coverUrl)
     * são lidos diretamente de cada documento da coleção historico_usuarios.
     * UsuarioRepository.registrarNoHistorico() já grava coverUrl no documento,
     * portanto nenhum join adicional é necessário.
     */
    private fun carregarHistorico() {
        val tvVazio      = findViewById<TextView>(R.id.tvHistoricoVazio)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewHistorico)

        Log.d("DEBUG_HISTORICO", "UID logado: $usuarioId")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection("historico_usuarios")
                    .whereEqualTo("usuarioId", usuarioId)
                    .get()
                    .await()

                Log.d("DEBUG_HISTORICO", "Documentos retornados: ${result.size()}")

                val itens = result.documents.mapNotNull { doc ->
                    val lId = doc.getString("livroId")
                    val uId = doc.getString("usuarioId")
                    val tit = doc.getString("titulo")
                    Log.d("DEBUG_HISTORICO", "Doc: livroId=$lId, usuarioId=$uId, titulo=$tit")

                    val livroId  = doc.getString("livroId")  ?: return@mapNotNull null
                    val titulo   = doc.getString("titulo")   ?: getString(R.string.sem_titulo)
                    val autor    = doc.getString("autor")    ?: getString(R.string.sem_autor)
                    val acao     = doc.getString("acao")     ?: "Adicionado"
                    val dataLido = doc.getLong("adicionadoEm") ?: 0L
                    val coverUrl = doc.getString("coverUrl") ?: ""
                    ItemHistorico(livroId, titulo, autor, acao, dataLido, coverUrl)
                }.sortedByDescending { it.dataLido }

                Log.d("DEBUG_HISTORICO", "Itens finais após mapNotNull: ${itens.size}")

                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    listaHistorico.clear()
                    listaHistorico.addAll(itens)
                    adapter.atualizarLista(itens)
                    val vazio = itens.isEmpty()
                    tvVazio?.visibility      = if (vazio) View.VISIBLE else View.GONE
                    recyclerView?.visibility = if (vazio) View.GONE   else View.VISIBLE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    Toast.makeText(this@TelaRF21Historico, "Erro ao carregar histórico.", Toast.LENGTH_SHORT).show()
                }
            }
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
