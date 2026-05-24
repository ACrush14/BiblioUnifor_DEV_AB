package com.example.bibliounifornew.adm

import android.app.Activity
import android.content.Intent
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.bibliounifornew.R

object NavigationHelperADM {

    fun configurarBarraNavegacao(activity: Activity) {
        val btnHome = activity.findViewById<ImageView>(R.id.btnHomeAdm)
        val btnFinanceiro = activity.findViewById<ImageView>(R.id.btnFinanceiro)
        val btnUsuarios = activity.findViewById<ImageView>(R.id.btnUsuarios)
        val btnSuporte = activity.findViewById<ImageView>(R.id.btnSuporte)
        val btnLivros = activity.findViewById<ImageView>(R.id.btnLivros)

        val intentFlags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT

        // btnHomeAdm -> TelaRF28DashboardADM
        btnHome?.setOnClickListener {
            if (activity !is TelaRF28DashboardADM) {
                val intent = Intent(activity, TelaRF28DashboardADM::class.java)
                intent.addFlags(intentFlags)
                activity.startActivity(intent)
            }
        }

        // btnFinanceiro -> TelaRF34FinanceiroADM
        btnFinanceiro?.setOnClickListener {
            if (activity !is TelaRF34FinanceiroADM) {
                val intent = Intent(activity, TelaRF34FinanceiroADM::class.java)
                intent.addFlags(intentFlags)
                activity.startActivity(intent)
            }
        }

        // btnUsuarios -> TelaRF31Solicitacoes
        btnUsuarios?.setOnClickListener {
            if (activity !is TelaRF31Solicitacoes) {
                val intent = Intent(activity, TelaRF31Solicitacoes::class.java)
                intent.addFlags(intentFlags)
                activity.startActivity(intent)
            }
        }

        // btnSuporte -> TelaRF29GerenciamentoDeUsuarios
        btnSuporte?.setOnClickListener {
            if (activity !is TelaRF29GerenciamentoDeUsuarios) {
                val intent = Intent(activity, TelaRF29GerenciamentoDeUsuarios::class.java)
                intent.addFlags(intentFlags)
                activity.startActivity(intent)
            }
        }

        // btnLivros -> TelaRF32LivrosCRUD
        btnLivros?.setOnClickListener {
            if (activity !is TelaRF32LivrosCRUD) {
                val intent = Intent(activity, TelaRF32LivrosCRUD::class.java)
                intent.addFlags(intentFlags)
                activity.startActivity(intent)
            }
        }

        marcarIconeAtivo(activity)
    }

    private fun marcarIconeAtivo(activity: Activity) {
        val corAtiva = ContextCompat.getColor(activity, R.color.biblio_blue)
        val corInativa = ContextCompat.getColor(activity, android.R.color.darker_gray)

        val btnHome = activity.findViewById<ImageView>(R.id.btnHomeAdm)
        val btnFinanceiro = activity.findViewById<ImageView>(R.id.btnFinanceiro)
        val btnUsuarios = activity.findViewById<ImageView>(R.id.btnUsuarios)
        val btnSuporte = activity.findViewById<ImageView>(R.id.btnSuporte)
        val btnLivros = activity.findViewById<ImageView>(R.id.btnLivros)

        // Resetar todos
        btnHome?.setColorFilter(corInativa)
        btnFinanceiro?.setColorFilter(corInativa)
        btnUsuarios?.setColorFilter(corInativa)
        btnSuporte?.setColorFilter(corInativa)
        btnLivros?.setColorFilter(corInativa)

        // Ativar o atual
        when (activity) {
            is TelaRF28DashboardADM -> btnHome?.setColorFilter(corAtiva)
            is TelaRF34FinanceiroADM -> btnFinanceiro?.setColorFilter(corAtiva)
            is TelaRF31Solicitacoes -> btnUsuarios?.setColorFilter(corAtiva)
            is TelaRF29GerenciamentoDeUsuarios -> btnSuporte?.setColorFilter(corAtiva)
            is TelaRF32LivrosCRUD -> btnLivros?.setColorFilter(corAtiva)
            
            // Telas que pertencem ao grupo Home mas não são a principal
            is TelaRF27CrudAdm -> btnHome?.setColorFilter(corAtiva)
            is TelaRF36ListaAlugueisADM -> btnHome?.setColorFilter(corAtiva)
            is TelaRF35ConfirmarCadastroADM -> btnHome?.setColorFilter(corAtiva)
        }
    }
}
