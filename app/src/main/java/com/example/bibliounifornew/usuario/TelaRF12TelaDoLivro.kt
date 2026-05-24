package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class TelaRF12TelaDoLivro : AppCompatActivity() {

    private val authRepository    = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private val db                = FirebaseFirestore.getInstance()

    private var livroIdAtual : String = ""
    private var tituloAtual  : String = ""
    private var autorAtual   : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf12_teladolivro)

        livroIdAtual = intent.getStringExtra("LIVRO_ID") ?: ""

        if (livroIdAtual.isNotEmpty()) {
            carregarDadosDoLivro(livroIdAtual)
        }

        configurarBotoesDeStatus()
        configurarBotoesAcao()
    }

    // ─── CARREGAMENTO DE DADOS ────────────────────────────────────────────────

    private fun carregarDadosDoLivro(id: String) {
        db.collection("livros").document(id).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Livro não encontrado.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                tituloAtual = doc.getString("title")  ?: doc.getString("titulo")  ?: ""
                autorAtual  = doc.getString("author") ?: doc.getString("autor")   ?: ""

                findViewById<TextView>(R.id.textTituloLivro)?.text = tituloAtual
                findViewById<TextView>(R.id.textAutorLivro)?.text  = autorAtual
                findViewById<TextView>(R.id.textSobreLivro)?.text  =
                    doc.getString("description") ?: doc.getString("descricao") ?: ""

                val coverUrl = doc.getString("coverUrl") ?: ""
                val imgCapa  = findViewById<ImageView>(R.id.imageLivroDetalhes)
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
                Toast.makeText(this, "Erro ao carregar livro.", Toast.LENGTH_SHORT).show()
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
        findViewById<MaterialButton>(R.id.buttonSolicitar)?.setOnClickListener {
            startActivity(Intent(this, TelaRF19Solicitacoes::class.java)
                .putExtra("LIVRO_ID", livroIdAtual))
        }
        findViewById<MaterialButton>(R.id.buttonLer)?.setOnClickListener {
            startActivity(Intent(this, TelaRF14LeituraActivity::class.java)
                .putExtra("LIVRO_ID", livroIdAtual))
        }
    }

    private fun adicionarListaDesejos() {
        if (livroIdAtual.isEmpty()) {
            Toast.makeText(this, "Livro sem ID. Tente novamente.", Toast.LENGTH_SHORT).show()
            return
        }
        val uid = authRepository.getUsuarioAtual()?.uid ?: run {
            Toast.makeText(this, "Faça login para usar esta função.", Toast.LENGTH_SHORT).show()
            return
        }
        val dados = hashMapOf(
            "usuarioId"    to uid,
            "livroId"      to livroIdAtual,
            "titulo"       to tituloAtual,
            "autor"        to autorAtual,
            "adicionadoEm" to System.currentTimeMillis()
        )
        usuarioRepository.salvarListaDesejos(uid, livroIdAtual, dados) { sucesso, _ ->
            if (sucesso) {
                Toast.makeText(this, "\"${tituloAtual.ifEmpty { livroIdAtual }}\" adicionado à Lista de Desejos!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao adicionar à Lista de Desejos.", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "\"${tituloAtual.ifEmpty { livroIdAtual }}\" adicionado à sua Livraria!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Não foi possível adicionar à Livraria. Tente novamente.", Toast.LENGTH_SHORT).show()
            }
    }
}
