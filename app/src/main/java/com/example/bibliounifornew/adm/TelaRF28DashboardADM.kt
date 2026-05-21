package com.example.bibliounifornew.adm

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF28DashboardADM : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Qualificação explícita para resolver erros de referência do IDE (Ghost Errors)
        this@TelaRF28DashboardADM.setContentView(R.layout.telarf28_inicial_adm)

        // 1. Botões de Navegação Principal
        val btnTelaCrud = this@TelaRF28DashboardADM.findViewById<MaterialButton>(R.id.buttonCrudAdm)
        val btnVerAlugueis = this@TelaRF28DashboardADM.findViewById<MaterialButton>(R.id.buttonVerAlugueis)
        val btnVerTodosAtrasos = this@TelaRF28DashboardADM.findViewById<MaterialButton>(R.id.buttonVerAtrasos)

        // 2. Novos Botões Adicionados
        val btnVerCadastros = this@TelaRF28DashboardADM.findViewById<MaterialButton>(R.id.buttonVerCadastros)
        val btnVerSolicitacoes = this@TelaRF28DashboardADM.findViewById<MaterialButton>(R.id.buttonVerSolicitacoes)

        // --- Listeners Principais ---
        btnTelaCrud?.setOnClickListener {
            val intent = Intent(this@TelaRF28DashboardADM, TelaRF28CrudADM::class.java)
            startActivity(intent)
        }

        btnVerAlugueis?.setOnClickListener {
            val intent = Intent(this@TelaRF28DashboardADM, TelaRF36ListaAlugueisADM::class.java)
            startActivity(intent)
        }

        btnVerTodosAtrasos?.setOnClickListener {
            val intent = Intent(this@TelaRF28DashboardADM, TelaRF34FinanceiroADM::class.java)
            startActivity(intent)
        }

        // --- Novos Listeners ---
        btnVerCadastros?.setOnClickListener {
            val intent = Intent(this@TelaRF28DashboardADM, TelaRF35ConfirmarCadastroADM::class.java)
            startActivity(intent)
        }

        btnVerSolicitacoes?.setOnClickListener {
            val intent = Intent(this@TelaRF28DashboardADM, TelaRF31Solicitacoes::class.java)
            startActivity(intent)
        }

        // --- Configuração (Icone do Topo) ---
        val iconConfig = findViewById<ImageView>(R.id.iconConfigAdm)
        iconConfig?.setOnClickListener {
            val intent = Intent(this@TelaRF28DashboardADM, TelaRF38ConfigADM::class.java)
            startActivity(intent)
        }
    }
}
