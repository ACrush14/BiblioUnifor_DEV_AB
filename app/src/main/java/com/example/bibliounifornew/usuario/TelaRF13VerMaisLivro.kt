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
        // Explicitly using this@TelaRF13VerMaisLivro to help the IDE resolve Context methods
        this@TelaRF13VerMaisLivro.setContentView(R.layout.telarf13_telavermaislivro)

        val livroId = intent.getIntExtra("LIVRO_ID", -1)
        if (livroId != -1) {
            carregarDadosDetalhados(livroId)
        }
    }

    private fun carregarDadosDetalhados(id: Int) {
        lifecycleScope.launch {
            val livro = libroDao.buscarLivroPorId(id)
            livro?.let {
                findViewById<TextView>(R.id.textTituloLivroInfo).text = it.title
                findViewById<TextView>(R.id.textAutorLivroInfo).text = it.author
                findViewById<TextView>(R.id.textDescricaoLivro).text = it.content
                
                val imgCapa = findViewById<ImageView>(R.id.imageLivroInfo)
                if (it.coverResourceId != 0) {
                    imgCapa.setImageResource(it.coverResourceId)
                }
            }
        }
    }
}
