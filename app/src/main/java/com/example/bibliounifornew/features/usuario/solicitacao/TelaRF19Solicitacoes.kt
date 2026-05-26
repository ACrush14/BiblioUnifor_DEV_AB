package com.example.bibliounifornew.features.usuario.solicitacao

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.R
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF19Solicitacoes : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var tituloAtual: String = ""
    private var autorAtual: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf19_solicitacoes)

        // ─── EXTRAS ───────────────────────────────────────────────────────────
        // Recebe o ID do livro da tela anterior (RF12 / TelaLivroActivity)
        val livroId = intent.getStringExtra("LIVRO_ID") ?: ""

        // ─── CABEÇALHO — POPULAR COM DADOS REAIS ─────────────────────────────
        // Os IDs abaixo mapeiam diretamente para telarf19_solicitacoes.xml
        val imgCapa   = findViewById<ImageView>(R.id.imageLivroSolicitacao)
        val txtTitulo = findViewById<TextView>(R.id.textTituloLivroSolicitacao)
        val txtAutor  = findViewById<TextView>(R.id.textAutorLivroSolicitacao)
        val txtGenero = findViewById<TextView>(R.id.textGeneroLivroSolicitacao)

        if (livroId.isNotEmpty()) {
            carregarDadosDoLivro(livroId, imgCapa, txtTitulo, txtAutor, txtGenero)
        }

        // ─── BOTÕES → TERMOS COM TIPO E LIVRO_ID ─────────────────────────────
        fun irParaTermos(tipoMidia: String) {
            startActivity(
                Intent(this, TelaRF19SolicitacoesTermosCondicoes::class.java)
                    .putExtra("TIPO_MIDIA", tipoMidia)
                    .putExtra("LIVRO_ID",   livroId)
                    .putExtra("TITULO",     tituloAtual)
                    .putExtra("AUTOR",      autorAtual)
            )
        }

        val btnPdf      = findViewById<Button>(R.id.buttonSolicitarPdf)
        val btnBraile   = findViewById<Button>(R.id.buttonSolicitarBraille)
        val btnAudio    = findViewById<Button>(R.id.buttonSolicitarAudiobook)
        val btnReservar = findViewById<Button>(R.id.buttonReservarLivro)
        val btnSetor    = findViewById<Button>(R.id.buttonSetorLocalizado)

        btnPdf?.setOnClickListener      { irParaTermos("PDF")       }
        btnBraile?.setOnClickListener   { irParaTermos("Braille")   }
        btnAudio?.setOnClickListener    { irParaTermos("Audiobook") }
        btnReservar?.setOnClickListener { irParaTermos("Reserva")   }
        btnSetor?.setOnClickListener    { showPopupSetor()           }
    }

    // ─── CARREGAMENTO DO CABEÇALHO ────────────────────────────────────────────

    /**
     * Consulta o Firestore pelo livroId e injeta capa, título, autor e gênero
     * nos campos do header do telarf19_solicitacoes.xml.
     * Tenta os campos em EN e PT-BR para máxima compatibilidade com o banco.
     */
    private fun carregarDadosDoLivro(
        livroId  : String,
        imgCapa  : ImageView?,
        txtTitulo: TextView?,
        txtAutor : TextView?,
        txtGenero: TextView?
    ) {
        db.collection("livros").document(livroId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists() || isFinishing || isDestroyed) return@addOnSuccessListener

                val titulo   = doc.getString("title")       ?: doc.getString("titulo")    ?: ""
                tituloAtual = titulo
                val autor    = doc.getString("author")      ?: doc.getString("autor")     ?: ""
                autorAtual  = autor
                val genero   = doc.getString("category")    ?: doc.getString("categoria") ?: ""
                val coverUrl = doc.getString("coverUrl")    ?: ""

                txtTitulo?.text = titulo.ifEmpty { "Sem título" }
                txtAutor?.text  = autor.ifEmpty  { "Autor desconhecido" }

                if (genero.isNotEmpty()) txtGenero?.text = genero

                if (coverUrl.isNotEmpty()) {
                    imgCapa?.load(coverUrl) {
                        placeholder(R.drawable.osda)
                        error(R.drawable.osda)
                    }
                } else {
                    imgCapa?.setImageResource(R.drawable.osda)
                }
            }
            // Falha silenciosa — cabeçalho permanece com valores padrão do XML
    }

    // ─── POPUP SETOR ─────────────────────────────────────────────────────────

    private fun showPopupSetor() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_setor_localizado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Injeta o nome do livro atual no popup
        dialog.findViewById<TextView>(R.id.textLivroSetor)?.text = "Livro: $tituloAtual"

        dialog.findViewById<Button>(R.id.buttonVoltarSetor)
            ?.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
