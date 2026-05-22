package com.example.bibliounifornew.usuario

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF20Notificacoes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Forçando a referência ao layout via R do pacote principal
        setContentView(com.example.bibliounifornew.R.layout.telarf20_notificacoes)
    }
}
