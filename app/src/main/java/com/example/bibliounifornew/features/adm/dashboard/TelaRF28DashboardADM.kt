package com.example.bibliounifornew.features.adm.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.gerenciamento.NavigationHelperADM
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF27CrudAdm
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF35ConfirmarCadastroADM
import com.example.bibliounifornew.features.adm.gerenciamento.TelaRF38ConfigADM
import com.example.bibliounifornew.features.adm.solicitacoes.TelaRF31Solicitacoes
import com.example.bibliounifornew.features.adm.solicitacoes.TelaRF36ListaAlugueisADM
import coil.load
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TelaRF28DashboardADM : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf28_inicial_adm)

        // ─── NAVEGAÇÃO ────────────────────────────────────────────────────────
        findViewById<MaterialButton>(R.id.buttonCrudAdm)?.setOnClickListener {
            startActivity(Intent(this, TelaRF27CrudAdm::class.java))
        }
        findViewById<MaterialButton>(R.id.buttonVerAlugueis)?.setOnClickListener {
            startActivity(Intent(this, TelaRF36ListaAlugueisADM::class.java))
        }
        findViewById<MaterialButton>(R.id.buttonVerAtrasos)?.setOnClickListener {
            startActivity(Intent(this, TelaRF34FinanceiroADM::class.java))
        }
        findViewById<MaterialButton>(R.id.buttonVerCadastros)?.setOnClickListener {
            startActivity(Intent(this, TelaRF35ConfirmarCadastroADM::class.java))
        }
        findViewById<MaterialButton>(R.id.buttonVerSolicitacoes)?.setOnClickListener {
            startActivity(Intent(this, TelaRF31Solicitacoes::class.java))
        }
        findViewById<ImageView>(R.id.iconConfigAdm)?.setOnClickListener {
            startActivity(Intent(this, TelaRF38ConfigADM::class.java))
        }

        // ─── DADOS REAIS ──────────────────────────────────────────────────────
        carregarEstatisticas()
        carregarAtrasosCriticos()
        carregarAnaliseAlugueis()

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    override fun onResume() {
        super.onResume()
        carregarDadosPerfil()
        carregarEstatisticas()
        carregarAtrasosCriticos()
        carregarAnaliseAlugueis()
    }

    private fun carregarDadosPerfil() {
        val uid         = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        val txtBemVindo = findViewById<TextView>(R.id.textBemVindoAdm)
        // cardFotoAdm é um MaterialCardView — não pode ser cast para ImageView.
        // imageFotoAdmInterna é o ImageView filho e é único no layout,
        // portanto Activity.findViewById o localiza diretamente.
        val imageFoto   = findViewById<ImageView>(R.id.imageFotoAdmInterna)

        if (uid != null) {
            db.collection("administradores").document(uid).get()
                .addOnSuccessListener { doc ->
                    val nome = doc.getString("nome") ?: "Administrador"
                    txtBemVindo?.text = "Bem-vindo, $nome"

                    val fotoUrl = doc.getString("fotoUrl") ?: ""
                    if (fotoUrl.isNotEmpty()) {
                        imageFoto?.load(fotoUrl) {
                            placeholder(R.drawable.user_placeholder)
                            error(R.drawable.user_placeholder)
                        }
                    }
                }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CARD 1 — Contadores: Usuários e Aluguéis Ativos
    // ─────────────────────────────────────────────────────────────────────────
    private fun carregarEstatisticas() {
        val txtUsuarios  = findViewById<TextView>(R.id.txtContadorUsuarios)
        val txtAlugueis  = findViewById<TextView>(R.id.txtContadorAlugueis)
        val containerCad = findViewById<LinearLayout>(R.id.containerCadastrosPendentes)

        // ── Contagem de alunos — filtra ADMs pelo campo role ─────────────────
        // Nota: novos usuários recebem tipoPerfil:"estudante" mas nunca um campo
        // "role", logo whereEqualTo("role","aluno") retornaria 0. A solução é
        // buscar todos os docs e excluir apenas os que têm role == "adm".
        db.collection("usuarios").get()
            .addOnSuccessListener { result ->
                val count = result.documents.count { it.getString("role") != "adm" }
                txtUsuarios?.text = count.toString()
            }
            .addOnFailureListener { txtUsuarios?.text = "—" }

        // ── Contagem de aluguéis ativos ───────────────────────────────────────
        db.collection("solicitacoes_emprestimo")
            .whereIn("status", listOf("pendente", "ativo"))
            .count()
            .get(AggregateSource.SERVER)
            .addOnSuccessListener { snapshot ->
                txtAlugueis?.text = snapshot.count.toString()
            }
            .addOnFailureListener { txtAlugueis?.text = "—" }

        // ── Card 4: Cadastros pendentes de confirmação ────────────────────────
        db.collection("usuarios")
            .whereEqualTo("cadastroConfirmado", false)
            .limit(3)
            .get()
            .addOnSuccessListener { result ->
                containerCad?.removeAllViews()
                if (result.isEmpty) {
                    containerCad?.addView(criarLinhaTexto("Nenhum cadastro pendente.", "#415E5E", false))
                } else {
                    for (doc in result) {
                        val nome = doc.getString("nome") ?: doc.getString("email") ?: "Usuário"
                        containerCad?.addView(criarLinhaTexto("• $nome", "#415E5E", false))
                    }
                }
            }
            .addOnFailureListener {
                containerCad?.addView(criarLinhaTexto("Erro ao carregar.", "#D9534F", false))
            }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CARD 3 — Atrasos Críticos (data vencida, com nome do usuário)
    // ─────────────────────────────────────────────────────────────────────────
    private fun carregarAtrasosCriticos() {
        val container = findViewById<LinearLayout>(R.id.containerAtrasosCriticos) ?: return
        val agora     = System.currentTimeMillis()

        db.collection("solicitacoes_emprestimo")
            .whereLessThan("dataDevolucao", agora)
            .whereEqualTo("status", "ativo")
            .orderBy("dataDevolucao", Query.Direction.ASCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { result ->
                container.removeAllViews()
                if (result.isEmpty) {
                    container.addView(criarLinhaTexto("Nenhum atraso crítico.", "#415E5E", false))
                    return@addOnSuccessListener
                }

                for (doc in result) {
                    val uidAluno  = doc.getString("uidAluno") ?: continue
                    val dataMs    = doc.getLong("dataDevolucao") ?: continue
                    val diasAtras = ((agora - dataMs) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)

                    db.collection("usuarios").document(uidAluno).get()
                        .addOnSuccessListener { u ->
                            val nome = u.getString("nome") ?: u.getString("email") ?: "Usuário"
                            val row  = criarLinhaAtrasoCritico(nome, "$diasAtras dias")
                            runOnUiThread { container.addView(row) }
                        }
                }
            }
            .addOnFailureListener {
                // Fallback: busca por status atrasado
                db.collection("solicitacoes_emprestimo")
                    .whereEqualTo("status", "atrasado")
                    .limit(3)
                    .get()
                    .addOnSuccessListener { result ->
                        container.removeAllViews()
                        if (result.isEmpty) {
                            container.addView(criarLinhaTexto("Nenhum atraso encontrado.", "#415E5E", false))
                            return@addOnSuccessListener
                        }
                        for (doc in result) {
                            val uid  = doc.getString("uidAluno") ?: continue
                            db.collection("usuarios").document(uid).get()
                                .addOnSuccessListener { u ->
                                    val nome = u.getString("nome") ?: "Usuário"
                                    runOnUiThread { container.addView(criarLinhaAtrasoCritico(nome, "atrasado")) }
                                }
                        }
                    }
            }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CARD 2 — Análise de Aluguéis por mês (últimos 3 meses)
    // ─────────────────────────────────────────────────────────────────────────
    private fun carregarAnaliseAlugueis() {
        val container = findViewById<LinearLayout>(R.id.containerAnaliseAlugueis) ?: return
        container.removeAllViews()

        val sdf    = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))
        val cal    = Calendar.getInstance()
        val meses  = mutableListOf<Pair<String, Long>>() // (nome, timestamp início)

        // Gera os 3 últimos meses
        repeat(3) {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val inicio = cal.timeInMillis
            val nome   = sdf.format(Date(inicio)).replaceFirstChar { it.uppercase() }
            meses.add(Pair(nome, inicio))
            cal.add(Calendar.MONTH, -1)
        }

        // Conta os aluguéis de cada mês
        for ((nomeMes, inicio) in meses) {
            val fim = inicio + (31L * 24 * 60 * 60 * 1000)
            db.collection("solicitacoes_emprestimo")
                .whereGreaterThanOrEqualTo("dataSolicitacao", inicio)
                .whereLessThan("dataSolicitacao", fim)
                .count()
                .get(AggregateSource.SERVER)
                .addOnSuccessListener { snapshot ->
                    val row = criarLinhaAnalise(nomeMes, snapshot.count.toString())
                    runOnUiThread { container.addView(row) }
                }
                .addOnFailureListener {
                    val row = criarLinhaAnalise(nomeMes, "—")
                    runOnUiThread { container.addView(row) }
                }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers de criação de Views programáticas
    // ─────────────────────────────────────────────────────────────────────────

    private fun criarLinhaTexto(texto: String, colorHex: String, bold: Boolean): TextView {
        return TextView(this).apply {
            text    = texto
            setTextColor(android.graphics.Color.parseColor(colorHex))
            textSize = 16f
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 8, 0, 8)
        }
    }

    /** Linha horizontal: nome à esquerda (weight 1) + dias em vermelho à direita */
    private fun criarLinhaAtrasoCritico(nome: String, dias: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams = lp
            setPadding(0, 8, 0, 8)

            addView(TextView(context).apply {
                text = nome
                setTextColor(android.graphics.Color.parseColor("#415E5E"))
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(TextView(context).apply {
                text = dias
                setTextColor(resources.getColor(R.color.biblio_red, null))
                textSize = 16f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.END
            })
        }
    }

    /** Linha horizontal: nome mês (weight 1) + count em preto negrito à direita */
    private fun criarLinhaAnalise(nomeMes: String, count: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 8, 0, 8)

            addView(TextView(context).apply {
                text = nomeMes
                setTextColor(android.graphics.Color.parseColor("#415E5E"))
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(TextView(context).apply {
                text = count
                setTextColor(android.graphics.Color.BLACK)
                textSize = 18f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.END
            })
        }
    }
}
