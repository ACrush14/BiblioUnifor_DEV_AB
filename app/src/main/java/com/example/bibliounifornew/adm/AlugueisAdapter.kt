package com.example.bibliounifornew.adm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlugueisAdapter(
    private var lista: List<ItemAluguel>,
    private val onVerLivro: (ItemAluguel) -> Unit,
    private val onVerUsuario: (ItemAluguel) -> Unit
) : RecyclerView.Adapter<AlugueisAdapter.AluguelViewHolder>() {

    class AluguelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNomeUsuario: TextView = view.findViewById(R.id.txtNomeUsuarioAluguel)
        val txtStatus: TextView = view.findViewById(R.id.txtStatusAluguel)
        val txtTituloLivro: TextView = view.findViewById(R.id.txtTituloLivroAluguel)
        val txtData: TextView = view.findViewById(R.id.txtDataAluguel)
        val btnVerUsuario: MaterialButton = view.findViewById(R.id.btnVerUsuarioAluguel)
        val btnVerLivro: MaterialButton = view.findViewById(R.id.btnVerLivroAluguel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AluguelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_aluguel, parent, false)
        return AluguelViewHolder(view)
    }

    override fun onBindViewHolder(holder: AluguelViewHolder, position: Int) {
        val item = lista[position]
        
        holder.txtNomeUsuario.text = item.nomeUsuario
        holder.txtStatus.text = item.status
        holder.txtTituloLivro.text = item.tituloLivro
        
        val dataFormatada = if (item.dataMs > 0L) {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(item.dataMs))
        } else {
            "--/--/----"
        }
        holder.txtData.text = "Data: $dataFormatada"

        holder.btnVerUsuario.setOnClickListener { onVerUsuario(item) }
        holder.btnVerLivro.setOnClickListener { onVerLivro(item) }
    }

    override fun getItemCount() = lista.size

    fun atualizarLista(novaLista: List<ItemAluguel>) {
        this.lista = novaLista
        notifyDataSetChanged()
    }
}
