package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF10RedefinirSenha : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf10_redefinirsenha)

        // CAMPOS
        val editNovaSenha = findViewById<EditText>(R.id.editNovaSenha)
        val editConfirmarSenha = findViewById<EditText>(R.id.editConfirmarSenha)
        val btnSalvar = findViewById<MaterialButton>(R.id.buttonSalvarAlteracoes)

        val textErroNovaSenha = findViewById<TextView>(R.id.textErroNovaSenha)
        val textErroConfirmacao = findViewById<TextView>(R.id.textErroConfirmacao)
        val textErroSenhas = findViewById<TextView>(R.id.textErroSenhas)

        // Inicialmente ocultar erros
        textErroNovaSenha.visibility = View.GONE
        textErroConfirmacao.visibility = View.GONE
        textErroSenhas.visibility = View.GONE

        // 🔥 VALIDAÇÃO + SALVAR
        btnSalvar.setOnClickListener {
            val senha = editNovaSenha.text.toString()
            val confirmar = editConfirmarSenha.text.toString()

            textErroNovaSenha.visibility = View.GONE
            textErroConfirmacao.visibility = View.GONE
            textErroSenhas.visibility = View.GONE

            var valido = true

            if (senha.isEmpty()) {
                textErroNovaSenha.visibility = View.VISIBLE
                textErroNovaSenha.text = "Campo obrigatório"
                valido = false
            } else if (senha.length < 8) {
                textErroNovaSenha.visibility = View.VISIBLE
                textErroNovaSenha.text = "Mínimo 8 caracteres"
                valido = false
            }

            if (confirmar.isEmpty()) {
                textErroConfirmacao.visibility = View.VISIBLE
                textErroConfirmacao.text = "Campo obrigatório"
                valido = false
            } else if (senha != confirmar) {
                textErroSenhas.visibility = View.VISIBLE
                valido = false
            }

            if (valido) {
                Toast.makeText(this@TelaRF10RedefinirSenha, "Senha alterada!", Toast.LENGTH_SHORT).show()

                // VOLTA PRA RF09
                val intent = Intent(this@TelaRF10RedefinirSenha, TelaRF09Configuracao::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
