package com.example.bibliounifornew.login

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF05RecuperacaoSenha : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf05_recuperacao_senha)

        // ─── LOGO ─────────────────────────────────────────────────────────────
        val imgLogo = findViewById<ImageView>(R.id.imageLogoRecSenha)
        if (imgLogo != null) {
            carregarLogoSegura(imgLogo)
        }

        // ─── COMPONENTES ──────────────────────────────────────────────────────
        val editEmail = findViewById<EditText>(R.id.editTextEmailRec)
        val btnEnviar = findViewById<MaterialButton>(R.id.buttonEnviarCOD)
        val txtErro   = findViewById<TextView>(R.id.textErroEmail)
        val txtVoltar = findViewById<TextView>(R.id.buttonVoltarLog)

        txtErro.visibility = View.GONE

        // ─── UX: limpa erro ao digitar ou focar ───────────────────────────────
        editEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                txtErro.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        editEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) txtErro.visibility = View.GONE
        }

        // ─── ENVIAR LINK DE RECUPERAÇÃO VIA FIREBASE ──────────────────────────
        btnEnviar.setOnClickListener {
            val email = editEmail.text.toString().trim()

            // 1) Validação: Campo vazio
            if (email.isEmpty()) {
                txtErro.text       = "Informe seu e-mail."
                txtErro.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // 2) Validação: Formato de e-mail
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                txtErro.text       = "Formato de e-mail inválido."
                txtErro.visibility = View.VISIBLE
                return@setOnClickListener
            }

            fecharTeclado()
            btnEnviar.isEnabled = false
            txtErro.visibility  = View.GONE

            // 3) Validação extra: Verifica se o e-mail existe no Firestore (Coleção Usuários)
            db.collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        btnEnviar.isEnabled = true
                        txtErro.text = "E-mail não cadastrado"
                        txtErro.visibility = View.VISIBLE
                    } else {
                        // 4) Chamada Oficial Firebase: Envia link de redefinição
                        enviarLinkFirebase(email, btnEnviar, txtErro)
                    }
                }
                .addOnFailureListener {
                    btnEnviar.isEnabled = true
                    txtErro.text = "Erro ao validar e-mail. Tente novamente."
                    txtErro.visibility = View.VISIBLE
                }
        }

        // ─── VOLTAR PARA LOGIN ────────────────────────────────────────────────
        txtVoltar.setOnClickListener {
            fecharTeclado()
            startActivity(Intent(this, TelaRF03LoginAluno::class.java))
            finish()
        }
    }

    private fun enviarLinkFirebase(email: String, btnEnviar: MaterialButton, txtErro: TextView) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                btnEnviar.isEnabled = true

                if (task.isSuccessful) {
                    // Navega para a tela de sucesso (Email Enviado)
                    startActivity(
                        Intent(
                            this,
                            TelaRF05EmailEnviado::class.java
                        )
                    )
                    finish()
                } else {
                    val exception = task.exception
                    val mensagemErro = when (exception) {
                        is FirebaseAuthInvalidUserException -> "Este e-mail não está cadastrado no sistema."
                        else -> "Erro ao enviar recuperação"
                    }
                    txtErro.text = mensagemErro
                    txtErro.visibility = View.VISIBLE
                }
            }
    }

    private fun fecharTeclado() {
        val view = currentFocus ?: View(this)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // ─── LOGO ─────────────────────────────────────────────────────────────────
    private fun carregarLogoSegura(imageView: ImageView) {
        try {
            val options = BitmapFactory.Options().apply {
                inSampleSize       = 4
                inJustDecodeBounds = false
            }
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.unifor_marca, options)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
