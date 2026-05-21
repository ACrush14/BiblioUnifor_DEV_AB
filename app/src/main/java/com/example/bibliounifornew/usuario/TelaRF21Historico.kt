package com.example.bibliounifornew.usuario

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF21Historico : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf21_historico)

        // Botões de remover da lista principal
        val btnRemover1 = findViewById<Button>(R.id.btnRemoverHistorico)
        val btnRemover2 = findViewById<Button>(R.id.buttonRemoverHistorico2)

        // Cards que serão removidos (IDs adicionados ao XML)
        val cardLivro1 = findViewById<View>(R.id.cardLivro1Historico)
        val cardStatus1 = findViewById<View>(R.id.cardStatus1Historico)
        val cardLivro2 = findViewById<View>(R.id.cardLivro2Historico)
        val cardStatus2 = findViewById<View>(R.id.cardStatus2Historico)

        btnRemover1?.setOnClickListener {
            showPopupRemover("Vidas Secas") {
                cardLivro1?.visibility = View.GONE
                cardStatus1?.visibility = View.GONE
                Toast.makeText(this, "Livro removido do histórico", Toast.LENGTH_SHORT).show()
            }
        }

        btnRemover2?.setOnClickListener {
            showPopupRemover("O Ceifador") {
                cardLivro2?.visibility = View.GONE
                cardStatus2?.visibility = View.GONE
                Toast.makeText(this, "Livro removido do histórico", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Exibe o popup de confirmação para remover um item do histórico
     */
    private fun showPopupRemover(nomeLivro: String, onConfirm: () -> Unit) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_remover_historico)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val textMensagem = dialog.findViewById<TextView>(R.id.textMensagemPopupRemoverHistorico)
        val btnConfirmar = dialog.findViewById<Button>(R.id.buttonPopupRemoverHistorico)
        val btnCancelar = dialog.findViewById<TextView>(R.id.textCancelarPopupRemoverHistorico)

        // Atualiza a mensagem com o nome do livro clicado
        textMensagem.text = "Tem certeza que deseja remover o livro \"$nomeLivro\" do seu histórico?"

        // Botão Sim, remover
        btnConfirmar.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        // Botão Cancelar
        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
