package com.example.bibliounifornew.login

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF05RecuperacaoSenha :
    AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.telarf05_recuperacao_senha
        )



        //--------------------------------
        // LOGO
        //--------------------------------

        val imageLogo =

            findViewById<ImageView>(
                R.id.imageLogoRecSenha
            )

        carregarLogoSegura(
            imageLogo
        )



        //--------------------------------
        // COMPONENTES
        //--------------------------------

        val email =

            findViewById<EditText>(
                R.id.editTextEmailRec
            )


        val enviar =
            findViewById<com.google.android.material.button.MaterialButton>(
                R.id.buttonEnviarCOD
            )


        val erro =

            findViewById<TextView>(
                R.id.textErroEmail
            )


        val voltar =

            findViewById<TextView>(
                R.id.buttonVoltarLog
            )



        erro.visibility =
            View.GONE



        //--------------------------------
        // EMAIL MOCKADO
        //--------------------------------

        val emailValido =

            "usuario@gmail.com"



        //--------------------------------
        // ENVIAR
        //--------------------------------

        enviar.setOnClickListener {


            val textoEmail =

                email.text
                    .toString()
                    .trim()



            erro.visibility =
                View.GONE



            when{


                //-------------------
                // CAMPO VAZIO
                //-------------------

                textoEmail.isEmpty() -> {

                    Toast.makeText(

                        this,

                        "Preencha todos os campos",

                        Toast.LENGTH_SHORT

                    ).show()

                }



                //-------------------
                // EMAIL ERRADO
                //-------------------

                textoEmail != emailValido -> {

                    erro.text =
                        "E-mail não cadastrado"

                    erro.visibility =
                        View.VISIBLE
                }



                //-------------------
                // SUCESSO
                //-------------------

                else -> {

                    val intent = Intent(
                        this,
                        TelaRF06ValidacaoCodigo::class.java
                    )

                    intent.putExtra("tipo", "usuario")

                    startActivity(intent)

                }

            }

        }



        //--------------------------------
        // VOLTAR LOGIN
        //--------------------------------

        voltar.setOnClickListener {

            startActivity(

                Intent(

                    this,

                    TelaRF03LoginAluno::class.java

                )

            )

            finish()

        }



        //--------------------------------
        // UX
        //--------------------------------

        email.setOnFocusChangeListener {

                _,
                hasFocus ->

            if(hasFocus){

                erro.visibility =
                    View.GONE
            }

        }

    }




    //--------------------------------
    // LOGO
    //--------------------------------

    private fun carregarLogoSegura(
        imageView: ImageView
    ){

        try{

            val options =

                BitmapFactory.Options()
                    .apply {

                        inSampleSize = 4
                    }


            val bitmap =

                BitmapFactory.decodeResource(

                    resources,

                    R.drawable.unifor_marca,

                    options

                )


            imageView.setImageBitmap(
                bitmap
            )

        }

        catch(e:Exception){

            e.printStackTrace()

        }

    }

}