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
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TelaRFAdmUsuarioAlugados : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: AlugueisUsuarioAdapter
    private val listaAlugueis = mutableListOf<ItemAluguel>()
    private var usuarioId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf_adm_usuario_alugados)

        usuarioId = intent.getStringExtra("USUARIO_ID") ?: ""

        val rv = findViewById<RecyclerView>(R.id.recyclerViewAlugueis)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = AlugueisUsuarioAdapter(listaAlugueis)
        rv.adapter = adapter

        if (usuarioId.isNotEmpty()) {
            carregarDadosUsuario()
            carregarLivrosAlugados()
        } else {
            Toast.makeText(this, "Erro: ID do usuário não encontrado.", Toast.LENGTH_SHORT).show()
        }
        
        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    private fun carregarDadosUsuario() {
        db.collection("usuarios").document(usuarioId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    findViewById<TextView>(R.id.textNomeUsuario)?.text = doc.getString("nome") ?: "Usuário"
                    findViewById<TextView>(R.id.textEmailUsuario)?.text = doc.getString("email") ?: ""
                }
            }
    }

    private fun carregarLivrosAlugados() {
        // Busca na coleção 'alugueis' filtrando pelo usuarioId (uidAluno no seu modelo)
        // Usamos status 'aprovado' ou 'alugado' dependendo de como você salva
        db.collection("alugueis")
            .whereEqualTo("uidAluno", usuarioId)
            .get()
            .addOnSuccessListener { result ->
                listaAlugueis.clear()
                for (doc in result) {
                    val item = doc.toObject(ItemAluguel::class.java)
                    listaAlugueis.add(item)
                }
                
                if (listaAlugueis.isEmpty()) {
                    Toast.makeText(this, "Este usuário não possui livros alugados.", Toast.LENGTH_SHORT).show()
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

class AlugueisUsuarioAdapter(private val lista: List<ItemAluguel>) : 
    RecyclerView.Adapter<AlugueisUsuarioAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nomeUsuario: TextView = view.findViewById(R.id.txtNomeUsuarioAluguel)
        val titulo: TextView = view.findViewById(R.id.txtTituloAluguel)
        val autor: TextView = view.findViewById(R.id.txtAutorAluguel)
        val data: TextView = view.findViewById(R.id.txtDataAluguel)
        val capa: ImageView = view.findViewById(R.id.imgCapaAluguel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_aluguel_adm, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        
        holder.nomeUsuario.text = item.nomeUsuario
        holder.titulo.text = item.tituloLivro
        holder.autor.text  = item.autorLivro
        
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        holder.data.text = if (item.dataMs > 0) sdf.format(Date(item.dataMs)) else "--/--/----"

        // Busca a capa do livro no Firestore se não estiver no ItemAluguel
        FirebaseFirestore.getInstance().collection("livros").document(item.idLivro).get()
            .addOnSuccessListener { doc ->
                val url = doc.getString("coverUrl") ?: ""
                holder.capa.load(url.ifEmpty { R.drawable.osda }) {
                    placeholder(R.drawable.osda)
                    error(R.drawable.osda)
                }
            }
    }

    override fun getItemCount() = lista.size
}
