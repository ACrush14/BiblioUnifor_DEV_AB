package com.example.bibliounifornew.login

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF07RedefinirSenha :
    AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.telarf07_redefinicao_de_senha
        )



        //---------------------------------
        // COMPONENTES
        //---------------------------------

        val logo =

            findViewById<ImageView>(
                R.id.imageLogoNovaSenha
            )

        carregarLogoSegura(logo)



        val senhaNova =

            findViewById<EditText>(
                R.id.editSenhaNova
            )


        val confirmar =

            findViewById<EditText>(
                R.id.editConfirmarSenha
            )



        val erro1 =

            findViewById<TextView>(
                R.id.textErroSenha1
            )


        val erro2 =

            findViewById<TextView>(
                R.id.textErroSenha2
            )


        val erroDif =

            findViewById<TextView>(
                R.id.textErroSenhaDiferente
            )


        val regras =

            findViewById<TextView>(
                R.id.textRegrasSenha
            )



        val btn =

            findViewById<MaterialButton>(
                R.id.buttonRedefinirSenha
            )



        val olhoSenha =

            findViewById<ImageView>(
                R.id.iconOlhoSenhaNova
            )


        val olhoConfirmar =

            findViewById<ImageView>(
                R.id.iconOlhoConfirmarSenha
            )



        erro1.visibility =
            View.GONE

        erro2.visibility =
            View.GONE

        erroDif.visibility =
            View.GONE



        //---------------------------------
        // BOTÃO
        //---------------------------------

        btn.setOnClickListener {


            val senha =

                senhaNova.text
                    .toString()


            val confirma =

                confirmar.text
                    .toString()



            erro1.visibility =
                View.GONE

            erro2.visibility =
                View.GONE

            erroDif.visibility =
                View.GONE



            when{


                senha.isEmpty()->{

                    erro1.text =
                        "Digite uma senha"

                    erro1.visibility =
                        View.VISIBLE
                }



                confirma.isEmpty()->{

                    erro2.text =
                        "Confirme a senha"

                    erro2.visibility =
                        View.VISIBLE
                }



                senha!=confirma->{

                    erroDif.visibility =
                        View.VISIBLE
                }



                senha.length<8 ||

                        !senha.any{
                            it.isDigit()
                        }

                        ||

                        !senha.any{
                            it.isUpperCase()
                        }

                    ->{

                    regras.visibility =
                        View.VISIBLE
                }



                else->{

                    mostrarPopup()

                }

            }

        }



        //---------------------------------
        // OLHO SENHA
        //---------------------------------

        var visivel1=false

        olhoSenha.setOnClickListener {

            visivel1=!visivel1

            if(visivel1){

                senhaNova.inputType=

                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

            }

            else{

                senhaNova.inputType=

                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

            }

            senhaNova.setSelection(
                senhaNova.text.length
            )

        }



        //---------------------------------
        // OLHO CONFIRMAR
        //---------------------------------

        var visivel2=false

        olhoConfirmar.setOnClickListener {

            visivel2=!visivel2

            if(visivel2){

                confirmar.inputType=

                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

            }

            else{

                confirmar.inputType=

                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD

            }

            confirmar.setSelection(
                confirmar.text.length
            )

        }

    }




    //---------------------------------
    // POPUP
    //---------------------------------

    private fun mostrarPopup(){

        val dialog =
            Dialog(this)

        dialog.setContentView(

            R.layout.popup_sucesso_redefinir_senha

        )

        dialog.window
            ?.setBackgroundDrawableResource(
                android.R.color.transparent
            )

        val imageLogo = dialog.findViewById<ImageView>(R.id.imageLogoPopup)
        carregarLogoSegura(imageLogo)

        val voltar =

            dialog.findViewById<Button>(
                R.id.buttonRetornarLogin
            )



        voltar.setOnClickListener {

            startActivity(

                Intent(

                    this,

                    TelaRF03LoginAluno::class.java

                )

            )

            dialog.dismiss()

            finish()

        }



        dialog.show()

    }




    //---------------------------------
    // LOGO
    //---------------------------------

    private fun carregarLogoSegura(
        imageView: ImageView
    ){

        try{

            val options = BitmapFactory.Options().apply {
                inSampleSize = 4
                inJustDecodeBounds = false
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