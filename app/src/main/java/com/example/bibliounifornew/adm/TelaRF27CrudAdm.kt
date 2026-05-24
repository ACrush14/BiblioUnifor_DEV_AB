package com.example.bibliounifornew.adm

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF27CrudAdm : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf27_crud_adm)

        val btnCriarMidia = findViewById<MaterialButton>(R.id.buttonCriarMidia)
        val btnVerificarMidia = findViewById<MaterialButton>(R.id.buttonVerificarMidia)
        val btnGerenciarUsuario = findViewById<MaterialButton>(R.id.buttonGerenciarUsuarios)

        btnCriarMidia.setOnClickListener {
            // Fluxo: CRUD ADM -> Criar mídia -> Cadastro livro
            val intent = Intent(this, TelaRF33CadastroLivro::class.java)
            startActivity(intent)
        }

        btnVerificarMidia.setOnClickListener {
            val intent = Intent(this, TelaRF32LivrosCRUD::class.java)
            startActivity(intent)
        }

        btnGerenciarUsuario.setOnClickListener {
            val intent = Intent(this, TelaRF29GerenciamentoDeUsuarios::class.java)
            startActivity(intent)
        }

        NavigationHelperADM.configurarBarraNavegacao(this)
    }
}
