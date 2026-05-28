package com.example.bibliounifornew.features.adm.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class VencidosAdapter(
    private val onAction: (LivroVencidoModel, String) -> Unit
) : RecyclerView.Adapter<VencidosAdapter.VencidoViewHolder>() {

    private var items: MutableList<LivroVencidoModel> = mutableListOf()

    fun atualizarLista(novaLista: List<LivroVencidoModel>) {
        items.clear()
        items.addAll(novaLista)
        notifyDataSetChanged()
    }

    fun removerPorDocId(docId: String) {
        val index = items.indexOfFirst { it.docIdAtual == docId }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VencidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_livro_vencido, parent, false)
        return VencidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VencidoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class VencidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtTitulo     : TextView = view.findViewById(R.id.txtTituloVencido)
        private val txtUsuario    : TextView = view.findViewById(R.id.txtUsuarioVencido)
        private val txtDiasAtraso : TextView = view.findViewById(R.id.txtDiasAtrasoVencido)
        private val txtMulta      : TextView = view.findViewById(R.id.txtMultaVencido)
        private val btnRenovar    : MaterialButton = view.findViewById(R.id.btnRenovarVencido)

        fun bind(modelo: LivroVencidoModel) {
            txtTitulo.text = modelo.tituloLivro
            txtUsuario.text = "Usuário: ${modelo.nomeAluno}"
            txtDiasAtraso.text = "${modelo.diasAtraso} dias"
            txtMulta.text = "Multa est.: R$ ${String.format("%.2f", modelo.valorMulta).replace('.', ',')}"

            btnRenovar.setOnClickListener {
                onAction(modelo, "RENOVAR")
            }

            itemView.setOnClickListener {
                onAction(modelo, "VER_DETALHES")
            }

            // ABRIR_LIVRO could be triggered by a long click or another button if it existed.
            // In the Activity it's defined, but let's stick to what's used.
            txtTitulo.setOnClickListener {
                onAction(modelo, "ABRIR_LIVRO")
            }
        }
    }
}
