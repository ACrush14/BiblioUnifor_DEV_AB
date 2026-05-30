package com.example.bibliounifornew.features.usuario.notificacao

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.Notificacao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Adapter para o RecyclerView de notificações (RF20).
 *
 * Funcionalidades:
 * - Carrega capa do livro via Coil a partir de [Notificacao.coverUrl]
 * - Formata o timestamp em texto relativo ("Hoje", "Ontem", "Há X dias")
 * - Persiste no Firestore quando o usuário marca uma notificação como lida
 * - Suporta swipe-to-dismiss via [removerItem] (chamado pelo ItemTouchHelper)
 * - [atualizarLista] recebe a lista fresca do SnapshotListener e notifica a UI
 *
 * Layout de item: R.layout.item_notificacao
 * IDs verificados: imgCapaItemNotif, textTituloItemNotif, textAutorItemNotif,
 * textDescricaoItemNotif, textTempoItemNotif, checkLidaItemNotif
 */
class NotificacaoAdapter(
    private val lista          : MutableList<Notificacao>,
    private val onNotifClick   : ((Notificacao) -> Unit)? = null
) : RecyclerView.Adapter<NotificacaoAdapter.NotificacaoViewHolder>() {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    private val db  = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // ─── VIEW HOLDER ──────────────────────────────────────────────────────────

    inner class NotificacaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCapa      : ImageView = itemView.findViewById(R.id.imgCapaItemNotif)
        val textTitulo   : TextView  = itemView.findViewById(R.id.textTituloItemNotif)
        val textAutor    : TextView  = itemView.findViewById(R.id.textAutorItemNotif)
        val textDescricao: TextView  = itemView.findViewById(R.id.textDescricaoItemNotif)
        val textTempo    : TextView  = itemView.findViewById(R.id.textTempoItemNotif)
        val checkLida    : CheckBox  = itemView.findViewById(R.id.checkLidaItemNotif)
    }

    // ─── ADAPTER CALLBACKS ────────────────────────────────────────────────────

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacaoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacao, parent, false)
        return NotificacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificacaoViewHolder, position: Int) {
        val notif = lista[position]

        // ── Capa via Coil ─────────────────────────────────────────────────────
        // Carrega URL remota; fallback para drawable local se vazia/inválida.
        if (notif.coverUrl.isNotEmpty()) {
            holder.imgCapa.load(notif.coverUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_sem_capa)
                error(R.drawable.ic_sem_capa)
            }
        } else {
            holder.imgCapa.setImageResource(R.drawable.ic_sem_capa)
        }

        // ── Textos ────────────────────────────────────────────────────────────
        // Título dinâmico: constrói "O PDF do livro X está pronto!" quando
        // os campos tipo/tituloLivro estão preenchidos pelo ADM.
        holder.textTitulo.text = when {
            notif.tipo.isNotEmpty() && notif.tituloLivro.isNotEmpty() ->
                "O ${notif.tipo} do livro \"${notif.tituloLivro}\" está pronto!"
            notif.tituloLivro.isNotEmpty() ->
                "\"${notif.tituloLivro}\" está disponível!"
            else -> notif.titulo
        }
        holder.textAutor.text     = notif.autor
        holder.textDescricao.text = notif.descricao
        holder.textTempo.text     = formatarTempo(notif.timestamp)

        // ── Checkbox "Lida" ───────────────────────────────────────────────────
        // Remove listener antigo ANTES de setar o estado para evitar chamadas
        // fantasmas ao reciclar o ViewHolder (bug clássico de RecyclerView).
        holder.checkLida.setOnCheckedChangeListener(null)
        holder.checkLida.isChecked = notif.lida

        holder.checkLida.setOnCheckedChangeListener { _, isChecked ->
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_ID.toInt()) return@setOnCheckedChangeListener

            lista[pos].lida = isChecked
            // Persiste no Firestore — subcoleção do usuário logado
            persistirLida(notif.id, isChecked)
        }

        // ── Clique no card ────────────────────────────────────────────────────
        // Usa adapterPosition para evitar stale closure após reciclagem.
        // FLAG_ACTIVITY_SINGLE_TOP é aplicado na Activity via onNotifClick.
        holder.itemView.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_ID.toInt()) {
                onNotifClick?.invoke(lista[pos])
            }
        }
    }

    override fun getItemCount(): Int = lista.size

    // ─── API PÚBLICA ──────────────────────────────────────────────────────────

    /**
     * Recebe a lista atualizada do SnapshotListener e notifica a UI.
     * Chamado pela Activity a cada vez que o Firestore emite um novo snapshot.
     */
    fun atualizarLista(nova: List<Notificacao>) {
        lista.clear()
        lista.addAll(nova)
        notifyDataSetChanged()
    }

    /**
     * Remove o item na posição indicada com animação de deslizamento.
     * Chamado pelo ItemTouchHelper (swipe-to-dismiss) na Activity.
     */
    fun removerItem(position: Int) {
        if (position < 0 || position >= lista.size) return
        lista.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, lista.size)
    }

    // ─── HELPERS PRIVADOS ────────────────────────────────────────────────────

    /**
     * Persiste o estado "lida" no Firestore.
     * Subcoleção: usuarios/{uid}/notificacoes/{docId}
     */
    private fun persistirLida(docId: String, lida: Boolean) {
        if (uid.isEmpty() || docId.isEmpty()) return
        db.collection("usuarios")
            .document(uid)
            .collection("notificacoes")
            .document(docId)
            .update("lida", lida)
        // Falha silenciosa — não é crítico para UX
    }

    /**
     * Converte epoch ms em texto relativo amigável:
     * 0   → "Hoje"
     * 1   → "Ontem"
     * 2-6 → "Há X dias"
     * 7+  → data formatada (ex: "12/05/2026")
     */
    private fun formatarTempo(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        val agora = System.currentTimeMillis()
        val dias  = TimeUnit.MILLISECONDS.toDays(agora - timestamp)
        return when {
            dias == 0L -> "Hoje"
            dias == 1L -> "Ontem"
            dias < 7L  -> "Há $dias dias"
            else       -> dateFormatter.format(Date(timestamp)) // Uso otimizado aqui
        }
    }

    /**
     * Retorna a notificação na posição específica para deleção no Swipe.
     */
    fun getNotificacaoAt(position: Int): Notificacao {
        return lista[position]
    }
}
