package com.example.bibliounifornew.features.usuario.amigo

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TelaRF17_3_BuscaAmigos : AppCompatActivity() {

    private val authRepository = AuthRepository()
    private val db             = FirebaseFirestore.getInstance()

    private lateinit var adapter     : BuscaAmigoAdapter
    private lateinit var editBuscar  : EditText

    // Lista base carregada do Firestore (máx 30 docs, sem amigos já existentes)
    // Acedida apenas em IO Thread após população inicial.
    private val listaCompleta = mutableListOf<UsuarioAmigo>()
    private val uidsExcluir   = mutableSetOf<String>()  // próprio uid + amigos atuais

    private var uidAtual: String = ""

    // Job de debounce: cancelado a cada nova digitação — evita N requisições por segundo
    private var debounceJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf17_3_busca_amigos)

        val usuarioAtual = authRepository.getUsuarioAtual()
        if (usuarioAtual == null) {
            startActivity(Intent(this, com.example.bibliounifornew.login.TelaRF03LoginAluno::class.java))
            finish()
            return
        }
        uidAtual = usuarioAtual.uid

        // ─── RECYCLERVIEW ─────────────────────────────────────────────────────
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewBuscaAmigos)
        adapter = BuscaAmigoAdapter(mutableListOf()) { usuario ->
            enviarSolicitacaoAmizade(usuario)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ─── CAMPO DE BUSCA ───────────────────────────────────────────────────
        editBuscar = findViewById(R.id.editBuscarAmigo)
        val btnProcurar = findViewById<MaterialButton>(R.id.buttonBuscarAmigo)

        // Botão: busca imediata (ação explícita — sem debounce)
        btnProcurar.setOnClickListener {
            debounceJob?.cancel()
            executarFiltragemLocal(editBuscar.text.toString().trim())
        }

        // TextWatcher com debounce de 500ms:
        // cancela o job anterior a cada nova letra, espera o usuário parar
        // de digitar antes de processar — evita burst de operações de string.
        editBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                debounceJob?.cancel()
                debounceJob = lifecycleScope.launch {
                    delay(500)                                  // aguarda pausa na digitação
                    executarFiltragemLocal(s?.toString() ?: "") // IO thread internamente
                }
            }
        })

        // ─── CARGA INICIAL ────────────────────────────────────────────────────
        carregarAmigosEUsuarios()
    }

    // ─── CARGA ASSÍNCRONA DO FIRESTORE ────────────────────────────────────────

    /**
     * Usa await() para tornar as chamadas Firestore suspendable no IO Thread,
     * eliminando callbacks aninhados e mantendo a Main Thread completamente livre.
     *
     * Fluxo:
     *   1. Carrega subcoleção de amigos → monta set de UIDs para exclusão
     *   2. Carrega usuários com .limit(30) → filtra e mapeia no IO Thread
     *   3. Atualiza adapter só no Main Thread
     */
    private fun carregarAmigosEUsuarios() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Passo 1: UIDs de amigos atuais (para excluir dos resultados de busca)
                val resultAmigos = db.collection("usuarios")
                    .document(uidAtual)
                    .collection("amigos")
                    .get().await()

                val novosUidsExcluir = resultAmigos.documents.map { it.id }.toMutableSet()
                novosUidsExcluir.add(uidAtual)  // inclui o próprio usuário

                // Passo 2: Usuários com limite rígido — sem .limit(), um banco com
                // 10 000 usuários faria download de todos os docs e processaria no GC
                val resultUsuarios = db.collection("usuarios")
                    .limit(30)
                    .get().await()

                // TODO PROCESSAMENTO DE STRING NO IO THREAD
                // filter + map aqui nunca bloqueia a Main Thread
                val novosUsuarios = resultUsuarios.documents
                    .filter { it.id !in novosUidsExcluir }
                    .map { doc ->
                        UsuarioAmigo(
                            uid     = doc.id,
                            nome    = doc.getString("nome")    ?: "Usuário",
                            usuario = doc.getString("usuario") ?: "",
                            fotoUrl = doc.getString("fotoUrl") ?: ""
                        )
                    }

                // Atualiza estado compartilhado — ainda no IO Thread (antes do Main switch)
                uidsExcluir.clear()
                uidsExcluir.addAll(novosUidsExcluir)
                listaCompleta.clear()
                listaCompleta.addAll(novosUsuarios)

                // Passa para Main APENAS para atualizar a UI
                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    adapter.atualizarLista(novosUsuarios)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isFinishing && !isDestroyed) {
                        Toast.makeText(
                            this@TelaRF17_3_BuscaAmigos,
                            getString(R.string.fmt_erro_buscar_usuarios, e.message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    // ─── FILTRAGEM LOCAL (IO → Main) ──────────────────────────────────────────

    /**
     * Filtra [listaCompleta] em IO Thread com contains — string ops nunca tocam
     * a Main Thread. Chama [adapter.atualizarLista] só após o withContext(Main).
     */
    private fun executarFiltragemLocal(texto: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val termoLower = texto.trim().lowercase()

            // FILTRAGEM NO IO THREAD — sem bloquear a UI
            val filtrados: List<UsuarioAmigo> = if (termoLower.isEmpty()) {
                listaCompleta.toList()
            } else {
                listaCompleta.filter { usuario ->
                    usuario.nome.lowercase().contains(termoLower) ||
                    usuario.usuario.lowercase().contains(termoLower)
                }
            }

            withContext(Dispatchers.Main) {
                if (isFinishing || isDestroyed) return@withContext
                adapter.atualizarLista(filtrados)
                if (filtrados.isEmpty() && termoLower.isNotEmpty()) {
                    Toast.makeText(
                        this@TelaRF17_3_BuscaAmigos,
                        "Nenhum usuário encontrado para \"$texto\".",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // ─── ENVIAR SOLICITAÇÃO DE AMIZADE ────────────────────────────────────────

    /**
     * Usa await() no IO Thread para buscar o nome do remetente e gravar
     * a solicitação — nenhuma operação de rede acontece na Main Thread.
     * Após o sucesso, remove o destinatário diretamente do Adapter (sem recreate
     * ou reload da tela) para evitar o crash por ConcurrentModificationException
     * que ocorria quando executarFiltragemLocal relançava uma coroutine IO paralela
     * enquanto esta ainda modificava listaCompleta.
     */
    private fun enviarSolicitacaoAmizade(destinatario: UsuarioAmigo) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val docRemetente = db.collection("usuarios")
                    .document(uidAtual).get().await()
                val nomeRemetente = docRemetente.getString("nome") ?: "Usuário"

                val dados = hashMapOf(
                    "uidRemetente"    to uidAtual,
                    "uidDestinatario" to destinatario.uid,
                    "nomeRemetente"   to nomeRemetente,
                    "status"          to "pendente",
                    "criadoEm"        to System.currentTimeMillis()
                )

                db.collection("solicitacoes_amizade").add(dados).await()

                // Remove da lista base no IO Thread (sem acesso concorrente ao adapter)
                listaCompleta.remove(destinatario)

                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    // Atualização pontual: remove só o item clicado, sem recarregar a tela
                    adapter.removerUsuario(destinatario)
                    Toast.makeText(
                        this@TelaRF17_3_BuscaAmigos,
                        "Solicitação enviada para ${destinatario.nome}!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("AddAmigo", "Erro: ", e)
                withContext(Dispatchers.Main) {
                    if (!isFinishing && !isDestroyed) {
                        Toast.makeText(
                            this@TelaRF17_3_BuscaAmigos,
                            getString(R.string.fmt_erro_solicitar, e.message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}
