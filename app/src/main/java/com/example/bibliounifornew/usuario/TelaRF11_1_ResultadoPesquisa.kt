package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.LivroAdapter
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.BibliotecaOnlineRepository
import com.example.bibliounifornew.data.EntidadeLivro
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF11_1_ResultadoPesquisa : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LivroAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf11_1_resultado_pesquisa)

        val termoPesquisa = intent.getStringExtra("TERMO_PESQUISA") ?: ""
        val textResultado = findViewById<TextView>(R.id.textResultadoTitulo)

        textResultado.text = "Resultado: \"$termoPesquisa\""

        configurarRecyclerView()

        if (termoPesquisa.isNotEmpty()) {
            // 1. Faz a busca no que já existe no Firebase localmente (cache/firestore)
            realizarBusca(termoPesquisa)
            
            // 2. Tenta buscar no Google Books e importar novos livros
            buscarNaNuvem(termoPesquisa)
        }
    }

    private fun configurarRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewResultados)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = LivroAdapter(mutableListOf()) { livroSelecionado ->
            val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            intent.putExtra("LIVRO_ID", livroSelecionado.id)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
    }

    private fun realizarBusca(termo: String) {
        val firestore = FirebaseFirestore.getInstance()
        val termoLower = termo.lowercase().trim()

        firestore.collection("livros")
            .get()
            .addOnSuccessListener { result ->
                val listaDeLivros = mutableListOf<EntidadeLivro>()

                for (document in result) {
                    val titulo = document.getString("titulo") ?: ""
                    val autor = document.getString("autor") ?: ""
                    val descricao = document.getString("descricao") ?: ""

                    // Filtro para encontrar o termo no título, autor ou descrição
                    if (titulo.lowercase().contains(termoLower) ||
                        autor.lowercase().contains(termoLower) ||
                        descricao.lowercase().contains(termoLower)) {

                        val livro = EntidadeLivro(
                            id = document.id,
                            title = titulo,
                            author = autor,
                            coverUrl = document.getString("coverUrl") ?: ""
                        )
                        listaDeLivros.add(livro)
                    }
                }

                // Atualiza a lista na tela
                adapter.updateData(listaDeLivros)
                Log.d("BUSCA", "Exibindo ${listaDeLivros.size} resultados para '$termo'")
            }
            .addOnFailureListener { e ->
                Log.e("BUSCA", "Erro ao acessar Firestore", e)
            }
    }

    private fun buscarNaNuvem(termo: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val repository = BibliotecaOnlineRepository()
                repository.buscarEImportarLivro(
                    termoDeBusca = termo,
                    onSuccess = {
                        // Quando a API do Google terminar de salvar no Firebase
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(this@TelaRF11_1_ResultadoPesquisa, "Buscando novidades...", Toast.LENGTH_SHORT).show()
                            // Aguarda um instante para o Firestore indexar e atualiza a lista
                            delay(1000)
                            realizarBusca(termo)
                        }
                    },
                    onFailure = { erro ->
                        Log.d("API_LIVROS", "Google Books finalizado: ${erro.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e("API_LIVROS", "Erro na busca online", e)
            }
        }
    }
}
