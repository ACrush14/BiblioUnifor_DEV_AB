package com.example.bibliounifornew.usuario

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF22RenovacaoOnline : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Uso explícito de this@ para contornar erros de sincronização do IDE
        this@TelaRF22RenovacaoOnline.setContentView(R.layout.telarf22_renovacao_online)

        val buttonConfirmarData = this@TelaRF22RenovacaoOnline.findViewById<Button>(R.id.btnConfirmarData)
        val buttonVoltar = this@TelaRF22RenovacaoOnline.findViewById<TextView>(R.id.textVoltarDaRenovacao)

        // Ao confirmar, encerra esta atividade e volta para a tela anterior
        buttonConfirmarData?.setOnClickListener {
            // Aqui será implementada a lógica de salvar a renovação no banco de dados
            this@TelaRF22RenovacaoOnline.finish()
        }

        // Ao clicar em voltar, apenas encerra a atividade atual
        buttonVoltar?.setOnClickListener {
            this@TelaRF22RenovacaoOnline.finish()
        }
    }
}
