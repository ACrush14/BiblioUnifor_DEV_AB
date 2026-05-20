package com.example.bibliounifornew.usuario

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bibliounifornew.R
import com.google.android.material.button.MaterialButton
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TelaCalendario : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var editDataSelecionada: EditText
    private lateinit var textMesAno: TextView
    private var dataFormatada: String = ""
    private val calendar = Calendar.getInstance()
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val sdfMesAno = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.popup_calendario_publicacao)

        calendarView = findViewById(R.id.calendarViewPublicacao)
        editDataSelecionada = findViewById(R.id.textDataSelecionada)
        textMesAno = findViewById(R.id.textMesAno)
        val btnConfirmar = findViewById<MaterialButton>(R.id.btnConfirmar)
        val textVoltar = findViewById<TextView>(R.id.textVoltar)
        val btnMesAnterior = findViewById<ImageButton>(R.id.btnMesAnterior)
        val btnProximoMes = findViewById<ImageButton>(R.id.btnProximoMes)
        val btnEditarData = findViewById<ImageButton>(R.id.btnEditarData)

        // Inicialização
        dataFormatada = sdf.format(calendar.time)
        editDataSelecionada.setText(dataFormatada)
        atualizarMesAno()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            dataFormatada = sdf.format(calendar.time)
            editDataSelecionada.setText(dataFormatada)
            atualizarMesAno()
        }

        btnMesAnterior.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            calendarView.date = calendar.timeInMillis
            atualizarInterface()
        }

        btnProximoMes.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            calendarView.date = calendar.timeInMillis
            atualizarInterface()
        }

        btnEditarData.setOnClickListener {
            editDataSelecionada.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editDataSelecionada, InputMethodManager.SHOW_IMPLICIT)
        }

        // Sincronizar campo de texto com o calendário enquanto digita e aplicar máscara
        editDataSelecionada.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "##/##/####"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) {
                    isUpdating = false
                    return
                }

                val str = s.toString().replace("[^\\d]".toRegex(), "")
                var formatted = ""
                var i = 0
                for (m in mask.toCharArray()) {
                    if (m != '#' && str.length > i) {
                        formatted += m
                        continue
                    }
                    try {
                        formatted += str[i]
                    } catch (e: Exception) {
                        break
                    }
                    i++
                }

                isUpdating = true
                editDataSelecionada.setText(formatted)
                editDataSelecionada.setSelection(formatted.length)
            }

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.length == 10) {
                    try {
                        val date = sdf.parse(input)
                        if (date != null) {
                            calendar.time = date
                            calendarView.date = calendar.timeInMillis
                            atualizarMesAno()
                        }
                    } catch (e: ParseException) {
                        // Data inválida
                    }
                }
            }
        })

        btnConfirmar.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("SELECTED_DATE", editDataSelecionada.text.toString())
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        textVoltar.setOnClickListener {
            finish()
        }
    }

    private fun atualizarMesAno() {
        textMesAno.text = sdfMesAno.format(calendar.time).replaceFirstChar { it.uppercase() }
    }

    private fun atualizarInterface() {
        dataFormatada = sdf.format(calendar.time)
        editDataSelecionada.setText(dataFormatada)
        atualizarMesAno()
    }
}
