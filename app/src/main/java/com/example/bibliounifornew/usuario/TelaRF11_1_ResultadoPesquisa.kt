package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
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
        setContentView(R.layout.telarf11_1_resultado_pesquisa) // Certifique-se que existe um RecyclerView neste XML

        val termoPesquisa = intent.getStringExtra("TERMO_PESQUISA") ?: ""

        configurarRecyclerView()

        if (termoPesquisa.isNotEmpty()) {
            realizarBusca(termoPesquisa)
        }
    }

    private fun configurarRecyclerView() {
        // AJUSTE PARA O ID DO RECYCLERVIEW DA TELA DE RESULTADOS
        recyclerView = findViewById(R.id.recyclerViewResultados)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = LivroAdapter(emptyList()) { livroSelecionado ->
            val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            intent.putExtra("LIVRO_ID", livroSelecionado.id)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
    }

    private fun realizarBusca(termo: String) {
        // Aqui chamamos a ViewModel, que vai varrer o Room buscando títulos, autores ou ISBNs!
        lifecycleScope.launch {
            viewModel.pesquisarLivros(termo).collect { livrosEncontrados ->
                adapter.updateData(livrosEncontrados)
            }
        }
    }
}