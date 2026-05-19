package com.example.bibliounifornew.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF25RedefinirSenhaADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf25_redefinir_senha_adm)

        // CAMPOS
        val senhaNova = findViewById<EditText>(R.id.editSenhaNova)
        val confirmarSenha = findViewById<EditText>(R.id.editConfirmarSenha)

        // BOTÃO
        val botaoRedefinir = findViewById<MaterialButton>(R.id.buttonRedefinirSenha)

        // ÍCONES
        val olhoSenha = findViewById<ImageView>(R.id.iconOlhoSenhaNova)
        val olhoConfirmar = findViewById<ImageView>(R.id.iconOlhoConfirmarSenha)

        // ERROS
        val erroSenha1 = findViewById<TextView>(R.id.textErroSenha1)
        val erroSenha2 = findViewById<TextView>(R.id.textErroSenha2)
        val erroSenhaIgual = findViewById<TextView>(R.id.textErroSenhaIgual)
        val erroSenhaDiferente = findViewById<TextView>(R.id.textErroSenhaDiferente)

        // SENHA ANTIGA MOCKADA
        val senhaAntiga = "12345678"

        // CONTROLE VISIBILIDADE SENHA
        var senhaVisivel = false
        var confirmarVisivel = false

        // OLHO SENHA
        olhoSenha.setOnClickListener {
            if (senhaVisivel) {
                // ESCONDER
                senhaNova.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                olhoSenha.setImageResource(R.drawable.ic_eye_closed)
                senhaVisivel = false
            } else {
                // MOSTRAR
                senhaNova.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                olhoSenha.setImageResource(R.drawable.ic_eye_open)
                senhaVisivel = true
            }
            senhaNova.setSelection(senhaNova.text.length)
        }

        // OLHO CONFIRMAR
        olhoConfirmar.setOnClickListener {
            if (confirmarVisivel) {
                // ESCONDER
                confirmarSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                olhoConfirmar.setImageResource(R.drawable.ic_eye_closed)
                confirmarVisivel = false
            } else {
                // MOSTRAR
                confirmarSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                olhoConfirmar.setImageResource(R.drawable.ic_eye_open)
                confirmarVisivel = true
            }
            confirmarSenha.setSelection(confirmarSenha.text.length)
        }

        // BOTÃO REDEFINIR
        botaoRedefinir.setOnClickListener {

            val textoSenha = senhaNova.text.toString().trim()
            val textoConfirmar = confirmarSenha.text.toString().trim()

            // RESET ERROS
            erroSenha1.visibility = View.GONE
            erroSenha2.visibility = View.GONE
            erroSenhaIgual.visibility = View.GONE
            erroSenhaDiferente.visibility = View.GONE

            when {

                textoSenha.isEmpty() -> {
                    erroSenha1.text = "Digite uma senha"
                    erroSenha1.visibility = View.VISIBLE
                }

                textoConfirmar.isEmpty() -> {
                    erroSenha2.text = "Confirme a senha"
                    erroSenha2.visibility = View.VISIBLE
                }

                textoSenha == senhaAntiga -> {
                    erroSenhaIgual.visibility = View.VISIBLE
                }

                textoSenha != textoConfirmar -> {
                    erroSenhaDiferente.visibility = View.VISIBLE
                }

                textoSenha.length < 8 -> {
                    erroSenha1.text = "A senha deve ter pelo menos 8 caracteres"
                    erroSenha1.visibility = View.VISIBLE
                }

                !textoSenha.any { it.isDigit() } -> {
                    erroSenha1.text = "A senha deve conter um número"
                    erroSenha1.visibility = View.VISIBLE
                }

                !textoSenha.any { it.isUpperCase() } -> {
                    erroSenha1.text = "A senha deve conter letra maiúscula"
                    erroSenha1.visibility = View.VISIBLE
                }

                else -> {
                    mostrarPopupSucesso()
                }
            }
        }

        // UX
        senhaNova.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                erroSenha1.visibility = View.GONE
                erroSenhaIgual.visibility = View.GONE
                erroSenhaDiferente.visibility = View.GONE
            }
        }

        confirmarSenha.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                erroSenha2.visibility = View.GONE
                erroSenhaDiferente.visibility = View.GONE
            }
        }

    }
    private fun mostrarPopupSucesso() {

        val dialog = android.app.Dialog(this@TelaRF25RedefinirSenhaADM)

        dialog.setContentView(R.layout.popup_confirmar_redefinir_senha)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // BOTÃO DO POPUP
        val botaoRetornar =
            dialog.findViewById<Button>(R.id.btnRetornarLogin)

        botaoRetornar.setOnClickListener {

            val intent = Intent(this@TelaRF25RedefinirSenhaADM, TelaRF23LoginADM::class.java)

            startActivity(intent)

            dialog.dismiss()

            finish()
        }

        dialog.show()
    }
}