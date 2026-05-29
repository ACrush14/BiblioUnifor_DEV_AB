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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException

class TelaRF10RedefinirSenha : AppCompatActivity() {

    private var senhaAtualVisivel = false
    private var novaSenhaVisivel = false
    private var confirmaSenhaVisivel = false
    private val auth = FirebaseAuth.getInstance()
    private var activeDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf10_redefinirsenha)

        // HEADER - SET EMAIL
        val textEmailUsuario = findViewById<TextView>(R.id.textEmailUsuario)
        val user = auth.currentUser
        textEmailUsuario.text = user?.email ?: "Email não disponível"

        // CAMPOS
        val editSenhaAtual = findViewById<EditText>(R.id.editSenhaAtual)
        val editNovaSenha = findViewById<EditText>(R.id.editNovaSenha)
        val editConfirmarSenha = findViewById<EditText>(R.id.editConfirmarSenha)
        val btnSalvar = findViewById<MaterialButton>(R.id.buttonSalvarAlteracoes)

        // ERROS
        val textErroSenhaAtual = findViewById<TextView>(R.id.textErroSenhaAtual)
        val textErroNovaSenha = findViewById<TextView>(R.id.textErroNovaSenha)
        val textErroConfirmacao = findViewById<TextView>(R.id.textErroConfirmacao)
        val textErroSenhaAntiga = findViewById<TextView>(R.id.textErroSenhaAntiga)
        val textRegrasSenha = findViewById<TextView>(R.id.textRegrasSenha)
        val textErroSenhas = findViewById<TextView>(R.id.textErroSenhas)

        // ÍCONES
        val iconOlhoSenhaAtual = findViewById<ImageView>(R.id.iconOlhoSenhaAtual)
        val iconOlhoNova = findViewById<ImageView>(R.id.iconOlhoNovaSenha)
        val iconOlhoConfirmar = findViewById<ImageView>(R.id.iconOlhoConfirmar)

        // --- LÓGICA DO OLHO (SENHA ATUAL) ---
        iconOlhoSenhaAtual.setOnClickListener {
            senhaAtualVisivel = !senhaAtualVisivel
            if (senhaAtualVisivel) {
                editSenhaAtual.transformationMethod = HideReturnsTransformationMethod.getInstance()
                iconOlhoSenhaAtual.setImageResource(R.drawable.ic_eye_open)
            } else {
                editSenhaAtual.transformationMethod = PasswordTransformationMethod.getInstance()
                iconOlhoSenhaAtual.setImageResource(R.drawable.ic_eye_closed)
            }
            editSenhaAtual.setSelection(editSenhaAtual.text.length)
        }

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
                textErroSenhaAtual.visibility = View.GONE
                textErroNovaSenha.visibility = View.GONE
                textErroConfirmacao.visibility = View.GONE
                textErroSenhaAntiga.visibility = View.GONE
                textRegrasSenha.visibility = View.GONE
                textErroSenhas.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        editSenhaAtual.addTextChangedListener(watcher)
        editNovaSenha.addTextChangedListener(watcher)
        editConfirmarSenha.addTextChangedListener(watcher)

        // --- BOTÃO SALVAR ---
        btnSalvar.setOnClickListener {
            val senhaAtual = editSenhaAtual.text.toString()
            val novaSenha = editNovaSenha.text.toString()
            val confirma = editConfirmarSenha.text.toString()

            // Esconder todos antes de validar
            textErroSenhaAtual.visibility = View.GONE
            textErroNovaSenha.visibility = View.GONE
            textErroConfirmacao.visibility = View.GONE
            textErroSenhaAntiga.visibility = View.GONE
            textRegrasSenha.visibility = View.GONE
            textErroSenhas.visibility = View.GONE

            when {
                senhaAtual.isEmpty() -> {
                    textErroSenhaAtual.text = getString(R.string.erro_campo)
                    textErroSenhaAtual.visibility = View.VISIBLE
                }
                novaSenha.isEmpty() -> {
                    textErroNovaSenha.text = getString(R.string.erro_campo)
                    textErroNovaSenha.visibility = View.VISIBLE
                }
                confirma.isEmpty() -> {
                    textErroConfirmacao.text = getString(R.string.erro_campo)
                    textErroConfirmacao.visibility = View.VISIBLE
                }
                novaSenha == senhaAtual -> {
                    textErroSenhaAntiga.text = getString(R.string.erro_senha_igual)
                    textErroSenhaAntiga.visibility = View.VISIBLE
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
                    textErroSenhas.text = getString(R.string.erro_senha_diferente)
                    textErroSenhas.visibility = View.VISIBLE
                }
                else -> {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null && firebaseUser.email != null) {
                        btnSalvar.isEnabled = false
                        btnSalvar.text = "Salvando..."

                        // 1. REAUTENTICAR
                        val credential = EmailAuthProvider.getCredential(firebaseUser.email!!, senhaAtual)
                        firebaseUser.reauthenticate(credential)
                            .addOnCompleteListener { reauthTask ->
                                if (isFinishing || isDestroyed) return@addOnCompleteListener
                                if (reauthTask.isSuccessful) {
                                    // 2. ATUALIZAR SENHA
                                    firebaseUser.updatePassword(novaSenha)
                                        .addOnCompleteListener { updateTask ->
                                            if (isFinishing || isDestroyed) return@addOnCompleteListener
                                            btnSalvar.isEnabled = true
                                            btnSalvar.text = "Salvar Alterações"

                                            if (updateTask.isSuccessful) {
                                                exibirPopupSucesso()
                                            } else {
                                                Toast.makeText(this, "Erro ao atualizar senha: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    btnSalvar.isEnabled = true
                                    btnSalvar.text = "Salvar Alterações"
                                    
                                    val exception = reauthTask.exception
                                    if (exception is FirebaseAuthInvalidCredentialsException) {
                                        textErroSenhaAtual.text = "Senha atual incorreta"
                                        textErroSenhaAtual.visibility = View.VISIBLE
                                    } else {
                                        Toast.makeText(this, "Erro na autenticação: ${exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                    } else {
                        Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun exibirPopupSucesso() {
        val dialog = Dialog(this)
        activeDialog = dialog
        dialog.setContentView(R.layout.popup_sucesso_redefinir_senha)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)

        val btnVoltar = dialog.findViewById<Button>(R.id.buttonRetornarLogin)
        btnVoltar.text = "Voltar às Configurações"
        btnVoltar.setOnClickListener {
            dialog.dismiss()
            finish() // Fecha TelaRF10 e volta para RF09
        }

        dialog.setOnDismissListener { activeDialog = null }
        dialog.show()
    }

    override fun onDestroy() {
        activeDialog?.dismiss()
        activeDialog = null
        super.onDestroy()
    }
}
