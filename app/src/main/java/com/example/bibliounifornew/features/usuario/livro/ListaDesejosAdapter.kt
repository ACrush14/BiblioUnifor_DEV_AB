package com.example.bibliounifornew.features.usuario.livro

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

data class ItemListaDesejos(
    val docId      : String = "",
    val livroId    : String = "",
    val titulo     : String = "",
    val autor      : String = "",
    val coverUrl   : String = "",
    val disponivel : Boolean = false
)

class ListaDesejosAdapter(
    private val lista      : MutableList<ItemListaDesejos>,
    private val onLivraria : (ItemListaDesejos) -> Unit,
    private val onAlugar   : (ItemListaDesejos) -> Unit,
    private val onExcluir  : (ItemListaDesejos, Int) -> Unit
) : RecyclerView.Adapter<ListaDesejosAdapter.DesejoViewHolder>() {

    inner class DesejoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCapa      : ImageView     = itemView.findViewById(R.id.imgCapaDesejos)
        val txtTitulo    : TextView      = itemView.findViewById(R.id.txtTituloDesejos)
        val txtAutor     : TextView      = itemView.findViewById(R.id.txtAutorDesejos)
        val txtStatus    : TextView      = itemView.findViewById(R.id.txtStatusDesejos)
        val menuIcon     : TextView      = itemView.findViewById(R.id.menuDesejos)
        val btnLivraria  : MaterialButton = itemView.findViewById(R.id.btnSuaLivrariaDesejos)
        val btnAlugar    : MaterialButton = itemView.findViewById(R.id.btnAlugarDesejos)
        val btnExcluir   : MaterialButton = itemView.findViewById(R.id.btnExcluirDesejos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DesejoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lista_desejos, parent, false)
        return DesejoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DesejoViewHolder, position: Int) {
        val item = lista[position]

        holder.txtTitulo.text = item.titulo.ifEmpty { item.livroId }
        holder.txtAutor.text  = item.autor

        if (item.disponivel) {
            holder.txtStatus.text      = "Disponível"
            holder.txtStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
            holder.btnAlugar.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0056FF")) // Azul original
        } else {
            holder.txtStatus.text      = "Indisponível"
            holder.txtStatus.setTextColor(android.graphics.Color.parseColor("#C62828"))
            holder.btnAlugar.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#9E9E9E")) // Cinza
        }

        if (item.coverUrl.isNotEmpty()) {
            holder.imgCapa.load(item.coverUrl) {
                placeholder(R.drawable.osda)
                error(R.drawable.osda)
            }
        } else {
            holder.imgCapa.setImageResource(R.drawable.osda)
        }

        // Menu (3 pontos) e clique no card → abre detalhes do livro (RF12)
        val abrirDetalhes = View.OnClickListener {
            val intent = Intent(holder.itemView.context, TelaRF12TelaDoLivro::class.java)
                .putExtra("LIVRO_ID", item.livroId)
            holder.itemView.context.startActivity(intent)
        }
        holder.itemView.setOnClickListener(abrirDetalhes)
        holder.menuIcon.setOnClickListener(abrirDetalhes)

        holder.btnLivraria.setOnClickListener { onLivraria(item) }
        holder.btnAlugar.setOnClickListener   { onAlugar(item)   }
        holder.btnExcluir.setOnClickListener  { onExcluir(item, holder.adapterPosition) }
    }

    override fun getItemCount(): Int = lista.size

    fun atualizarLista(nova: List<ItemListaDesejos>) {
        lista.clear()
        lista.addAll(nova)
        notifyDataSetChanged()
    }

    fun removerItem(position: Int) {
        if (position < 0 || position >= lista.size) return
        lista.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, lista.size)
    }
}
