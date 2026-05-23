package com.example.bibliounifornew.usuario

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF10RedefinirSenha : AppCompatActivity() {

    private var senhaVisivel = false
    private var confirmarVisivel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf10_redefinirsenha)

        // CAMPOS
        val editNovaSenha = findViewById<EditText>(R.id.editNovaSenha)
        val editConfirmarSenha = findViewById<EditText>(R.id.editConfirmarSenha)
        val btnSalvar = findViewById<MaterialButton>(R.id.buttonSalvarAlteracoes)

        // ERROS
        val textErroNovaSenha = findViewById<TextView>(R.id.textErroNovaSenha)
        val textErroConfirmacao = findViewById<TextView>(R.id.textErroConfirmacao)
        val textErroSenhaAntiga = findViewById<TextView>(R.id.textErroSenhaAntiga)
        val textRegrasSenha = findViewById<TextView>(R.id.textRegrasSenha)
        val textErroSenhas = findViewById<TextView>(R.id.textErroSenhas)

        // ÍCONES
        val iconOlhoNova = findViewById<ImageView>(R.id.iconOlhoNovaSenha)
        val iconOlhoConfirmar = findViewById<ImageView>(R.id.iconOlhoConfirmar)

        // --- LÓGICA DO OLHO (NOVA SENHA) ---
        iconOlhoNova.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                editNovaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iconOlhoNova.setImageResource(R.drawable.ic_eye_open)
            } else {
                editNovaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconOlhoNova.setImageResource(R.drawable.ic_eye_closed)
            }
            editNovaSenha.setSelection(editNovaSenha.text.length)
        }

        // --- LÓGICA DO OLHO (CONFIRMAR SENHA) ---
        iconOlhoConfirmar.setOnClickListener {
            confirmarVisivel = !confirmarVisivel
            if (confirmarVisivel) {
                editConfirmarSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iconOlhoConfirmar.setImageResource(R.drawable.ic_eye_open)
            } else {
                editConfirmarSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconOlhoConfirmar.setImageResource(R.drawable.ic_eye_closed)
            }
            editConfirmarSenha.setSelection(editConfirmarSenha.text.length)
        }

        // --- LIMPAR ERROS AO DIGITAR ---
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textErroNovaSenha.visibility = View.GONE
                textErroConfirmacao.visibility = View.GONE
                textErroSenhaAntiga.visibility = View.GONE
                textRegrasSenha.visibility = View.GONE
                textErroSenhas.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        editNovaSenha.addTextChangedListener(watcher)
        editConfirmarSenha.addTextChangedListener(watcher)

        // --- BOTÃO SALVAR ---
        btnSalvar.setOnClickListener {
            val novaSenha = editNovaSenha.text.toString()
            val confirma = editConfirmarSenha.text.toString()
            val senhaAtualMock = "12345678" // Simulação de senha atual

            // Esconder todos antes de validar
            textErroNovaSenha.visibility = View.GONE
            textErroConfirmacao.visibility = View.GONE
            textErroSenhaAntiga.visibility = View.GONE
            textRegrasSenha.visibility = View.GONE
            textErroSenhas.visibility = View.GONE

            when {
                novaSenha.isEmpty() -> {
                    textErroNovaSenha.text = "Campo obrigatório"
                    textErroNovaSenha.visibility = View.VISIBLE
                }
                confirma.isEmpty() -> {
                    textErroConfirmacao.text = "Campo obrigatório"
                    textErroConfirmacao.visibility = View.VISIBLE
                }
                novaSenha.length < 8 -> {
                    textRegrasSenha.text = "A senha deve conter pelo menos 8 caracteres"
                    textRegrasSenha.visibility = View.VISIBLE
                }
                !novaSenha.any { it.isDigit() } -> {
                    textRegrasSenha.text = "Um número"
                    textRegrasSenha.visibility = View.VISIBLE
                }
                !novaSenha.any { it.isUpperCase() } -> {
                    textRegrasSenha.text = "Uma letra maiúscula"
                    textRegrasSenha.visibility = View.VISIBLE
                }
                novaSenha == senhaAtualMock -> {
                    textErroSenhaAntiga.visibility = View.VISIBLE
                }
                novaSenha != confirma -> {
                    textErroSenhas.visibility = View.VISIBLE
                }
                else -> {
                    // SUCESSO - MOSTRAR POPUP
                    exibirPopupSucesso()
                }
            }
        }
    }

    private fun exibirPopupSucesso() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_sucesso_redefinir_senha)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)

        val btnVoltar = dialog.findViewById<Button>(R.id.buttonRetornarLogin)
        btnVoltar.text = "Voltar às Configurações"
        btnVoltar.setOnClickListener {
            dialog.dismiss()
            finish() // Fecha TelaRF10 e volta para RF09
        }

        dialog.show()
    }
}
