package com.example.bibliounifornew.features.usuario.perfil

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.example.bibliounifornew.login.TelaRF01BemVindo
import com.example.bibliounifornew.login.TelaRF03LoginAluno
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.io.ByteArrayOutputStream

class TelaRF09Configuracao : AppCompatActivity() {

    private val authRepository    = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private val db                = FirebaseFirestore.getInstance()
    private var usuarioAtual      : FirebaseUser? = null

    // Listener em tempo real — cancelado em onDestroy para evitar memory leak
    private var snapshotListener  : ListenerRegistration? = null

    // Launcher de galeria — registrado antes de onCreate
    private val galeria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { processarESubirFoto(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf09_configuracao)

        usuarioAtual = authRepository.getUsuarioAtual()

        // ── Mapeamento de views ───────────────────────────────────────────────
        val btnSalvar       = findViewById<MaterialButton>(R.id.buttonSalvarConfig)
        val btnRedefinir    = findViewById<MaterialButton>(R.id.buttonRedefinirSenha)
        val btnApagar       = findViewById<MaterialButton>(R.id.buttonApagarConta)
        val editNome        = findViewById<EditText>(R.id.editNome)
        val editUsuario     = findViewById<EditText>(R.id.editUsuario)
        val editBio         = findViewById<EditText>(R.id.editBio)
        val textEmail       = findViewById<TextView>(R.id.textUsuario)
        val iconEditNome    = findViewById<ImageView>(R.id.iconEditNome)
        val iconEditUsuario = findViewById<ImageView>(R.id.iconEditUsuario)
        val iconEditBio     = findViewById<ImageView>(R.id.iconEditBio)
        val imagePerfil     = findViewById<ShapeableImageView>(R.id.imagePerfilUsuario)

        // Estado inicial: campos bloqueados até o lápis ser pressionado
        editNome.isEnabled    = false
        editUsuario.isEnabled = false
        editBio.isEnabled     = false

        // ── 1. Carregar dados via Snapshot Listener ───────────────────────────
        if (usuarioAtual != null) {
            textEmail?.text = usuarioAtual!!.email

            snapshotListener = usuarioRepository.observarPerfilUsuario(usuarioAtual!!.uid) { dados ->
                if (dados != null) {
                    if (!editNome.isEnabled) {
                        editNome.setText(dados["nome"] as? String ?: "")
                    }
                    if (!editUsuario.isEnabled) {
                        editUsuario.setText(dados["usuario"] as? String ?: "")
                    }
                    if (!editBio.isEnabled) {
                        val bio = dados["biografia"] as? String ?: ""
                        editBio.setText(bio.ifEmpty { "Escreva um pouco sobre você..." })
                    }
                    // Carrega foto de perfil se disponível
                    val fotoUrl = dados["fotoUrl"] as? String ?: ""
                    if (fotoUrl.isNotEmpty()) {
                        imagePerfil?.load(fotoUrl) {
                            placeholder(R.drawable.user_placeholder)
                            error(R.drawable.user_placeholder)
                        }
                    }
                }
            }
        } else {
            startActivity(Intent(this, TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        // ── 2. Lógica de edição (lápis destrava o campo) ─────────────────────
        configurarIconeEdicao(editNome, iconEditNome)
        configurarIconeEdicao(editUsuario, iconEditUsuario)
        configurarIconeEdicao(editBio, iconEditBio)

        // ── 3. Botão Salvar ───────────────────────────────────────────────────
        btnSalvar.setOnClickListener {
            val uid = usuarioAtual?.uid ?: return@setOnClickListener
            val campos = mapOf(
                "nome"      to editNome.text.toString().trim(),
                "usuario"   to editUsuario.text.toString().trim(),
                "biografia" to editBio.text.toString().trim()
            )
            btnSalvar.isEnabled = false
            btnSalvar.text      = "Salvando..."
            usuarioRepository.salvarPerfilCompleto(uid, campos) { sucesso, erro ->
                btnSalvar.isEnabled = true
                btnSalvar.text      = "Salvar Alterações"
                if (sucesso) {
                    editNome.isEnabled    = false
                    editUsuario.isEnabled = false
                    editBio.isEnabled     = false
                    Toast.makeText(this, "Perfil salvo com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Erro ao salvar: $erro", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ── 4. Redefinir senha → RF10 ─────────────────────────────────────────
        btnRedefinir.setOnClickListener {
            startActivity(Intent(this, TelaRF10RedefinirSenha::class.java))
        }

        // ── 5. Apagar conta ───────────────────────────────────────────────────
        btnApagar.setOnClickListener { exibirPopupApagarConta() }

        // ── 6. Clique na foto de perfil ───────────────────────────────────────
        imagePerfil?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Foto de Perfil")
                .setMessage("Deseja mudar sua foto?")
                .setPositiveButton("Escolher da Galeria") { _, _ ->
                    galeria.launch("image/*")
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Força sincronização ao voltar para a tela (Ex: se mudou foto no Dashboard)
        usuarioAtual?.uid?.let { uid ->
            usuarioRepository.buscarPerfilUsuario(uid) { sucesso, dados, _ ->
                if (sucesso && dados != null) {
                    val imagePerfil = findViewById<ShapeableImageView>(R.id.imagePerfilUsuario)
                    val fotoUrl = dados["fotoUrl"] as? String ?: ""
                    if (fotoUrl.isNotEmpty()) {
                        imagePerfil?.load(fotoUrl) {
                            placeholder(R.drawable.user_placeholder)
                            error(R.drawable.user_placeholder)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        snapshotListener?.remove()
    }

    // ─── UPLOAD DE FOTO ───────────────────────────────────────────────────────

    private fun processarESubirFoto(uri: Uri) {
        val uid = usuarioAtual?.uid ?: return
        val imagePerfil = findViewById<ShapeableImageView>(R.id.imagePerfilUsuario)

        try {
            // 1. ATUALIZAÇÃO IMEDIATA NA UI (Feedback instantâneo)
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                imagePerfil?.setImageBitmap(bitmap)
                imagePerfil?.alpha = 0.5f // Indicador visual de "em progresso"

                // 2. Preparação dos bytes para upload
                val redimensionado = Bitmap.createScaledBitmap(bitmap, 400, 400, true)
                val baos = ByteArrayOutputStream()
                redimensionado.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val bytes = baos.toByteArray()

                // 3. Upload em segundo plano
                usuarioRepository.uploadFotoPerfil(uid, bytes) { sucesso, url, erro ->
                    if (isFinishing || isDestroyed) return@uploadFotoPerfil
                    imagePerfil?.alpha = 1.0f
                    if (sucesso && url != null) {
                        // Recarrega via Coil para garantir que o cache seja atualizado
                        imagePerfil?.load(url) {
                            placeholder(R.drawable.user_placeholder)
                            error(R.drawable.user_placeholder)
                        }
                        Toast.makeText(this, "Foto atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Erro: $erro", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao processar imagem.", Toast.LENGTH_SHORT).show()
        }
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private fun configurarIconeEdicao(editText: EditText, icone: ImageView) {
        icone.setOnClickListener {
            editText.isEnabled = true
            editText.requestFocus()
            editText.setSelection(editText.text.length)
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                    as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun exibirPopupApagarConta() {
        val dialogView = layoutInflater.inflate(R.layout.popup_apagar_conta, null)
        val builder    = AlertDialog.Builder(this).setView(dialogView)
        val dialog     = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnConfirmar = dialogView.findViewById<MaterialButton>(R.id.buttonConfirmarApagarConta)
        val btnCancelar  = dialogView.findViewById<TextView>(R.id.textCancelarApagarConta)
        val editSenha    = dialogView.findViewById<EditText>(R.id.editSenhaPopup)
        val iconOlho     = dialogView.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)

        var senhaVisivelPopup = false
        iconOlho.setOnClickListener {
            senhaVisivelPopup = !senhaVisivelPopup
            if (senhaVisivelPopup) {
                editSenha.transformationMethod = HideReturnsTransformationMethod.getInstance()
                iconOlho.setImageResource(R.drawable.ic_eye_open)
            } else {
                editSenha.transformationMethod = PasswordTransformationMethod.getInstance()
                iconOlho.setImageResource(R.drawable.ic_eye_closed)
            }
            editSenha.setSelection(editSenha.text.length)
        }

        btnConfirmar.setOnClickListener {
            authRepository.getUsuarioAtual()?.delete()
            val intent = Intent(this, TelaRF01BemVindo::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
