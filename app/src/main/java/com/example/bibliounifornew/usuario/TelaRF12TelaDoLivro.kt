package com.example.bibliounifornew.usuario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.data.AppDatabase
import com.example.bibliounifornew.R
import kotlinx.coroutines.launch

class TelaRF12TelaDoLivro : AppCompatActivity() {

    private val database by lazy { AppDatabase.getDatabase(this@TelaRF12TelaDoLivro) }
    private val libroDao by lazy { database.livroDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.telarf12_teladolivro)

        val context: Context = this@TelaRF12TelaDoLivro
        // CORREÇÃO: Pegando o ID como String em vez de Int
        val livroId = intent.getStringExtra("LIVRO_ID")

        // CORREÇÃO: Verificando se a String não é nula
        if (livroId != null) {
            carregarDadosDoLivro(livroId)
        }

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

    // CORREÇÃO: O parâmetro id agora é uma String
    private fun carregarDadosDoLivro(id: String) {
        lifecycleScope.launch {
            val livro = libroDao.buscarLivroPorId(id)
            livro?.let {
                findViewById<TextView>(R.id.textTituloLivro).text = it.title
                findViewById<TextView>(R.id.textAutorLivro).text = it.author
                findViewById<TextView>(R.id.textSobreLivro).text = it.content

                val imgCapa = findViewById<ImageView>(R.id.imageLivroDetalhes)
                if (it.coverUrl.isNotEmpty()) {
                    imgCapa.setImageResource(R.drawable.osda)
                } else {
                    imgCapa.setImageResource(R.drawable.osda)
                }
            }
        }
    }
}