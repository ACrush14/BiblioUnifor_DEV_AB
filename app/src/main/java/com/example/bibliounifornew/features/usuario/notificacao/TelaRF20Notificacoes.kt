package com.example.bibliounifornew.features.usuario.notificacao

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.example.bibliounifornew.features.usuario.livro.TelaLivroActivity
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * TelaRF20Notificacoes — Lista de notificações do usuário em tempo real.
 *
 * Fonte de dados: subcoleção Firestore usuarios/{uid}/notificacoes
 * Listener: SnapshotListener (atualiza a UI automaticamente quando o ADM
 * cria ou altera notificações, sem necessidade de recarregar a tela).
 *
 * Ciclo de vida do listener:
 * onCreate  → registra o SnapshotListener
 * onDestroy → cancela o SnapshotListener (evita memory leak)
 */
class TelaRF20Notificacoes : AppCompatActivity() {

    private val authRepository    = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private lateinit var adapter  : NotificacaoAdapter

    // Ambos os listeners são cancelados em onDestroy
    private var listenerPerfil      : ListenerRegistration? = null
    private var listenerNotificacoes: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf20_notificacoes)

        val usuarioAtual = authRepository.getUsuarioAtual()
        if (usuarioAtual == null) {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        // ─── CABEÇALHO ────────────────────────────────────────────────────────
        configurarCabecalho(usuarioAtual.uid)

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        configurarRecyclerView(usuarioAtual.uid)

        // ─── BARRA DE NAVEGAÇÃO ───────────────────────────────────────────────
        NavigationHelper.configurarBarraNavegacao(this)
    }

    // ─── CABEÇALHO ────────────────────────────────────────────────────────────

    /**
     * Atualiza nome e foto do usuário em tempo real via SnapshotListener.
     * IDs do XML: textNomeNotif, imagePerfilNotif
     */
    private fun configurarCabecalho(uid: String) {
        val textNome    = findViewById<TextView>(R.id.textNomeNotif)
        val imagePerfil = findViewById<ImageView>(R.id.imagePerfilNotif)

        textNome?.text = "Carregando..."

        listenerPerfil = usuarioRepository.observarPerfilUsuario(uid) { dados ->
            if (dados != null) {
                textNome?.text = dados["nome"] as? String ?: "Usuário"
                val fotoUrl = dados["fotoUrl"] as? String ?: ""
                if (fotoUrl.isNotEmpty()) {
                    imagePerfil?.load(fotoUrl) {
                        placeholder(R.drawable.user_placeholder)
                        error(R.drawable.user_placeholder)
                    }
                }
            }
        }
    }

    // ─── RECYCLERVIEW + SNAPSHOT LISTENER ────────────────────────────────────

    /**
     * Configura o RecyclerView com o [NotificacaoAdapter] e o swipe-to-dismiss.
     */
    private fun configurarRecyclerView(uid: String) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewNotificacoes)
        val tvVazia      = findViewById<TextView>(R.id.tvNenhumaNotificacao)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = NotificacaoAdapter(
            lista        = mutableListOf(),
            onNotifClick = { notif ->
                // Se a notificação tem livroId → navega direto para o livro
                if (notif.livroId.isNotEmpty()) {
                    startActivity(
                        Intent(this, TelaLivroActivity::class.java)
                            .putExtra("LIVRO_ID", notif.livroId)
                    )
                }
            }
        )
        recyclerView.adapter = adapter

        // ── SnapshotListener — atualiza a lista e o empty state em tempo real ─
        listenerNotificacoes = usuarioRepository.escutarNotificacoes(uid) { lista ->
            if (!isFinishing && !isDestroyed) {
                adapter.atualizarLista(lista)
                val vazio = lista.isEmpty()
                tvVazia?.visibility      = if (vazio) View.VISIBLE else View.GONE
                recyclerView?.visibility = if (vazio) View.GONE   else View.VISIBLE
            }
        }

        // ── Swipe-to-dismiss ──────────────────────────────────────────────────
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                rv         : RecyclerView,
                viewHolder : RecyclerView.ViewHolder,
                target     : RecyclerView.ViewHolder
            ): Boolean = false // sem drag-and-drop

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_ID.toInt()) {
                    val notif = adapter.getNotificacaoAt(position)

                    // Exclui do Firestore primeiro
                    FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(uid)
                        .collection("notificacoes")
                        .document(notif.id)
                        .delete()
                        .addOnSuccessListener {
                            adapter.removerItem(position)
                            Toast.makeText(this@TelaRF20Notificacoes, "Notificação descartada", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
    }

    // ─── CICLO DE VIDA ────────────────────────────────────────────────────────

    override fun onDestroy() {
        super.onDestroy()
        // Cancela AMBOS os listeners para evitar memory leak
        listenerPerfil?.remove()
        listenerNotificacoes?.remove()
    }
}
