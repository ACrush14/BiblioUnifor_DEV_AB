package com.example.bibliounifornew.features.adm.solicitacoes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.gerenciamento.NavigationHelperADM
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF30UsuariosParaADM
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF37InfoLivroADM
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
        // Tenta buscar na coleção solicitacoes_emprestimo
        // Se falhar, tentamos carregar sem o orderBy para evitar erro de índice ausente
        db.collection("solicitacoes_emprestimo")
            .whereIn("status", listOf("pendente", "ativo", "atrasado"))
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    // Se estiver vazio, talvez a coleção seja 'alugueis'? 
                    // Vamos tentar uma segunda busca preventiva
                    buscarNaColecaoAlternativa()
                    return@addOnSuccessListener
                }
                processarDocumentos(result.documents)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("FirestoreError", "Erro ao carregar alugueis: ${e.message}")
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                buscarNaColecaoAlternativa()
            }
    }

    private fun buscarNaColecaoAlternativa() {
        db.collection("alugueis")
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    processarDocumentos(result.documents)
                } else {
                    Toast.makeText(this, "Nenhum aluguel encontrado em nenhuma coleção.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun processarDocumentos(documentos: List<com.google.firebase.firestore.DocumentSnapshot>) {
        val totalDocs = documentos.size
        var processados = 0
        val listaTemp = mutableListOf<ItemAluguel>()

        for (doc in documentos) {
            val docId = doc.id
            val uidAluno = doc.getString("uidAluno") ?: doc.getString("usuarioId") ?: ""
            val idLivro = doc.getString("idLivro") ?: doc.getString("livroId") ?: ""
            val status = doc.getString("status") ?: "ativo"
            val dataMs = doc.getLong("dataSolicitacao") ?: doc.getLong("dataMs") ?: 0L

            val itemBase = ItemAluguel(
                docId = docId,
                uidAluno = uidAluno,
                idLivro = idLivro,
                dataMs = dataMs,
                status = status
            )
            listaTemp.add(itemBase)

            var nomeUsuario = "Usuário"
            var tituloLivro = "Título..."
            var autorLivro = "Autor..."
            var joinsRestantes = 2

            fun verificarConclusao() {
                joinsRestantes--
                if (joinsRestantes == 0) {
                    val idx = listaTemp.indexOfFirst { it.docId == docId }
                    if (idx >= 0) {
                        listaTemp[idx] = listaTemp[idx].copy(
                            nomeUsuario = nomeUsuario,
                            tituloLivro = tituloLivro,
                            autorLivro = autorLivro
                        )
                    }
                    processados++
                    if (processados == totalDocs) {
                        adapter.atualizarLista(listaTemp.sortedByDescending { it.dataMs })
                    }
                }
            }

            if (uidAluno.isNotEmpty()) {
                db.collection("usuarios").document(uidAluno).get()
                    .addOnSuccessListener { u ->
                        nomeUsuario = u.getString("nome") ?: u.getString("email") ?: "Usuário"
                        verificarConclusao()
                    }.addOnFailureListener { verificarConclusao() }
            } else { verificarConclusao() }

            if (idLivro.isNotEmpty()) {
                db.collection("livros").document(idLivro).get()
                    .addOnSuccessListener { l ->
                        tituloLivro = l.getString("title") ?: l.getString("titulo") ?: "Título"
                        autorLivro = l.getString("author") ?: l.getString("autor") ?: "Autor"
                        verificarConclusao()
                    }.addOnFailureListener { verificarConclusao() }
            } else { verificarConclusao() }
        }
    }
}
