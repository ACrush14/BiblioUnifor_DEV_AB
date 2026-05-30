package com.example.bibliounifornew.features.usuario.amigo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R

/**
 * Adapter para a tela de Busca de Amigos (RF17_3).
 *
 * Avatar carregado via Coil com size(150, 150) + crossfade — evita OOM ao reciclar
 * muitos itens rapidamente (o Coil descarta bitmaps fora da viewport automaticamente).
 * Toda lógica Firestore fica no callback [onEnviarSolicitacao].
 */
class BuscaAmigoAdapter(
    private val lista                : MutableList<UsuarioAmigo>,
    private val onEnviarSolicitacao  : (UsuarioAmigo) -> Unit
) : RecyclerView.Adapter<BuscaAmigoAdapter.BuscaViewHolder>() {

    inner class BuscaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAmigo: ImageView = itemView.findViewById(R.id.imgAmigo)
        val txtNome : TextView  = itemView.findViewById(R.id.txtNomeAmigo)
        val btnMais : ImageView = itemView.findViewById(R.id.btnMaisAmigo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuscaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_amigo_busca, parent, false)
        return BuscaViewHolder(view)
    }

    override fun onBindViewHolder(holder: BuscaViewHolder, position: Int) {
        val usuario = lista[position]

        holder.txtNome.text = usuario.nome.ifEmpty { usuario.usuario }.ifEmpty { "Usuário" }

        // Avatar via Coil: size(150, 150) libera pressão de memória no GC ao
        // reciclar muitos itens. crossfade elimina o flash branco entre loads.
        holder.imgAmigo.load(usuario.fotoUrl.ifEmpty { null }) {
            size(150, 150)
            crossfade(true)
            placeholder(R.drawable.user_placeholder)
            error(R.drawable.user_placeholder)
            fallback(R.drawable.user_placeholder)
        }

        holder.btnMais.setOnClickListener { onEnviarSolicitacao(usuario) }
        holder.itemView.setOnClickListener { onEnviarSolicitacao(usuario) }
    }

    override fun getItemCount(): Int = lista.size

    fun atualizarLista(novaLista: List<UsuarioAmigo>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }

    fun removerUsuario(usuario: UsuarioAmigo) {
        val pos = lista.indexOfFirst { it.uid == usuario.uid }
        if (pos != -1) {
            lista.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }
}
