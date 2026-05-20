package com.example.bibliounifornew

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.data.EntidadeLivro
import kotlin.collections.get

class LivroAdapter(
    private var livros: List<EntidadeLivro>,
    private val onItemClick: (EntidadeLivro) -> Unit
) : RecyclerView.Adapter<LivroAdapter.LivroViewHolder>() {

    class LivroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgLivro: ImageView = view.findViewById(R.id.imgCapaLivro)
        val textTitulo: TextView = view.findViewById(R.id.txtTituloLivro)
        val textAutor: TextView = view.findViewById(R.id.txtAutorLivro)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book_card, parent, false)
        return LivroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LivroViewHolder, position: Int) {
        val livro = livros[position]
        holder.textTitulo.text = livro.title
        holder.textAutor.text = livro.author

        // O carregamento real da URL da internet será feito via Glide ou Coil futuramente.
        if (livro.coverUrl.isNotEmpty()) {
            // TODO: Glide.with(holder.itemView.context).load(livro.coverUrl).into(holder.imgLivro)
            holder.imgLivro.setImageResource(R.drawable.osda)
        } else {
            holder.imgLivro.setImageResource(R.drawable.osda) // Default
        }

        holder.itemView.setOnClickListener { onItemClick(livro) }
    }

    override fun getItemCount() = livros.size

    fun updateData(newLivros: List<EntidadeLivro>) {
        livros = newLivros
        notifyDataSetChanged()
    }
}
