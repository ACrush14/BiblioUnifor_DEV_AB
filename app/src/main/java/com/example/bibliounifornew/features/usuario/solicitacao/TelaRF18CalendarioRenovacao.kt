package com.example.bibliounifornew.features.usuario.solicitacao

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TelaRF18CalendarioRenovacao : AppCompatActivity() {

    private lateinit var editDataManual : EditText
    private lateinit var calendarView   : CalendarView
    private lateinit var textMesAno     : TextView
    private val calendar   = Calendar.getInstance()
    private var isUpdating = false

    // ─── ESTADO RECEBIDO DE TelaRF18StatusAluguel ────────────────────────────
    // DOC_ID é o ID do documento em "solicitacoes_emprestimo" a ser renovado.
    // Sem ele a tela não pode escrever no Firestore — finish() imediato.
    private var docId : String = ""

    private val db = FirebaseFirestore.getInstance()

    companion object {
        /** Número máximo de renovações por empréstimo (RF18 — regra de negócio) */
        private const val MAX_RENOVACOES = 1
        /** Período de renovação: 15 dias em ms (alinhado com RF34.PRAZO_MS) */
        private const val PRAZO_RENOVACAO_MS = 15L * 24L * 60L * 60L * 1_000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf18_2_calendario_renovacao)

        docId = intent.getStringExtra("DOC_ID") ?: ""

        editDataManual  = findViewById(R.id.editDataManual)
        calendarView    = findViewById(R.id.calendarViewRenovacaoTela)
        textMesAno      = findViewById(R.id.textMesAnoCalendario)

        val btnVoltar   = findViewById<ImageButton>(R.id.btnVoltarCalendario)
        val btnEditar   = findViewById<ImageButton>(R.id.btnEditarDataManual)
        val btnAnterior = findViewById<ImageButton>(R.id.btnMesAnteriorCalendario)
        val btnProximo  = findViewById<ImageButton>(R.id.btnProximoMesCalendario)
        val btnConfirmar = findViewById<TextView>(R.id.btnConfirmarRenovacaoTela)

        // Inicializa com hoje + 15 dias como data sugerida de devolução
        calendar.add(Calendar.DAY_OF_MONTH, 15)
        calendarView.date = calendar.timeInMillis
        atualizarDataExibida()
        atualizarMesAnoTexto()

        btnVoltar.setOnClickListener { finish() }

        // Lápis: habilita edição manual no EditText
        btnEditar.setOnClickListener {
            editDataManual.isEnabled = true
            editDataManual.requestFocus()
            Toast.makeText(this,
                getString(R.string.msg_edicao_manual_habilitada),
                Toast.LENGTH_SHORT).show()
        }

        // Sincroniza EditText → CalendarView
        editDataManual.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                val input = s.toString()
                if (input.length == 10) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                    sdf.isLenient = false
                    try {
                        val date = sdf.parse(input)
                        if (date != null) {
                            isUpdating = true
                            calendar.time = date
                            calendarView.date = calendar.timeInMillis
                            atualizarMesAnoTexto()
                            isUpdating = false
                        }
                    } catch (e: ParseException) {
                        // Data inválida — aguarda o usuário terminar de digitar
                    }
                }
            }
        })

        btnAnterior.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            calendarView.date = calendar.timeInMillis
            atualizarMesAnoTexto()
            atualizarDataExibida()
        }

        btnProximo.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            calendarView.date = calendar.timeInMillis
            atualizarMesAnoTexto()
            atualizarDataExibida()
        }

        // Sincroniza CalendarView → EditText
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            if (isUpdating) return@setOnDateChangeListener
            isUpdating = true
            calendar.set(year, month, dayOfMonth)
            atualizarDataExibida()
            atualizarMesAnoTexto()
            isUpdating = false
        }

        // ─── CONFIRMAR RENOVAÇÃO ─────────────────────────────────────────────
        // CRÍTICO: antes essa função apenas chamava setResult()+finish() sem
        // nenhuma escrita no Firestore. Agora escreve de verdade.
        btnConfirmar.setOnClickListener {
            val novaData = editDataManual.text.toString().trim()
            if (novaData.isEmpty()) {
                Toast.makeText(this,
                    getString(R.string.erro_data_invalida_renovacao),
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (docId.isEmpty()) {
                Toast.makeText(this,
                    getString(R.string.erro_id_emprestimo_invalido),
                    Toast.LENGTH_SHORT).show()
                finish()
                return@setOnClickListener
            }
            btnConfirmar.isEnabled = false
            confirmarRenovacao(novaData, btnConfirmar)
        }
    }

    // ─── RENOVAÇÃO COM FIRESTORE ──────────────────────────────────────────────

    /**
     * 1. Lê o documento de empréstimo para verificar o contador de renovações.
     * 2. Se já atingiu [MAX_RENOVACOES], exibe popup de limite e retorna.
     * 3. Caso contrário, atualiza [dataDevolucao], [status] e incrementa
     *    o campo [renovacoes] atomicamente via [update].
     */
    private fun confirmarRenovacao(novaDataStr: String, btnConfirmar: View) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val doc = db.collection("solicitacoes_emprestimo")
                    .document(docId).get().await()

                val renovacoesFeitas = (doc.getLong("renovacoes") ?: 0L).toInt()

                if (renovacoesFeitas >= MAX_RENOVACOES) {
                    withContext(Dispatchers.Main) {
                        if (isFinishing || isDestroyed) return@withContext
                        btnConfirmar.isEnabled = true
                        exibirPopupLimiteRenovacao()
                    }
                    return@launch
                }

                // Converte a data selecionada para milissegundos; se falhar usa +15d
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                val novaDataMs = try {
                    sdf.parse(novaDataStr)?.time
                        ?: (System.currentTimeMillis() + PRAZO_RENOVACAO_MS)
                } catch (e: ParseException) {
                    System.currentTimeMillis() + PRAZO_RENOVACAO_MS
                }

                db.collection("solicitacoes_emprestimo").document(docId)
                    .update(mapOf(
                        "dataDevolucao" to novaDataMs,
                        "status"        to "ativo",
                        "renovacoes"    to (renovacoesFeitas + 1)
                    ))
                    .await()

                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    Toast.makeText(
                        this@TelaRF18CalendarioRenovacao,
                        getString(R.string.msg_renovacao_realizada, novaDataStr),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    btnConfirmar.isEnabled = true
                    Toast.makeText(
                        this@TelaRF18CalendarioRenovacao,
                        getString(R.string.erro_conexao_banco),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // ─── POPUP LIMITE DE RENOVAÇÕES ───────────────────────────────────────────

    private fun exibirPopupLimiteRenovacao() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_limite_renovacao)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.findViewById<MaterialButton>(R.id.buttonVoltarPopupRenovacao)
            ?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ─── HELPERS DE DATA ─────────────────────────────────────────────────────

    private fun atualizarDataExibida() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        editDataManual.setText(sdf.format(calendar.time))
    }

    private fun atualizarMesAnoTexto() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))
        val mesAno = sdf.format(calendar.time)
        textMesAno.text = mesAno.replaceFirstChar { it.uppercase() }
    }
}
