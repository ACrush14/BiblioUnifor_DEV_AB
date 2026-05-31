package com.example.bibliounifornew.login

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.features.usuario.biblioteca.TelaRF08DashboardUsuario
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF03LoginAluno : AppCompatActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf03_loginaluno)

        val imageLogo = findViewById<ImageView>(R.id.imageLogoLogin)
        carregarLogoSegura(imageLogo)

        val email = findViewById<EditText>(R.id.editEmail)
        val senha = findViewById<EditText>(R.id.editSenha)
        val botaoEntrar = findViewById<Button>(R.id.buttonEntrar)
        val mostrarSenha = findViewById<ImageView>(R.id.iconOlhoSenha)
        val erro = findViewById<TextView>(R.id.textErroLogin)
        val criarConta = findViewById<TextView>(R.id.textCriarConta)
        val esqueceuSenha = findViewById<TextView>(R.id.textEsqueceuSenha)

        erro.visibility = View.GONE

        // ----------------------------------------------------
        // CLIQUE: LOGIN EMAIL E SENHA
        // ----------------------------------------------------
        botaoEntrar.setOnClickListener {
            val textoEmail = email.text.toString().trim()
            val textoSenha = senha.text.toString().trim()

            erro.visibility = View.GONE

            if (textoEmail.isEmpty() || textoSenha.isEmpty()) {
                erro.text = "Preencha todos os campos"
                erro.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // UX: Desativa o botão enquanto carrega
            botaoEntrar.isEnabled = false
            botaoEntrar.text = "Entrando..."

            // Login Real via Firebase
            authRepository.loginUsuario(textoEmail, textoSenha) { sucesso, mensagemOuUid ->
                if (!sucesso) {
                    botaoEntrar.isEnabled = true
                    botaoEntrar.text = "Entrar"
                    erro.text = "E-mail ou senha incorretos"
                    erro.visibility = View.VISIBLE
                    return@loginUsuario
                }

                // RACE CONDITION FIX: usa o UID capturado no exato momento do
                // addOnCompleteListener do Auth, em vez de re-consultar currentUser.
                // Se currentUser fosse lido aqui (num frame posterior), poderia
                // retornar null caso o estado Auth ainda não tivesse propagado.
                val uid = mensagemOuUid ?: run {
                    botaoEntrar.isEnabled = true
                    botaoEntrar.text = "Entrar"
                    erro.text = "Erro ao obter sessão. Tente novamente."
                    erro.visibility = View.VISIBLE
                    return@loginUsuario
                }

                FirebaseFirestore.getInstance()
                    .collection("usuarios").document(uid).get()
                    .addOnSuccessListener { doc ->
                        botaoEntrar.isEnabled = true
                        botaoEntrar.text = "Entrar"

                        // Guard: documento inexistente no Firestore (ex: Auth criado mas
                        // Firestore falhou no cadastro). Permite acesso e loga diagnóstico.
                        if (!doc.exists()) {
                            Log.w("LoginAluno", "Documento do usuário não encontrado no " +
                                "Firestore (uid=$uid). Liberando acesso sem checagem de status.")
                            navegarParaDashboard()
                            return@addOnSuccessListener
                        }

                        // GAP-1 GATE: conta desativada pelo ADM.
                        // lerCampoBooleano() tolera Boolean nativo E String legada
                        // ("false", "inativo"), eliminando bugs de case sensitivity.
                        if (!lerCampoBooleano(doc, "contaAtiva")) {
                            Log.w("LoginAluno", "Bloqueio GAP-1: contaAtiva=false (uid=$uid)")
                            FirebaseAuth.getInstance().signOut()
                            erro.text = "Sua conta foi desativada. Entre em contato com a biblioteca."
                            erro.visibility = View.VISIBLE
                            return@addOnSuccessListener
                        }

                        // RF33 GATE: cadastro aguardando aprovação do ADM.
                        // Documentos sem o campo assumem confirmado=true (retrocompatibilidade).
                        if (!lerCampoBooleano(doc, "cadastroConfirmado")) {
                            Log.w("LoginAluno", "Bloqueio RF33: cadastroConfirmado=false (uid=$uid)")
                            FirebaseAuth.getInstance().signOut()
                            erro.text = "Cadastro aguardando aprovação da biblioteca. Tente novamente em breve."
                            erro.visibility = View.VISIBLE
                            return@addOnSuccessListener
                        }

                        navegarParaDashboard()
                    }
                    .addOnFailureListener { e ->
                        // Falha na leitura do Firestore → bloqueia por segurança.
                        // Sem confirmar o status da conta, não libera o dashboard.
                        Log.e("LoginAluno", "Falha ao ler documento do usuário (uid=$uid): ${e.message}")
                        FirebaseAuth.getInstance().signOut()
                        botaoEntrar.isEnabled = true
                        botaoEntrar.text = "Entrar"
                        erro.text = "Erro de conexão. Verifique sua internet e tente novamente."
                        erro.visibility = View.VISIBLE
                    }
            }
        }

        // ----------------------------------------------------
        // NAVEGAÇÃO SECUNDÁRIA
        // ----------------------------------------------------
        criarConta.setOnClickListener {
            startActivity(Intent(this, TelaRF04CadastroNovoUsuario::class.java))
        }

        esqueceuSenha.setOnClickListener {
            startActivity(Intent(this, TelaRF05RecuperacaoSenha::class.java))
        }

        // UX MELHORADA (remove erro ao focar)
        email.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) erro.visibility = View.GONE }
        senha.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) erro.visibility = View.GONE }

        var senhaVisivel = false
        mostrarSenha.setOnClickListener {
            if (senhaVisivel) {
                senha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                mostrarSenha.setImageResource(R.drawable.ic_eye_closed)
            } else {
                senha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                mostrarSenha.setImageResource(R.drawable.ic_eye_open)
            }
            senhaVisivel = !senhaVisivel
            senha.setSelection(senha.text.length)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS DE LOGIN
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Navega para o Dashboard do aluno limpando a back-stack de login.
     * Extraído para evitar duplicação entre os dois caminhos de sucesso
     * (doc existe vs. doc ausente).
     */
    private fun navegarParaDashboard() {
        Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, TelaRF08DashboardUsuario::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Lê um campo booleano do Firestore de forma tolerante a tipos.
     *
     * Problema resolvido: `getBoolean()` retorna null para campos armazenados
     * como String (ex: documentos criados por versões antigas do app ou por
     * scripts externos), fazendo o `?: true` padrão ignorar um "false" legítimo.
     *
     * Prioridade de leitura:
     *   1. Boolean nativo → retorna diretamente.
     *   2. String → converte case-insensitive:
     *        "false" | "inativo" | "desativado" | "pendente" | "0"  → false
     *        "true"  | "ativo"   | "confirmado"               | "1"  → true
     *   3. Campo ausente → retorna [defaultSeAusente] (padrão: true),
     *      mantendo retrocompatibilidade com documentos sem o campo.
     */
    private fun lerCampoBooleano(
        doc: DocumentSnapshot,
        campo: String,
        defaultSeAusente: Boolean = true
    ): Boolean {
        doc.getBoolean(campo)?.let { return it }
        return when (doc.getString(campo)?.trim()?.lowercase()) {
            "false", "inativo", "desativado", "pendente", "0" -> false
            "true",  "ativo",   "confirmado",             "1" -> true
            else -> defaultSeAusente
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