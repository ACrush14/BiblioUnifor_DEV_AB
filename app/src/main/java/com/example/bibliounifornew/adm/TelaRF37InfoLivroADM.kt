package com.example.bibliounifornew.adm

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF37InfoLivroADM : AppCompatActivity() {

    private var senhaVisivel = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf37_info_livro_adm)

        // 🔹 Configurar cliques dos lápis para editar campos
        configurarEdicao(R.id.btnEditTitulo, R.id.editTituloLivro)
        configurarEdicao(R.id.btnEditAutor, R.id.editAutorLivro)
        configurarEdicao(R.id.btnEditDescricao, R.id.editDescricaoLivro)
        configurarEdicao(R.id.btnEditLingua, R.id.editLingua)
        configurarEdicao(R.id.btnEditEditora, R.id.editEditora)
        configurarEdicao(R.id.btnEditDimensao, R.id.editDimensao)
        configurarEdicao(R.id.btnEditISBN10, R.id.editISBN10)
        configurarEdicao(R.id.btnEditISBN13, R.id.editISBN13)
        configurarEdicao(R.id.btnEditData, R.id.editData)
        configurarEdicao(R.id.btnEditPaginas, R.id.editPaginas)

        // 🔹 Controle de Exemplares
        val tvTotal = findViewById<TextView>(R.id.textExemplaresTotal)

        findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnDiminuirExemplares)?.setOnClickListener {
            val atual = tvTotal.text.toString().toIntOrNull() ?: 0
            if (atual > 0) tvTotal.text = (atual - 1).toString()
        }

        findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnAumentarExemplares)?.setOnClickListener {
            val atual = tvTotal.text.toString().toIntOrNull() ?: 0
            tvTotal.text = (atual + 1).toString()
        }

        // 🔹 Botão Apagar Mídia
        findViewById<Button>(R.id.btnApagarMidia)?.setOnClickListener {
            abrirPopupApagar()
        }
    }

    /**
     * Habilita o EditText, dá foco e coloca cursor no final ao clicar no ícone de lápis.
     */
    private fun configurarEdicao(botaoId: Int, campoId: Int) {
        val botao = findViewById<ImageView>(botaoId)
        val campo = findViewById<EditText>(campoId)

        botao?.setOnClickListener {
            campo?.isEnabled = true
            campo?.requestFocus()
            // Posiciona cursor no final do texto
            campo?.setSelection(campo.text.length)
            
            // Opcional: Mostrar feedback que está editando
            Toast.makeText(this, "Editando campo...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirPopupApagar() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_apagar_midia)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Ajustar largura (90% do display)
        val window = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        window?.attributes = layoutParams

        val editSenha = dialog.findViewById<EditText>(R.id.editSenhaAtual)
        val btnOlho = dialog.findViewById<ImageView>(R.id.iconMostrarSenha)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarApagar)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarApagar)

        // Lógica mostrar/esconder senha
        senhaVisivel = false
        btnOlho?.setOnClickListener {
            if (senhaVisivel) {
                editSenha?.transformationMethod = PasswordTransformationMethod.getInstance()
                btnOlho.setImageResource(R.drawable.ic_eye_closed)
            } else {
                editSenha?.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnOlho.setImageResource(R.drawable.ic_eye_open)
            }
            senhaVisivel = !senhaVisivel
            // Manter cursor no final após trocar transformação
            editSenha?.setSelection(editSenha.text.length)
        }

        btnConfirmar?.setOnClickListener {
            val senha = editSenha?.text.toString()
            if (senha.isEmpty()) {
                dialog.findViewById<TextView>(R.id.textErroSenha)?.visibility = android.view.View.VISIBLE
            } else {
                // Simulação de exclusão
                Toast.makeText(this, "Mídia apagada com sucesso", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                
                // TODO: Implementar exclusão real no banco de dados futuramente
                
                finish() // Volta para a tela anterior (Gestão de Mídias)
            }
        }

        btnCancelar?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
