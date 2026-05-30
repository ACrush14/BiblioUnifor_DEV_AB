package com.example.bibliounifornew.features.usuario.solicitacao

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ItemAluguel(
    val docId           : String = "",
    val livroId         : String = "",
    val titulo          : String = "",
    val autorLivro      : String = "",
    val coverUrl        : String = "",
    val status          : String = "pendente",
    val dataDevolucao   : Long   = 0L,
    val dataSolicitacao : Long   = 0L,
    val renovacoes      : Int    = 0
)

class StatusAluguelAdapter(
    private val lista       : MutableList<ItemAluguel>,
    private val onRenovar   : (ItemAluguel) -> Unit
) : RecyclerView.Adapter<StatusAluguelAdapter.AluguelViewHolder>() {

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    inner class AluguelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCapa         : ImageView      = itemView.findViewById(R.id.imgCapaAluguel)
        val txtTitulo       : TextView       = itemView.findViewById(R.id.txtTituloAluguel)
        val txtAutor        : TextView       = itemView.findViewById(R.id.txtAutorAluguel)
        val txtData         : TextView       = itemView.findViewById(R.id.txtDataAluguel)
        val txtStatus       : TextView       = itemView.findViewById(R.id.txtStatusAluguel)
        val txtDiasRestantes: TextView       = itemView.findViewById(R.id.txtDiasRestantes)
        val btnRenovar      : MaterialButton = itemView.findViewById(R.id.btnRenovarAluguel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AluguelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_status_aluguel, parent, false)
        return AluguelViewHolder(view)
    }

    override fun onBindViewHolder(holder: AluguelViewHolder, position: Int) {
        val item  = lista[position]
        val agora = System.currentTimeMillis()

        holder.txtTitulo.text = item.titulo.ifEmpty { "Título indisponível" }
        holder.txtAutor.text  = item.autorLivro

        if (item.coverUrl.isNotEmpty()) {
            holder.imgCapa.load(item.coverUrl) {
                placeholder(R.drawable.ic_sem_capa)
                error(R.drawable.ic_sem_capa)
            }
        } else {
            holder.imgCapa.setImageResource(R.drawable.ic_sem_capa)
        }

        // ── Data de devolução ─────────────────────────────────────────────────
        holder.txtData.text = if (item.dataDevolucao > 0L)
            sdf.format(Date(item.dataDevolucao))
        else
            "—"

        // ── Status e botão ────────────────────────────────────────────────────
        when {
            item.status == "pendente" -> {
                holder.txtStatus.text      = "Aguardando aprovação"
                holder.txtStatus.setTextColor(android.graphics.Color.parseColor("#E65100"))
                holder.btnRenovar.isEnabled = false
                holder.btnRenovar.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#BDBDBD"))
            }
            item.status == "concluido" || item.status == "devolvido" -> {
                holder.txtStatus.text      = "Devolvido"
                holder.txtStatus.setTextColor(android.graphics.Color.parseColor("#616161"))
                holder.btnRenovar.isEnabled = false
                holder.btnRenovar.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#BDBDBD"))
            }
            item.status == "ativo" && item.dataDevolucao > agora -> {
                holder.txtStatus.text      = "Disponível para renovação"
                holder.txtStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
                holder.btnRenovar.isEnabled = true
                holder.btnRenovar.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(
                        holder.itemView.context.getColor(com.example.bibliounifornew.R.color.biblio_blue)
                    )
            }
            item.status == "ativo" && item.dataDevolucao <= agora -> {
                holder.txtStatus.text      = "Prazo expirado"
                holder.txtStatus.setTextColor(android.graphics.Color.parseColor("#C62828"))
                holder.btnRenovar.isEnabled = false
                holder.btnRenovar.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#BDBDBD"))
            }
            else -> {
                holder.txtStatus.text = item.status
                holder.txtStatus.setTextColor(android.graphics.Color.parseColor("#616161"))
                holder.btnRenovar.isEnabled = false
                holder.btnRenovar.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#BDBDBD"))
            }
        }

        // ── Dias restantes / alertas (RF18.3 / RF18.5) ───────────────────────────
        // Calcula e exibe apenas para aluguéis ativos com data de devolução definida.
        if (item.dataDevolucao > 0L && item.status == "ativo") {
            val diffMs = item.dataDevolucao - agora
            val dias   = (diffMs / (1000L * 60 * 60 * 24)).toInt()
            when {
                diffMs < 0L -> {
                    val diasAtraso = ((-diffMs) / (1000L * 60 * 60 * 24)).toInt()
                    holder.txtDiasRestantes.text = "Atrasado há $diasAtraso dias"
                    holder.txtDiasRestantes.setTextColor(android.graphics.Color.parseColor("#C62828"))
                    holder.txtStatus.text = "Atrasado"
                    holder.txtStatus.setTextColor(android.graphics.Color.parseColor("#C62828"))
                }
                dias <= 3 -> {
                    holder.txtDiasRestantes.text = "Vencimento próximo: faltam $dias dias!"
                    holder.txtDiasRestantes.setTextColor(android.graphics.Color.parseColor("#E65100"))
                }
                else -> {
                    holder.txtDiasRestantes.text = "Faltam $dias dias"
                    holder.txtDiasRestantes.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
                }
            }
            holder.txtDiasRestantes.visibility = View.VISIBLE
        } else {
            holder.txtDiasRestantes.visibility = View.GONE
        }

        holder.btnRenovar.setOnClickListener { if (holder.btnRenovar.isEnabled) onRenovar(item) }

        // Clique no card → detalhes do livro
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, TelaRF12TelaDoLivro::class.java)
                .putExtra("LIVRO_ID", item.livroId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun atualizarLista(nova: List<ItemAluguel>) {
        lista.clear()
        lista.addAll(nova)
        notifyDataSetChanged()
    }
}
