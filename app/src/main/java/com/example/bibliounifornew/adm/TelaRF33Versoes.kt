package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF33Versoes : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf33_adicionar_midia_arquivos)

        val btnSalvarLivro = findViewById<MaterialButton>(R.id.btnSalvarLivro)

        btnSalvarLivro.setOnClickListener {
            exibirPopupSucesso()
        }

    }

    private fun exibirPopupSucesso() {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_livro_salvo_sucesso)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)

        val btnVoltarPopup = dialog.findViewById<MaterialButton>(R.id.btnVoltarMidias)

        btnVoltarPopup.setOnClickListener {
            dialog.dismiss()

            val intent = Intent(this@TelaRF33Versoes, TelaRF28DashboardADM::class.java)

            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)

            finish()
        }

        dialog.show()
    }
}