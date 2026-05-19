package com.example.bibliounifornew.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

/**
 * Tela inicial de Boas-Vindas (RF01)
 */
class TelaRF01BemVindo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define o layout da tela
        setContentView(R.layout.telarf01_bemvindo)

        // Busca o botão pelo ID (ID do XML permanece buttonComecar)
        val btnEntrar = findViewById<Button>(R.id.buttonComecar)

        // Configura a navegação para a próxima tela
        btnEntrar.setOnClickListener {
            val intent = Intent(this@TelaRF01BemVindo, TelaRF02Intermediaria::class.java)
            startActivity(intent)
        }
    }
}
