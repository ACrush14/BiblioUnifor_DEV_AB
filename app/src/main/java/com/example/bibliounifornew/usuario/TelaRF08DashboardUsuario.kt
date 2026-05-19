package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.MainActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF08DashboardUsuario : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf08_dashboardusuario)

        // Botões do Header
        val btnConfig = findViewById<ImageView>(R.id.btnConfig)
        val btnNotificacao = findViewById<ImageView>(R.id.btnNotificacao)
        val profileImage = findViewById<ImageView>(R.id.imagePerfilUsuario)
        val textNomeUsuario = findViewById<TextView>(R.id.textNomeUsuario)

        // Botões de Ações Rápidas (Cards/Buttons no ScrollView)
        val btnMinhaLivraria = findViewById<MaterialButton>(R.id.btnMinhaLivraria)
        val btnListaDesejo = findViewById<MaterialButton>(R.id.btnListaDesejos)
        val btnAmigos = findViewById<MaterialButton>(R.id.btnAmigos)
        val btnHistorico = findViewById<MaterialButton>(R.id.btnHistorico)
        val btnStatusAluguel = findViewById<MaterialButton>(R.id.btnStatusAluguel)
        val btnSair = findViewById<MaterialButton>(R.id.btnSairConta)

        // Navegação via Engrenagem -> Configuração (RF09)
        btnConfig.setOnClickListener {
            val intent = Intent(this@TelaRF08DashboardUsuario, TelaRF09Configuracao::class.java)
            startActivity(intent)
        }

        btnNotificacao.setOnClickListener {
            val intent = Intent(this@TelaRF08DashboardUsuario, TelaRF20Notificacoes::class.java)
            startActivity(intent)
        }

        // Ações Rápidas
        btnMinhaLivraria.setOnClickListener {
            val intent = Intent(this@TelaRF08DashboardUsuario, TelaRF15MinhaLivrariaActivity::class.java)
            startActivity(intent)
        }

        btnListaDesejo.setOnClickListener {
            val intent = Intent(this@TelaRF08DashboardUsuario, TelaRF16ListaDesejosActivity::class.java)
            startActivity(intent)
        }

        btnAmigos.setOnClickListener {
            val intent = Intent(this@TelaRF08DashboardUsuario, TelaRF17Amigos::class.java)
            startActivity(intent)
        }

        btnHistorico.setOnClickListener {
            val intent = Intent(this@TelaRF08DashboardUsuario, TelaRF21Historico::class.java)
            startActivity(intent)
        }

        btnStatusAluguel.setOnClickListener {
            val intent = Intent(this@TelaRF08DashboardUsuario, TelaRF19Solicitacoes::class.java)
            startActivity(intent)
        }

        btnSair.setOnClickListener {
            val intent = Intent(this@TelaRF08DashboardUsuario, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }
}
