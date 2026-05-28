package com.example.bibliounifornew.login

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaRF05EmailEnviado : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf05_email_enviado)

        val imageLogo = findViewById<ImageView>(R.id.imageLogoEnviado)
        carregarLogoSegura(imageLogo)

        val btnVoltar = findViewById<MaterialButton>(R.id.buttonVoltarLoginEnviado)

        btnVoltar.setOnClickListener {
            val intent = Intent(this, TelaRF03LoginAluno::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // ─── LOGO (Carregamento Seguro para evitar Canvas Limit Crash) ───────────
    private fun carregarLogoSegura(imageView: ImageView) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val resId = R.drawable.unifor_marca
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeResource(resources, resId, options)

                // Reduz para no máximo 500px para evitar estouro de memória (Canvas Limit)
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
