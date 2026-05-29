package com.example.bibliounifornew.features.adm.gerenciamento

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF23LoginADM
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException

class TelaRF39RedefinirADMInterno : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf39_redefinir_adm_interno)

        val etSenhaAtual       = findViewById<EditText>(R.id.editTextSenhaAtual)
        val etSenha            = findViewById<EditText>(R.id.editTextTextPassword)
        val etSenhaConfirmacao = findViewById<EditText>(R.id.editTextTextPasswordConfirmacao)
        val btnX               = findViewById<ImageView>(R.id.buttonX)
        val btnSalvar          = findViewById<MaterialButton>(R.id.buttonSalvar)

        val iconOlhoSenhaAtual       = findViewById<ImageView>(R.id.iconOlhoSenhaAtual)
        val iconOlhoSenha            = findViewById<ImageView>(R.id.iconOlhoSenha)
        val iconOlhoSenhaConfirmacao = findViewById<ImageView>(R.id.iconOlhoSenhaConfirmacao)

        val textViewUserEmail = findViewById<TextView>(R.id.textViewUserEmail)
        textViewUserEmail?.text = auth.currentUser?.email ?: ""

        // TextViews de validação dinâmica
        val tvErroSenhaAtual = findViewById<TextView>(R.id.tvErroSenhaAtual)
        val tvErroMin       = findViewById<TextView>(R.id.tvErroMinCaracteres)
        val tvErroNum       = findViewById<TextView>(R.id.tvErroNumero)
        val tvErroMaiusc    = findViewById<TextView>(R.id.tvErroMaiuscula)
        val tvErroIgual     = findViewById<TextView>(R.id.tvErroIgualAntiga)
        val tvErroDiferente = findViewById<TextView>(R.id.textErroDiferente)

        // ── Olhos ─────────────────────────────────────────────────────────────
        var senhaAtualVisivel = false
        iconOlhoSenhaAtual?.setOnClickListener {
            senhaAtualVisivel = !senhaAtualVisivel
            if (senhaAtualVisivel) {
                etSenhaAtual.transformationMethod = HideReturnsTransformationMethod.getInstance()
                iconOlhoSenhaAtual.setImageResource(R.drawable.ic_eye_open)
            } else {
                etSenhaAtual.transformationMethod = PasswordTransformationMethod.getInstance()
                iconOlhoSenhaAtual.setImageResource(R.drawable.ic_eye_closed)
            }
            etSenhaAtual.setSelection(etSenhaAtual.text.length)
        }

        var senhaVisivel = false
        iconOlhoSenha?.setOnClickListener {
            senhaVisivel = !senhaVisivel
            if (senhaVisivel) {
                etSenha.transformationMethod = HideReturnsTransformationMethod.getInstance()
                iconOlhoSenha.setImageResource(R.drawable.ic_eye_open)
            } else {
                etSenha.transformationMethod = PasswordTransformationMethod.getInstance()
                iconOlhoSenha.setImageResource(R.drawable.ic_eye_closed)
            }
            etSenha.setSelection(etSenha.text.length)
        }

        var senhaConfirmacaoVisivel = false
        iconOlhoSenhaConfirmacao?.setOnClickListener {
            senhaConfirmacaoVisivel = !senhaConfirmacaoVisivel
            if (senhaConfirmacaoVisivel) {
                etSenhaConfirmacao.transformationMethod = HideReturnsTransformationMethod.getInstance()
                iconOlhoSenhaConfirmacao.setImageResource(R.drawable.ic_eye_open)
            } else {
                etSenhaConfirmacao.transformationMethod = PasswordTransformationMethod.getInstance()
                iconOlhoSenhaConfirmacao.setImageResource(R.drawable.ic_eye_closed)
            }
            etSenhaConfirmacao.setSelection(etSenhaConfirmacao.text.length)
        }

        // ── Validação dinâmica ────────────────────────────────────────────────
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val current = etSenhaAtual.text.toString()
                val pass    = etSenha.text.toString()
                val confirm = etSenhaConfirmacao.text.toString()

                val temMinimo    = pass.length >= 8
                val temNumero    = pass.any { it.isDigit() }
                val temMaiuscula = pass.any { it.isUpperCase() }
                val coincidem    = pass == confirm && pass.isNotEmpty()
                val diferenteDaAtual = pass != current || pass.isEmpty()

                tvErroSenhaAtual?.visibility = View.GONE
                tvErroMin?.visibility    = if (!temMinimo    && pass.isNotEmpty()) View.VISIBLE else View.GONE
                tvErroNum?.visibility    = if (!temNumero    && pass.isNotEmpty()) View.VISIBLE else View.GONE
                tvErroMaiusc?.visibility = if (!temMaiuscula && pass.isNotEmpty()) View.VISIBLE else View.GONE
                tvErroIgual?.visibility  = if (!diferenteDaAtual && pass.isNotEmpty()) View.VISIBLE else View.GONE
                tvErroDiferente?.visibility = if (!coincidem && confirm.isNotEmpty()) View.VISIBLE else View.GONE

                val isValid = current.isNotEmpty() && temMinimo && temNumero && temMaiuscula && coincidem && diferenteDaAtual
                btnSalvar.isEnabled = isValid
                btnSalvar.alpha     = if (isValid) 1.0f else 0.5f
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etSenhaAtual.addTextChangedListener(watcher)
        etSenha.addTextChangedListener(watcher)
        etSenhaConfirmacao.addTextChangedListener(watcher)

        // ── Botão X (fechar) ──────────────────────────────────────────────────
        btnX?.setOnClickListener { finish() }

        // ── Salvar ───────────────────────────────────────────────────────────
        btnSalvar.setOnClickListener {
            val senhaAtual  = etSenhaAtual.text.toString()
            val novaSenha   = etSenha.text.toString()
            val currentUser = auth.currentUser

            if (currentUser == null || currentUser.email == null) {
                Toast.makeText(this, getString(R.string.erro_sessao_expirada), Toast.LENGTH_SHORT).show()
                finish()
                return@setOnClickListener
            }

            btnSalvar.isEnabled = false
            btnSalvar.text = getString(R.string.msg_salvando)

            // 1. Reautenticar
            val credential = EmailAuthProvider.getCredential(currentUser.email!!, senhaAtual)
            currentUser.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (isFinishing || isDestroyed) return@addOnCompleteListener

                    if (reauthTask.isSuccessful) {
                        // 2. Atualizar senha
                        currentUser.updatePassword(novaSenha)
                            .addOnCompleteListener { updateTask ->
                                if (isFinishing || isDestroyed) return@addOnCompleteListener
                                
                                btnSalvar.isEnabled = true
                                btnSalvar.text = getString(R.string.btn_salvar_alteracoes)

                                if (updateTask.isSuccessful) {
                                    exibirPopupSucesso()
                                } else {
                                    val e = updateTask.exception
                                    if (e is FirebaseAuthRecentLoginRequiredException) {
                                        Toast.makeText(this, getString(R.string.msg_sessao_expirada_senha), Toast.LENGTH_LONG).show()
                                        auth.signOut()
                                        val intent = Intent(this, TelaRF23LoginADM::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this, getString(R.string.erro_alterar_senha), Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                    } else {
                        btnSalvar.isEnabled = true
                        btnSalvar.text = getString(R.string.btn_salvar_alteracoes)
                        
                        val exception = reauthTask.exception
                        if (exception is FirebaseAuthInvalidCredentialsException) {
                            tvErroSenhaAtual?.text = getString(R.string.erro_senha_incorreta)
                            tvErroSenhaAtual?.visibility = View.VISIBLE
                        } else {
                            Toast.makeText(this, getString(R.string.erro_generico), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }

    private fun exibirPopupSucesso() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_alteracoes_salvas_sucesso)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        dialog.findViewById<MaterialButton>(R.id.buttonVoltarPopup)?.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, TelaRF38ConfigADM::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        dialog.show()

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window?.attributes)
        lp.width  = (resources.displayMetrics.widthPixels * 0.90).toInt()
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = lp
    }
}
