package com.example.bibliounifornew.login

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.bibliounifornew.features.adm.dashboard.TelaRF28DashboardADM
import com.example.bibliounifornew.data.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF23LoginADM : AppCompatActivity() {

    private val authRepository = AuthRepository()
    private val db             = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf23_login_adm)

        val imageLogo = findViewById<ImageView>(R.id.imageLogoAdm)
        carregarLogoSegura(imageLogo)

        // ─── CAMPOS ──────────────────────────────────────────────────────────
        val editEmail      = findViewById<EditText>(R.id.editEmailAdm)
        val editSenha      = findViewById<EditText>(R.id.editSenhaAdm)
        val editCredencial = findViewById<EditText>(R.id.editCredencialAdm)
        val erro           = findViewById<TextView>(R.id.textErroAdm)
        val criarConta     = findViewById<TextView>(R.id.textCriarContaAdm)
        val esqueceuSenha  = findViewById<TextView>(R.id.textEsqueceuSenhaAdm)
        val botaoEntrar    = findViewById<Button>(R.id.buttonEntrarAdm)
        val btnOlho        = findViewById<ImageView>(R.id.iconOlhoSenhaAdm)

        // UX: limpa erro ao focar OU ao digitar em qualquer campo
        val limparErro = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                erro.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        listOf(editEmail, editSenha, editCredencial).forEach { campo ->
            campo.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) erro.visibility = View.GONE }
            campo.addTextChangedListener(limparErro)
        }

        // ─── MOSTRAR/OCULTAR SENHA ────────────────────────────────────────────
        var senhaVisivel = false
        btnOlho.setOnClickListener {
            senhaVisivel = !senhaVisivel
            editSenha.transformationMethod = if (senhaVisivel)
                HideReturnsTransformationMethod.getInstance()
            else
                PasswordTransformationMethod.getInstance()
            btnOlho.setImageResource(if (senhaVisivel) R.drawable.ic_eye_open else R.drawable.ic_eye_closed)
            editSenha.setSelection(editSenha.text.length)
        }

        // ─── LOGIN COM FIREBASE AUTH + RBAC ──────────────────────────────────
        botaoEntrar.setOnClickListener {
            val sEmail      = editEmail.text.toString().trim()
            val sSenha      = editSenha.text.toString()
            val sCredencial = editCredencial.text.toString().trim()

            // 1) Validação básica
            if (sEmail.isEmpty() || sSenha.isEmpty() || sCredencial.isEmpty()) {
                erro.text       = "Preencha todos os campos"
                erro.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // 2) A segurança de acesso ADM é garantida pelo RBAC no passo 5 (role == "adm"
            //    verificado no Firestore). Não há mais senha mestra local — qualquer string
            //    hardcoded no APK pode ser lida após decompilação (apktool).
            //    O campo editCredencialAdm permanece no XML por compatibilidade de UI.

            // 3) Loading state
            botaoEntrar.isEnabled = false
            botaoEntrar.text = "Verificando..."

            // 4) Firebase Auth
            authRepository.loginUsuario(sEmail, sSenha) { sucesso, uid ->
                if (!sucesso || uid == null) {
                    runOnUiThread {
                        botaoEntrar.isEnabled = true
                        botaoEntrar.text = "Entrar"
                        erro.text       = "E-mail ou senha incorretos"
                        erro.visibility = View.VISIBLE
                    }
                    return@loginUsuario
                }

                // 5) RBAC — verifica role no Firestore
                db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val role = doc.getString("role") ?: ""
                        when {
                            !doc.exists() -> {
                                FirebaseAuth.getInstance().signOut()
                                botaoEntrar.isEnabled = true
                                botaoEntrar.text = "Entrar"
                                erro.text       = "Perfil não encontrado. Registre-se como ADM primeiro."
                                erro.visibility = View.VISIBLE
                            }
                            role == "adm" -> {
                                // Entra independente de cadastroConfirmado
                                val intent = Intent(this, TelaRF28DashboardADM::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            else -> {
                                FirebaseAuth.getInstance().signOut()
                                botaoEntrar.isEnabled = true
                                botaoEntrar.text = "Entrar"
                                erro.text       = "Acesso negado: conta sem permissão de administrador"
                                erro.visibility = View.VISIBLE
                            }
                        }
                    }
                    .addOnFailureListener {
                        FirebaseAuth.getInstance().signOut()
                        botaoEntrar.isEnabled = true
                        botaoEntrar.text = "Entrar"
                        erro.text       = "Não foi possível verificar as permissões. Tente novamente."
                        erro.visibility = View.VISIBLE
                    }
            }
        }

        // ─── NAVEGAÇÃO ────────────────────────────────────────────────────────
        criarConta.setOnClickListener {
            startActivity(Intent(this, TelaRF26NovaContaADM::class.java))
        }

        esqueceuSenha.setOnClickListener {
            startActivity(Intent(this, TelaRF24RecuperacaoSenhaADM::class.java))
        }
    }

    // ─── LOGO (Carregamento Seguro para evitar Canvas Limit Crash) ───────────
    private fun carregarLogoSegura(imageView: ImageView) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val resId = R.drawable.unifor_marca
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeResource(resources, resId, options)

                // Reduz para no máximo 500px para evitar estouro de memória (Canvas Limit)
                val targetSize = 500
                var inSampleSize = 1
                if (options.outHeight > targetSize || options.outWidth > targetSize) {
                    val halfHeight = options.outHeight / 2
                    val halfWidth = options.outWidth / 2
                    while (halfHeight / inSampleSize >= targetSize && halfWidth / inSampleSize >= targetSize) {
                        inSampleSize *= 2
                    }
                }

                options.inJustDecodeBounds = false
                options.inSampleSize = inSampleSize
                val bitmap = BitmapFactory.decodeResource(resources, resId, options)

                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}