package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF23LoginADM
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class TelaRF38ConfigADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf38_config_adm)

        // Inicialização dos componentes da tela principal
        val btnVoltarDashboard = findViewById<ImageView>(R.id.btnVoltarDashboard)
        val btnRedefinirSenha = findViewById<MaterialButton>(R.id.btnRedefinirSenha)
        val btnApagarConta = findViewById<MaterialButton>(R.id.btnApagarConta)
        val editSenhaAtual = findViewById<EditText>(R.id.editSenhaAtual)
        val iconOlhoSenha = findViewById<ImageView>(R.id.iconOlhoSenhaAtual)

        // Campo de senha na tela principal é apenas visual, desativado para edição direta
        editSenhaAtual.isEnabled = false

        // Lógica de voltar
        btnVoltarDashboard?.setOnClickListener {
            finish()
        }

        // FLUXO 1: Redefinir Senha
        btnRedefinirSenha?.setOnClickListener {
            val intent = Intent(this, TelaRF39RedefinirADMInterno::class.java)
            startActivity(intent)
        }

        // FLUXO 2: Apagar Conta
        btnApagarConta?.setOnClickListener {
            exibirPopupApagarConta()
        }

        // Lógica de visibilidade da senha (apenas visual para a senha atual mockada)
        var senhaVisivel = false
        iconOlhoSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                editSenhaAtual.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iconOlhoSenha.setImageResource(R.drawable.ic_eye_open)
            } else {
                editSenhaAtual.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconOlhoSenha.setImageResource(R.drawable.ic_eye_closed)
            }
            editSenhaAtual.setSelection(editSenhaAtual.text.length)
        }
    }

    /**
     * Função para exibir o popup de confirmação para apagar conta
     */
    private fun exibirPopupApagarConta() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_apagar_conta_adm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancelar = dialog.findViewById<MaterialButton>(R.id.buttonCancelarApagarContaADM)
        val btnConfirmar = dialog.findViewById<MaterialButton>(R.id.buttonConfirmarApagarContaADM)
        val editSenha = dialog.findViewById<TextInputEditText>(R.id.editSenhaApagarContaADM)

        // Ao clicar em cancelar, apenas fecha o popup
        btnCancelar?.setOnClickListener {
            dialog.dismiss()
        }

        // Ao clicar em apagar, exibe toast, limpa sessão e vai para login
        btnConfirmar?.setOnClickListener {
            val senhaDigitada = editSenha?.text?.toString()?.trim() ?: ""
            if (senhaDigitada.isEmpty()) {
                Toast.makeText(this, "Por favor, digite sua senha", Toast.LENGTH_SHORT).show()
            } else {
                dialog.dismiss()

                Toast.makeText(this, "Conta removida", Toast.LENGTH_SHORT).show()

                // Navegação para Tela de Login ADM (RF23)
                val intent = Intent(this, TelaRF23LoginADM::class.java)
                // Limpa a pilha de atividades para não permitir voltar
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        dialog.show()
    }
}