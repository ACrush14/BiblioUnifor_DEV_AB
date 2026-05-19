package com.example.bibliounifornew.adm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF30UsuariosParaADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Nome do XML CORRETO
        setContentView(R.layout.telarf30_usuariosparaadm)

        // Botões (IDs do seu XML)
        val btnSolicitacoes = findViewById<Button>(R.id.buttonSolicitacoes)
        val btnAlugados = findViewById<Button>(R.id.buttonLivrosAlugados)
        val btnAtrasos = findViewById<Button>(R.id.buttonAtrasos)
        val btnExcluirConta = findViewById<Button>(R.id.buttonPermissao)

        // Abrir RF33 (Solicitações)
        btnSolicitacoes?.setOnClickListener {
            val intent = Intent(this@TelaRF30UsuariosParaADM, TelaRF31Solicitacoes::class.java)
            startActivity(intent)
        }

        // Ações simuladas
        btnAlugados?.setOnClickListener {
            Toast.makeText(this@TelaRF30UsuariosParaADM, "Abrir livros alugados", Toast.LENGTH_SHORT).show()
        }

        btnExcluirConta?.setOnClickListener {
            Toast.makeText(this@TelaRF30UsuariosParaADM, "Conta excluída (simulação)", Toast.LENGTH_SHORT).show()
        }

    }
}