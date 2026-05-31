package com.example.bibliounifornew.features.adm.gerenciamento

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.SolicitacaoRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class TelaRF30UsuariosParaADM : AppCompatActivity() {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val solicitacaoRepository = SolicitacaoRepository()

    /**
     * RF28.5 / RF28.13 — Mantido aqui para ser cancelado em onDestroy(),
     * evitando memory leak. Criado em exibirPopupSolicitacoes(), um por vez.
     */
    private var solicitacoesListener: ListenerRegistration? = null

    private var usuarioId    : String = ""
    private var usuarioNome  : String = ""
    private var usuarioEmail : String = ""

    private var activeDialog: Dialog? = null

    // Estado do status da conta — espelha o campo contaAtiva do Firestore
    private var contaAtiva = true
    private lateinit var btnAtivarConta: MaterialButton
    private lateinit var btnDesativarConta: MaterialButton

    // ─────────────────────────────────────────────────────────────────────────
    // CICLO DE VIDA
    // ─────────────────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf30_usuariosparaadm)

        usuarioId    = intent.getStringExtra("USUARIO_ID")    ?: ""
        usuarioNome  = intent.getStringExtra("USUARIO_NOME")  ?: "Usuário"
        usuarioEmail = intent.getStringExtra("USUARIO_EMAIL") ?: ""

        val textNome  = findViewById<TextView>(R.id.textNomeUsuario)
        val textEmail = findViewById<TextView>(R.id.textEmailUsuario)
        val textTipo  = findViewById<TextView>(R.id.textTipoUsuario)

        textNome?.text  = usuarioNome
        textEmail?.text = usuarioEmail

        buscarDadosCompletosUsuario(textTipo)

        findViewById<MaterialButton>(R.id.buttonSolicitacoes)?.setOnClickListener {
            exibirPopupSolicitacoes()
        }
        findViewById<MaterialButton>(R.id.buttonLivrosAlugados)?.setOnClickListener {
            val i = Intent(this, TelaRFAdmUsuarioAlugados::class.java)
            i.putExtra("USUARIO_ID",   usuarioId)
            i.putExtra("USUARIO_NOME", usuarioNome)
            startActivity(i)
        }
        findViewById<MaterialButton>(R.id.buttonAtrasos)?.setOnClickListener {
            exibirPopupAtraso()
        }
        findViewById<MaterialButton>(R.id.buttonPermissao)?.setOnClickListener {
            exibirPopupPermissao(textTipo)
        }
        btnAtivarConta    = findViewById(R.id.buttonAtivarConta)
        btnDesativarConta = findViewById(R.id.buttonDesativarConta)
        btnAtivarConta.setOnClickListener    { alterarStatusConta(ativar = true) }
        btnDesativarConta.setOnClickListener { exibirModalDesativar() }
    }

    /**
     * RF28.5 / RF28.13 FIX — cancela o SnapshotListener antes de destruir a Activity,
     * evitando callbacks órfãos e consumo desnecessário de rede/memória.
     */
    override fun onDestroy() {
        activeDialog?.dismiss()
        activeDialog = null
        solicitacoesListener?.remove()
        solicitacoesListener = null
        super.onDestroy()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DADOS DO PERFIL
    // ─────────────────────────────────────────────────────────────────────────

    private fun buscarDadosCompletosUsuario(textTipo: TextView?) {
        if (usuarioId.isEmpty()) return
        db.collection("usuarios").document(usuarioId).get()
            .addOnSuccessListener { doc ->
                if (isFinishing || isDestroyed) return@addOnSuccessListener
                if (!doc.exists()) return@addOnSuccessListener
                val role = doc.getString("role") ?: doc.getString("tipoPerfil") ?: "aluno"
                textTipo?.text = role.uppercase()
                contaAtiva = doc.getBoolean("contaAtiva") ?: true
                atualizarBotoesStatus()
                if (usuarioNome == "Usuário") {
                    usuarioNome = doc.getString("nome") ?: "Usuário"
                    findViewById<TextView>(R.id.textNomeUsuario)?.text = usuarioNome
                }
            }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RF28.5 / RF28.13 — SOLICITAÇÕES
    // ─────────────────────────────────────────────────────────────────────────

    private fun exibirPopupSolicitacoes() {
        activeDialog?.dismiss()
        val dialog = Dialog(this)
        activeDialog = dialog

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_solicitacoes_usuario_adm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setOnDismissListener { activeDialog = null }

        val txtNome   = dialog.findViewById<TextView>(R.id.textPopupNomeUsuario)
        val txtLista  = dialog.findViewById<TextView>(R.id.textPopupListaSolicitacoes)
        val txtStatus = dialog.findViewById<TextView>(R.id.textPopupStatus)
        val cardLivro = dialog.findViewById<MaterialCardView>(R.id.cardSolicitacaoLivro)
        val txtTitulo = dialog.findViewById<TextView>(R.id.textTituloLivroSolicitado)
        val txtAutor  = dialog.findViewById<TextView>(R.id.textAutorLivroSolicitado)
        val txtData   = dialog.findViewById<TextView>(R.id.textDataLivroSolicitado)
        val imgCapa   = dialog.findViewById<ImageView>(R.id.imageLivroSolicitado)
        val editPdf   = dialog.findViewById<TextInputEditText>(R.id.editLinkPdf)
        val editAudio = dialog.findViewById<TextInputEditText>(R.id.editLinkAudiobook)
        val btnSalvar = dialog.findViewById<MaterialButton>(R.id.btnSalvarLinks)

        // Variável para guardar os IDs da solicitação e do livro atuais
        var currentLivroId = ""
        var currentSolicitacaoId = ""

        // Zera o estado antes de buscar para não piscar dados antigos
        txtNome?.text  = getString(R.string.popup_solicitacoes_label_usuario, usuarioNome.uppercase())
        txtLista?.text = getString(R.string.msg_buscando_dados)
        txtStatus?.text = ""
        cardLivro?.visibility = View.GONE
        txtTitulo?.text = ""
        txtAutor?.text  = ""
        txtData?.text   = ""
        imgCapa?.setImageDrawable(null)

        // RF28.5 FIX: cancela qualquer listener anterior antes de criar um novo
        solicitacoesListener?.remove()

        if (usuarioId.isNotEmpty()) {
            solicitacoesListener = solicitacaoRepository
                .escutarSolicitacoesDoUsuario(usuarioId) { lista ->
                    if (!isFinishing && !isDestroyed) {
                        if (lista.isNullOrEmpty()) {
                            txtLista?.text  = getString(R.string.popup_solicitacoes_sem_dados)
                            txtStatus?.text = getString(R.string.popup_status_vazio)
                            cardLivro?.visibility = View.GONE
                            currentLivroId = ""
                            currentSolicitacaoId = ""
                        } else {
                            // RF28.13 FIX: exibe total + mais recente (não apenas a primeira)
                            val ultima    = lista.first()
                            currentLivroId = ultima.idLivro
                            currentSolicitacaoId = ultima.id

                            val tipoTexto = ultima.tipos.ifEmpty { "Geral" }
                            txtLista?.text  = getString(
                                R.string.popup_solicitacoes_total,
                                lista.size,
                                tipoTexto.uppercase()
                            )
                            txtStatus?.text = getString(
                                R.string.popup_status_label,
                                ultima.status.uppercase()
                            )

                            // Join assíncrono: busca dados do livro mais recente
                            if (ultima.idLivro.isNotEmpty()) {
                                db.collection("livros").document(ultima.idLivro).get()
                                    .addOnSuccessListener { doc ->
                                        if (!doc.exists()) {
                                            txtLista?.text = getString(R.string.popup_livro_nao_encontrado)
                                            cardLivro?.visibility = View.GONE
                                            return@addOnSuccessListener
                                        }
                                        cardLivro?.visibility = View.VISIBLE
                                        txtTitulo?.text = doc.getString("title")
                                            ?: doc.getString("titulo")
                                            ?: getString(R.string.sem_titulo)
                                        txtAutor?.text  = doc.getString("author")
                                            ?: doc.getString("autor")
                                            ?: getString(R.string.sem_autor)
                                        
                                        // Preenche os links atuais do livro
                                        editPdf?.setText(doc.getString("linkPdf") ?: "")
                                        editAudio?.setText(doc.getString("linkAudiobook") ?: "")

                                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                                        val dataFmt = if (ultima.dataSolicitacao > 0)
                                            sdf.format(Date(ultima.dataSolicitacao)) else "N/A"
                                        txtData?.text = getString(R.string.popup_data_pedido, dataFmt)
                                        // BUG-4B FIX: usa ic_sem_capa — sem Tolkien estático
                                        val coverUrl = doc.getString("coverUrl") ?: ""
                                        imgCapa?.load(coverUrl.ifEmpty { null }) {
                                            crossfade(true)
                                            placeholder(R.drawable.ic_sem_capa)
                                            error(R.drawable.ic_sem_capa)
                                            fallback(R.drawable.ic_sem_capa)
                                        }
                                    }
                                    .addOnFailureListener {
                                        txtLista?.text = getString(R.string.erro_conexao_banco)
                                        cardLivro?.visibility = View.GONE
                                    }
                            }
                        }
                    }
                }
        }

        btnSalvar?.setOnClickListener {
            val linkPdf   = editPdf?.text.toString().trim()
            val linkAudio = editAudio?.text.toString().trim()

            if (currentLivroId.isEmpty()) {
                Toast.makeText(this, "Nenhum livro identificado para salvar links.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSalvar.isEnabled = false
            val updates = hashMapOf<String, Any>(
                "linkPdf" to linkPdf,
                "linkAudiobook" to linkAudio,
                "hasPdf" to linkPdf.isNotEmpty(),
                "hasAudiobook" to linkAudio.isNotEmpty(),
                "temPdf" to linkPdf.isNotEmpty(),
                "temAudiobook" to linkAudio.isNotEmpty()
            )

            db.collection("livros").document(currentLivroId)
                .update(updates)
                .addOnSuccessListener {
                    // 1. Marca a solicitação como concluída (se houver uma ativa)
                    if (currentSolicitacaoId.isNotEmpty()) {
                        db.collection("solicitacoes_midia").document(currentSolicitacaoId)
                            .update("status", "concluido")
                    }

                    // 2. Cria notificação na subcoleção do usuário (Padrão do Projeto)
                    val notificacao = hashMapOf(
                        "titulo" to "Mídia disponível",
                        "mensagem" to "Novos links de mídia foram adicionados ao livro que você solicitou.",
                        "data" to System.currentTimeMillis(),
                        "lida" to false
                    )
                    db.collection("usuarios").document(usuarioId)
                        .collection("notificacoes").add(notificacao)

                    // 3. Feedback e fechamento
                    btnSalvar.isEnabled = true
                    exibirNotificacaoCinza()
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    btnSalvar.isEnabled = true
                    Toast.makeText(this, "Erro ao salvar links: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.findViewById<Button>(R.id.btnFecharSolicitacoes)?.setOnClickListener {
            dialog.dismiss()
            // O listener NÃO é cancelado aqui — permanece ativo até onDestroy(),
            // para que atualizações em tempo real continuem funcionando se o popup
            // for reaberto sem reiniciar a Activity.
        }
        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun exibirNotificacaoCinza() {
        val inflater = LayoutInflater.from(this)
        val layout = inflater.inflate(R.layout.toast_links_salvos, null)

        with(Toast(applicationContext)) {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            duration = Toast.LENGTH_SHORT
            @Suppress("DEPRECATION")
            view = layout
            show()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RF28.7 — ATRASOS (query real no Firestore)
    // ─────────────────────────────────────────────────────────────────────────

    private fun exibirPopupAtraso() {
        activeDialog?.dismiss()
        val dialog = Dialog(this)
        activeDialog = dialog

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_atraso_aluguel_usuario)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setOnDismissListener { activeDialog = null }

        val txtMensagem  = dialog.findViewById<TextView>(R.id.textNomeLivroAtrasado)
        val txtMulta     = dialog.findViewById<TextView>(R.id.textValorMulta)
        val btnQuitar    = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonQuitarMulta)

        txtMensagem?.text = getString(R.string.msg_verificando_atrasos)
        txtMulta?.text    = "--"

        if (usuarioId.isNotEmpty()) {
            // RF29.9: onAtrasados recebe (colecao, docIds) quando há atrasos reais
            buscarAtrasosNaColecao("alugueis", txtMensagem, txtMulta,
                onVazio = {
                    buscarAtrasosNaColecao("solicitacoes_emprestimo", txtMensagem, txtMulta, null) { col, ids ->
                        btnQuitar?.visibility = View.VISIBLE
                        btnQuitar?.setOnClickListener { quitarMultaComSenha(col, ids, dialog) }
                    }
                },
                onAtrasados = { col, ids ->
                    btnQuitar?.visibility = View.VISIBLE
                    btnQuitar?.setOnClickListener { quitarMultaComSenha(col, ids, dialog) }
                }
            )
        }

        dialog.findViewById<Button>(R.id.buttonFecharAtraso)?.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * Busca aluguéis atrasados de [colecao] para [usuarioId].
     * Um aluguel é considerado atrasado quando:
     *   - status == "atrasado"  OU
     *   - dataDevolucaoMs (ou campos equivalentes) < agora
     *
     * Multa calculada a R$ 1,00 por dia de atraso (mínimo R$ 1,00).
     */
    private fun buscarAtrasosNaColecao(
        colecao     : String,
        txtMsg      : TextView?,
        txtMulta    : TextView?,
        onVazio     : (() -> Unit)? = null,
        onAtrasados : ((colecao: String, docIds: List<String>) -> Unit)? = null
    ) {
        val agora = System.currentTimeMillis()

        db.collection(colecao)
            .whereEqualTo("uidAluno", usuarioId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    onVazio?.invoke() ?: run {
                        txtMsg?.text   = getString(R.string.popup_atraso_sem_pendencias)
                        txtMulta?.text = getString(R.string.popup_multa_zero)
                    }
                    return@addOnSuccessListener
                }

                // Filtra apenas os documentos efetivamente atrasados
                val atrasados = result.documents.filter { doc ->
                    val status = doc.getString("status") ?: ""
                    val dataDev = doc.getLong("dataDevolucaoMs")
                        ?: doc.getLong("dataVencimentoMs")
                        ?: doc.getLong("dataFimMs")
                        ?: -1L
                    status == "atrasado" || (dataDev in 1 until agora)
                }

                if (atrasados.isEmpty()) {
                    txtMsg?.text   = getString(R.string.popup_atraso_sem_pendencias)
                    txtMulta?.text = getString(R.string.popup_multa_zero)
                    return@addOnSuccessListener
                }

                // Calcula a multa total acumulada (R$ 1,00 / dia)
                val totalMultaReais = atrasados.sumOf { doc ->
                    val dataDev = doc.getLong("dataDevolucaoMs")
                        ?: doc.getLong("dataVencimentoMs")
                        ?: doc.getLong("dataFimMs")
                        ?: 0L
                    if (dataDev in 1 until agora) {
                        TimeUnit.MILLISECONDS.toDays(agora - dataDev).coerceAtLeast(1).toDouble()
                    } else {
                        1.0 // status == "atrasado" sem data: multa mínima
                    }
                }

                val brl = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                txtMulta?.text = brl.format(totalMultaReais)

                // RF29.9: notifica o caller com a coleção e IDs para o botão "Quitar Multa"
                onAtrasados?.invoke(colecao, atrasados.map { it.id })

                // Busca o título do livro mais atrasado para exibir na mensagem
                val maisAtrasado = atrasados.maxByOrNull { doc ->
                    val dataDev = doc.getLong("dataDevolucaoMs")
                        ?: doc.getLong("dataVencimentoMs")
                        ?: doc.getLong("dataFimMs") ?: 0L
                    agora - dataDev
                }
                val idLivro = maisAtrasado?.getString("idLivro")
                    ?: maisAtrasado?.getString("livroId") ?: ""

                if (idLivro.isNotEmpty()) {
                    db.collection("livros").document(idLivro).get()
                        .addOnSuccessListener { livroDoc ->
                            val titulo = livroDoc.getString("title")
                                ?: livroDoc.getString("titulo")
                                ?: getString(R.string.sem_titulo)
                            txtMsg?.text = getString(
                                R.string.popup_atraso_mensagem,
                                atrasados.size,
                                titulo
                            )
                        }
                        .addOnFailureListener {
                            txtMsg?.text = getString(
                                R.string.popup_atraso_sem_titulo,
                                atrasados.size
                            )
                        }
                } else {
                    txtMsg?.text = getString(
                        R.string.popup_atraso_sem_titulo,
                        atrasados.size
                    )
                }
            }
            .addOnFailureListener {
                onVazio?.invoke() ?: run {
                    txtMsg?.text   = getString(R.string.erro_conexao_banco)
                    txtMulta?.text = "--"
                }
            }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RF28.9 / RF28.10 — ALTERAR PERMISSÃO (com reauthenticate)
    // ─────────────────────────────────────────────────────────────────────────

    private fun exibirPopupPermissao(textTipo: TextView?) {
        activeDialog?.dismiss()
        val dialog = Dialog(this)
        activeDialog = dialog

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_mudar_permissao_adm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setOnDismissListener { activeDialog = null }

        val editSenha   = dialog.findViewById<TextInputEditText>(R.id.editSenhaPermissao)
        val txtErro     = dialog.findViewById<TextView>(R.id.textErroPermissao)
        val btnMudar    = dialog.findViewById<Button>(R.id.buttonMudarPermissao)
        val btnCancelar = dialog.findViewById<TextView>(R.id.textCancelarPermissao)

        btnMudar?.setOnClickListener {
            val senha = editSenha?.text?.toString()?.trim() ?: ""

            if (senha.isEmpty()) {
                txtErro?.text = getString(R.string.erro_campo)
                txtErro?.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            val adminEmail  = currentUser?.email
            if (currentUser == null || adminEmail.isNullOrEmpty()) {
                txtErro?.text = getString(R.string.erro_sessao_expirada)
                txtErro?.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Feedback visual: desabilita botão durante a verificação
            btnMudar.isEnabled = false
            btnMudar.text      = getString(R.string.msg_verificando)
            txtErro?.visibility = View.GONE

            // RF28.10 FIX: reautentica o admin antes de alterar permissões
            val credential = EmailAuthProvider.getCredential(adminEmail, senha)
            currentUser.reauthenticate(credential)
                .addOnSuccessListener {
                    if (usuarioId.isEmpty()) {
                        reativarBotaoPermissao(btnMudar, txtErro, getString(R.string.erro_generico))
                        return@addOnSuccessListener
                    }
                    db.collection("usuarios").document(usuarioId)
                        .update("role", "adm")
                        .addOnSuccessListener {
                            textTipo?.text = "ADM"
                            Toast.makeText(
                                this,
                                getString(R.string.msg_permissao_alterada),
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                        }
                        .addOnFailureListener { e ->
                            reativarBotaoPermissao(
                                btnMudar, txtErro,
                                e.message ?: getString(R.string.erro_generico)
                            )
                        }
                }
                .addOnFailureListener {
                    // RF28.10: senha incorreta → erro específico, botão reativado
                    reativarBotaoPermissao(
                        btnMudar, txtErro,
                        getString(R.string.erro_senha_incorreta)
                    )
                }
        }

        btnCancelar?.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun reativarBotaoPermissao(btn: Button?, txtErro: TextView?, msg: String) {
        btn?.isEnabled = true
        btn?.text      = getString(R.string.popup_permissao_btn_confirmar)
        txtErro?.text  = msg
        txtErro?.visibility = View.VISIBLE
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STATUS DA CONTA — Ativar / Desativar com log de auditoria
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sincroniza a visibilidade dos botões com o estado local [contaAtiva].
     * Sempre chamada após qualquer leitura ou escrita que altere o status.
     */
    private fun atualizarBotoesStatus() {
        if (contaAtiva) {
            btnAtivarConta.visibility    = View.GONE
            btnDesativarConta.visibility = View.VISIBLE
        } else {
            btnAtivarConta.visibility    = View.VISIBLE
            btnDesativarConta.visibility = View.GONE
        }
    }

    /**
     * Modal de confirmação Material antes de desativar.
     * Só o botão "Desativar" exige confirmação — ativar é ação
     * de baixo risco e dispensa confirmação.
     */
    private fun exibirModalDesativar() {
        if (usuarioId == auth.currentUser?.uid) {
            Toast.makeText(this, getString(R.string.erro_nao_pode_excluir_proprio), Toast.LENGTH_LONG).show()
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle("Desativar conta")
            .setMessage("Tem certeza que deseja desativar a conta de $usuarioNome?\n\nO usuário perderá o acesso ao aplicativo imediatamente.")
            .setPositiveButton("Desativar") { dialog, _ ->
                dialog.dismiss()
                alterarStatusConta(ativar = false)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Altera [contaAtiva] no Firestore com:
     *  - loading state (botão desabilitado durante a requisição)
     *  - revert automático em caso de falha
     *  - gravação de log de auditoria em subcoleção dedicada
     */
    private fun alterarStatusConta(ativar: Boolean) {
        if (usuarioId.isEmpty()) return

        val estadoAnterior = contaAtiva
        val btnAtivo       = if (ativar) btnAtivarConta else btnDesativarConta
        val textoOriginal  = btnAtivo.text.toString()

        // Loading state — bloqueia cliques duplos
        btnAtivo.isEnabled = false
        btnAtivo.text      = getString(R.string.msg_verificando)

        val admUid = auth.currentUser?.uid ?: ""
        val agora  = System.currentTimeMillis()
        val campos = mutableMapOf<String, Any>("contaAtiva" to ativar)
        if (ativar) {
            campos["reativadoPorAdm"] = admUid
            campos["reativadoEm"]     = agora
        } else {
            campos["desativadoPorAdm"] = admUid
            campos["desativadoEm"]     = agora
        }

        db.collection("usuarios").document(usuarioId)
            .update(campos)
            .addOnSuccessListener {
                contaAtiva = ativar
                atualizarBotoesStatus()
                gravarLogAuditoria(if (ativar) "ativado" else "desativado", admUid, agora)
                val msg = if (ativar) "Conta ativada com sucesso." else "Conta desativada com sucesso."
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Revert: UI volta exatamente ao estado anterior à tentativa
                contaAtiva         = estadoAnterior
                btnAtivo.isEnabled = true
                btnAtivo.text      = textoOriginal
                Toast.makeText(this, getString(R.string.erro_generico), Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Grava uma entrada imutável no log de auditoria do usuário.
     *
     * Estrutura do documento em usuarios/{uid}/log_auditoria/{autoId}:
     *   - acao        : "ativado" | "desativado"
     *   - realizadoPor: UID do administrador
     *   - timestamp   : epoch em ms (Long)
     *
     * Fire-and-forget: falha silenciosa — o dado principal já foi salvo com
     * sucesso antes desta chamada, então um erro aqui não deve reverter o estado.
     */
    // ─────────────────────────────────────────────────────────────────────────
    // RF29.9 — QUITAR MULTA (requer re-autenticação do ADM)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Exibe um diálogo de confirmação com campo de senha.
     * Após re-autenticação bem-sucedida do ADM, marca todos os documentos
     * de atraso como [multaQuitada = true] via WriteBatch atômico.
     * Falha de senha → mensagem de erro sem fechar o popup pai.
     */
    private fun quitarMultaComSenha(colecao: String, docIds: List<String>, parentDialog: Dialog) {
        if (docIds.isEmpty()) return

        val passwordInput = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Senha do administrador"
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar Quitação de Multa")
            .setMessage("Informe sua senha para quitar a multa de ${docIds.size} aluguel(is) de \"$usuarioNome\".")
            .setView(passwordInput)
            .setPositiveButton("Confirmar") { _, _ ->
                val senha = passwordInput.text.toString().trim()
                if (senha.isEmpty()) {
                    Toast.makeText(this, "Senha obrigatória.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val currentUser = auth.currentUser
                val adminEmail  = currentUser?.email
                if (currentUser == null || adminEmail.isNullOrEmpty()) {
                    Toast.makeText(this, getString(R.string.erro_sessao_expirada), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val credential = EmailAuthProvider.getCredential(adminEmail, senha)
                currentUser.reauthenticate(credential)
                    .addOnSuccessListener {
                        val agora     = System.currentTimeMillis()
                        val admUid    = auth.currentUser?.uid ?: ""
                        val batch     = db.batch()

                        docIds.forEach { docId ->
                            batch.update(
                                db.collection(colecao).document(docId),
                                mapOf(
                                    "multaQuitada"  to true,
                                    "quitadoPorAdm" to admUid,
                                    "quitadoEm"     to agora
                                )
                            )
                        }

                        batch.commit()
                            .addOnSuccessListener {
                                gravarLogAuditoria("multa_quitada", admUid, agora)
                                Toast.makeText(this, "Multa quitada com sucesso.", Toast.LENGTH_SHORT).show()
                                parentDialog.dismiss()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Erro ao quitar multa: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, getString(R.string.erro_senha_incorreta), Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun gravarLogAuditoria(acao: String, admUid: String, timestamp: Long) {
        if (usuarioId.isEmpty() || admUid.isEmpty()) return
        val entrada = mapOf(
            "acao"         to acao,
            "realizadoPor" to admUid,
            "timestamp"    to timestamp
        )
        db.collection("usuarios").document(usuarioId)
            .collection("log_auditoria")
            .add(entrada)
    }
}
