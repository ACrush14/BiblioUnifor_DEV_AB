package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

// Modelo simples para simular as solicitações
data class Solicitacao(
    val id: Int,
    val usuario: String,
    val livro: String,
    val autor: String,
    val tipos: String,
    val status: String
)

class TelaRF31Solicitacoes : AppCompatActivity() {

    // Lista simulada de dados
    private var listaOriginal = ArrayList<Solicitacao>()
    private var listaFiltrada = ArrayList<Solicitacao>()

    // Referência ao container do card no XML (simulando um comportamento dinâmico simplificado)
    private lateinit var containerSolicitacoes: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf31_solicitacoes_adm)

        // Inicializa dados simulados
        carregarDadosSimulados()

        // Mapeamento de componentes
        val btnFiltro = findViewById<ImageView>(R.id.buttonFiltroMidia)
        val editPesquisa = findViewById<EditText>(R.id.editPesquisaMidia)

        // No XML fornecido só existe um card fixo, para fins didáticos e seguindo a restrição de não usar RecyclerView,
        // vamos controlar as ações desse card fixo e usar Toasts/Popups para o fluxo solicitado.
        // Em um cenário real com ArrayList simulada, poderíamos inflar layouts dinamicamente aqui.

        configurarAcoesCardFixo()

        // Clique no ícone de filtro
        btnFiltro?.setOnClickListener {
            abrirPopupFiltro()
        }

        // Pesquisa simples por texto
        editPesquisa?.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                // Simulação de filtragem por nome
                Toast.makeText(this@TelaRF31Solicitacoes, "Filtrando: ${s.toString()}", Toast.LENGTH_SHORT).show()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun carregarDadosSimulados() {
        listaOriginal.add(Solicitacao(1, "Ronaldo Alves", "O Senhor dos Anéis", "J. R. R. Tolkien", "Audiobook, PDF, Braille", "Pendente"))
        listaOriginal.add(Solicitacao(2, "Maria Silva", "Dom Casmurro", "Machado de Assis", "PDF", "Concluído"))
        listaFiltrada.addAll(listaOriginal)
    }

    private fun configurarAcoesCardFixo() {
        val btnVerSolicitacoes = findViewById<Button>(R.id.buttonVerSolicitacoesUsuario)
        val btnEnviarAudiobook = findViewById<Button>(R.id.buttonEnviarAudiobook)
        val btnEnviarPDF = findViewById<Button>(R.id.buttonEnviarPDF)
        val btnNotificarBraille = findViewById<Button>(R.id.buttonBrailleConcluido)
        val btnExcluirSolicitacao = findViewById<Button>(R.id.buttonExcluirSolicitacao)

        btnVerSolicitacoes?.setOnClickListener {
            abrirPopupSolicitacoesUsuario("Ronaldo Alves", "Audiobook, PDF, Braille", "Em processamento")
        }

        btnEnviarAudiobook?.setOnClickListener {
            escolherArquivo("audio/*", "Audiobook enviado com sucesso")
        }

        btnEnviarPDF?.setOnClickListener {
            escolherArquivo("application/pdf", "PDF enviado com sucesso")
        }

        btnNotificarBraille?.setOnClickListener {
            abrirPopupSucesso("Braille concluído e usuário notificado")
        }

        btnExcluirSolicitacao?.setOnClickListener {
            abrirPopupConfirmarExclusao()
        }
    }

    // 1) POPUP FILTRO
    private fun abrirPopupFiltro() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_filtrar_midia)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val spinner = dialog.findViewById<Spinner>(R.id.spinnerSolicitacao)
        val editNome = dialog.findViewById<EditText>(R.id.editNomeUsuario)
        val btnSalvar = dialog.findViewById<Button>(R.id.btnSalvarFiltro)
        val btnLimpar = dialog.findViewById<Button>(R.id.btnLimparFiltro)

        // Configurar Spinner
        val opcoes = arrayOf("Selecione...", "PDF", "Audiobook", "Braille", "Aluguel")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcoes)
        spinner.adapter = adapter

        btnSalvar.setOnClickListener {
            val tipo = spinner.selectedItem.toString()
            val nome = editNome.text.toString()
            Toast.makeText(this, "Filtrando por: $tipo e $nome", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        btnLimpar.setOnClickListener {
            editNome.setText("")
            spinner.setSelection(0)
            Toast.makeText(this, "Filtros limpos", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    // 2) POPUP SOLICITAÇÕES USUÁRIO
// 2) POPUP SOLICITAÇÕES USUÁRIO
    private fun abrirPopupSolicitacoesUsuario(nome: String, solicitacoes: String, status: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_solicitacoes_usuario_adm)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Buscando explicitamente dentro do escopo do layout inflado no dialog
        val txtNome = dialog.findViewById<TextView>(R.id.textPopupNomeUsuario)
        val txtSolicitacoes = dialog.findViewById<TextView>(R.id.textPopupListaSolicitacoes)
        val txtStatus = dialog.findViewById<TextView>(R.id.textPopupStatus)
        val btnFechar = dialog.findViewById<Button>(R.id.btnFecharSolicitacoes)

        // Atribuição segura com Safe Cast ou validação de nulidade para evitar Crash se o ID mudar
        txtNome?.text = "Nome: $nome"
        txtSolicitacoes?.text = "Solicitações: $solicitacoes"
        txtStatus?.text = "Status: $status"

        btnFechar?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // 3 e 4) CHOOSER ARQUIVOS
    private fun escolherArquivo(tipo: String, mensagemSucesso: String) {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT) // Use GET_CONTENT para escolher arquivos
            intent.type = tipo
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, "Selecione o arquivo"), 100)
            
            // Simulação: como não temos o retorno real de arquivo aqui, 
            // vamos mostrar o popup de sucesso logo após abrir o chooser (para fins de demonstração do fluxo)
            abrirPopupSucesso(mensagemSucesso)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao abrir seletor", Toast.LENGTH_SHORT).show()
        }
    }

    // 5) POPUP NOTIFICAÇÃO SUCESSO
    private fun abrirPopupSucesso(mensagem: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_notificacao_braille_concluido)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val txtMsg = dialog.findViewById<TextView>(R.id.textMensagemSucesso)
        val btnOk = dialog.findViewById<Button>(R.id.btnOkNotificacao)

        txtMsg.text = mensagem
        btnOk.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // 6) POPUP EXCLUIR COM SENHA
    private fun abrirPopupConfirmarExclusao() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_confirmar_exclusao_solicitacao)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val editSenha = dialog.findViewById<EditText>(R.id.editSenhaConfirmacao)
        val btnOlho = dialog.findViewById<ImageView>(R.id.buttonVerSenha)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarExclusao)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarExclusao)

        var senhaVisivel = false

        btnOlho.setOnClickListener {
            if (senhaVisivel) {
                editSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnOlho.setImageResource(R.drawable.ic_eye_closed)
            } else {
                editSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnOlho.setImageResource(R.drawable.ic_eye_open)
            }
            senhaVisivel = !senhaVisivel
            editSenha.setSelection(editSenha.text.length)
        }

        btnConfirmar.setOnClickListener {
            val senha = editSenha.text.toString()
            if (senha == "admin123") { // Senha simulada
                Toast.makeText(this, "Solicitação apagada", Toast.LENGTH_SHORT).show()
                // Simulação: sumir com o card (encontrando o card pai se fosse dinâmico, ou apenas feedback visual)
                dialog.dismiss()
            } else {
                val toast = Toast.makeText(this, "Senha incorreta", Toast.LENGTH_SHORT)
                // Personalização simples do Toast para "vermelho" não é trivial no Android moderno sem custom view,
                // mas seguindo a lógica de simplicidade:
                toast.show()
            }
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
