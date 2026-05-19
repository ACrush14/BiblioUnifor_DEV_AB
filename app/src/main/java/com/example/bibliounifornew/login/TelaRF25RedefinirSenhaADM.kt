package com.example.bibliounifornew.login

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF25RedefinirSenhaADM :
    AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.telarf25_redefinir_senha_adm
        )



        //--------------------------------
        // LOGO
        //--------------------------------

        val logo =

            findViewById<ImageView>(
                R.id.imageLogoNovaSenha
            )

        carregarLogoSegura(logo)



        //--------------------------------
        // COMPONENTES
        //--------------------------------

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


        val erroIgual =

            findViewById<TextView>(
                R.id.textErroSenhaIgual
            )

        val regras =
            findViewById<TextView>(
                R.id.textRegrasSenha
            )



        val btn =

            findViewById<MaterialButton>(
                R.id.buttonRedefinirSenha
            )



        val olho1 =

            findViewById<ImageView>(
                R.id.iconOlhoSenhaNova
            )


        val olho2 =

            findViewById<ImageView>(
                R.id.iconOlhoConfirmarSenha
            )



        erro1.visibility =
            View.GONE

        erro2.visibility =
            View.GONE

        erroDif.visibility =
            View.GONE

        erroIgual.visibility =
            View.GONE

        regras.visibility =
            View.GONE

        //---------------------------------
        // TEXT WATCHER (LIMPAR ERROS)
        //---------------------------------

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                erro1.visibility = View.GONE
                erro2.visibility = View.GONE
                erroDif.visibility = View.GONE
                erroIgual.visibility = View.GONE
                regras.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        senhaNova.addTextChangedListener(watcher)
        confirmar.addTextChangedListener(watcher)



        //--------------------------------
        // BOTÃO
        //--------------------------------

        btn.setOnClickListener {


            val senha =

                senhaNova.text
                    .toString()


            val confirma =

                confirmar.text
                    .toString()

            erro1.visibility = View.GONE
            erro2.visibility = View.GONE
            erroDif.visibility = View.GONE
            erroIgual.visibility = View.GONE
            regras.visibility = View.GONE


            when {
                senha.isEmpty() -> {
                    erro1.text = "Campo obrigatório"
                    erro1.visibility = View.VISIBLE
                }

                confirma.isEmpty() -> {
                    erro2.text = "Campo obrigatório"
                    erro2.visibility = View.VISIBLE
                }

                senha.length < 8 -> {
                    erro1.text = "A senha deve conter pelo menos 8 caracteres"
                    erro1.visibility = View.VISIBLE
                }

                !senha.any { it.isDigit() } -> {
                    erro1.text = "A senha deve conter um número"
                    erro1.visibility = View.VISIBLE
                }

                !senha.any { it.isUpperCase() } -> {
                    erro1.text = "A senha deve conter uma letra maiúscula"
                    erro1.visibility = View.VISIBLE
                }

                senha == "12345678" -> {
                    erroIgual.text = "A senha é a mesma da antiga"
                    erroIgual.visibility = View.VISIBLE
                }

                senha != confirma -> {
                    erroDif.text = "As senhas estão diferentes"
                    erroDif.visibility = View.VISIBLE
                }

                else -> {
                    popup()
                }
            }

        }



        //--------------------------------
        // OLHO 1
        //--------------------------------

        var v1=false

        olho1.setOnClickListener {

            v1=!v1

            if(v1){

                senhaNova.inputType=

                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                olho1.setImageResource(R.drawable.ic_eye_open)

            }

            else{

                senhaNova.inputType=

                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
                olho1.setImageResource(R.drawable.ic_eye_closed)

            }

            senhaNova.setSelection(
                senhaNova.text.length
            )

        }



        //--------------------------------
        // OLHO 2
        //--------------------------------

        var v2=false

        olho2.setOnClickListener {

            v2=!v2

            if(v2){

                confirmar.inputType=

                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                olho2.setImageResource(R.drawable.ic_eye_open)

            }

            else{

                confirmar.inputType=

                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
                olho2.setImageResource(R.drawable.ic_eye_closed)

            }

            confirmar.setSelection(
                confirmar.text.length
            )

        }

    }




    //--------------------------------
    // POPUP
    //--------------------------------

    private fun popup(){

        val dialog=
            Dialog(this)

        dialog.setContentView(

            R.layout.popup_confirmar_redefinir_senha

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