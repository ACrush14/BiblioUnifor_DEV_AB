package com.example.bibliounifornew.usuario

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TelaRF18CalendarioRenovacao : AppCompatActivity() {

    private lateinit var editDataManual: EditText
    private lateinit var calendarView: CalendarView
    private lateinit var textMesAno: TextView
    private val calendar = Calendar.getInstance()
    private var isUpdating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.telarf18_2_calendario_renovacao)

        editDataManual = findViewById(R.id.editDataManual)
        calendarView = findViewById(R.id.calendarViewRenovacaoTela)
        textMesAno = findViewById(R.id.textMesAnoCalendario)

        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltarCalendario)
        val btnEditar = findViewById<ImageButton>(R.id.btnEditarDataManual)
        val btnAnterior = findViewById<ImageButton>(R.id.btnMesAnteriorCalendario)
        val btnProximo = findViewById<ImageButton>(R.id.btnProximoMesCalendario)
        val btnConfirmar = findViewById<TextView>(R.id.btnConfirmarRenovacaoTela)

        // Inicializa com a data atual formatada
        atualizarDataExibida()
        atualizarMesAnoTexto()

        btnVoltar.setOnClickListener { finish() }

        // Lápis: Habilita edição manual no EditText
        btnEditar.setOnClickListener {
            editDataManual.isEnabled = true
            editDataManual.requestFocus()
            Toast.makeText(this, "Edição manual habilitada", Toast.LENGTH_SHORT).show()
        }

        // Sincroniza EditText com CalendarView
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
                        // Data inválida, ignora sincronização
                    }
                }
            }
        })

        // Seta Esquerda: Mês anterior
        btnAnterior.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            calendarView.date = calendar.timeInMillis
            atualizarMesAnoTexto()
            atualizarDataExibida()
        }

        // Seta Direita: Próximo mês
        btnProximo.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            calendarView.date = calendar.timeInMillis
            atualizarMesAnoTexto()
            atualizarDataExibida()
        }

        // Sincroniza CalendarView com cliques
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            if (isUpdating) return@setOnDateChangeListener
            isUpdating = true
            calendar.set(year, month, dayOfMonth)
            atualizarDataExibida()
            atualizarMesAnoTexto()
            isUpdating = false
        }

        // Confirmar Renovação
        btnConfirmar.setOnClickListener {
            val novaData = editDataManual.text.toString()
            if (novaData.isNotEmpty()) {
                // Simulação de sucesso e retorno de dados para a tela anterior
                val intent = Intent()
                intent.putExtra("nova_data", novaData)
                setResult(RESULT_OK, intent)
                
                Toast.makeText(this, "Renovação realizada com sucesso", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Informe uma data válida", Toast.LENGTH_SHORT).show()
            }
        }
    }

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