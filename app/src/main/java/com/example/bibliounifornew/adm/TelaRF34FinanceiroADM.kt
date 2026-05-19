package com.example.bibliounifornew.adm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF34FinanceiroADM : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Qualificação explícita para resolver o erro de referência do IDE (Ghost Error)
        this@TelaRF34FinanceiroADM.setContentView(R.layout.telarf34_finaceiro_adm)
    }
}
