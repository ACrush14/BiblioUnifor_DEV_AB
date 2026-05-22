package com.example.bibliounifornew.usuario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF19SolicitacoesTermosCondicoes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf19_solicitacoes_termos_condicoes)

        val checkBox = findViewById<CheckBox>(R.id.checkTelaAceitarTermos)
        val buttonContinuar = findViewById<Button>(R.id.buttonConfirmarTermosTela)
        
        val context: Context = this@TelaRF19SolicitacoesTermosCondicoes

        buttonContinuar?.setOnClickListener {
            if (checkBox?.isChecked == true) {
                // Caminho: Termos -> Voltar Biblioteca
                val intent = Intent(context, TelaRF19SolicitacoesVoltarBiblioteca::class.java)
                startActivity(intent)
            } else {
                // Aviso caso não marque a caixa
                Toast.makeText(context, "Por favor, aceite os termos para continuar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
