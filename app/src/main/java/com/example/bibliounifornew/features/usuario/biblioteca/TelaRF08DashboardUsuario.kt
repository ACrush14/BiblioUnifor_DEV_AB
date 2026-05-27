package com.example.bibliounifornew.features.usuario.biblioteca

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.MainActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.example.bibliounifornew.features.usuario.amigo.TelaRF17Amigos
import com.example.bibliounifornew.features.usuario.livro.TelaRF11TelaDePesquisa
import com.example.bibliounifornew.features.usuario.livro.TelaRF12TelaDoLivro
import com.example.bibliounifornew.features.usuario.livro.TelaRF16ListaDesejosActivity
import com.example.bibliounifornew.features.usuario.notificacao.TelaRF20Notificacoes
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper
import com.example.bibliounifornew.features.usuario.perfil.TelaRF09Configuracao
import com.example.bibliounifornew.features.usuario.solicitacao.TelaRF18StatusAluguel
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class TelaRF08DashboardUsuario : AppCompatActivity() {

    private val authRepository    = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private val db                = FirebaseFirestore.getInstance()

    private lateinit var imagePerfil: ShapeableImageView

    // Launcher para selecionar imagem da galeria
    private val getGalleryImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            processarESubirFoto(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf08_dashboardusuario)

        val textNomeUsuario = findViewById<TextView>(R.id.textNomeUsuario)
        imagePerfil         = findViewById(R.id.imagePerfilUsuario)
        val uidAtual        = authRepository.getUsuarioAtual()?.uid

        if (uidAtual != null) {
            textNomeUsuario?.text = "Carregando..."

            usuarioRepository.buscarPerfilUsuario(uidAtual) { sucesso, dados, erro ->
                if (sucesso && dados != null) {
                    textNomeUsuario?.text = dados["nome"] as? String ?: "Usuário"

                    // Carrega foto de perfil se disponível
                    val fotoUrl = dados["fotoUrl"] as? String ?: ""
                    if (fotoUrl.isNotEmpty()) {
                        imagePerfil.load(fotoUrl) {
                            placeholder(R.drawable.user_placeholder)
                            error(R.drawable.user_placeholder)
                            crossfade(true)
                        }
                    }
                } else {
                    Toast.makeText(this, "Erro ao carregar perfil: $erro", Toast.LENGTH_SHORT).show()
                    textNomeUsuario?.text = "Erro ao carregar"
                }
            }

            carregarDescobrir(uidAtual)
        } else {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        // ─── CLIQUE NA FOTO PARA TROCAR ────────────────────────────────────────
        imagePerfil.setOnClickListener {
            getGalleryImage.launch("image/*")
        }

        // ─── NAVEGAÇÃO ────────────────────────────────────────────────────────
        val btnConfig         = findViewById<ImageView>(R.id.btnConfig)
        val btnNotificacao    = findViewById<ImageView>(R.id.btnNotificacao)
        val btnPesquisarLivros        = findViewById<MaterialButton>(R.id.btnPesquisarLivros)
        val btnMinhaLivrariaDashboard = findViewById<MaterialButton>(R.id.btnMinhaLivraria)
        val btnListaDesejo            = findViewById<MaterialButton>(R.id.btnListaDesejos)
        val btnAmigosDashboard        = findViewById<MaterialButton>(R.id.btnAmigos)
        val btnHistoricoDashboard     = findViewById<MaterialButton>(R.id.btnHistorico)
        val btnStatusAluguel          = findViewById<MaterialButton>(R.id.btnStatusAluguel)
        val btnSair                   = findViewById<MaterialButton>(R.id.btnSairConta)

        btnConfig.setOnClickListener         { startActivity(Intent(this, TelaRF09Configuracao::class.java)) }
        btnNotificacao.setOnClickListener    { startActivity(Intent(this, TelaRF20Notificacoes::class.java)) }
        btnPesquisarLivros.setOnClickListener        { startActivity(Intent(this, TelaRF11TelaDePesquisa::class.java)) }
        btnMinhaLivrariaDashboard.setOnClickListener { startActivity(Intent(this, TelaRF15MinhaLivrariaActivity::class.java)) }
        btnListaDesejo.setOnClickListener            { startActivity(Intent(this, TelaRF16ListaDesejosActivity::class.java)) }
        btnAmigosDashboard.setOnClickListener        { startActivity(Intent(this, TelaRF17Amigos::class.java)) }
        btnHistoricoDashboard.setOnClickListener     { startActivity(Intent(this, TelaRF21Historico::class.java)) }
        btnStatusAluguel.setOnClickListener          { startActivity(Intent(this, TelaRF18StatusAluguel::class.java)) }
        btnSair.setOnClickListener                   { showExitPopup() }

        NavigationHelper.configurarBarraNavegacao(this)
    }

    private fun processarESubirFoto(uri: Uri) {
        val uid = authRepository.getUsuarioAtual()?.uid ?: return
        Toast.makeText(this, "Processando imagem...", Toast.LENGTH_SHORT).show()

        try {
            @Suppress("DEPRECATION")
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val redimensionado = Bitmap.createScaledBitmap(bitmap, 400, 400, true)
            val baos = ByteArrayOutputStream()
            redimensionado.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val bytes = baos.toByteArray()

            imagePerfil.alpha = 0.5f

            usuarioRepository.uploadFotoPerfil(uid, bytes) { sucesso, url, erro ->
                imagePerfil.alpha = 1.0f
                if (sucesso && url != null) {
                    imagePerfil.load(url) {
                        placeholder(R.drawable.user_placeholder)
                        error(R.drawable.user_placeholder)
                        crossfade(true)
                    }
                    Toast.makeText(this, "Foto atualizada!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Erro: $erro", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao processar imagem.", Toast.LENGTH_SHORT).show()
        }
    }

    // ─── SEÇÃO DESCOBRIR ──────────────────────────────────────────────────────

    /**
     * Passo 1: busca livros que o usuário já tem na biblioteca para descobrir a categoria favorita.
     * Se não houver histórico, carrega livros sem filtro de categoria.
     */
    private fun carregarDescobrir(uid: String) {
        db.collection("biblioteca_usuarios")
            .whereEqualTo("usuarioId", uid)
            .limit(20)
            .get()
            .addOnSuccessListener { snapshot ->
                val livroIds = snapshot.documents.mapNotNull { it.getString("livroId") }.distinct()
                if (livroIds.isEmpty()) {
                    carregarLivrosDescobrir(null)
                    return@addOnSuccessListener
                }
                // Passo 2: descobre categorias dos livros do usuário
                db.collection("livros")
                    .whereIn(FieldPath.documentId(), livroIds.take(10))
                    .get()
                    .addOnSuccessListener { livrosSnap ->
                        val categoria = livrosSnap.documents
                            .mapNotNull { it.getString("category") ?: it.getString("categoria") }
                            .groupingBy { it }
                            .eachCount()
                            .maxByOrNull { it.value }
                            ?.key
                        carregarLivrosDescobrir(categoria)
                    }
                    .addOnFailureListener { carregarLivrosDescobrir(null) }
            }
            .addOnFailureListener { carregarLivrosDescobrir(null) }
    }

    /**
     * Passo 2: monta os cards dinâmicos no containerDescobrir.
     * Se categoria for null, exibe qualquer livro do Firestore.
     */
    private fun carregarLivrosDescobrir(categoria: String?) {
        val container = findViewById<LinearLayout>(R.id.containerDescobrir) ?: return
        container.removeAllViews()

        val query = if (!categoria.isNullOrEmpty()) {
            db.collection("livros").whereEqualTo("category", categoria).limit(10)
        } else {
            db.collection("livros").limit(10)
        }

        query.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) return@addOnSuccessListener
                for (doc in snapshot.documents) {
                    val titulo   = doc.getString("title")    ?: doc.getString("titulo")  ?: "Sem título"
                    val autor    = doc.getString("author")   ?: doc.getString("autor")   ?: ""
                    val coverUrl = doc.getString("coverUrl") ?: ""
                    val livroId  = doc.id

                    val cardView  = layoutInflater.inflate(R.layout.item_livro_descobrir, container, false)
                    val imgCapa   = cardView.findViewById<ImageView>(R.id.imgCapaDescobrir)
                    val txtTitulo = cardView.findViewById<TextView>(R.id.txtTituloDescobrir)
                    val txtAutor  = cardView.findViewById<TextView>(R.id.txtAutorDescobrir)

                    if (coverUrl.isNotEmpty()) {
                        imgCapa.load(coverUrl) {
                            placeholder(R.drawable.osda)
                            error(R.drawable.osda)
                        }
                    } else {
                        imgCapa.setImageResource(R.drawable.osda)
                    }
                    txtTitulo.text = titulo
                    txtAutor.text  = autor

                    cardView.setOnClickListener {
                        startActivity(
                            Intent(this, TelaRF12TelaDoLivro::class.java)
                                .putExtra("LIVRO_ID", livroId)
                        )
                    }
                    container.addView(cardView)
                }
            }
            .addOnFailureListener { /* ignora silenciosamente */ }
    }

    // ─── POPUP SAIR ───────────────────────────────────────────────────────────

    private fun showExitPopup() {
        val dialogView = layoutInflater.inflate(R.layout.popup_sair_conta, null)
        val builder    = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<MaterialButton>(R.id.btnConfirmarSair).setOnClickListener {
            dialog.dismiss()
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            val intentSair = Intent(this, com.example.bibliounifornew.login.TelaRF01BemVindo::class.java)
            intentSair.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intentSair)
            finish()
        }

        dialogView.findViewById<TextView>(R.id.btnCancelarSair).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}
