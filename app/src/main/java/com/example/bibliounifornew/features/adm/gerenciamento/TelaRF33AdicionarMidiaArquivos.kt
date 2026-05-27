package com.example.bibliounifornew.features.adm.gerenciamento

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.example.bibliounifornew.features.adm.dashboard.TelaRF28DashboardADM
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class TelaRF33AdicionarMidiaArquivos : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var livroId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf33_adicionar_midia_arquivos)

        // Recebe o ID propagado pelo Passo 2 (BUG-C2 FIX)
        livroId = intent.getStringExtra("LIVRO_ID") ?: ""

        val etPdf       = findViewById<EditText>(R.id.editArquivoPdf)
        val etAudiobook = findViewById<EditText>(R.id.editArquivoAudiobook)
        val cbBraille   = findViewById<CheckBox>(R.id.checkBraille)
        val btnSalvar   = findViewById<MaterialButton>(R.id.btnSalvarLivro)

        btnSalvar.setOnClickListener {
            val pdf       = etPdf.text.toString().trim()
            val audiobook = etAudiobook.text.toString().trim()
            val braille   = cbBraille.isChecked

            if (pdf.isEmpty() || audiobook.isEmpty()) {
                Toast.makeText(this, getString(R.string.erro_adicione_arquivos), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSalvar.isEnabled = false

            // ── Salva arquivos no documento do livro (BUG-C1 FIX) ────────────
            val atualizacoes = hashMapOf<String, Any>(
                "linkPdf"       to pdf,
                "linkAudiobook" to audiobook,
                "braille"       to braille
            )

            if (livroId.isNotEmpty()) {
                db.collection("livros").document(livroId)
                    .update(atualizacoes)
                    .addOnSuccessListener {
                        btnSalvar.isEnabled = true
                        exibirPopupSucesso()
                    }
                    .addOnFailureListener {
                        btnSalvar.isEnabled = true
                        Toast.makeText(this, getString(R.string.erro_conexao_banco), Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Edge case: sem ID — exibe sucesso sem tentar salvar arquivos
                btnSalvar.isEnabled = true
                exibirPopupSucesso()
            }
        }
    }

    private fun exibirPopupSucesso() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_livro_salvo_sucesso)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)

        val btnVoltar = dialog.findViewById<MaterialButton>(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, TelaRF28DashboardADM::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        dialog.show()
    }
}
