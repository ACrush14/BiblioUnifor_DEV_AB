package com.example.bibliounifornew.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF07RedefinirSenha : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf07_redefinicao_de_senha)

        val editSenhaNova = findViewById<EditText>(R.id.editSenhaNova)
        val editConfirmarSenha = findViewById<EditText>(R.id.editConfirmarSenha)
        val btnConfirmar = findViewById<MaterialButton>(R.id.buttonRedefinirSenha)

        val erroSenha1 = findViewById<TextView>(R.id.textErroSenha1)
        val erroSenha2 = findViewById<TextView>(R.id.textErroSenha2)

        val textErroDiferente = findViewById<TextView>(R.id.textErroSenhaDiferente)
        val textErroIgual = findViewById<TextView>(R.id.textErroSenhaIgual)
        val textErroRequisitos = findViewById<TextView>(R.id.textRegrasSenha)

        val bntOlhoSenha = findViewById<ImageView>(R.id.iconOlhoSenhaNova)
        val bntOlhoConfirmarSenha = findViewById<ImageView>(R.id.iconOlhoConfirmarSenha)

        // Inicializa erros como invisíveis
        textErroDiferente.visibility = View.GONE
        textErroIgual.visibility = View.GONE
        textErroRequisitos.visibility = View.GONE
        erroSenha2.visibility = View.GONE
        erroSenha1.visibility = View.GONE

        btnConfirmar.setOnClickListener {
            val senhanova = editSenhaNova.text.toString()
            val confirmarsenha = editConfirmarSenha.text.toString()

            val senhaValida = validarSenha(senhanova)
            val senhasIguais = senhanova == confirmarsenha

            if (senhaValida && senhasIguais) {
                mostrarPopupSucesso()
            } else if (senhanova.isEmpty() || confirmarsenha.isEmpty()) {
                erroSenha2.visibility = View.VISIBLE
                erroSenha1.visibility = View.VISIBLE
            } else {
                if (!senhasIguais) {
                    textErroDiferente.visibility = View.VISIBLE
                    textErroDiferente.text = "As senhas não coincidem"
                } else {
                    textErroDiferente.visibility = View.GONE
                }

                if (!senhaValida) {
                    Toast.makeText(this@TelaRF07RedefinirSenha, "A senha não atende aos requisitos", Toast.LENGTH_SHORT).show()
                    textErroRequisitos.setTextColor(ContextCompat.getColor(this@TelaRF07RedefinirSenha, android.R.color.holo_red_dark))
                } else {
                    textErroRequisitos.setTextColor(ContextCompat.getColor(this@TelaRF07RedefinirSenha, android.R.color.darker_gray))
                }
            }
        }

        var senhaVisivel = false
        var confirmarSenhaVisivel = false

        bntOlhoSenha.setOnClickListener {
            if (senhaVisivel) {
                editSenhaNova.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                bntOlhoSenha.setImageResource(R.drawable.ic_eye_closed)
                senhaVisivel = false
            } else {
                editSenhaNova.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                bntOlhoSenha.setImageResource(R.drawable.ic_eye_open)
                senhaVisivel = true
            }
            editSenhaNova.setSelection(editSenhaNova.text.length)
        }

        bntOlhoConfirmarSenha.setOnClickListener {
            if (confirmarSenhaVisivel) {
                editConfirmarSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                bntOlhoConfirmarSenha.setImageResource(R.drawable.ic_eye_closed)
                confirmarSenhaVisivel = false
            } else {
                editConfirmarSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                bntOlhoConfirmarSenha.setImageResource(R.drawable.ic_eye_open)
                confirmarSenhaVisivel = true
            }
            editConfirmarSenha.setSelection(editConfirmarSenha.text.length)
        }
    }

    private fun validarSenha(senha: String): Boolean {
        val temOitoDigitos = senha.length >= 8
        val temMaiuscula = senha.any { it.isUpperCase() }
        val temNumero = senha.any { it.isDigit() }
        return temOitoDigitos && temMaiuscula && temNumero
    }

    private fun mostrarPopupSucesso() {
        val dialog = android.app.Dialog(this@TelaRF07RedefinirSenha)
        dialog.setContentView(R.layout.popup_confirmar_redefinir_senha)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val botaoRetornar = dialog.findViewById<Button>(R.id.btnRetornarLogin)
        botaoRetornar.setOnClickListener {
            val intent = Intent(this@TelaRF07RedefinirSenha, TelaRF03LoginAluno::class.java)
            startActivity(intent)
            dialog.dismiss()
            finish()
        }
        dialog.show()
    }
}
