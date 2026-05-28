package com.example.bibliounifornew.features.usuario.livro

import android.content.Intent
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.BibliotecaOnlineRepository
import com.example.bibliounifornew.data.EntidadeLivro
import com.example.bibliounifornew.features.usuario.solicitacao.TelaRF18StatusAluguel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
        val filtroTitulo  = intent.getStringExtra("FILTRO_TITULO") ?: ""
        val filtroAutor   = intent.getStringExtra("FILTRO_AUTOR") ?: ""
        val filtroCat     = intent.getStringExtra("FILTRO_CATEGORIA") ?: ""
        val filtroDisp    = intent.getStringExtra("FILTRO_DISPONIBILIDADE") ?: "Todos"

        val textResultado = findViewById<TextView>(R.id.textResultadoTitulo)
        textResultado.text = "Resultado: \"${termoPesquisa.ifEmpty { "Filtros aplicados" }}\""

        configurarRecyclerView()

        // Realiza a busca combinada
        realizarBusca(termoPesquisa, filtroTitulo, filtroAutor, filtroCat, filtroDisp)

        // Se tem um termo, busca na nuvem. 
        // Se não tem termo mas tem uma categoria específica, busca livros dessa categoria na nuvem.
        if (termoPesquisa.isNotEmpty()) {
            buscarNaNuvem(termoPesquisa)
        } else {
            val catTodos = getString(R.string.categoria_todos)
            if (filtroCat.isNotEmpty() && filtroCat != catTodos && filtroCat != "Todas as Categorias") {
                buscarNaNuvem(filtroCat)
            }
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
            onAlugarLivro = { livro -> exibirPopupAlugar(livro) }
        )

        recyclerView.adapter = adapter
    }

    private fun adicionarSuaLivraria(livro: EntidadeLivro) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, getString(R.string.erro_login_para_livraria), Toast.LENGTH_SHORT).show()
            return
        }
        val dados = hashMapOf(
            "usuarioId"     to uid,
            "livroId"       to livro.id,
            "titulo"        to livro.title,
            "autor"         to livro.author,
            "coverUrl"      to livro.coverUrl,
            "statusLeitura" to "Não Lido",
            "adicionadoEm"  to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance()
            .collection("biblioteca_usuarios")
            .document("${uid}_${livro.id}")
            .set(dados, SetOptions.merge())
            .addOnSuccessListener {
                val snackbar = Snackbar.make(recyclerView, "Livro adicionado à sua livraria.", Snackbar.LENGTH_LONG)
                snackbar.setBackgroundTint(Color.parseColor("#444444"))
                snackbar.setTextColor(Color.WHITE)
                snackbar.show()
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.erro_adicionar_livraria_pesquisa), Toast.LENGTH_SHORT).show()
            }
    }

    private fun exibirPopupAlugar(livro: EntidadeLivro) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_alugar_livro)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val txtTitulo = dialog.findViewById<TextView>(R.id.textTituloPopupAlugar)
        txtTitulo.text = "Você deseja alugar o livro\n\"${livro.title}\"?"

        val btnAlugar = dialog.findViewById<Button>(R.id.buttonAdicionarLivro)
        val btnCancelar = dialog.findViewById<TextView>(R.id.textCancelarPopup)

        btnAlugar.setOnClickListener {
            dialog.dismiss()
            confirmarAluguel(livro)
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun confirmarAluguel(livro: EntidadeLivro) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        val solicitacao = hashMapOf(
            "uidAluno" to uid,
            "idLivro" to livro.id,
            "tituloLivro" to livro.title,
            "autorLivro" to livro.author,
            "coverUrl" to livro.coverUrl,
            "dataSolicitacao" to System.currentTimeMillis(),
            "status" to "pendente"
        )

        FirebaseFirestore.getInstance()
            .collection("solicitacoes_emprestimo")
            .add(solicitacao)
            .addOnSuccessListener {
                exibirPopupSucessoAluguel()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao solicitar aluguel.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun exibirPopupSucessoAluguel() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_livro_adicionado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnVerMeusLivros = dialog.findViewById<Button>(R.id.buttonVerMeusLivros)
        btnVerMeusLivros.setOnClickListener {
            dialog.dismiss()
            val intent = android.content.Intent(this, TelaRF18StatusAluguel::class.java)
            startActivity(intent)
            finish()
        }

        dialog.show()
    }

    private fun realizarBusca(
        termo: String,
        fTitulo: String = "",
        fAutor: String = "",
        fCat: String = "",
        fDisp: String = "Todos"
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val termoLower = termo.lowercase().trim()
        val titLower = fTitulo.lowercase().trim()
        val autLower = fAutor.lowercase().trim()
        
        val catTodos = try { getString(R.string.categoria_todos) } catch (e: Exception) { "Todas as Categorias" }
        val filtroCatTratado = if (fCat.isEmpty()) catTodos else fCat
        val ignorarCat = filtroCatTratado.equals(catTodos, ignoreCase = true) || 
                         filtroCatTratado.equals("Todas as Categorias", ignoreCase = true) ||
                         filtroCatTratado.equals("Todas", ignoreCase = true)

        firestore.collection("livros")
            .get()
            .addOnSuccessListener { result ->
                val listaDeLivros = mutableListOf<EntidadeLivro>()
                Log.d("BUSCA", "Firestore retornou ${result.size()} documentos. Termo: '$termoLower', Cat: '$filtroCatTratado'")

                for (document in result) {
                    try {
                        val id = document.id
                        val title = (document.getString("title") ?: document.getString("titulo") ?: "").trim()
                        val author = (document.getString("author") ?: document.getString("autor") ?: "").trim()
                        val category = (document.getString("category") ?: document.getString("categoria") ?: "Outros").trim()
                        val coverUrl = document.getString("coverUrl") ?: ""
                        val isbn10 = document.getString("isbn10") ?: ""
                        val isbn13 = document.getString("isbn13") ?: ""
                        val publishDate = document.getString("publishDate") ?: document.getString("data") ?: ""
                        
                        val stockVal = document.get("estoque") ?: document.get("quantidade") ?: document.get("stock")
                        val stock = when(stockVal) {
                            is Long -> stockVal
                            is Int -> stockVal.toLong()
                            is String -> stockVal.toLongOrNull()
                            else -> null
                        }
                        
                        val isAvailable = document.getBoolean("isAvailable") 
                            ?: (if (stock != null) stock > 0 else true)

                        // 1. Busca Principal (Título, Autor ou ISBN)
                        val matchTermo = termoLower.isEmpty() ||
                                title.lowercase().contains(termoLower) ||
                                author.lowercase().contains(termoLower) ||
                                isbn10.lowercase().contains(termoLower) ||
                                isbn13.lowercase().contains(termoLower)

                        // 2. Filtros Adicionais
                        val matchTitulo = titLower.isEmpty() || title.lowercase().contains(titLower)
                        val matchAutor = autLower.isEmpty() || author.lowercase().contains(autLower)
                        
                        val catLower = category.lowercase()
                        val fCatLower = filtroCatTratado.lowercase()
                        
                        val matchCatDirect = catLower.contains(fCatLower)
                        val matchCatSynonym = when(fCatLower) {
                            "tecnologia" -> {
                                val sinomimos = listOf(
                                    "tecnologia", "programação", "computação", "computer science", "software", "ti", "desenvolvimento", 
                                    "programming", "computing", "informática", "web", "android", "ios", "java", "python", "javascript", 
                                    "cloud", "aws", "azure", "docker", "agile", "scrum", "devops", "segurança", "cybersecurity", 
                                    "hacking", "banco de dados", "sql", "nosql", "artificial intelligence", "inteligência artificial", 
                                    "ai", "ia", "machine learning", "frontend", "backend", "fullstack", "data science", "hardware", 
                                    "internet", "digital", "algoritmos", "coding", "networks", "redes"
                                )
                                sinomimos.any { catLower.contains(it) }
                            }
                            "fantasia" -> {
                                val sinomimos = listOf(
                                    "fantasia", "fantasia épica", "fantasia juvenil", "ficção científica", "science fiction", "sci-fi", 
                                    "distopia", "fantasy", "magic", "magia", "dragons", "dragões", "bruxaria", "witchcraft", "vampiro", 
                                    "lobisomem", "zumbi", "mitologia", "mythology", "steampunk", "cyberpunk", "space opera", "alien", 
                                    "universo", "medieval", "espada", "feitiçaria"
                                )
                                sinomimos.any { catLower.contains(it) }
                            }
                            "suspense" -> {
                                val sinomimos = listOf(
                                    "suspense", "thriller", "mistério", "mistério policial", "crime", "mystery", "police", "terror", 
                                    "horror", "investigação", "detetive", "noir", "psicológico", "spy", "espionagem", "assassinato", 
                                    "murder", "true crime", "sobrenatural", "paranormal"
                                )
                                sinomimos.any { catLower.contains(it) }
                            }
                            "romance" -> {
                                val sinomimos = listOf(
                                    "romance", "drama", "romance histórico", "romance contemporâneo", "love", "romantic", "amor", 
                                    "paixão", "comédia romântica", "rom-com", "new adult", "young adult", "ya", "erótico", "sentimental"
                                )
                                sinomimos.any { catLower.contains(it) }
                            }
                            "literatura" -> {
                                val sinomimos = listOf("fiction", "literature", "poetry", "literatura", "clássico", "classic", "contos", "short stories", "prosa", "antologia")
                                sinomimos.any { catLower.contains(it) }
                            }
                            "ciência", "ciencia" -> {
                                val sinomimos = listOf(
                                    "science", "nature", "math", "ciência", "biologia", "física", "química", "astronomia", "matemática", 
                                    "physics", "chemistry", "biology", "astronomy", "mathematics", "pesquisa", "research", "evolução", "evolution"
                                )
                                sinomimos.any { catLower.contains(it) }
                            }
                            "história", "historia" -> {
                                val sinomimos = listOf("history", "história", "arqueologia", "archaeology", "guerra", "war", "civilização", "biografia histórica")
                                sinomimos.any { catLower.contains(it) }
                            }
                            "biografia"  -> {
                                val sinomimos = listOf("biography", "autobiography", "biografia", "memoir", "memórias", "vida de")
                                sinomimos.any { catLower.contains(it) }
                            }
                            "outros"     -> true
                            else -> false
                        }
                        
                        val matchCat = ignorarCat || matchCatDirect || matchCatSynonym
                        
                        val matchDisp = when (fDisp) {
                            "Disponível" -> isAvailable
                            "Indisponível" -> !isAvailable
                            else -> true
                        }

                        if (matchTermo && matchTitulo && matchAutor && matchCat && matchDisp) {
                            listaDeLivros.add(EntidadeLivro(
                                id = id, title = title, author = author, category = category,
                                coverUrl = coverUrl, isbn10 = isbn10, isbn13 = isbn13,
                                isAvailable = isAvailable, publishDate = publishDate
                            ))
                        }
                    } catch (e: Exception) {
                        Log.e("BUSCA", "Erro ao processar livro ${document.id}", e)
                    }
                }

                adapter.updateData(listaDeLivros)
                Log.d("BUSCA", "Total exibido após filtros: ${listaDeLivros.size}")
            }
            .addOnFailureListener { e ->
                Log.e("BUSCA", "Erro Firestore", e)
            }
    }

    private fun buscarNaNuvem(termoParaApi: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val repository = BibliotecaOnlineRepository()
                repository.buscarEImportarLivro(
                    termoDeBusca = termoParaApi,
                    onSuccess = {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(this@TelaRF11_1_ResultadoPesquisa, getString(R.string.msg_buscando_novidades), Toast.LENGTH_SHORT).show()
                            delay(1000)
                            
                            val originalTermo = intent.getStringExtra("TERMO_PESQUISA") ?: ""
                            val fTitulo  = intent.getStringExtra("FILTRO_TITULO") ?: ""
                            val fAutor   = intent.getStringExtra("FILTRO_AUTOR") ?: ""
                            val fCat     = intent.getStringExtra("FILTRO_CATEGORIA") ?: ""
                            val fDisp    = intent.getStringExtra("FILTRO_DISPONIBILIDADE") ?: "Todos"
                            
                            realizarBusca(originalTermo, fTitulo, fAutor, fCat, fDisp)
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
