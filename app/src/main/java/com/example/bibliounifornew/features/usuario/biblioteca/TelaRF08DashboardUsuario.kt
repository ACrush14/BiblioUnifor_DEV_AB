package com.example.bibliounifornew.features.usuario.biblioteca

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import androidx.lifecycle.lifecycleScope
import coil.load
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class TelaRF08DashboardUsuario : AppCompatActivity() {

    private val authRepository    = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private val db                = FirebaseFirestore.getInstance()

    private lateinit var imagePerfil: ShapeableImageView
    private lateinit var textNomeUsuario: TextView
    private var isFirstLoad = true

    private val getGalleryImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            processarESubirFoto(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf08_dashboardusuario)

        textNomeUsuario = findViewById(R.id.textNomeUsuario)
        imagePerfil     = findViewById(R.id.imagePerfilUsuario)
        val uidAtual    = authRepository.getUsuarioAtual()?.uid

        if (uidAtual != null) {
            textNomeUsuario.text = getString(R.string.carregando_dados)
            carregarDadosUsuario(uidAtual)
            carregarDescobrir(uidAtual)
        } else {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        imagePerfil.setOnClickListener {
            getGalleryImage.launch("image/*")
        }

        configurarBotoesNavegacao()
        NavigationHelper.configurarBarraNavegacao(this)
    }

    override fun onResume() {
        super.onResume()
        if (!isFirstLoad) {
            val uidAtual = authRepository.getUsuarioAtual()?.uid
            if (uidAtual != null) {
                carregarDadosUsuario(uidAtual)
            }
        }
        isFirstLoad = false
    }

    private fun configurarBotoesNavegacao() {
        findViewById<ImageView>(R.id.btnConfig).setOnClickListener { startActivity(Intent(this, TelaRF09Configuracao::class.java)) }
        findViewById<ImageView>(R.id.btnNotificacao).setOnClickListener { startActivity(Intent(this, TelaRF20Notificacoes::class.java)) }
        findViewById<MaterialButton>(R.id.btnPesquisarLivros).setOnClickListener { startActivity(Intent(this, TelaRF11TelaDePesquisa::class.java)) }
        findViewById<MaterialButton>(R.id.btnMinhaLivraria).setOnClickListener { startActivity(Intent(this, TelaRF15MinhaLivrariaActivity::class.java)) }
        findViewById<MaterialButton>(R.id.btnListaDesejos).setOnClickListener { startActivity(Intent(this, TelaRF16ListaDesejosActivity::class.java)) }
        findViewById<MaterialButton>(R.id.btnAmigos).setOnClickListener { startActivity(Intent(this, TelaRF17Amigos::class.java)) }
        findViewById<MaterialButton>(R.id.btnHistorico).setOnClickListener { startActivity(Intent(this, TelaRF21Historico::class.java)) }
        findViewById<MaterialButton>(R.id.btnStatusAluguel).setOnClickListener { startActivity(Intent(this, TelaRF18StatusAluguel::class.java)) }
        findViewById<MaterialButton>(R.id.btnSairConta).setOnClickListener { showExitPopup() }
    }

    private fun carregarDadosUsuario(uid: String) {
        usuarioRepository.buscarPerfilUsuario(uid) { sucesso, dados, _ ->
            if (sucesso && dados != null) {
                textNomeUsuario.text = dados["nome"] as? String ?: "Usuário"

                val fotoUrl = dados["fotoUrl"] as? String ?: ""
                if (fotoUrl.isNotEmpty()) {
                    imagePerfil.load(fotoUrl) {
                        placeholder(R.drawable.user_placeholder)
                        error(R.drawable.user_placeholder)
                        crossfade(true)
                    }
                }
            } else {
                textNomeUsuario.text = getString(R.string.erro_carregar_usuario)
            }
        }
    }

    private fun processarESubirFoto(uri: Uri) {
        val uid = authRepository.getUsuarioAtual()?.uid ?: return
        imagePerfil.alpha = 0.5f

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (bitmap != null) {
                    val redimensionado = bitmap.scale(400, 400)
                    val baos = ByteArrayOutputStream()
                    redimensionado.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    val bytes = baos.toByteArray()

                    withContext(Dispatchers.Main) {
                        imagePerfil.setImageBitmap(bitmap)

                        usuarioRepository.uploadFotoPerfil(uid, bytes) { sucesso, url, erro ->
                            if (isFinishing || isDestroyed) return@uploadFotoPerfil
                            imagePerfil.alpha = 1.0f

                            if (sucesso && url != null) {
                                imagePerfil.load(url) { crossfade(true) }
                                Toast.makeText(this@TelaRF08DashboardUsuario, "Foto atualizada!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@TelaRF08DashboardUsuario, "Erro: $erro", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    imagePerfil.alpha = 1.0f
                    Toast.makeText(this@TelaRF08DashboardUsuario, "Erro ao processar imagem.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun carregarDescobrir(uid: String) {
        // Lança uma corrotina para tirar o peso da Thread Principal
        lifecycleScope.launch {
            try {
                // Vai para o processador de fundo (Background)
                val categoria = withContext(Dispatchers.IO) {
                    val snapshot = db.collection("biblioteca_usuarios")
                        .whereEqualTo("usuarioId", uid)
                        .limit(20)
                        .get()
                        .await()

                    val livroIds = snapshot.documents.mapNotNull { it.getString("livroId") }.distinct()

                    if (livroIds.isEmpty()) return@withContext null

                    val livrosSnap = db.collection("livros")
                        .whereIn(FieldPath.documentId(), livroIds.take(10))
                        .get()
                        .await()

                    // Matemática pesada feita fora da tela principal!
                    livrosSnap.documents
                        .mapNotNull { it.getString("category") ?: it.getString("categoria") }
                        .groupingBy { it }
                        .eachCount()
                        .maxByOrNull { it.value }?.key
                }

                // De volta à thread principal apenas para chamar a UI
                carregarLivrosDescobrir(categoria)

            } catch (e: Exception) {
                carregarLivrosDescobrir(null)
            }
        }
    }

    private fun carregarLivrosDescobrir(categoria: String?) {
        val container = findViewById<LinearLayout>(R.id.containerDescobrir) ?: return
        container.removeAllViews()

        lifecycleScope.launch {
            try {
                // Busca no banco de dados em Background
                val snapshot = withContext(Dispatchers.IO) {
                    val query = if (!categoria.isNullOrEmpty()) {
                        db.collection("livros").whereEqualTo("category", categoria).limit(10)
                    } else {
                        db.collection("livros").limit(10)
                    }
                    query.get().await()
                }

                if (snapshot.isEmpty) return@launch

                // Só processa a inflação visual se tiver dados
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
                            placeholder(R.drawable.user_placeholder)
                            error(R.drawable.user_placeholder)
                        }
                    } else {
                        imgCapa.setImageResource(R.drawable.user_placeholder)
                    }

                    txtTitulo.text = titulo
                    txtAutor.text  = autor

                    cardView.setOnClickListener {
                        startActivity(
                            Intent(this@TelaRF08DashboardUsuario, TelaRF12TelaDoLivro::class.java)
                                .putExtra("LIVRO_ID", livroId)
                        )
                    }
                    container.addView(cardView)
                }
            } catch (e: Exception) {
                // Tratamento de erro silencioso para não crashar a tela
            }
        }
    }

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