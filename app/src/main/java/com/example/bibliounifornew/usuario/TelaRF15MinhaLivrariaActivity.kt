package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class TelaRF15MinhaLivrariaActivity : AppCompatActivity() {

    // Referências dos Cards dos Livros para controle de visibilidade (MaterialCardView)
    private lateinit var cardLivro1: MaterialCardView
    private lateinit var cardLivro2: MaterialCardView
    private lateinit var cardLivro3: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf15_minha_livraria)

        // Inicialização dos componentes mapeados no XML
        mapearComponentes()

        // 1) Configuração do FILTRO (Seta ao lado do status)
        findViewById<ImageView>(R.id.imgFiltroStatus).setOnClickListener {
            abrirPopupFiltro()
        }

        // Configuração individual das ações de cada livro (Três pontinhos e Remover)
        configurarAcoesLivro1()
        configurarAcoesLivro2()
        configurarAcoesLivro3()
    }

    private fun mapearComponentes() {
        cardLivro1 = findViewById(R.id.cardLivro1)
        cardLivro2 = findViewById(R.id.cardLivro2)
        cardLivro3 = findViewById(R.id.cardLivro3)
    }

    /**
     * LÓGICA DE FILTRAGEM (RF15)
     * Abre o popup_filtro_status_livros.xml e filtra os livros na própria tela.
     */
    private fun abrirPopupFiltro() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_filtro_status_livros, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Configura o fundo do dialog como transparente para respeitar o layout customizado
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Opção LIDO -> Mostra apenas livros com status lido (Livro 1)
        dialogView.findViewById<MaterialButton>(R.id.buttonFiltroLido).setOnClickListener {
            cardLivro1.visibility = View.VISIBLE
            cardLivro2.visibility = View.GONE
            cardLivro3.visibility = View.GONE
            dialog.dismiss()
        }

        // Opção LENDO -> Mostra apenas livros com status lendo (Livro 3)
        dialogView.findViewById<MaterialButton>(R.id.buttonFiltroLendo).setOnClickListener {
            cardLivro1.visibility = View.GONE
            cardLivro2.visibility = View.GONE
            cardLivro3.visibility = View.VISIBLE
            dialog.dismiss()
        }

        // Opção NÃO LIDO -> Mostra apenas livros com status não lido (Livro 2)
        dialogView.findViewById<MaterialButton>(R.id.buttonFiltroNaoLido).setOnClickListener {
            cardLivro1.visibility = View.GONE
            cardLivro2.visibility = View.VISIBLE
            cardLivro3.visibility = View.GONE
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * AÇÕES DO LIVRO 1 (O Alienista)
     */
    private fun configurarAcoesLivro1() {
        // 2) TRÊS PONTINHOS (⋮) -> Vai para detalhes do livro (TelaRF12TelaDoLivro)
        findViewById<TextView>(R.id.menu1).setOnClickListener {
            val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            intent.putExtra("LIVRO_ID", "1")
            startActivity(intent)
        }

        // 3) BOTÃO REMOVER -> Remove da tela e mostra Toast
        findViewById<MaterialButton>(R.id.btnRemover1).setOnClickListener {
            cardLivro1.visibility = View.GONE
            Toast.makeText(this, "Livro removido da sua livraria", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * AÇÕES DO LIVRO 2 (A Sociedade do Anel)
     */
    private fun configurarAcoesLivro2() {
        // 2) TRÊS PONTINHOS (⋮)
        findViewById<TextView>(R.id.menu2).setOnClickListener {
            val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            intent.putExtra("LIVRO_ID", "2")
            startActivity(intent)
        }

        // 3) BOTÃO REMOVER
        findViewById<MaterialButton>(R.id.btnRemover2).setOnClickListener {
            cardLivro2.visibility = View.GONE
            Toast.makeText(this, "Livro removido da sua livraria", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * AÇÕES DO LIVRO 3 (Vidas Secas)
     */
    private fun configurarAcoesLivro3() {
        // 2) TRÊS PONTINHOS (⋮)
        findViewById<TextView>(R.id.menu3).setOnClickListener {
            val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            intent.putExtra("LIVRO_ID", "3")
            startActivity(intent)
        }

        // 3) BOTÃO REMOVER
        findViewById<MaterialButton>(R.id.btnRemover3).setOnClickListener {
            cardLivro3.visibility = View.GONE
            Toast.makeText(this, "Livro removido da sua livraria", Toast.LENGTH_SHORT).show()
        }
    }
}
