package com.example.bibliounifornew.adm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF32LivrosCRUD : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf32_livroscrud)

        // 🔹 Botão Criar Mídia
        val btnCriarMidia = findViewById<Button>(R.id.btnAdicionarMidia)
        btnCriarMidia?.setOnClickListener {
            val intent = Intent(this@TelaRF32LivrosCRUD, TelaRF33CadastroDeLivros::class.java)
            startActivity(intent)
        }

        // 🔹 Botão Editar
        findViewById<Button>(R.id.btnEditarInformacoes)?.setOnClickListener {
            val intent = Intent(this@TelaRF32LivrosCRUD, TelaRF33CadastroDeLivros::class.java)
            startActivity(intent)
        }

    }
}