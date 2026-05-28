package com.example.bibliounifornew.login

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.CadastroViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF04CadastroNovoUsuario : AppCompatActivity() {

    private var senhaVisivel = false
    private var confirmarSenhaVisivel = false

    // Instância do ViewModel
    private lateinit var viewModel: CadastroViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf04_cadastrar_novo_usuario)

        // Inicializa o ViewModel
        viewModel = ViewModelProvider(this)[CadastroViewModel::class.java]

        //-----------------------------------
        // COMPONENTES
        //-----------------------------------
        val imageLogo = findViewById<ImageView>(R.id.imageLogoCadastro)
        carregarLogoSegura(imageLogo)

        val nome = findViewById<EditText>(R.id.editTextNome)
        val usuario = findViewById<EditText>(R.id.editTextUsuario)
        val email = findViewById<EditText>(R.id.editTextEmail)
        val senha = findViewById<EditText>(R.id.editTextSenha)
        val confirmaSenha = findViewById<EditText>(R.id.editTextConfirmaSenha)

        val erroNome = findViewById<TextView>(R.id.tvErroNome)
        val erroUsuario = findViewById<TextView>(R.id.tvErroUsuario)
        val erroEmail = findViewById<TextView>(R.id.tvErroEmail)
        val erroSenha = findViewById<TextView>(R.id.tvErroSenha)
        val erroSenhaDiferente = findViewById<TextView>(R.id.tvErroSenhaDiferente)

        val btnCriar = findViewById<Button>(R.id.btnCriar)

        val olhoSenha = findViewById<ImageView>(R.id.iconOlhoSenha)
        val olhoConfirmar = findViewById<ImageView>(R.id.iconOlhoConfirmarSenha)

        //-----------------------------------
        // ENTRE AQUI
        //-----------------------------------
        findViewById<TextView>(R.id.textEntreAqui).setOnClickListener {
            startActivity(Intent(this, TelaRF03LoginAluno::class.java))
            finish()
        }

        //-----------------------------------
        // OLHO SENHA
        //-----------------------------------
        olhoSenha.setOnClickListener {
            if (senhaVisivel) {
                senha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                olhoSenha.setImageResource(R.drawable.ic_eye_closed)
            } else {
                senha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                olhoSenha.setImageResource(R.drawable.ic_eye_open)
            }
            senhaVisivel = !senhaVisivel
            senha.setSelection(senha.text.length)
        }

        //-----------------------------------
        // OLHO CONFIRMAR SENHA
        //-----------------------------------
        olhoConfirmar.setOnClickListener {
            if (confirmarSenhaVisivel) {
                confirmaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                olhoConfirmar.setImageResource(R.drawable.ic_eye_closed)
            } else {
                confirmaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                olhoConfirmar.setImageResource(R.drawable.ic_eye_open)
            }
            confirmarSenhaVisivel = !confirmarSenhaVisivel
            confirmaSenha.setSelection(confirmaSenha.text.length)
        }

        //-----------------------------------
        // BOTÃO CRIAR
        //-----------------------------------
        btnCriar.setOnClickListener {

            // Resetar visibilidade dos erros
            erroNome.visibility = View.GONE
            erroUsuario.visibility = View.GONE
            erroEmail.visibility = View.GONE
            erroSenha.visibility = View.GONE
            erroSenhaDiferente.visibility = View.GONE

            val nomeTexto = nome.text.toString().trim()
            val usuarioTexto = usuario.text.toString().trim()
            val emailTexto = email.text.toString().trim()
            val senhaTexto = senha.text.toString()
            val confirmaTexto = confirmaSenha.text.toString()

            var temErro = false

            // Validação individual
            if (nomeTexto.isEmpty()) {
                erroNome.visibility = View.VISIBLE
                temErro = true
            }
            if (usuarioTexto.isEmpty()) {
                erroUsuario.visibility = View.VISIBLE
                temErro = true
            }
            if (emailTexto.isEmpty()) {
                erroEmail.text = "Campo obrigatório"
                erroEmail.visibility = View.VISIBLE
                temErro = true
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailTexto).matches()) {
                erroEmail.text = "E-mail inválido"
                erroEmail.visibility = View.VISIBLE
                temErro = true
            }

            // Validação de Requisitos da Senha (8+ chars, Número, Maiúscula)
            val senhaValida = senhaTexto.length >= 8 &&
                              senhaTexto.any { it.isDigit() } &&
                              senhaTexto.any { it.isUpperCase() }

            if (!senhaValida) {
                erroSenha.visibility = View.VISIBLE
                temErro = true
            }

            // Validação de Confirmação
            if (senhaTexto != confirmaTexto) {
                erroSenhaDiferente.visibility = View.VISIBLE
                temErro = true
            }

            if (temErro) return@setOnClickListener

            //-----------------------------------
            // INTEGRAÇÃO FIREBASE (CRIAR CONTA)
            //-----------------------------------
            btnCriar.isEnabled = false
            btnCriar.text = "Criando..."

            viewModel.cadastrarUsuario(emailTexto, senhaTexto, nomeTexto, usuarioTexto) { sucesso, mensagem ->
                runOnUiThread {
                    btnCriar.isEnabled = true
                    btnCriar.text = "Criar Conta"

                    if (sucesso) {
                        mostrarPopupSucesso()
                    } else {
                        val erroTraduzido = traduzirErroFirebase(mensagem)
                        if (erroTraduzido.contains("e-mail já está cadastrado", ignoreCase = true)) {
                            erroEmail.text = erroTraduzido
                            erroEmail.visibility = View.VISIBLE
                            email.requestFocus()
                        } else {
                            Toast.makeText(this@TelaRF04CadastroNovoUsuario, erroTraduzido, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun mostrarPopupSucesso() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_sucesso_cadastro)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)

        val btnRetornar = dialog.findViewById<Button>(R.id.btnRetornarLogin)
        btnRetornar.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, TelaRF03LoginAluno::class.java)
            startActivity(intent)
            finish()
        }
        dialog.show()
    }

    private fun traduzirErroFirebase(mensagem: String?): String {
        return when {
            mensagem == null                                        -> "Ocorreu um erro inesperado. Tente novamente."
            mensagem.contains("email address is already")          -> "Este e-mail já está cadastrado."
            mensagem.contains("badly formatted")                   -> "Formato de e-mail inválido."
            mensagem.contains("network error", ignoreCase = true)
                || mensagem.contains("unreachable")                -> "Sem conexão com a internet. Verifique sua rede."
            mensagem.contains("password is invalid")
                || mensagem.contains("least 6 characters")        -> "Senha muito fraca. Use pelo menos 8 caracteres."
            mensagem.contains("too many requests", ignoreCase = true) -> "Muitas tentativas. Aguarde alguns minutos."
            mensagem.contains("Conta criada, mas falha")           -> "Conta criada! Mas não foi possível salvar seu perfil. Tente fazer login."
            else                                                   -> "Não foi possível criar a conta. Tente novamente."
        }
    }

    // ─── LOGO (Carregamento Seguro para evitar Canvas Limit Crash) ───────────
    private fun carregarLogoSegura(imageView: ImageView) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val resId = R.drawable.unifor_marca
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeResource(resources, resId, options)

                // Reduz para no máximo 500px para evitar estouro de memória (Canvas Limit)
                val targetSize = 500
                var inSampleSize = 1
                if (options.outHeight > targetSize || options.outWidth > targetSize) {
                    val halfHeight = options.outHeight / 2
                    val halfWidth = options.outWidth / 2
                    while (halfHeight / inSampleSize >= targetSize && halfWidth / inSampleSize >= targetSize) {
                        inSampleSize *= 2
                    }
                }

                options.inJustDecodeBounds = false
                options.inSampleSize = inSampleSize
                val bitmap = BitmapFactory.decodeResource(resources, resId, options)

                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}