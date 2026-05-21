package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.Simulados
import com.google.android.material.card.MaterialCardView

/**
 * Tela de Gerenciamento Financeiro / Aluguéis ADM
 * RF34
 */
class TelaRF34FinanceiroADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf34_finaceiro_adm)

        // Mapeamento dos botões da tela principal
        val btnVerPendentes = findViewById<Button>(R.id.btnPendentesRetirada)
        val btnRenovarAluguel = findViewById<Button>(R.id.btnRenovarAluguel)
        val cardLivroVencido = findViewById<MaterialCardView>(R.id.cardLivroVencido)
        val iconMais = findViewById<View>(R.id.iconMais)

        // 1) Ao clicar: btnVerPendentesRetirada -> abre popup: popup_pendentes_retirada.xml
        btnVerPendentes?.setOnClickListener {
            exibirPopupPendentes(cardLivroVencido)
        }

        // Adição: Clique nos três pontinhos -> vai para telarf37_info_livro_adm.xml
        iconMais?.setOnClickListener {
            val intent = Intent(this, TelaRF37InfoLivroADM::class.java)
            startActivity(intent)
        }

        // 6) Ao clicar: btnRenovarAluguel -> abre: telarf18_2_calendario_renovacao.xml
        btnRenovarAluguel?.setOnClickListener {
            val intent = Intent(this, TelaRF18_2CalendarioRenovacao::class.java)
            startActivity(intent)
        }
    }

    /**
     * Exibe o popup de pendências de retirada
     */
    private fun exibirPopupPendentes(cardParaRemover: View) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_pendentes_retirada)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Botões do Popup
        val btnNotificarAtraso = dialog.findViewById<Button>(R.id.btnNotificarAtraso)
        val btnNotificarValor = dialog.findViewById<Button>(R.id.btnNotificarValor)
        val btnConfirmarAluguel = dialog.findViewById<Button>(R.id.btnConfirmacaoAluguel)
        val btnRemoverRegistro = dialog.findViewById<Button>(R.id.btnRemoverRegistro)

        // 2) Ao clicar: btnNotificarAtraso -> Toast + Notificação Simulada
        btnNotificarAtraso?.setOnClickListener {
            Toast.makeText(this, "Aviso de atraso enviado", Toast.LENGTH_SHORT).show()
            Simulados.notificacoesUsuario.add("Seu aluguel está atrasado. Regularize para evitar bloqueios.")
            dialog.dismiss()
        }

        // 3) Ao clicar: btnNotificarValor -> Toast + Notificação Simulada
        btnNotificarValor?.setOnClickListener {
            Toast.makeText(this, "Valor pendente enviado", Toast.LENGTH_SHORT).show()
            Simulados.notificacoesUsuario.add("Existe valor pendente referente ao aluguel.")
            dialog.dismiss()
        }

        // 4) Ao clicar: btnConfirmarAluguel -> Toast + Notificação Simulada
        btnConfirmarAluguel?.setOnClickListener {
            Toast.makeText(this, "Aluguel confirmado", Toast.LENGTH_SHORT).show()
            Simulados.notificacoesUsuario.add("Seu aluguel foi confirmado.")
            dialog.dismiss()
        }

        // 5) Ao clicar: btnRemoverRegistro -> Toast + Remover da ArrayList + Esconder Card
        btnRemoverRegistro?.setOnClickListener {
            Toast.makeText(this, "Registro apagado", Toast.LENGTH_SHORT).show()
            
            // Simulação de remoção da lista
            if (Simulados.registrosFinanceiros.isNotEmpty()) {
                Simulados.registrosFinanceiros.removeAt(0)
            }
            
            // Esconde o card da interface
            cardParaRemover.visibility = View.GONE
            dialog.dismiss()
        }

        dialog.show()
    }
}