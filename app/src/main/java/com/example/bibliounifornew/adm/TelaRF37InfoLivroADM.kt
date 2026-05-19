package com.example.bibliounifornew.adm

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF37InfoLivroADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf37_info_livro_adm)

        // 1. Encontrar o botão de voltar pelo ID do XML
        val btnApagarMidia = findViewById<Button>(R.id.btnApagarMidia)

        // 2. Criar a ação de clique
        btnApagarMidia.setOnClickListener {

            //apagar midia

        }
    }
}