package com.example.bibliounifornew.adm

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TelaRF34FinanceiroADM : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    // Documento Firestore do aluguel vencido actualmente exibido
    private var docIdAtual     : String = ""
    private var uidAlunoAtual  : String = ""
    private var idLivroAtual   : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf34_finaceiro_adm)

        val btnVerPendentes  = findViewById<Button>(R.id.btnPendentesRetirada)
        val btnRenovar       = findViewById<Button>(R.id.btnRenovarAluguel)
        val cardLivroVencido = findViewById<MaterialCardView>(R.id.cardLivroVencido)
        val iconMais         = findViewById<View>(R.id.iconMais)

        // Carrega o primeiro aluguel vencido
        carregarVencidos(cardLivroVencido)

        // ─── VER PENDENTES ────────────────────────────────────────────────────
        btnVerPendentes?.setOnClickListener {
            exibirPopupPendentes(cardLivroVencido)
        }

        // ─── TRÊS PONTOS → Detalhe do livro ───────────────────────────────────
        iconMais?.setOnClickListener {
            val intent = Intent(this, TelaRF37InfoLivroADM::class.java)
            intent.putExtra("LIVRO_ID", idLivroAtual)
            startActivity(intent)
        }

        // ─── RENOVAR ALUGUEL ─────────────────────────────────────────────────
        btnRenovar?.setOnClickListener {
            if (docIdAtual.isEmpty()) {
                Toast.makeText(this, "Nenhum aluguel vencido carregado.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btnRenovar.isEnabled = false
            renovarAluguel(cardLivroVencido) { btnRenovar.isEnabled = true }
        }

        NavigationHelperADM.configurarBarraNavegacao(this)
    }

    /**
     * Busca o aluguel vencido mais antigo (data_devolucao < agora).
     * Fallback: primeiro com status "atrasado" ou "ativo".
     */
    private fun carregarVencidos(card: MaterialCardView) {
        val agora = System.currentTimeMillis()

        db.collection("solicitacoes_emprestimo")
            .whereLessThan("dataDevolucao", agora)
            .whereEqualTo("status", "ativo")
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val doc = result.documents[0]
                    docIdAtual    = doc.id
                    uidAlunoAtual = doc.getString("uidAluno") ?: ""
                    idLivroAtual  = doc.getString("idLivro")  ?: ""
                    val dataMs    = doc.getLong("dataSolicitacao") ?: 0L
                    val multaVal  = doc.getLong("multa") ?: 20L

                    // Calcula dias vencidos
                    val diasVencidos = ((agora - (doc.getLong("dataDevolucao") ?: agora)) /
                            (1000 * 60 * 60 * 24)).coerceAtLeast(0)

                    preencherCard(uidAlunoAtual, idLivroAtual, dataMs, diasVencidos, multaVal)
                    card.visibility = View.VISIBLE
                } else {
                    // Fallback: status == "atrasado"
                    carregarVencidosFallback(card)
                }
            }
            .addOnFailureListener {
                carregarVencidosFallback(card)
            }
    }

    private fun carregarVencidosFallback(card: MaterialCardView) {
        db.collection("solicitacoes_emprestimo")
            .whereEqualTo("status", "atrasado")
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val doc = result.documents[0]
                    docIdAtual    = doc.id
                    uidAlunoAtual = doc.getString("uidAluno") ?: ""
                    idLivroAtual  = doc.getString("idLivro")  ?: ""
                    val dataMs    = doc.getLong("dataSolicitacao") ?: 0L
                    val multaVal  = doc.getLong("multa") ?: 20L
                    preencherCard(uidAlunoAtual, idLivroAtual, dataMs, 0L, multaVal)
                    card.visibility = View.VISIBLE
                } else {
                    card.visibility = View.GONE
                    Toast.makeText(this, "Nenhum aluguel vencido encontrado.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                card.visibility = View.GONE
            }
    }

    /**
     * Preenche os TextViews do card com dados do Firestore.
     */
    private fun preencherCard(uidAluno: String, idLivro: String, dataMs: Long, diasVencidos: Long, multa: Long) {
        val txtTitulo  = findViewById<TextView>(R.id.textTituloLivro)
        val txtAutor   = findViewById<TextView>(R.id.textAutorLivro)
        val txtData    = findViewById<TextView>(R.id.textDataLivro)
        val txtNome    = findViewById<TextView>(R.id.textNomeUsuario)
        val txtDias    = findViewById<TextView>(R.id.textDias)
        val txtMulta   = findViewById<TextView>(R.id.textMulta)

        // Formata a data
        if (dataMs > 0L) {
            txtData?.text = SimpleDateFormat("dd MMM yyyy", Locale("pt", "BR")).format(Date(dataMs))
        }
        txtDias?.text  = if (diasVencidos > 0) "$diasVencidos dias vencidos" else "Vencido"
        txtMulta?.text = "Multa auto: R$$multa,00"

        // Join: nome do usuário
        if (uidAluno.isNotEmpty()) {
            db.collection("usuarios").document(uidAluno).get()
                .addOnSuccessListener { u ->
                    txtNome?.text = u.getString("nome") ?: u.getString("email") ?: "Usuário"
                }
        }

        // Join: dados do livro
        if (idLivro.isNotEmpty()) {
            db.collection("livros").document(idLivro).get()
                .addOnSuccessListener { l ->
                    txtTitulo?.text = l.getString("title")  ?: l.getString("titulo") ?: "Título Indisponível"
                    txtAutor?.text  = "por ${l.getString("author") ?: l.getString("autor") ?: "Autor Desconhecido"}"
                }
        }
    }

    /**
     * Renova o aluguel: adiciona 14 dias à dataDevolucao e remove a multa.
     * [onReenableButton] é chamado em ambas as ramificações para restaurar o botão.
     */
    private fun renovarAluguel(card: MaterialCardView, onReenableButton: () -> Unit) {
        val novaDevolucao = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000)

        db.collection("solicitacoes_emprestimo").document(docIdAtual)
            .set(
                mapOf(
                    "dataDevolucao" to novaDevolucao,
                    "status"        to "ativo",
                    "multa"         to 0L
                ),
                SetOptions.merge()
            )
            .addOnSuccessListener {
                // Remove notificação de multa se existir
                db.collection("notificacoes")
                    .whereEqualTo("docAluguel", docIdAtual)
                    .whereEqualTo("tipo", "multa")
                    .get()
                    .addOnSuccessListener { notifs ->
                        for (n in notifs) n.reference.delete()
                    }

                Toast.makeText(this, "Aluguel renovado por 14 dias. Multa removida.", Toast.LENGTH_SHORT).show()
                card.visibility = View.GONE
                docIdAtual = ""
                // Botão some junto com o card — não precisa re-habilitar
            }
            .addOnFailureListener {
                onReenableButton()
                Toast.makeText(this, "Não foi possível renovar o aluguel. Verifique sua conexão.", Toast.LENGTH_SHORT).show()
            }
    }

    // ─── POPUP PENDENTES ─────────────────────────────────────────────────────

    private fun exibirPopupPendentes(cardParaRemover: View) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_pendentes_retirada)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnNotificarAtraso  = dialog.findViewById<Button>(R.id.btnNotificarAtraso)
        val btnNotificarValor   = dialog.findViewById<Button>(R.id.btnNotificarValor)
        val btnConfirmarAluguel = dialog.findViewById<Button>(R.id.btnConfirmacaoAluguel)
        val btnRemoverRegistro  = dialog.findViewById<Button>(R.id.btnRemoverRegistro)

        btnNotificarAtraso?.setOnClickListener {
            if (uidAlunoAtual.isNotEmpty()) {
                val notif = hashMapOf(
                    "uidAluno"    to uidAlunoAtual,
                    "docAluguel"  to docIdAtual,
                    "tipo"        to "atraso",
                    "mensagem"    to "Seu aluguel está atrasado. Regularize para evitar bloqueios.",
                    "criadoEm"    to System.currentTimeMillis()
                )
                db.collection("notificacoes").add(notif)
            }
            Toast.makeText(this, "Aviso de atraso enviado.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnNotificarValor?.setOnClickListener {
            if (uidAlunoAtual.isNotEmpty()) {
                val notif = hashMapOf(
                    "uidAluno"   to uidAlunoAtual,
                    "docAluguel" to docIdAtual,
                    "tipo"       to "multa",
                    "mensagem"   to "Existe valor pendente referente ao seu aluguel.",
                    "criadoEm"   to System.currentTimeMillis()
                )
                db.collection("notificacoes").add(notif)
            }
            Toast.makeText(this, "Valor pendente notificado.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnConfirmarAluguel?.setOnClickListener {
            if (docIdAtual.isNotEmpty()) {
                db.collection("solicitacoes_emprestimo").document(docIdAtual)
                    .set(mapOf("status" to "confirmado"), SetOptions.merge())
            }
            Toast.makeText(this, "Aluguel confirmado.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnRemoverRegistro?.setOnClickListener {
            if (docIdAtual.isNotEmpty()) {
                db.collection("solicitacoes_emprestimo").document(docIdAtual)
                    .delete()
                    .addOnSuccessListener {
                        cardParaRemover.visibility = View.GONE
                        docIdAtual = ""
                    }
            }
            Toast.makeText(this, "Registro removido.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
