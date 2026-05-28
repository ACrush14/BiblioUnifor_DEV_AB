package com.example.bibliounifornew.features.adm.solicitacoes

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Window
import android.view.ViewGroup
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
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /* filtro futuro */ }
            override fun afterTextChanged(s: Editable?) {}
        })

        val btnFiltro = findViewById<ImageView>(R.id.buttonFiltroMidia)
        btnFiltro?.setOnClickListener { abrirPopupFiltro() }

        carregarSolicitacoes()
        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    /**
     * Carrega solicitações de mídia (tipo_midia != null) da coleção solicitacoes_midia.
     * Se não existir essa coleção, usa solicitacoes_emprestimo com filtro de tipo.
     */
    private fun carregarSolicitacoes() {
        db.collection("solicitacoes_midia")
            .whereEqualTo("status", "pendente")
            .get()
            .addOnSuccessListener { result ->
                val docs = result.documents
                if (docs.isEmpty()) {
                    listaSolicit.clear()
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "Nenhuma solicitação de mídia pendente.", Toast.LENGTH_SHORT).show()
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
                                adapter.atualizarLista(listaTemp)
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
                Toast.makeText(this, "Erro ao carregar solicitações: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // ─── POPUP VER SOLICITAÇÕES DO USUÁRIO ───────────────────────────────────

    private fun abrirPopupSolicitacoes(item: ItemSolicitacaoMidia) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_solicitacoes_usuario_adm)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // ── Cabeçalho ──────────────────────────────────────────────────────────
        dialog.findViewById<TextView>(R.id.textPopupNomeUsuario)?.text       = "Usuário: ${item.nomeUsuario}"
        dialog.findViewById<TextView>(R.id.textPopupListaSolicitacoes)?.text = "Tipos: ${item.tiposSolicit.ifBlank { "—" }}"
        dialog.findViewById<TextView>(R.id.textPopupStatus)?.text            = "Status: ${item.status}"

        // ── Card do livro: dados dinâmicos ─────────────────────────────────────
        dialog.findViewById<TextView>(R.id.textTituloLivroSolicitado)?.text = item.tituloLivro
        dialog.findViewById<TextView>(R.id.textAutorLivroSolicitado)?.text  = item.autorLivro

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        dialog.findViewById<TextView>(R.id.textDataLivroSolicitado)?.text =
            if (item.dataSolicitacao > 0L)
                "Solicitado em: ${sdf.format(Date(item.dataSolicitacao))}"
            else "Solicitado em: —"

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
                Toast.makeText(this, "Preencha ao menos um link antes de salvar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (item.idLivro.isEmpty()) {
                Toast.makeText(this, "ID do livro não encontrado. Não foi possível salvar.", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@TelaRF31Solicitacoes, "Links salvos com sucesso!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@TelaRF31Solicitacoes, "Erro ao salvar links: ${e.message}", Toast.LENGTH_LONG).show()
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
            .setPositiveButton("Salvar") { _, _ ->
                val link = editLink.text.toString().trim()
                if (link.isEmpty()) {
                    Toast.makeText(this, "Informe um link válido.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (item.idLivro.isEmpty()) {
                    Toast.makeText(this, "ID do livro não encontrado. Verifique a solicitação.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                salvarLinkMidia(item, campoFirestore, link)
            }
            .setNegativeButton("Cancelar", null)
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
                    Toast.makeText(
                        this@TelaRF31Solicitacoes,
                        "Link salvo e usuário notificado!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TelaRF31Solicitacoes,
                        "Erro ao salvar link: ${e.message}",
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
                criarNotificacaoMidia(item.uidUsuario)
                Toast.makeText(this, "Braille aprovado e usuário notificado.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao notificar usuário.", Toast.LENGTH_SHORT).show()
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
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_confirmar_exclusao_solicitacao)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

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
                    db.collection("solicitacoes_midia").document(item.docId)
                        .delete()
                        .addOnSuccessListener {
                            adapter.removerItem(position)
                            Toast.makeText(this, "Solicitação excluída.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            btnConfirm.isEnabled = true
                            Toast.makeText(this, "Erro ao excluir: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    btnConfirm.isEnabled = true
                    Toast.makeText(this, getString(R.string.erro_senha_incorreta), Toast.LENGTH_SHORT).show()
                }
        }

        btnCancel?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ─── POPUP FILTRO ────────────────────────────────────────────────────────

    private fun abrirPopupFiltro() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_filtrar_midia)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val spinner  = dialog.findViewById<Spinner>(R.id.spinnerSolicitacao)
        val editNome = dialog.findViewById<EditText>(R.id.editNomeUsuario)
        val btnSalvar = dialog.findViewById<Button>(R.id.btnSalvarFiltro)
        val btnLimpar = dialog.findViewById<Button>(R.id.btnLimparFiltro)

        val opcoes = arrayOf("Selecione...", "PDF", "Audiobook", "Braille")
        spinner?.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcoes)

        btnSalvar?.setOnClickListener {
            Toast.makeText(this, "Filtro aplicado.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        btnLimpar?.setOnClickListener {
            editNome?.setText("")
            spinner?.setSelection(0)
            dialog.dismiss()
        }
        dialog.show()
    }
}
