package com.example.bibliounifornew.adm

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.adm.TelaRF30UsuariosParaADM

class TelaRF29GerenciamentoDeUsuarios : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Correção: Usando o ID do layout XML (R.layout.telarf31_gerenciamentousuarios)
        setContentView(R.layout.telarf29_gerenciamentousuarios)

        // 🔹 Referenciando os itens incluídos via ID (definidos na tag <include> do XML)
        // Usamos View para evitar erros de cast caso o layout mude
        val usuario1 = findViewById<View>(R.id.viewVerUsuario)

        // 🔹 Clique no primeiro usuário
        usuario1?.setOnClickListener {
            val intent = Intent(this@TelaRF29GerenciamentoDeUsuarios, TelaRF30UsuariosParaADM::class.java)
            startActivity(intent)
        }

        NavigationHelperADM.configurarBarraNavegacao(this)
    }
}