package com.example.bibliounifornew.features.usuario.livro

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.graphics.drawable.ColorDrawable
import android.graphics.Color
import coil.load
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.data.UsuarioRepository
import com.example.bibliounifornew.features.usuario.biblioteca.TelaRF15MinhaLivrariaActivity
import com.example.bibliounifornew.features.usuario.perfil.NavigationHelper
import com.example.bibliounifornew.features.usuario.solicitacao.TelaRF18StatusAluguel
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TelaRF16ListaDesejosActivity : AppCompatActivity() {

    private val authRepository    = AuthRepository()
    private val usuarioRepository = UsuarioRepository()
    private val db                = FirebaseFirestore.getInstance()

    private lateinit var adapter  : ListaDesejosAdapter
    private val listaDesejos      = mutableListOf<ItemListaDesejos>()
    private var usuarioId         : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf16_lista_desejos)

        // ─── RECYCLER VIEW ────────────────────────────────────────────────────
        val recycler = findViewById<RecyclerView>(R.id.recyclerListaDesejos)
        adapter = ListaDesejosAdapter(
            lista      = listaDesejos,
            onLivraria = { item -> adicionarNaLivraria(item) },
            onAlugar   = { item -> 
                if (item.disponivel) {
                    showPopupAlugar(item)
                } else {
                    Toast.makeText(this, "Sinto muito, \"${item.titulo}\" está indisponível no momento.", Toast.LENGTH_LONG).show()
                }
            },
            onExcluir  = { item, pos -> excluirDaLista(item, pos) }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // ─── CABEÇALHO ────────────────────────────────────────────────────────
        val textNome    = findViewById<TextView>(R.id.textNomeUsuarioDesejos)
        val imagePerfil = findViewById<ImageView?>(R.id.imageUsuarioDesejos)
        val usuarioAtual = authRepository.getUsuarioAtual()

        if (usuarioAtual != null) {
            usuarioId = usuarioAtual.uid
            textNome?.text = "Carregando..."

            usuarioRepository.buscarPerfilUsuario(usuarioId) { sucesso, dados, _ ->
                if (sucesso && dados != null) {
                    textNome?.text = dados["nome"] as? String ?: "Usuário"
                    val fotoUrl = dados["fotoUrl"] as? String ?: ""
                    if (fotoUrl.isNotEmpty()) {
                        imagePerfil?.load(fotoUrl) {
                            placeholder(R.drawable.user_placeholder)
                            error(R.drawable.user_placeholder)
                        }
                    }
                } else {
                    textNome?.text = "Usuário"
                }
            }

            carregarListaDesejos()
        } else {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }

        NavigationHelper.configurarBarraNavegacao(this)
    }

    // ─── CARREGAR DO FIRESTORE ────────────────────────────────────────────────

    /**
     * PERF-2: Elimina o padrão N+1 — lê todos os campos diretamente de cada
     * documento da coleção lista_desejos, sem joins adicionais.
     *
     * Campos lidos e fallbacks:
     *   coverUrl   → doc["coverUrl"] ?: "" (placeholder exibido pelo adapter)
     *   disponivel → doc["disponivel"] ?: true (otimista para docs sem o campo)
     *   titulo     → doc["titulo"] ?: doc["bookTitle"] ?: sem_titulo
     *   autor      → doc["autor"] ?: doc["bookAuthor"] ?: sem_autor
     *
     * Docs antigos sem coverUrl mostram o drawable placeholder — aceitável.
     * Docs novos (gravados via salvarListaDesejos com coverUrl incluído) mostram
     * a capa correta sem nenhuma query extra.
     */
    private fun carregarListaDesejos() {
        val tvVazio  = findViewById<TextView>(R.id.tvDesejosVazios)
        val recycler = findViewById<RecyclerView>(R.id.recyclerListaDesejos)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection("lista_desejos")
                    .whereEqualTo("usuarioId", usuarioId)
                    .get()
                    .await()

                val itens = result.documents.mapNotNull { doc ->
                    val livroId = doc.getString("livroId") ?: return@mapNotNull null
                    val titulo  = doc.getString("titulo")
                        ?: doc.getString("bookTitle")
                        ?: getString(R.string.sem_titulo)
                    val autor   = doc.getString("autor")
                        ?: doc.getString("bookAuthor")
                        ?: getString(R.string.sem_autor)
                    // coverUrl desnormalizado — vazio para docs antigos (adapter usa placeholder)
                    val coverUrl   = doc.getString("coverUrl") ?: ""
                    // disponivel desnormalizado — otimista para docs sem o campo
                    val disponivel = doc.getBoolean("disponivel") ?: true
                    ItemListaDesejos(
                        docId      = doc.id,
                        livroId    = livroId,
                        titulo     = titulo,
                        autor      = autor,
                        coverUrl   = coverUrl,
                        disponivel = disponivel
                    )
                }

                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    val vazio = itens.isEmpty()
                    tvVazio?.visibility  = if (vazio) View.VISIBLE else View.GONE
                    recycler?.visibility = if (vazio) View.GONE   else View.VISIBLE
                    adapter.atualizarLista(itens)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    Toast.makeText(
                        this@TelaRF16ListaDesejosActivity,
                        "Erro ao carregar lista de desejos: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // ─── AÇÕES ────────────────────────────────────────────────────────────────

    private fun adicionarNaLivraria(item: ItemListaDesejos) {
        val dados = hashMapOf(
            "usuarioId"     to usuarioId,
            "livroId"       to item.livroId,
            "titulo"        to item.titulo,
            "autor"         to item.autor,
            "statusLeitura" to "Não Lido",
            "adicionadoEm"  to System.currentTimeMillis()
        )
        db.collection("biblioteca_usuarios").document("${usuarioId}_${item.livroId}")
            .set(dados, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "\"${item.titulo}\" adicionado à sua Livraria!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao adicionar à Livraria.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun excluirDaLista(item: ItemListaDesejos, position: Int) {
        usuarioRepository.removerDaListaDesejos(usuarioId, item.livroId) { sucesso ->
            adapter.removerItem(position)
            val msg = if (sucesso) "\"${item.titulo}\" removido da lista de desejos."
                      else "Removido localmente (falha no servidor)."
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // ─── POPUPS ───────────────────────────────────────────────────────────────

    private fun showPopupAlugar(item: ItemListaDesejos) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_alugar_livro)
        
        // Garante fundo transparente e centralização
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                (resources.displayMetrics.widthPixels * 0.90).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        val txtTitulo   = dialog.findViewById<TextView>(R.id.textTituloPopupAlugar)
        val btnAlugar   = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonAdicionarLivro)
        val btnCancelar = dialog.findViewById<TextView>(R.id.textCancelarPopup)

        // Título formatado com aspas
        txtTitulo?.text = "Você deseja alugar o livro\n\"${item.titulo}\"?"

        btnAlugar?.setOnClickListener {
            dialog.dismiss()
            gravarSolicitacaoAluguel(item)
        }
        
        btnCancelar?.setOnClickListener { 
            dialog.dismiss() 
        }
        
        dialog.show()
    }

    private fun gravarSolicitacaoAluguel(item: ItemListaDesejos) {
        if (usuarioId.isEmpty()) return

        val dados = hashMapOf(
            "uidAluno"        to usuarioId,
            "idLivro"         to item.livroId,
            "status"          to "pendente",
            "dataSolicitacao" to System.currentTimeMillis()
        )

        db.collection("solicitacoes_emprestimo")
            .add(dados)
            .addOnSuccessListener {
                if (!isFinishing && !isDestroyed) showPopupLivroAdicionado()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao solicitar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPopupLivroAdicionado() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_livro_adicionado)
        
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                (resources.displayMetrics.widthPixels * 0.90).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        val btnVerMeusLivros = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonVerMeusLivros)
        btnVerMeusLivros?.setOnClickListener {
            dialog.dismiss()
            // Direciona para Status de Aluguel (RF18) como na imagem
            val intent = Intent(this, com.example.bibliounifornew.features.usuario.solicitacao.TelaRF18StatusAluguel::class.java)
            startActivity(intent)
            finish()
        }
        dialog.show()
    }
}
