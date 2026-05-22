package com.example.bibliounifornew.adm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF33CadastroLivro : AppCompatActivity() {

    private lateinit var etData: EditText

    // Launcher para obter o resultado do calendário
    private val calendarLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val dataSelecionada = result.data?.getStringExtra("dataSelecionada")
            etData.setText(dataSelecionada)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf33_cadastro_livro)

        val etTitulo = findViewById<EditText>(R.id.editTituloLivro)
        val etAutor = findViewById<EditText>(R.id.editAutorLivro)
        val etISBN = findViewById<EditText>(R.id.editCodigoIsbn)
        etData = findViewById<EditText>(R.id.editDataPublicacao)
        val etQuantidade = findViewById<EditText>(R.id.editQuantidadeExemplares)
        val btnAvancar = findViewById<MaterialButton>(R.id.btnEditarMaisInformacoes2)
        val tvErro = findViewById<TextView>(R.id.textErroCampos)

        // Esconder erro inicialmente
        tvErro.visibility = View.GONE

        // Abrir calendário (RF33 -> Calendário) ao clicar no campo de data
        etData.setOnClickListener {
            val intent = Intent(this, TelaRF33CalendarioPublicacao::class.java)
            // Se já houver algo digitado, podemos passar para o calendário
            intent.putExtra("dataAtual", etData.text.toString())
            calendarLauncher.launch(intent)
        }

        btnAvancar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val autor = etAutor.text.toString().trim()
            val isbn = etISBN.text.toString().trim()
            val data = etData.text.toString().trim()
            val quantidade = etQuantidade.text.toString().trim()

            // Validação de campos obrigatórios
            if (titulo.isEmpty() || autor.isEmpty() || isbn.isEmpty() || data.isEmpty() || quantidade.isEmpty()) {
                tvErro.visibility = View.VISIBLE
                tvErro.text = "Preencha todas as informações do livro"
                Toast.makeText(this, "Preencha todas as informações do livro", Toast.LENGTH_SHORT).show()
            } else if (!validarFormatoData(data)) {
                tvErro.visibility = View.VISIBLE
                tvErro.text = "Data inválida (dd/mm/aaaa)"
                Toast.makeText(this, "Data inválida", Toast.LENGTH_SHORT).show()
            } else {
                tvErro.visibility = View.GONE
                val intent = Intent(this, TelaRF33AdicionarMidiasExtras::class.java)
                startActivity(intent)
            }
        }
    }

    // Função simples para validar formato de data dd/MM/yyyy
    private fun validarFormatoData(data: String): Boolean {
        return data.matches(Regex("""\d{2}/\d{2}/\d{4}"""))
    }
}