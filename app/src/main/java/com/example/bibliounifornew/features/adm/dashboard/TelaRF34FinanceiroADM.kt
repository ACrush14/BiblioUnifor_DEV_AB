package com.example.bibliounifornew.features.adm.dashboard

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.gerenciamento.NavigationHelperADM
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF37InfoLivroADM
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TelaRF34FinanceiroADM : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var recyclerView    : RecyclerView
    private lateinit var tvNenhumVencido : TextView
    private lateinit var adapter         : VencidosAdapter

    // Cópia imutável da lista atual — usada pelo btnVerPendentes
    private var listaAtual: List<LivroVencidoModel> = emptyList()

    companion object {
        /** Prazo padrão de devolução: 15 dias em milissegundos */
        const val PRAZO_MS      = 15L * 24L * 60L * 60L * 1_000L
        /** R$ 1,00 por dia de atraso (regra de negócio) */
        const val MULTA_POR_DIA = 1.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf34_finaceiro_adm)

        tvNenhumVencido = findViewById(R.id.txtNenhumVencido)
        recyclerView    = findViewById(R.id.recyclerViewLivrosVencidos)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = VencidosAdapter { modelo, acao ->
            when (acao) {
                "ABRIR_LIVRO"  -> startActivity(
                    Intent(this, TelaRF37InfoLivroADM::class.java)
                        .putExtra("LIVRO_ID", modelo.idLivro)
                )
                "RENOVAR"      -> renovarAluguel(modelo)
                "VER_DETALHES" -> exibirPopupPendentes(modelo)
            }
        }
        recyclerView.adapter = adapter

        carregarVencidos()

        findViewById<MaterialButton>(R.id.btnPendentesRetirada)?.setOnClickListener {
            val primeiro = listaAtual.firstOrNull()
            if (primeiro != null) exibirPopupPendentes(primeiro)
            else Toast.makeText(this, getString(R.string.msg_lista_vencidos_vazia), Toast.LENGTH_SHORT).show()
        }

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    // ─── CARREGAR VENCIDOS ────────────────────────────────────────────────────

    /**
     * PERF-2: Elimina N+1 — campos [nomeAluno], [tituloLivro] e [autorLivro]
     * são lidos diretamente do documento de aluguel (desnormalizados).
     *
     * Regras de negócio aplicadas em Dispatchers.IO:
     *   prazo      = doc["dataDevolucao"] ?: (dataSolicitacao + 15 dias)
     *   diasAtraso = max(0, (agora − prazo) / 1 dia)
     *   valorMulta = diasAtraso × R$ 1,00
     *
     * Query sem whereCompound: status ∈ {"ativo","atrasado"}
     * Nenhum índice composto necessário — filtro client-side no IO thread.
     */
    private fun carregarVencidos() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection("solicitacoes_emprestimo")
                    .whereIn("status", listOf("ativo", "atrasado"))
                    .get()
                    .await()

                val agora = System.currentTimeMillis()
                val lista = result.documents
                    .mapNotNull { doc -> mapearDocumento(doc, agora) }
                    .sortedByDescending { it.diasAtraso }

                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    listaAtual = lista
                    val vazio = lista.isEmpty()
                    tvNenhumVencido.visibility = if (vazio) View.VISIBLE else View.GONE
                    recyclerView.visibility    = if (vazio) View.GONE   else View.VISIBLE
                    adapter.atualizarLista(lista)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    Toast.makeText(
                        this@TelaRF34FinanceiroADM,
                        getString(R.string.erro_carregar_vencidos),
                        Toast.LENGTH_SHORT
                    ).show()
                    tvNenhumVencido.visibility = View.VISIBLE
                    recyclerView.visibility    = View.GONE
                }
            }
        }
    }

    /**
     * Mapeia um [DocumentSnapshot] para [LivroVencidoModel] sem queries adicionais.
     * Retorna null se [uidAluno], [idLivro] ou [dataSolicitacao] estiverem ausentes.
     *
     * Chamado dentro de Dispatchers.IO — seguro usar getString() pois o Context
     * do Application é thread-safe.
     */
    private fun mapearDocumento(doc: DocumentSnapshot, agora: Long): LivroVencidoModel? {
        val uidAluno        = doc.getString("uidAluno")      ?: return null
        val idLivro         = doc.getString("idLivro")       ?: return null
        val dataSolicitacao = doc.getLong("dataSolicitacao") ?: return null
        val status          = doc.getString("status")        ?: "ativo"

        // Usa dataDevolucao explícita quando disponível (gravada pela renovação),
        // caso contrário calcula os 15 dias a partir da data de solicitação.
        val dataLimiteMs = doc.getLong("dataDevolucao") ?: (dataSolicitacao + PRAZO_MS)

        val diasAtraso = if (agora > dataLimiteMs) {
            ((agora - dataLimiteMs) / (24L * 60L * 60L * 1_000L)).toInt()
        } else 0

        val valorMulta = diasAtraso * MULTA_POR_DIA   // R$ 1,00 / dia

        // Leitura desnormalizada — sem queries extras
        val nomeAluno   = doc.getString("nomeAluno")
            ?: doc.getString("usuarioNome")
            ?: getString(R.string.usuario_desconhecido)
        val tituloLivro = doc.getString("tituloLivro")
            ?: doc.getString("titulo")
            ?: getString(R.string.sem_titulo)
        val autorLivro  = doc.getString("autorLivro")
            ?: doc.getString("autor")
            ?: getString(R.string.sem_autor)

        return LivroVencidoModel(
            docIdAtual        = doc.id,
            uidAlunoAtual     = uidAluno,
            idLivro           = idLivro,
            dataSolicitacaoMs = dataSolicitacao,
            dataLimiteMs      = dataLimiteMs,
            diasAtraso        = diasAtraso,
            valorMulta        = valorMulta,
            nomeAluno         = nomeAluno,
            tituloLivro       = tituloLivro,
            autorLivro        = autorLivro,
            status            = status
        )
    }

    // ─── RENOVAR ALUGUEL ──────────────────────────────────────────────────────

    /**
     * Estende o prazo em 15 dias a partir de agora.
     *
     * Grava [dataDevolucao] explicitamente — garante que [mapearDocumento] use
     * o campo renovado na próxima carga (e não recalcule a partir de dataSolicitacao).
     * Multa zerada implicitamente: próximo cálculo partirá do novo prazo.
     */
    private fun renovarAluguel(modelo: LivroVencidoModel) {
        val novaDataLimite = System.currentTimeMillis() + PRAZO_MS

        db.collection("solicitacoes_emprestimo").document(modelo.docIdAtual)
            .update(mapOf(
                "dataDevolucao" to novaDataLimite,
                "status"        to "ativo"
            ))
            .addOnSuccessListener {
                listaAtual = listaAtual.filter { it.docIdAtual != modelo.docIdAtual }
                adapter.removerPorDocId(modelo.docIdAtual)
                if (listaAtual.isEmpty()) {
                    tvNenhumVencido.visibility = View.VISIBLE
                    recyclerView.visibility    = View.GONE
                }
                Toast.makeText(this, getString(R.string.msg_aluguel_renovado), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.erro_renovar_aluguel), Toast.LENGTH_SHORT).show()
            }
    }

    // ─── POPUP PENDENTES ─────────────────────────────────────────────────────

    private fun exibirPopupPendentes(modelo: LivroVencidoModel) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_pendentes_retirada)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val sdf            = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        val prazoFormatado = sdf.format(Date(modelo.dataLimiteMs))
        val multaFormatada = String.format("%.2f", modelo.valorMulta).replace('.', ',')

        dialog.findViewById<TextView>(R.id.textInfoPendencia)?.text = getString(
            R.string.fmt_info_pendencia,
            modelo.tituloLivro,
            modelo.nomeAluno,
            prazoFormatado,
            modelo.diasAtraso,
            multaFormatada
        )

        val btnNotificarAtraso  = dialog.findViewById<MaterialButton>(R.id.btnNotificarAtraso)
        val btnNotificarValor   = dialog.findViewById<MaterialButton>(R.id.btnNotificarValor)
        val btnConfirmarAluguel = dialog.findViewById<MaterialButton>(R.id.btnConfirmacaoAluguel)
        val btnRemoverRegistro  = dialog.findViewById<MaterialButton>(R.id.btnRemoverRegistro)

        // GAP-3 FIX: notificações gravadas na subcoleção do aluno
        // (usuarios/{uid}/notificacoes) — lida corretamente por TelaRF20Notificacoes.
        // O código anterior gravava na coleção global "notificacoes" e as notificações
        // nunca chegavam ao aluno.
        btnNotificarAtraso?.setOnClickListener {
            gravarNotificacaoAluno(
                uid        = modelo.uidAlunoAtual,
                docAluguel = modelo.docIdAtual,
                tipo       = "atraso",
                titulo     = getString(R.string.notif_atraso_titulo),
                mensagem   = getString(R.string.notif_atraso_mensagem)
            )
            dialog.dismiss()
        }

        btnNotificarValor?.setOnClickListener {
            gravarNotificacaoAluno(
                uid        = modelo.uidAlunoAtual,
                docAluguel = modelo.docIdAtual,
                tipo       = "multa",
                titulo     = getString(R.string.notif_multa_titulo),
                mensagem   = getString(R.string.notif_multa_mensagem)
            )
            dialog.dismiss()
        }

        btnConfirmarAluguel?.setOnClickListener {
            db.collection("solicitacoes_emprestimo").document(modelo.docIdAtual)
                .update(mapOf("status" to "devolvido"))
                .addOnSuccessListener {
                    removerDaLista(modelo.docIdAtual)
                    Toast.makeText(this, getString(R.string.msg_aluguel_confirmado), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.erro_conexao_banco), Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }

        btnRemoverRegistro?.setOnClickListener {
            db.collection("solicitacoes_emprestimo").document(modelo.docIdAtual)
                .delete()
                .addOnSuccessListener {
                    removerDaLista(modelo.docIdAtual)
                    Toast.makeText(this, getString(R.string.msg_registro_removido), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.erro_conexao_banco), Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    // ─── HELPERS PRIVADOS ─────────────────────────────────────────────────────

    /**
     * Remove o item da lista local e do adapter; exibe empty state se necessário.
     * Centraliza a lógica usada por confirmar e remover no popup.
     */
    private fun removerDaLista(docId: String) {
        listaAtual = listaAtual.filter { it.docIdAtual != docId }
        adapter.removerPorDocId(docId)
        if (listaAtual.isEmpty()) {
            tvNenhumVencido.visibility = View.VISIBLE
            recyclerView.visibility    = View.GONE
        }
    }

    /**
     * GAP-3: Grava notificação na subcoleção correta do aluno.
     *
     * Caminho: usuarios/{uidAluno}/notificacoes
     * Lida por: TelaRF20Notificacoes via UsuarioRepository.escutarNotificacoes()
     *
     * Campos compatíveis com Notificacao.fromFirestore():
     *   "titulo"    → exibido como título do card
     *   "mensagem"  → mapeado para descricao (campo visível ao aluno)
     *   "lida"      → false (padrão para nova notificação)
     *   "criadoEm"  → resolvido pelo fromFirestore como timestamp
     */
    private fun gravarNotificacaoAluno(
        uid        : String,
        docAluguel : String,
        tipo       : String,
        titulo     : String,
        mensagem   : String
    ) {
        val notif = hashMapOf(
            "titulo"     to titulo,
            "mensagem"   to mensagem,
            "tipo"       to tipo,
            "lida"       to false,
            "docAluguel" to docAluguel,
            "criadoEm"   to System.currentTimeMillis()
        )
        db.collection("usuarios").document(uid)
            .collection("notificacoes")
            .add(notif)
            .addOnSuccessListener {
                val msgRes = if (tipo == "atraso") R.string.msg_aviso_atraso_enviado
                             else R.string.msg_multa_notificada
                Toast.makeText(this, getString(msgRes), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.erro_conexao_banco), Toast.LENGTH_SHORT).show()
            }
    }
}
