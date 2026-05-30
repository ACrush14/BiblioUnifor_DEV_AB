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
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TelaRF11_1_ResultadoPesquisa : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LivroAdapter
    private var activeDialog: Dialog? = null

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
        if (isFinishing || isDestroyed) return
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
                if (!isFinishing && !isDestroyed) {
                    val snackbar = Snackbar.make(recyclerView, "Livro adicionado à sua livraria.", Snackbar.LENGTH_LONG)
                    snackbar.setBackgroundTint(Color.parseColor("#444444"))
                    snackbar.setTextColor(Color.WHITE)
                    snackbar.show()
                }
            }
            .addOnFailureListener {
                if (!isFinishing && !isDestroyed) {
                    Toast.makeText(this, getString(R.string.erro_adicionar_livraria_pesquisa), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun exibirPopupAlugar(livro: EntidadeLivro) {
        if (isFinishing || isDestroyed) return
        activeDialog?.dismiss()

        activeDialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.popup_alugar_livro)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            findViewById<TextView>(R.id.textTituloPopupAlugar)?.text = "Você deseja alugar o livro\n\"${livro.title}\"?"

            findViewById<Button>(R.id.buttonAdicionarLivro)?.setOnClickListener {
                dismiss()
                confirmarAluguel(livro)
            }

            findViewById<TextView>(R.id.textCancelarPopup)?.setOnClickListener {
                dismiss()
            }
            show()
        }
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
                if (!isFinishing && !isDestroyed) {
                    exibirPopupSucessoAluguel()
                }
            }
            .addOnFailureListener {
                if (!isFinishing && !isDestroyed) {
                    Toast.makeText(this, "Erro ao solicitar aluguel.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun exibirPopupSucessoAluguel() {
        if (isFinishing || isDestroyed) return
        activeDialog?.dismiss()

        activeDialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.popup_livro_adicionado)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            findViewById<Button>(R.id.buttonVerMeusLivros)?.setOnClickListener {
                dismiss()
                val intent = android.content.Intent(this@TelaRF11_1_ResultadoPesquisa, TelaRF18StatusAluguel::class.java)
                startActivity(intent)
                finish()
            }
            show()
        }
    }

    override fun onDestroy() {
        activeDialog?.dismiss()
        activeDialog = null
        super.onDestroy()
    }

    private fun realizarBusca(
        termo: String,
        fTitulo: String = "",
        fAutor: String = "",
        fCat: String = "",
        fDisp: String = "Todos"
    ) {
        // getString() precisa da Main thread — captura ANTES de entrar na coroutine.
        val termoLower = termo.lowercase().trim()
        val titLower   = fTitulo.lowercase().trim()
        val autLower   = fAutor.lowercase().trim()

        val catTodos = try { getString(R.string.categoria_todos) } catch (_: Exception) { "Todas as Categorias" }
        val filtroCatTratado = if (fCat.isEmpty()) catTodos else fCat
        val ignorarCat = fCat.isEmpty()
            || filtroCatTratado.equals(catTodos, ignoreCase = true)
            || filtroCatTratado.equals("Todas as Categorias", ignoreCase = true)
            || filtroCatTratado.equals("Todas", ignoreCase = true)
            || filtroCatTratado.equals("Todos", ignoreCase = true)

        // Issue #7 FIX: move o Firestore get() + todo o loop de filtragem para IO.
        // O addOnSuccessListener rodava na Main thread — com 150+ docs e comparações
        // de sinônimos isso bloqueava a UI e fazia os filtros parecerem inoperantes.
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = FirebaseFirestore.getInstance()
                    .collection("livros")
                    .limit(150)
                    .get()
                    .await()

                Log.d("BUSCA", "Firestore retornou ${result.size()} docs. Termo: '$termoLower', Cat: '$filtroCatTratado', Disp: '$fDisp'")

                val listaDeLivros = mutableListOf<EntidadeLivro>()

                for (document in result) {
                    try {
                        val id          = document.id
                        val title       = (document.getString("title")    ?: document.getString("titulo")    ?: "").trim()
                        val author      = (document.getString("author")   ?: document.getString("autor")     ?: "").trim()
                        val category    = (document.getString("category") ?: document.getString("categoria") ?: "Outros").trim()
                        val coverUrl    = document.getString("coverUrl")    ?: ""
                        val isbn10      = document.getString("isbn10")      ?: ""
                        val isbn13      = document.getString("isbn13")      ?: ""
                        val publishDate = document.getString("publishDate") ?: document.getString("data") ?: ""

                        val stockVal = document.get("estoque") ?: document.get("quantidade") ?: document.get("stock")
                        val stock = when (stockVal) {
                            is Long   -> stockVal
                            is Int    -> stockVal.toLong()
                            is String -> stockVal.toLongOrNull()
                            else      -> null
                        }
                        val isAvailable = document.getBoolean("isAvailable")
                            ?: (if (stock != null) stock > 0 else true)

                        // ── Cadeia AND cumulativa (Issue #7) ──────────────────────
                        // 1. Termo principal (título, autor ou ISBN)
                        val matchTermo = termoLower.isEmpty()
                            || title.contains(termoLower, ignoreCase = true)
                            || author.contains(termoLower, ignoreCase = true)
                            || isbn10.contains(termoLower, ignoreCase = true)
                            || isbn13.contains(termoLower, ignoreCase = true)

                        // 2. Filtro de título avançado
                        val matchTitulo = titLower.isEmpty() || title.contains(titLower, ignoreCase = true)

                        // 3. Filtro de autor avançado
                        val matchAutor  = autLower.isEmpty() || author.contains(autLower, ignoreCase = true)

                        // 4. Filtro de categoria (direto + sinônimos)
                        val catLower  = category.lowercase()
                        val fCatLower = filtroCatTratado.lowercase()

                        val matchCatDirect  = category.contains(filtroCatTratado, ignoreCase = true)
                        val matchCatSynonym = when (fCatLower) {
                            "tecnologia" -> {
                                val s = listOf(
                                    "tecnologia", "programação", "computação", "computer science", "software", "ti", "desenvolvimento",
                                    "programming", "computing", "informática", "web", "android", "ios", "java", "python", "javascript",
                                    "cloud", "aws", "azure", "docker", "agile", "scrum", "devops", "segurança", "cybersecurity",
                                    "hacking", "banco de dados", "sql", "nosql", "artificial intelligence", "inteligência artificial",
                                    "ai", "ia", "machine learning", "frontend", "backend", "fullstack", "data science", "hardware",
                                    "internet", "digital", "algoritmos", "coding", "networks", "redes"
                                )
                                s.any { catLower.contains(it) }
                            }
                            "fantasia" -> {
                                val s = listOf(
                                    "fantasia", "fantasia épica", "fantasia juvenil", "ficção científica", "science fiction", "sci-fi",
                                    "distopia", "fantasy", "magic", "magia", "dragons", "dragões", "bruxaria", "witchcraft", "vampiro",
                                    "lobisomem", "zumbi", "mitologia", "mythology", "steampunk", "cyberpunk", "space opera", "alien",
                                    "universo", "medieval", "espada", "feitiçaria"
                                )
                                s.any { catLower.contains(it) }
                            }
                            "suspense" -> {
                                val s = listOf(
                                    "suspense", "thriller", "mistério", "mistério policial", "crime", "mystery", "police", "terror",
                                    "horror", "investigação", "detetive", "noir", "psicológico", "spy", "espionagem", "assassinato",
                                    "murder", "true crime", "sobrenatural", "paranormal"
                                )
                                s.any { catLower.contains(it) }
                            }
                            "romance" -> {
                                val s = listOf(
                                    "romance", "drama", "romance histórico", "romance contemporâneo", "love", "romantic", "amor",
                                    "paixão", "comédia romântica", "rom-com", "new adult", "young adult", "ya", "erótico", "sentimental"
                                )
                                s.any { catLower.contains(it) }
                            }
                            "literatura" -> {
                                val s = listOf("fiction", "literature", "poetry", "literatura", "clássico", "classic", "contos", "short stories", "prosa", "antologia")
                                s.any { catLower.contains(it) }
                            }
                            "ciência", "ciencia" -> {
                                val s = listOf(
                                    "science", "nature", "math", "ciência", "biologia", "física", "química", "astronomia", "matemática",
                                    "physics", "chemistry", "biology", "astronomy", "mathematics", "pesquisa", "research", "evolução", "evolution"
                                )
                                s.any { catLower.contains(it) }
                            }
                            "história", "historia" -> {
                                val s = listOf("history", "história", "arqueologia", "archaeology", "guerra", "war", "civilização", "biografia histórica")
                                s.any { catLower.contains(it) }
                            }
                            "biografia" -> {
                                val s = listOf("biography", "autobiography", "biografia", "memoir", "memórias", "vida de")
                                s.any { catLower.contains(it) }
                            }
                            "outros" -> true
                            else     -> false
                        }

                        val matchCat = ignorarCat || matchCatDirect || matchCatSynonym

                        // 5. Filtro de disponibilidade
                        val matchDisp = when (fDisp) {
                            "Disponível"   -> isAvailable
                            "Indisponível" -> !isAvailable
                            else           -> true
                        }

                        // Inclui somente se TODOS os filtros forem satisfeitos (AND cumulativo)
                        if (matchTermo && matchTitulo && matchAutor && matchCat && matchDisp) {
                            listaDeLivros.add(
                                EntidadeLivro(
                                    id          = id,
                                    title       = title,
                                    author      = author,
                                    category    = category,
                                    coverUrl    = coverUrl,
                                    isbn10      = isbn10,
                                    isbn13      = isbn13,
                                    isAvailable = isAvailable,
                                    publishDate = publishDate
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("BUSCA", "Erro ao processar livro ${document.id}", e)
                    }
                }

                // Atualiza o adapter exclusivamente na Main thread
                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    adapter.updateData(listaDeLivros)
                    Log.d("BUSCA", "Total exibido após filtros: ${listaDeLivros.size}")
                }

            } catch (e: Exception) {
                Log.e("BuscaLivros", "Erro: ", e)
            }
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
                            if (isFinishing || isDestroyed) return@launch
                            Toast.makeText(this@TelaRF11_1_ResultadoPesquisa, getString(R.string.msg_buscando_novidades), Toast.LENGTH_SHORT).show()
                            delay(1000)
                            
                            if (isFinishing || isDestroyed) return@launch
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
