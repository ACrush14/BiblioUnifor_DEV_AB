package com.example.bibliounifornew.usuario

import android.app.Activity
import android.content.Intent
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.bibliounifornew.R

object NavigationHelper {

    fun configurarBarraNavegacao(activity: Activity) {
        val btnHome = activity.findViewById<ImageView>(R.id.nav_btnHome)
        val btnMinhaLivraria = activity.findViewById<ImageView>(R.id.nav_btnMinhaLivraria)
        val btnPesquisa = activity.findViewById<ImageView>(R.id.nav_btnPesquisa)
        val btnDesejos = activity.findViewById<ImageView>(R.id.nav_btnDesejos)
        val btnHistorico = activity.findViewById<ImageView>(R.id.nav_btnHistorico)
        val btnAmigos = activity.findViewById<ImageView>(R.id.nav_btnAmigos)

        val intentFlags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT

        btnHome?.setOnClickListener {
            if (activity !is TelaRF08DashboardUsuario) {
                val intent = Intent(activity, TelaRF08DashboardUsuario::class.java)
                intent.addFlags(intentFlags)
                activity.startActivity(intent)
            }
        }

        btnMinhaLivraria?.setOnClickListener {
            if (activity !is TelaRF15MinhaLivrariaActivity) {
                val intent = Intent(activity, TelaRF15MinhaLivrariaActivity::class.java)
                intent.addFlags(intentFlags)
                activity.startActivity(intent)
            }
        }

        btnPesquisa?.setOnClickListener {
            if (activity !is TelaRF11TelaDePesquisa) {
                val intent = Intent(activity, TelaRF11TelaDePesquisa::class.java)
                intent.addFlags(intentFlags)
                activity.startActivity(intent)
            }
        }

        btnDesejos?.setOnClickListener {
            if (activity !is TelaRF16ListaDesejosActivity) {
                val intent = Intent(activity, TelaRF16ListaDesejosActivity::class.java)
                intent.addFlags(intentFlags)
                activity.startActivity(intent)
            }
        }

        btnHistorico?.setOnClickListener {
            if (activity !is TelaRF21Historico) {
                val intent = Intent(activity, TelaRF21Historico::class.java)
                intent.addFlags(intentFlags)
                activity.startActivity(intent)
            }
        }

        btnAmigos?.setOnClickListener {
            if (activity !is TelaRF17Amigos) {
                val intent = Intent(activity, TelaRF17Amigos::class.java)
                intent.addFlags(intentFlags)
                activity.startActivity(intent)
            }
        }

        // Marcar ícone ativo
        marcarIconeAtivo(activity)
    }

    private fun marcarIconeAtivo(activity: Activity) {
        val corAtiva = ContextCompat.getColor(activity, R.color.biblio_blue)
        val corInativa = ContextCompat.getColor(activity, android.R.color.darker_gray) // Ou use #B7B7B7

        val btnHome = activity.findViewById<ImageView>(R.id.nav_btnHome)
        val btnMinhaLivraria = activity.findViewById<ImageView>(R.id.nav_btnMinhaLivraria)
        val btnPesquisa = activity.findViewById<ImageView>(R.id.nav_btnPesquisa)
        val btnDesejos = activity.findViewById<ImageView>(R.id.nav_btnDesejos)
        val btnHistorico = activity.findViewById<ImageView>(R.id.nav_btnHistorico)
        val btnAmigos = activity.findViewById<ImageView>(R.id.nav_btnAmigos)

        // Resetar todos
        btnHome?.setColorFilter(corInativa)
        btnMinhaLivraria?.setColorFilter(corInativa)
        btnPesquisa?.setColorFilter(corInativa)
        btnDesejos?.setColorFilter(corInativa)
        btnHistorico?.setColorFilter(corInativa)
        btnAmigos?.setColorFilter(corInativa)

        // Ativar o atual
        when (activity) {
            is TelaRF08DashboardUsuario -> btnHome?.setColorFilter(corAtiva)
            is TelaRF15MinhaLivrariaActivity -> btnMinhaLivraria?.setColorFilter(corAtiva)
            is TelaRF11TelaDePesquisa -> btnPesquisa?.setColorFilter(corAtiva)
            is TelaRF16ListaDesejosActivity -> btnDesejos?.setColorFilter(corAtiva)
            is TelaRF21Historico -> btnHistorico?.setColorFilter(corAtiva)
            is TelaRF17Amigos -> btnAmigos?.setColorFilter(corAtiva)
            is TelaRF18StatusAluguel -> btnMinhaLivraria?.setColorFilter(corAtiva)
        }
    }
}
