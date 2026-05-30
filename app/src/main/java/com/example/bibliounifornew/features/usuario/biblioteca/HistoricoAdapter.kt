package com.example.bibliounifornew.features.usuario.biblioteca

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.usuario.livro.TelaRF12TelaDoLivro
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ItemHistorico(
    val livroId  : String = "",
    val titulo   : String = "",
    val autor    : String = "",
    val acao     : String = "",
    val dataLido : Long   = 0L,
    val coverUrl : String = ""
)

class HistoricoAdapter(
    private val lista: MutableList<ItemHistorico>,
    private val onRemover: (ItemHistorico, Int) -> Unit
) : RecyclerView.Adapter<HistoricoAdapter.HistoricoViewHolder>() {

    inner class HistoricoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView  : MaterialCardView = itemView.findViewById(R.id.cardItemHistorico)
        val txtTitulo : TextView         = itemView.findViewById(R.id.txtTituloHistorico)
        val txtAutor  : TextView         = itemView.findViewById(R.id.txtAutorHistorico)
        val txtAcao   : TextView         = itemView.findViewById(R.id.txtAcaoHistorico)
        val imgCapa   : ImageView        = itemView.findViewById(R.id.imgCapaHistorico)
        val btnRemover: MaterialButton   = itemView.findViewById(R.id.btnRemoverHistorico)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historico, parent, false)
        return HistoricoViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoricoViewHolder, position: Int) {
        val item = lista[position]
        holder.txtTitulo.text = item.titulo
        holder.txtAutor.text  = item.autor
        
        // Formatação da data para o formato: "14 de mar."
        val dataFormatada = if (item.dataLido > 0L) {
            SimpleDateFormat("d 'de' MMM'.'", Locale("pt", "BR")).format(Date(item.dataLido))
        } else {
            ""
        }

        // Legenda de ação personalizada conforme o design
        val legenda = when {
            item.acao.contains("Adicionado", ignoreCase = true) -> "Livro adicionado à livraria ($dataFormatada)"
            item.acao.contains("Removido", ignoreCase = true) -> "Livro removido da livraria ($dataFormatada)"
            item.acao.contains("Solicitado", ignoreCase = true) -> "${item.acao} de ${item.titulo} ($dataFormatada)"
            else -> "${item.acao} ($dataFormatada)"
        }
        
        holder.txtAcao.text = legenda

        // Carregar imagem da capa
        if (item.coverUrl.isNotEmpty()) {
            holder.imgCapa.load(item.coverUrl) {
                placeholder(R.drawable.ic_sem_capa)
                error(R.drawable.ic_sem_capa)
            }
        } else {
            holder.imgCapa.setImageResource(R.drawable.ic_sem_capa)
        }

        holder.btnRemover.setOnClickListener {
            onRemover(item, holder.adapterPosition)
        }

        // RF21.05: clique no card abre os detalhes do livro.
        // O click listener fica no cardView (não no itemView) pois MaterialCardView
        // tem android:clickable=true por padrão no Material theme e consumiria o evento
        // antes de ele chegar ao itemView pai.
        holder.cardView.setOnClickListener {
            if (item.livroId.isNotEmpty()) {
                it.context.startActivity(
                    Intent(it.context, TelaRF12TelaDoLivro::class.java)
                        .putExtra("LIVRO_ID", item.livroId)
                )
            }
        }
    }

    override fun getItemCount(): Int = lista.size

    fun atualizarLista(novaLista: List<ItemHistorico>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }

    fun removerItem(position: Int) {
        lista.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, lista.size)
    }
}
