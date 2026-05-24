package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.card.MaterialCardView

class TelaRF32LivrosCRUD : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf32_livroscrud)

        // 🔹 Botão Adicionar Mídia (já existente no projeto)
        val btnAdicionarMidia = findViewById<Button>(R.id.btnAdicionarMidia)
        btnAdicionarMidia?.setOnClickListener {
            val intent = Intent(this@TelaRF32LivrosCRUD, TelaRF33CadastroLivro::class.java)
            startActivity(intent)
        }

        // 🔹 Botão Editar (Abre RF37)
        findViewById<Button>(R.id.btnEditarInformacoes)?.setOnClickListener {
            val intent = Intent(this@TelaRF32LivrosCRUD, TelaRF37InfoLivroADM::class.java)
            startActivity(intent)
        }

        // 🔹 Botão Filtro (Setinha ao lado da busca)
        findViewById<ImageView>(R.id.btnFiltro)?.setOnClickListener {
            abrirPopupFiltro()
        }

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    private fun abrirPopupFiltro() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_filtro_verificar_midia)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnSalvar = dialog.findViewById<Button>(R.id.buttonSalvarFiltro)
        val btnLimpar = dialog.findViewById<Button>(R.id.buttonLimparFiltro)
        val editTitulo = dialog.findViewById<EditText>(R.id.editTituloFiltro)

        btnSalvar?.setOnClickListener {
            Toast.makeText(this, "Filtro aplicado", Toast.LENGTH_SHORT).show()
            
            val termo = editTitulo?.text.toString()
            if (termo.isNotEmpty()) {
                if (!"Senhor".contains(termo, ignoreCase = true) && !"Alienista".contains(termo, ignoreCase = true)) {
                    findViewById<MaterialCardView>(R.id.cardLivroExemplo)?.visibility = View.GONE
                }
            }
            
            dialog.dismiss()
        }

        btnLimpar?.setOnClickListener {
            editTitulo?.text?.clear()
            dialog.findViewById<EditText>(R.id.editAutorFiltro)?.text?.clear()
            dialog.findViewById<EditText>(R.id.editISBNFiltro)?.text?.clear()
            findViewById<MaterialCardView>(R.id.cardLivroExemplo)?.visibility = View.VISIBLE
            Toast.makeText(this, "Filtros limpos", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()

        // Ajustar tamanho do popup (90% da largura)
        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        window?.attributes = layoutParams
    }
}
