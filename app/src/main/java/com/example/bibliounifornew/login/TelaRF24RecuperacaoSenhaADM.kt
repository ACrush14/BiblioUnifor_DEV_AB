package com.example.bibliounifornew.login

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class TelaRF24RecuperacaoSenhaADM : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf24_recuperacao_senha_adm)

        // ─── LOGO ─────────────────────────────────────────────────────────────
        carregarLogoSegura(findViewById(R.id.imageLogoRecuperar))

        // ─── COMPONENTES ──────────────────────────────────────────────────────
        val editEmail = findViewById<EditText>(R.id.editEmailRecuperar)
        val btnEnviar = findViewById<MaterialButton>(R.id.buttonEnviarCodigo)
        val txtErro   = findViewById<TextView>(R.id.textErroEmailRecuperar)
        val txtVoltar = findViewById<TextView>(R.id.textVoltarLogin)

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

        // ─── ENVIAR E-MAIL DE RECUPERAÇÃO ─────────────────────────────────────
        btnEnviar.setOnClickListener {
            val email = editEmail.text.toString().trim()

            // 1) Campo vazio
            if (email.isEmpty()) {
                txtErro.text       = "Informe seu e-mail."
                txtErro.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // 2) Formato inválido (validação client-side)
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                txtErro.text       = "Formato de e-mail inválido."
                txtErro.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // 3) Chama Firebase — desabilita botão para evitar double-tap
            btnEnviar.isEnabled = false
            txtErro.visibility  = View.GONE

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    // Firebase não informa se o e-mail existe ou não (segurança).
                    // Em ambos os casos exibimos a mensagem de sucesso.
                    btnEnviar.isEnabled = true
                    Toast.makeText(
                        this,
                        "E-mail de recuperação enviado com sucesso!",
                        Toast.LENGTH_LONG
                    ).show()
                    // Volta para o Login ADM
                    startActivity(Intent(this, TelaRF23LoginADM::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    btnEnviar.isEnabled = true
                    val mensagem = when (e) {
                        is FirebaseAuthInvalidCredentialsException ->
                            "Formato de e-mail inválido."
                        else ->
                            "Erro ao enviar e-mail. Verifique sua conexão e tente novamente."
                    }
                    txtErro.text       = mensagem
                    txtErro.visibility = View.VISIBLE
                }
        }

        // ─── VOLTAR PARA LOGIN ADM ────────────────────────────────────────────
        txtVoltar.setOnClickListener {
            finish()
        }
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
