package com.example.bibliounifornew.usuario

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R

class TelaRF14LeituraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf14_leitura)

        // Botão Alugar
        findViewById<Button>(R.id.buttonAlugarLivro).setOnClickListener {
            showPopupAlugar()
        }

        // Botão Procurar
        findViewById<Button>(R.id.buttonProcurarLivro).setOnClickListener {
            val intent = Intent(this, TelaRF12TelaDoLivro::class.java)
            startActivity(intent)
        }

        // Botão Abrir PDF
        findViewById<Button>(R.id.buttonAbrirPdfLivro).setOnClickListener {
            abrirPdf()
        }

        // Botão Abrir Audiobook
        findViewById<Button>(R.id.buttonAbrirAudioLivro).setOnClickListener {
            abrirAudiobook()
        }

        // Botão Reservar
        findViewById<Button>(R.id.buttonReservarLivro).setOnClickListener {
            val intent = Intent(this, TelaRF19SolicitacoesTermosCondicoes::class.java)
            startActivity(intent)
        }

        // Botão Setor Localizado
        findViewById<Button>(R.id.buttonSetorLivro).setOnClickListener {
            showPopupSetor()
        }
    }

    private fun showPopupAlugar() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_alugar_livro)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Ajustar largura do popup
        val window = dialog.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val btnConfirmar = dialog.findViewById<Button>(R.id.buttonAdicionarLivro)
        val btnCancelar = dialog.findViewById<TextView>(R.id.textCancelarPopup)

        btnConfirmar.setOnClickListener {
            dialog.dismiss()
            showPopupSucesso()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showPopupSucesso() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_livro_adicionado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Ajustar largura do popup
        val window = dialog.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val btnVerMeusLivros = dialog.findViewById<Button>(R.id.buttonVerMeusLivros)

        btnVerMeusLivros.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, TelaRF18StatusAluguel::class.java)
            startActivity(intent)
        }

        dialog.show()
    }

    private fun showPopupSetor() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_setor_localizado)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnVoltar = dialog.findViewById<Button>(R.id.buttonVoltarSetor)
        btnVoltar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun abrirPdf() {
        val pdfUri = Uri.parse("https://www.google.com") // Link de exemplo
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(pdfUri, "application/pdf")
        
        val chooser = Intent.createChooser(intent, "Abrir PDF com...")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        } else {
            // Fallback para browser caso não tenha leitor PDF
            val browserIntent = Intent(Intent.ACTION_VIEW, pdfUri)
            startActivity(browserIntent)
        }
    }

    private fun abrirAudiobook() {
        val audioUri = Uri.parse("https://www.spotify.com") // Link de exemplo
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(audioUri, "audio/*")

        val chooser = Intent.createChooser(intent, "Ouvir Audiobook com...")
        startActivity(chooser)
    }
}
