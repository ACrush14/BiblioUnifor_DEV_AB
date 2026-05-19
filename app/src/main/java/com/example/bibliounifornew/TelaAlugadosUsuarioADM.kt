package com.example.bibliounifornew

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaAlugadosUsuarioADM : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Qualificação explícita para resolver o erro de referência do IDE (Ghost Error)
        this@TelaAlugadosUsuarioADM.setContentView(R.layout.telarf_adm_usuario_alugados)
    }
}
