package com.example.bibliounifornew.features.adm.gerenciamento

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.R
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF37InfoLivroADM : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var livroId: String = ""
    private var senhaVisivel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf37_info_livro_adm)

        livroId = intent.getStringExtra("LIVRO_ID") ?: ""

        if (livroId.isNotEmpty()) {
            carregarDadosLivro()
        } else {
            Toast.makeText(this, "Erro: ID do livro não fornecido.", Toast.LENGTH_SHORT).show()
        }

        // Configurar cliques dos lápis para editar campos
        configurarEdicao(R.id.btnEditTitulo, R.id.editTituloLivro, "title")
        configurarEdicao(R.id.btnEditAutor, R.id.editAutorLivro, "author")
        configurarEdicao(R.id.btnEditDescricao, R.id.editDescricaoLivro, "description")
        configurarEdicao(R.id.btnEditLingua, R.id.editLingua, "language")
        configurarEdicao(R.id.btnEditEditora, R.id.editEditora, "publisher")
        configurarEdicao(R.id.btnEditDimensao, R.id.editDimensao, "dimensions")
        configurarEdicao(R.id.btnEditISBN10, R.id.editISBN10, "isbn10")
        configurarEdicao(R.id.btnEditISBN13, R.id.editISBN13, "isbn13")
        configurarEdicao(R.id.btnEditData, R.id.editData, "publishedDate")
        configurarEdicao(R.id.btnEditPaginas, R.id.editPaginas, "pageCount")

        // Controle de Exemplares
        val tvTotal = findViewById<TextView>(R.id.textExemplaresTotal)
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnDiminuirExemplares)?.setOnClickListener {
            atualizarExemplares(-1)
        }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnAumentarExemplares)?.setOnClickListener {
            atualizarExemplares(1)
        }

        // Botão Apagar Mídia
        findViewById<Button>(R.id.btnApagarMidia)?.setOnClickListener {
            abrirPopupApagar()
        }
    }

    private fun carregarDadosLivro() {
        db.collection("livros").document(livroId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    findViewById<EditText>(R.id.editTituloLivro)?.setText(doc.getString("title") ?: doc.getString("titulo"))
                    findViewById<EditText>(R.id.editAutorLivro)?.setText(doc.getString("author") ?: doc.getString("autor"))
                    findViewById<EditText>(R.id.editDescricaoLivro)?.setText(doc.getString("description") ?: doc.getString("descricao"))
                    findViewById<EditText>(R.id.editLingua)?.setText(doc.getString("language") ?: "Português")
                    findViewById<EditText>(R.id.editEditora)?.setText(doc.getString("publisher") ?: "Indisponível")
                    findViewById<EditText>(R.id.editDimensao)?.setText(doc.getString("dimensions") ?: "")
                    findViewById<EditText>(R.id.editISBN10)?.setText(doc.getString("isbn10") ?: "")
                    findViewById<EditText>(R.id.editISBN13)?.setText(doc.getString("isbn13") ?: "")
                    findViewById<EditText>(R.id.editData)?.setText(doc.getString("publishedDate") ?: "")
                    findViewById<EditText>(R.id.editPaginas)?.setText(doc.getLong("pageCount")?.toString() ?: "0")
                    
                    val total = doc.getLong("totalExemplares") ?: 0L
                    findViewById<TextView>(R.id.textExemplaresTotal)?.text = total.toString()

                    val coverUrl = doc.getString("coverUrl") ?: ""
                    val card = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardCapaLivro)
                    val iv = card?.getChildAt(0) as? ImageView
                    iv?.load(coverUrl.ifEmpty { R.drawable.osda }) {
                        placeholder(R.drawable.osda)
                        error(R.drawable.osda)
                    }

                    // Carregar Avaliações dinamicamente
                    carregarAvaliacoes()
                }
            }
    }

    private fun carregarAvaliacoes() {
        val container = findViewById<LinearLayout>(R.id.containerAvaliacoes)
        val textSemAvaliacoes = findViewById<TextView>(R.id.textSemAvaliacoes)

        // Por enquanto, como não há coleção de avaliações definida, 
        // limpamos o container e mostramos a mensagem de "Sem avaliações"
        // para remover o texto genérico do Ronaldo Alves.
        container?.let {
            // Remove tudo exceto a mensagem de "Sem avaliações"
            for (i in it.childCount - 1 downTo 0) {
                val child = it.getChildAt(i)
                if (child.id != R.id.textSemAvaliacoes) {
                    it.removeView(child)
                }
            }
        }
        
        textSemAvaliacoes?.visibility = View.VISIBLE
    }

    private fun configurarEdicao(botaoId: Int, campoId: Int, firestoreField: String) {
        val botao = findViewById<ImageView>(botaoId)
        val campo = findViewById<EditText>(campoId)

        botao?.setOnClickListener {
            if (campo?.isEnabled == false) {
                campo.isEnabled = true
                campo.requestFocus()
                campo.setSelection(campo.text.length)
                botao.setImageResource(android.R.drawable.ic_menu_save) // Muda ícone para "Salvar"
                Toast.makeText(this, "Editando...", Toast.LENGTH_SHORT).show()
            } else {
                // Salva no Firestore
                val novoValor = campo?.text.toString()
                db.collection("livros").document(livroId).update(firestoreField, novoValor)
                    .addOnSuccessListener {
                        campo?.isEnabled = false
                        botao.setImageResource(R.drawable.ic_edit_pencil)
                        Toast.makeText(this, "Campo atualizado!", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun atualizarExemplares(delta: Int) {
        val tvTotal = findViewById<TextView>(R.id.textExemplaresTotal)
        val atual = tvTotal.text.toString().toIntOrNull() ?: 0
        val novo = (atual + delta).coerceAtLeast(0)
        
        db.collection("livros").document(livroId).update("totalExemplares", novo)
            .addOnSuccessListener {
                tvTotal.text = novo.toString()
            }
    }

    private fun abrirPopupApagar() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_apagar_midia)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val window = dialog.window
        window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        val editSenha = dialog.findViewById<EditText>(R.id.editSenhaAtual)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarApagar)

        btnConfirmar?.setOnClickListener {
            if (editSenha?.text.isNullOrEmpty()) {
                Toast.makeText(this, "Digite sua senha", Toast.LENGTH_SHORT).show()
            } else {
                db.collection("livros").document(livroId).delete().addOnSuccessListener {
                    Toast.makeText(this, "Mídia removida com sucesso", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    finish()
                }
            }
        }
        dialog.findViewById<Button>(R.id.btnCancelarApagar)?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
