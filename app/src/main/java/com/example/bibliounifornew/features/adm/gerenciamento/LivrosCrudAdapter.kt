package com.example.bibliounifornew.features.adm.gerenciamento

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

// ─── Modelo de dado ───────────────────────────────────────────────────────────
data class ItemLivroAdm(
    val docId      : String = "",
    val titulo     : String = "Título Indisponível",
    val autor      : String = "Autor Desconhecido",
    val isbn       : String = "",
    val quantidade : Long   = 0L,
    val coverUrl   : String = ""
)

// ─── Adapter ─────────────────────────────────────────────────────────────────
class LivrosCrudAdapter(
    private val lista    : MutableList<ItemLivroAdm>,
    private val onEditar : (ItemLivroAdm) -> Unit
) : RecyclerView.Adapter<LivrosCrudAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCapa      : ImageView     = itemView.findViewById(R.id.imgCapaLivroCrud)
        val txtTitulo    : TextView      = itemView.findViewById(R.id.txtTituloLivroCrud)
        val txtAutor     : TextView      = itemView.findViewById(R.id.txtAutorLivroCrud)
        val txtIsbn      : TextView      = itemView.findViewById(R.id.txtIsbnLivroCrud)
        val txtQtd       : TextView      = itemView.findViewById(R.id.txtQuantidadeLivroCrud)
        val btnEditar    : MaterialButton = itemView.findViewById(R.id.btnEditarLivroCrud)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_livro_crud_adm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        holder.txtTitulo.text = item.titulo
        holder.txtAutor.text  = item.autor
        val ctx = holder.itemView.context
        holder.txtIsbn.text = if (item.isbn.isNotEmpty())
            ctx.getString(R.string.fmt_isbn, item.isbn)
            else ctx.getString(R.string.fmt_isbn_vazio)
        holder.txtQtd.text = if (item.quantidade > 0)
            ctx.getString(R.string.fmt_exemplares, item.quantidade)
            else ctx.getString(R.string.fmt_exemplares_vazio)

        // Capa via Coil — usa coverUrl pré-carregada; fallback para placeholder
        holder.imgCapa.load(item.coverUrl.ifEmpty { null }) {
            placeholder(R.drawable.user_placeholder)
            error(R.drawable.user_placeholder)
            fallback(R.drawable.user_placeholder)
        }

        holder.btnEditar.setOnClickListener { onEditar(item) }
    }

    override fun getItemCount(): Int = lista.size

    /** Substitui toda a lista e atualiza o RecyclerView. */
    fun atualizarLista(novaLista: List<ItemLivroAdm>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }
}
