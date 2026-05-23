package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.example.bibliounifornew.login.TelaRF01BemVindo
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseUser

class TelaRF09Configuracao : AppCompatActivity() {

    // 1. Instanciando os Repositórios e Variáveis Globais
    private val authRepository = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private var usuarioAtual: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf09_configuracao)

        // Pega o usuário logado para usarmos na tela inteira
        usuarioAtual = authRepository.getUsuarioAtual()

        // Mapeamento de Componentes
        val btnRedefinir = findViewById<MaterialButton>(R.id.buttonRedefinirSenha)
        val btnApagar = findViewById<MaterialButton>(R.id.buttonApagarConta)
        val editNome = findViewById<EditText>(R.id.editNome)
        val editUsuario = findViewById<EditText>(R.id.editUsuario)
        val editBio = findViewById<EditText>(R.id.editBio)
        val textEmail = findViewById<TextView>(R.id.textUsuario)
        val iconEditNome = findViewById<ImageView>(R.id.iconEditNome)
        val iconEditUsuario = findViewById<ImageView>(R.id.iconEditUsuario)
        val iconEditBio = findViewById<ImageView>(R.id.iconEditBio)
        val editSenhaAtual = findViewById<EditText>(R.id.editSenhaAtual)
        val iconOlhoSenha = findViewById<ImageView>(R.id.iconOlhoSenhaAtual)

        // 2. ESTADO INICIAL: Campos desabilitados
        editNome.isEnabled = false
        editUsuario.isEnabled = false
        editBio.isEnabled = false
        editSenhaAtual.isEnabled = false

        // 3. CARREGAR DADOS REAIS DO FIREBASE
        if (usuarioAtual != null) {
            textEmail?.text = usuarioAtual!!.email

            editNome.setText("Carregando...")
            editUsuario.setText("...")

            usuarioRepository.buscarPerfilUsuario(usuarioAtual!!.uid) { sucesso, dados, erro ->
                if (sucesso && dados != null) {
                    val nomeBanco = dados["nome"] as? String ?: ""
                    val usernameBanco = dados["usuario"] as? String ?: ""
                    val bioBanco = dados["biografia"] as? String ?: ""

                    editNome.setText(nomeBanco)
                    editUsuario.setText(usernameBanco)

                    if (bioBanco.isNotEmpty()) {
                        editBio.setText(bioBanco)
                    } else {
                        editBio.setText("Escreva um pouco sobre você...")
                    }
                } else {
                    Toast.makeText(this, "Erro ao buscar dados: $erro", Toast.LENGTH_SHORT).show()
                    editNome.setText("")
                    editUsuario.setText("")
                }
            }
        }

        // 4. APLICANDO A LÓGICA DE EDIÇÃO INTELIGENTE
        configurarCampoEditavel(editNome, iconEditNome, "nome")
        configurarCampoEditavel(editUsuario, iconEditUsuario, "usuario")
        configurarCampoEditavel(editBio, iconEditBio, "biografia")

        // 5. LÓGICA DO OLHO DA SENHA
        var senhaVisivel = false
        iconOlhoSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                editSenhaAtual.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iconOlhoSenha.setImageResource(R.drawable.ic_eye_open)
            } else {
                editSenhaAtual.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconOlhoSenha.setImageResource(R.drawable.ic_eye_closed)
            }
            editSenhaAtual.setSelection(editSenhaAtual.text.length)
        }

        // 6. REDEFINIR SENHA (ABRE RF10)
        btnRedefinir.setOnClickListener {
            startActivity(Intent(this, TelaRF10RedefinirSenha::class.java))
        }

        // 7. APAGAR CONTA
        btnApagar.setOnClickListener {
            exibirPopupApagarConta()
        }
    }

    // ---------------------------------------------------------
    // MÉTODOS PRIVADOS DA CLASSE
    // ---------------------------------------------------------

    private fun configurarCampoEditavel(editText: EditText, iconEdicao: ImageView, nomeDoCampoNoBanco: String) {
        // Destrava o campo
        iconEdicao.setOnClickListener {
            editText.isEnabled = true
            editText.requestFocus()
            editText.setSelection(editText.text.length)

            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }

        // Trava e salva ao perder o foco
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && usuarioAtual != null) {
                editText.isEnabled = false
                val novoValor = editText.text.toString().trim()

                usuarioRepository.atualizarCampoPerfil(usuarioAtual!!.uid, nomeDoCampoNoBanco, novoValor) { sucesso, erro ->
                    if (sucesso) {
                        Toast.makeText(this, "Alteração salva com sucesso!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Erro ao salvar: $erro", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun exibirPopupApagarConta() {
        val dialogView = layoutInflater.inflate(R.layout.popup_apagar_conta, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnConfirmar = dialogView.findViewById<MaterialButton>(R.id.buttonConfirmarApagarConta)
        val btnCancelar = dialogView.findViewById<TextView>(R.id.textCancelarApagarConta)
        val editSenha = dialogView.findViewById<EditText>(R.id.editSenhaPopup)
        val iconOlho = dialogView.findViewById<ImageView>(R.id.iconOlhoSenhaPopup)

        var senhaVisivelPopup = false
        iconOlho.setOnClickListener {
            senhaVisivelPopup = !senhaVisivelPopup
            if (senhaVisivelPopup) {
                editSenha.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                iconOlho.setImageResource(R.drawable.ic_eye_open)
            } else {
                editSenha.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                iconOlho.setImageResource(R.drawable.ic_eye_closed)
            }
            editSenha.setSelection(editSenha.text.length)
        }

        btnConfirmar.setOnClickListener {
            authRepository.getUsuarioAtual()?.delete()

            val intent = Intent(this, TelaRF01BemVindo::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}