package com.example.bibliounifornew.adm

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

/**
 * Tela de Confirmação de Cadastro ADM
 * RF35
 */
class TelaRF35ConfirmarCadastroADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf35_confirmar_cadastro_adm)

        // Itens da lista de usuários
        val item1 = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.itemUsuario1)
        val item2 = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.itemUsuario2)
        val item3 = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.itemUsuario3)

        // Ao clicar no usuário -> abre popup_confirmacao_usuario.xml
        item1?.setOnClickListener { exibirPopupConfirmacao() }
        item2?.setOnClickListener { exibirPopupConfirmacao() }
        item3?.setOnClickListener { exibirPopupConfirmacao() }
    }

    /**
     * Exibe o popup de confirmação de cadastro
     */
    private fun exibirPopupConfirmacao() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_confirmacao_usuario)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarCadastro)
        val btnVoltar = dialog.findViewById<TextView>(R.id.textVoltar)

        // Quando clica em confirmar cadastro -> Toast + fecha popup
        btnConfirmar?.setOnClickListener {
            Toast.makeText(this, "Cadastro confirmado", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        // Quando clica em voltar -> fecha popup
        btnVoltar?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}