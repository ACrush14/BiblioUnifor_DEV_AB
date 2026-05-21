package com.example.bibliounifornew.usuario

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF18StatusAluguel : AppCompatActivity() {

    private lateinit var textDataVencimento1: TextView

    // Lançador para a tela de calendário, esperando o resultado da nova data
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val novaData = result.data?.getStringExtra("nova_data")
            if (novaData != null) {
                // Atualiza a data exibida no Livro 1 (Dom Casmurro)
                textDataVencimento1.text = novaData
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf18_status_aluguel)

        // Referências
        textDataVencimento1 = findViewById(R.id.textDataVencimento1)
        val btnRenovar1 = findViewById<MaterialButton>(R.id.btnRenovarLivro1)
        val btnRenovar2 = findViewById<MaterialButton>(R.id.btnRenovarLivro2)
        val btnRenovar3 = findViewById<MaterialButton>(R.id.btnRenovarLivro3)

        // LIVRO 1: Dom Casmurro (Disponível renovação)
        btnRenovar1.setOnClickListener {
            // Abre tela inteira de calendário
            val intent = Intent(this, TelaRF18CalendarioRenovacao::class.java)
            startForResult.launch(intent)
        }

        // LIVRO 2: O Alienista (Limite atingido)
        btnRenovar2.setOnClickListener {
            showPopupLimite()
        }

        // LIVRO 3: 1984 (Prazo expirado)
        btnRenovar3.setOnClickListener {
            showPopupExpirado()
        }
    }

    // =/=/=/=/=/=/=/=/=/=/=/=/=/=/=/=/=/=/=/=/=
    // POPUPS DE CENÁRIOS (Simulados)
    // =/=/=/=/=/=/=/=/=/=/=/=/=/=/=/=/=/=/=/=/=

    private fun showPopupLimite() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_limite_renovacao)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnVoltar = dialog.findViewById<MaterialButton>(R.id.buttonVoltarPopupRenovacao)
        btnVoltar?.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showPopupExpirado() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_prazo_renovacao_expirado)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnVoltar = dialog.findViewById<MaterialButton>(R.id.buttonVoltarPopupPrazoRenovacao)
        btnVoltar?.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}