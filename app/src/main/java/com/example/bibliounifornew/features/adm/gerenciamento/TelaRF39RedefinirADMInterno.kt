package com.example.bibliounifornew.features.adm.gerenciamento

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException

class TelaRF39RedefinirADMInterno : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf39_redefinir_adm_interno)

        val etSenha            = findViewById<EditText>(R.id.editTextTextPassword)
        val etSenhaConfirmacao = findViewById<EditText>(R.id.editTextTextPasswordConfirmacao)
        // BUG-D1 FIX: XML declara <ImageView>, não <TextView> — cast corrigido
        val btnX               = findViewById<ImageView>(R.id.buttonX)
        val btnSalvar          = findViewById<MaterialButton>(R.id.buttonSalvar)

        val iconOlhoSenha            = findViewById<ImageView>(R.id.iconOlhoSenha)
        val iconOlhoSenhaConfirmacao = findViewById<ImageView>(R.id.iconOlhoSenhaConfirmacao)

        // BUG-D2 FIX: email real do admin — nunca hardcoded
        val textViewUserEmail = findViewById<TextView>(R.id.textViewUserEmail)
        textViewUserEmail?.text = auth.currentUser?.email ?: ""

        // TextViews de validação dinâmica
        val tvErroMin       = findViewById<TextView>(R.id.tvErroMinCaracteres)
        val tvErroNum       = findViewById<TextView>(R.id.tvErroNumero)
        val tvErroMaiusc    = findViewById<TextView>(R.id.tvErroMaiuscula)
        val tvErroIgual     = findViewById<TextView>(R.id.tvErroIgualAntiga)
        val tvErroDiferente = findViewById<TextView>(R.id.textErroDiferente)

        // Esconde erro "igual à antiga" — não temos senha atual para comparar aqui
        tvErroIgual?.visibility = View.GONE

        // ── Olhos ─────────────────────────────────────────────────────────────
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
                val pass    = etSenha.text.toString()
                val confirm = etSenhaConfirmacao.text.toString()

                val temMinimo    = pass.length >= 8
                val temNumero    = pass.any { it.isDigit() }
                val temMaiuscula = pass.any { it.isUpperCase() }
                val coincidem    = pass == confirm && pass.isNotEmpty()

                tvErroMin?.visibility    = if (!temMinimo    && pass.isNotEmpty()) View.VISIBLE else View.GONE
                tvErroNum?.visibility    = if (!temNumero    && pass.isNotEmpty()) View.VISIBLE else View.GONE
                tvErroMaiusc?.visibility = if (!temMaiuscula && pass.isNotEmpty()) View.VISIBLE else View.GONE
                tvErroDiferente?.visibility = if (!coincidem && confirm.isNotEmpty()) View.VISIBLE else View.GONE

                val isValid = temMinimo && temNumero && temMaiuscula && coincidem
                btnSalvar.isEnabled = isValid
                btnSalvar.alpha     = if (isValid) 1.0f else 0.5f
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etSenha.addTextChangedListener(watcher)
        etSenhaConfirmacao.addTextChangedListener(watcher)

        // ── Botão X (fechar) ──────────────────────────────────────────────────
        btnX?.setOnClickListener { finish() }

        // ── Salvar — chama updatePassword() real ──────────────────────────────
        btnSalvar.setOnClickListener {
            val novaSenha   = etSenha.text.toString()
            val currentUser = auth.currentUser

            if (currentUser == null) {
                Toast.makeText(this, getString(R.string.erro_sessao_expirada), Toast.LENGTH_SHORT).show()
                finish()
                return@setOnClickListener
            }

            btnSalvar.isEnabled = false

            currentUser.updatePassword(novaSenha)
                .addOnSuccessListener {
                    btnSalvar.isEnabled = true
                    exibirPopupSucesso()
                }
                .addOnFailureListener { e ->
                    btnSalvar.isEnabled = true
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
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Popup de sucesso
    // ─────────────────────────────────────────────────────────────────────────
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
