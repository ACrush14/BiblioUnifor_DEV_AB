package com.example.bibliounifornew.features.usuario.solicitacao

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TelaRF18StatusAluguel : AppCompatActivity() {

    private val authRepository    = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private val db                = FirebaseFirestore.getInstance()

    private lateinit var adapter       : StatusAluguelAdapter
    private lateinit var recyclerView  : RecyclerView
    private var tvNenhumAluguel        : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf18_status_aluguel)

        // ─── RECYCLER VIEW ────────────────────────────────────────────────────
        recyclerView = findViewById(R.id.recyclerStatusAluguel)
        tvNenhumAluguel = findViewById(R.id.tvNenhumAluguel)

        adapter = StatusAluguelAdapter(
            lista     = mutableListOf(),
            onRenovar = { item -> renovarAluguel(item) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ─── CABEÇALHO ────────────────────────────────────────────────────────
        val textNome    = findViewById<TextView>(R.id.textNomeUsuarioAlugados)
        val imagePerfil = findViewById<ImageView?>(R.id.imagePerfilAlugados)
        val usuarioAtual = authRepository.getUsuarioAtual()

        if (usuarioAtual != null) {
            textNome?.text = getString(R.string.placeholder_usuario)

            usuarioRepository.buscarPerfilUsuario(usuarioAtual.uid) { sucesso, dados, _ ->
                if (!isFinishing && !isDestroyed) {
                    if (sucesso && dados != null) {
                        textNome?.text = dados["nome"] as? String
                            ?: getString(R.string.placeholder_usuario)
                        val fotoUrl = dados["fotoUrl"] as? String ?: ""
                        if (fotoUrl.isNotEmpty()) {
                            imagePerfil?.load(fotoUrl) {
                                placeholder(R.drawable.user_placeholder)
                                error(R.drawable.user_placeholder)
                            }
                        }
                    } else {
                        textNome?.text = getString(R.string.placeholder_usuario)
                    }
                }
            }

            carregarAlugueis(usuarioAtual.uid)
        } else {
            startActivity(Intent(this,
                com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        NavigationHelper.configurarBarraNavegacao(this)
    }

    // ─── CARREGAR ALUGUÉIS — PERF-2 FIX: sem N+1 ─────────────────────────────
    //
    // Antes: fazia 1 query em "livros" por documento de empréstimo (N+1).
    // Agora: lê os campos desnormalizados "tituloLivro" e "coverUrl" diretamente
    //        do documento de "solicitacoes_emprestimo" — zero queries extras.
    //
    // Campos gravados por SolicitacaoRepository.criarEmprestimoComControleDeEstoque():
    //   "tituloLivro" — título para exibição rápida
    //   "autorLivro"  — autor  para exibição rápida
    //   (coverUrl é escrito na nova versão do repository)

    private fun carregarAlugueis(uid: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection("solicitacoes_emprestimo")
                    .whereEqualTo("uidAluno", uid)
                    .get()
                    .await()

                val lista = result.documents.mapNotNull { doc ->
                    val docId           = doc.id
                    val idLivro         = doc.getString("idLivro")
                        ?: doc.getString("livroId") ?: ""
                    val status          = doc.getString("status")        ?: "pendente"
                    val dataDevolucao   = doc.getLong("dataDevolucao")   ?: 0L
                    val dataSolicitacao = doc.getLong("dataSolicitacao") ?: 0L
                    val renovacoes      = (doc.getLong("renovacoes")     ?: 0L).toInt()

                    // Leitura desnormalizada — sem query adicional à coleção "livros"
                    val titulo     = doc.getString("tituloLivro")
                        ?: doc.getString("titulo") ?: ""
                    val autorLivro = doc.getString("autorLivro")
                        ?: doc.getString("autor")  ?: ""
                    val coverUrl   = doc.getString("coverUrl") ?: ""

                    ItemAluguel(
                        docId           = docId,
                        livroId         = idLivro,
                        titulo          = titulo,
                        autorLivro      = autorLivro,
                        coverUrl        = coverUrl,
                        status          = status,
                        dataDevolucao   = dataDevolucao,
                        dataSolicitacao = dataSolicitacao,
                        renovacoes      = renovacoes
                    )
                }.sortedByDescending { it.dataSolicitacao }

                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    val vazio = lista.isEmpty()
                    tvNenhumAluguel?.visibility = if (vazio) View.VISIBLE else View.GONE
                    recyclerView.visibility     = if (vazio) View.GONE   else View.VISIBLE
                    adapter.atualizarLista(lista)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    Toast.makeText(
                        this@TelaRF18StatusAluguel,
                        getString(R.string.erro_carregar_alugueis),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // ─── RENOVAR ALUGUEL (RF18.5) ─────────────────────────────────────────────
    //
    // Travas de negócio verificadas ANTES de tocar o Firestore:
    //   1. Prazo expirado  → bloqueia imediatamente na Main Thread (sem IO)
    //   2. Limite atingido → bloqueia imediatamente na Main Thread (sem IO)
    // Se passar nas travas: atualiza dataDevolucao (+14 dias) e renovacoes (+1) no IO Thread.

    private fun renovarAluguel(item: ItemAluguel) {
        val agora = System.currentTimeMillis()

        if (agora > item.dataDevolucao) {
            Toast.makeText(this, "Impossível renovar: Prazo de aluguel expirado.", Toast.LENGTH_SHORT).show()
            return
        }
        if (item.renovacoes >= 1) {
            Toast.makeText(this, "Limite de renovações atingido.", Toast.LENGTH_SHORT).show()
            return
        }

        val novaDataDevolucao = item.dataDevolucao + 14L * 24 * 60 * 60 * 1000

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.collection("solicitacoes_emprestimo")
                    .document(item.docId)
                    .update(mapOf(
                        "dataDevolucao" to novaDataDevolucao,
                        "renovacoes"    to item.renovacoes + 1
                    ))
                    .await()

                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    Toast.makeText(
                        this@TelaRF18StatusAluguel,
                        "Aluguel renovado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                    authRepository.getUsuarioAtual()?.uid?.let { carregarAlugueis(it) }
                }
            } catch (e: Exception) {
                Log.e("Renovar", "Erro ao renovar: ", e)
                withContext(Dispatchers.Main) {
                    if (!isFinishing && !isDestroyed) {
                        Toast.makeText(
                            this@TelaRF18StatusAluguel,
                            "Erro ao renovar aluguel.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}
