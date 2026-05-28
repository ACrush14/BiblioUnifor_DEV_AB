package com.example.bibliounifornew.login

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Tela inicial de Boas-Vindas (RF01)
 */
class TelaRF01BemVindo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf01_bemvindo)

        // Carrega a imagem de forma segura para evitar crash de memória
        val imageLogo = findViewById<ImageView>(R.id.imageIcon2)
        carregarLogoSegura(imageLogo)

        val btnEntrar = findViewById<Button>(R.id.buttonComecar)
        btnEntrar.setOnClickListener {
            val intent = Intent(this@TelaRF01BemVindo, TelaRF02Intermediaria::class.java)
            startActivity(intent)
        }
    }

    // Decode em Dispatchers.IO — libera a Main Thread e elimina o ANR de frame skip.
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
