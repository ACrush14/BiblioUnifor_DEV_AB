package com.example.bibliounifornew.usuario

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.R
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF13VerMaisLivro : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf13_telavermaislivro)

        val livroId = intent.getStringExtra("LIVRO_ID")
        if (livroId.isNullOrEmpty()) {
            Toast.makeText(this, "ID do livro não encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        carregarDadosDetalhados(livroId)
    }

    private fun carregarDadosDetalhados(id: String) {
        db.collection("livros").document(id).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Livro não encontrado.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val sem = "Sem informação"

                // ── Header ────────────────────────────────────────────────────
                val titulo = doc.getString("title")  ?: doc.getString("titulo")  ?: sem
                val autor  = doc.getString("author") ?: doc.getString("autor")   ?: sem
                val desc   = doc.getString("description") ?: doc.getString("descricao") ?: sem
                val cover  = doc.getString("coverUrl") ?: ""

                findViewById<TextView>(R.id.textTituloLivroInfo)?.text = titulo
                findViewById<TextView>(R.id.textAutorLivroInfo)?.text  = autor
                findViewById<TextView>(R.id.textDescricaoLivro)?.text  = desc

                val imgCapa = findViewById<ImageView>(R.id.imageLivroInfo)
                if (cover.isNotEmpty()) {
                    imgCapa?.load(cover) { placeholder(R.drawable.osda); error(R.drawable.osda) }
                } else {
                    imgCapa?.setImageResource(R.drawable.osda)
                }

                // ── Campos de detalhe ─────────────────────────────────────────
                // Padrão: campo EN primeiro, depois PT-BR, fallback "Sem informação"
                fun s(vararg keys: String) =
                    keys.mapNotNull { doc.getString(it)?.takeIf { v -> v.isNotEmpty() } }
                        .firstOrNull() ?: sem

                fun b(vararg keys: String): String {
                    for (k in keys) {
                        val v = doc.getBoolean(k) ?: continue
                        return if (v) "Sim" else "Não"
                    }
                    // fallback: checar link não vazio como indicador de "Sim"
                    return sem
                }

                fun n(vararg keys: String): String {
                    for (k in keys) {
                        val v = doc.getLong(k) ?: continue
                        if (v > 0) return v.toString()
                    }
                    return sem
                }

                findViewById<TextView>(R.id.textValorLingua)?.text =
                    s("language", "lingua", "idioma")

                findViewById<TextView>(R.id.textValorEditora)?.text =
                    s("publisher", "editora")

                findViewById<TextView>(R.id.textValorDimensao)?.text =
                    s("dimensions", "dimensoes", "dimensao")

                findViewById<TextView>(R.id.textValorIsbn10)?.text =
                    s("isbn10", "isbn_10", "ISBN10")

                findViewById<TextView>(R.id.textValorIsbn13)?.text =
                    s("isbn13", "isbn_13", "ISBN13")

                findViewById<TextView>(R.id.textValorAsin)?.text =
                    s("asin", "ASIN")

                findViewById<TextView>(R.id.textValorData)?.text =
                    s("publishDate", "dataPublicacao", "data")

                // Para PDF/Braille/Audiobook: se há link de mídia, indica "Sim"
                val linkPdf   = doc.getString("linkPdf")      ?: ""
                val linkAudio = doc.getString("linkAudiobook") ?: ""
                findViewById<TextView>(R.id.textValorPdf)?.text =
                    if (linkPdf.isNotEmpty()) "Sim"
                    else b("hasPdf", "temPdf")

                findViewById<TextView>(R.id.textValorBraille)?.text =
                    b("hasBraille", "temBraille")

                findViewById<TextView>(R.id.textValorAudiobook)?.text =
                    if (linkAudio.isNotEmpty()) "Sim"
                    else b("hasAudiobook", "temAudiobook")

                findViewById<TextView>(R.id.textValorPaginas)?.text =
                    n("totalPages", "paginas", "numeroPaginas")

                findViewById<TextView>(R.id.textValorSetor)?.text =
                    s("librarySector", "setorBiblioteca", "setor")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar detalhes do livro.", Toast.LENGTH_SHORT).show()
            }
    }
}
