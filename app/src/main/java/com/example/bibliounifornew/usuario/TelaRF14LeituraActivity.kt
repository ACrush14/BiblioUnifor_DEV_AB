package com.example.bibliounifornew.usuario

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF14LeituraActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.telarf14_leitura)

        val livroId = intent.getIntExtra("LIVRO_ID", 1)

    }
}