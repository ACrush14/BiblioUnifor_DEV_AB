package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.LivroAdapter
import com.example.bibliounifornew.data.AppDatabase
import com.example.bibliounifornew.data.LivroRepository
import com.example.bibliounifornew.viewmodel.LivroViewModel
import com.example.bibliounifornew.viewmodel.LivroViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.Toast
import android.util.Log
import com.example.bibliounifornew.data.BibliotecaOnlineRepository

class TelaRF11_1_ResultadoPesquisa : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LivroAdapter

    private val viewModel: LivroViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = LivroRepository(database.livroDao(), FirebaseFirestore.getInstance())
        LivroViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf11_1_resultado_pesquisa)

        val termoPesquisa = intent.getStringExtra("TERMO_PESQUISA") ?: ""

        // Atualiza o título com o termo pesquisado
        val textResultado = findViewById<TextView>(R.id.textResultadoTitulo)
        textResultado.text = "Resultado: \"$termoPesquisa\""

        configurarRecyclerView()

        if (termoPesquisa.isNotEmpty()) {
            realizarBusca(termoPesquisa) // 1. Busca no seu banco local
            buscarNaNuvem(termoPesquisa) // 2. Busca no Google Books simultaneamente
        }
    }

    private fun configurarRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewResultados)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = LivroAdapter(emptyList()) { livroSelecionado ->
            // Navegação para a tela de detalhes do livro
            val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            intent.putExtra("LIVRO_ID", livroSelecionado.id)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
    }

    private fun realizarBusca(termo: String) {
        lifecycleScope.launch {
            viewModel.pesquisarLivros(termo).collect { livrosEncontrados ->
                adapter.updateData(livrosEncontrados)
            }
        }
    }

    private fun buscarNaNuvem(termo: String) {
        // Avisa o usuário que está buscando fora
        Toast.makeText(this, "Buscando '$termo' no Google Books...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val repository = BibliotecaOnlineRepository()
            repository.buscarEImportarLivro(
                termoDeBusca = termo,
                onSuccess = {
                    Toast.makeText(this@TelaRF11_1_ResultadoPesquisa, "Novo livro importado com sucesso!", Toast.LENGTH_SHORT).show()

                    // Recarrega a lista para o livro novo aparecer na hora para o usuário!
                    realizarBusca(termo)
                },
                onFailure = { erro ->
                    // Se não achar nada online, apenas ignora silenciosamente ou avisa no Log
                    Log.d("API_LIVROS", "Nenhum livro novo encontrado na internet: ${erro.message}")
                }
            )
        }
    }
}