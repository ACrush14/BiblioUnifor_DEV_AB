package com.example.bibliounifornew.usuario

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF21Historico : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf21_historico)

        val context: Context = this@TelaRF21Historico

        // Primeiro botão de remover
        val buttonRemoverHistorico = findViewById<Button>(R.id.btnRemoverHistorico)
        buttonRemoverHistorico?.setOnClickListener {
            // Lógica para remover item do histórico
        }

        // Segundo botão de remover (se houver na lista estática)
        val buttonRemoverHistorico2 = findViewById<Button>(R.id.buttonRemoverHistorico2)
        buttonRemoverHistorico2?.setOnClickListener {
            // Lógica para remover item do histórico
        }
    }
}
