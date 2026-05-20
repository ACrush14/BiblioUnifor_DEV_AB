package com.example.bibliounifornew.usuario

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TelaRF11ResultadoPesquisa : AppCompatActivity() {

    private var editDataPublicacaoReferencia: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf11_1_resultado_pesquisa)

        // Configuração da Barra de Pesquisa e Filtro
        val editPesquisar = findViewById<EditText>(R.id.editPesquisarLivroResultado)
        val iconFiltro = findViewById<ImageView>(R.id.iconFiltroResultado)
        val btnProcurar = findViewById<MaterialButton>(R.id.buttonProcurarResultado)

        // Recupera termo de pesquisa se houver
        val queryAnterior = intent.getStringExtra("QUERY")
        if (!queryAnterior.isNullOrEmpty()) {
            editPesquisar.setText(queryAnterior)
        }

        iconFiltro.setOnClickListener { exibirPopupFiltro() }

        btnProcurar.setOnClickListener {
            val query = editPesquisar.text.toString()
            Toast.makeText(this, "Pesquisando por: $query", Toast.LENGTH_SHORT).show()
        }

        // Configuração dos Cards de Livros
        configurarCardsLivros()
    }

    private fun configurarCardsLivros() {
        val btnAdd1 = findViewById<MaterialButton>(R.id.buttonAdicionarLista1)
        val btnAdd2 = findViewById<MaterialButton>(R.id.buttonAdicionarLista2)
        val btnOpcoes1 = findViewById<ImageView>(R.id.btnOpcoesLivro1)
        val btnOpcoes2 = findViewById<ImageView>(R.id.btnOpcoesLivro2)

        btnAdd1.setOnClickListener {
            Toast.makeText(this, "Livro adicionado à lista de desejos", Toast.LENGTH_SHORT).show()
        }
        btnAdd2.setOnClickListener {
            Toast.makeText(this, "Livro adicionado à lista de desejos", Toast.LENGTH_SHORT).show()
        }

        btnOpcoes1.setOnClickListener {
            startActivity(Intent(this, TelaRF12TelaDoLivro::class.java))
        }
        btnOpcoes2.setOnClickListener {
            startActivity(Intent(this, TelaRF12TelaDoLivro::class.java))
        }
    }

    private fun exibirPopupFiltro() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_filtro_pesquisa)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val editAutor = dialog.findViewById<EditText>(R.id.editAutor)
        val editDataPublicacao = dialog.findViewById<EditText>(R.id.editDataPublicacao)
        val chipGroupDisponibilidade = dialog.findViewById<ChipGroup>(R.id.chipGroupDisponibilidade)
        val chipGroupCategoria = dialog.findViewById<ChipGroup>(R.id.chipGroupCategoria)
        val btnSalvar = dialog.findViewById<MaterialButton>(R.id.buttonSalvarFiltro)
        val btnLimpar = dialog.findViewById<MaterialButton>(R.id.buttonLimparFiltro)

        editDataPublicacao.isFocusable = false
        editDataPublicacao.setOnClickListener { 
            editDataPublicacaoReferencia = editDataPublicacao
            val intent = Intent(this, TelaCalendario::class.java)
            startActivityForResult(intent, 100)
        }

        btnSalvar.setOnClickListener { dialog.dismiss() }

        btnLimpar.setOnClickListener {
            editAutor.text.clear()
            editDataPublicacao.text.clear()
            chipGroupDisponibilidade.clearCheck()
            chipGroupCategoria.clearCheck()
        }

        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val selectedDate = data?.getStringExtra("SELECTED_DATE")
            editDataPublicacaoReferencia?.setText(selectedDate)
        }
    }
}
