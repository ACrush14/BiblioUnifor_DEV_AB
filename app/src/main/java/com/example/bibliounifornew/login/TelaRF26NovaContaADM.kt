package com.example.bibliounifornew.login

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF26NovaContaADM :
    AppCompatActivity() {

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

            v1=!v1

            if(v1){

                senha.inputType=

                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                olho1.setImageResource(R.drawable.ic_eye_open)

            }

            else{

                senha.inputType=

                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
                olho1.setImageResource(R.drawable.ic_eye_closed)

            }

            senha.setSelection(
                senha.text.length
            )

        }



        olho2.setOnClickListener{

            v2=!v2

            if(v2){

                confirma.inputType=

                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                olho2.setImageResource(R.drawable.ic_eye_open)

            }

            else{

                confirma.inputType=

                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
                olho2.setImageResource(R.drawable.ic_eye_closed)

            }

            confirma.setSelection(
                confirma.text.length
            )

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

            when {
                sNome.isEmpty() || sUsuario.isEmpty() || sEmail.isEmpty() || sCredencial.isEmpty() -> {
                    Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                }

                !sEmail.contains("@") || sEmail == "admin@jaexiste.com" -> {
                    erroEmail.visibility = View.VISIBLE
                }

                sCredencial == "123" -> { // Simulação de credencial inválida/já cadastrada
                    erroCredencial.visibility = View.VISIBLE
                }

                sSenha.isEmpty() -> {
                    erroSenha1.text = "Campo obrigatório"
                    erroSenha1.visibility = View.VISIBLE
                }

                sConfirma.isEmpty() -> {
                    erroSenha2.text = "Campo obrigatório"
                    erroSenha2.visibility = View.VISIBLE
                }

                sSenha.length < 8 -> {
                    erroSenha.text = "A senha deve conter pelo menos 8 caracteres"
                    erroSenha.visibility = View.VISIBLE
                }

                !sSenha.any { it.isDigit() } -> {
                    erroSenha.text = "Um número"
                    erroSenha.visibility = View.VISIBLE
                }

                !sSenha.any { it.isUpperCase() } -> {
                    erroSenha.text = "Uma letra maiúscula"
                    erroSenha.visibility = View.VISIBLE
                }

                sSenha != sConfirma -> {
                    erroSenhaDiferente.visibility = View.VISIBLE
                }

                sSenha == "12345678" -> { // Simulação de senha igual à anterior
                    erroSenhaIgual.visibility = View.VISIBLE
                }

                else -> {
                    popup()
                }
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

}