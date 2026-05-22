package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.MainActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton

class TelaRF08DashboardUsuario : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf08_dashboardusuario)

        // Header
        val btnConfig =
            findViewById<ImageView>(R.id.btnConfig)

        val btnNotificacao =
            findViewById<ImageView>(R.id.btnNotificacao)

        // Ações rápidas
        val btnPesquisarLivros =
            findViewById<MaterialButton>(R.id.btnPesquisarLivros)

        val btnMinhaLivraria =
            findViewById<MaterialButton>(R.id.btnMinhaLivraria)

        val btnListaDesejo =
            findViewById<MaterialButton>(R.id.btnListaDesejos)

        val btnAmigos =
            findViewById<MaterialButton>(R.id.btnAmigos)

        val btnHistorico =
            findViewById<MaterialButton>(R.id.btnHistorico)

        val btnStatusAluguel =
            findViewById<MaterialButton>(R.id.btnStatusAluguel)

        val btnSair =
            findViewById<MaterialButton>(R.id.btnSairConta)

        // Livro destaque
        val imgLivroAlienista =
            findViewById<ImageView>(R.id.imgLivroAlienista)


        //------------------------------------
        // NAVEGAÇÕES
        //------------------------------------

        btnConfig.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    TelaRF09Configuracao::class.java
                )
            )
        }

        btnNotificacao.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    TelaRF20Notificacoes::class.java
                )
            )

        }

        btnPesquisarLivros.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    TelaRF11TelaDePesquisa::class.java
                )
            )

        }

        btnMinhaLivraria.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    TelaRF15MinhaLivrariaActivity::class.java
                )
            )

        }

        btnListaDesejo.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    TelaRF16ListaDesejosActivity::class.java
                )
            )

        }

        btnAmigos.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    TelaRF17Amigos::class.java
                )
            )

        }

        btnHistorico.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    TelaRF21Historico::class.java
                )
            )

        }

        btnStatusAluguel.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    TelaRF18StatusAluguel::class.java
                )
            )

        }

        btnSair.setOnClickListener {
            showExitPopup()
        }

        imgLivroAlienista.setOnClickListener {

            val intent =
                Intent(
                    this,
                    TelaRF12TelaDoLivro::class.java
                )

            intent.putExtra(
                "LIVRO_ID",
                "1"
            )

            startActivity(intent)

        }

    }


    //------------------------------------
    // POPUP SAIR
    //------------------------------------

    private fun showExitPopup() {

        val dialogView =
            layoutInflater.inflate(
                R.layout.popup_sair_conta,
                null
            )

        val builder =
            AlertDialog.Builder(this)

        builder.setView(dialogView)

        val dialog =
            builder.create()

        dialog.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )


        // Confirmar saída

        dialogView
            .findViewById<MaterialButton>(
                R.id.btnConfirmarSair
            )

            .setOnClickListener {

                dialog.dismiss()

                val intentSair =
                    Intent(
                        this,
                        MainActivity::class.java
                    )

                intentSair.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intentSair)

                finish()

            }


        // Cancelar

        dialogView
            .findViewById<TextView>(
                R.id.btnCancelarSair
            )

            .setOnClickListener {

                dialog.dismiss()

            }

        dialog.show()

    }

}