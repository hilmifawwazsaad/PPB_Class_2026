package com.example.kalkulatorsederhana

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CalculatorViewModel : ViewModel() {
    var displayValue by mutableStateOf("0")
    var infoText    by mutableStateOf("Siap menghitung")
    private var firstOperand: Double? = null
    private var pendingOperator: String? = null
    private var secondOperandText = ""
    private var isEnteringSecond = false
    private var showingFinalResult = false
    private var lastResult: Double? = null

    fun onDigitClick(digit: String) {
        if (showingFinalResult) {
            resetState()
        }

        if (!isEnteringSecond) {
            displayValue = if (displayValue == "0") digit else displayValue + digit
            infoText = "Input angka pertama"
        } else {
            secondOperandText += digit
            val first  = firstOperand ?: 0.0
            val second = secondOperandText.toDoubleOrNull() ?: 0.0
            val result = calculate(first, pendingOperator ?: "+", second)

            displayValue = if (result != null) {
                "${format(first)} ${pendingOperator} $secondOperandText = ${format(result)}"
            } else {
                "${format(first)} ${pendingOperator} $secondOperandText = Error"
            }
            infoText = "Input angka kedua (preview hasil)"
        }
    }

    fun onOperatorClick(op: String) {
        if (showingFinalResult) {
            val base = lastResult ?: 0.0
            resetState()
            firstOperand    = base
            pendingOperator = op
            isEnteringSecond = true
            displayValue    = "${format(base)} $op "
            infoText        = "Lanjut dari hasil: ${format(base)}"
            return
        }

        if (!isEnteringSecond) {
            firstOperand    = displayValue.toDoubleOrNull() ?: 0.0
            pendingOperator = op
            isEnteringSecond = true
            displayValue    = "${format(firstOperand!!)} $op "
            infoText        = "Pilih angka kedua"
        } else {
            val first  = firstOperand ?: 0.0
            val second = secondOperandText.toDoubleOrNull() ?: 0.0
            val result = calculate(first, pendingOperator ?: "+", second)

            if (result != null) {
                firstOperand       = result
                pendingOperator    = op
                secondOperandText  = ""
                displayValue       = "${format(result)} $op "
                infoText           = "Chain: ${format(result)} $op"
            } else {
                resetState()
                infoText = "Error: Pembagian dengan nol"
            }
        }
    }

    fun onEqualsClick() {
        if (!isEnteringSecond || secondOperandText.isEmpty()) return

        val first  = firstOperand ?: 0.0
        val second = secondOperandText.toDoubleOrNull() ?: 0.0
        val result = calculate(first, pendingOperator ?: "+", second)

        if (result != null) {
            lastResult         = result
            displayValue       = "${format(first)} ${pendingOperator} $second = ${format(result)}"
            infoText           = "Hasil Akhir. Operator → lanjut | Angka → mulai baru"
            showingFinalResult = true
        } else {
            resetState()
            infoText = "Error: Pembagian dengan nol"
        }
    }

    fun onClearClick() {
        resetState()
        infoText = "Direset"
    }

    private fun resetState() {
        displayValue       = "0"
        firstOperand       = null
        pendingOperator    = null
        secondOperandText  = ""
        isEnteringSecond   = false
        showingFinalResult = false
        lastResult         = null
    }

    private fun format(v: Double) =
        if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()

    private fun calculate(a: Double, op: String, b: Double): Double? = when (op) {
        "+" -> a + b
        "-" -> a - b
        "*" -> a * b
        "/" -> if (b != 0.0) a / b else null
        else -> null
    }
}