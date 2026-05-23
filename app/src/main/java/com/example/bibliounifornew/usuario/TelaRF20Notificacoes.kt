package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository

class TelaRF20Notificacoes : AppCompatActivity() {

    private val authRepository = AuthRepository()
    private val usuarioRepository = UsuarioRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf20_notificacoes)

        // 1. CABEÇALHO
        val textNomeUsuario = findViewById<TextView>(R.id.textNomeNotif)
        val usuarioAtual = authRepository.getUsuarioAtual()

        if (usuarioAtual != null) {
            textNomeUsuario?.text = "Carregando..."
            usuarioRepository.buscarPerfilUsuario(usuarioAtual.uid) { sucesso, dados, erro ->
                if (sucesso && dados != null) {
                    textNomeUsuario?.text = dados["nome"] as? String ?: "Usuário"
                } else {
                    textNomeUsuario?.text = "Erro"
                }
            }
        } else {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        // 2. CONFIGURAÇÃO DO SWIPE (Apenas se você já tiver um RecyclerView no XML)
        // Se você ainda usa LinearLayout no XML, o código abaixo deve ficar comentado
        // ou você precisará converter o XML para RecyclerView.
        /*
        val recyclerView = findViewById<RecyclerView>(R.id.seuRecyclerViewId)
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Aqui entraria a chamada para o seu adapter: notificacaoAdapter.removerItem(viewHolder.adapterPosition)
                Toast.makeText(this@TelaRF20Notificacoes, "Notificação removida!", Toast.LENGTH_SHORT).show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        */
    }
}