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
        findViewById<MaterialButton>(R.id.buttonExcluirConta)?.setOnClickListener {
            exibirPopupExcluirConta()
        }
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
                                        val coverUrl = doc.getString("coverUrl") ?: ""
                                        imgCapa?.load(coverUrl.ifEmpty { R.drawable.osda }) {
                                            placeholder(R.drawable.osda)
                                            error(R.drawable.osda)
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

        val txtMensagem = dialog.findViewById<TextView>(R.id.textNomeLivroAtrasado)
        val txtMulta    = dialog.findViewById<TextView>(R.id.textValorMulta)

        txtMensagem?.text = getString(R.string.msg_verificando_atrasos)
        txtMulta?.text    = "--"

        if (usuarioId.isNotEmpty()) {
            buscarAtrasosNaColecao("alugueis", txtMensagem, txtMulta) {
                // Fallback para a coleção alternativa
                buscarAtrasosNaColecao("solicitacoes_emprestimo", txtMensagem, txtMulta, null)
            }
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
        colecao   : String,
        txtMsg    : TextView?,
        txtMulta  : TextView?,
        onVazio   : (() -> Unit)?
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
    // RF28.11 / RF28.12 — EXCLUIR CONTA (com reauthenticate + guard)
    // ─────────────────────────────────────────────────────────────────────────

    private fun exibirPopupExcluirConta() {
        // Guard RF28.11: impede que o admin exclua a própria conta por aqui,
        // prevenindo o bug "volta ao Login ADM" — que ocorria porque o admin
        // selecionava seu próprio UID na lista e deletava o próprio documento,
        // disparando o check RBAC "!doc.exists() → signOut()" no próximo login.
        if (usuarioId == auth.currentUser?.uid) {
            Toast.makeText(
                this,
                getString(R.string.erro_nao_pode_excluir_proprio),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val dialog = Dialog(this)
        activeDialog = dialog

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_apagar_conta_adm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setOnDismissListener { activeDialog = null }

        val editSenha    = dialog.findViewById<TextInputEditText>(R.id.editSenhaApagarContaADM)
        val txtErro      = dialog.findViewById<TextView>(R.id.textErroApagarContaADM)
        val btnConfirmar = dialog.findViewById<Button>(R.id.buttonConfirmarApagarContaADM)
        val btnCancelar  = dialog.findViewById<Button>(R.id.buttonCancelarApagarContaADM)

        btnConfirmar?.setOnClickListener {
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

            btnConfirmar.isEnabled = false
            btnConfirmar.text      = getString(R.string.msg_verificando)
            txtErro?.visibility    = View.GONE

            // RF28.12 FIX: valida a senha do admin via reauthenticate antes de excluir
            val credential = EmailAuthProvider.getCredential(adminEmail, senha)
            currentUser.reauthenticate(credential)
                .addOnSuccessListener {
                    // GAP-1 FIX: Desativação lógica (soft-delete) em vez de delete().
                    //
                    // Motivo: delete() apaga todos os dados do usuário permanentemente
                    // (histórico, perfil, aluguéis passados). Com update() o documento
                    // permanece, preservando rastreabilidade. O campo "contaAtiva = false"
                    // é verificado em TelaRF03LoginAluno após autenticação bem-sucedida
                    // para bloquear o acesso sem apagar o Auth record.
                    //
                    // O registro no Firebase Authentication permanece — deleção de Auth
                    // de terceiros exige Cloud Function com Admin SDK (fora do escopo cliente).
                    val admUid = currentUser.uid
                    db.collection("usuarios").document(usuarioId)
                        .update(
                            mapOf(
                                "contaAtiva"      to false,
                                "desativadoPorAdm" to admUid,
                                "desativadoEm"    to System.currentTimeMillis()
                            )
                        )
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                getString(R.string.msg_conta_removida),
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                            // finish() retorna para TelaRF29GerenciamentoDeUsuarios,
                            // que recarrega a lista em onResume().
                            finish()
                        }
                        .addOnFailureListener { e ->
                            reativarBotaoExcluir(
                                btnConfirmar, txtErro,
                                e.message ?: getString(R.string.erro_generico)
                            )
                        }
                }
                .addOnFailureListener {
                    reativarBotaoExcluir(
                        btnConfirmar, txtErro,
                        getString(R.string.erro_senha_incorreta)
                    )
                }
        }

        btnCancelar?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun reativarBotaoExcluir(btn: Button?, txtErro: TextView?, msg: String) {
        btn?.isEnabled = true
        btn?.text      = getString(R.string.btn_apagar)
        txtErro?.text  = msg
        txtErro?.visibility = View.VISIBLE
    }
}
