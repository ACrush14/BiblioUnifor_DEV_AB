package com.example.bibliounifornew.adm

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF39RedefinirADMInterno : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf39_redefinir_adm_interno)

        // 1. Inicialização dos componentes (findViewById)
        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        val btnSalvar = findViewById<MaterialButton>(R.id.btnSalvarAlteracoes)

        val editSenhaAtual = findViewById<EditText>(R.id.editSenhaAtual)
        val editNovaSenha = findViewById<EditText>(R.id.editNovaSenha)
        val editConfirmarSenha = findViewById<EditText>(R.id.editConfirmarSenha)

        val eyeSenhaAtual = findViewById<ImageView>(R.id.eyeSenhaAtual)
        val eyeNovaSenha = findViewById<ImageView>(R.id.eyeNovaSenha)
        val eyeConfirmarSenha = findViewById<ImageView>(R.id.eyeConfirmarSenha)

        val errorSenhaAtual = findViewById<TextView>(R.id.errorSenhaAtual)
        val errorNovaSenha = findViewById<TextView>(R.id.errorNovaSenha)
        val errorConfirmarSenha = findViewById<TextView>(R.id.errorConfirmarSenha)

        // 2. Lógica para mostrar/ocultar senha (Olho)
        configurarOlhoSenha(editSenhaAtual, eyeSenhaAtual)
        configurarOlhoSenha(editNovaSenha, eyeNovaSenha)
        configurarOlhoSenha(editConfirmarSenha, eyeConfirmarSenha)

        // 3. Botão Voltar
        btnVoltar.setOnClickListener {
            finish()
        }

        // 4. Botão Salvar com Validações
        btnSalvar.setOnClickListener {
            val senhaAtual = editSenhaAtual.text.toString()
            val novaSenha = editNovaSenha.text.toString()
            val confirmarSenha = editConfirmarSenha.text.toString()

            // Resetar erros visualmente
            errorSenhaAtual.visibility = View.GONE
            errorNovaSenha.visibility = View.GONE
            errorConfirmarSenha.visibility = View.GONE

            var isValid = true

            // Validação: Senha Atual (Simulação: senha correta é 'admin123')
            if (senhaAtual.isEmpty()) {
                errorSenhaAtual.text = "Preencha este campo"
                errorSenhaAtual.visibility = View.VISIBLE
                isValid = false
            } else if (senhaAtual != "admin123") {
                errorSenhaAtual.text = "Senha inválida"
                errorSenhaAtual.visibility = View.VISIBLE
                isValid = false
            }

            // Validação: Nova Senha
            if (novaSenha.isEmpty()) {
                errorNovaSenha.text = "Preencha este campo"
                errorNovaSenha.visibility = View.VISIBLE
                isValid = false
            } else if (novaSenha.length < 8) {
                errorNovaSenha.text = "Mínimo 8 caracteres"
                errorNovaSenha.visibility = View.VISIBLE
                isValid = false
            }

            // Validação: Confirmar Senha
            if (confirmarSenha.isEmpty()) {
                errorConfirmarSenha.text = "Preencha este campo"
                errorConfirmarSenha.visibility = View.VISIBLE
                isValid = false
            } else if (confirmarSenha != novaSenha) {
                errorConfirmarSenha.text = "As senhas não coincidem"
                errorConfirmarSenha.visibility = View.VISIBLE
                isValid = false
            }

            // Se tudo estiver correto, abre o popup de sucesso
            if (isValid) {
                exibirPopupSucesso()
            }
        }
    }

    /**
     * Função reutilizável para a lógica do olho (mostrar/ocultar senha)
     */
    private fun configurarOlhoSenha(editText: EditText, imageView: ImageView) {
        var isVisible = false
        imageView.setOnClickListener {
            isVisible = !isVisible
            if (isVisible) {
                // Mostra a senha
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                imageView.setImageResource(R.drawable.ic_eye_open)
            } else {
                // Oculta a senha (formato •••••)
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                imageView.setImageResource(R.drawable.ic_eye_closed)
            }
            // Mantém o cursor no final do texto
            editText.setSelection(editText.text.length)
        }
    }

    /**
     * Exibe o popup de sucesso ao salvar alterações
     */
    private fun exibirPopupSucesso() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_salvar_sucesso)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false) // Impede fechar clicando fora

        val btnVoltarPopup = dialog.findViewById<MaterialButton>(R.id.buttonVoltarPopup)

        btnVoltarPopup.setOnClickListener {
            dialog.dismiss()
            finish() // Retorna para a tela de Configurações ADM
        }

        dialog.show()
    }
}