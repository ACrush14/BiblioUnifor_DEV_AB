package com.example.bibliounifornew.usuario

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AppDatabase
import com.example.bibliounifornew.data.LivroRepository
import com.example.bibliounifornew.viewmodel.LivroViewModel
import com.example.bibliounifornew.viewmodel.LivroViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class TelaRF13VerMaisLivro : AppCompatActivity() {

    // Arquitetura limpa: Instanciando a ViewModel via Factory
    private val viewModel: LivroViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = LivroRepository(database.livroDao(), FirebaseFirestore.getInstance())
        LivroViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf13_telavermaislivro)

        val livroId = intent.getStringExtra("LIVRO_ID")

        if (livroId != null) {
            carregarDadosDetalhados(livroId)
        }
    }

    private fun carregarDadosDetalhados(id: String) {
        lifecycleScope.launch {
            // Buscando da ViewModel em vez do DAO
            val livro = viewModel.buscarLivroPorId(id)

            livro?.let {
                findViewById<TextView>(R.id.textTituloLivroInfo).text = it.title
                findViewById<TextView>(R.id.textAutorLivroInfo).text = it.author

                // CORRIGIDO: Trocado de 'content' para 'description'
                findViewById<TextView>(R.id.textDescricaoLivro).text = it.description

                val imgCapa = findViewById<ImageView>(R.id.imageLivroInfo)
                if (it.coverUrl.isNotEmpty()) {
                    imgCapa.setImageResource(R.drawable.osda)
                } else {
                    imgCapa.setImageResource(R.drawable.osda)
                }
            }
        }
    }
}