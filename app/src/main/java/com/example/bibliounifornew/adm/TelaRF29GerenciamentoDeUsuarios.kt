package com.example.bibliounifornew.adm

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF29GerenciamentoDeUsuarios : AppCompatActivity() {

    private val db             = FirebaseFirestore.getInstance()
    private lateinit var adapter: UsuariosAdmAdapter
    private val listaUsuarios  = mutableListOf<ItemUsuarioAdm>()
    private val listaCompleta  = mutableListOf<ItemUsuarioAdm>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf29_gerenciamentousuarios)

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewUsuariosAdm)
        adapter = UsuariosAdmAdapter(listaUsuarios) { item ->
            val intent = Intent(this, TelaRF30UsuariosParaADM::class.java)
            intent.putExtra("USUARIO_ID",    item.uid)
            intent.putExtra("USUARIO_NOME",  item.nome)
            intent.putExtra("USUARIO_EMAIL", item.email)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ─── BUSCA ────────────────────────────────────────────────────────────
        val editBusca = findViewById<EditText>(R.id.editBuscarUsuario)
        editBusca?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarLista(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        carregarUsuarios()
        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    /**
     * Carrega todos os usuários com role != "adm" (alunos).
     */
    private fun carregarUsuarios() {
        db.collection("usuarios")
            .whereEqualTo("role", "aluno")
            .get()
            .addOnSuccessListener { result ->
                listaCompleta.clear()
                for (doc in result) {
                    listaCompleta.add(
                        ItemUsuarioAdm(
                            uid     = doc.id,
                            nome    = doc.getString("nome")    ?: "Usuário",
                            email   = doc.getString("email")   ?: "",
                            usuario = doc.getString("usuario") ?: ""
                        )
                    )
                }
                if (listaCompleta.isEmpty()) {
                    // Fallback: carrega todos (caso o campo role não esteja preenchido)
                    carregarTodosUsuarios()
                } else {
                    filtrarLista("")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar usuários: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun carregarTodosUsuarios() {
        db.collection("usuarios").get()
            .addOnSuccessListener { result ->
                listaCompleta.clear()
                for (doc in result) {
                    val role = doc.getString("role") ?: ""
                    if (role == "adm") continue // exclui administradores
                    listaCompleta.add(
                        ItemUsuarioAdm(
                            uid     = doc.id,
                            nome    = doc.getString("nome")    ?: "Usuário",
                            email   = doc.getString("email")   ?: "",
                            usuario = doc.getString("usuario") ?: ""
                        )
                    )
                }
                filtrarLista("")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar usuários: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filtrarLista(query: String) {
        val filtrado = if (query.isBlank()) listaCompleta
        else listaCompleta.filter {
            it.nome.contains(query, ignoreCase = true) ||
            it.email.contains(query, ignoreCase = true) ||
            it.usuario.contains(query, ignoreCase = true)
        }
        listaUsuarios.clear()
        listaUsuarios.addAll(filtrado)
        adapter.notifyDataSetChanged()
    }
}
