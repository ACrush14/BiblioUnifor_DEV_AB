package com.example.bibliounifornew.adm

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF33InfosAdicionais : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf33_adicionar_midia_extras)

        // Mapeamento dos campos
        val etPaginas = findViewById<EditText>(R.id.editQuantidadePaginas)
        val etCategoria = findViewById<EditText>(R.id.editCategoriaLivro)
        val etEditora = findViewById<EditText>(R.id.editEditoraLivro)
        val etLinkCapa = findViewById<EditText>(R.id.editLinkImagem)
        val etSinopse = findViewById<EditText>(R.id.editSinopse)
        val btnAvancar = findViewById<MaterialButton>(R.id.btnEditarMaisInformacoes2)

        btnAvancar.setOnClickListener {
            // Validação simples (Pode ser expandida conforme necessidade)
            if (etPaginas.text.isEmpty() || etCategoria.text.isEmpty()) {
                Toast.makeText(this, "Preencha os campos principais para continuar", Toast.LENGTH_SHORT).show()
            } else {
                // NOTA: Toda criação de livro deverá futuramente integrar banco de dados.
                // A estrutura deve suportar a persistência de: Categorias, Editoras e Sinopses.
                
                val intent = Intent(this@TelaRF33InfosAdicionais, TelaRF33Versoes::class.java)
                startActivity(intent)
            }
        }
    }
}