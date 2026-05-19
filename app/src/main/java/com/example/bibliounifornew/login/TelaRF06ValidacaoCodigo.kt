package com.example.bibliounifornew.login

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import java.util.Locale

class TelaRF06ValidacaoCodigo :
    AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.telarf06_validacao_de_codigo
        )

        val imageLogo = findViewById<ImageView>(R.id.imageLogoCodigo)
        carregarLogoSegura(imageLogo)



        //--------------------------------
        // COMPONENTES
        //--------------------------------

        val codigo =

            findViewById<EditText>(
                R.id.editTextCodigo
            )


        val erro =

            findViewById<TextView>(
                R.id.textErroCodigo
            )


        val timer =

            findViewById<TextView>(
                R.id.textTimer
            )


        val enviar =

            findViewById<MaterialButton>(
                R.id.buttonEnviarCodigo
            )



        erro.visibility =
            View.GONE



        //--------------------------------
        // TIMER 2 MINUTOS
        //--------------------------------

        object : CountDownTimer(

            120000,
            1000

        ) {

            override fun onTick(
                millisUntilFinished: Long
            ) {

                val segundos =

                    millisUntilFinished / 1000


                timer.text =

                    String.format(

                        Locale.getDefault(),

                        "%02d:%02d",

                        segundos / 60,

                        segundos % 60

                    )

            }


            override fun onFinish() {

                timer.text =
                    "Expirado"

            }

        }.start()



        //--------------------------------
        // BOTÃO ENVIAR
        //--------------------------------

        enviar.setOnClickListener {

            val textoCodigo = codigo.text.toString().trim()
            val tipoExtra = intent.getStringExtra("tipo") ?: "usuario"

            erro.visibility = View.GONE

            when {
                //-------------------
                // CAMPO VAZIO
                //-------------------
                textoCodigo.isEmpty() -> {
                    erro.text = "Digite o código"
                    erro.visibility = View.VISIBLE
                }

                //-------------------
                // CÓDIGO ERRADO
                //-------------------
                textoCodigo != "1234" -> {
                    erro.text = "Código incorreto"
                    erro.visibility = View.VISIBLE
                }

                //-------------------
                // CÓDIGO CERTO (Direcionamento por Tipo)
                //-------------------
                else -> {
                    val destino = if (tipoExtra == "adm") {
                        TelaRF25RedefinirSenhaADM::class.java
                    } else {
                        TelaRF07RedefinirSenha::class.java
                    }

                    val intent = Intent(this, destino)
                    startActivity(intent)
                    finish()
                }
            }
        }



        //--------------------------------
        // UX
        //--------------------------------

        codigo.setOnFocusChangeListener {

                _,
                hasFocus ->

            if (hasFocus) {

                erro.visibility =
                    View.GONE
            }

        }

    }

    private fun carregarLogoSegura(imageView: ImageView) {
        try {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 4
                inJustDecodeBounds = false
            }
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.unifor_marca, options)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}