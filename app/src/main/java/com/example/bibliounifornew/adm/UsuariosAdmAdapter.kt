package com.example.bibliounifornew.adm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R

/**
 * Modelo de dados para a lista de usuários na gestão do ADM.
 */
data class ItemUsuarioAdm(
    val uid: String = "",
    val nome: String = "",
    val email: String = "",
    val usuario: String = ""
)

/**
 * Adapter para exibir a lista de usuários na TelaRF29 (Gerenciamento de Usuários).
 */
class UsuariosAdmAdapter(
    private val lista: List<ItemUsuarioAdm>,
    private val onClick: (ItemUsuarioAdm) -> Unit
) : RecyclerView.Adapter<UsuariosAdmAdapter.UsuarioViewHolder>() {

    class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAvatar: ImageView = view.findViewById(R.id.imgUserAvatar)
        val txtNome: TextView = view.findViewById(R.id.txtUserName)
        val btnOptions: ImageView = view.findViewById(R.id.btnUserOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario_gestao, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val item = lista[position]
        holder.txtNome.text = item.nome
        
        // Clique no item todo ou no botão de opções abre os detalhes/gestão
        holder.itemView.setOnClickListener { onClick(item) }
        holder.btnOptions.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = lista.size
}
