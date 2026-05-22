package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF23LoginADM
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class TelaRF30UsuariosParaADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf30_usuariosparaadm)

        val textTipoUsuario = findViewById<TextView>(R.id.textTipoUsuario)

        // Mapeamento dos botões do layout
        val btnSolicitacoes = findViewById<MaterialButton>(R.id.buttonSolicitacoes)
        val btnAlugados = findViewById<MaterialButton>(R.id.buttonLivrosAlugados)
        val btnAtrasos = findViewById<MaterialButton>(R.id.buttonAtrasos)
        val btnPermissao = findViewById<MaterialButton>(R.id.buttonPermissao)
        val btnExcluirConta = findViewById<MaterialButton>(R.id.buttonExcluirConta)

        // 3) BOTÃO SOLICITAÇÕES -> Popup (Dialog)
        btnSolicitacoes?.setOnClickListener {
            exibirPopupSolicitacoes()
        }

        // 4) BOTÃO ALUGADOS -> Nova Tela (Intent)
        btnAlugados?.setOnClickListener {
            val intent = Intent(this, TelaRF36ListaAlugueisADM::class.java)
            startActivity(intent)
        }

        // 5) BOTÃO ATRASOS DE ALUGUEL -> Popup (Dialog)
        btnAtrasos?.setOnClickListener {
            exibirPopupAtraso()
        }

        // 6) BOTÃO MUDAR PERMISSÃO -> Popup (Dialog)
        btnPermissao?.setOnClickListener {
            exibirPopupPermissao(textTipoUsuario)
        }

        // 7) BOTÃO EXCLUIR CONTA -> Popup (Dialog)
        btnExcluirConta?.setOnClickListener {
            exibirPopupExcluirConta()
        }
    }

    private fun exibirPopupSolicitacoes() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_solicitacoes_usuario_adm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnFechar = dialog.findViewById<Button>(R.id.btnFecharSolicitacoes)
        btnFechar?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun exibirPopupAtraso() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_atraso_aluguel_usuario)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnFechar = dialog.findViewById<Button>(R.id.buttonFecharAtraso)
        btnFechar?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun exibirPopupPermissao(textTipo: TextView?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_mudar_permissao_adm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnMudar = dialog.findViewById<Button>(R.id.buttonMudarPermissao)
        val btnCancelar = dialog.findViewById<TextView>(R.id.textCancelarPermissao)

        btnMudar?.setOnClickListener {
            textTipo?.text = "ADMINISTRADOR"
            textTipo?.setBackgroundResource(R.drawable.bg_pill_status)
            textTipo?.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            Toast.makeText(this, "Permissão alterada para administrador", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnCancelar?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun exibirPopupExcluirConta() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_apagar_conta_adm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnConfirmar = dialog.findViewById<Button>(R.id.buttonConfirmarApagarContaADM)
        val btnCancelar = dialog.findViewById<Button>(R.id.buttonCancelarApagarContaADM)
        val editSenha = dialog.findViewById<TextInputEditText>(R.id.editSenhaApagarContaADM)

        btnConfirmar?.setOnClickListener {
            val senhaDigitada = editSenha?.text?.toString()?.trim() ?: ""
            if (senhaDigitada.isEmpty()) {
                Toast.makeText(this, "Por favor, digite sua senha", Toast.LENGTH_SHORT).show()
            } else {
                // 7) Confirmar -> Volta para Login ADM
                Toast.makeText(this, "Usuário excluído", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                val intent = Intent(this, TelaRF23LoginADM::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        btnCancelar?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
