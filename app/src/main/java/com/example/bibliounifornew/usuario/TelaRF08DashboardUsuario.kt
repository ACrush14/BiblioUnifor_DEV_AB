package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.MainActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.google.android.material.button.MaterialButton

class TelaRF08DashboardUsuario : AppCompatActivity() {

    // Instanciando os repositórios
    private val authRepository = AuthRepository()
    private val usuarioRepository = UsuarioRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf08_dashboardusuario)

        // ------------------------------------
        // CARREGAR DADOS DO USUÁRIO
        // ------------------------------------
        val textNomeUsuario = findViewById<TextView>(R.id.textNomeUsuario) // Confirme se este é o ID correto do "João Bobo" no seu XML
        val uidAtual = authRepository.getUsuarioAtual()?.uid

        if (uidAtual != null) {
            // Se demorar para carregar, pelo menos não fica "João Bobo"
            textNomeUsuario?.text = "Carregando..."

            usuarioRepository.buscarPerfilUsuario(uidAtual) { sucesso, dados, erro ->
                if (sucesso && dados != null) {
                    val nomeBanco = dados["nome"] as? String ?: "Usuário"

                    // Se você quiser mostrar apenas o primeiro nome (ex: "Anderson"):
                    // val primeiroNome = nomeBanco.split(" ").firstOrNull() ?: "Usuário"

                    textNomeUsuario?.text = nomeBanco
                } else {
                    Toast.makeText(this, "Erro ao carregar perfil: $erro", Toast.LENGTH_SHORT).show()
                    textNomeUsuario?.text = "Erro ao carregar"
                }
            }
        } else {
            // Se o usuário não estiver logado por algum motivo, chuta ele de volta pro login
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
        }

        // ------------------------------------
        // COMPONENTES E CLIQUES
        // ------------------------------------
        val btnConfig = findViewById<ImageView>(R.id.btnConfig)
        val btnNotificacao = findViewById<ImageView>(R.id.btnNotificacao)
        val btnPesquisarLivros = findViewById<MaterialButton>(R.id.btnPesquisarLivros)
        val btnMinhaLivrariaDashboard = findViewById<MaterialButton>(R.id.btnMinhaLivraria)
        val btnListaDesejo = findViewById<MaterialButton>(R.id.btnListaDesejos)
        val btnAmigosDashboard = findViewById<MaterialButton>(R.id.btnAmigos)
        val btnHistoricoDashboard = findViewById<MaterialButton>(R.id.btnHistorico)
        val btnStatusAluguel = findViewById<MaterialButton>(R.id.btnStatusAluguel)
        val btnSair = findViewById<MaterialButton>(R.id.btnSairConta)
        val imgLivroAlienista = findViewById<ImageView>(R.id.imgLivroAlienista)

        btnConfig.setOnClickListener { startActivity(Intent(this, TelaRF09Configuracao::class.java)) }
        btnNotificacao.setOnClickListener { startActivity(Intent(this, TelaRF20Notificacoes::class.java)) }
        btnPesquisarLivros.setOnClickListener { startActivity(Intent(this, TelaRF11TelaDePesquisa::class.java)) }
        btnMinhaLivrariaDashboard.setOnClickListener { startActivity(Intent(this, TelaRF15MinhaLivrariaActivity::class.java)) }
        btnListaDesejo.setOnClickListener { startActivity(Intent(this, TelaRF16ListaDesejosActivity::class.java)) }
        btnAmigosDashboard.setOnClickListener { startActivity(Intent(this, TelaRF17Amigos::class.java)) }
        btnHistoricoDashboard.setOnClickListener { startActivity(Intent(this, TelaRF21Historico::class.java)) }
        btnStatusAluguel.setOnClickListener { startActivity(Intent(this, TelaRF18StatusAluguel::class.java)) }

        // Sair desloga do Firebase antes de voltar para o menu inicial
        btnSair.setOnClickListener { showExitPopup() }

        imgLivroAlienista.setOnClickListener {
            val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            intent.putExtra("LIVRO_ID", "1")
            startActivity(intent)
        }

        // Configurar Barra de Navegação
        NavigationHelper.configurarBarraNavegacao(this)
    }

    private fun showExitPopup() {
        val dialogView = layoutInflater.inflate(R.layout.popup_sair_conta, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Confirmar saída
        dialogView.findViewById<MaterialButton>(R.id.btnConfirmarSair).setOnClickListener {
            dialog.dismiss()

            // OBRIGATÓRIO: Deslogar a sessão do Firebase
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

            val intentSair = Intent(this, MainActivity::class.java)
            intentSair.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intentSair)
            finish()
        }

        // Cancelar
        dialogView.findViewById<TextView>(R.id.btnCancelarSair).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}