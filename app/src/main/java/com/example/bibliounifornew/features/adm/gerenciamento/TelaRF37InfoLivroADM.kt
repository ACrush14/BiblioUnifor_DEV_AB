package com.example.bibliounifornew.features.adm.gerenciamento

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF37InfoLivroADM : AppCompatActivity() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var livroId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf37_info_livro_adm)

        livroId = intent.getStringExtra("LIVRO_ID") ?: ""

        if (livroId.isNotEmpty()) {
            carregarDadosLivro()
        } else {
            // GAP-2 FIX: ID ausente → não deixa tela vazia com "O Alienista" do XML.
            // Toast + finish() retornam imediatamente para RF32 sem estado quebrado.
            Toast.makeText(this, getString(R.string.erro_id_livro_nao_fornecido), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Configurar cliques dos lápis para editar campos
        configurarEdicao(R.id.btnEditTitulo,        R.id.editTituloLivro,   "title")
        configurarEdicao(R.id.btnEditAutor,         R.id.editAutorLivro,    "author")
        configurarEdicao(R.id.btnEditDescricao,     R.id.editDescricaoLivro,"description")
        configurarEdicao(R.id.btnEditLingua,        R.id.editLingua,        "language")
        configurarEdicao(R.id.btnEditEditora,       R.id.editEditora,       "publisher")
        configurarEdicao(R.id.btnEditDimensao,      R.id.editDimensao,      "dimensions")
        configurarEdicao(R.id.btnEditISBN10,        R.id.editISBN10,        "isbn10")
        configurarEdicao(R.id.btnEditISBN13,        R.id.editISBN13,        "isbn13")
        configurarEdicao(R.id.btnEditData,          R.id.editData,          "publishedDate")
        configurarEdicao(R.id.btnEditPaginas,       R.id.editPaginas,       "pageCount")
        // Campos de URL e Categoria — adicionados para fechar o schema completo
        configurarEdicao(R.id.btnEditCategoria,     R.id.editCategoria,     "category")
        configurarEdicao(R.id.btnEditLinkCapa,      R.id.editLinkCapa,      "coverUrl")
        configurarEdicao(R.id.btnEditLinkPdf,       R.id.editLinkPdf,       "linkPdf")
        configurarEdicao(R.id.btnEditLinkAudiobook, R.id.editLinkAudiobook, "linkAudiobook")

        // Controle de Exemplares
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnDiminuirExemplares)
            ?.setOnClickListener { atualizarExemplares(-1) }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnAumentarExemplares)
            ?.setOnClickListener { atualizarExemplares(1) }

        // Botão Apagar Mídia
        findViewById<Button>(R.id.btnApagarMidia)?.setOnClickListener {
            abrirPopupApagar()
        }
    }

    // ─── CARREGAR DADOS ───────────────────────────────────────────────────────

    private fun carregarDadosLivro() {
        db.collection("livros").document(livroId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                findViewById<EditText>(R.id.editTituloLivro)?.setText(
                    doc.getString("title")       ?: doc.getString("titulo")      ?: "")
                findViewById<EditText>(R.id.editAutorLivro)?.setText(
                    doc.getString("author")      ?: doc.getString("autor")       ?: "")
                findViewById<EditText>(R.id.editDescricaoLivro)?.setText(
                    doc.getString("description") ?: doc.getString("descricao")   ?: "")
                findViewById<EditText>(R.id.editLingua)?.setText(
                    doc.getString("language")    ?: "Português")
                findViewById<EditText>(R.id.editEditora)?.setText(
                    doc.getString("publisher")   ?: doc.getString("editora")     ?: "")
                findViewById<EditText>(R.id.editDimensao)?.setText(
                    doc.getString("dimensions")  ?: "")
                findViewById<EditText>(R.id.editISBN10)?.setText(
                    doc.getString("isbn10")      ?: "")
                findViewById<EditText>(R.id.editISBN13)?.setText(
                    doc.getString("isbn13")      ?: "")
                findViewById<EditText>(R.id.editData)?.setText(
                    doc.getString("publishedDate") ?: "")
                findViewById<EditText>(R.id.editPaginas)?.setText(
                    doc.getLong("pageCount")?.toString() ?: "0")

                // Campos de URL e Categoria
                val categoria = doc.getString("category")
                    ?: doc.getString("categoria") ?: ""
                findViewById<EditText>(R.id.editCategoria)?.setText(categoria)

                val linkCapa = doc.getString("coverUrl")
                    ?: doc.getString("imagemUrl") ?: ""
                findViewById<EditText>(R.id.editLinkCapa)?.setText(linkCapa)

                val linkPdf = doc.getString("linkPdf") ?: ""
                findViewById<EditText>(R.id.editLinkPdf)?.setText(linkPdf)

                val linkAudio = doc.getString("linkAudiobook") ?: ""
                findViewById<EditText>(R.id.editLinkAudiobook)?.setText(linkAudio)

                // Braille — define o estado ANTES de registrar o listener para evitar
                // disparos espúrios ao reciclar / recarregar.
                val hasBraille = doc.getBoolean("braille")
                    ?: doc.getBoolean("hasBraille") ?: false
                val checkBraille = findViewById<CheckBox>(R.id.checkBrailleInfo)
                checkBraille?.isChecked = hasBraille
                checkBraille?.setOnCheckedChangeListener { _, isChecked ->
                    db.collection("livros").document(livroId)
                        .update(mapOf("braille" to isChecked, "hasBraille" to isChecked))
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@TelaRF37InfoLivroADM,
                                getString(R.string.msg_campo_atualizado),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@TelaRF37InfoLivroADM,
                                getString(R.string.erro_conexao_banco),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }

                // BUG-F2 FIX: lê campo "quantidade" (padrão do projeto) com fallback
                val total = doc.getLong("quantidade")
                    ?: doc.getLong("exemplares")
                    ?: doc.getLong("totalExemplares")
                    ?: 0L
                findViewById<TextView>(R.id.textExemplaresTotal)?.text = total.toString()

                // BUG-F5 FIX: usa ID direto em vez de getChildAt(0)
                val coverUrl = doc.getString("coverUrl") ?: doc.getString("imagemUrl") ?: ""
                findViewById<ImageView>(R.id.imgCapaLivroDetalhe)?.load(
                    coverUrl.ifEmpty { null }
                ) {
                    placeholder(R.drawable.osda)
                    error(R.drawable.osda)
                    fallback(R.drawable.osda)
                }

                carregarAvaliacoes()
            }
    }

    // ─── AVALIAÇÕES ───────────────────────────────────────────────────────────

    private fun carregarAvaliacoes() {
        val container = findViewById<LinearLayout>(R.id.containerAvaliacoes)
        val textSemAvaliacoes = findViewById<TextView>(R.id.textSemAvaliacoes)

        container?.let {
            for (i in it.childCount - 1 downTo 0) {
                val child = it.getChildAt(i)
                if (child.id != R.id.textSemAvaliacoes) it.removeView(child)
            }
        }
        textSemAvaliacoes?.visibility = View.VISIBLE
    }

    // ─── EDIÇÃO DE CAMPOS ─────────────────────────────────────────────────────

    private fun configurarEdicao(botaoId: Int, campoId: Int, firestoreField: String) {
        val botao = findViewById<ImageView>(botaoId)
        val campo = findViewById<EditText>(campoId)

        botao?.setOnClickListener {
            if (campo?.isEnabled == false) {
                campo.isEnabled = true
                campo.requestFocus()
                campo.setSelection(campo.text.length)
                botao.setImageResource(android.R.drawable.ic_menu_save)
                Toast.makeText(this, getString(R.string.msg_editando), Toast.LENGTH_SHORT).show()
            } else {
                val novoValor = campo?.text.toString()
                db.collection("livros").document(livroId).update(firestoreField, novoValor)
                    .addOnSuccessListener {
                        campo?.isEnabled = false
                        botao.setImageResource(R.drawable.ic_edit_pencil)
                        Toast.makeText(this, getString(R.string.msg_campo_atualizado), Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, getString(R.string.erro_conexao_banco), Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // ─── CONTROLE DE EXEMPLARES ───────────────────────────────────────────────

    private fun atualizarExemplares(delta: Int) {
        val tvTotal = findViewById<TextView>(R.id.textExemplaresTotal)
        val atual   = tvTotal.text.toString().toIntOrNull() ?: 0
        val novo    = (atual + delta).coerceAtLeast(0)

        // BUG-F2 FIX: salva em "quantidade" (campo padrão lido por TelaRF32LivrosCRUD)
        db.collection("livros").document(livroId)
            .update(mapOf("quantidade" to novo, "exemplares" to novo))
            .addOnSuccessListener {
                tvTotal.text = novo.toString()
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.erro_conexao_banco), Toast.LENGTH_SHORT).show()
            }
    }

    // ─── POPUP APAGAR ─────────────────────────────────────────────────────────

    /**
     * BUG-F3 FIX: valida senha via reauthenticate() antes de deletar o documento.
     */
    private fun abrirPopupApagar() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_apagar_midia)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val inputSenha   = dialog.findViewById<TextInputEditText>(R.id.editSenhaAtual)
        val textErro     = dialog.findViewById<TextView>(R.id.textErroSenha)
        val btnConfirmar = dialog.findViewById<MaterialButton>(R.id.btnConfirmarApagar)
        val btnCancelar  = dialog.findViewById<MaterialButton>(R.id.btnCancelarApagar)

        textErro?.visibility = View.GONE

        btnConfirmar?.setOnClickListener {
            val senha = inputSenha?.text.toString().trim()
            if (senha.isEmpty()) {
                textErro?.visibility = View.VISIBLE
                textErro?.text = getString(R.string.popup_apagar_midia_erro_senha)
                return@setOnClickListener
            }

            val user = auth.currentUser
            if (user == null || user.email.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.erro_sessao_expirada), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnConfirmar.isEnabled = false
            textErro?.visibility = View.GONE

            val credential = EmailAuthProvider.getCredential(user.email!!, senha)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    db.collection("livros").document(livroId).delete()
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                getString(R.string.msg_midia_removida),
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                            finish()
                        }
                        .addOnFailureListener {
                            btnConfirmar.isEnabled = true
                            Toast.makeText(this, getString(R.string.erro_conexao_banco), Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    btnConfirmar.isEnabled = true
                    textErro?.visibility = View.VISIBLE
                    textErro?.text = getString(R.string.erro_senha_incorreta)
                }
        }

        btnCancelar?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
