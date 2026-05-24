package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.text.InputType
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF02Intermediaria
import com.example.bibliounifornew.login.TelaRF23LoginADM
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class TelaRF38ConfigADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf38_config_adm)


        // Botões específicos da tela de configuração
        val btnRedefinirSenha = findViewById<MaterialButton>(R.id.btnRedefinirSenha)
        val btnApagarConta = findViewById<MaterialButton>(R.id.btnApagarConta)

        btnRedefinirSenha?.setOnClickListener {
            val intent = Intent(this@TelaRF38ConfigADM, TelaRF39RedefinirADMInterno::class.java)
            startActivity(intent)
        }

        btnApagarConta?.setOnClickListener {
            exibirPopupApagarConta()
        }

        // Senha e Olho
        val editSenhaAtual = findViewById<EditText>(R.id.editSenhaAtual)
        val iconOlhoSenha = findViewById<ImageView>(R.id.iconOlhoSenhaAtual)

        editSenhaAtual.isEnabled = false

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

    private fun exibirPopupApagarConta() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_apagar_conta_adm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnConfirmar = dialog.findViewById<MaterialButton>(R.id.buttonConfirmarApagarContaADM)
        val btnCancelar = dialog.findViewById<MaterialButton>(R.id.buttonCancelarApagarContaADM)
        val editSenha = dialog.findViewById<TextInputEditText>(R.id.editSenhaApagarContaADM)

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmar.setOnClickListener {
            val senha = editSenha.text.toString()
            if (senha.isNotEmpty()) {
                Toast.makeText(this, "Conta apagada com sucesso", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                val intent = Intent(this, TelaRF23LoginADM::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(this, "Por favor, digite sua senha", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()

        // Ajusta a largura do popup para ser responsivo (90% da largura da tela)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = layoutParams
    }
}