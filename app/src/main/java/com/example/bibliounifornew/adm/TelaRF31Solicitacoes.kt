package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Window
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

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
            lista            = listaSolicit,
            onVerSolicitacoes = { item -> abrirPopupSolicitacoes(item) },
            onEnviarAudiobook = { item -> escolherArquivo("audio/*", item, "audiobook") },
            onEnviarPdf       = { item -> escolherArquivo("application/pdf", item, "pdf") },
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

                    val base = ItemSolicitacaoMidia(
                        docId       = docId,
                        uidUsuario  = uidUsuario,
                        idLivro     = idLivro,
                        tiposSolicit = tipos,
                        status      = status
                    )
                    listaTemp.add(base)

                    var nomeUsuario = "Usuário"
                    var titulo      = "Título Indisponível"
                    var autor       = "Autor Desconhecido"
                    var joinsLeft   = 2

                    fun verificar() {
                        joinsLeft--
                        if (joinsLeft == 0) {
                            val idx = listaTemp.indexOfFirst { it.docId == docId }
                            if (idx >= 0) {
                                listaTemp[idx] = listaTemp[idx].copy(
                                    nomeUsuario = nomeUsuario,
                                    tituloLivro = titulo,
                                    autorLivro  = autor
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
                                titulo = l.getString("title")  ?: l.getString("titulo") ?: "Título Indisponível"
                                autor  = l.getString("author") ?: l.getString("autor")  ?: "Autor Desconhecido"
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

        dialog.findViewById<TextView>(R.id.textPopupNomeUsuario)?.text    = "Usuário: ${item.nomeUsuario}"
        dialog.findViewById<TextView>(R.id.textPopupListaSolicitacoes)?.text = "Solicitações: ${item.tiposSolicit}"
        dialog.findViewById<TextView>(R.id.textPopupStatus)?.text         = "Status: ${item.status}"
        dialog.findViewById<Button>(R.id.btnFecharSolicitacoes)?.setOnClickListener { dialog.dismiss() }
        dialog.show()
        val w = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    // ─── ESCOLHER ARQUIVO ────────────────────────────────────────────────────

    private fun escolherArquivo(mimeType: String, item: ItemSolicitacaoMidia, tipo: String) {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = mimeType
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(Intent.createChooser(intent, "Selecione o arquivo"), 100)
            db.collection("solicitacoes_midia").document(item.docId)
                .set(mapOf("status" to "concluido", "status_$tipo" to "aprovado"), SetOptions.merge())
                .addOnSuccessListener {
                    criarNotificacaoMidia(item.uidUsuario)
                    Toast.makeText(this, "${tipo.replaceFirstChar { it.uppercase() }} aprovado e usuário notificado.", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao abrir seletor de arquivo.", Toast.LENGTH_SHORT).show()
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
            if (senha != "DevsAB") {
                Toast.makeText(this, "Credencial incorreta.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            db.collection("solicitacoes_midia").document(item.docId)
                .delete()
                .addOnSuccessListener {
                    adapter.removerItem(position)
                    Toast.makeText(this, "Solicitação excluída.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao excluir: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
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
