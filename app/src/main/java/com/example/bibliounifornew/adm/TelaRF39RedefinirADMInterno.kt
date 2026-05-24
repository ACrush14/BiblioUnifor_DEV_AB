package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF39RedefinirADMInterno : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf39_redefinir_adm_interno)

        val etSenha = findViewById<EditText>(R.id.editTextTextPassword)
        val etSenhaConfirmacao = findViewById<EditText>(R.id.editTextTextPasswordConfirmacao)
        val btnX = findViewById<TextView>(R.id.buttonX)
        val btnSalvar = findViewById<Button>(R.id.buttonSalvar)

        val iconOlhoSenha = findViewById<ImageView>(R.id.iconOlhoSenha)
        val iconOlhoSenhaConfirmacao = findViewById<ImageView>(R.id.iconOlhoSenhaConfirmacao)

        // TextViews de Erro
        val tvErroMin = findViewById<TextView>(R.id.tvErroMinCaracteres)
        val tvErroNum = findViewById<TextView>(R.id.tvErroNumero)
        val tvErroMaiusc = findViewById<TextView>(R.id.tvErroMaiuscula)
        val tvErroIgual = findViewById<TextView>(R.id.tvErroIgualAntiga)
        val tvErroDiferente = findViewById<TextView>(R.id.textErroDiferente)

        // Placeholder para "senha antiga" (simulação)
        val senhaAntiga = "12345678"

        var senhaVisivel = false
        var senhaConfirmacaoVisivel = false

        // Lógica dos olhos para mostrar/esconder senha
        iconOlhoSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                etSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iconOlhoSenha.setImageResource(R.drawable.ic_eye_open)
            } else {
                etSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconOlhoSenha.setImageResource(R.drawable.ic_eye_closed)
            }
            etSenha.setSelection(etSenha.text.length)
        }

        iconOlhoSenhaConfirmacao.setOnClickListener {
            senhaConfirmacaoVisivel = !senhaConfirmacaoVisivel
            if (senhaConfirmacaoVisivel) {
                etSenhaConfirmacao.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iconOlhoSenhaConfirmacao.setImageResource(R.drawable.ic_eye_open)
            } else {
                etSenhaConfirmacao.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconOlhoSenhaConfirmacao.setImageResource(R.drawable.ic_eye_closed)
            }
            etSenhaConfirmacao.setSelection(etSenhaConfirmacao.text.length)
        }

        // Validação dinâmica
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val pass = etSenha.text.toString()
                val confirm = etSenhaConfirmacao.text.toString()

                // Regras
                val temMinimo = pass.length >= 8
                val temNumero = pass.any { it.isDigit() }
                val temMaiuscula = pass.any { it.isUpperCase() }
                val eDiferenteDaAntiga = pass != senhaAntiga
                val coincidem = pass == confirm && pass.isNotEmpty()

                // Mostrar/Esconder erros
                tvErroMin.visibility = if (!temMinimo && pass.isNotEmpty()) View.VISIBLE else View.GONE
                tvErroNum.visibility = if (!temNumero && pass.isNotEmpty()) View.VISIBLE else View.GONE
                tvErroMaiusc.visibility = if (!temMaiuscula && pass.isNotEmpty()) View.VISIBLE else View.GONE
                tvErroIgual.visibility = if (!eDiferenteDaAntiga && pass.isNotEmpty()) View.VISIBLE else View.GONE
                tvErroDiferente.visibility = if (!coincidem && confirm.isNotEmpty()) View.VISIBLE else View.GONE

                // Habilitar botão apenas se tudo estiver ok
                val isAllValid = temMinimo && temNumero && temMaiuscula && eDiferenteDaAntiga && coincidem
                btnSalvar.isEnabled = isAllValid
                btnSalvar.alpha = if (isAllValid) 1.0f else 0.5f
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etSenha.addTextChangedListener(watcher)
        etSenhaConfirmacao.addTextChangedListener(watcher)

        btnSalvar.setOnClickListener {
            exibirPopupSucesso()
        }

        btnX.setOnClickListener {
            finish()
        }
    }

    private fun exibirPopupSucesso() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_alteracoes_salvas_sucesso)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val btnVoltar = dialog.findViewById<MaterialButton>(R.id.buttonVoltarPopup)

        btnVoltar.setOnClickListener {
            dialog.dismiss()
            // Volta para a tela de configurações ADM
            val intent = Intent(this, TelaRF38ConfigADM::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        dialog.show()

        // Ajuste de largura (90% da tela)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = layoutParams
    }
}
