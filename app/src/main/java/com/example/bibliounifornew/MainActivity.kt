package com.example.bibliounifornew

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.adm.TelaRF28DashboardADM
import com.example.bibliounifornew.login.TelaRF02Intermediaria
import com.example.bibliounifornew.usuario.TelaRF08DashboardUsuario

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.telarf01_bemvindo)

        val buttonComecar = findViewById<Button>(R.id.buttonComecar)
        buttonComecar.setOnClickListener {

            val usuarioLogado = false
            val tipoUsuario = listOf("adm", "user").random()

            if (!usuarioLogado) {
                startActivity(Intent(this@MainActivity, TelaRF02Intermediaria::class.java))

            } else {
                if (tipoUsuario == "adm") {
                    startActivity(Intent(this@MainActivity, TelaRF28DashboardADM::class.java))
                } else {
                    startActivity(Intent(this@MainActivity, TelaRF08DashboardUsuario::class.java))
                }
            }

            finish()
        }
    }
}