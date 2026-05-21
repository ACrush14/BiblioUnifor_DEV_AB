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
import com.example.bibliounifornew.adm.TelaRF28DashboardADM

class TelaRF23LoginADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf23_login_adm)

        val imageLogo = findViewById<ImageView>(R.id.imageLogoAdm)
        carregarLogoSegura(imageLogo)

        // CAMPOS
        val email = findViewById<EditText>(R.id.editEmailAdm)
        val senha = findViewById<EditText>(R.id.editSenhaAdm)
        val credential = findViewById<EditText>(R.id.editCredencialAdm)


        // BOTÃO
        val botaoEntrar = findViewById<Button>(R.id.buttonEntrarAdm)
        val bntMostraSenha = findViewById<ImageView>(R.id.iconOlhoSenhaAdm)

        // TEXTOS
        val erro = findViewById<TextView>(R.id.textErroAdm)
        val criarConta = findViewById<TextView>(R.id.textCriarContaAdm)
        val esqueceuSenha = findViewById<TextView>(R.id.textEsqueceuSenhaAdm)

        // LOGIN
        botaoEntrar.setOnClickListener {
            // TODO: Integrar com autenticação futuramente
            // Por enquanto, apenas navegação direta conforme solicitado
            val intent = Intent(this@TelaRF23LoginADM, TelaRF28DashboardADM::class.java)
            startActivity(intent)
            finish()
        }

        // CRIAR CONTA -> TelaRF27
        criarConta.setOnClickListener {
            val intent = Intent(this@TelaRF23LoginADM, TelaRF26NovaContaADM::class.java)
            startActivity(intent)
        }

        // ESQUECEU SENHA -> TelaRF24
        esqueceuSenha.setOnClickListener {
            val intent = Intent(this@TelaRF23LoginADM, TelaRF24RecuperacaoSenhaADM::class.java)
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

        bntMostraSenha.setOnClickListener {
            if (senhaVisivel) {

                // ESCONDER SENHA
                senha.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

                bntMostraSenha.setImageResource(R.drawable.ic_eye_closed)

                senhaVisivel = false

            } else {

                // MOSTRAR SENHA
                senha.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                bntMostraSenha.setImageResource(R.drawable.ic_eye_open)

                senhaVisivel = true
            }

            // Mantém cursor no final
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