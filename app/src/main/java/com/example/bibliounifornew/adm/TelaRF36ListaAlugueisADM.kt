package com.example.bibliounifornew.adm

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TelaRF36ListaAlugueisADM : AppCompatActivity() {

    private val db            = FirebaseFirestore.getInstance()
    private lateinit var adapter: AlugueisAdapter
    private val listaAlugueis = mutableListOf<ItemAluguel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf36_lista_alugueis_adm)

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAlugueis)
        adapter = AlugueisAdapter(
            listaAlugueis,
            onVerLivro    = { item ->
                val intent = Intent(this, TelaRF37InfoLivroADM::class.java)
                intent.putExtra("LIVRO_ID", item.idLivro)
                startActivity(intent)
            },
            onVerUsuario  = { item ->
                val intent = Intent(this, TelaRF30UsuariosParaADM::class.java)
                intent.putExtra("USUARIO_ID", item.uidAluno)
                startActivity(intent)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        carregarAlugueis()

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    /**
     * Carrega solicitacoes_emprestimo (status != "devolvido"), depois enriquece
     * com nome do usuário via join em usuarios/{uidAluno} e título via livros/{idLivro}.
     */
    private fun carregarAlugueis() {
        db.collection("solicitacoes_emprestimo")
            .whereIn("status", listOf("pendente", "ativo"))
            .orderBy("dataSolicitacao", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "Nenhum aluguel ativo.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val documentos = result.documents
                val totalDocs  = documentos.size
                var processados = 0
                val listaTemp  = mutableListOf<ItemAluguel>()

                for (doc in documentos) {
                    val docId    = doc.id
                    val uidAluno = doc.getString("uidAluno") ?: ""
                    val idLivro  = doc.getString("idLivro")  ?: ""
                    val status   = doc.getString("status")   ?: "pendente"
                    val dataMs   = doc.getLong("dataSolicitacao") ?: 0L

                    // Placeholders enquanto os joins carregam
                    val itemBase = ItemAluguel(
                        docId       = docId,
                        uidAluno    = uidAluno,
                        idLivro     = idLivro,
                        dataMs      = dataMs,
                        status      = status
                    )
                    listaTemp.add(itemBase)

                    // Join: busca nome do usuário e título do livro em paralelo
                    var nomeUsuario = "Usuário"
                    var tituloLivro = "Título Indisponível"
                    var autorLivro  = "Autor Desconhecido"
                    var joinsRestantes = 2

                    fun verificarConclusao() {
                        joinsRestantes--
                        if (joinsRestantes == 0) {
                            val idx = listaTemp.indexOfFirst { it.docId == docId }
                            if (idx >= 0) {
                                listaTemp[idx] = listaTemp[idx].copy(
                                    nomeUsuario = nomeUsuario,
                                    tituloLivro = tituloLivro,
                                    autorLivro  = autorLivro
                                )
                            }
                            processados++
                            if (processados == totalDocs) {
                                adapter.atualizarLista(listaTemp)
                            }
                        }
                    }

                    if (uidAluno.isNotEmpty()) {
                        db.collection("usuarios").document(uidAluno).get()
                            .addOnSuccessListener { u ->
                                nomeUsuario = u.getString("nome") ?: u.getString("email") ?: "Usuário"
                                verificarConclusao()
                            }
                            .addOnFailureListener { verificarConclusao() }
                    } else {
                        verificarConclusao()
                    }

                    if (idLivro.isNotEmpty()) {
                        db.collection("livros").document(idLivro).get()
                            .addOnSuccessListener { l ->
                                tituloLivro = l.getString("title")  ?: l.getString("titulo") ?: "Título Indisponível"
                                autorLivro  = l.getString("author") ?: l.getString("autor")  ?: "Autor Desconhecido"
                                verificarConclusao()
                            }
                            .addOnFailureListener { verificarConclusao() }
                    } else {
                        verificarConclusao()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Não foi possível carregar os aluguéis. Verifique sua conexão.", Toast.LENGTH_SHORT).show()
            }
    }
}
