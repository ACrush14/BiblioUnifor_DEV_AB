package com.example.bibliounifornew.features.adm.solicitacoes

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.gerenciamento.NavigationHelperADM
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF30UsuariosParaADM
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF37InfoLivroADM
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TelaRF36ListaAlugueisADM : AppCompatActivity() {

    private val db            = FirebaseFirestore.getInstance()
    private lateinit var adapter: AlugueisAdapter
    private val listaAlugueis = mutableListOf<ItemAluguel>()
    private var filterUsuarioId: String? = null
    private var activeDialog: Dialog? = null

    // Estados dos filtros
    private var filtroNomeUsuario: String = ""
    private var filtroStatus: String      = "Todos"
    private var termoBuscaBarra: String   = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf36_lista_alugueis_adm)

        filterUsuarioId = intent.getStringExtra("USUARIO_ID")

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAlugueis)
        adapter = AlugueisAdapter(
            listaAlugueis,
            onVerLivro   = { item ->
                // Issue #11: abre RF37 com o LIVRO_ID para o ADM gerenciar a mídia
                if (item.idLivro.isNotEmpty()) {
                    startActivity(
                        Intent(this, TelaRF37InfoLivroADM::class.java)
                            .putExtra("LIVRO_ID", item.idLivro)
                    )
                }
            },
            onVerUsuario = { item ->
                // Issue #11: abre RF30 com o UID do aluno para o ADM ver o perfil
                if (item.uidAluno.isNotEmpty()) {
                    startActivity(
                        Intent(this, TelaRF30UsuariosParaADM::class.java)
                            .putExtra("USUARIO_ID", item.uidAluno)
                    )
                }
            },
            onAprovar    = { item -> aprovarAluguel(item) },
            onReceber    = { item -> receberLivro(item)  }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        carregarAlugueis()

        // ─── FILTRO / BUSCA ───────────────────────────────────────────────────
        val editPesquisa = findViewById<EditText>(R.id.editPesquisaAlugueis)
        editPesquisa?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                termoBuscaBarra = s?.toString()?.trim()?.lowercase() ?: ""
                aplicarFiltros()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val btnFiltro = findViewById<ImageView>(R.id.buttonFiltroAlugueis)
        btnFiltro?.setOnClickListener { abrirPopupFiltro() }

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    override fun onDestroy() {
        activeDialog?.dismiss()
        activeDialog = null
        super.onDestroy()
    }

    /**
     * GAP-4 / PERF-2 FIX — carrega aluguéis ativos sem N+1 queries.
     * Se filterUsuarioId estiver presente, filtra apenas por esse usuário.
     */
    private fun carregarAlugueis() {
        val tvVazia = findViewById<TextView>(R.id.tvListaVazia)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                var query: com.google.firebase.firestore.Query = db.collection("solicitacoes_emprestimo")
                    .whereIn("status", listOf("pendente", "ativo", "atrasado"))
                
                if (!filterUsuarioId.isNullOrEmpty()) {
                    query = query.whereEqualTo("uidAluno", filterUsuarioId)
                }

                val result = query.get().await()

                if (result.isEmpty) {
                    // Fallback: tenta coleção legada "alugueis"
                    var queryAlt: com.google.firebase.firestore.Query = db.collection("alugueis")
                    if (!filterUsuarioId.isNullOrEmpty()) {
                        queryAlt = queryAlt.whereEqualTo("uidAluno", filterUsuarioId)
                    }
                    val resultAlt = queryAlt.get().await()
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
                    listaAlugueis.clear()
                    listaAlugueis.addAll(lista)
                    tvVazia?.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                    aplicarFiltros()
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
     * Hidratação de dados com JOIN paralelo por coroutine.
     *
     * Para cada documento de solicitacoes_emprestimo dispara em paralelo:
     *   • db.collection("usuarios").document(uidAluno).get().await()
     *     → preenche nomeUsuario e fotoUsuario
     *   • db.collection("livros").document(idLivro).get().await()
     *     → preenche tituloLivro, autorLivro e coverUrl
     *
     * Usa async/awaitAll dentro de coroutineScope — os 2 fetches por item
     * rodam em paralelo, todos em Dispatchers.IO, sem tocar a Main Thread.
     * Fallback: campos desnormalizados do próprio documento (docs antigos).
     */
    private suspend fun mapearDocumentos(
        documentos: List<com.google.firebase.firestore.DocumentSnapshot>
    ): List<ItemAluguel> = coroutineScope {
        documentos.map { doc ->
            async {
                val uidAluno      = doc.getString("uidAluno")  ?: doc.getString("usuarioId") ?: ""
                val idLivro       = doc.getString("idLivro")   ?: doc.getString("livroId")   ?: ""
                val status        = doc.getString("status")    ?: "ativo"
                val dataMs        = doc.getLong("dataSolicitacao") ?: doc.getLong("dataMs")   ?: 0L
                val dataDevolucao = doc.getLong("dataDevolucao")
                    ?: doc.getLong("dataDevolucaoMs") ?: 0L

                // ── JOIN paralelo: usuario + livro em simultâneo ──────────────
                val deferredUsuario = if (uidAluno.isNotEmpty()) {
                    async {
                        try { db.collection("usuarios").document(uidAluno).get().await() }
                        catch (_: Exception) { null }
                    }
                } else null

                val deferredLivro = if (idLivro.isNotEmpty()) {
                    async {
                        try { db.collection("livros").document(idLivro).get().await() }
                        catch (_: Exception) { null }
                    }
                } else null

                val docUsuario = deferredUsuario?.await()
                val docLivro   = deferredLivro?.await()

                // ── Resolve campos com fallback para desnormalizados ──────────
                val nomeUsuario = docUsuario?.getString("nome")
                    ?: doc.getString("nomeAluno") ?: doc.getString("usuarioNome")
                    ?: getString(R.string.placeholder_usuario)

                val fotoUsuario = docUsuario?.getString("fotoUrl") ?: ""

                val tituloLivro = (docLivro?.getString("title") ?: docLivro?.getString("titulo"))
                    ?: doc.getString("tituloLivro") ?: doc.getString("titulo")
                    ?: getString(R.string.sem_titulo)

                val autorLivro = (docLivro?.getString("author") ?: docLivro?.getString("autor"))
                    ?: doc.getString("autorLivro") ?: doc.getString("autor")
                    ?: getString(R.string.sem_autor)

                val coverUrl = docLivro?.getString("coverUrl")
                    ?: doc.getString("coverUrl") ?: ""

                ItemAluguel(
                    docId         = doc.id,
                    uidAluno      = uidAluno,
                    idLivro       = idLivro,
                    dataMs        = dataMs,
                    dataDevolucao = dataDevolucao,
                    status        = status,
                    nomeUsuario   = nomeUsuario,
                    fotoUsuario   = fotoUsuario,
                    tituloLivro   = tituloLivro,
                    autorLivro    = autorLivro,
                    coverUrl      = coverUrl
                )
            }
        }.awaitAll().sortedByDescending { it.dataMs }
    }

    // ─── FILTRAGEM ──────────────────────────────────────────────────────────

    private fun aplicarFiltros() {
        val filtrada = listaAlugueis.filter { item ->
            // 1. Barra de pesquisa (Nome Usuário ou Título Livro)
            val matchBarra = termoBuscaBarra.isEmpty() ||
                    item.nomeUsuario.lowercase().contains(termoBuscaBarra) ||
                    item.tituloLivro.lowercase().contains(termoBuscaBarra)

            // 2. Filtro Nome no Popup
            val matchNome = filtroNomeUsuario.isEmpty() ||
                    item.nomeUsuario.lowercase().contains(filtroNomeUsuario.lowercase())

            // 3. Filtro Status no Popup
            val matchStatus = filtroStatus == "Todos" ||
                    item.status.equals(filtroStatus, ignoreCase = true)

            matchBarra && matchNome && matchStatus
        }
        adapter.atualizarLista(filtrada)
        
        val tvVazia = findViewById<TextView>(R.id.tvListaVazia)
        tvVazia?.visibility = if (filtrada.isEmpty()) View.VISIBLE else View.GONE
    }

    // ─── APROVAR ALUGUEL (pendente → ativo) ───────────────────────────────────

    /**
     * Aprova uma solicitação de empréstimo físico:
     *   • status: "pendente" → "ativo"
     *   • dataEmprestimo: agora
     *   • dataDevolucao: agora + 14 dias
     *
     * A alteração reflete em tempo real no StatusAluguelAdapter do aluno
     * (status muda de "Aguardando aprovação" para "Ativo" com a data de devolução).
     */
    private fun aprovarAluguel(item: ItemAluguel) {
        if (item.docId.isEmpty()) return
        val agora        = System.currentTimeMillis()
        val dataDevolucao = agora + (14L * 24 * 60 * 60 * 1_000)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.collection("solicitacoes_emprestimo").document(item.docId)
                    .update(mapOf(
                        "status"          to "ativo",
                        "dataEmprestimo"  to agora,
                        "dataDevolucao"   to dataDevolucao,
                        "dataDevolucaoMs" to dataDevolucao
                    ))
                    .await()

                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    Toast.makeText(
                        this@TelaRF36ListaAlugueisADM,
                        getString(R.string.msg_aluguel_aprovado_adm),
                        Toast.LENGTH_SHORT
                    ).show()
                    // Recarrega a lista para refletir o novo status
                    listaAlugueis.clear()
                    carregarAlugueis()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isFinishing && !isDestroyed) {
                        Toast.makeText(
                            this@TelaRF36ListaAlugueisADM,
                            "Erro ao aprovar aluguel: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    // ─── RECEBER LIVRO (ativo → devolvido + estoque +1) ──────────────────────

    /**
     * Registra a devolução física do livro via writeBatch atômico:
     *   Etapa A — solicitacoes_emprestimo/{docId}.status → "devolvido"
     *   Etapa B — livros/{idLivro}: quantidade, estoque, exemplares += 1
     *             (FieldValue.increment é atômico — sem risco de race condition)
     *
     * Remove o item da lista do ADM após o commit bem-sucedido.
     */
    private fun receberLivro(item: ItemAluguel) {
        if (item.docId.isEmpty()) return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val batch = db.batch()

                // Etapa A: marca devolução no empréstimo
                batch.update(
                    db.collection("solicitacoes_emprestimo").document(item.docId),
                    mapOf(
                        "status"            to "devolvido",
                        "dataDevolucaoReal" to System.currentTimeMillis()
                    )
                )

                // Etapa B: incrementa estoque disponível do livro atomicamente
                if (item.idLivro.isNotEmpty()) {
                    batch.update(
                        db.collection("livros").document(item.idLivro),
                        mapOf(
                            "quantidade" to FieldValue.increment(1),
                            "estoque"    to FieldValue.increment(1),
                            "exemplares" to FieldValue.increment(1)
                        )
                    )
                }

                batch.commit().await()

                // RF20.6: notifica usuários que tinham este livro na lista de desejos
                // — roda no mesmo contexto IO, falha silenciosa para não bloquear o fluxo principal
                dispararNotificacoesDisponibilidade(item.idLivro, item.tituloLivro)

                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    Toast.makeText(
                        this@TelaRF36ListaAlugueisADM,
                        getString(R.string.msg_livro_recebido),
                        Toast.LENGTH_SHORT
                    ).show()
                    // Remove da lista sem recarregar (resposta imediata ao ADM)
                    adapter.removerItem(item.docId)
                    listaAlugueis.removeAll { it.docId == item.docId }
                    val tvVazia = findViewById<TextView>(R.id.tvListaVazia)
                    tvVazia?.visibility = if (listaAlugueis.isEmpty()) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isFinishing && !isDestroyed) {
                        Toast.makeText(
                            this@TelaRF36ListaAlugueisADM,
                            "Erro ao registrar devolução: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    // ─── RF20.6: GATILHO DE DISPONIBILIDADE ──────────────────────────────────
    //
    // Chamado no IO Thread logo após batch.commit() da devolução.
    // Busca todos os docs em lista_desejos com idLivro == livro devolvido
    // e cria uma notificação na subcoleção usuarios/{uid}/notificacoes de cada um.
    // Falha silenciosa: erros são logados mas não interrompem o fluxo de devolução.

    private suspend fun dispararNotificacoesDisponibilidade(idLivro: String, tituloLivro: String) {
        if (idLivro.isEmpty()) return
        try {
            val desejos = db.collection("lista_desejos")
                .whereEqualTo("idLivro", idLivro)
                .get()
                .await()

            if (desejos.isEmpty) return

            val agora      = System.currentTimeMillis()
            val notifBatch = db.batch()

            for (doc in desejos.documents) {
                val uid = doc.getString("uidUsuario")
                    ?: doc.getString("userId")
                    ?: doc.getString("uid")
                    ?: continue
                if (uid.isEmpty()) continue

                val notifRef = db.collection("usuarios")
                    .document(uid)
                    .collection("notificacoes")
                    .document()

                notifBatch.set(
                    notifRef,
                    mapOf(
                        "titulo"      to "Livro disponível!",
                        "descricao"   to "O livro \"$tituloLivro\" que você deseja acabou de ficar disponível no estoque!",
                        "tituloLivro" to tituloLivro,
                        "livroId"     to idLivro,
                        "tipo"        to "Disponibilidade",
                        "lida"        to false,
                        "data"        to agora
                    )
                )
            }

            notifBatch.commit().await()
            Log.d("RF20_6", "Notificações disparadas para ${desejos.size()} usuário(s) → '$tituloLivro'")

        } catch (e: Exception) {
            Log.e("RF20_6", "Erro ao disparar notificações de disponibilidade: ", e)
        }
    }

    private fun abrirPopupFiltro() {
        activeDialog?.dismiss()
        val dialog = Dialog(this)
        activeDialog = dialog

        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_filtrar_midia) // Reutilizando layout compatível
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setOnDismissListener { activeDialog = null }

        val spinner   = dialog.findViewById<Spinner>(R.id.spinnerSolicitacao)
        val editNome  = dialog.findViewById<EditText>(R.id.editNomeUsuario)
        val btnSalvar = dialog.findViewById<Button>(R.id.btnSalvarFiltro)
        val btnLimpar = dialog.findViewById<Button>(R.id.btnLimparFiltro)
        val txtTitulo = dialog.findViewById<TextView>(R.id.textTituloPopup)

        txtTitulo?.text = getString(R.string.titulo_lista_alugueis)
        editNome?.hint  = "Nome do Usuário"

        val opcoes = arrayOf("Todos", "pendente", "ativo", "atrasado")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcoes)
        spinner?.adapter = spinnerAdapter

        // Restaurar valores
        editNome?.setText(filtroNomeUsuario)
        val pos = opcoes.indexOf(filtroStatus)
        if (pos >= 0) spinner?.setSelection(pos)

        btnSalvar?.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)

            filtroNomeUsuario = editNome?.text.toString().trim()
            filtroStatus      = spinner?.selectedItem.toString()

            aplicarFiltros()
            dialog.dismiss()
        }

        btnLimpar?.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)

            filtroNomeUsuario = ""
            filtroStatus      = "Todos"
            editNome?.setText("")
            spinner?.setSelection(0)
            aplicarFiltros()
            dialog.dismiss()
        }

        dialog.show()
    }
}
