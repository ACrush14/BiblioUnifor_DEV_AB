package com.example.bibliounifornew.features.adm.solicitacoes

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Window
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.gerenciamento.NavigationHelperADM
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TelaRF31Solicitacoes : AppCompatActivity() {

    private val db              = FirebaseFirestore.getInstance()
    private lateinit var adapter: SolicitacoesMidiaAdapter
    private val listaSolicit    = mutableListOf<ItemSolicitacaoMidia>()
    private var activeDialog: Dialog? = null

    // Estados dos filtros
    private var filtroNomeUsuario: String = ""
    private var filtroTipoMidia: String   = "Selecione..."
    private var termoBuscaBarra: String   = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf31_solicitacoes_adm)

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewSolicitacoesMidia)
        adapter = SolicitacoesMidiaAdapter(
            lista             = listaSolicit,
            onVerSolicitacoes = { item -> abrirPopupSolicitacoes(item) },
            onEnviarAudiobook = { item -> abrirPopupLink(item, "audiobook") },
            onEnviarPdf       = { item -> abrirPopupLink(item, "pdf") },
            onBraille         = { item -> notificarBraille(item) },
            onExcluir         = { item, pos -> abrirPopupExcluir(item, pos) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ─── FILTRO / BUSCA ───────────────────────────────────────────────────
        val editPesquisa = findViewById<EditText>(R.id.editPesquisaMidia)
        editPesquisa?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                termoBuscaBarra = s?.toString()?.trim()?.lowercase() ?: ""
                aplicarFiltros()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val btnFiltro = findViewById<ImageView>(R.id.buttonFiltroMidia)
        btnFiltro?.setOnClickListener { abrirPopupFiltro(editPesquisa) }

        carregarSolicitacoes()
        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    override fun onDestroy() {
        activeDialog?.dismiss()
        activeDialog = null
        super.onDestroy()
    }

    /**
     * Carrega solicitações de mídia (tipo_midia != null) da coleção solicitacoes_midia.
     * Se não existir essa coleção, usa solicitacoes_emprestimo com filtro de tipo.
     */
    private fun carregarSolicitacoes() {
        listaSolicit.clear() // Limpa antes de carregar para evitar duplicatas
        db.collection("solicitacoes_midia")
            .whereEqualTo("status", "pendente")
            .get()
            .addOnSuccessListener { result ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener
                val docs = result.documents
                if (docs.isEmpty()) {
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, getString(R.string.msg_sem_solicitacoes_midia), Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val total       = docs.size
                var processados = 0
                val listaTemp   = mutableListOf<ItemSolicitacaoMidia>()

                for (doc in docs) {
                    val docId       = doc.id
                    val uidUsuario  = doc.getString("uidUsuario") ?: doc.getString("uidAluno") ?: ""
                    val idLivro     = doc.getString("idLivro")    ?: ""
                    val tipos       = doc.getString("tipos")      ?: ""
                    val status      = doc.getString("status")     ?: "pendente"

                    val dataSolicit = doc.getLong("criadoEm")
                        ?: doc.getLong("dataSolicitacao") ?: 0L

                    val base = ItemSolicitacaoMidia(
                        docId           = docId,
                        uidUsuario      = uidUsuario,
                        idLivro         = idLivro,
                        tiposSolicit    = tipos,
                        status          = status,
                        dataSolicitacao = dataSolicit
                    )
                    listaTemp.add(base)

                    var nomeUsuario = "Usuário"
                    var titulo      = "Título Indisponível"
                    var autor       = "Autor Desconhecido"
                    var coverUrl    = ""
                    var joinsLeft   = 2

                    fun verificar() {
                        if (isFinishing || isDestroyed) return
                        joinsLeft--
                        if (joinsLeft == 0) {
                            val idx = listaTemp.indexOfFirst { it.docId == docId }
                            if (idx >= 0) {
                                listaTemp[idx] = listaTemp[idx].copy(
                                    nomeUsuario = nomeUsuario,
                                    tituloLivro = titulo,
                                    autorLivro  = autor,
                                    coverUrl    = coverUrl
                                )
                            }
                            processados++
                            if (processados == total) {
                                runOnUiThread {
                                    if (!isFinishing && !isDestroyed) {
                                        listaSolicit.clear()
                                        listaSolicit.addAll(listaTemp)
                                        aplicarFiltros()
                                    }
                                }
                            }
                        }
                    }

                    if (uidUsuario.isNotEmpty()) {
                        db.collection("usuarios").document(uidUsuario).get()
                            .addOnSuccessListener { u ->
                                nomeUsuario = u.getString("nome") ?: u.getString("email") ?: "Usuário"
                                verificar()
                            }.addOnFailureListener { verificar() }
                    } else verificar()

                    if (idLivro.isNotEmpty()) {
                        db.collection("livros").document(idLivro).get()
                            .addOnSuccessListener { l ->
                                titulo   = l.getString("title")    ?: l.getString("titulo")  ?: "Título Indisponível"
                                autor    = l.getString("author")   ?: l.getString("autor")   ?: "Autor Desconhecido"
                                coverUrl = l.getString("coverUrl") ?: ""
                                verificar()
                            }.addOnFailureListener { verificar() }
                    } else verificar()
                }
            }
            .addOnFailureListener { e ->
                if (isFinishing || isDestroyed) return@addOnFailureListener
                Toast.makeText(this, "Erro ao carregar solicitações: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ─── FILTRAGEM ──────────────────────────────────────────────────────────

    private fun aplicarFiltros() {
        val filtrada = listaSolicit.filter { item ->
            // 1. Barra de pesquisa (Nome Usuário ou Título Livro)
            val matchBarra = termoBuscaBarra.isEmpty() ||
                    item.nomeUsuario.lowercase().contains(termoBuscaBarra) ||
                    item.tituloLivro.lowercase().contains(termoBuscaBarra)

            // 2. Filtro Nome no Popup
            val matchNome = filtroNomeUsuario.isEmpty() ||
                    item.nomeUsuario.lowercase().contains(filtroNomeUsuario.lowercase())

            // 3. Filtro Tipo Mídia no Popup
            val matchTipo = filtroTipoMidia == "Selecione..." ||
                    item.tiposSolicit.lowercase().contains(filtroTipoMidia.lowercase())

            matchBarra && matchNome && matchTipo
        }
        adapter.atualizarLista(filtrada.toMutableList())
    }

    // ─── POPUP VER SOLICITAÇÕES DO USUÁRIO ───────────────────────────────────

    private fun abrirPopupSolicitacoes(item: ItemSolicitacaoMidia) {
        activeDialog?.dismiss()
        val dialog = Dialog(this)
        activeDialog = dialog

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_solicitacoes_usuario_adm)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setOnDismissListener { activeDialog = null }

        // ── Cabeçalho ──────────────────────────────────────────────────────────
        dialog.findViewById<TextView>(R.id.textPopupNomeUsuario)?.text       = getString(R.string.label_usuario_prefix, item.nomeUsuario)
        dialog.findViewById<TextView>(R.id.textPopupListaSolicitacoes)?.text = getString(R.string.label_tipos_prefix, item.tiposSolicit.ifBlank { "—" })
        dialog.findViewById<TextView>(R.id.textPopupStatus)?.text            = getString(R.string.label_status_prefix, item.status)

        // ── Card do livro: dados dinâmicos ─────────────────────────────────────
        dialog.findViewById<TextView>(R.id.textTituloLivroSolicitado)?.text = item.tituloLivro
        dialog.findViewById<TextView>(R.id.textAutorLivroSolicitado)?.text  = item.autorLivro

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        dialog.findViewById<TextView>(R.id.textDataLivroSolicitado)?.text =
            if (item.dataSolicitacao > 0L)
                getString(R.string.label_solicitado_em, sdf.format(Date(item.dataSolicitacao)))
            else getString(R.string.label_solicitado_em, "—")

        val imgCapa = dialog.findViewById<ImageView>(R.id.imageLivroSolicitado)
        if (item.coverUrl.isNotBlank()) {
            imgCapa?.load(item.coverUrl) {
                placeholder(R.drawable.osda)
                error(R.drawable.osda)
            }
        } else {
            imgCapa?.setImageResource(R.drawable.osda)
        }

        // ── Campos de link ─────────────────────────────────────────────────────
        val editPdf   = dialog.findViewById<TextInputEditText>(R.id.editLinkPdf)
        val editAudio = dialog.findViewById<TextInputEditText>(R.id.editLinkAudiobook)

        // ── Salvar Links ───────────────────────────────────────────────────────
        dialog.findViewById<MaterialButton>(R.id.btnSalvarLinks)?.setOnClickListener {
            val linkPdf   = editPdf?.text.toString().trim()
            val linkAudio = editAudio?.text.toString().trim()

            if (linkPdf.isEmpty() && linkAudio.isEmpty()) {
                Toast.makeText(this, getString(R.string.erro_link_vazio), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (item.idLivro.isEmpty()) {
                Toast.makeText(this, getString(R.string.erro_id_livro_invalido), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val atualizacoes = mutableMapOf<String, Any>()
            if (linkPdf.isNotEmpty())   atualizacoes["linkPdf"]       = linkPdf
            if (linkAudio.isNotEmpty()) atualizacoes["linkAudiobook"] = linkAudio

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    db.collection("livros").document(item.idLivro)
                        .update(atualizacoes)
                        .await()

                    // Marca a solicitação como concluída
                    db.collection("solicitacoes_midia").document(item.docId)
                        .set(mapOf("status" to "concluido"), SetOptions.merge())
                        .await()

                    criarNotificacaoMidia(item.uidUsuario)

                    withContext(Dispatchers.Main) {
                        if (isFinishing || isDestroyed) return@withContext
                        // Esconde o teclado antes de fechar para evitar erro de callback IME
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(it.windowToken, 0)

                        Toast.makeText(this@TelaRF31Solicitacoes, getString(R.string.msg_links_salvos), Toast.LENGTH_SHORT).show()
                        
                        // Remove o item da lista local e atualiza UI
                        listaSolicit.removeAll { it.docId == item.docId }
                        aplicarFiltros()
                        
                        dialog.dismiss()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (isFinishing || isDestroyed) return@withContext
                        Toast.makeText(this@TelaRF31Solicitacoes, getString(R.string.fmt_erro_salvar_links, e.message), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // ── Fechar ─────────────────────────────────────────────────────────────
        dialog.findViewById<MaterialButton>(R.id.btnFecharSolicitacoes)?.setOnClickListener { dialog.dismiss() }

        dialog.show()
        val w = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    // ─── POPUP DE LINK (substitui o file picker) ─────────────────────────────

    /**
     * Exibe um AlertDialog com um EditText para o ADM colar a URL do PDF ou Audiobook.
     *
     * Ao confirmar, salva a URL no documento do livro ([item.idLivro]) e marca a
     * solicitação como "concluido" — sem nenhum upload de arquivo para o Storage.
     *
     * @param tipo "pdf" → campo "linkPdf" / "audiobook" → campo "linkAudiobook"
     */
    private fun abrirPopupLink(item: ItemSolicitacaoMidia, tipo: String) {
        val campoFirestore = if (tipo == "pdf") "linkPdf" else "linkAudiobook"
        val titulo         = if (tipo == "pdf") "Link do PDF" else "Link do Audiobook"

        val editLink = EditText(this).apply {
            hint      = "Cole a URL aqui (https://...)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            setPadding(64, 40, 64, 24)
            setSingleLine(false)
            maxLines = 3
        }

        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage("Insira o Link da Mídia a ser disponibilizada para o usuário.")
            .setView(editLink)
            .setPositiveButton(getString(R.string.dialog_btn_salvar)) { _, _ ->
                val link = editLink.text.toString().trim()
                if (link.isEmpty()) {
                    Toast.makeText(this, getString(R.string.erro_link_invalido), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (item.idLivro.isEmpty()) {
                    Toast.makeText(this, getString(R.string.erro_id_livro_solicitacao), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                salvarLinkMidia(item, campoFirestore, link)
            }
            .setNegativeButton(getString(R.string.dialog_btn_cancelar), null)
            .show()
    }

    /**
     * Persiste o link no Firestore em IO thread:
     * 1. Atualiza [campoFirestore] no doc do livro
     * 2. Marca a solicitação como "concluido"
     * 3. Envia notificação ao aluno via subcoleção
     */
    private fun salvarLinkMidia(
        item           : ItemSolicitacaoMidia,
        campoFirestore : String,
        link           : String
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                db.collection("livros").document(item.idLivro)
                    .update(campoFirestore, link)
                    .await()

                db.collection("solicitacoes_midia").document(item.docId)
                    .set(
                        mapOf(
                            "status"             to "concluido",
                            "status_$campoFirestore" to "aprovado"
                        ),
                        SetOptions.merge()
                    )
                    .await()

                criarNotificacaoMidia(item.uidUsuario)

                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    Toast.makeText(
                        this@TelaRF31Solicitacoes,
                        getString(R.string.msg_links_salvos),
                        Toast.LENGTH_SHORT
                    ).show()

                    // Remove o item da lista local e atualiza UI
                    listaSolicit.removeAll { it.docId == item.docId }
                    aplicarFiltros()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    Toast.makeText(
                        this@TelaRF31Solicitacoes,
                        getString(R.string.fmt_erro_salvar_links, e.message),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // ─── NOTIFICAR BRAILLE ────────────────────────────────────────────────────

    private fun notificarBraille(item: ItemSolicitacaoMidia) {
        db.collection("solicitacoes_midia").document(item.docId)
            .set(mapOf("status" to "concluido", "status_braille" to "aprovado"), SetOptions.merge())
            .addOnSuccessListener {
                if (isFinishing || isDestroyed) return@addOnSuccessListener
                criarNotificacaoMidia(item.uidUsuario)
                Toast.makeText(this, getString(R.string.msg_braille_aprovado), Toast.LENGTH_SHORT).show()

                // Remove o item da lista local e atualiza UI
                listaSolicit.removeAll { it.docId == item.docId }
                aplicarFiltros()
            }
            .addOnFailureListener {
                if (isFinishing || isDestroyed) return@addOnFailureListener
                Toast.makeText(this, getString(R.string.erro_notificar_usuario), Toast.LENGTH_SHORT).show()
            }
    }

    // ─── HELPER: cria notificação na subcoleção do usuário ────────────────────

    private fun criarNotificacaoMidia(uidAluno: String) {
        if (uidAluno.isEmpty()) return
        val notif = hashMapOf(
            "titulo"   to "Sua mídia está pronta!",
            "mensagem" to "O ADM liberou o acesso ao seu material especial.",
            "lida"     to false,
            "data"     to System.currentTimeMillis()
        )
        db.collection("usuarios").document(uidAluno)
            .collection("notificacoes").add(notif)
    }

    // ─── POPUP EXCLUIR ────────────────────────────────────────────────────────

    private fun abrirPopupExcluir(item: ItemSolicitacaoMidia, position: Int) {
        activeDialog?.dismiss()
        val dialog = Dialog(this)
        activeDialog = dialog

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_confirmar_exclusao_solicitacao)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setOnDismissListener { activeDialog = null }

        val editSenha  = dialog.findViewById<EditText>(R.id.editSenhaConfirmacao)
        val btnOlho    = dialog.findViewById<ImageView>(R.id.buttonVerSenha)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirmarExclusao)
        val btnCancel  = dialog.findViewById<Button>(R.id.btnCancelarExclusao)

        var senhaVisivel = false
        btnOlho?.setOnClickListener {
            senhaVisivel = !senhaVisivel
            editSenha?.inputType = if (senhaVisivel)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnOlho.setImageResource(if (senhaVisivel) R.drawable.ic_eye_open else R.drawable.ic_eye_closed)
            editSenha?.setSelection(editSenha.text.length)
        }

        btnConfirm?.setOnClickListener {
            val senha = editSenha?.text.toString()

            if (senha.isEmpty()) {
                Toast.makeText(this, "Informe sua senha para confirmar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Esconde o teclado
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)

            val currentUser = FirebaseAuth.getInstance().currentUser
            val adminEmail  = currentUser?.email

            if (currentUser == null || adminEmail.isNullOrEmpty()) {
                Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Reautentica o admin com a senha dele — mesmo padrão de RF30 e RF38.
            // Substitui a senha mestra hardcoded ("DevsAB") que era legível após decompilação.
            btnConfirm.isEnabled = false
            val credential = EmailAuthProvider.getCredential(adminEmail, senha)
            currentUser.reauthenticate(credential)
                .addOnSuccessListener {
                    if (isFinishing || isDestroyed) return@addOnSuccessListener
                    db.collection("solicitacoes_midia").document(item.docId)
                        .delete()
                        .addOnSuccessListener {
                            if (isFinishing || isDestroyed) return@addOnSuccessListener
                            adapter.removerItem(position)
                            Toast.makeText(this, "Solicitação excluída.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            if (isFinishing || isDestroyed) return@addOnFailureListener
                            btnConfirm.isEnabled = true
                            Toast.makeText(this, "Erro ao excluir: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    if (isFinishing || isDestroyed) return@addOnFailureListener
                    btnConfirm.isEnabled = true
                    Toast.makeText(this, getString(R.string.erro_senha_incorreta), Toast.LENGTH_SHORT).show()
                }
        }

        btnCancel?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ─── POPUP FILTRO ────────────────────────────────────────────────────────

    private fun abrirPopupFiltro(editPesquisa: EditText?) {
        activeDialog?.dismiss()
        val dialog = Dialog(this)
        activeDialog = dialog

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_filtrar_midia)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setOnDismissListener { activeDialog = null }

        val spinner  = dialog.findViewById<Spinner>(R.id.spinnerSolicitacao)
        val editNome = dialog.findViewById<EditText>(R.id.editNomeUsuario)
        val btnSalvar = dialog.findViewById<Button>(R.id.btnSalvarFiltro)
        val btnLimpar = dialog.findViewById<Button>(R.id.btnLimparFiltro)

        val opcoes = arrayOf("Selecione...", "PDF", "Audiobook", "Braille")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcoes)
        spinner?.adapter = spinnerAdapter

        // Restaurar valores salvos
        editNome?.setText(filtroNomeUsuario)
        val pos = opcoes.indexOf(filtroTipoMidia)
        if (pos >= 0) spinner?.setSelection(pos)

        btnSalvar?.setOnClickListener {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)

            filtroNomeUsuario = editNome?.text.toString().trim()
            filtroTipoMidia   = spinner?.selectedItem.toString()

            aplicarFiltros()
            Toast.makeText(this, getString(R.string.msg_filtro_aplicado), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        btnLimpar?.setOnClickListener {
            filtroNomeUsuario = ""
            filtroTipoMidia   = "Selecione..."
            termoBuscaBarra   = ""
            editPesquisa?.setText("") // Limpa a barra de busca também
            
            aplicarFiltros()
            Toast.makeText(this, getString(R.string.msg_filtros_limpos), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }
}
