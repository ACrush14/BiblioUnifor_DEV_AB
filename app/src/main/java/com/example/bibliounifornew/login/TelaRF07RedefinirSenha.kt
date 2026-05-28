package com.example.bibliounifornew.login

import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF07RedefinirSenha :
    AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

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

        val erroIgual =
            findViewById<TextView>(
                R.id.textErroSenhaIgual
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

        regras.visibility =
            View.GONE

        erroIgual.visibility =
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



        //---------------------------------
        // BOTÃO
        //---------------------------------

        btn.setOnClickListener {

            val email = intent.getStringExtra("email") ?: ""
            val senha = senhaNova.text.toString()
            val confirma = confirmar.text.toString()



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
                    fecharTeclado()
                    // RF07: Dispara o reset real no Firebase para o e-mail validado
                    auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            mostrarPopupEmail()
                        } else {
                            erro1.text = "Erro ao redefinir senha"
                            erro1.visibility = View.VISIBLE
                        }
                    }
                }
            }

        }



        //---------------------------------
        // OLHO SENHA
        //---------------------------------
        var visivel1 = false
        olhoSenha.setOnClickListener {
            if (visivel1) {
                senhaNova.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                olhoSenha.setImageResource(R.drawable.ic_eye_closed)
            } else {
                senhaNova.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                olhoSenha.setImageResource(R.drawable.ic_eye_open)
            }
            visivel1 = !visivel1
            senhaNova.setSelection(senhaNova.text.length)
        }

        //---------------------------------
        // OLHO CONFIRMAR
        //---------------------------------
        var visivel2 = false
        olhoConfirmar.setOnClickListener {
            if (visivel2) {
                confirmar.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                olhoConfirmar.setImageResource(R.drawable.ic_eye_closed)
            } else {
                confirmar.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                olhoConfirmar.setImageResource(R.drawable.ic_eye_open)
            }
            visivel2 = !visivel2
            confirmar.setSelection(confirmar.text.length)
        }

    }




    //---------------------------------
    // POPUP
    //---------------------------------

    private fun mostrarPopupEmail() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_sucesso_redefinir_senha)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val imageLogo = dialog.findViewById<ImageView>(R.id.imageLogoPopup)
        carregarLogoSegura(imageLogo)

        val titulo = dialog.findViewById<TextView>(R.id.textTituloPopup)
        val descricao = dialog.findViewById<TextView>(R.id.textDescricaoPopup)
        val voltar = dialog.findViewById<Button>(R.id.buttonRetornarLogin)

        titulo.text = "Tudo certo!"
        descricao.text = "Enviamos um link oficial do Firebase para seu e-mail.\n\nAbra o e-mail para concluir a alteração da senha."
        voltar.text = "Voltar ao Login"

        voltar.setOnClickListener {
            startActivity(Intent(this, TelaRF03LoginAluno::class.java))
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }

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




    private fun fecharTeclado() {
        val view = currentFocus ?: View(this)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    //---------------------------------
    // LOGO (Carregamento Seguro para evitar Canvas Limit Crash)
    //---------------------------------
    private fun carregarLogoSegura(imageView: ImageView) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val resId = R.drawable.unifor_marca
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeResource(resources, resId, options)

                // Reduz para no máximo 300px (seguro para UI e memória)
                val targetSize = 300
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
                options.inPreferredConfig = Bitmap.Config.RGB_565
                val decoded = BitmapFactory.decodeResource(resources, resId, options)

                val finalBitmap = if (decoded != null && (decoded.width > targetSize || decoded.height > targetSize)) {
                    Bitmap.createScaledBitmap(decoded, targetSize, targetSize, true)
                } else {
                    decoded
                }

                withContext(Dispatchers.Main) {
                    imageView.setImageBitmap(finalBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}