package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF17_3_BuscaAmigos : AppCompatActivity() {

    // Lista simulada de amigos já adicionados para evitar duplicidade (Requisito 5)
    private val amigosAdicionados = mutableListOf("Ronaldo Alves", "Robson Gonçalves", "Vitoria Ferreira", "Marta Viana", "Adriano de Souza")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf17_3_busca_amigos)

        // 3) Lógica de adicionar amigo diretamente pelo ícone +
        val btnAdd1 = findViewById<View>(R.id.buttonAdicionarResultado1)
        val btnAdd2 = findViewById<View>(R.id.buttonAdicionarResultado2)

        btnAdd1.setOnClickListener {
            adicionarAmigo("Marcos Antônio")
        }

        btnAdd2.setOnClickListener {
            adicionarAmigo("Marcos Ferreira")
        }

        // Botão procurar (Apenas para simular a ação de busca)
        findViewById<View>(R.id.buttonBuscarAmigo).setOnClickListener {
            Toast.makeText(this, "Buscando...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun adicionarAmigo(nome: String) {
        // 5) Evitar duplicidade
        if (amigosAdicionados.contains(nome)) {
            Toast.makeText(this, "Esse amigo já foi adicionado", Toast.LENGTH_SHORT).show()
        } else {
            // Simula a adição
            amigosAdicionados.add(nome)
            
            // Toast de sucesso
            Toast.makeText(this, "Amigo adicionado com sucesso", Toast.LENGTH_SHORT).show()

            // Voltar automaticamente para a tela de amigos
            val intent = Intent(this, TelaRF17Amigos::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Garante que volta para a instância existente
            startActivity(intent)
            finish()
        }
    }
}