package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF17Amigos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf17_amigos)

        // 1) Lógica do botão "Adicionar Amigos" -> Abre busca
        val layoutAdicionarAmigos = findViewById<View>(R.id.layoutAdicionarAmigos)
        layoutAdicionarAmigos.setOnClickListener {
            val intent = Intent(this, TelaRF17_3_BuscaAmigos::class.java)
            startActivity(intent)
        }

        // 4) Ao clicar na foto, nome ou nos 3 pontinhos -> Abre Perfil (apenas visualização)
        configurarCliquePerfil(R.id.fotoAmigo1, R.id.nomeAmigo1, R.id.menuAmigo1)
        configurarCliquePerfil(R.id.fotoAmigo2, R.id.nomeAmigo2, R.id.menuAmigo2)
        configurarCliquePerfil(R.id.fotoAmigo3, R.id.nomeAmigo3, R.id.menuAmigo3)
        configurarCliquePerfil(R.id.fotoAmigo4, R.id.nomeAmigo4, R.id.menuAmigo4)
        configurarCliquePerfil(R.id.fotoAmigo5, R.id.nomeAmigo5, R.id.menuAmigo5)
    }

    private fun configurarCliquePerfil(fotoId: Int, nomeId: Int, menuId: Int) {
        val clickListener = View.OnClickListener {
            val intent = Intent(this, TelaRF17_5_PerfilAmigo::class.java)
            startActivity(intent)
        }

        findViewById<View>(fotoId).setOnClickListener(clickListener)
        findViewById<View>(nomeId).setOnClickListener(clickListener)
        findViewById<View>(menuId).setOnClickListener(clickListener)
    }
}
