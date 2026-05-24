package com.example.bibliounifornew

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.data.EntidadeLivro
import com.google.android.material.button.MaterialButton

class LivroAdapter(
    private var livros: List<EntidadeLivro>,
    private val onItemClick   : (EntidadeLivro) -> Unit,
    private val onSuaLivraria : (EntidadeLivro) -> Unit = {},
    private val onAlugarLivro : (EntidadeLivro) -> Unit = {}
) : RecyclerView.Adapter<LivroAdapter.LivroViewHolder>() {

    class LivroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgLivro   : ImageView      = view.findViewById(R.id.imgCapaLivro)
        val textTitulo : TextView       = view.findViewById(R.id.txtTituloLivro)
        val textAutor  : TextView       = view.findViewById(R.id.txtAutorLivro)
        val textData   : TextView       = view.findViewById(R.id.txtDataLivro)
        val btnMais    : ImageView      = view.findViewById(R.id.btnMais)
        val btnLivraria: MaterialButton = view.findViewById(R.id.btnSuaLivraria)
        val btnAlugar  : MaterialButton = view.findViewById(R.id.btnAlugarLivro)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book_card, parent, false)
        return LivroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LivroViewHolder, position: Int) {
        val livro = livros[position]

        holder.textTitulo.text = livro.title
        holder.textAutor.text  = livro.author
        holder.textData.text   = livro.publishDate.ifEmpty { "" }

        if (livro.coverUrl.isNotEmpty()) {
            holder.imgLivro.load(livro.coverUrl) {
                crossfade(true)
                placeholder(R.drawable.osda)
                error(R.drawable.osda)
            }
        } else {
            holder.imgLivro.setImageResource(R.drawable.osda)
        }

        // Todos os pontos de entrada para os detalhes do livro
        holder.itemView.setOnClickListener    { onItemClick(livro) }
        holder.btnMais.setOnClickListener     { onItemClick(livro) }
        // Ações de atalho direto no card
        holder.btnLivraria.setOnClickListener { onSuaLivraria(livro) }
        holder.btnAlugar.setOnClickListener   { onAlugarLivro(livro) }
    }

    override fun getItemCount() = livros.size

    fun updateData(novosLivros: List<EntidadeLivro>) {
        this.livros = novosLivros
        notifyDataSetChanged()
    }
}
