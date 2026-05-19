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
        val textTitulo: TextView = view.findViewById(R.id.textTituloLivro)
        val textAutor: TextView = view.findViewById(R.id.textAutorLivro)
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

        // Se houver uma imagem real, usaríamos uma lib como Glide ou Coil.
        // Por enquanto, usamos o placeholder ou o coverResourceId se disponível
        if (livro.coverResourceId != 0) {
            holder.imgLivro.setImageResource(livro.coverResourceId)
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