package com.example.bibliounifornew.login

import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            val codigoRecebido = intent.getStringExtra("codigo") ?: ""

            erro.visibility = View.GONE

            if (codigoRecebido.isEmpty()) {
                erro.text = "Código expirado"
                erro.visibility = View.VISIBLE
                return@setOnClickListener
            }

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
                textoCodigo != codigoRecebido -> {
                    erro.text = "Código incorreto"
                    erro.visibility = View.VISIBLE
                }

                //-------------------
                // CÓDIGO CERTO (Direcionamento por Tipo)
                //-------------------
                else -> {
                    fecharTeclado()
                    val destino = if (tipoExtra == "adm") {
                        TelaRF25RedefinirSenhaADM::class.java
                    } else {
                        TelaRF07RedefinirSenha::class.java
                    }

                    val nextIntent = Intent(this, destino)
                    nextIntent.putExtra("email", intent.getStringExtra("email"))
                    startActivity(nextIntent)
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

    private fun fecharTeclado() {
        val view = currentFocus ?: View(this)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun carregarLogoSegura(imageView: ImageView) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val resId = R.drawable.unifor_marca
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeResource(resources, resId, options)

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