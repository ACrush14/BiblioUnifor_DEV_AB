package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.LivroAdapter
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AppDatabase
import com.example.bibliounifornew.data.BibliotecaOnlineRepository
import com.example.bibliounifornew.data.LivroRepository
import com.example.bibliounifornew.viewmodel.LivroViewModel
import com.example.bibliounifornew.viewmodel.LivroViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.bibliounifornew.data.EntidadeLivro

class TelaRF11_1_ResultadoPesquisa : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LivroAdapter

    // Inicializa o ViewModel para a busca local
    private val viewModel: LivroViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = LivroRepository(database.livroDao(), FirebaseFirestore.getInstance())
        LivroViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf11_1_resultado_pesquisa)

        val termoPesquisa = intent.getStringExtra("TERMO_PESQUISA") ?: ""
        val textResultado = findViewById<TextView>(R.id.textResultadoTitulo)

        textResultado.text = "Resultado: \"$termoPesquisa\""

        configurarRecyclerView()

        if (termoPesquisa.isNotEmpty()) {
            // 1. Busca no banco local imediatamente
            realizarBusca(termoPesquisa)
            // 2. Dispara a busca no Google Books em segundo plano
            buscarNaNuvem(termoPesquisa)
        }
    }

    private fun configurarRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewResultados)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // A MÁGICA AQUI: emptyList<EntidadeLivro>()
        adapter = LivroAdapter(emptyList<EntidadeLivro>()) { livroSelecionado ->
            val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            intent.putExtra("LIVRO_ID", livroSelecionado.id)
            startActivity(intent)
        }

        // ANEXA O ADAPTER À TELA
        recyclerView.adapter = adapter
    }

    private fun realizarBusca(termo: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Vamos direto no banco online buscar qualquer livro que o título contenha o termo
        firestore.collection("livros")
            .get()
            .addOnSuccessListener { result ->
                val listaDeLivros = mutableListOf<EntidadeLivro>()

                for (document in result) {
                    val titulo = document.getString("titulo") ?: ""

                    // Filtra na mão: se o título (em minúsculas) contém o termo (em minúsculas)
                    if (titulo.lowercase().contains(termo.lowercase())) {

                        // Transforma o documento do Firebase no molde EntidadeLivro
                        val livro = EntidadeLivro(
                            id = document.id,
                            title = titulo,
                            author = document.getString("autor") ?: "Desconhecido",
                            coverUrl = document.getString("coverUrl") ?: ""
                        )
                        listaDeLivros.add(livro)
                    }
                }

                // Manda o adapter desenhar a lista fresquinha que acabou de vir da internet!
                adapter.updateData(listaDeLivros)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar lista do banco.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun buscarNaNuvem(termo: String) {
        Toast.makeText(this, "Buscando '$termo' no Google Books...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val repository = BibliotecaOnlineRepository()
            repository.buscarEImportarLivro(
                termoDeBusca = termo,
                onSuccess = {
                    Toast.makeText(this@TelaRF11_1_ResultadoPesquisa, "Novo livro importado com sucesso!", Toast.LENGTH_SHORT).show()

                    // Dá tempo para o Firebase sincronizar e manda a tela desenhar o livro novo
                    lifecycleScope.launch {
                        delay(1000)
                        realizarBusca(termo)
                    }
                },
                onFailure = { erro ->
                    Log.d("API_LIVROS", "Nenhum livro novo encontrado na internet: ${erro.message}")
                }
            )
        }
    }
}