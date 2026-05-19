package com.example.bibliounifornew.adm

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF31Solicitacoes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Qualificação explícita para contornar erros de indexação (Ghost Errors)
        this@TelaRF31Solicitacoes.setContentView(R.layout.telarf31_solicitacoes_adm)

        // =========================
        // MAPEAMENTO E USO DOS BOTÕES
        // =========================
        val btnFiltro = this@TelaRF31Solicitacoes.findViewById<ImageView>(R.id.buttonFiltroMidia)
        val btnVerSolicitacoes = this@TelaRF31Solicitacoes.findViewById<Button>(R.id.buttonVerSolicitacoesUsuario)
        val btnEnviarAudiobook = this@TelaRF31Solicitacoes.findViewById<Button>(R.id.buttonEnviarAudiobook)
        val btnEnviarPDF = this@TelaRF31Solicitacoes.findViewById<Button>(R.id.buttonEnviarPDF)
        val btnNotificarBraille = this@TelaRF31Solicitacoes.findViewById<Button>(R.id.buttonBrailleConcluido)
        val btnExcluirSolicitacao = this@TelaRF31Solicitacoes.findViewById<Button>(R.id.buttonExcluirSolicitacao)

        // LISTENERS (Para resolver o aviso de "unused variable")
        btnFiltro?.setOnClickListener {
            Toast.makeText(this@TelaRF31Solicitacoes, "Filtro clicado", Toast.LENGTH_SHORT).show()
        }

        btnVerSolicitacoes?.setOnClickListener {
            Toast.makeText(this@TelaRF31Solicitacoes, "Ver solicitações clicado", Toast.LENGTH_SHORT).show()
        }

        btnEnviarAudiobook?.setOnClickListener {
            Toast.makeText(this@TelaRF31Solicitacoes, "Enviar Audiobook clicado", Toast.LENGTH_SHORT).show()
        }

        btnEnviarPDF?.setOnClickListener {
            Toast.makeText(this@TelaRF31Solicitacoes, "Enviar PDF clicado", Toast.LENGTH_SHORT).show()
        }

        btnNotificarBraille?.setOnClickListener {
            Toast.makeText(this@TelaRF31Solicitacoes, "Notificar Braille clicado", Toast.LENGTH_SHORT).show()
        }

        btnExcluirSolicitacao?.setOnClickListener {
            Toast.makeText(this@TelaRF31Solicitacoes, "Excluir solicitação clicada", Toast.LENGTH_SHORT).show()
        }
    }
}
