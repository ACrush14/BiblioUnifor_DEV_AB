package com.example.bibliounifornew.login

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF24EmailEnviadoADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf24_email_enviado_adm)

        val imageLogo = findViewById<ImageView>(R.id.imageLogoEnviadoAdm)
        carregarLogoSegura(imageLogo)

        val btnVoltar = findViewById<MaterialButton>(R.id.buttonVoltarLoginEnviadoAdm)

        btnVoltar.setOnClickListener {
            val intent = Intent(this, TelaRF23LoginADM::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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
