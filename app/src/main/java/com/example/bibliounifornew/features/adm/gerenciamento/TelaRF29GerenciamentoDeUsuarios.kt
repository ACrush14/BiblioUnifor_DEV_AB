package com.example.bibliounifornew.features.adm.gerenciamento

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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

    // Mantém o filtro ativo entre onPause/onResume (navegar para detalhe e voltar)
    private var termoBuscaAtivo: String = ""

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
                termoBuscaAtivo = s?.toString() ?: ""
                filtrarLista(termoBuscaAtivo)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    /**
     * Recarrega a lista ao retornar de TelaRF30UsuariosParaADM.
     * Garante que usuários removidos/alterados desapareçam sem precisar
     * de startActivityForResult — o onResume é chamado automaticamente
     * ao empilhar de volta após finish().
     * FIX #7: preserva termoBuscaAtivo em vez de resetar o filtro para "".
     */
    override fun onResume() {
        super.onResume()
        carregarUsuarios()
    }

    /**
     * RF30.9 — Carrega exclusivamente os cadastros com aprovação pendente.
     *
     * Fonte de verdade: Firestore filtra no servidor por:
     *   • role == "aluno"
     *   • cadastroConfirmado == false
     *
     * Zero lógica de filtragem no cliente — lista vazia significa
     * que não há pendentes, não que o fallback deve ser acionado.
     *
     * ⚠️  ÍNDICE COMPOSTO NECESSÁRIO:
     *   Coleção : usuarios
     *   Campos  : role (Ascending) + cadastroConfirmado (Ascending)
     *   Se a query falhar com FAILED_PRECONDITION, o Logcat mostrará o
     *   link direto para criar o índice no Firebase Console.
     */
    private fun carregarUsuarios() {
        db.collection("usuarios")
            .whereEqualTo("role", "aluno")
            .whereEqualTo("cadastroConfirmado", false)
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
                // Lista vazia = nenhum cadastro pendente — comportamento correto,
                // sem fallback que traria usuários já aprovados.
                filtrarLista(termoBuscaAtivo)
            }
            .addOnFailureListener { e ->
                val msg = e.message ?: ""
                if (msg.contains("FAILED_PRECONDITION", ignoreCase = true)) {
                    Log.e(
                        "RF30_INDEX",
                        "Índice composto ausente para a query de cadastros pendentes.\n" +
                        "Crie em: Firebase Console → Firestore → Indexes → Add index\n" +
                        "  Coleção : usuarios\n" +
                        "  Campos  : role (Ascending), cadastroConfirmado (Ascending)\n" +
                        "O próprio Firebase costuma incluir o link direto neste erro:\n$msg"
                    )
                } else {
                    Log.e("RF30", "Erro ao carregar cadastros pendentes: $msg")
                }
                Toast.makeText(this, "Erro ao carregar usuários: $msg", Toast.LENGTH_SHORT).show()
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
