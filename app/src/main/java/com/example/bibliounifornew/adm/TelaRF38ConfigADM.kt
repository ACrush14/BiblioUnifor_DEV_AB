package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.login.TelaRF01BemVindo
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class TelaRF38ConfigADM : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf38_config_adm)

        val editNome    = findViewById<EditText>(R.id.editNomeAdm)
        val editUsuario = findViewById<EditText>(R.id.editUsuarioAdm)

        val btnSalvar        = findViewById<MaterialButton>(R.id.btnSalvarAlteracoes)
        val btnRedefinirSenha = findViewById<MaterialButton>(R.id.btnRedefinirSenha)
        val btnApagarConta   = findViewById<MaterialButton>(R.id.btnApagarConta)

        // ── Olho para campo senha (decorativo, desabilitado) ──────────────────
        val editSenhaAtual  = findViewById<EditText>(R.id.editSenhaAtual)
        val iconOlhoSenha   = findViewById<ImageView>(R.id.iconOlhoSenhaAtual)
        editSenhaAtual.isEnabled = false

        var senhaVisivel = false
        iconOlhoSenha.setOnClickListener {
            senhaVisivel = !senhaVisivel
            editSenhaAtual.inputType = if (senhaVisivel)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            iconOlhoSenha.setImageResource(if (senhaVisivel) R.drawable.ic_eye_open else R.drawable.ic_eye_closed)
            editSenhaAtual.setSelection(editSenhaAtual.text.length)
        }

        // ── Carrega dados do perfil ───────────────────────────────────────────
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { doc ->
                    editNome.setText(doc.getString("nome")    ?: "")
                    editUsuario.setText(doc.getString("usuario") ?: "")
                }
        }

        // ── Salvar alterações de perfil ───────────────────────────────────────
        btnSalvar?.setOnClickListener {
            val novoNome    = editNome.text.toString().trim()
            val novoUsuario = editUsuario.text.toString().trim()

            if (novoNome.isEmpty() || novoUsuario.isEmpty()) {
                Toast.makeText(this, "Preencha nome e usuário.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (uid == null) {
                Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSalvar.isEnabled = false
            db.collection("usuarios").document(uid)
                .set(mapOf("nome" to novoNome, "usuario" to novoUsuario), SetOptions.merge())
                .addOnSuccessListener {
                    btnSalvar.isEnabled = true
                    Toast.makeText(this, "Alterações salvas com sucesso!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    btnSalvar.isEnabled = true
                    Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // ── Redefinir senha: abre tela interna ───────────────────────────────
        btnRedefinirSenha?.setOnClickListener {
            startActivity(Intent(this, TelaRF39RedefinirADMInterno::class.java))
        }

        // ── Apagar conta ──────────────────────────────────────────────────────
        btnApagarConta?.setOnClickListener {
            exibirPopupApagarConta(uid)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POPUP APAGAR CONTA — re-autentica antes de deletar
    // ─────────────────────────────────────────────────────────────────────────
    private fun exibirPopupApagarConta(uid: String?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_apagar_conta_adm)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnConfirmar = dialog.findViewById<MaterialButton>(R.id.buttonConfirmarApagarContaADM)
        val btnCancelar  = dialog.findViewById<MaterialButton>(R.id.buttonCancelarApagarContaADM)
        val editSenha    = dialog.findViewById<TextInputEditText>(R.id.editSenhaApagarContaADM)

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnConfirmar.setOnClickListener {
            val senha = editSenha.text.toString()
            if (senha.isEmpty()) {
                Toast.makeText(this, "Digite sua senha para confirmar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            val email       = currentUser?.email

            if (currentUser == null || email.isNullOrEmpty()) {
                Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }

            btnConfirmar.isEnabled = false

            // ── 1. Re-autenticar ──────────────────────────────────────────────
            val credencial = EmailAuthProvider.getCredential(email, senha)
            currentUser.reauthenticate(credencial)
                .addOnSuccessListener {
                    // ── 2. Deletar documento Firestore ────────────────────────
                    val firestoreDelete = if (!uid.isNullOrEmpty()) {
                        db.collection("usuarios").document(uid).delete()
                    } else null

                    // ── 3. Deletar usuário Firebase Auth ──────────────────────
                    currentUser.delete()
                        .addOnSuccessListener {
                            firestoreDelete?.addOnFailureListener { /* silencia erro de limpeza */ }
                            dialog.dismiss()
                            Toast.makeText(this, "Conta apagada com sucesso.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, TelaRF01BemVindo::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            btnConfirmar.isEnabled = true
                            Toast.makeText(this, "Erro ao apagar conta: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    btnConfirmar.isEnabled = true
                    Toast.makeText(this, "Senha incorreta ou sessão expirada.", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width  = (resources.displayMetrics.widthPixels * 0.90).toInt()
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = layoutParams
    }
}
