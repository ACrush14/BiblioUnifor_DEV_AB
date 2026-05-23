package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF01BemVindo
import com.google.android.material.button.MaterialButton

class TelaRF09Configuracao : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf09_configuracao)

        // Botões principais
        val btnRedefinir = findViewById<MaterialButton>(R.id.buttonRedefinirSenha)
        val btnApagar = findViewById<MaterialButton>(R.id.buttonApagarConta)

        // Campos e ícones de edição
        val editNome = findViewById<EditText>(R.id.editNome)
        val editUsuario = findViewById<EditText>(R.id.editUsuario)
        val editBio = findViewById<EditText>(R.id.editBio)
        
        val iconEditNome = findViewById<ImageView>(R.id.iconEditNome)
        val iconEditUsuario = findViewById<ImageView>(R.id.iconEditUsuario)
        val iconEditBio = findViewById<ImageView>(R.id.iconEditBio)

        // Senha e Olho
        val editSenhaAtual = findViewById<EditText>(R.id.editSenhaAtual)
        val iconOlhoSenha = findViewById<ImageView>(R.id.iconOlhoSenhaAtual)

        // 1. ESTADO INICIAL: Campos desabilitados
        editNome.isEnabled = false
        editUsuario.isEnabled = false
        editBio.isEnabled = false
        editSenhaAtual.isEnabled = false

        // 2. LÓGICA DO LÁPIS (EDITAR)
        iconEditNome.setOnClickListener {
            editNome.isEnabled = true
            editNome.requestFocus()
            editNome.setSelection(editNome.text.length)
        }

        iconEditUsuario.setOnClickListener {
            editUsuario.isEnabled = true
            editUsuario.requestFocus()
            editUsuario.setSelection(editUsuario.text.length)
        }

        iconEditBio.setOnClickListener {
            editBio.isEnabled = true
            editBio.requestFocus()
            editBio.setSelection(editBio.text.length)
        }

        // 3. LÓGICA DO OLHO
        var senhaVisivel = false
        iconOlhoSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                editSenhaAtual.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iconOlhoSenha.setImageResource(R.drawable.ic_eye_open)
            } else {
                editSenhaAtual.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconOlhoSenha.setImageResource(R.drawable.ic_eye_closed)
            }
            editSenhaAtual.setSelection(editSenhaAtual.text.length)
        }

        // 4. REDEFINIR SENHA (ABRE RF10)
        btnRedefinir.setOnClickListener {
            val intent = Intent(this, TelaRF10RedefinirSenha::class.java)
            startActivity(intent)
        }

        // 5. APAGAR CONTA
        btnApagar.setOnClickListener {
            exibirPopupApagarConta()
        }
    }

    private fun exibirPopupApagarConta() {
        val dialogView = layoutInflater.inflate(R.layout.popup_apagar_conta, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnConfirmar = dialogView.findViewById<MaterialButton>(R.id.buttonConfirmarApagarConta)
        val btnCancelar = dialogView.findViewById<TextView>(R.id.textCancelarApagarConta)
        val editSenha = dialogView.findViewById<EditText>(R.id.editSenhaPopup)
        val iconOlho = dialogView.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)

        // Lógica do olho no popup
        var senhaVisivelPopup = false
        iconOlho.setOnClickListener {
            senhaVisivelPopup = !senhaVisivelPopup
            if (senhaVisivelPopup) {
                editSenha.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iconOlho.setImageResource(R.drawable.ic_eye_open)
            } else {
                editSenha.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconOlho.setImageResource(R.drawable.ic_eye_closed)
            }
            editSenha.setSelection(editSenha.text.length)
        }

        btnConfirmar.setOnClickListener {
            // Navegar para a tela de Bem-Vindo (RF01) e limpar a pilha de atividades
            val intent = Intent(this, TelaRF01BemVindo::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
