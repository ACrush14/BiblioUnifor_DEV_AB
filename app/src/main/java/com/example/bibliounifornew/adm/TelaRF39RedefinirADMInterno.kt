package com.example.bibliounifornew.adm

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF39RedefinirADMInterno : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf39_redefinir_adm_interno)

        val etSenha = findViewById<EditText>(R.id.editTextTextPassword)
        val etSenhaConfirmacao = findViewById<EditText>(R.id.editTextTextPasswordConfirmacao)
        val bntX = findViewById<TextView>(R.id.buttonX) // Alterado para TextView para evitar crash
        val bntSalvar = findViewById<Button>(R.id.buttonSalvar)

        bntSalvar.setOnClickListener {
            val s1 = etSenha.text.toString()
            val s2 = etSenhaConfirmacao.text.toString()

            if (s1 == s2 && s1.isNotEmpty()) {
                Toast.makeText(this, "Senha redefinida com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
            }
        }

        bntX.setOnClickListener {
            finish()
        }
    }
}