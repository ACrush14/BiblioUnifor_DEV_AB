package com.example.bibliounifornew.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.data.AppDatabase
import com.example.bibliounifornew.data.Usuario
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class TelaRF04CadastroNovoUsuario : AppCompatActivity() {

    private lateinit var etNome: EditText
    private lateinit var etUsuario: EditText
    private lateinit var etEmail: EditText
    private lateinit var etSenha: EditText
    private lateinit var etConfirmaSenha: EditText
    private lateinit var tvErroEmail: TextView
    private lateinit var tvErroSenha: TextView
    private lateinit var btnCriar: Button
    private lateinit var btnEntreAqui: TextView

    private lateinit var bntOlhoSenha: ImageView

    private lateinit var bntOlhoConfirmarSenha: ImageView


    private val db by lazy { AppDatabase.Companion.getDatabase(this@TelaRF04CadastroNovoUsuario) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf04_cadastrar_novo_usuario)

        etNome = findViewById(R.id.editTextNome)
        etUsuario = findViewById(R.id.editTextUsuario)
        etEmail = findViewById(R.id.editTextEmail)
        etSenha = findViewById(R.id.editTextSenha)
        etConfirmaSenha = findViewById(R.id.editTextConfirmaSenha)
        tvErroEmail = findViewById(R.id.tvErroEmail)
        tvErroSenha = findViewById(R.id.tvErroSenha)
        btnEntreAqui = findViewById(R.id.textEntreAqui)
        btnCriar = findViewById(R.id.btnCriar)
        bntOlhoSenha = findViewById(R.id.iconOlhoSenha)
        bntOlhoConfirmarSenha = findViewById(R.id.iconOlhoConfirmarSenha)

        btnCriar.setOnClickListener {
            validarECadastrar()
        }

        btnEntreAqui.setOnClickListener {
            irParaLogin()
        }

        tvErroEmail.visibility = View.GONE
        tvErroSenha.visibility = View.GONE

        var senhaVisivel = false
        var confirmarSenhaVisivel = false

        //Mostrar Senha
        bntOlhoSenha.setOnClickListener {

            if (senhaVisivel) {

                // ESCONDER
                etSenha.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

                bntOlhoSenha.setImageResource(R.drawable.ic_eye_closed)

                senhaVisivel = false

            } else {

                // MOSTRAR
                etSenha.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                bntOlhoSenha.setImageResource(R.drawable.ic_eye_open)

                senhaVisivel = true
            }

            etSenha.setSelection(etSenha.text.length)
        }

        bntOlhoConfirmarSenha.setOnClickListener {

            if (confirmarSenhaVisivel) {

                // ESCONDER
                etConfirmaSenha.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

                bntOlhoConfirmarSenha.setImageResource(R.drawable.ic_eye_closed)

                confirmarSenhaVisivel = false

            } else {

                // MOSTRAR
                etConfirmaSenha.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                bntOlhoConfirmarSenha.setImageResource(R.drawable.ic_eye_open)

                confirmarSenhaVisivel = true
            }

            etConfirmaSenha.setSelection(etConfirmaSenha.text.length)
        }


    }

    private fun validarECadastrar() {
        val email = etEmail.text.toString().trim()
        val senha = etSenha.text.toString()
        val confirmaSenha = etConfirmaSenha.text.toString()

        var valido = true
        tvErroEmail.visibility = View.GONE
        tvErroSenha.visibility = View.GONE

        // Validar E-mail
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvErroEmail.text = "E-mail inválido"
            tvErroEmail.visibility = View.VISIBLE
            valido = false
        }

        // Validar Senha (8 caracteres, 1 número, 1 maiúscula)
        val senhaRegex = "^(?=.*[0-9])(?=.*[A-Z]).{8,}$".toRegex()
        if (!senhaRegex.matches(senha)) {
            tvErroSenha.visibility = View.VISIBLE
            valido = false
        } else if (senha != confirmaSenha) {
            tvErroSenha.text = "As senhas não coincidem"
            tvErroSenha.visibility = View.VISIBLE
            valido = false
        }

        if (valido) {
            lifecycleScope.launch {
                val usuarioExistente = db.usuarioDao().buscarPorEmail(email)
                if (usuarioExistente != null) {
                    tvErroEmail.text = "E-mail já cadastrado"
                    tvErroEmail.visibility = View.VISIBLE
                } else {
                    db.usuarioDao().inserir(
                        Usuario(
                            nome = etNome.text.toString(),
                            usuario = etUsuario.text.toString(),
                            email = email,
                            senha = senha
                        )
                    )
                    mostrarPopupSucesso()
                }
            }
        }
    }

    private fun mostrarPopupSucesso() {
        println("POP UP ABRIU")

        val dialog = android.app.Dialog(this@TelaRF04CadastroNovoUsuario)

        dialog.setContentView(R.layout.popup_sucesso_cadastro)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // BOTÃO DO POPUP
        val botaoRetornar =
            dialog.findViewById<MaterialButton>(R.id.btnRetorneLogin)

        botaoRetornar.setOnClickListener {

            val intent = Intent(this@TelaRF04CadastroNovoUsuario, TelaRF03LoginAluno::class.java)

            startActivity(intent)

            dialog.dismiss()

            finish()
        }

        dialog.show()
    }

    private fun irParaLogin() {
        val intent = Intent(this@TelaRF04CadastroNovoUsuario, TelaRF03LoginAluno::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}