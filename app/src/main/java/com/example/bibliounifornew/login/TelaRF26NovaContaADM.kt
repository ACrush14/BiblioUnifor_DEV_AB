package com.example.bibliounifornew.login

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF26NovaContaADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf26_nova_conta_adm)

        val edtNomeCompleto = findViewById<EditText>(R.id.editNomeCompletoAdm)
        val edtNomeUsuario = findViewById<EditText>(R.id.editNomeUsuarioAdm)
        val edtEmail = findViewById<EditText>(R.id.editEmailAdmCadastro)
        val edtCredencial = findViewById<EditText>(R.id.editCredencialAdmCadastro)
        val edtSenha = findViewById<EditText>(R.id.editSenhaAdmCadastro)
        val edtConfirmaSenha = findViewById<EditText>(R.id.editConfirmarSenhaAdm)

        val txtErroEmail = findViewById<TextView>(R.id.textErroEmailAdmCadastro)
        val txtErroCredencial = findViewById<TextView>(R.id.textErroCredencialAdm)
        val txtErroSenha = findViewById<TextView>(R.id.textRegrasSenhaAdm)

        val btnCriar = findViewById<Button>(R.id.buttonCriarContaAdm)
        val txtEntreAqui = findViewById<TextView>(R.id.textEntreAquiAdm)

        // MOSTRAR/OCULTAR SENHA
        val iconOlhoSenha = findViewById<ImageView>(R.id.iconOlhoSenhaAdmCadastro)
        val iconOlhoConfirma = findViewById<ImageView>(R.id.iconOlhoConfirmarSenhaAdm)

        var senhaVisivel = false
        iconOlhoSenha.setOnClickListener {
            if (senhaVisivel) {

                // ESCONDER
                edtSenha.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

                iconOlhoSenha.setImageResource(R.drawable.ic_eye_closed)

                senhaVisivel = false

            } else {

                // MOSTRAR
                edtSenha.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                iconOlhoSenha.setImageResource(R.drawable.ic_eye_open)

                senhaVisivel = true
            }
            edtSenha.setSelection(edtSenha.text.length)
        }

        var confirmaVisivel = false
        iconOlhoConfirma.setOnClickListener {
            if (confirmaVisivel) {
                // ESCONDER
                edtConfirmaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconOlhoConfirma.setImageResource(R.drawable.ic_eye_closed)
                confirmaVisivel = false
            } else {
                // MOSTRAR
                edtConfirmaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iconOlhoConfirma.setImageResource(R.drawable.ic_eye_open)
                confirmaVisivel = true
            }
            edtConfirmaSenha.setSelection(edtConfirmaSenha.text.length)
        }

        btnCriar.setOnClickListener {
            txtErroEmail.visibility = View.GONE
            txtErroCredencial.visibility = View.GONE
            txtErroSenha.visibility = View.GONE

            val email = edtEmail.text.toString().trim()
            val credencial = edtCredencial.text.toString().trim()
            val senha1 = edtSenha.text.toString()
            val senha2 = edtConfirmaSenha.text.toString()

            var temErro = false

            if (email.isEmpty() || !email.contains("@")) {
                txtErroEmail.visibility = View.VISIBLE
                temErro = true
            }

            if (credencial.isEmpty()) {
                txtErroCredencial.visibility = View.VISIBLE
                temErro = true
            }

            val temMaiuscula = senha1.any { it.isUpperCase() }
            val temNumero = senha1.any { it.isDigit() }
            val temTamanho = senha1.length >= 8

            if (!temTamanho || !temNumero || !temMaiuscula || senha1 != senha2) {
                txtErroSenha.visibility = View.VISIBLE
                temErro = true
            }

            if (!temErro) {
                mostrarPopUpSucesso()
            }
        }

        txtEntreAqui.setOnClickListener {
            finish()
        }
    }

    private fun mostrarPopUpSucesso() {
        val dialog = Dialog(this@TelaRF26NovaContaADM)
        dialog.setContentView(R.layout.popup_sucesso_cadastro)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)

        // BOTÃO DO POPUP
        val botaoRetornar = dialog.findViewById<Button>(R.id.btnRetorneLogin)
        botaoRetornar.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }
}