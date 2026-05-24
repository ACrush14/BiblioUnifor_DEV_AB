package com.example.bibliounifornew.adm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R

/**
 * Modelo de dados para as solicitações de mídia.
 */
data class ItemSolicitacaoMidia(
    val docId: String = "",
    val uidUsuario: String = "",
    val idLivro: String = "",
    val tiposSolicit: String = "",
    val status: String = "pendente",
    val nomeUsuario: String = "Usuário",
    val tituloLivro: String = "Título Indisponível",
    val autorLivro: String = "Autor Desconhecido"
)

/**
 * Adapter para gerenciar as solicitações de mídias acessíveis (Audiobook, PDF, Braille).
 */
class SolicitacoesMidiaAdapter(
    private var lista: MutableList<ItemSolicitacaoMidia>,
    private val onVerSolicitacoes: (ItemSolicitacaoMidia) -> Unit,
    private val onEnviarAudiobook: (ItemSolicitacaoMidia) -> Unit,
    private val onEnviarPdf: (ItemSolicitacaoMidia) -> Unit,
    private val onBraille: (ItemSolicitacaoMidia) -> Unit,
    private val onExcluir: (ItemSolicitacaoMidia, Int) -> Unit
) : RecyclerView.Adapter<SolicitacoesMidiaAdapter.SolicitacaoViewHolder>() {

    class SolicitacaoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitulo: TextView = view.findViewById(R.id.txtTituloLivro)
        val txtAutor: TextView = view.findViewById(R.id.txtAutorLivro)
        val txtUsuario: TextView = view.findViewById(R.id.txtNomeUsuario)
        val txtTipos: TextView = view.findViewById(R.id.txtTiposSolicitados)
        
        val btnVer: Button = view.findViewById(R.id.btnVerSolicitacao)
        val btnAudio: Button = view.findViewById(R.id.btnAudiobook)
        val btnPdf: Button = view.findViewById(R.id.btnPdf)
        val btnBraille: Button = view.findViewById(R.id.btnBraille)
        val btnExcluir: Button = view.findViewById(R.id.btnExcluir)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitacaoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_solicitacao_midia, parent, false)
        return SolicitacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: SolicitacaoViewHolder, position: Int) {
        val item = lista[position]
        
        holder.txtTitulo.text = item.tituloLivro
        holder.txtAutor.text = item.autorLivro
        holder.txtUsuario.text = "Solicitado por: ${item.nomeUsuario}"
        holder.txtTipos.text = "Tipos: ${item.tiposSolicit}"

        holder.btnVer.setOnClickListener { onVerSolicitacoes(item) }
        holder.btnAudio.setOnClickListener { onEnviarAudiobook(item) }
        holder.btnPdf.setOnClickListener { onEnviarPdf(item) }
        holder.btnBraille.setOnClickListener { onBraille(item) }
        holder.btnExcluir.setOnClickListener { onExcluir(item, position) }
        
        // Lógica simples para habilitar botões conforme o que foi solicitado
        val solicitados = item.tiposSolicit.lowercase()
        holder.btnAudio.visibility = if (solicitados.contains("audio")) View.VISIBLE else View.GONE
        holder.btnPdf.visibility = if (solicitados.contains("pdf")) View.VISIBLE else View.GONE
        holder.btnBraille.visibility = if (solicitados.contains("braille")) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = lista.size

    fun atualizarLista(novaLista: List<ItemSolicitacaoMidia>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }

    fun removerItem(position: Int) {
        if (position in lista.indices) {
            lista.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, lista.size)
        }
    }
}
