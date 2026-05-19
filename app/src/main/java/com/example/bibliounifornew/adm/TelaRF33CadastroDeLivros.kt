package com.example.bibliounifornew.adm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF33CadastroDeLivros : AppCompatActivity() {

    private lateinit var etData: EditText
    private val REQUEST_CODE_DATA = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf33_cadastro_livro)

        val etTitulo = findViewById<EditText>(R.id.editTituloLivro)
        val etAutor = findViewById<EditText>(R.id.editAutorLivro)
        val etISBN = findViewById<EditText>(R.id.editCodigoIsbn)
        etData = findViewById<EditText>(R.id.editDataPublicacao)
        val etQuantidade = findViewById<EditText>(R.id.editQuantidadeExemplares)
        val btnEditarMais1 = findViewById<MaterialButton>(R.id.btnEditarMaisInformacoes2)
        val tvErro = findViewById<TextView>(R.id.textErroCampos)

        // Abrir calendário
        // Ver qual pop up faz isso!

        btnEditarMais1.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val autor = etAutor.text.toString().trim()
            val isbn = etISBN.text.toString().trim()
            val data = etData.text.toString().trim()
            val quantidade = etQuantidade.text.toString().trim()

            if (titulo.isEmpty() || autor.isEmpty() || isbn.isEmpty() || data.isEmpty() || quantidade.isEmpty()) {
                tvErro.visibility = View.VISIBLE
                // Feedback visual para o usuário
                Toast.makeText(this@TelaRF33CadastroDeLivros, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
            } else {
                tvErro.visibility = View.GONE
                val intent = Intent(this@TelaRF33CadastroDeLivros, TelaRF33InfosAdicionais::class.java)
                startActivity(intent)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_DATA && resultCode == RESULT_OK) {
            val dataSelecionada = data?.getStringExtra("dataSelecionada")
            etData.setText(dataSelecionada)
        }
    }

}