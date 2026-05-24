package com.example.bibliounifornew.adm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R

/**
 * Modelo de dados para livros na visão do ADM.
 */
data class ItemLivroAdm(
    val docId: String = "",
    val titulo: String = "",
    val autor: String = "",
    val isbn: String = "",
    val quantidade: Long = 0,
    val coverUrl: String = ""
)

/**
 * Adapter para gerenciar o acervo de livros na TelaRF32.
 */
class LivrosCrudAdapter(
    private var lista: MutableList<ItemLivroAdm>,
    private val onEditar: (ItemLivroAdm) -> Unit
) : RecyclerView.Adapter<LivrosCrudAdapter.LivroViewHolder>() {

    class LivroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCapa: ImageView = view.findViewById(R.id.imgCapaLivro)
        val txtTitulo: TextView = view.findViewById(R.id.txtTituloLivro)
        val txtAutor: TextView = view.findViewById(R.id.txtAutorLivro)
        val txtIsbn: TextView = view.findViewById(R.id.txtIsbnLivro)
        val txtQtd: TextView = view.findViewById(R.id.txtQuantidadeLivro)
        val btnEditar: ImageView = view.findViewById(R.id.btnEditarLivro)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_livro_adm, parent, false)
        return LivroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LivroViewHolder, position: Int) {
        val item = lista[position]
        holder.txtTitulo.text = item.titulo
        holder.txtAutor.text = item.autor
        holder.txtIsbn.text = "ISBN: ${item.isbn}"
        holder.txtQtd.text = "Qtd: ${item.quantidade}"

        // Em uma implementação real, o Coil carregaria a imagem aqui:
        // holder.imgCapa.load(item.coverUrl)

        holder.btnEditar.setOnClickListener { onEditar(item) }
        holder.itemView.setOnClickListener { onEditar(item) }
    }

    override fun getItemCount() = lista.size

    fun atualizarLista(novaLista: List<ItemLivroAdm>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }
}
