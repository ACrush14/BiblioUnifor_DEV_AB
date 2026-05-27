package com.example.bibliounifornew.features.usuario.perfil

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF03LoginAluno
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException

class TelaRF10RedefinirSenha : AppCompatActivity() {

    private var novaSenhaVisivel = false
    private var confirmaSenhaVisivel = false
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf10_redefinirsenha)

        // CAMPOS
        val editNovaSenha = findViewById<EditText>(R.id.editNovaSenha)
        val editConfirmarSenha = findViewById<EditText>(R.id.editConfirmarSenha)
        val btnSalvar = findViewById<MaterialButton>(R.id.buttonSalvarAlteracoes)

        // ERROS
        val textErroNovaSenha = findViewById<TextView>(R.id.textErroNovaSenha)
        val textErroConfirmacao = findViewById<TextView>(R.id.textErroConfirmacao)
        val textErroSenhaAntiga = findViewById<TextView>(R.id.textErroSenhaAntiga)
        val textRegrasSenha = findViewById<TextView>(R.id.textRegrasSenha)
        val textErroSenhas = findViewById<TextView>(R.id.textErroSenhas)

        // ÍCONES
        val iconOlhoNova = findViewById<ImageView>(R.id.iconOlhoNovaSenha)
        val iconOlhoConfirmar = findViewById<ImageView>(R.id.iconOlhoConfirmar)

        // --- LÓGICA DO OLHO (NOVA SENHA) ---
        iconOlhoNova.setOnClickListener {
            novaSenhaVisivel = !novaSenhaVisivel
            if (novaSenhaVisivel) {
                editNovaSenha.transformationMethod = HideReturnsTransformationMethod.getInstance()
                iconOlhoNova.setImageResource(R.drawable.ic_eye_open)
            } else {
                editNovaSenha.transformationMethod = PasswordTransformationMethod.getInstance()
                iconOlhoNova.setImageResource(R.drawable.ic_eye_closed)
            }
            editNovaSenha.setSelection(editNovaSenha.text.length)
        }

        // --- LÓGICA DO OLHO (CONFIRMAR SENHA) ---
        iconOlhoConfirmar.setOnClickListener {
            confirmaSenhaVisivel = !confirmaSenhaVisivel
            if (confirmaSenhaVisivel) {
                editConfirmarSenha.transformationMethod = HideReturnsTransformationMethod.getInstance()
                iconOlhoConfirmar.setImageResource(R.drawable.ic_eye_open)
            } else {
                editConfirmarSenha.transformationMethod = PasswordTransformationMethod.getInstance()
                iconOlhoConfirmar.setImageResource(R.drawable.ic_eye_closed)
            }
            editConfirmarSenha.setSelection(editConfirmarSenha.text.length)
        }

        // --- LIMPAR ERROS AO DIGITAR ---
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textErroNovaSenha.visibility = View.GONE
                textErroConfirmacao.visibility = View.GONE
                textErroSenhaAntiga.visibility = View.GONE
                textRegrasSenha.visibility = View.GONE
                textErroSenhas.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        editNovaSenha.addTextChangedListener(watcher)
        editConfirmarSenha.addTextChangedListener(watcher)

        // --- BOTÃO SALVAR ---
        btnSalvar.setOnClickListener {
            val novaSenha = editNovaSenha.text.toString()
            val confirma = editConfirmarSenha.text.toString()

            // Esconder todos antes de validar
            textErroNovaSenha.visibility = View.GONE
            textErroConfirmacao.visibility = View.GONE
            textErroSenhaAntiga.visibility = View.GONE
            textRegrasSenha.visibility = View.GONE
            textErroSenhas.visibility = View.GONE

            when {
                novaSenha.isEmpty() -> {
                    textErroNovaSenha.text = "Campo obrigatório"
                    textErroNovaSenha.visibility = View.VISIBLE
                }
                confirma.isEmpty() -> {
                    textErroConfirmacao.text = "Campo obrigatório"
                    textErroConfirmacao.visibility = View.VISIBLE
                }
                novaSenha.length < 8 -> {
                    textRegrasSenha.text = "A senha deve conter pelo menos 8 caracteres"
                    textRegrasSenha.visibility = View.VISIBLE
                }
                !novaSenha.any { it.isDigit() } -> {
                    textRegrasSenha.text = "A senha deve conter pelo menos um número"
                    textRegrasSenha.visibility = View.VISIBLE
                }
                !novaSenha.any { it.isUpperCase() } -> {
                    textRegrasSenha.text = "A senha deve conter pelo menos uma letra maiúscula"
                    textRegrasSenha.visibility = View.VISIBLE
                }
                novaSenha != confirma -> {
                    textErroSenhas.visibility = View.VISIBLE
                }
                else -> {
                    btnSalvar.isEnabled = false
                    btnSalvar.text = "Salvando..."
                    
                    val user = auth.currentUser
                    user?.updatePassword(novaSenha)
                        ?.addOnCompleteListener { task ->
                            btnSalvar.isEnabled = true
                            btnSalvar.text = "Salvar Alterações"
                            
                            if (task.isSuccessful) {
                                // SUCESSO - MOSTRAR POPUP
                                exibirPopupSucesso()
                            } else {
                                val exception = task.exception
                                if (exception is FirebaseAuthRecentLoginRequiredException) {
                                    Toast.makeText(this, "Por segurança, faça login novamente para alterar a senha.", Toast.LENGTH_LONG).show()
                                    auth.signOut()
                                    val intent = Intent(this@TelaRF10RedefinirSenha, TelaRF03LoginAluno::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Erro ao atualizar senha: ${exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                }
            }
        }
    }

    private fun exibirPopupSucesso() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_sucesso_redefinir_senha)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)

        val btnVoltar = dialog.findViewById<Button>(R.id.buttonRetornarLogin)
        btnVoltar.text = "Voltar às Configurações"
        btnVoltar.setOnClickListener {
            dialog.dismiss()
            finish() // Fecha TelaRF10 e volta para RF09
        }

        dialog.show()
    }
}
