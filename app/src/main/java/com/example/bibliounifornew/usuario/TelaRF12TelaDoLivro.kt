package com.example.bibliounifornew.usuario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AppDatabase
import com.example.bibliounifornew.data.LivroRepository
import com.example.bibliounifornew.viewmodel.LivroViewModel
import com.example.bibliounifornew.viewmodel.LivroViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class TelaRF12TelaDoLivro : AppCompatActivity() {

    private val viewModel: LivroViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = LivroRepository(database.livroDao(), FirebaseFirestore.getInstance())
        LivroViewModelFactory(repository)
    }

    // DECLARAÇÃO FORA DO VIEWMODEL
    private val usuarioFantasmaId = "USUARIO_TESTE_123"
    private var livroIdAtual: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.telarf12_teladolivro)

        val context: Context = this@TelaRF12TelaDoLivro
        // CORREÇÃO: Pegando o ID como String em vez de Int
        val livroId = intent.getStringExtra("LIVRO_ID")
        livroIdAtual = livroId

        // CORREÇÃO: Verificando se a String não é nula
        if (livroId != null) {
            carregarDadosDoLivro(livroId)
        }

        configurarBotoesDeStatus()

        findViewById<Button>(R.id.buttonVerMais).setOnClickListener {
            val intentVerMais = Intent(context, TelaRF13VerMaisLivro::class.java)
            intentVerMais.putExtra("LIVRO_ID", livroId) // Passando a String
            startActivity(intentVerMais)
        }

        findViewById<Button>(R.id.buttonSolicitar).setOnClickListener {
            val intentSolicitar = Intent(context, TelaRF19Solicitacoes::class.java)
            intentSolicitar.putExtra("LIVRO_ID", livroId) // Passando a String
            startActivity(intentSolicitar)
        }

        findViewById<Button>(R.id.buttonLer).setOnClickListener {
            val intentLer = Intent(context, TelaRF14LeituraActivity::class.java)
            intentLer.putExtra("LIVRO_ID", livroId) // Passando a String
            startActivity(intentLer)
        }
    }

    private fun carregarDadosDoLivro(id: String) {
        lifecycleScope.launch {
            val livro = viewModel.buscarLivroPorId(id)
            livro?.let {
                findViewById<TextView>(R.id.textTituloLivro).text = it.title
                findViewById<TextView>(R.id.textAutorLivro).text = it.author
                findViewById<TextView>(R.id.textSobreLivro).text = it.description

                val imgCapa = findViewById<ImageView>(R.id.imageLivroDetalhes)
                if (it.coverUrl.isNotEmpty()) {
                    imgCapa.load(it.coverUrl) {
                        placeholder(R.drawable.osda)
                        error(R.drawable.osda)
                    }
                } else {
                    imgCapa.setImageResource(R.drawable.osda)
                }
            }
        }
    }

    private fun configurarBotoesDeStatus() {
        val btnNaoLido = findViewById<Button>(R.id.buttonNaoLido)
        val btnLendo = findViewById<Button>(R.id.buttonLendo)
        val btnLido = findViewById<Button>(R.id.buttonLido)

        btnNaoLido.setOnClickListener {
            mudarCorDosBotoes(btnNaoLido, btnLendo, btnLido)
            salvarStatusNoFirebase("Não Lido")
        }
        btnLendo.setOnClickListener {
            mudarCorDosBotoes(btnLendo, btnNaoLido, btnLido)
            salvarStatusNoFirebase("Lendo")
        }
        btnLido.setOnClickListener {
            mudarCorDosBotoes(btnLido, btnNaoLido, btnLendo)
            salvarStatusNoFirebase("Lido")
        }
    }

    private fun mudarCorDosBotoes(ativo: Button, inativo1: Button, inativo2: Button) {
        ativo.backgroundTintList = getColorStateList(R.color.biblio_dark)
        inativo1.backgroundTintList = getColorStateList(R.color.biblio_blue)
        inativo2.backgroundTintList = getColorStateList(R.color.biblio_blue)
    }

    private fun salvarStatusNoFirebase(status: String) {
        val idDoLivro = livroIdAtual ?: return
        val firestore = FirebaseFirestore.getInstance()
        val documentoId = "${usuarioFantasmaId}_${idDoLivro}"

        val dados = hashMapOf(
            "usuarioId" to usuarioFantasmaId,
            "livroId" to idDoLivro,
            "statusLeitura" to status,
            "atualizadoEm" to System.currentTimeMillis()
        )

        firestore.collection("biblioteca_usuarios")
            .document(documentoId)
            .set(dados)
            .addOnSuccessListener {
                Toast.makeText(this, "Status: $status salvo!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar.", Toast.LENGTH_SHORT).show()
            }
    }
}
