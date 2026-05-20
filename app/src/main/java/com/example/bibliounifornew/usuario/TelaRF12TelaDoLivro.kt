package com.example.bibliounifornew.usuario

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.data.AppDatabase
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class TelaRF12TelaDoLivro : AppCompatActivity() {

    private val database by lazy { AppDatabase.getDatabase(this@TelaRF12TelaDoLivro) }
    private val libroDao by lazy { database.livroDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.telarf12_teladolivro)

        val context: Context = this@TelaRF12TelaDoLivro
        val livroId = intent.getStringExtra("LIVRO_ID")

        if (livroId != null) {
            carregarDadosDoLivro(livroId)
        }

        // 1. Botão Lista de Desejos
        findViewById<Button>(R.id.buttonListaDesejos).setOnClickListener {
            Toast.makeText(context, "Livro adicionado à lista de desejos", Toast.LENGTH_SHORT).show()
        }

        // 2. Botão Sua Livraria
        findViewById<Button>(R.id.buttonSuaLivraria).setOnClickListener {
            Toast.makeText(context, "Livro adicionado à sua livraria", Toast.LENGTH_SHORT).show()
        }

        // 3. Status de Leitura (Toggle)
        val btnNaoLido = findViewById<MaterialButton>(R.id.buttonNaoLido)
        val btnLendo = findViewById<MaterialButton>(R.id.buttonLendo)
        val btnLido = findViewById<MaterialButton>(R.id.buttonLido)

        fun atualizarBotoesLeitura(selecionado: MaterialButton) {
            val corAtiva = Color.parseColor("#B3D7FF") // Mais escuro
            val corInativa = Color.parseColor("#F0F7FF") // Cor normal/clara

            btnNaoLido.backgroundTintList = ColorStateList.valueOf(corInativa)
            btnLendo.backgroundTintList = ColorStateList.valueOf(corInativa)
            btnLido.backgroundTintList = ColorStateList.valueOf(corInativa)

            selecionado.backgroundTintList = ColorStateList.valueOf(corAtiva)
        }

        btnNaoLido.setOnClickListener { atualizarBotoesLeitura(btnNaoLido) }
        btnLendo.setOnClickListener { atualizarBotoesLeitura(btnLendo) }
        btnLido.setOnClickListener { atualizarBotoesLeitura(btnLido) }

        // 4. Botão Solicitar -> TelaRF19Solicitacoes
        findViewById<Button>(R.id.buttonSolicitar).setOnClickListener {
            val intentSolicitar = Intent(context, TelaRF19Solicitacoes::class.java)
            intentSolicitar.putExtra("LIVRO_ID", livroId)
            startActivity(intentSolicitar)
        }

        // 5. Botão Ver Mais -> TelaRF13VerMaisLivro
        findViewById<Button>(R.id.buttonVerMais).setOnClickListener {
            val intentVerMais = Intent(context, TelaRF13VerMaisLivro::class.java)
            intentVerMais.putExtra("LIVRO_ID", livroId)
            startActivity(intentVerMais)
        }

        // 6. Botão LER -> TelaRF14LeituraActivity
        findViewById<Button>(R.id.buttonLer).setOnClickListener {
            val intentLer = Intent(context, TelaRF14LeituraActivity::class.java)
            intentLer.putExtra("LIVRO_ID", livroId)
            startActivity(intentLer)
        }
    }

    private fun carregarDadosDoLivro(id: String) {
        lifecycleScope.launch {
            val livro = libroDao.buscarLivroPorId(id)
            livro?.let {
                findViewById<TextView>(R.id.textTituloLivro).text = it.title
                findViewById<TextView>(R.id.textAutorLivro).text = it.author
                findViewById<TextView>(R.id.textSobreLivro).text = it.content

                val imgCapa = findViewById<ImageView>(R.id.imageLivroDetalhes)
                // Usando placeholder ou imagem real se disponível
                imgCapa.setImageResource(R.drawable.o_alienista_capa)
            }
        }
    }
}