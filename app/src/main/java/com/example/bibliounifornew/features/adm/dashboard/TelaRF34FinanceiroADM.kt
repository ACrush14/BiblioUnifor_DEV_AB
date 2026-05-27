package com.example.bibliounifornew.features.adm.dashboard

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.gerenciamento.NavigationHelperADM
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF37InfoLivroADM
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TelaRF34FinanceiroADM : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private val listaVencidos = mutableListOf<LivroVencidoModel>()
    private lateinit var adapter: VencidosAdapter

    companion object {
        /** R$ 2,00 por dia de atraso — calculado dinamicamente (BUG-A3 FIX) */
        const val MULTA_POR_DIA = 2L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf34_finaceiro_adm)

        val btnVerPendentes  = findViewById<MaterialButton>(R.id.btnPendentesRetirada)
        val txtNenhumVencido = findViewById<TextView>(R.id.txtNenhumVencido)
        recyclerView         = findViewById(R.id.recyclerViewLivrosVencidos)

        recyclerView.layoutManager = LinearLayoutManager(this)
        // BUG-A1 FIX: adapter usa item_livro_vencido.xml (Design System Premium)
        adapter = VencidosAdapter(listaVencidos) { modelo, acao ->
            when (acao) {
                "ABRIR_LIVRO"  -> {
                    val intent = Intent(this, TelaRF37InfoLivroADM::class.java)
                    intent.putExtra("LIVRO_ID", modelo.idLivro)
                    startActivity(intent)
                }
                "RENOVAR"      -> renovarAluguel(modelo.docIdAtual)
                "VER_DETALHES" -> exibirPopupPendentes(modelo)
            }
        }
        recyclerView.adapter = adapter

        carregarVencidos(txtNenhumVencido)

        btnVerPendentes?.setOnClickListener {
            if (listaVencidos.isNotEmpty()) {
                exibirPopupPendentes(listaVencidos[0])
            } else {
                Toast.makeText(this, getString(R.string.msg_lista_vencidos_vazia), Toast.LENGTH_SHORT).show()
            }
        }

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    // ─── CARREGAR VENCIDOS ────────────────────────────────────────────────────

    private fun carregarVencidos(txtNenhumVencido: TextView) {
        val agora = System.currentTimeMillis()

        db.collection("solicitacoes_emprestimo")
            .whereLessThan("dataDevolucao", agora)
            .whereEqualTo("status", "ativo")
            .limit(50)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    txtNenhumVencido.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                txtNenhumVencido.visibility = View.GONE
                val docs = result.documents

                // BUG-A2 FIX: contador atômico — notifyDataSetChanged() disparado UMA VEZ
                val totalJoins = docs.size * 2
                var processados = 0

                fun verificarConcluido() {
                    processados++
                    if (processados >= totalJoins) {
                        adapter.notifyDataSetChanged()
                    }
                }

                for (doc in docs) {
                    val docId    = doc.id
                    val uidAluno = doc.getString("uidAluno") ?: ""
                    val idLivro  = doc.getString("idLivro")  ?: ""
                    val dataDev  = doc.getLong("dataDevolucao") ?: agora

                    // BUG-A3 FIX: cálculo dinâmico (R$ 2,00/dia) — não lê campo "multa" do Firestore
                    val diasVencidos   = ((agora - dataDev) / (1_000L * 60 * 60 * 24)).coerceAtLeast(0)
                    val multaCalculada = diasVencidos * MULTA_POR_DIA

                    val modelo = LivroVencidoModel(
                        docIdAtual      = docId,
                        uidAlunoAtual   = uidAluno,
                        idLivro         = idLivro,
                        dataDevolucaoMs = dataDev,
                        diasVencidos    = diasVencidos,
                        multa           = multaCalculada
                    )
                    listaVencidos.add(modelo)

                    // Join: nome do usuário
                    if (uidAluno.isNotEmpty()) {
                        db.collection("usuarios").document(uidAluno).get()
                            .addOnSuccessListener { u ->
                                modelo.nomeUsuario = u.getString("nome") ?: getString(R.string.usuario_desconhecido)
                                verificarConcluido()
                            }
                            .addOnFailureListener { verificarConcluido() }
                    } else verificarConcluido()

                    // Join: título do livro
                    if (idLivro.isNotEmpty()) {
                        db.collection("livros").document(idLivro).get()
                            .addOnSuccessListener { l ->
                                modelo.tituloLivro = l.getString("title") ?: l.getString("titulo")
                                    ?: getString(R.string.sem_titulo)
                                verificarConcluido()
                            }
                            .addOnFailureListener { verificarConcluido() }
                    } else verificarConcluido()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.erro_carregar_vencidos), Toast.LENGTH_SHORT).show()
                txtNenhumVencido.visibility = View.VISIBLE
            }
    }

    // ─── RENOVAR ALUGUEL ──────────────────────────────────────────────────────

    private fun renovarAluguel(docId: String) {
        val novaDevolucao = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1_000)

        db.collection("solicitacoes_emprestimo").document(docId)
            .set(
                mapOf("dataDevolucao" to novaDevolucao, "status" to "ativo", "multa" to 0L),
                SetOptions.merge()
            )
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.msg_aluguel_renovado), Toast.LENGTH_SHORT).show()
                listaVencidos.removeAll { it.docIdAtual == docId }
                adapter.notifyDataSetChanged()
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

        // BUG-A4 FIX: popula textInfoPendencia com dados reais do modelo
        val sdf            = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        val prazoFormatado = sdf.format(Date(modelo.dataDevolucaoMs))
        val multaFormatada = String.format("%.2f", modelo.multa.toDouble()).replace('.', ',')
        dialog.findViewById<TextView>(R.id.textInfoPendencia)?.text =
            getString(
                R.string.fmt_info_pendencia,
                modelo.tituloLivro,
                modelo.nomeUsuario,
                prazoFormatado,
                modelo.diasVencidos,
                multaFormatada
            )

        val btnNotificarAtraso  = dialog.findViewById<MaterialButton>(R.id.btnNotificarAtraso)
        val btnNotificarValor   = dialog.findViewById<MaterialButton>(R.id.btnNotificarValor)
        val btnConfirmarAluguel = dialog.findViewById<MaterialButton>(R.id.btnConfirmacaoAluguel)
        val btnRemoverRegistro  = dialog.findViewById<MaterialButton>(R.id.btnRemoverRegistro)

        // BUG-A5 FIX: mensagens via getString() — zero hardcode
        btnNotificarAtraso?.setOnClickListener {
            val notif = hashMapOf(
                "uidAluno"   to modelo.uidAlunoAtual,
                "docAluguel" to modelo.docIdAtual,
                "tipo"       to "atraso",
                "mensagem"   to getString(R.string.notif_atraso_mensagem),
                "criadoEm"   to System.currentTimeMillis()
            )
            db.collection("notificacoes").add(notif)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.msg_aviso_atraso_enviado), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.erro_conexao_banco), Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }

        btnNotificarValor?.setOnClickListener {
            val notif = hashMapOf(
                "uidAluno"   to modelo.uidAlunoAtual,
                "docAluguel" to modelo.docIdAtual,
                "tipo"       to "multa",
                "mensagem"   to getString(R.string.notif_multa_mensagem),
                "criadoEm"   to System.currentTimeMillis()
            )
            db.collection("notificacoes").add(notif)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.msg_multa_notificada), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.erro_conexao_banco), Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }

        btnConfirmarAluguel?.setOnClickListener {
            db.collection("solicitacoes_emprestimo").document(modelo.docIdAtual)
                .set(mapOf("status" to "confirmado"), SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.msg_aluguel_confirmado), Toast.LENGTH_SHORT).show()
                    listaVencidos.remove(modelo)
                    adapter.notifyDataSetChanged()
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
                    Toast.makeText(this, getString(R.string.msg_registro_removido), Toast.LENGTH_SHORT).show()
                    listaVencidos.remove(modelo)
                    adapter.notifyDataSetChanged()
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
}

