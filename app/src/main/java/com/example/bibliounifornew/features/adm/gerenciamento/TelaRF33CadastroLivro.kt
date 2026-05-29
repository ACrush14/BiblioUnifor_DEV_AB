package com.example.bibliounifornew.features.adm.gerenciamento

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF33CadastroLivro : AppCompatActivity() {

    private val db            = FirebaseFirestore.getInstance()
    private lateinit var etData: EditText

    // Launcher para obter o resultado do calendário
    private val calendarLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val dataSelecionada = result.data?.getStringExtra("dataSelecionada")
            etData.setText(dataSelecionada)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf33_cadastro_livro)

        val etTitulo   = findViewById<EditText>(R.id.editTituloLivro)
        val etAutor    = findViewById<EditText>(R.id.editAutorLivro)
        val etISBN     = findViewById<EditText>(R.id.editCodigoIsbn)
        val etSetor    = findViewById<EditText>(R.id.editSetorLivro)
        etData         = findViewById<EditText>(R.id.editDataPublicacao)
        val etQtd      = findViewById<EditText>(R.id.editQuantidadeExemplares)
        val btnAvancar = findViewById<MaterialButton>(R.id.btnEditarMaisInformacoes2)
        val tvErro     = findViewById<TextView>(R.id.textErroCampos)

        tvErro.visibility = View.GONE

        // Abrir calendário ao clicar no campo de data
        etData.setOnClickListener {
            val intent = Intent(this, TelaRF33CalendarioPublicacao::class.java)
            intent.putExtra("dataAtual", etData.text.toString())
            calendarLauncher.launch(intent)
        }

        btnAvancar.setOnClickListener {
            val titulo    = etTitulo.text.toString().trim()
            val autor     = etAutor.text.toString().trim()
            val isbn      = etISBN.text.toString().trim()
            val setor     = etSetor.text.toString().trim()
            val data      = etData.text.toString().trim()
            val qtdStr    = etQtd.text.toString().trim()

            // ── Validação ────────────────────────────────────────────────────
            if (titulo.isEmpty() || autor.isEmpty() || isbn.isEmpty() || setor.isEmpty() || data.isEmpty() || qtdStr.isEmpty()) {
                tvErro.visibility = View.VISIBLE
                tvErro.text = getString(R.string.erro_preencha_infos_livro)
                return@setOnClickListener
            }
            if (!validarFormatoData(data)) {
                tvErro.visibility = View.VISIBLE
                tvErro.text = getString(R.string.erro_data_invalida)
                return@setOnClickListener
            }
            val quantidade = qtdStr.toLongOrNull()
            if (quantidade == null || quantidade < 1) {
                tvErro.visibility = View.VISIBLE
                tvErro.text = getString(R.string.erro_quantidade_invalida)
                return@setOnClickListener
            }

            // ── Salvar no Firestore ───────────────────────────────────────────
            tvErro.visibility    = View.GONE
            btnAvancar.isEnabled = false

            val dados = hashMapOf(
                "title"            to titulo,
                "titulo"           to titulo,
                "author"           to autor,
                "autor"            to autor,
                "isbn"             to isbn,
                "codigo_isbn"      to isbn,
                "librarySector"    to setor,
                "setorBiblioteca"  to setor,
                "setor"            to setor,
                "dataPublicacao"   to data,
                "publishDate"      to data,
                "quantidade"       to quantidade,
                "exemplares"       to quantidade,
                "criadoEm"         to System.currentTimeMillis()
            )

            // Lógica para ISBN10 e ISBN13 para compatibilidade com RF13
            if (isbn.length == 10) {
                dados["isbn10"] = isbn
            } else if (isbn.length == 13) {
                dados["isbn13"] = isbn
            }

            db.collection("livros")
                .add(dados)
                .addOnSuccessListener { docRef ->
                    if (isFinishing || isDestroyed) return@addOnSuccessListener
                    btnAvancar.isEnabled = true
                    Toast.makeText(this, getString(R.string.msg_livro_cadastrado_sucesso), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, TelaRF33AdicionarMidiasExtras::class.java)
                    intent.putExtra("LIVRO_ID", docRef.id)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    if (isFinishing || isDestroyed) return@addOnFailureListener
                    btnAvancar.isEnabled = true
                    tvErro.visibility    = View.VISIBLE
                    tvErro.text          = getString(R.string.erro_conexao_banco)
                    Toast.makeText(this, getString(R.string.erro_falha_cadastrar_livro), Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun validarFormatoData(data: String): Boolean =
        data.matches(Regex("""\d{2}/\d{2}/\d{4}"""))
}
