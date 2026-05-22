package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF09Configuracao : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf09_configuracao)

        val btnRedefinir = findViewById<MaterialButton>(R.id.buttonRedefinirSenha)
        val btnApagar = findViewById<MaterialButton>(R.id.buttonApagarConta)

        btnRedefinir.setOnClickListener {
            val intent = Intent(this@TelaRF09Configuracao, TelaRF10RedefinirSenha::class.java)
            startActivity(intent)
        }

        // LÓGICA PARA EDITAR USUÁRIO
        val btnEditarUsuario = findViewById<ImageView>(R.id.btnEditarUsuario)
        val textUsuario = findViewById<TextView>(R.id.textUsuario)

        btnEditarUsuario.setOnClickListener {
            val builder = AlertDialog.Builder(this@TelaRF09Configuracao)
            builder.setTitle("Editar Usuário")

            // Criar um EditText dinamicamente para o popup
            val input = EditText(this@TelaRF09Configuracao)
            input.hint = "Digite o novo nome ou email"
            input.setText(textUsuario.text)
            builder.setView(input)

            builder.setPositiveButton("Salvar") { dialog, _ ->
                val novoNome = input.text.toString().trim()
                if (novoNome.isNotEmpty()) {
                    textUsuario.text = novoNome
                }
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }

        // Exemplo de uso para evitar o warning de Unused variable
        btnApagar.setOnClickListener {
            // Implementação futura
        }
    }
}
