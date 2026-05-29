package com.example.bibliounifornew.features.usuario.livro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ItemAvaliacaoLivro(
    val idUsuario     : String = "",
    val nomeUsuario   : String = "Usuário",
    val textoAvaliacao: String = "",
    val dataMs        : Long   = 0L
)

class AvaliacoesLivroAdapter(
    private val lista: MutableList<ItemAvaliacaoLivro>
) : RecyclerView.Adapter<AvaliacoesLivroAdapter.ViewHolder>() {

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome : TextView = view.findViewById(R.id.txtNomeAvaliador)
        val txtData : TextView = view.findViewById(R.id.txtDataAvaliacao)
        val txtTexto: TextView = view.findViewById(R.id.txtTextoAvaliacao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_avaliacao_livro, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.txtNome.text  = item.nomeUsuario
        holder.txtData.text  = if (item.dataMs > 0L) sdf.format(Date(item.dataMs)) else ""
        holder.txtTexto.text = item.textoAvaliacao
    }

    override fun getItemCount(): Int = lista.size

    fun atualizarLista(novaLista: List<ItemAvaliacaoLivro>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }
}
