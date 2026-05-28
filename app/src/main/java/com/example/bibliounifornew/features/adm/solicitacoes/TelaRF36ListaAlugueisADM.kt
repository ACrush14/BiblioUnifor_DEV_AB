package com.example.bibliounifornew.features.adm.solicitacoes

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
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.gerenciamento.NavigationHelperADM
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF30UsuariosParaADM
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF37InfoLivroADM
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TelaRF36ListaAlugueisADM : AppCompatActivity() {

    private val db            = FirebaseFirestore.getInstance()
    private lateinit var adapter: AlugueisAdapter
    private val listaAlugueis = mutableListOf<ItemAluguel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf36_lista_alugueis_adm)

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAlugueis)
        adapter = AlugueisAdapter(
            listaAlugueis,
            onVerLivro    = { item ->
                val intent = Intent(this, TelaRF37InfoLivroADM::class.java)
                intent.putExtra("LIVRO_ID", item.idLivro)
                startActivity(intent)
            },
            onVerUsuario  = { item ->
                val intent = Intent(this, TelaRF30UsuariosParaADM::class.java)
                intent.putExtra("USUARIO_ID", item.uidAluno)
                startActivity(intent)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        carregarAlugueis()

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    /**
     * GAP-4 / PERF-2 FIX — carrega aluguéis ativos sem N+1 queries.
     *
     * Estratégia de desnormalização:
     *   Lê nomeAluno/tituloLivro/autorLivro diretamente do documento de empréstimo.
     *   Novos docs criados por SolicitacaoRepository.criarEmprestimoComControleDeEstoque()
     *   já incluem tituloLivro e autorLivro. Docs antigos recebem fallback seguro.
     *   Zero joins adicionais por item → custo fixo de 1 query total.
     *
     * FAILED_PRECONDITION:
     *   Se o Firestore exigir um índice composto (ex: ao adicionar .orderBy()),
     *   o erro é capturado e logado com o link direto de criação de índice.
     *
     * Estado vazio:
     *   tvListaVazia é exibido quando nenhuma coleção retorna resultados.
     */
    private fun carregarAlugueis() {
        val tvVazia = findViewById<TextView>(R.id.tvListaVazia)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection("solicitacoes_emprestimo")
                    .whereIn("status", listOf("pendente", "ativo", "atrasado"))
                    .get()
                    .await()

                if (result.isEmpty) {
                    // Fallback: tenta coleção legada "alugueis"
                    val resultAlt = db.collection("alugueis").get().await()
                    val lista = mapearDocumentos(resultAlt.documents)
                    withContext(Dispatchers.Main) {
                        if (isFinishing || isDestroyed) return@withContext
                        if (lista.isEmpty()) {
                            tvVazia?.visibility = View.VISIBLE
                            adapter.atualizarLista(emptyList())
                        } else {
                            tvVazia?.visibility = View.GONE
                            adapter.atualizarLista(lista)
                        }
                    }
                    return@launch
                }

                val lista = mapearDocumentos(result.documents)
                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    tvVazia?.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                    adapter.atualizarLista(lista)
                }

            } catch (e: Exception) {
                // FAILED_PRECONDITION → índice composto ausente no Firestore
                val msg = e.message ?: ""
                if (msg.contains("FAILED_PRECONDITION", ignoreCase = true)) {
                    Log.e(
                        "RF36_INDEX",
                        "Índice composto ausente. Crie-o em:\n" +
                        "https://console.firebase.google.com → Firestore → Indexes\n" +
                        "Campos: status (ASC) + dataSolicitacao (DESC)\n" +
                        "Erro original: $msg"
                    )
                } else {
                    Log.e("RF36", "Erro ao carregar aluguéis: $msg")
                }
                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    tvVazia?.visibility = View.VISIBLE
                    Toast.makeText(
                        this@TelaRF36ListaAlugueisADM,
                        getString(R.string.erro_carregar_alugueis),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * PERF-2: Mapeia documentos Firestore para [ItemAluguel] SEM N+1 queries.
     *
     * Prioridade de leitura para cada campo:
     *   nomeUsuario → "nomeAluno" | "usuarioNome" | fallback "Usuário"
     *   tituloLivro → "tituloLivro" | "titulo" | fallback "Livro Desconhecido"
     *   autorLivro  → "autorLivro"  | "autor"  | fallback "Autor Desconhecido"
     *
     * Documentos antigos (sem campos desnormalizados) exibem o fallback.
     * Documentos novos (criados via GAP-5) já carregam os campos corretamente.
     */
    private fun mapearDocumentos(
        documentos: List<com.google.firebase.firestore.DocumentSnapshot>
    ): List<ItemAluguel> {
        return documentos
            .mapNotNull { doc ->
                val docId   = doc.id
                val uidAluno = doc.getString("uidAluno")   ?: doc.getString("usuarioId") ?: ""
                val idLivro  = doc.getString("idLivro")    ?: doc.getString("livroId")   ?: ""
                val status   = doc.getString("status")     ?: "ativo"
                val dataMs   = doc.getLong("dataSolicitacao") ?: doc.getLong("dataMs")   ?: 0L

                // ── Campos desnormalizados — zero joins adicionais ────────────
                val nomeUsuario = doc.getString("nomeAluno")
                    ?: doc.getString("usuarioNome")
                    ?: getString(R.string.placeholder_usuario)

                val tituloLivro = doc.getString("tituloLivro")
                    ?: doc.getString("titulo")
                    ?: getString(R.string.sem_titulo)

                val autorLivro  = doc.getString("autorLivro")
                    ?: doc.getString("autor")
                    ?: getString(R.string.sem_autor)

                ItemAluguel(
                    docId       = docId,
                    uidAluno    = uidAluno,
                    idLivro     = idLivro,
                    dataMs      = dataMs,
                    status      = status,
                    nomeUsuario = nomeUsuario,
                    tituloLivro = tituloLivro,
                    autorLivro  = autorLivro
                )
            }
            .sortedByDescending { it.dataMs }
    }
}
