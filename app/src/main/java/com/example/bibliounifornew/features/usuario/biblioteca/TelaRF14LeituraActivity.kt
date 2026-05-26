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
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.features.usuario.livro.TelaRF11TelaDePesquisa
import com.example.bibliounifornew.features.usuario.solicitacao.TelaRF18StatusAluguel
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF14LeituraActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()
    private val db             = FirebaseFirestore.getInstance()
    private var livroIdAtual   : String = ""

    /** URLs de mídia — populadas por carregarCabecalho(), usadas pelos botões */
    private var linkPdfAtual   : String = ""
    private var linkAudioAtual : String = ""
    private var tituloAtual    : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf14_leitura)

        livroIdAtual = intent.getStringExtra("LIVRO_ID") ?: ""

        // Placeholder enquanto os dados carregam
        findViewById<TextView>(R.id.textTituloLivroAcoes)?.text    = "Carregando..."
        findViewById<TextView>(R.id.textAutorLivroAcoes)?.text     = ""
        findViewById<TextView>(R.id.textCategoriaLivroAcoes)?.text = ""

        if (livroIdAtual.isNotEmpty()) {
            carregarCabecalho(livroIdAtual)
        }

        // ─── BOTÃO ALUGAR ─────────────────────────────────────────────────────
        findViewById<Button>(R.id.buttonAlugarLivro).setOnClickListener {
            showPopupAlugar()
        }

        // ─── BOTÃO PROCURAR ───────────────────────────────────────────────────
        findViewById<Button>(R.id.buttonProcurarLivro).setOnClickListener {
            startActivity(Intent(this, TelaRF11TelaDePesquisa::class.java))
        }

        // ─── BOTÃO ABRIR PDF ──────────────────────────────────────────────────
        findViewById<Button>(R.id.buttonAbrirPdfLivro).setOnClickListener {
            abrirMidia(linkPdfAtual, "pdf")
        }

        // ─── BOTÃO ABRIR AUDIOBOOK ────────────────────────────────────────────
        findViewById<Button>(R.id.buttonAbrirAudioLivro).setOnClickListener {
            abrirMidia(linkAudioAtual, "audio")
        }

        // ─── BOTÃO SETOR LOCALIZADO ──────────────────────────────────────────
        findViewById<Button>(R.id.buttonSetorLivro).setOnClickListener {
            showPopupSetor()
        }
    }

    // ─── CABEÇALHO DINÂMICO ───────────────────────────────────────────────────

    private fun carregarCabecalho(id: String) {
        db.collection("livros").document(id).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                val titulo    = doc.getString("title")    ?: doc.getString("titulo")    ?: ""
                val autor     = doc.getString("author")   ?: doc.getString("autor")     ?: ""
                val categoria = doc.getString("category") ?: doc.getString("categoria") ?: ""
                val coverUrl  = doc.getString("coverUrl") ?: ""

                // Armazena para uso posterior nos listeners dos botões
                tituloAtual    = titulo
                linkPdfAtual   = doc.getString("linkPdf")      ?: ""
                linkAudioAtual = doc.getString("linkAudiobook") ?: ""

                findViewById<TextView>(R.id.textTituloLivroAcoes)?.text    = titulo.ifEmpty { "Título indisponível" }
                findViewById<TextView>(R.id.textAutorLivroAcoes)?.text     = autor
                findViewById<TextView>(R.id.textCategoriaLivroAcoes)?.text = categoria

                val imgCapa = findViewById<ImageView>(R.id.imageLivroAcoes)
                if (coverUrl.isNotEmpty()) {
                    imgCapa?.load(coverUrl) {
                        placeholder(R.drawable.osda)
                        error(R.drawable.osda)
                    }
                } else {
                    imgCapa?.setImageResource(R.drawable.osda)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar dados do livro.", Toast.LENGTH_SHORT).show()
                findViewById<TextView>(R.id.textTituloLivroAcoes)?.text = "Sem título"
            }
    }

    // ─── ABRIR MÍDIA (PDF ou AUDIOBOOK) ──────────────────────────────────────

    /**
     * Abre a URL de mídia com um chooser nativo.
     * Se o link estiver vazio/nulo, exibe mensagem orientando o usuário a aguardar o bibliotecário.
     */
    private fun abrirMidia(url: String, tipo: String) {
        if (url.isBlank()) {
            Toast.makeText(
                this,
                "Bibliotecário ainda não mandou o arquivo. Aguarde ou fale com um administrador",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        try {
            val uri    = Uri.parse(url)
            val mime   = if (tipo == "pdf") "application/pdf" else "audio/*"
            val titulo = if (tipo == "pdf") "Abrir PDF com..." else "Ouvir Audiobook com..."
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(Intent.createChooser(intent, titulo))
        } catch (e: Exception) {
            Toast.makeText(this, "Não foi possível abrir o arquivo.", Toast.LENGTH_SHORT).show()
        }
    }

    // ─── POPUP ALUGAR ─────────────────────────────────────────────────────────

    private fun showPopupAlugar() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_alugar_livro)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<TextView>(R.id.textTituloPopupAlugar)?.text =
            "Você deseja alugar o livro\n\"${tituloAtual.ifEmpty { "este livro" }}\"?"

        val btnConfirmar = dialog.findViewById<Button>(R.id.buttonAdicionarLivro)
        val btnCancelar  = dialog.findViewById<TextView>(R.id.textCancelarPopup)

        btnConfirmar.setOnClickListener {
            dialog.dismiss()
            gravarSolicitacaoEmprestimo()
        }
        btnCancelar.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun gravarSolicitacaoEmprestimo() {
        val usuarioAtual = authRepository.getUsuarioAtual()
        if (usuarioAtual == null) {
            Toast.makeText(this, "Faça login para alugar.", Toast.LENGTH_SHORT).show()
            return
        }

        val dados = hashMapOf(
            "uidAluno"        to usuarioAtual.uid,
            "idLivro"         to livroIdAtual,
            "status"          to "pendente",
            "dataSolicitacao" to System.currentTimeMillis()
        )

        db.collection("solicitacoes_emprestimo")
            .add(dados)
            .addOnSuccessListener {
                if (!isFinishing && !isDestroyed) showPopupSucesso()
            }
            .addOnFailureListener { e ->
                if (!isFinishing && !isDestroyed)
                    Toast.makeText(this, "Erro ao registrar solicitação: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPopupSucesso() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_livro_adicionado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<Button>(R.id.buttonVerMeusLivros).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, TelaRF18StatusAluguel::class.java))
        }

        dialog.show()
    }

    private fun showPopupSetor() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_setor_localizado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Define o nome do livro dinamicamente
        dialog.findViewById<TextView>(R.id.textLivroSetor)?.text = "Livro: $tituloAtual"

        dialog.findViewById<Button>(R.id.buttonVoltarSetor).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
