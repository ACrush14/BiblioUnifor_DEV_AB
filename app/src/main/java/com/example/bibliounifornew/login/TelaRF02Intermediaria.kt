package com.example.bibliounifornew.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF02Intermediaria : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf02_intermediaria)

        val btnEstudante = findViewById<MaterialButton>(R.id.btnEstudante)
        val btnAdmin = findViewById<MaterialButton>(R.id.btnAdmin)

        btnEstudante.setOnClickListener {
            val intent = Intent(this@TelaRF02Intermediaria, TelaRF03LoginAluno::class.java)
            startActivity(intent)
        }

        btnAdmin.setOnClickListener {
            val intent = Intent(this@TelaRF02Intermediaria, TelaRF23LoginADM::class.java)
            startActivity(intent)
        }
    }
}
