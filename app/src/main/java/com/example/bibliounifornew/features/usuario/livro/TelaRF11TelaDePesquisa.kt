package com.example.bibliounifornew.features.usuario.livro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper

class TelaRF11TelaDePesquisa : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf11_teladepesquisa)

        val editPesquisa = findViewById<EditText>(R.id.editPesquisarLivro)
        val btnProcurar = findViewById<Button>(R.id.buttonProcurar)

        btnProcurar.setOnClickListener {
            val termoBusca = editPesquisa.text.toString().trim()

            if (termoBusca.isNotEmpty()) {
                // Passa o termo que o usuário digitou para a tela de resultados
                val intent = Intent(this, TelaRF11_1_ResultadoPesquisa::class.java)
                intent.putExtra("TERMO_PESQUISA", termoBusca)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Digite o nome de um livro ou autor", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar Barra de Navegação
        NavigationHelper.configurarBarraNavegacao(this)
    }
}
