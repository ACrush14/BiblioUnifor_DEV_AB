package com.example.bibliounifornew.features.adm.gerenciamento

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF33AdicionarMidiasExtras : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var livroId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf33_adicionar_midia_extras)

        // Recebe o ID do documento criado no Passo 1
        livroId = intent.getStringExtra("LIVRO_ID") ?: ""

        val etPaginas   = findViewById<EditText>(R.id.editQuantidadePaginas)
        val etCategoria = findViewById<EditText>(R.id.editCategoriaLivro)
        val etEditora   = findViewById<EditText>(R.id.editEditoraLivro)
        val etIdioma    = findViewById<EditText>(R.id.editIdiomaLivro)
        val etDimensoes = findViewById<EditText>(R.id.editDimensoesLivro)
        val etAsin      = findViewById<EditText>(R.id.editAsinLivro)
        val etLinkCapa  = findViewById<EditText>(R.id.editLinkImagem)
        val etSinopse   = findViewById<EditText>(R.id.editSinopse)
        val btnAvancar  = findViewById<MaterialButton>(R.id.btnEditarMaisInformacoes2)

        btnAvancar.setOnClickListener {
            val paginas   = etPaginas.text.toString().trim()
            val categoria = etCategoria.text.toString().trim()
            val editora   = etEditora.text.toString().trim()
            val idioma    = etIdioma.text.toString().trim()
            val dimensoes = etDimensoes.text.toString().trim()
            val asin      = etAsin.text.toString().trim()
            val sinopse   = etSinopse.text.toString().trim()
            val linkCapa  = etLinkCapa.text.toString().trim()   // opcional

            if (paginas.isEmpty() || categoria.isEmpty() || editora.isEmpty() || idioma.isEmpty() || sinopse.isEmpty()) {
                Toast.makeText(this, getString(R.string.erro_preencha_infos_extras), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnAvancar.isEnabled = false

            // ── Salva campos extras no documento já criado no Passo 1 ──────
            val atualizacoes = hashMapOf<String, Any>(
                "totalPages"    to (paginas.toLongOrNull() ?: 0L),
                "paginas"       to (paginas.toLongOrNull() ?: 0L),
                "pageCount"     to (paginas.toLongOrNull() ?: 0L),
                "category"      to categoria,
                "categoria"     to categoria,
                "publisher"     to editora,
                "editora"       to editora,
                "language"      to idioma,
                "lingua"        to idioma,
                "idioma"        to idioma,
                "dimensions"    to dimensoes,
                "dimensoes"     to dimensoes,
                "asin"          to asin,
                "ASIN"          to asin,
                "description"   to sinopse,
                "descricao"     to sinopse,
                "coverUrl"      to linkCapa,
                "imagemUrl"     to linkCapa
            )

            if (livroId.isNotEmpty()) {
                db.collection("livros").document(livroId)
                    .update(atualizacoes)
                    .addOnSuccessListener {
                        btnAvancar.isEnabled = true
                        avancarParaArquivos()
                    }
                    .addOnFailureListener {
                        btnAvancar.isEnabled = true
                        Toast.makeText(this, getString(R.string.erro_conexao_banco), Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Edge case: livroId vazio (cadastro fora do fluxo normal) — avança sem salvar
                btnAvancar.isEnabled = true
                avancarParaArquivos()
            }
        }
    }

    private fun avancarParaArquivos() {
        val intent = Intent(this, TelaRF33AdicionarMidiaArquivos::class.java)
        intent.putExtra("LIVRO_ID", livroId)   // BUG-C2 FIX: propaga o ID para o Passo 3
        startActivity(intent)
    }
}
