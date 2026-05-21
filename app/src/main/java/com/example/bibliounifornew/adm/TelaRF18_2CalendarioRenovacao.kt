package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

/**
 * Tela de Calendário para Renovação de Aluguel (ADM)
 * RF18.2 do Fluxo Financeiro
 */
class TelaRF18_2CalendarioRenovacao : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf18_2_calendario_renovacao)

        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltarCalendario)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarRenovacaoTela)

        // Botão Voltar da Toolbar
        btnVoltar.setOnClickListener {
            finish()
        }

        // 7) Ao clicar Confirmar Renovação -> Abre Popup Sucesso
        btnConfirmar.setOnClickListener {
            exibirPopupSucesso()
        }
    }

    /**
     * Exibe o popup de sucesso após confirmar a renovação
     */
    private fun exibirPopupSucesso() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_livro_salvo_sucesso)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnVoltarPopup = dialog.findViewById<Button>(R.id.btnVoltar)
        val textMensagem = dialog.findViewById<TextView>(R.id.textMensagemSucesso)

        // Ajusta texto para o contexto de renovação
        textMensagem?.text = "Renovação realizada com sucesso"

        // 8) No popup, ao clicar Voltar -> Volta para Financeiro ADM
        btnVoltarPopup?.setOnClickListener {
            dialog.dismiss()
            
            // Cria intent para garantir que volte para o Financeiro e limpe a pilha se necessário
            val intent = Intent(this, TelaRF34FinanceiroADM::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        dialog.show()
    }
}