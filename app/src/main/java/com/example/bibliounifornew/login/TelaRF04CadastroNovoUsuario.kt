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

class TelaRF04CadastroNovoUsuario : AppCompatActivity() {

    private var senhaVisivel = false
    private var confirmarSenhaVisivel = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(
            R.layout.telarf04_cadastrar_novo_usuario
        )



        //-----------------------------------
        // COMPONENTES
        //-----------------------------------

        val imageLogo =
            findViewById<ImageView>(
                R.id.imageLogoCadastro
            )

        carregarLogoSegura(imageLogo)



        val nome =
            findViewById<EditText>(
                R.id.editTextNome
            )

        val usuario =
            findViewById<EditText>(
                R.id.editTextUsuario
            )

        val email =
            findViewById<EditText>(
                R.id.editTextEmail
            )

        val senha =
            findViewById<EditText>(
                R.id.editTextSenha
            )

        val confirmaSenha =
            findViewById<EditText>(
                R.id.editTextConfirmaSenha
            )



        val erroEmail =
            findViewById<TextView>(
                R.id.tvErroEmail
            )

        val erroSenha =
            findViewById<TextView>(
                R.id.tvErroSenha
            )



        val btnCriar =
            findViewById<Button>(
                R.id.btnCriar
            )



        val olhoSenha =
            findViewById<ImageView>(
                R.id.iconOlhoSenha
            )

        val olhoConfirmar =
            findViewById<ImageView>(
                R.id.iconOlhoConfirmarSenha
            )



        //-----------------------------------
        // ENTRE AQUI
        //-----------------------------------

        findViewById<TextView>(
            R.id.textEntreAqui
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    TelaRF03LoginAluno::class.java
                )
            )

            finish()
        }



        //-----------------------------------
        // OLHO SENHA
        //-----------------------------------

        olhoSenha.setOnClickListener {

            senhaVisivel =
                !senhaVisivel


            if (senhaVisivel){

                senha.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

            }else{

                senha.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            senha.setSelection(
                senha.text.length
            )
        }



        //-----------------------------------
        // OLHO CONFIRMAR SENHA
        //-----------------------------------

        olhoConfirmar.setOnClickListener {

            confirmarSenhaVisivel =
                !confirmarSenhaVisivel


            if(confirmarSenhaVisivel){

                confirmaSenha.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

            }else{

                confirmaSenha.inputType =
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
            }


            confirmaSenha.setSelection(
                confirmaSenha.text.length
            )
        }



        //-----------------------------------
        // BOTÃO CRIAR
        //-----------------------------------

        btnCriar.setOnClickListener {


            erroEmail.visibility =
                View.GONE

            erroSenha.visibility =
                View.GONE



            val nomeTexto =
                nome.text.toString().trim()

            val usuarioTexto =
                usuario.text.toString().trim()

            val emailTexto =
                email.text.toString().trim()

            val senhaTexto =
                senha.text.toString()

            val confirmaTexto =
                confirmaSenha.text.toString()



            //-----------------------------------
            // CAMPOS VAZIOS
            //-----------------------------------

            if(

                nomeTexto.isEmpty()
                ||
                usuarioTexto.isEmpty()
                ||
                emailTexto.isEmpty()
                ||
                senhaTexto.isEmpty()
                ||
                confirmaTexto.isEmpty()

            ){

                Toast.makeText(

                    this,
                    "Preencha todos os campos",
                    Toast.LENGTH_SHORT

                ).show()

                return@setOnClickListener
            }



            //-----------------------------------
            // EMAIL
            //-----------------------------------

            if(

                !android.util.Patterns
                    .EMAIL_ADDRESS
                    .matcher(emailTexto)
                    .matches()

            ){

                erroEmail.visibility =
                    View.VISIBLE

                return@setOnClickListener
            }



            //-----------------------------------
            // SENHA FORTE
            //-----------------------------------

            val senhaValida =

                senhaTexto.length >= 8
                        &&
                        senhaTexto.any { it.isDigit() }
                        &&
                        senhaTexto.any { it.isUpperCase() }



            if(!senhaValida){

                erroSenha.visibility =
                    View.VISIBLE

                return@setOnClickListener
            }



            //-----------------------------------
            // SENHAS DIFERENTES
            //-----------------------------------

            if(
                senhaTexto
                !=
                confirmaTexto
            ){

                erroSenha.text =
                    "As senhas não coincidem"

                erroSenha.visibility =
                    View.VISIBLE

                return@setOnClickListener
            }



            //-----------------------------------
            // POPUP SUCESSO
            //-----------------------------------

            val dialog =
                Dialog(this)

            dialog.setContentView(
                R.layout.popup_sucesso_cadastro
            )


            dialog.window
                ?.setBackgroundDrawableResource(
                    android.R.color.transparent
                )


            dialog.setCancelable(false)



            val btnRetornar =

                dialog.findViewById<Button>(
                    R.id.btnRetornarLogin
                )



            btnRetornar.setOnClickListener {

                dialog.dismiss()


                val intent =

                    Intent(

                        this,
                        TelaRF03LoginAluno::class.java

                    )


                startActivity(intent)

                finish()

            }



            dialog.show()

        }

    }




    //-----------------------------------
    // LOGO
    //-----------------------------------

    private fun carregarLogoSegura(
        imageView: ImageView
    ){

        try{

            val options =
                BitmapFactory.Options().apply {

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

        catch (e: Exception){

            e.printStackTrace()
        }

    }

}