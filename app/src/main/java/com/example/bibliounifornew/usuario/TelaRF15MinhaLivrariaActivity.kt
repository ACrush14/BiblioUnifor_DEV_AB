package com.example.bibliounifornew.usuario

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class TelaRF15MinhaLivrariaActivity : AppCompatActivity() {

    // 1. Instanciando Repositórios
    private val authRepository = AuthRepository()
    private val usuarioRepository = UsuarioRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf15_minha_livraria)

        // ----------------------------------------------------
        // 2. ATUALIZANDO O CABEÇALHO (Buscando E-mail Real)
        // ----------------------------------------------------
        val textEmailCabecalho = findViewById<TextView>(R.id.textEmailLivraria)
        val usuarioAtual = authRepository.getUsuarioAtual()

        if (usuarioAtual != null) {
            // Se o auth já tem o e-mail, nem precisamos bater no Firestore para isso.
            // Exibimos direto para dar velocidade à tela!
            textEmailCabecalho?.text = usuarioAtual.email

            // Opcional: Se você quiser buscar a foto de perfil no futuro,
            // você chamaria o usuarioRepository.buscarPerfilUsuario aqui.
        } else {
            // Rota Segura: Sem usuário logado, manda pro login.
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        // ----------------------------------------------------
        // 3. LÓGICA DO FILTRO
        // ----------------------------------------------------
        val imgFiltroStatus = findViewById<ImageView>(R.id.imgFiltroStatus)
        imgFiltroStatus?.setOnClickListener {
            abrirPopupFiltro()
        }

        // ----------------------------------------------------
        // 4. LÓGICA DOS LIVROS (Cards Estáticos por enquanto)
        // ----------------------------------------------------

        // Livro 1: O Alienista
        configurarLivro(
            cardId = R.id.cardLivro1,
            btnRemoverId = R.id.btnRemover1,
            menuIconId = R.id.menu1,
            livroId = 1
        )

        // Livro 2: A Sociedade do Anel
        configurarLivro(
            cardId = R.id.cardLivro2,
            btnRemoverId = R.id.btnRemover2,
            menuIconId = R.id.menu2,
            livroId = 2
        )

        // Livro 3: Vidas Secas
        configurarLivro(
            cardId = R.id.cardLivro3,
            btnRemoverId = R.id.btnRemover3,
            menuIconId = R.id.menu3,
            livroId = 3
        )

        // Configurar Barra de Navegação
        NavigationHelper.configurarBarraNavegacao(this)
    }

    // ----------------------------------------------------
    // MÉTODOS DE APOIO
    // ----------------------------------------------------

    private fun configurarLivro(cardId: Int, btnRemoverId: Int, menuIconId: Int, livroId: Int) {
        val card = findViewById<MaterialCardView>(cardId)
        val btnRemover = findViewById<MaterialButton>(btnRemoverId)
        val menuIcon = findViewById<View>(menuIconId)

        // Botão Remover (Esconde o card temporariamente na UI mockada)
        btnRemover?.setOnClickListener {
            card?.visibility = View.GONE
            Toast.makeText(this, "Livro removido da sua livraria", Toast.LENGTH_SHORT).show()
        }

        // Clique nos 3 Pontinhos (Abre os detalhes do livro)
        menuIcon?.setOnClickListener {
            val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            intent.putExtra("LIVRO_ID", livroId.toString())
            startActivity(intent)
        }
    }

    private fun abrirPopupFiltro() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_filtrar_midia) // Confirme se o nome do layout é esse
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Mapear elementos do popup se necessário
        val btnLimpar = dialog.findViewById<MaterialButton>(R.id.btnLimparFiltro)
        val btnSalvar = dialog.findViewById<MaterialButton>(R.id.btnSalvarFiltro)

        btnLimpar?.setOnClickListener {
            dialog.dismiss()
        }

        btnSalvar?.setOnClickListener {
            Toast.makeText(this, "Filtro aplicado (Simulação)", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}