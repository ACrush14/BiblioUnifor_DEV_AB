package com.example.bibliounifornew.usuario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF19Solicitacoes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf19_solicitacoes)

        val btnPdf = findViewById<Button>(R.id.buttonSolicitarPdf)
        val btnBraile = findViewById<Button>(R.id.buttonSolicitarBraille)
        val btnAudio = findViewById<Button>(R.id.buttonSolicitarAudiobook)
        val btnReservar = findViewById<Button>(R.id.buttonReservarLivro)
        val btnSetor = findViewById<Button>(R.id.buttonSetorLocalizado)

        val context: Context = this@TelaRF19Solicitacoes

        btnPdf?.setOnClickListener {
            val intent = Intent(context, TelaRF19SolicitacoesTermosCondicoes::class.java)
            startActivity(intent)
        }

        btnSetor?.setOnClickListener {
            Toast.makeText(
                context,
                "Setor do livro O Alienista: Setor X",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnBraile?.setOnClickListener { 
            // Implementação futura
        }
        btnAudio?.setOnClickListener { 
            // Implementação futura
        }
        btnReservar?.setOnClickListener { 
            // Implementação futura
        }
    }
}
