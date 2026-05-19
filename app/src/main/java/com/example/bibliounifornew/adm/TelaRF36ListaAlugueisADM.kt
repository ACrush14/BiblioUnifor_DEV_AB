package com.example.bibliounifornew.adm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adm.TelaRF37InfoLivroADM

class TelaRF36ListaAlugueisADM : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf36_lista_alugueis_adm)

        // 1. Encontrar o botão pelo ID que está no XML
        val botaoVerLivro1 = findViewById<Button>(R.id.btnVerLivro1)

        // 2. Criar a ação de clique
        botaoVerLivro1.setOnClickListener {
            // 3. Fazer a ponte (Intent) para a Tela 39
            val intent = Intent(this@TelaRF36ListaAlugueisADM, TelaRF37InfoLivroADM::class.java)
            startActivity(intent)
        }
    }
}