// ─────────────────────────────────────────────────────────────────────────────
// MODELO
// ─────────────────────────────────────────────────────────────────────────────

data class LivroVencidoModel(
    val docIdAtual       : String,
    val uidAlunoAtual    : String,
    val idLivro          : String,
    val dataDevolucaoMs  : Long,
    val diasVencidos     : Long,
    /** Calculado dinamicamente — R$ 2,00/dia (nunca lido do Firestore) */
    val multa            : Long,
    var nomeUsuario      : String = "Carregando…",
    var tituloLivro      : String = "Carregando…"
)

// ─────────────────────────────────────────────────────────────────────────────
// ADAPTER — BUG-A1 FIX: item_livro_vencido.xml com Design System Premium
// ─────────────────────────────────────────────────────────────────────────────

class VencidosAdapter(
    private val lista   : List<LivroVencidoModel>,
    private val onClick : (LivroVencidoModel, String) -> Unit
) : RecyclerView.Adapter<VencidosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitulo  : TextView      = view.findViewById(R.id.txtTituloVencido)
        val txtUsuario : TextView      = view.findViewById(R.id.txtUsuarioVencido)
        val txtDias    : TextView      = view.findViewById(R.id.txtDiasAtrasoVencido)
        val txtMulta   : TextView      = view.findViewById(R.id.txtMultaVencido)
        val btnRenovar : MaterialButton = view.findViewById(R.id.btnRenovarVencido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_livro_vencido, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        val ctx  = holder.itemView.context

        holder.txtTitulo.text  = item.tituloLivro
        holder.txtUsuario.text = ctx.getString(R.string.fmt_usuario_vencido, item.nomeUsuario)
        holder.txtDias.text    = ctx.getString(R.string.fmt_item_atraso, item.diasVencidos)
        holder.txtMulta.text   = ctx.getString(
            R.string.fmt_item_multa,
            String.format("%.2f", item.multa.toDouble()).replace('.', ',')
        )

        holder.itemView.setOnClickListener { onClick(item, "VER_DETALHES") }
        holder.btnRenovar.setOnClickListener { onClick(item, "RENOVAR") }
    }

    override fun getItemCount() = lista.size
}
