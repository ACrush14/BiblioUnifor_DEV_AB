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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.example.bibliounifornew.features.adm.dashboard.TelaRF28DashboardADM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF23LoginADM : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

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

            if (sEmail.isEmpty() || sSenha.isEmpty() || sCredencial.isEmpty()) {
                erro.text       = "Preencha todos os campos"
                erro.visibility = View.VISIBLE
                return@setOnClickListener
            }

            botaoEntrar.isEnabled = false
            botaoEntrar.text = "Verificando..."

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // ── Etapa 1: Validação Direta da Credencial (Bypass para Faculdade) ──
                    if (sCredencial != "DevsAB") {
                        withContext(Dispatchers.Main) {
                            if (isFinishing) return@withContext
                            botaoEntrar.isEnabled = true
                            botaoEntrar.text = "Entrar"
                            erro.text       = "Credencial de acesso inválida"
                            erro.visibility = View.VISIBLE
                        }
                        return@launch
                    }

                    // ── Etapa 2: Firebase Auth ────────────────────────────────────
                    val authResult = FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(sEmail, sSenha)
                        .await()

                    val uid = authResult.user?.uid ?: run {
                        withContext(Dispatchers.Main) {
                            if (isFinishing) return@withContext
                            botaoEntrar.isEnabled = true
                            botaoEntrar.text = "Entrar"
                            erro.text       = "E-mail ou senha incorretos"
                            erro.visibility = View.VISIBLE
                        }
                        return@launch
                    }

                    // ── Etapa 3: RBAC (role + contaAtiva) ────────────────────────
                    val userDoc = db.collection("usuarios").document(uid).get().await()
                    val role    = userDoc.getString("role") ?: ""

                    withContext(Dispatchers.Main) {
                        if (isFinishing) return@withContext
                        when {
                            !userDoc.exists() -> {
                                FirebaseAuth.getInstance().signOut()
                                botaoEntrar.isEnabled = true
                                botaoEntrar.text = "Entrar"
                                erro.text       = "Perfil não encontrado. Registre-se como ADM primeiro."
                                erro.visibility = View.VISIBLE
                            }
                            role == "adm" -> {
                                val contaAtiva = userDoc.getBoolean("contaAtiva") ?: true
                                if (!contaAtiva) {
                                    FirebaseAuth.getInstance().signOut()
                                    botaoEntrar.isEnabled = true
                                    botaoEntrar.text = "Entrar"
                                    erro.text       = "Esta conta de administrador foi desativada."
                                    erro.visibility = View.VISIBLE
                                } else {
                                    startActivity(
                                        Intent(this@TelaRF23LoginADM, TelaRF28DashboardADM::class.java)
                                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    )
                                    finish()
                                }
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

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (isFinishing) return@withContext
                        botaoEntrar.isEnabled = true
                        botaoEntrar.text = "Entrar"
                        erro.text = when {
                            e.message?.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) == true
                                    || e.message?.contains("password is invalid", ignoreCase = true) == true
                                    || e.message?.contains("no user record", ignoreCase = true) == true
                                -> "E-mail ou senha incorretos"
                            e.message?.contains("network", ignoreCase = true) == true
                                -> "Sem conexão. Verifique a internet e tente novamente."
                            e.message?.contains("too many", ignoreCase = true) == true
                                -> "Muitas tentativas. Aguarde alguns minutos."
                            else -> "Não foi possível verificar as permissões. Tente novamente."
                        }
                        erro.visibility = View.VISIBLE
                    }
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