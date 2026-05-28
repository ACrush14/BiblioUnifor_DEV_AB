package com.example.bibliounifornew.features.usuario.livro

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AppDatabase
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.EntidadeLivro
import com.example.bibliounifornew.data.LivroDao
import com.example.bibliounifornew.data.UsuarioRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF12TelaDoLivro : AppCompatActivity() {

    private val authRepository    = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private val db                = FirebaseFirestore.getInstance()

    private var livroIdAtual      : String               = ""
    private var tituloAtual       : String               = ""
    private var autorAtual        : String               = ""
    private var coverUrlAtual     : String               = ""
    private var disponivelAtual   : Boolean              = true
    private var linkPdfAtual      : String               = ""
    private var linkAudiobookAtual: String               = ""
    private var hasBrailleAtual   : Boolean              = false
    private var setorAtual        : String               = ""
    private var livroListener     : ListenerRegistration? = null
    private lateinit var livroDao : LivroDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificação de Segurança Crítica: Evita abertura indevida durante transições de login/cadastro
        livroIdAtual = intent.getStringExtra("LIVRO_ID") ?: ""
        val usuarioAutenticado = authRepository.getUsuarioAtual()

        if (livroIdAtual.isEmpty() || usuarioAutenticado == null) {
            finish()
            return
        }

        setContentView(R.layout.telarf12_teladolivro)

        livroDao = AppDatabase.getDatabase(this).livroDao()

        carregarDadosDoLivro(livroIdAtual)
        carregarNota()

        configurarBotoesDeStatus()
        configurarBotoesAcao()
    }

    override fun onDestroy() {
        super.onDestroy()
        livroListener?.remove()  // Evita memory leak e callbacks pós-destruição
    }

    // ─── CARREGAMENTO DE DADOS ────────────────────────────────────────────────

    private fun carregarDadosDoLivro(id: String) {
        // SnapshotListener: recebe atualizações em tempo real e é removido em onDestroy()
        livroListener = db.collection("livros").document(id)
            .addSnapshotListener { snapshot, exception ->

                // ── Falha de rede → fallback para cache Room ───────────────────
                if (exception != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val cached = livroDao.buscarLivroPorId(id)
                        withContext(Dispatchers.Main) {
                            if (cached != null) {
                                renderizarDadosDaEntidade(cached)
                            } else {
                                Toast.makeText(
                                    this@TelaRF12TelaDoLivro,
                                    getString(R.string.erro_sem_conexao_sem_cache),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    Toast.makeText(this, "Livro não encontrado.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // ── Monta objeto local com os campos do Firestore ──────────────
                val entidadeRemota = EntidadeLivro(
                    id            = id,
                    title         = snapshot.getString("title")       ?: snapshot.getString("titulo")    ?: "",
                    author        = snapshot.getString("author")      ?: snapshot.getString("autor")     ?: "",
                    description   = snapshot.getString("description") ?: snapshot.getString("descricao") ?: "",
                    coverUrl      = snapshot.getString("coverUrl")    ?: "",
                    category      = snapshot.getString("category")    ?: snapshot.getString("categoria") ?: "",
                    stockQuantity = (snapshot.getLong("estoque") ?: snapshot.getLong("quantidade")
                        ?: snapshot.getLong("stock") ?: 0L).toInt(),
                    isAvailable   = (snapshot.getLong("estoque") ?: snapshot.getLong("quantidade")
                        ?: snapshot.getLong("stock") ?: 0L) > 0,
                    linkPdf       = snapshot.getString("linkPdf")       ?: "",
                    linkAudiobook = snapshot.getString("linkAudiobook") ?: "",
                    hasBraille    = snapshot.get("hasBraille") as? Boolean ?: false,
                    librarySector = snapshot.getString("librarySector") ?: snapshot.getString("setor") ?: ""
                )

                tituloAtual = entidadeRemota.title
                autorAtual  = entidadeRemota.author

                // ── Atualiza UI (já estamos na Main Thread) ────────────────────
                renderizarDadosDaEntidade(entidadeRemota)

                // ── Persiste no cache Room sem bloquear a UI ───────────────────
                lifecycleScope.launch(Dispatchers.IO) {
                    val base = livroDao.buscarLivroPorId(id)
                    livroDao.inserirLivro(
                        if (base != null) base.copy(
                            title         = entidadeRemota.title,
                            author        = entidadeRemota.author,
                            description   = entidadeRemota.description,
                            coverUrl      = entidadeRemota.coverUrl,
                            category      = entidadeRemota.category,
                            stockQuantity = entidadeRemota.stockQuantity,
                            isAvailable   = entidadeRemota.isAvailable,
                            linkPdf       = entidadeRemota.linkPdf,
                            linkAudiobook = entidadeRemota.linkAudiobook,
                            hasBraille    = entidadeRemota.hasBraille,
                            librarySector = entidadeRemota.librarySector
                        ) else entidadeRemota
                    )
                }
            }
    }

    // ─── RENDERIZAÇÃO A PARTIR DE EntidadeLivro ───────────────────────────────
    // Fonte única de renderização — usada tanto pelo Firestore quanto pelo Room.

    private fun renderizarDadosDaEntidade(livro: EntidadeLivro) {
        tituloAtual        = livro.title
        autorAtual         = livro.author
        coverUrlAtual      = livro.coverUrl
        disponivelAtual    = livro.isAvailable
        linkPdfAtual       = livro.linkPdf
        linkAudiobookAtual = livro.linkAudiobook
        hasBrailleAtual    = livro.hasBraille
        setorAtual         = livro.librarySector

        findViewById<TextView>(R.id.textTituloLivro)?.text = livro.title
        findViewById<TextView>(R.id.textAutorLivro)?.text  = livro.author
        findViewById<TextView>(R.id.textSobreLivro)?.text  = livro.description

        val imgCapa = findViewById<ImageView>(R.id.imageLivroDetalhes)
        if (livro.coverUrl.isNotEmpty()) {
            imgCapa?.load(livro.coverUrl) {
                placeholder(R.drawable.osda)
                error(R.drawable.osda)
            }
        } else {
            imgCapa?.setImageResource(R.drawable.osda)
        }

        if (livro.category.isNotEmpty()) {
            findViewById<MaterialButton>(R.id.buttonGenero)?.text = livro.category
        }

        val txtDisp    = findViewById<TextView>(R.id.textDisponivel)
        val txtEstoque = findViewById<TextView>(R.id.textEstoque)
        val indicador  = findViewById<View>(R.id.statusIndicator)

        if (livro.isAvailable) {
            txtDisp?.text = "Disponível para aluguel"
            txtDisp?.setTextColor(Color.parseColor("#2E7D32"))
            txtEstoque?.text = "${livro.stockQuantity} unidade${if (livro.stockQuantity == 1) "" else "s"} em estoque"
            indicador?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        } else {
            txtDisp?.text = "Indisponível no momento"
            txtDisp?.setTextColor(Color.parseColor("#C62828"))
            txtEstoque?.text = "Sem estoque"
            indicador?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#C62828"))
        }

    }

    // ─── NOTA DO USUÁRIO ──────────────────────────────────────────────────────

    private fun carregarNota() {
        if (livroIdAtual.isEmpty()) return
        val uid      = authRepository.getUsuarioAtual()?.uid ?: return
        val ratingBar = findViewById<RatingBar>(R.id.ratingBarLivro) ?: return

        // Carrega nota salva anteriormente
        db.collection("biblioteca_usuarios").document("${uid}_${livroIdAtual}").get()
            .addOnSuccessListener { doc ->
                ratingBar.rating = doc.getDouble("nota")?.toFloat() ?: 0f
            }

        // Salva quando o usuário toca nas estrelas
        ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (!fromUser) return@setOnRatingBarChangeListener
            db.collection("biblioteca_usuarios").document("${uid}_${livroIdAtual}")
                .set(
                    hashMapOf(
                        "usuarioId"    to uid,
                        "livroId"      to livroIdAtual,
                        "nota"         to rating.toDouble(),
                        "atualizadoEm" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
        }
    }

    // ─── BOTÕES DE STATUS DE LEITURA ─────────────────────────────────────────

    private fun configurarBotoesDeStatus() {
        val btnNaoLido = findViewById<MaterialButton>(R.id.buttonNaoLido) ?: return
        val btnLendo   = findViewById<MaterialButton>(R.id.buttonLendo)   ?: return
        val btnLido    = findViewById<MaterialButton>(R.id.buttonLido)    ?: return

        definirBotaoInativo(btnNaoLido)
        definirBotaoInativo(btnLendo)
        definirBotaoInativo(btnLido)

        btnNaoLido.setOnClickListener {
            definirBotaoAtivo(btnNaoLido); definirBotaoInativo(btnLendo); definirBotaoInativo(btnLido)
            salvarStatusNoFirestore("Não Lido")
        }
        btnLendo.setOnClickListener {
            definirBotaoInativo(btnNaoLido); definirBotaoAtivo(btnLendo); definirBotaoInativo(btnLido)
            salvarStatusNoFirestore("Lendo")
        }
        btnLido.setOnClickListener {
            definirBotaoInativo(btnNaoLido); definirBotaoInativo(btnLendo); definirBotaoAtivo(btnLido)
            salvarStatusNoFirestore("Lido")
        }
    }

    private fun definirBotaoAtivo(btn: MaterialButton) {
        btn.backgroundTintList = getColorStateList(R.color.biblio_blue)
        btn.setTextColor(getColor(android.R.color.white))
    }

    private fun definirBotaoInativo(btn: MaterialButton) {
        btn.backgroundTintList = getColorStateList(R.color.biblio_detalhes)
        btn.setTextColor(getColor(R.color.biblio_dark))
    }

    private fun salvarStatusNoFirestore(status: String) {
        if (livroIdAtual.isEmpty()) return
        val uid = authRepository.getUsuarioAtual()?.uid ?: return

        val campos = hashMapOf(
            "usuarioId"     to uid,
            "livroId"       to livroIdAtual,
            "titulo"        to tituloAtual,
            "autor"         to autorAtual,
            "statusLeitura" to status,
            "atualizadoEm"  to System.currentTimeMillis()
        )
        db.collection("biblioteca_usuarios").document("${uid}_${livroIdAtual}")
            .set(campos, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Status: $status salvo!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar status.", Toast.LENGTH_SHORT).show()
            }
    }

    // ─── BOTÕES DE AÇÃO ───────────────────────────────────────────────────────

    private fun configurarBotoesAcao() {
        findViewById<MaterialButton>(R.id.buttonListaDesejos)?.setOnClickListener {
            adicionarListaDesejos()
        }
        findViewById<MaterialButton>(R.id.buttonSuaLivraria)?.setOnClickListener {
            adicionarSuaLivraria()
        }
        findViewById<MaterialButton>(R.id.buttonVerMais)?.setOnClickListener {
            startActivity(Intent(this, TelaRF13VerMaisLivro::class.java)
                .putExtra("LIVRO_ID", livroIdAtual))
        }
        findViewById<MaterialButton>(R.id.buttonOpcoesLeitura)?.setOnClickListener {
            if (livroIdAtual.isEmpty()) {
                Toast.makeText(this, "Aguarde o carregamento do livro...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            OpcoesDeLeituraBottomSheet.newInstance(
                livroId       = livroIdAtual,
                titulo        = tituloAtual,
                autor         = autorAtual,
                linkPdf       = linkPdfAtual,
                linkAudiobook = linkAudiobookAtual,
                hasBraille    = hasBrailleAtual,
                setor         = setorAtual
            ).show(supportFragmentManager, OpcoesDeLeituraBottomSheet.TAG)
        }
    }

    private fun adicionarListaDesejos() {
        if (livroIdAtual.isEmpty()) {
            Toast.makeText(this, "Aguarde o carregamento do livro...", Toast.LENGTH_SHORT).show()
            return
        }
        val uid = authRepository.getUsuarioAtual()?.uid ?: run {
            Toast.makeText(this, "Faça login para usar esta função.", Toast.LENGTH_SHORT).show()
            return
        }

        // Buscamos os nomes atuais dos campos ou usamos o ID como fallback seguro
        val tituloParaSalvar = if (tituloAtual.isNotEmpty()) tituloAtual else "Livro em processamento"
        val autorParaSalvar  = if (autorAtual.isNotEmpty()) autorAtual else "Aguardando dados"

        val dados = hashMapOf(
            "usuarioId"    to uid,
            "livroId"      to livroIdAtual,
            "titulo"       to tituloParaSalvar,
            "autor"        to autorParaSalvar,
            "coverUrl"     to coverUrlAtual,
            "disponivel"   to disponivelAtual,
            "adicionadoEm" to System.currentTimeMillis()
        )

        usuarioRepository.salvarListaDesejos(uid, livroIdAtual, dados) { sucesso, erro ->
            if (sucesso) {
                Toast.makeText(this, "Adicionado à Lista de Desejos!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao salvar: $erro", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun adicionarSuaLivraria() {
        if (livroIdAtual.isEmpty()) {
            Toast.makeText(this, "Livro sem ID. Tente novamente.", Toast.LENGTH_SHORT).show()
            return
        }
        val uid = authRepository.getUsuarioAtual()?.uid ?: run {
            Toast.makeText(this, "Faça login para usar esta função.", Toast.LENGTH_SHORT).show()
            return
        }
        val dados = hashMapOf(
            "usuarioId"     to uid,
            "livroId"       to livroIdAtual,
            "titulo"        to tituloAtual,
            "autor"         to autorAtual,
            "statusLeitura" to "Não Lido",
            "adicionadoEm"  to System.currentTimeMillis()
        )
        db.collection("biblioteca_usuarios").document("${uid}_${livroIdAtual}")
            .set(dados, SetOptions.merge())
            .addOnSuccessListener {
                // RF15.8: Registra no histórico a adição
                usuarioRepository.registrarNoHistorico(uid, livroIdAtual, tituloAtual, autorAtual, "Adicionado")
                Toast.makeText(this, "\"${tituloAtual.ifEmpty { livroIdAtual }}\" adicionado à sua Livraria!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Não foi possível adicionar à Livraria. Tente novamente.", Toast.LENGTH_SHORT).show()
            }
    }
}
