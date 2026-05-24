package com.example.bibliounifornew.usuario

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF19Solicitacoes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf19_solicitacoes)

        val btnPdf = findViewById<Button>(R.id.buttonSolicitarPdf)
        val btnBraile = findViewById<Button>(R.id.buttonSolicitarBraille)
        val btnAudio = findViewById<Button>(R.id.buttonSolicitarAudiobook)
        val btnReservar = findViewById<Button>(R.id.buttonReservarLivro)
        val btnSetor = findViewById<Button>(R.id.buttonSetorLocalizado)

        // Todos levam para Termos e Condições
        val clickParaTermos = {
            startActivity(Intent(this, TelaRF19SolicitacoesTermosCondicoes::class.java))
        }

        btnPdf?.setOnClickListener { clickParaTermos() }
        btnBraile?.setOnClickListener { clickParaTermos() }
        btnAudio?.setOnClickListener { clickParaTermos() }
        btnReservar?.setOnClickListener { clickParaTermos() }

        btnSetor?.setOnClickListener {
            showPopupSetor()
        }
    }

    private fun showPopupSetor() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_setor_localizado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnVoltar = dialog.findViewById<Button>(R.id.buttonVoltarSetor)
        btnVoltar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
