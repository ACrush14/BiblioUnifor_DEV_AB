package com.example.bibliounifornew.login

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

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

    private fun carregarLogoSegura(imageView: ImageView) {
        try {
            val options = BitmapFactory.Options().apply {
                // inSampleSize = 4 reduz a resolução em 4x (consome 16x menos memória)
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
