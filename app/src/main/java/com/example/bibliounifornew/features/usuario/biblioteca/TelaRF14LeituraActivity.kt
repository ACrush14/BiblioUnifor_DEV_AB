package com.example.bibliounifornew.features.usuario.biblioteca

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.SolicitacaoRepository
import com.example.bibliounifornew.features.usuario.livro.TelaRF11TelaDePesquisa
import com.example.bibliounifornew.features.usuario.solicitacao.TelaRF18StatusAluguel
import com.example.bibliounifornew.features.usuario.solicitacao.TelaRF19SolicitacoesTermosCondicoes
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class TelaRF14LeituraActivity : AppCompatActivity() {

    private val authRepository        = AuthRepository()
    private val solicitacaoRepository = SolicitacaoRepository()
    private val db                    = FirebaseFirestore.getInstance()
    private var livroIdAtual   : String = ""

    /** URLs de mídia — populadas por carregarCabecalho(), usadas pelos botões */
    private var linkPdfAtual   : String = ""
    private var linkAudioAtual : String = ""
    private var tituloAtual    : String = ""
    private var autorAtual     : String = ""
    private var coverUrlAtual  : String = ""
    private var setorAtual     : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf14_leitura)

        livroIdAtual = intent.getStringExtra("LIVRO_ID") ?: ""

        // Placeholder enquanto os dados carregam
        findViewById<TextView>(R.id.textTituloLivroAcoes)?.text    = getString(R.string.carregando_dados)
        findViewById<TextView>(R.id.textAutorLivroAcoes)?.text     = ""
        findViewById<TextView>(R.id.textCategoriaLivroAcoes)?.text = ""

        if (livroIdAtual.isNotEmpty()) {
            carregarCabecalho(livroIdAtual)
            verificarStatusAluguelRapido()
        }

        // ─── BOTÃO ALUGAR ─────────────────────────────────────────────────────
        findViewById<Button>(R.id.buttonAlugarLivro)?.setOnClickListener {
            irParaSolicitacao("Aluguel")
        }

        // ─── BOTÃO RESERVAR ──────────────────────────────────────────────────
        findViewById<Button>(R.id.buttonReservar)?.setOnClickListener {
            irParaSolicitacao("Reserva")
        }

        // ─── BOTÕES DE SOLICITAÇÃO DE MÍDIA ──────────────────────────────────
        findViewById<Button>(R.id.buttonSolicitarPdf)?.setOnClickListener {
            irParaSolicitacao("PDF")
        }
        findViewById<Button>(R.id.buttonSolicitarBraille)?.setOnClickListener {
            irParaSolicitacao("Braille")
        }
        findViewById<Button>(R.id.buttonSolicitarAudio)?.setOnClickListener {
            irParaSolicitacao("Audiobook")
        }

        // ─── BOTÃO PROCURAR ───────────────────────────────────────────────────
        findViewById<Button>(R.id.buttonProcurarLivro)?.setOnClickListener {
            startActivity(Intent(this, TelaRF11TelaDePesquisa::class.java))
        }

        // ─── BOTÃO ABRIR PDF ──────────────────────────────────────────────────
        findViewById<Button>(R.id.buttonAbrirPdfLivro)?.setOnClickListener {
            abrirMidia(linkPdfAtual, "pdf")
        }

        // ─── BOTÃO ABRIR AUDIOBOOK ────────────────────────────────────────────
        findViewById<Button>(R.id.buttonAbrirAudioLivro)?.setOnClickListener {
            abrirMidia(linkAudioAtual, "audio")
        }

        // ─── BOTÃO SETOR LOCALIZADO ──────────────────────────────────────────
        findViewById<Button>(R.id.buttonSetorLivro)?.setOnClickListener {
            showPopupSetor()
        }
    }

    // ─── CABEÇALHO DINÂMICO ───────────────────────────────────────────────────

    private fun carregarCabecalho(id: String) {
        db.collection("livros").document(id).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists() || isFinishing || isDestroyed) return@addOnSuccessListener

                val titulo    = doc.getString("title")    ?: doc.getString("titulo")    ?: "Título Indisponível"
                val autor     = doc.getString("author")   ?: doc.getString("autor")     ?: "Autor Desconhecido"
                val categoria = doc.getString("category") ?: doc.getString("categoria") ?: "Geral"
                val coverUrl  = doc.getString("coverUrl") ?: ""

                // Armazena para uso posterior nos listeners dos botões
                tituloAtual    = titulo
                autorAtual     = autor
                coverUrlAtual  = coverUrl
                setorAtual     = doc.getString("librarySector") ?: doc.getString("setor") ?: getString(R.string.msg_setor_nao_informado)
                linkPdfAtual   = doc.getString("linkPdf")      ?: ""
                linkAudioAtual = doc.getString("linkAudiobook") ?: ""

                findViewById<TextView>(R.id.textTituloLivroAcoes)?.text    = titulo
                findViewById<TextView>(R.id.textAutorLivroAcoes)?.text     = autor
                findViewById<TextView>(R.id.textCategoriaLivroAcoes)?.text = categoria

                val imgCapa = findViewById<ImageView>(R.id.imageLivroAcoes)
                if (coverUrl.isNotEmpty()) {
                    imgCapa?.load(coverUrl) {
                        placeholder(R.drawable.osda)
                        error(R.drawable.osda)
                        crossfade(true)
                        size(500, 750) // Reduz pressão de memória para capas grandes
                    }
                } else {
                    imgCapa?.setImageResource(R.drawable.osda)
                }
            }
            .addOnFailureListener {
                if (!isFinishing && !isDestroyed) {
                    Toast.makeText(this, "Erro ao carregar dados do livro.", Toast.LENGTH_SHORT).show()
                    findViewById<TextView>(R.id.textTituloLivroAcoes)?.text = "Sem título"
                }
            }
    }

    /**
     * Verifica se o livro já está alugado ANTES do usuário clicar no botão,
     * melhorando a fluidez e evitando popups desnecessários.
     */
    private fun verificarStatusAluguelRapido() {
        val uid = authRepository.getUsuarioAtual()?.uid ?: return
        db.collection("solicitacoes_emprestimo")
            .whereEqualTo("uidAluno", uid)
            .whereEqualTo("idLivro", livroIdAtual)
            .get()
            .addOnSuccessListener { snapshot ->
                val jaAlugado = snapshot.documents.any { doc ->
                    val status = doc.getString("status") ?: ""
                    status in listOf("pendente", "ativo", "atrasado")
                }
                if (jaAlugado) {
                    val btnAlugar = findViewById<Button>(R.id.buttonAlugarLivro)
                    btnAlugar?.text = getString(R.string.label_alugado)
                    btnAlugar?.isEnabled = false
                    btnAlugar?.alpha = 0.6f
                }
            }
    }

    // ─── NAVEGAÇÃO PARA SOLICITAÇÕES (RF19) ──────────────────────────────────

    private fun irParaSolicitacao(tipo: String) {
        if (tituloAtual.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_aguarde_carregamento), Toast.LENGTH_SHORT).show()
            return
        }

        // Se for aluguel, fazemos uma verificação prévia de duplicata para evitar que o usuário 
        // aceite os termos à toa se já possuir o livro.
        if (tipo == "Aluguel") {
            val uid = authRepository.getUsuarioAtual()?.uid ?: return
            db.collection("solicitacoes_emprestimo")
                .whereEqualTo("uidAluno", uid)
                .whereEqualTo("idLivro", livroIdAtual)
                .get()
                .addOnSuccessListener { snapshot ->
                    val jaAlugado = snapshot.documents.any { doc ->
                        val status = doc.getString("status") ?: ""
                        status in listOf("pendente", "ativo", "atrasado")
                    }

                    if (jaAlugado) {
                        Toast.makeText(this, getString(R.string.msg_livro_ja_alugado), Toast.LENGTH_LONG).show()
                    } else {
                        navegarParaTermos(tipo)
                    }
                }
                .addOnFailureListener {
                    navegarParaTermos(tipo)
                }
        } else {
            navegarParaTermos(tipo)
        }
    }

    private fun navegarParaTermos(tipo: String) {
        val intent = Intent(this, TelaRF19SolicitacoesTermosCondicoes::class.java).apply {
            putExtra("TIPO_MIDIA", tipo)
            putExtra("LIVRO_ID",   livroIdAtual)
            putExtra("TITULO",     tituloAtual)
            putExtra("AUTOR",      autorAtual)
        }
        startActivity(intent)
    }

    // ─── ABRIR MÍDIA (PDF ou AUDIOBOOK) ──────────────────────────────────────

    /**
     * Abre a URL de mídia com um chooser nativo.
     * Se o link estiver vazio/nulo, exibe mensagem orientando o usuário a aguardar o bibliotecário.
     */
    private fun abrirMidia(url: String, tipo: String) {
        if (url.isNullOrBlank()) {
            val msg = if (tipo == "pdf") getString(R.string.msg_pdf_indisponivel) else getString(R.string.msg_audiobook_indisponivel)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            return
        }

        try {
            val uri = Uri.parse(url)
            val mime = if (tipo == "pdf") "application/pdf" else "audio/*"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val titulo = if (tipo == "pdf") "Abrir PDF com..." else "Ouvir Audiobook com..."
            val chooser = Intent.createChooser(intent, titulo)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            if (intent.resolveActivity(packageManager) != null || url.startsWith("http")) {
                startActivity(chooser)
            } else {
                // Se falhar o chooser por falta de app específico, tenta abrir via browser
                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            // Último recurso: tentar abrir a URL pura
            try {
                val lastChanceIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                lastChanceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(lastChanceIntent)
            } catch (e2: Exception) {
                Toast.makeText(this, "Não foi possível abrir este tipo de arquivo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPopupSetor() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_setor_localizado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Define o nome do livro e setor dinamicamente
        dialog.findViewById<TextView>(R.id.textLivroSetor)?.text = "Livro: $tituloAtual"
        dialog.findViewById<TextView>(R.id.textSetorLocalizado)?.text = setorAtual

        dialog.findViewById<Button>(R.id.buttonVoltarSetor).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
