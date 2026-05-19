package com.example.bibliounifornew.adm

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF33InfosAdicionais : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf33_adicionar_midia_extras)

        val btnIrVersoes = findViewById<MaterialButton>(R.id.btnEditarMaisInformacoes2)

        btnIrVersoes.setOnClickListener {
            val intent = Intent(this@TelaRF33InfosAdicionais, TelaRF33Versoes::class.java)
            startActivity(intent)
        }
    }
}