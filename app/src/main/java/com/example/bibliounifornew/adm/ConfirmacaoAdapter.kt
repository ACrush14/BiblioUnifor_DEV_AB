package com.example.bibliounifornew.adm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R

/**
 * Modelo de dados para usuários com cadastro pendente.
 */
data class ItemUsuarioPendente(
    val uid: String = "",
    val nome: String = "",
    val email: String = ""
)

/**
 * Adapter para a lista de confirmação de cadastro de novos usuários.
 */
class ConfirmacaoAdapter(
    private var lista: MutableList<ItemUsuarioPendente>,
    private val onConfirmar: (ItemUsuarioPendente, Int) -> Unit
) : RecyclerView.Adapter<ConfirmacaoAdapter.ConfirmacaoViewHolder>() {

    class ConfirmacaoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome: TextView = view.findViewById(R.id.txtNomeUsuario)
        val txtEmail: TextView = view.findViewById(R.id.txtEmailUsuario)
        val btnConfirmar: Button = view.findViewById(R.id.btnConfirmar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfirmacaoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_confirmacao_usuario, parent, false)
        return ConfirmacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConfirmacaoViewHolder, position: Int) {
        val item = lista[position]
        holder.txtNome.text = item.nome
        holder.txtEmail.text = item.email
        holder.btnConfirmar.setOnClickListener { onConfirmar(item, position) }
    }

    override fun getItemCount() = lista.size

    fun removerItem(position: Int) {
        if (position in lista.indices) {
            lista.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, lista.size)
        }
    }
}
