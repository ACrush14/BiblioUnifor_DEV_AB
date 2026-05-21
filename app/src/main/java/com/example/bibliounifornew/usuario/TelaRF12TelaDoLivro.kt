package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.telarf12_teladolivro)

        val livroId = intent.getStringExtra("LIVRO_ID")

        if (livroId != null) {
            carregarDadosDoLivro(livroId)
        }

        // Botão para voltar ou ir para Minha Livraria
        findViewById<Button>(R.id.buttonSuaLivraria).setOnClickListener {
            val intent = Intent(this, TelaRF15MinhaLivrariaActivity::class.java)
            startActivity(intent)
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
                        crossfade(true)
                        placeholder(R.drawable.osda)
                        error(R.drawable.osda)
                    }
                } else {
                    imgCapa.setImageResource(R.drawable.osda)
                }
            }
        }
    }
}