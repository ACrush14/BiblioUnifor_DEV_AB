package com.example.bibliounifornew.usuario

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF19SolicitacoesTermosCondicoes : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf19_solicitacoes_termos_condicoes)

        val scrollView = findViewById<ScrollView>(R.id.scrollTermos)
        val checkBox = findViewById<CheckBox>(R.id.checkTelaAceitarTermos)
        val btnConfirmar = findViewById<Button>(R.id.buttonConfirmarTermosTela)

        // Inicialmente desabilitados
        checkBox.isEnabled = false
        btnConfirmar.isEnabled = false
        btnConfirmar.alpha = 0.5f

        // Lógica de Scroll para habilitar checkbox
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val view = scrollView.getChildAt(scrollView.childCount - 1)
            val diff = (view.bottom - (scrollView.height + scrollView.scrollY))

            if (diff <= 0) {
                checkBox.isEnabled = true
            }
        }

        // Lógica do Checkbox para habilitar botão
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            btnConfirmar.isEnabled = isChecked
            btnConfirmar.alpha = if (isChecked) 1.0f else 0.5f
        }

        // Botão Confirmar -> Abre Popup Sucesso
        btnConfirmar.setOnClickListener {
            showPopupSucesso()
        }
    }

    private fun showPopupSucesso() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.telarf19_solicitacoes_voltar_biblioteca)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val btnOk = dialog.findViewById<Button>(R.id.buttonPopupOkSolicitacao)

        btnOk.setOnClickListener {
            dialog.dismiss()
            // Volta para a tela do livro
            val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        dialog.show()
    }
}
