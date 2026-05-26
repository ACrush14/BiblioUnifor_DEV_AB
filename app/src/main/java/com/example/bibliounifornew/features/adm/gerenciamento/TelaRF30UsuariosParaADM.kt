package com.example.bibliounifornew.features.adm.gerenciamento

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import com.example.bibliounifornew.login.TelaRF23LoginADM
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TelaRF30UsuariosParaADM : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val solicitacaoRepository = SolicitacaoRepository()

    private var usuarioId: String = ""
    private var usuarioNome: String = ""
    private var usuarioEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf30_usuariosparaadm)

        // 1) Pegar dados vindos da tela anterior (TelaRF29)
        usuarioId    = intent.getStringExtra("USUARIO_ID")    ?: ""
        usuarioNome  = intent.getStringExtra("USUARIO_NOME")  ?: "Usuário"
        usuarioEmail = intent.getStringExtra("USUARIO_EMAIL") ?: ""

        // 2) Atualizar o Header com as informações REAIS do usuário
        val textNome  = findViewById<TextView>(R.id.textNomeUsuario)
        val textEmail = findViewById<TextView>(R.id.textEmailUsuario)
        val textTipo  = findViewById<TextView>(R.id.textTipoUsuario)

        textNome?.text  = usuarioNome
        textEmail?.text = usuarioEmail

        // Buscar role real no firestore
        buscarDadosCompletosUsuario(textTipo)

        // 3) Configurar botões
        findViewById<MaterialButton>(R.id.buttonSolicitacoes)?.setOnClickListener { exibirPopupSolicitacoes() }
        
        findViewById<MaterialButton>(R.id.buttonLivrosAlugados)?.setOnClickListener {
            val intent = Intent(this, TelaRFAdmUsuarioAlugados::class.java)
            intent.putExtra("USUARIO_ID", usuarioId)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.buttonAtrasos)?.setOnClickListener { exibirPopupAtraso() }
        findViewById<MaterialButton>(R.id.buttonPermissao)?.setOnClickListener { exibirPopupPermissao(textTipo) }
        findViewById<MaterialButton>(R.id.buttonExcluirConta)?.setOnClickListener { exibirPopupExcluirConta() }
    }

    private fun buscarDadosCompletosUsuario(textTipo: TextView?) {
        if (usuarioId.isEmpty()) return
        db.collection("usuarios").document(usuarioId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val role = doc.getString("role") ?: doc.getString("tipoPerfil") ?: "aluno"
                    textTipo?.text = role.uppercase()
                    
                    // Se o nome não veio pelo intent, pegamos do firestore
                    if (usuarioNome == "Usuário") {
                        findViewById<TextView>(R.id.textNomeUsuario)?.text = doc.getString("nome") ?: "Usuário"
                    }
                }
            }
    }

    // ─── POPUPS ───────────────────────────────────────────────────────────────

    private fun exibirPopupSolicitacoes() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_solicitacoes_usuario_adm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val txtNome   = dialog.findViewById<TextView>(R.id.textPopupNomeUsuario)
        val txtLista  = dialog.findViewById<TextView>(R.id.textPopupListaSolicitacoes)
        val txtStatus = dialog.findViewById<TextView>(R.id.textPopupStatus)
        val cardLivro = dialog.findViewById<MaterialCardView>(R.id.cardSolicitacaoLivro)

        val txtTitulo = dialog.findViewById<TextView>(R.id.textTituloLivroSolicitado)
        val txtAutor  = dialog.findViewById<TextView>(R.id.textAutorLivroSolicitado)
        val txtData   = dialog.findViewById<TextView>(R.id.textDataLivroSolicitado)
        val imgCapa   = dialog.findViewById<ImageView>(R.id.imageLivroSolicitado)

        // 1. Limpeza agressiva imediata (Zera tudo do XML)
        txtNome?.text = "USUÁRIO: ${usuarioNome.uppercase()}"
        txtLista?.text = "Buscando dados no banco..."
        txtStatus?.text = "STATUS: --"
        cardLivro?.visibility = View.GONE
        
        // Zera o conteúdo interno do card para não "piscar" o Ceifador
        txtTitulo?.text = ""
        txtAutor?.text = ""
        txtData?.text = ""
        imgCapa?.setImageDrawable(null)

        if (usuarioId.isNotEmpty()) {
            solicitacaoRepository.escutarSolicitacoesDoUsuario(usuarioId) { lista ->
                if (lista.isNullOrEmpty()) {
                    txtLista?.text = "Nenhuma solicitação encontrada."
                    txtStatus?.text = "STATUS: VAZIO"
                    cardLivro?.visibility = View.GONE
                } else {
                    val ultima = lista.first()
                    val tipoTexto = if (ultima.tipos.isNotEmpty()) ultima.tipos else "Geral"
                    
                    // Texto alterado para confirmar que o código NOVO está rodando
                    txtLista?.text  = "MÍDIA PEDIDA: ${tipoTexto.uppercase()}"
                    txtStatus?.text = "STATUS ATUAL: ${ultima.status.uppercase()}"

                    // Busca detalhes reais do livro
                    db.collection("livros").document(ultima.idLivro).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                cardLivro?.visibility = View.VISIBLE
                                
                                txtTitulo?.text = doc.getString("title") ?: doc.getString("titulo") ?: "Sem Título"
                                txtAutor?.text  = doc.getString("author") ?: doc.getString("autor") ?: "Sem Autor"
                                
                                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                                val dataFormatada = if (ultima.dataSolicitacao > 0) sdf.format(Date(ultima.dataSolicitacao)) else "N/A"
                                txtData?.text = "Data do Pedido: $dataFormatada"

                                val coverUrl = doc.getString("coverUrl") ?: ""
                                imgCapa?.load(coverUrl.ifEmpty { R.drawable.osda }) {
                                    placeholder(R.drawable.osda)
                                    error(R.drawable.osda)
                                }
                            } else {
                                txtLista?.text = "Livro não encontrado no sistema."
                                cardLivro?.visibility = View.GONE
                            }
                        }
                        .addOnFailureListener {
                            txtLista?.text = "Erro na conexão com o banco."
                            cardLivro?.visibility = View.GONE
                        }
                }
            }
        }

        dialog.findViewById<Button>(R.id.btnFecharSolicitacoes)?.setOnClickListener { dialog.dismiss() }
        dialog.show()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun exibirPopupAtraso() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_atraso_aluguel_usuario)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val txtMensagem = dialog.findViewById<TextView>(R.id.textNomeLivroAtrasado)
        val txtMulta    = dialog.findViewById<TextView>(R.id.textValorMulta)

        // Por enquanto limpamos os textos genéricos do XML
        txtMensagem?.text = "Verificando atrasos para $usuarioNome..."
        txtMulta?.text    = "Multa: R$ 0,00"

        // TODO: Implementar busca na coleção 'alugueis' filtrando por usuarioId e dataDevolucao < agora
        
        dialog.findViewById<Button>(R.id.buttonFecharAtraso)?.setOnClickListener { dialog.dismiss() }
        dialog.show()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun exibirPopupPermissao(textTipo: TextView?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_mudar_permissao_adm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnMudar    = dialog.findViewById<Button>(R.id.buttonMudarPermissao)
        val btnCancelar = dialog.findViewById<TextView>(R.id.textCancelarPermissao)

        btnMudar?.setOnClickListener {
            if (usuarioId.isEmpty()) return@setOnClickListener
            db.collection("usuarios").document(usuarioId).update("role", "adm")
                .addOnSuccessListener {
                    textTipo?.text = "ADMINISTRADOR"
                    Toast.makeText(this, "Permissão alterada com sucesso!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
        }

        btnCancelar?.setOnClickListener { dialog.dismiss() }
        dialog.show()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun exibirPopupExcluirConta() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_apagar_conta_adm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.findViewById<Button>(R.id.buttonConfirmarApagarContaADM)?.setOnClickListener {
            val editSenha = dialog.findViewById<TextInputEditText>(R.id.editSenhaApagarContaADM)
            if (editSenha?.text.isNullOrEmpty()) {
                Toast.makeText(this, "Confirme com sua senha de administrador", Toast.LENGTH_SHORT).show()
            } else {
                db.collection("usuarios").document(usuarioId).delete().addOnSuccessListener {
                    Toast.makeText(this, "Usuário removido", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    finish()
                }
            }
        }

        dialog.findViewById<Button>(R.id.buttonCancelarApagarContaADM)?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
