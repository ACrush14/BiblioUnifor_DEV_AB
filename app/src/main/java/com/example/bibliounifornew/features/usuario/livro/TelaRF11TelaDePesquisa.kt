package com.example.bibliounifornew.features.usuario.livro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper
import com.google.android.material.bottomsheet.BottomSheetDialog

class TelaRF11TelaDePesquisa : AppCompatActivity() {

    private var filtroTitulo: String = ""
    private var filtroAutor: String = ""
    private var filtroCategoria: String = "Todas as Categorias"
    private var filtroDisponibilidade: String = "Todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf11_teladepesquisa)

        carregarFiltrosSalvos()

        val editPesquisa = findViewById<EditText>(R.id.editPesquisarLivro)
        val btnProcurar = findViewById<Button>(R.id.buttonProcurar)
        val btnFiltro = findViewById<ImageView>(R.id.btnAbrirFiltro)

        // Escutar o botão "Procurar" (Lupa) do teclado
        editPesquisa.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                executarBusca(editPesquisa.text.toString().trim())
                true
            } else {
                false
            }
        }

        btnFiltro.setOnClickListener {
            exibirPopupFiltro(editPesquisa.text.toString().trim())
        }

        btnProcurar.setOnClickListener {
            executarBusca(editPesquisa.text.toString().trim())
        }

        // Configurar Barra de Navegação
        NavigationHelper.configurarBarraNavegacao(this)
    }

    private fun carregarFiltrosSalvos() {
        val prefs = getSharedPreferences("filtros_busca", MODE_PRIVATE)
        filtroTitulo = prefs.getString("filtro_titulo", "") ?: ""
        filtroAutor = prefs.getString("filtro_autor", "") ?: ""
        filtroCategoria = prefs.getString("filtro_categoria", getString(R.string.categoria_todos)) ?: getString(R.string.categoria_todos)
        filtroDisponibilidade = prefs.getString("filtro_disponibilidade", "Todos") ?: "Todos"
    }

    private fun salvarFiltros() {
        val prefs = getSharedPreferences("filtros_busca", MODE_PRIVATE)
        prefs.edit().apply {
            putString("filtro_titulo", filtroTitulo)
            putString("filtro_autor", filtroAutor)
            putString("filtro_categoria", filtroCategoria)
            putString("filtro_disponibilidade", filtroDisponibilidade)
            apply()
        }
    }

    private fun executarBusca(termo: String) {
        val intent = Intent(this, TelaRF11_1_ResultadoPesquisa::class.java)
        intent.putExtra("TERMO_PESQUISA", termo)
        intent.putExtra("FILTRO_TITULO", filtroTitulo)
        intent.putExtra("FILTRO_AUTOR", filtroAutor)
        intent.putExtra("FILTRO_CATEGORIA", filtroCategoria)
        intent.putExtra("FILTRO_DISPONIBILIDADE", filtroDisponibilidade)
        startActivity(intent)
    }

    private fun exibirPopupFiltro(termoAtual: String) {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(this).inflate(R.layout.popup_filtro_pesquisa, null)

        val edtTitulo = view.findViewById<EditText>(R.id.editTituloFiltro)
        val edtAutor = view.findViewById<EditText>(R.id.editAutorFiltro)
        val spnCategoria = view.findViewById<Spinner>(R.id.spinnerCategoriaFiltro)
        val rgDisponibilidade = view.findViewById<RadioGroup>(R.id.radioGroupDisponibilidade)
        val btnAplicar = view.findViewById<Button>(R.id.buttonAplicarFiltro)
        val btnLimpar = view.findViewById<Button>(R.id.buttonLimparFiltro)

        // Configurar Spinner de Categorias
        val categorias = arrayOf(
            getString(R.string.categoria_todos),
            getString(R.string.categoria_tecnologia),
            getString(R.string.categoria_literatura),
            getString(R.string.categoria_ciencia),
            getString(R.string.categoria_historia),
            getString(R.string.categoria_biografia),
            getString(R.string.categoria_fantasia),
            getString(R.string.categoria_suspense),
            getString(R.string.categoria_romance),
            getString(R.string.categoria_outros)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnCategoria.adapter = adapter

        // Restaurar valores anteriores
        edtTitulo.setText(filtroTitulo)
        edtAutor.setText(filtroAutor)
        val posCat = categorias.indexOf(filtroCategoria)
        if (posCat >= 0) spnCategoria.setSelection(posCat)
        
        when (filtroDisponibilidade) {
            "Disponível" -> rgDisponibilidade.check(R.id.radioDisponivel)
            "Indisponível" -> rgDisponibilidade.check(R.id.radioIndisponivel)
            else -> rgDisponibilidade.check(R.id.radioTodos)
        }

        btnAplicar.setOnClickListener {
            filtroTitulo = edtTitulo.text.toString().trim()
            filtroAutor = edtAutor.text.toString().trim()
            filtroCategoria = spnCategoria.selectedItem.toString()
            
            filtroDisponibilidade = when (rgDisponibilidade.checkedRadioButtonId) {
                R.id.radioDisponivel -> "Disponível"
                R.id.radioIndisponivel -> "Indisponível"
                else -> "Todos"
            }
            
            salvarFiltros()
            dialog.dismiss()
            Toast.makeText(this, "Filtros aplicados!", Toast.LENGTH_SHORT).show()

            // Realiza a procura imediatamente com o filtro salvo
            executarBusca(termoAtual)
        }

        btnLimpar.setOnClickListener {
            filtroTitulo = ""
            filtroAutor = ""
            filtroCategoria = getString(R.string.categoria_todos)
            filtroDisponibilidade = "Todos"
            salvarFiltros()
            
            edtTitulo.setText("")
            edtAutor.setText("")
            spnCategoria.setSelection(0)
            rgDisponibilidade.check(R.id.radioTodos)
            
            dialog.dismiss()
            Toast.makeText(this, "Filtros limpos", Toast.LENGTH_SHORT).show()
        }

        dialog.setContentView(view)
        dialog.show()
    }
}
