package com.example.bibliounifornew.login

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class TelaRF26NovaContaADM : AppCompatActivity() {

    private val authRepository = AuthRepository()
    private val db             = FirebaseFirestore.getInstance()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.telarf26_nova_conta_adm
        )



        //--------------------------------
        // CAMPOS
        //--------------------------------

        val nome =

            findViewById<EditText>(
                R.id.editNomeCompletoAdm
            )


        val usuario =

            findViewById<EditText>(
                R.id.editNomeUsuarioAdm
            )


        val email =

            findViewById<EditText>(
                R.id.editEmailAdmCadastro
            )


        val credencial =

            findViewById<EditText>(
                R.id.editCredencialAdmCadastro
            )


        val senha =

            findViewById<EditText>(
                R.id.editSenhaAdmCadastro
            )


        val confirma =

            findViewById<EditText>(
                R.id.editConfirmarSenhaAdm
            )



        //--------------------------------
        // ERROS
        //--------------------------------

        val erroEmail =
            findViewById<TextView>(
                R.id.textErroEmailAdmCadastro
            )


        val erroCredencial =
            findViewById<TextView>(
                R.id.textErroCredencialAdm
            )


        val erroSenha =
            findViewById<TextView>(
                R.id.textRegrasSenhaAdm
            )

        val erroSenha1 =
            findViewById<TextView>(
                R.id.textErroSenha1
            )

        val erroSenha2 =
            findViewById<TextView>(
                R.id.textErroSenha2
            )

        val erroSenhaDiferente =
            findViewById<TextView>(
                R.id.textErroSenhaDiferente
            )

        val erroSenhaIgual =
            findViewById<TextView>(
                R.id.textErroSenhaIgual
            )



        //--------------------------------
        // ESTADO INICIAL (OCULTAR ERROS)
        //--------------------------------

        erroEmail.visibility = View.GONE
        erroCredencial.visibility = View.GONE
        erroSenha.visibility = View.GONE
        erroSenha1.visibility = View.GONE
        erroSenha2.visibility = View.GONE
        erroSenhaDiferente.visibility = View.GONE
        erroSenhaIgual.visibility = View.GONE

        //--------------------------------
        // TEXT WATCHER (LIMPAR ERROS)
        //--------------------------------

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                erroEmail.visibility = View.GONE
                erroCredencial.visibility = View.GONE
                erroSenha.visibility = View.GONE
                erroSenha1.visibility = View.GONE
                erroSenha2.visibility = View.GONE
                erroSenhaDiferente.visibility = View.GONE
                erroSenhaIgual.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        nome.addTextChangedListener(watcher)
        usuario.addTextChangedListener(watcher)
        email.addTextChangedListener(watcher)
        credencial.addTextChangedListener(watcher)
        senha.addTextChangedListener(watcher)
        confirma.addTextChangedListener(watcher)

        //--------------------------------
        // BOTÃO
        //--------------------------------

        val criar =

            findViewById<Button>(
                R.id.buttonCriarContaAdm
            )


        val entrar =

            findViewById<TextView>(
                R.id.textEntreAquiAdm
            )



        //--------------------------------
        // OLHOS
        //--------------------------------

        val olho1 =
            findViewById<ImageView>(
                R.id.iconOlhoSenhaAdmCadastro
            )

        val olho2 =
            findViewById<ImageView>(
                R.id.iconOlhoConfirmarSenhaAdm
            )



        //--------------------------------
        // MOSTRAR SENHA
        //--------------------------------

        var v1=false
        var v2=false



        olho1.setOnClickListener{
            v1 = !v1
            if(v1){
                senha.transformationMethod = HideReturnsTransformationMethod.getInstance()
                olho1.setImageResource(R.drawable.ic_eye_open)
            } else {
                senha.transformationMethod = PasswordTransformationMethod.getInstance()
                olho1.setImageResource(R.drawable.ic_eye_closed)
            }
            senha.setSelection(senha.text.length)
        }

        olho2.setOnClickListener{
            v2 = !v2
            if(v2){
                confirma.transformationMethod = HideReturnsTransformationMethod.getInstance()
                olho2.setImageResource(R.drawable.ic_eye_open)
            } else {
                confirma.transformationMethod = PasswordTransformationMethod.getInstance()
                olho2.setImageResource(R.drawable.ic_eye_closed)
            }
            confirma.setSelection(confirma.text.length)
        }



        //--------------------------------
        // CRIAR CONTA
        //--------------------------------

        criar.setOnClickListener{

            erroEmail.visibility = View.GONE
            erroCredencial.visibility = View.GONE
            erroSenha.visibility = View.GONE
            erroSenha1.visibility = View.GONE
            erroSenha2.visibility = View.GONE
            erroSenhaDiferente.visibility = View.GONE
            erroSenhaIgual.visibility = View.GONE

            val sNome = nome.text.toString().trim()
            val sUsuario = usuario.text.toString().trim()
            val sEmail = email.text.toString().trim()
            val sCredencial = credencial.text.toString().trim()
            val sSenha = senha.text.toString()
            val sConfirma = confirma.text.toString()

            // ── Validações síncronas (sem I/O) ───────────────────────────────
            when {
                sNome.isEmpty() || sUsuario.isEmpty() || sEmail.isEmpty() || sCredencial.isEmpty() -> {
                    Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(sEmail).matches() -> {
                    erroEmail.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                sSenha.isEmpty() -> {
                    erroSenha1.text = "Campo obrigatório"
                    erroSenha1.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                sConfirma.isEmpty() -> {
                    erroSenha2.text = "Campo obrigatório"
                    erroSenha2.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                sSenha.length < 8 -> {
                    erroSenha.text = "A senha deve conter pelo menos 8 caracteres"
                    erroSenha.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                !sSenha.any { it.isDigit() } -> {
                    erroSenha.text = "Um número"
                    erroSenha.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                !sSenha.any { it.isUpperCase() } -> {
                    erroSenha.text = "Uma letra maiúscula"
                    erroSenha.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                sSenha != sConfirma -> {
                    erroSenhaDiferente.visibility = View.VISIBLE
                    return@setOnClickListener
                }
            }

            // ── Validação assíncrona do código de convite ─────────────────────
            // O código vive em Firestore (configuracoes/adm → codigoConvite).
            // Pode ser rotacionado pelo time sem rebuild do APK.
            // Para criar o documento pela primeira vez:
            //   Firebase Console > Firestore > configuracoes > adm > codigoConvite: "SuaSenhaForte"
            criar.isEnabled = false
            criar.text = "Verificando credencial..."
            erroCredencial.visibility = View.GONE

            db.collection("configuracoes").document("adm")
                .get()
                .addOnSuccessListener { doc ->
                    val codigoValido = doc.getString("codigoConvite") ?: ""

                    if (codigoValido.isEmpty() || !sCredencial.equals(codigoValido, ignoreCase = false)) {
                        // Código errado ou documento inexistente no Firestore
                        criar.isEnabled = true
                        criar.text = "Criar Conta ADM"
                        erroCredencial.visibility = View.VISIBLE
                        return@addOnSuccessListener
                    }

                    // Código válido — prossegue com criação de conta
                    criar.text = "Criando..."
                    authRepository.registrarUsuario(sEmail, sSenha) { sucesso, uidOuErro ->
                        runOnUiThread {
                            if (sucesso && uidOuErro != null) {
                                val perfil = hashMapOf(
                                    "nome"               to sNome,
                                    "usuario"            to sUsuario,
                                    "email"              to sEmail,
                                    "role"               to "adm",
                                    "cadastroConfirmado" to false,
                                    "criadoEm"           to System.currentTimeMillis()
                                )
                                db.collection("administradores").document(uidOuErro)
                                    .set(perfil, SetOptions.merge())
                                    .addOnSuccessListener {
                                        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                                            ?.sendEmailVerification()
                                        popup()
                                    }
                                    .addOnFailureListener {
                                        criar.isEnabled = true
                                        criar.text = "Criar Conta ADM"
                                        Toast.makeText(this, "Conta criada, mas não foi possível salvar o perfil. Tente fazer login.", Toast.LENGTH_LONG).show()
                                        popup()
                                    }
                            } else {
                                criar.isEnabled = true
                                criar.text = "Criar Conta ADM"
                                Toast.makeText(this, traduzirErroFirebase(uidOuErro), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    criar.isEnabled = true
                    criar.text = "Criar Conta ADM"
                    Toast.makeText(this, "Não foi possível verificar a credencial. Verifique sua conexão.", Toast.LENGTH_SHORT).show()
                }
        }



        //--------------------------------
        // ENTRE AQUI
        //--------------------------------

        entrar.setOnClickListener{

            startActivity(

                Intent(

                    this,

                    TelaRF23LoginADM::class.java

                )

            )

            finish()

        }

    }




    //--------------------------------
    // POPUP
    //--------------------------------

    private fun popup(){

        val dialog=
            Dialog(this)

        dialog.setContentView(
            R.layout.popup_sucesso_cadastro
        )

        dialog.setCancelable(false)

        dialog.window
            ?.setBackgroundDrawableResource(
                android.R.color.transparent
            )



        val voltar=

            dialog.findViewById<Button>(
                R.id.btnRetornarLogin
            )



        voltar.setOnClickListener {

            startActivity(

                Intent(

                    this,

                    TelaRF23LoginADM::class.java

                )

            )

            dialog.dismiss()

            finish()

        }



        dialog.show()

    }

    //--------------------------------
    // TRADUÇÃO DE ERROS DO FIREBASE
    //--------------------------------

    private fun traduzirErroFirebase(mensagem: String?): String {
        return when {
            mensagem == null                                   -> "Ocorreu um erro inesperado. Tente novamente."
            mensagem.contains("email address is already")     -> "Este e-mail já está cadastrado."
            mensagem.contains("badly formatted")              -> "Formato de e-mail inválido."
            mensagem.contains("network error", ignoreCase = true)
                || mensagem.contains("unreachable")           -> "Sem conexão com a internet. Verifique sua rede."
            mensagem.contains("password is invalid")
                || mensagem.contains("least 6 characters")   -> "Senha muito fraca. Use pelo menos 8 caracteres."
            mensagem.contains("too many requests", ignoreCase = true) -> "Muitas tentativas. Aguarde alguns minutos e tente novamente."
            else                                              -> "Não foi possível criar a conta. Tente novamente."
        }
    }

}