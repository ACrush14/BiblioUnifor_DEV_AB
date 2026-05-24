package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF17Amigos : AppCompatActivity() {

    private val authRepository = AuthRepository()
    private val db             = FirebaseFirestore.getInstance()

    // ─── AMIGOS CONFIRMADOS ────────────────────────────────────────────────────
    private lateinit var adapterAmigos  : AmigoAdapter
    private val listaAmigos             = mutableListOf<UsuarioAmigo>()

    // ─── SOLICITAÇÕES DE AMIZADE RECEBIDAS ────────────────────────────────────
    private lateinit var adapterSolicitacoes  : SolicitacaoAmizadeAdapter
    private val listaSolicitacoes             = mutableListOf<SolicitacaoAmizade>()

    // Referências de UI para show/hide da seção de solicitações
    private var textSolicitacoesTitulo  : TextView?     = null
    private var recyclerViewSolicitacoes: RecyclerView? = null
    private var dividerSolicitacoes     : View?         = null

    private var uidAtual: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf17_amigos)

        // ─── AUTENTICAÇÃO ─────────────────────────────────────────────────────
        val usuarioAtual = authRepository.getUsuarioAtual()
        if (usuarioAtual == null) {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }
        uidAtual = usuarioAtual.uid

        // ─── BOTÃO ADICIONAR AMIGOS ───────────────────────────────────────────
        findViewById<View>(R.id.layoutAdicionarAmigos)?.setOnClickListener {
            startActivity(Intent(this, TelaRF17_3_BuscaAmigos::class.java))
        }

        // ─── REFERÊNCIAS DA SEÇÃO DE SOLICITAÇÕES ─────────────────────────────
        textSolicitacoesTitulo   = findViewById(R.id.textSolicitacoesTitulo)
        recyclerViewSolicitacoes = findViewById(R.id.recyclerViewSolicitacoes)
        dividerSolicitacoes      = findViewById(R.id.dividerSolicitacoes)

        // ─── ADAPTER DE SOLICITAÇÕES ──────────────────────────────────────────
        adapterSolicitacoes = SolicitacaoAmizadeAdapter(
            listaSolicitacoes,
            onAceitar = { solicitacao, position -> aceitarSolicitacao(solicitacao, position) },
            onRecusar = { solicitacao, position -> recusarSolicitacao(solicitacao, position) }
        )
        recyclerViewSolicitacoes?.layoutManager = LinearLayoutManager(this)
        recyclerViewSolicitacoes?.adapter = adapterSolicitacoes

        // ─── ADAPTER DE AMIGOS ────────────────────────────────────────────────
        adapterAmigos = AmigoAdapter(listaAmigos)
        val recyclerViewAmigos = findViewById<RecyclerView>(R.id.recyclerViewAmigos)
        recyclerViewAmigos.layoutManager = LinearLayoutManager(this)
        recyclerViewAmigos.adapter = adapterAmigos

        // ─── CARREGAMENTO DE DADOS ────────────────────────────────────────────
        carregarAmigos()
        carregarSolicitacoesRecebidas()

        // Configurar Barra de Navegação
        NavigationHelper.configurarBarraNavegacao(this)
    }

    // ─── CARREGAMENTO ─────────────────────────────────────────────────────────

    /**
     * Carrega SOMENTE os amigos confirmados da subcoleção usuarios/{uid}/amigos.
     * Exclui todos os outros usuários — diferente do comportamento anterior que listava todos.
     */
    private fun carregarAmigos() {
        db.collection("usuarios").document(uidAtual).collection("amigos")
            .get()
            .addOnSuccessListener { result ->
                listaAmigos.clear()
                for (doc in result.documents) {
                    listaAmigos.add(
                        UsuarioAmigo(
                            uid     = doc.getString("uid")     ?: doc.id,
                            nome    = doc.getString("nome")    ?: "Amigo",
                            usuario = doc.getString("usuario") ?: ""
                        )
                    )
                }
                adapterAmigos.notifyDataSetChanged()
                if (listaAmigos.isEmpty()) {
                    Toast.makeText(this, "Você ainda não tem amigos adicionados.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao carregar amigos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Carrega solicitações de amizade pendentes onde o usuário atual é destinatário.
     * Mostra a seção de solicitações somente se houver alguma pendente.
     */
    private fun carregarSolicitacoesRecebidas() {
        db.collection("solicitacoes_amizade")
            .whereEqualTo("uidDestinatario", uidAtual)
            .whereEqualTo("status", "pendente")
            .get()
            .addOnSuccessListener { result ->
                listaSolicitacoes.clear()
                for (doc in result.documents) {
                    listaSolicitacoes.add(
                        SolicitacaoAmizade(
                            docId             = doc.id,
                            uidRemetente      = doc.getString("uidRemetente")  ?: "",
                            nomeRemetente     = doc.getString("nomeRemetente") ?: "Usuário",
                            status            = doc.getString("status")        ?: "pendente"
                        )
                    )
                }
                adapterSolicitacoes.notifyDataSetChanged()
                atualizarVisibilidadeSolicitacoes()
            }
            .addOnFailureListener {
                // Falha silenciosa — usuário pode não ter índice composto criado ainda
                // Índice necessário: solicitacoes_amizade (uidDestinatario ASC, status ASC)
            }
    }

    private fun atualizarVisibilidadeSolicitacoes() {
        val visivel = if (listaSolicitacoes.isNotEmpty()) View.VISIBLE else View.GONE
        textSolicitacoesTitulo?.visibility   = visivel
        recyclerViewSolicitacoes?.visibility = visivel
        dividerSolicitacoes?.visibility      = visivel
    }

    // ─── ACEITAR SOLICITAÇÃO ──────────────────────────────────────────────────

    /**
     * Aceitar solicitação de amizade:
     * 1. Busca os dados dos dois perfis
     * 2. Cria documentos mútuos em usuarios/{uid}/amigos usando Batch
     * 3. Atualiza o status da solicitação para "aceito"
     * 4. Atualiza RecyclerViews localmente
     */
    private fun aceitarSolicitacao(solicitacao: SolicitacaoAmizade, position: Int) {
        // Busca perfil do remetente para adicionar nos amigos do destinatário
        db.collection("usuarios").document(solicitacao.uidRemetente).get()
            .addOnSuccessListener { docRemetente ->
                val nomeRemetente    = docRemetente.getString("nome")    ?: solicitacao.nomeRemetente
                val usuarioRemetente = docRemetente.getString("usuario") ?: ""

                // Busca perfil do destinatário (eu) para adicionar nos amigos do remetente
                db.collection("usuarios").document(uidAtual).get()
                    .addOnSuccessListener { docAtual ->
                        val nomeAtual    = docAtual.getString("nome")    ?: "Usuário"
                        val usuarioAtual = docAtual.getString("usuario") ?: ""

                        val batch = db.batch()

                        // Adiciona remetente na subcoleção de amigos do destinatário
                        val amigoParaAtual = db.collection("usuarios").document(uidAtual)
                            .collection("amigos").document(solicitacao.uidRemetente)
                        batch.set(amigoParaAtual, mapOf(
                            "uid"     to solicitacao.uidRemetente,
                            "nome"    to nomeRemetente,
                            "usuario" to usuarioRemetente
                        ))

                        // Adiciona destinatário na subcoleção de amigos do remetente
                        val amigoParaRemetente = db.collection("usuarios").document(solicitacao.uidRemetente)
                            .collection("amigos").document(uidAtual)
                        batch.set(amigoParaRemetente, mapOf(
                            "uid"     to uidAtual,
                            "nome"    to nomeAtual,
                            "usuario" to usuarioAtual
                        ))

                        // Marca solicitação como aceita
                        val solicitacaoRef = db.collection("solicitacoes_amizade").document(solicitacao.docId)
                        batch.update(solicitacaoRef, "status", "aceito")

                        batch.commit()
                            .addOnSuccessListener {
                                // Remove da lista de solicitações
                                adapterSolicitacoes.removerItem(position)
                                atualizarVisibilidadeSolicitacoes()

                                // Adiciona na lista de amigos
                                listaAmigos.add(UsuarioAmigo(
                                    uid     = solicitacao.uidRemetente,
                                    nome    = nomeRemetente,
                                    usuario = usuarioRemetente
                                ))
                                adapterAmigos.notifyDataSetChanged()

                                Toast.makeText(
                                    this,
                                    "$nomeRemetente foi adicionado(a) como amigo(a)!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Erro ao aceitar solicitação: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro ao buscar seu perfil.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao buscar perfil do solicitante.", Toast.LENGTH_SHORT).show()
            }
    }

    // ─── RECUSAR SOLICITAÇÃO ──────────────────────────────────────────────────

    /**
     * Recusa a solicitação: atualiza o status para "recusado" no Firestore
     * e remove o item localmente.
     */
    private fun recusarSolicitacao(solicitacao: SolicitacaoAmizade, position: Int) {
        db.collection("solicitacoes_amizade").document(solicitacao.docId)
            .update("status", "recusado")
            .addOnSuccessListener {
                adapterSolicitacoes.removerItem(position)
                atualizarVisibilidadeSolicitacoes()
                Toast.makeText(this, "Solicitação recusada.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao recusar solicitação: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
