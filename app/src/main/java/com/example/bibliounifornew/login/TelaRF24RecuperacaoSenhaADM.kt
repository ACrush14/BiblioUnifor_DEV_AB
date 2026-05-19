package com.example.bibliounifornew.login

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF24RecuperacaoSenhaADM :
    AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.telarf24_recuperacao_senha_adm
        )



        //--------------------------------
        // LOGO
        //--------------------------------

        val logo =

            findViewById<ImageView>(
                R.id.imageLogoRecuperar
            )

        carregarLogoSegura(logo)



        //--------------------------------
        // COMPONENTES
        //--------------------------------

        val email =

            findViewById<EditText>(
                R.id.editEmailRecuperar
            )


        val erro =

            findViewById<TextView>(
                R.id.textErroEmailRecuperar
            )


        val voltar =

            findViewById<TextView>(
                R.id.textVoltarLogin
            )


        val enviar =

            findViewById<MaterialButton>(
                R.id.buttonEnviarCodigo
            )



        erro.visibility =
            View.GONE



        //--------------------------------
        // EMAIL MOCKADO
        //--------------------------------

        val emailValido =

            "emailvalido@gmail.com"



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


                //----------------
                // CAMPO VAZIO
                //----------------

                textoEmail.isEmpty()->{

                    Toast.makeText(

                        this,

                        "Preencha todos os campos",

                        Toast.LENGTH_SHORT

                    ).show()

                }



                //----------------
                // EMAIL ERRADO
                //----------------

                textoEmail != emailValido -> {

                    erro.text =
                        "E-mail não cadastrado"

                    erro.visibility =
                        View.VISIBLE
                }



                //----------------
                // SUCESSO
                //----------------

                else -> {


                    val intent =

                        Intent(

                            this,

                            TelaRF06ValidacaoCodigo::class.java

                        )



                    intent.putExtra(

                        "tipo",

                        "adm"

                    )



                    startActivity(
                        intent
                    )

                }

            }

        }



        //--------------------------------
        // VOLTAR
        //--------------------------------

        voltar.setOnClickListener {

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
            val options = BitmapFactory.Options().apply {
                inSampleSize = 4
                inJustDecodeBounds = false
            }
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.unifor_marca, options)
            imageView.setImageBitmap(bitmap)
        } catch(e:Exception){
            e.printStackTrace()
        }
    }

}