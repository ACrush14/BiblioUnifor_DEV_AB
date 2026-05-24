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

        adapter = LivroAdapter(
            livros        = mutableListOf(),
            onItemClick   = { livro ->
                startActivity(
                    Intent(this, TelaRF12TelaDoLivro::class.java)
                        .putExtra("LIVRO_ID", livro.id)
                )
            },
            onSuaLivraria = { livro -> adicionarSuaLivraria(livro) },
            onAlugarLivro = { livro ->
                startActivity(
                    Intent(this, TelaRF14LeituraActivity::class.java)
                        .putExtra("LIVRO_ID", livro.id)
                )
            }
        )

        recyclerView.adapter = adapter
    }

    private fun adicionarSuaLivraria(livro: com.example.bibliounifornew.data.EntidadeLivro) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Faça login para usar esta função.", Toast.LENGTH_SHORT).show()
            return
        }
        val dados = hashMapOf(
            "usuarioId"     to uid,
            "livroId"       to livro.id,
            "titulo"        to livro.title,
            "autor"         to livro.author,
            "statusLeitura" to "Não Lido",
            "adicionadoEm"  to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance()
            .collection("biblioteca_usuarios")
            .document("${uid}_${livro.id}")
            .set(dados, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "\"${livro.title}\" adicionado à sua Livraria!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Não foi possível adicionar à Livraria.", Toast.LENGTH_SHORT).show()
            }
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
