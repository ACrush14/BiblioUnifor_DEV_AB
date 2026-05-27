package com.example.bibliounifornew.login

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.features.usuario.biblioteca.TelaRF08DashboardUsuario

class TelaRF03LoginAluno : AppCompatActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf03_loginaluno)

        val imageLogo = findViewById<ImageView>(R.id.imageLogoLogin)
        carregarLogoSegura(imageLogo)

        val email = findViewById<EditText>(R.id.editEmail)
        val senha = findViewById<EditText>(R.id.editSenha)
        val botaoEntrar = findViewById<Button>(R.id.buttonEntrar)
        val mostrarSenha = findViewById<ImageView>(R.id.iconOlhoSenha)
        val erro = findViewById<TextView>(R.id.textErroLogin)
        val criarConta = findViewById<TextView>(R.id.textCriarConta)
        val esqueceuSenha = findViewById<TextView>(R.id.textEsqueceuSenha)

        erro.visibility = View.GONE

        // ----------------------------------------------------
        // CLIQUE: LOGIN EMAIL E SENHA
        // ----------------------------------------------------
        botaoEntrar.setOnClickListener {
            val textoEmail = email.text.toString().trim()
            val textoSenha = senha.text.toString().trim()

            erro.visibility = View.GONE

            if (textoEmail.isEmpty() || textoSenha.isEmpty()) {
                erro.text = "Preencha todos os campos"
                erro.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // UX: Desativa o botão enquanto carrega
            botaoEntrar.isEnabled = false
            botaoEntrar.text = "Entrando..."

            // Login Real via Firebase
            authRepository.loginUsuario(textoEmail, textoSenha) { sucesso, mensagemOuUid ->
                botaoEntrar.isEnabled = true
                botaoEntrar.text = "Entrar"

                if (sucesso) {
                    Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, TelaRF08DashboardUsuario::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    erro.text = "E-mail ou senha incorretos"
                    erro.visibility = View.VISIBLE
                }
            }
        }

        // ----------------------------------------------------
        // NAVEGAÇÃO SECUNDÁRIA
        // ----------------------------------------------------
        criarConta.setOnClickListener {
            startActivity(Intent(this, TelaRF04CadastroNovoUsuario::class.java))
        }

        esqueceuSenha.setOnClickListener {
            startActivity(Intent(this, TelaRF05RecuperacaoSenha::class.java))
        }

        // UX MELHORADA (remove erro ao focar)
        email.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) erro.visibility = View.GONE }
        senha.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) erro.visibility = View.GONE }

        var senhaVisivel = false
        mostrarSenha.setOnClickListener {
            if (senhaVisivel) {
                senha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                mostrarSenha.setImageResource(R.drawable.ic_eye_closed)
            } else {
                senha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                mostrarSenha.setImageResource(R.drawable.ic_eye_open)
            }
            senhaVisivel = !senhaVisivel
            senha.setSelection(senha.text.length)
        }
    }

    private fun carregarLogoSegura(imageView: ImageView) {
        try {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 4
                inJustDecodeBounds = false
            }
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.unifor_marca, options)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}