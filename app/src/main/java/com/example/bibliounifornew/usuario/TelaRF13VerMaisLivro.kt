package com.example.bibliounifornew.usuario

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.data.AppDatabase
import com.example.bibliounifornew.R
import kotlinx.coroutines.launch

class TelaRF13VerMaisLivro : AppCompatActivity() {

    private val database by lazy { AppDatabase.getDatabase(this@TelaRF13VerMaisLivro) }
    private val libroDao by lazy { database.livroDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this@TelaRF13VerMaisLivro.setContentView(R.layout.telarf13_telavermaislivro)

        // CORREÇÃO: Pegando o ID como String
        val livroId = intent.getStringExtra("LIVRO_ID")

        // CORREÇÃO: Verificando se a String não é nula
        if (livroId != null) {
            carregarDadosDetalhados(livroId)
        }
    }

    // CORREÇÃO: O parâmetro id agora é uma String
    private fun carregarDadosDetalhados(id: String) {
        lifecycleScope.launch {
            val livro = libroDao.buscarLivroPorId(id)
            livro?.let {
                findViewById<TextView>(R.id.textTituloLivroInfo).text = it.title
                findViewById<TextView>(R.id.textAutorLivroInfo).text = it.author
                findViewById<TextView>(R.id.textDescricaoLivro).text = it.content

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