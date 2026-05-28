package com.example.bibliounifornew.features.usuario.livro

/**
 * TelaLivroActivity — Tela de Detalhes do Livro (padrão ViewModel + Room).
 *
 * Fluxo de dados (offline-first):
 *   1. Extrai LIVRO_ID da Intent.
 *   2. Tenta buscar no Room (cache local) via LivroViewModel.
 *   3. Se cache miss → dispara sincronização com Firestore e tenta de novo.
 *   4. Popula os campos do layout telarf12_teladolivro.xml.
 *
 * NOTA DE REGISTRO no AndroidManifest.xml:
 *   <activity android:name=".features.usuario.livro.TelaLivroActivity" />
 *
 * Como usar o LivroAdapter para navegar até esta Activity:
 *   adapter = LivroAdapter(
 *       livros      = lista,
 *       onItemClick = { livro ->
 *           startActivity(
 *               Intent(this, TelaLivroActivity::class.java)
 *                   .putExtra("LIVRO_ID", livro.id)   // ← OBRIGATÓRIO
 *           )
 *       }
 *   )
 */

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AppDatabase
import com.example.bibliounifornew.data.EntidadeLivro
import com.example.bibliounifornew.data.LivroRepository
import com.example.bibliounifornew.features.usuario.biblioteca.TelaRF14LeituraActivity
import com.example.bibliounifornew.viewmodel.LivroViewModel
import com.example.bibliounifornew.viewmodel.LivroViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import android.content.Intent

class TelaLivroActivity : AppCompatActivity() {

    private lateinit var viewModel: LivroViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf12_teladolivro)

        val livroId = intent.getStringExtra("LIVRO_ID") ?: run {
            Toast.makeText(this, "ID do livro não informado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db         = AppDatabase.getDatabase(applicationContext)
        val repository = LivroRepository(db.livroDao(), FirebaseFirestore.getInstance())
        val factory    = LivroViewModelFactory(repository)
        viewModel      = ViewModelProvider(this, factory)[LivroViewModel::class.java]

        // ── CHAMA A FUNÇÃO EXTRAÍDA AQUI ──
        carregarLivro(livroId)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val novoLivroId = intent.getStringExtra("LIVRO_ID")
        if (novoLivroId != null) {
            carregarLivro(novoLivroId)
        }
    }

    // ── FUNÇÃO QUE VOCÊ ESQUECEU DE CRIAR ──
    private fun carregarLivro(livroId: String) {
        lifecycleScope.launch {
            var livro = viewModel.buscarLivroPorId(livroId)

            if (livro == null) {
                viewModel.sincronizarComNuvem()
                livro = viewModel.buscarLivroPorId(livroId)
            }

            if (livro != null) {
                popularUI(livro)
            } else {
                Toast.makeText(this@TelaLivroActivity, "Livro não encontrado.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // ─── POPULAR UI ───────────────────────────────────────────────────────────

    /**
     * Injeta os dados de [EntidadeLivro] em todos os campos do
     * layout telarf12_teladolivro.xml.
     * Todos os IDs foram verificados contra o XML real do projeto.
     */
    private fun popularUI(livro: EntidadeLivro) {

        // Título e autor (cabeçalho)
        findViewById<TextView>(R.id.textTituloLivro)?.text = livro.title
        findViewById<TextView>(R.id.textAutorLivro)?.text  = livro.author

        // Sinopse
        findViewById<TextView>(R.id.textSobreLivro)?.text  = livro.description

        // Capa via Coil — placeholder enquanto carrega, fallback se falhar
        val imgCapa = findViewById<ImageView>(R.id.imageLivroDetalhes)
        if (livro.coverUrl.isNotEmpty()) {
            imgCapa?.load(livro.coverUrl) {
                placeholder(R.drawable.osda)
                error(R.drawable.osda)
            }
        } else {
            imgCapa?.setImageResource(R.drawable.osda)
        }

        // Etiqueta de categoria
        if (livro.category.isNotEmpty()) {
            findViewById<MaterialButton>(R.id.buttonGenero)?.text = livro.category
        }

        // ── Disponibilidade ────────────────────────────────────────────────────
        val txtDisp    = findViewById<TextView>(R.id.textDisponivel)
        val txtEstoque = findViewById<TextView>(R.id.textEstoque)
        val indicador  = findViewById<View>(R.id.statusIndicator)

        if (livro.isAvailable && livro.stockQuantity > 0) {
            txtDisp?.text = "Disponível para aluguel"
            txtDisp?.setTextColor(Color.parseColor("#2E7D32"))
            txtEstoque?.text =
                "${livro.stockQuantity} unidade${if (livro.stockQuantity == 1) "" else "s"} em estoque"
            indicador?.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        } else {
            txtDisp?.text = "Indisponível no momento"
            txtDisp?.setTextColor(Color.parseColor("#C62828"))
            txtEstoque?.text = "Sem estoque"
            indicador?.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#C62828"))
        }

        // ── Botão "Ver Mais" → RF13 ────────────────────────────────────────────
        findViewById<MaterialButton>(R.id.buttonVerMais)?.setOnClickListener {
            startActivity(
                android.content.Intent(this, TelaRF13VerMaisLivro::class.java)
                    .putExtra("LIVRO_ID", livro.id)
            )
        }

        // ── Botão "Opções de Leitura" → RF14 ──────────────────────────────────
        findViewById<MaterialButton>(R.id.buttonOpcoesLeitura)?.setOnClickListener {
            startActivity(
                android.content.Intent(this, TelaRF14LeituraActivity::class.java)
                    .putExtra("LIVRO_ID", livro.id)
            )
        }
    }
}
