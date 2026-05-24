package com.example.bibliounifornew.adm

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class TelaRF35ConfirmarCadastroADM : AppCompatActivity() {

    private val db                  = FirebaseFirestore.getInstance()
    private lateinit var adapter    : ConfirmacaoAdapter
    private val listaPendentes      = mutableListOf<ItemUsuarioPendente>()
    private val listaCompleta       = mutableListOf<ItemUsuarioPendente>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf35_confirmar_cadastro_adm)

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewConfirmacao)
        adapter = ConfirmacaoAdapter(listaPendentes) { item, position ->
            exibirPopupConfirmacao(item, position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ─── BARRA DE PESQUISA ────────────────────────────────────────────────
        val editPesquisa = findViewById<EditText>(R.id.editPesquisarUsuario)
        editPesquisa?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarLista(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        carregarPendentes()
        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    /**
     * Carrega usuários com cadastroConfirmado == false do Firestore.
     */
    private fun carregarPendentes() {
        db.collection("usuarios")
            .whereEqualTo("cadastroConfirmado", false)
            .get()
            .addOnSuccessListener { result ->
                listaCompleta.clear()
                for (doc in result) {
                    val uid   = doc.id
                    val nome  = doc.getString("nome")  ?: "Usuário"
                    val email = doc.getString("email") ?: ""
                    listaCompleta.add(ItemUsuarioPendente(uid, nome, email))
                }
                filtrarLista("")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Não foi possível carregar os cadastros pendentes. Verifique sua conexão.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filtrarLista(query: String) {
        val filtrado = if (query.isBlank()) listaCompleta
        else listaCompleta.filter {
            it.nome.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)
        }
        listaPendentes.clear()
        listaPendentes.addAll(filtrado)
        adapter.notifyDataSetChanged()
    }

    /**
     * Popup de confirmação de cadastro — confirma no Firestore e remove da lista.
     */
    private fun exibirPopupConfirmacao(item: ItemUsuarioPendente, position: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_confirmacao_usuario)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarCadastro)
        val btnVoltar    = dialog.findViewById<TextView>(R.id.textVoltar)

        btnConfirmar?.setOnClickListener {
            btnConfirmar.isEnabled = false
            // Atualiza cadastroConfirmado no Firestore
            db.collection("usuarios").document(item.uid)
                .set(mapOf("cadastroConfirmado" to true), SetOptions.merge())
                .addOnSuccessListener {
                    dialog.dismiss()
                    Toast.makeText(this, "Cadastro de ${item.nome} confirmado!", Toast.LENGTH_SHORT).show()
                    listaCompleta.removeAll { it.uid == item.uid }
                    adapter.removerItem(position)
                }
                .addOnFailureListener {
                    btnConfirmar.isEnabled = true
                    Toast.makeText(this, "Não foi possível confirmar o cadastro. Tente novamente.", Toast.LENGTH_SHORT).show()
                }
        }

        btnVoltar?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
