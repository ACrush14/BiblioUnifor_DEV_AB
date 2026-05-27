package com.example.bibliounifornew.features.adm.gerenciamento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.solicitacoes.ItemAluguel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TelaRFAdmUsuarioAlugados : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: AlugueisUsuarioAdapter
    private val listaAlugueis = mutableListOf<ItemAluguel>()

    private var usuarioId   : String = ""
    private var usuarioNome : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf_adm_usuario_alugados)

        usuarioId   = intent.getStringExtra("USUARIO_ID")   ?: ""
        usuarioNome = intent.getStringExtra("USUARIO_NOME") ?: ""

        val rv = findViewById<RecyclerView>(R.id.recyclerViewAlugueis)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = AlugueisUsuarioAdapter(listaAlugueis)
        rv.adapter = adapter

        if (usuarioId.isNotEmpty()) {
            carregarDadosUsuario()
            carregarLivrosAlugados()
        } else {
            Toast.makeText(
                this,
                getString(R.string.erro_id_usuario_nao_encontrado),
                Toast.LENGTH_SHORT
            ).show()
        }

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    // ─── HEADER ───────────────────────────────────────────────────────────────

    private fun carregarDadosUsuario() {
        db.collection("usuarios").document(usuarioId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener
                val nome  = doc.getString("nome")  ?: usuarioNome.ifEmpty { "Usuário" }
                val email = doc.getString("email") ?: ""
                usuarioNome = nome
                findViewById<TextView>(R.id.textNomeUsuario)?.text  = nome
                findViewById<TextView>(R.id.textEmailUsuario)?.text = email
            }
    }

    // ─── DADOS DOS ALUGUÉIS ───────────────────────────────────────────────────

    /**
     * Busca na coleção primária. Se vazia, tenta a alternativa.
     * RF28.6 FIX: usa extração manual de campos (não toObject()) para
     * compatibilidade com documentos que não seguem o modelo data class.
     */
    private fun carregarLivrosAlugados() {
        db.collection("alugueis")
            .whereEqualTo("uidAluno", usuarioId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    buscarNaColecaoAlternativa()
                } else {
                    processarDocumentos(result.documents)
                }
            }
            .addOnFailureListener { buscarNaColecaoAlternativa() }
    }

    private fun buscarNaColecaoAlternativa() {
        db.collection("solicitacoes_emprestimo")
            .whereEqualTo("uidAluno", usuarioId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(
                        this,
                        getString(R.string.msg_nenhum_livro_alugado),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    processarDocumentos(result.documents)
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(R.string.erro_conexao_banco),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Processa documentos de aluguel com join assíncrono na coleção "livros".
     *
     * RF28.6 FIX — elimina N+1 queries no adapter:
     *   - Extrai campos manualmente (sem toObject) — compatível com variações de nome de campo.
     *   - Pré-carrega título, autor e coverUrl antes de atualizar o RecyclerView.
     *   - O adapter usa os campos já preenchidos — sem nenhuma query Firestore no onBindViewHolder.
     */
    private fun processarDocumentos(documentos: List<DocumentSnapshot>) {
        val total = documentos.size
        var processados = 0
        val listaTemp = mutableListOf<ItemAluguel>()

        // Popula a lista com dados base (sem dados do livro ainda)
        for (doc in documentos) {
            val item = ItemAluguel(
                docId       = doc.id,
                uidAluno    = doc.getString("uidAluno")    ?: doc.getString("usuarioId")   ?: "",
                idLivro     = doc.getString("idLivro")     ?: doc.getString("livroId")     ?: "",
                dataMs      = doc.getLong("dataSolicitacao") ?: doc.getLong("dataMs")      ?: 0L,
                status      = doc.getString("status")      ?: "ativo",
                nomeUsuario = usuarioNome.ifEmpty { "Usuário" }
            )
            listaTemp.add(item)
        }

        // Join assíncrono: busca dados de cada livro em paralelo
        for (item in listaTemp) {
            if (item.idLivro.isEmpty()) {
                processados++
                if (processados == total) publicarLista(listaTemp)
                continue
            }

            db.collection("livros").document(item.idLivro).get()
                .addOnSuccessListener { livroDoc ->
                    val titulo   = livroDoc.getString("title")    ?: livroDoc.getString("titulo")  ?: getString(R.string.sem_titulo)
                    val autor    = livroDoc.getString("author")   ?: livroDoc.getString("autor")   ?: getString(R.string.sem_autor)
                    val coverUrl = livroDoc.getString("coverUrl") ?: ""

                    val idx = listaTemp.indexOfFirst { it.docId == item.docId }
                    if (idx >= 0) {
                        listaTemp[idx] = listaTemp[idx].copy(
                            tituloLivro = titulo,
                            autorLivro  = autor,
                            coverUrl    = coverUrl
                        )
                    }
                    processados++
                    if (processados == total) publicarLista(listaTemp)
                }
                .addOnFailureListener {
                    processados++
                    if (processados == total) publicarLista(listaTemp)
                }
        }
    }

    /** Publica a lista final no adapter na main thread (já estamos nela via Firestore callback). */
    private fun publicarLista(lista: List<ItemAluguel>) {
        listaAlugueis.clear()
        listaAlugueis.addAll(lista.sortedByDescending { it.dataMs })
        adapter.notifyDataSetChanged()
    }
}

// ─── ADAPTER ─────────────────────────────────────────────────────────────────

/**
 * Adapter para exibir livros alugados de um único usuário.
 *
 * RF28.6 FIX — sem queries Firestore no onBindViewHolder:
 * usa [ItemAluguel.coverUrl] e [ItemAluguel.tituloLivro] pré-carregados
 * pela Activity via join assíncrono em processarDocumentos().
 */
class AlugueisUsuarioAdapter(
    private val lista: List<ItemAluguel>
) : RecyclerView.Adapter<AlugueisUsuarioAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nomeUsuario : TextView  = view.findViewById(R.id.txtNomeUsuarioAluguel)
        val titulo      : TextView  = view.findViewById(R.id.txtTituloAluguel)
        val autor       : TextView  = view.findViewById(R.id.txtAutorAluguel)
        val data        : TextView  = view.findViewById(R.id.txtDataAluguel)
        val capa        : ImageView = view.findViewById(R.id.imgCapaAluguel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_aluguel_adm, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.nomeUsuario.text = item.nomeUsuario
        holder.titulo.text      = item.tituloLivro
        holder.autor.text       = item.autorLivro

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        holder.data.text = if (item.dataMs > 0) sdf.format(Date(item.dataMs)) else "--/--/----"

        // Usa a coverUrl pré-carregada — sem query Firestore no bind
        holder.capa.load(item.coverUrl.ifEmpty { R.drawable.osda }) {
            placeholder(R.drawable.osda)
            error(R.drawable.osda)
        }
    }

    override fun getItemCount(): Int = lista.size
}
