package com.example.bibliounifornew.login

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
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

            }

            else{

                senha.inputType=

                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

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

            }

            else{

                confirma.inputType=

                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

            }

            confirma.setSelection(
                confirma.text.length
            )

        }



        //--------------------------------
        // CRIAR CONTA
        //--------------------------------

        criar.setOnClickListener{


            erroEmail.visibility=
                View.GONE

            erroCredencial.visibility=
                View.GONE

            erroSenha.visibility=
                View.GONE



            val emailTexto=
                email.text.toString()

            val senhaTexto=
                senha.text.toString()

            val confirmaTexto=
                confirma.text.toString()



            when{


                nome.text.isEmpty()
                        ||
                        usuario.text.isEmpty()
                        ||
                        email.text.isEmpty()
                        ||
                        credencial.text.isEmpty()
                        ||
                        senha.text.isEmpty()
                        ||
                        confirma.text.isEmpty()

                    ->{

                    Toast.makeText(

                        this,

                        "Preencha todos os campos",

                        Toast.LENGTH_SHORT

                    ).show()

                }



                !emailTexto.contains("@")->{

                    erroEmail.visibility=
                        View.VISIBLE

                }



                senhaTexto!=confirmaTexto
                        ||
                        senhaTexto.length<8
                        ||
                        !senhaTexto.any{
                            it.isDigit()
                        }
                        ||
                        !senhaTexto.any{
                            it.isUpperCase()
                        }

                    ->{

                    erroSenha.visibility=
                        View.VISIBLE

                }



                else->{

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