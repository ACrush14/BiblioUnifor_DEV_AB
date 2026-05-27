package com.example.bibliounifornew.features.adm.solicitacoes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

data class ItemSolicitacaoMidia(
    val docId           : String = "",
    val uidUsuario      : String = "",
    val idLivro         : String = "",
    val nomeUsuario     : String = "Usuário",
    val tituloLivro     : String = "Título Indisponível",
    val autorLivro      : String = "Autor Desconhecido",
    val coverUrl        : String = "",     // URL da capa — preenchida no join com livros/
    val tiposSolicit    : String = "",     // "audiobook,pdf,braille" (separado por vírgula)
    val status          : String = "pendente",
    val dataSolicitacao : Long   = 0L     // timestamp epoch ms da solicitação
)

class SolicitacoesMidiaAdapter(
    private val lista           : MutableList<ItemSolicitacaoMidia>,
    private val onVerSolicitacoes: (ItemSolicitacaoMidia) -> Unit,
    private val onEnviarAudiobook: (ItemSolicitacaoMidia) -> Unit,
    private val onEnviarPdf      : (ItemSolicitacaoMidia) -> Unit,
    private val onBraille        : (ItemSolicitacaoMidia) -> Unit,
    private val onExcluir        : (ItemSolicitacaoMidia, Int) -> Unit
) : RecyclerView.Adapter<SolicitacoesMidiaAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitulo   : TextView      = view.findViewById(R.id.txtTituloSolicitacao)
        val txtAutor    : TextView      = view.findViewById(R.id.txtAutorSolicitacao)
        val txtUsuario  : TextView      = view.findViewById(R.id.txtUsuarioSolicitacao)
        val txtTipos    : TextView      = view.findViewById(R.id.txtTiposSolicitacao)
        val btnVer      : MaterialButton = view.findViewById(R.id.btnVerSolicitacoesAdm)
        val btnAudio    : MaterialButton = view.findViewById(R.id.btnEnviarAudiobookAdm)
        val btnPdf      : MaterialButton = view.findViewById(R.id.btnEnviarPdfAdm)
        val btnBraille  : MaterialButton = view.findViewById(R.id.btnBrailleAdm)
        val btnExcluir  : MaterialButton = view.findViewById(R.id.btnExcluirSolicitacaoAdm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_solicitacao_midia_adm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        holder.txtTitulo.text  = item.tituloLivro
        holder.txtAutor.text   = item.autorLivro
        holder.txtUsuario.text = item.nomeUsuario
        holder.txtTipos.text   = if (item.tiposSolicit.isNotBlank())
            "Solicitação: ${item.tiposSolicit}" else "Solicitação: —"

        holder.btnVer.setOnClickListener      { onVerSolicitacoes(item) }
        holder.btnAudio.setOnClickListener    { onEnviarAudiobook(item) }
        holder.btnPdf.setOnClickListener      { onEnviarPdf(item) }
        holder.btnBraille.setOnClickListener  { onBraille(item) }
        holder.btnExcluir.setOnClickListener  { onExcluir(item, position) }
    }

    override fun getItemCount(): Int = lista.size

    fun removerItem(position: Int) {
        lista.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, lista.size)
    }

    fun atualizarLista(novaLista: List<ItemSolicitacaoMidia>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }
}
