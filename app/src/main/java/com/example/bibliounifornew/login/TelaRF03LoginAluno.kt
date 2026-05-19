package com.example.bibliounifornew.login

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.usuario.TelaRF08DashboardUsuario

class TelaRF03LoginAluno : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf03_loginaluno)

        val imageLogo = findViewById<ImageView>(R.id.imageLogoLogin)
        carregarLogoSegura(imageLogo)

        // CAMPOS
        val email = findViewById<EditText>(R.id.editEmail)
        val senha = findViewById<EditText>(R.id.editSenha)

        // BOTÕES
        val botaoEntrar = findViewById<Button>(R.id.buttonEntrar)
        val mostrarSenha = findViewById<ImageView>(R.id.iconOlhoSenha)

        // TEXTOS
        val erro = findViewById<TextView>(R.id.textErroLogin)
        val criarConta = findViewById<TextView>(R.id.textCriarConta)
        val esqueceuSenha = findViewById<TextView>(R.id.textEsqueceuSenha)

        erro.visibility = View.GONE

        // LOGIN
        botaoEntrar.setOnClickListener {
            val textoEmail = email.text.toString().trim()
            val textoSenha = senha.text.toString().trim()

            erro.visibility = View.GONE

            // Base de dados mockada
            val usuariosValidos = mapOf(
                "teste@email.com" to "12345678",
                "anderson.link.crush@hotmail.com" to "123456",
                "1" to "2"
            )

            when {
                textoEmail.isEmpty() || textoSenha.isEmpty() -> {
                    erro.text = "Preencha todos os campos"
                    erro.visibility = View.VISIBLE
                }
                usuariosValidos[textoEmail] != textoSenha -> {
                    erro.text = "E-mail ou senha incorretos"
                    erro.visibility = View.VISIBLE
                }
                else -> {
                    Toast.makeText(this@TelaRF03LoginAluno, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@TelaRF03LoginAluno, TelaRF08DashboardUsuario::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        // CRIAR CONTA -> TelaRF04
        criarConta.setOnClickListener {
            val intent = Intent(this@TelaRF03LoginAluno, TelaRF04CadastroNovoUsuario::class.java)
            startActivity(intent)
        }

        // ESQUECEU SENHA -> TelaRF05
        esqueceuSenha.setOnClickListener {
            val intent = Intent(this@TelaRF03LoginAluno, com.example.bibliounifornew.login.TelaRF05RecuperacaoSenha::class.java)
            startActivity(intent)
        }

        // UX MELHORADA (remove erro ao focar)
        email.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) erro.visibility = View.GONE
        }

        senha.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) erro.visibility = View.GONE
        }

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
