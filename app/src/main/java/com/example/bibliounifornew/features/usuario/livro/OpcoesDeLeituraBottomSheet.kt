package com.example.bibliounifornew.features.usuario.livro

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.bibliounifornew.R
import com.example.bibliounifornew.data.AuthRepository
import com.example.bibliounifornew.features.usuario.biblioteca.TelaRF14LeituraActivity
import com.example.bibliounifornew.features.usuario.solicitacao.TelaRF18StatusAluguel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class OpcoesDeLeituraBottomSheet : BottomSheetDialogFragment() {

    private val authRepository = AuthRepository()
    private val db = FirebaseFirestore.getInstance()

    private var livroId: String = ""
    private var titulo: String = ""
    private var autor: String = ""
    private var linkPdf: String = ""
    private var linkAudiobook: String = ""
    private var hasBraille: Boolean = false
    private var setor: String = ""

    companion object {
        const val TAG = "OpcoesDeLeituraBottomSheet"

        private const val ARG_ID = "livroId"
        private const val ARG_TITULO = "titulo"
        private const val ARG_AUTOR = "autor"
        private const val ARG_PDF = "linkPdf"
        private const val ARG_AUDIO = "linkAudiobook"
        private const val ARG_BRAILLE = "hasBraille"
        private const val ARG_SETOR = "setor"

        fun newInstance(
            livroId: String,
            titulo: String,
            autor: String,
            linkPdf: String,
            linkAudiobook: String,
            hasBraille: Boolean,
            setor: String
        ): OpcoesDeLeituraBottomSheet {
            val fragment = OpcoesDeLeituraBottomSheet()
            val args = Bundle().apply {
                putString(ARG_ID, livroId)
                putString(ARG_TITULO, titulo)
                putString(ARG_AUTOR, autor)
                putString(ARG_PDF, linkPdf)
                putString(ARG_AUDIO, linkAudiobook)
                putBoolean(ARG_BRAILLE, hasBraille)
                putString(ARG_SETOR, setor)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            livroId = it.getString(ARG_ID) ?: ""
            titulo = it.getString(ARG_TITULO) ?: ""
            autor = it.getString(ARG_AUTOR) ?: ""
            linkPdf = it.getString(ARG_PDF) ?: ""
            linkAudiobook = it.getString(ARG_AUDIO) ?: ""
            hasBraille = it.getBoolean(ARG_BRAILLE) ?: false
            setor = it.getString(ARG_SETOR) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_opcoes_leitura, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.textTituloBottomSheet)?.text = titulo
        view.findViewById<TextView>(R.id.textAutorBottomSheet)?.text = autor

        val btnPdf = view.findViewById<MaterialButton>(R.id.btnAbrirPdf)
        val btnAudio = view.findViewById<MaterialButton>(R.id.btnAbrirAudiobook)
        val btnBraille = view.findViewById<MaterialButton>(R.id.btnSolicitarBraille)
        val btnAlugar = view.findViewById<MaterialButton>(R.id.btnAlugarFisico)

        // Configura visibilidade/clique PDF
        btnPdf?.setOnClickListener {
            if (linkPdf.isNotBlank()) {
                abrirMidia(linkPdf, "pdf")
                dismiss()
            } else {
                Toast.makeText(requireContext(), "PDF indisponível para este livro no momento.", Toast.LENGTH_SHORT).show()
            }
        }

        // Configura visibilidade/clique Audiobook
        btnAudio?.setOnClickListener {
            if (linkAudiobook.isNotBlank()) {
                abrirMidia(linkAudiobook, "audio")
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Audiobook indisponível para este livro no momento.", Toast.LENGTH_SHORT).show()
            }
        }

        // Configura Visibilidade Braille
        if (hasBraille) {
            btnBraille?.visibility = View.VISIBLE
            btnBraille?.setOnClickListener {
                solicitarBraille()
                dismiss()
            }
        } else {
            btnBraille?.visibility = View.GONE
        }

        btnAlugar?.setOnClickListener {
            // Redireciona para a tela de leitura que já tem a lógica de aluguel e localização
            val intent = Intent(requireContext(), TelaRF14LeituraActivity::class.java)
            intent.putExtra("LIVRO_ID", livroId)
            startActivity(intent)
            dismiss()
        }
    }

    private fun abrirMidia(url: String, tipo: String) {
        if (url.isBlank()) {
            Toast.makeText(requireContext(), "Arquivo indisponível no momento.", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(url), if (tipo == "pdf") "application/pdf" else "audio/*")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(Intent.createChooser(intent, "Abrir com..."))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erro ao abrir mídia.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun solicitarBraille() {
        val uid = authRepository.getUsuarioAtual()?.uid ?: return
        val dados = hashMapOf(
            "usuarioId" to uid,
            "livroId" to livroId,
            "titulo" to titulo,
            "tipo" to "braille",
            "status" to "pendente",
            "dataSolicitacao" to System.currentTimeMillis()
        )

        db.collection("solicitacoes_acessibilidade")
            .add(dados)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Solicitação de Braille enviada!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Erro ao solicitar.", Toast.LENGTH_SHORT).show()
            }
    }
}
