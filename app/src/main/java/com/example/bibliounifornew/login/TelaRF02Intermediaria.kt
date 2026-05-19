package com.example.bibliounifornew.login

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF02Intermediaria : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf02_intermediaria)

        // Carregamento seguro da logo
        val imageLogo = findViewById<ImageView>(R.id.imageIcon2)
        carregarLogoSegura(imageLogo)

        val btnEstudante = findViewById<Button>(R.id.btnEstudante)
        val btnAdmin = findViewById<Button>(R.id.btnAdmin)

        btnEstudante.setOnClickListener {
            val intent = Intent(this, TelaRF03LoginAluno::class.java)
            startActivity(intent)
        }

        btnAdmin.setOnClickListener {
            val intent = Intent(this, TelaRF23LoginADM::class.java)
            startActivity(intent)
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
