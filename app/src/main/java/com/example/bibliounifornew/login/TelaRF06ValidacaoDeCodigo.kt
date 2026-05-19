package com.example.bibliounifornew.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF06ValidacaoDeCodigo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf06_validacao_de_codigo)

        val editCodigo = findViewById<EditText>(R.id.editTextCodigo)
        val textErro = findViewById<TextView>(R.id.textErroCodigo)
        val buttonReenviarCod = findViewById<TextView>(R.id.textReenviarCodigo)
        val buttonEnviarCodigo = findViewById<Button>(R.id.buttonEnviarCodigo)

        // Inicialmente o erro fica invisível
        textErro.visibility = View.INVISIBLE

        buttonEnviarCodigo.setOnClickListener {
            val codigoDigitado = editCodigo.text.toString().trim()

            // Simulação de código correto (ex: 123456)
            if (codigoDigitado == "123456") {
                textErro.visibility = View.INVISIBLE
                val intent = Intent(this@TelaRF06ValidacaoDeCodigo, TelaRF07RedefinirSenha::class.java)
                startActivity(intent)
            } else {
                // Código incorreto
                textErro.visibility = View.VISIBLE
                textErro.text = "Código incorreto. Verifique seu e-mail."
            }
        }

        buttonReenviarCod.setOnClickListener {
            Toast.makeText(this@TelaRF06ValidacaoDeCodigo, "Código enviado!", Toast.LENGTH_SHORT).show()
        }
    }
}
