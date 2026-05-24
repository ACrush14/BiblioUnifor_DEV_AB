package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF33AdicionarMidiaArquivos : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf33_adicionar_midia_arquivos)

        val etPdf = findViewById<EditText>(R.id.editArquivoPdf)
        val etAudiobook = findViewById<EditText>(R.id.editArquivoAudiobook)
        val cbBraille = findViewById<CheckBox>(R.id.checkBraille)
        val btnSalvar = findViewById<MaterialButton>(R.id.btnSalvarLivro)

        btnSalvar.setOnClickListener {
            val pdf = etPdf.text.toString().trim()
            val audio = etAudiobook.text.toString().trim()

            // Validação de arquivos (PDF e Audiobook como obrigatórios conforme prompt)
            if (pdf.isEmpty() || audio.isEmpty()) {
                Toast.makeText(this, "Adicione todos os arquivos necessários", Toast.LENGTH_SHORT).show()
            } else {
                // NOTA: Toda criação de livro deverá futuramente integrar banco de dados.
                // Aqui os caminhos dos arquivos seriam persistidos.
                exibirPopupSucesso()
            }
        }
    }

    private fun exibirPopupSucesso() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_livro_salvo_sucesso)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)

        val btnVoltar = dialog.findViewById<MaterialButton>(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            dialog.dismiss()
            // Retorna para a Dashboard Inicial ADM
            val intent = Intent(this, TelaRF28DashboardADM::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        dialog.show()
    }
}