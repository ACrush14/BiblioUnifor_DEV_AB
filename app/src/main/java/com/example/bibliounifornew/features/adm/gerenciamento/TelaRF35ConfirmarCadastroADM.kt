package com.example.bibliounifornew.features.adm.gerenciamento

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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
                Toast.makeText(this, getString(R.string.erro_carregar_pendentes), Toast.LENGTH_SHORT).show()
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
     * GAP-3 FIX: Popup de confirmação de cadastro.
     *
     * Operações realizadas em sequência no Dispatchers.IO:
     *   1. update() com cadastroConfirmado=true, confirmadoEm e confirmadoPor (audit trail)
     *   2. add() na subcoleção notificacoes do aluno — notifica cadastro aprovado
     *
     * Ambas as operações usam .await() para encadear sem callback hell.
     * A UI (Toast + remoção do item) retorna à Main Thread via withContext(Main).
     *
     * Position stale-safety: a remoção usa indexOf na lista live em vez de
     * capturar o Int do adapterPosition no momento do clique.
     */
    private fun exibirPopupConfirmacao(item: ItemUsuarioPendente, @Suppress("UNUSED_PARAMETER") position: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_confirmacao_usuario)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val txtAviso     = dialog.findViewById<TextView>(R.id.textAvisoCadastro)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarCadastro)
        val btnVoltar    = dialog.findViewById<TextView>(R.id.textVoltar)

        // Informa ao ADM qual usuário está sendo aprovado
        txtAviso?.text = getString(R.string.popup_confirmacao_texto_usuario, item.nome)

        btnConfirmar?.setOnClickListener {
            val admUid = FirebaseAuth.getInstance().currentUser?.uid
            if (admUid.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.erro_sessao_expirada), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnConfirmar.isEnabled = false
            btnConfirmar.text      = getString(R.string.msg_salvando)

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // ── Passo 1: aprova o cadastro com campos de audit trail ────
                    db.collection("usuarios").document(item.uid)
                        .update(
                            mapOf(
                                "cadastroConfirmado" to true,
                                "confirmadoEm"       to System.currentTimeMillis(),
                                "confirmadoPor"      to admUid
                            )
                        ).await()

                    // ── Passo 2: notificação na subcoleção do aluno ────────────
                    // Lida pelo NotificacaoAdapter do usuário na RF20.
                    db.collection("usuarios").document(item.uid)
                        .collection("notificacoes")
                        .add(
                            mapOf(
                                "titulo"    to getString(R.string.notif_cadastro_aprovado_titulo),
                                "mensagem"  to getString(R.string.notif_cadastro_aprovado_msg),
                                "tipo"      to "cadastro_aprovado",
                                "lida"      to false,
                                "criadoEm"  to System.currentTimeMillis()
                            )
                        ).await()

                    // ── Passo 3: atualiza UI na Main Thread ───────────────────
                    withContext(Dispatchers.Main) {
                        if (isFinishing || isDestroyed) return@withContext
                        dialog.dismiss()
                        Toast.makeText(
                            this@TelaRF35ConfirmarCadastroADM,
                            getString(R.string.fmt_cadastro_confirmado, item.nome),
                            Toast.LENGTH_SHORT
                        ).show()
                        // Remove da lista completa primeiro, depois do adapter
                        listaCompleta.removeAll { it.uid == item.uid }
                        val liveIdx = listaPendentes.indexOfFirst { it.uid == item.uid }
                        if (liveIdx >= 0) adapter.removerItem(liveIdx)
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (isFinishing || isDestroyed) return@withContext
                        btnConfirmar.isEnabled = true
                        btnConfirmar.text      = getString(R.string.popup_confirmacao_btn_confirmar)
                        Toast.makeText(
                            this@TelaRF35ConfirmarCadastroADM,
                            getString(R.string.erro_confirmar_cadastro),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        btnVoltar?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
