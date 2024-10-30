package com.example.app_imc

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var editTextWeight: EditText
    private lateinit var editTextHeight: EditText
    private lateinit var textViewResult: TextView
    private lateinit var bmiDao: BmiDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        editTextWeight = findViewById(R.id.editTextWeight)
        editTextHeight = findViewById(R.id.editTextHeight)
        textViewResult = findViewById(R.id.textViewResult)
        val buttonCalculate: Button = findViewById(R.id.buttonCalculate)
        val buttonSave: Button = findViewById(R.id.buttonSave)
        val buttonLoad: Button = findViewById(R.id.buttonLoad)
        val buttonUpdate: Button = findViewById(R.id.buttonUpdate)
        val buttonDelete: Button = findViewById(R.id.buttonDelete)

        // Inicializar o banco de dados
        val db = BmiDatabase.getDatabase(this)
        bmiDao = db.bmiDao()

        // Configurações dos botões
        buttonCalculate.setOnClickListener { calculateBMI() }
        buttonSave.setOnClickListener { saveBMI() }

        // Botão para carregar IMCs
        buttonLoad.setOnClickListener {
            lifecycleScope.launch {
                val bmiList = loadAllBmiEntries()
                if (bmiList.isNotEmpty()) {
                    val resultText = bmiList.joinToString(separator = "\n") { entry ->
                        "IMC: %.2f | Altura: %.2f | Peso: %.2f".format(entry.bmi, entry.height, entry.weight)
                    }
                    textViewResult.text = resultText
                } else {
                    textViewResult.text = "Nenhum IMC encontrado."
                }
            }
        }

        // Botão para atualizar IMCs
        buttonUpdate.setOnClickListener {
            val weightStr = editTextWeight.text.toString()
            val heightStr = editTextHeight.text.toString()

            if (weightStr.isNotEmpty() && heightStr.isNotEmpty()) {
                val weight = weightStr.toDouble()
                val height = heightStr.toDouble()

                lifecycleScope.launch {
                    val bmiEntry = loadBMIEntryByWeightAndHeight(weight, height)
                    if (bmiEntry != null) {
                        updateBMI(bmiEntry)
                    } else {
                        textViewResult.text = "Nenhum IMC encontrado para atualizar."
                    }
                }
            } else {
                textViewResult.text = "Preencha todos os campos."
            }
        }

        // Botão para deletar IMCs
        buttonDelete.setOnClickListener {
            val weightStr = editTextWeight.text.toString()
            val heightStr = editTextHeight.text.toString()

            if (weightStr.isNotEmpty() && heightStr.isNotEmpty()) {
                val weight = weightStr.toDouble()
                val height = heightStr.toDouble()

                lifecycleScope.launch {
                    val bmiEntry = loadBMIEntryByWeightAndHeight(weight, height)
                    if (bmiEntry != null) {
                        deleteBMI(bmiEntry)
                    } else {
                        textViewResult.text = "Nenhum IMC encontrado para deletar."
                    }
                }
            } else {
                textViewResult.text = "Preencha todos os campos."
            }
        }

        // Configuração para aplicar as margens da janela
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun calculateBMI() {
        val weightStr = editTextWeight.text.toString()
        val heightStr = editTextHeight.text.toString()

        if (weightStr.isNotEmpty() && heightStr.isNotEmpty()) {
            val weight = weightStr.toDouble()
            val height = heightStr.toDouble()
            val bmi = weight / (height * height)
            textViewResult.text = String.format("IMC: %.2f", bmi)
        } else {
            textViewResult.text = "Preencha todos os campos."
        }
    }

    private fun saveBMI() {
        val bmiStr = textViewResult.text.toString()
        if (bmiStr.isNotEmpty()) {
            val bmiValue = bmiStr.substringAfter("IMC: ").toDoubleOrNull()

            // Adicionando trim e replace
            val heightInput = editTextHeight.text.toString().trim().replace(',', '.')
            val weightInput = editTextWeight.text.toString().trim().replace(',', '.')

            val height = heightInput.toDoubleOrNull()
            val weight = weightInput.toDoubleOrNull()

            if (bmiValue != null && height != null && weight != null) {
                val bmiEntry = Bmi(bmi = bmiValue, height = height, weight = weight)

                lifecycleScope.launch {
                    try {
                        bmiDao.insert(bmiEntry)
                        textViewResult.text = "IMC salvo com sucesso!"
                    } catch (e: Exception) {
                        textViewResult.text = "Erro ao salvar IMC: ${e.message}"
                    }
                }
            } else {
                textViewResult.text = "Erro ao salvar IMC: campos inválidos."
            }
        }
    }

    private suspend fun loadAllBmiEntries(): List<Bmi> {
        return bmiDao.getAllBmiEntries()
    }

    private suspend fun loadBMIEntryByWeightAndHeight(weight: Double, height: Double): Bmi? {
        return bmiDao.getAllBmiEntries().find { it.weight == weight && it.height == height }
    }

    private fun updateBMI(bmiEntry: Bmi) {
        val weightStr = editTextWeight.text.toString()
        val heightStr = editTextHeight.text.toString()

        if (weightStr.isNotEmpty() && heightStr.isNotEmpty()) {
            val weight = weightStr.toDouble()
            val height = heightStr.toDouble()
            val bmiValue = weight / (height * height)

            val updatedBmiEntry = bmiEntry.copy(bmi = bmiValue, height = height, weight = weight)

            lifecycleScope.launch {
                try {
                    bmiDao.updateBmi(updatedBmiEntry)
                    textViewResult.text = "IMC atualizado com sucesso!"
                } catch (e: Exception) {
                    textViewResult.text = "Erro ao atualizar IMC: ${e.message}"
                }
            }
        } else {
            textViewResult.text = "Preencha todos os campos."
        }
    }

    private fun deleteBMI(bmiEntry: Bmi) {
        lifecycleScope.launch {
            try {
                bmiDao.deleteBmi(bmiEntry)
                textViewResult.text = "IMC deletado com sucesso!"
            } catch (e: Exception) {
                textViewResult.text = "Erro ao deletar IMC: ${e.message}"
            }
        }
    }
}