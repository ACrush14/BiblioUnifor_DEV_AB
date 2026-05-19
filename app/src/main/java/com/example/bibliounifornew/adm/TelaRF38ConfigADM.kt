package com.example.bibliounifornew.adm

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adm.TelaRF39RedefinirADMInterno
import com.example.bibliounifornew.login.TelaRF02Intermediaria
import com.google.android.material.button.MaterialButton

class TelaRF38ConfigADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf38_config_adm)


        // Botões específicos da tela de configuração
        val btnVoltarDashboard = findViewById<MaterialButton>(R.id.btnVoltarDashboard)
        val btnVoltar = findViewById<MaterialButton>(R.id.btnVoltar)
        val btnRedefinirSenha = findViewById<MaterialButton>(R.id.btnRedefinirSenha)
        val btnApagarConta = findViewById<MaterialButton>(R.id.btnApagarConta)

        btnVoltarDashboard?.setOnClickListener {
            val intent = Intent(this@TelaRF38ConfigADM, TelaRF28DashboardADM::class.java)
            startActivity(intent)
        }

        btnVoltar?.setOnClickListener {
            finish() // Fecha a tela atual e volta para a anterior
        }

        btnRedefinirSenha?.setOnClickListener {
            val intent = Intent(this@TelaRF38ConfigADM, TelaRF39RedefinirADMInterno::class.java)
            startActivity(intent)
        }

        btnApagarConta?.setOnClickListener {
            // Lógica para apagar conta ou logout
            val intent = Intent(this@TelaRF38ConfigADM, TelaRF02Intermediaria::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
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
}