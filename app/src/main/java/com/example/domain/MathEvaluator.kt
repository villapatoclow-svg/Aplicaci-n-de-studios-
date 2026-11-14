package com.example.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object MathEvaluator {

    data class EvalOutcome(
        val formattedResult: String,
        val steps: List<String>
    )

    fun evaluate(expression: String, useDegrees: Boolean = true): Result<EvalOutcome> {
        val cleaned = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "pi")
            .replace("√", "sqrt")
            .replace("²", "^2")
            .replace(",", ".")
            .trim()
        if (cleaned.isEmpty()) return Result.failure(IllegalArgumentException("Ingresa una expresión"))

        return try {
            val steps = mutableListOf<String>()
            val parser = Parser(cleaned, useDegrees, steps)
            val value = parser.parseExpression()
            if (!parser.isAtEnd()) {
                return Result.failure(IllegalArgumentException("Símbolo inesperado cerca de '${parser.peekChar()}'"))
            }
            if (value.isNaN() || value.isInfinite()) {
                return Result.failure(ArithmeticException("Indeterminación matemática (división entre 0 o dominio inválido)"))
            }
            val formatted = formatNumber(value)
            Result.success(EvalOutcome(formattedResult = formatted, steps = steps))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun formatNumber(value: Double): String {
        val rounded = (value * 1_000_000_000.0).roundToLong() / 1_000_000_000.0
        return if (abs(rounded - rounded.toLong()) < 1e-9 && abs(rounded) < 1e14) {
            rounded.toLong().toString()
        } else {
            rounded.toString().trimEnd('0').trimEnd('.')
        }
    }

    private class Parser(
        private val input: String,
        private val useDegrees: Boolean,
        private val steps: MutableList<String>
    ) {
        private var pos = 0

        fun isAtEnd(): Boolean {
            skipSpaces()
            return pos >= input.length
        }

        fun peekChar(): Char = if (pos < input.length) input[pos] else ' '

        private fun skipSpaces() {
            while (pos < input.length && input[pos].isWhitespace()) pos++
        }

        private fun eat(ch: Char): Boolean {
            skipSpaces()
            if (pos < input.length && input[pos] == ch) {
                pos++
                return true
            }
            return false
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> {
                        val y = parseTerm()
                        val res = x + y
                        steps.add("Suma: ${formatNumber(x)} + ${formatNumber(y)} = ${formatNumber(res)}")
                        x = res
                    }
                    eat('-') -> {
                        val y = parseTerm()
                        val res = x - y
                        steps.add("Resta: ${formatNumber(x)} - ${formatNumber(y)} = ${formatNumber(res)}")
                        x = res
                    }
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parsePower()
            while (true) {
                when {
                    eat('*') -> {
                        val y = parsePower()
                        val res = x * y
                        steps.add("Multiplicación: ${formatNumber(x)} × ${formatNumber(y)} = ${formatNumber(res)}")
                        x = res
                    }
                    eat('/') -> {
                        val y = parsePower()
                        if (y == 0.0) throw ArithmeticException("División entre cero")
                        val res = x / y
                        steps.add("División: ${formatNumber(x)} ÷ ${formatNumber(y)} = ${formatNumber(res)}")
                        x = res
                    }
                    eat('%') -> {
                        val y = parsePower()
                        if (y == 0.0) throw ArithmeticException("Módulo entre cero")
                        val res = x % y
                        steps.add("Residuo: ${formatNumber(x)} mod ${formatNumber(y)} = ${formatNumber(res)}")
                        x = res
                    }
                    else -> return x
                }
            }
        }

        private fun parsePower(): Double {
            var x = parseUnary()
            if (eat('^')) {
                val expVal = parsePower()
                val res = x.pow(expVal)
                steps.add("Potencia: ${formatNumber(x)}^${formatNumber(expVal)} = ${formatNumber(res)}")
                x = res
            }
            return x
        }

        private fun parseUnary(): Double {
            if (eat('+')) return parseUnary()
            if (eat('-')) return -parseUnary()
            return parsePostfix()
        }

        private fun parsePostfix(): Double {
            var x = parsePrimary()
            while (eat('!')) {
                val n = x.toInt()
                if (n < 0 || n > 20 || abs(x - n) > 1e-9) {
                    throw IllegalArgumentException("Factorial válido solo para enteros entre 0 y 20")
                }
                var fact = 1L
                for (i in 2..n) fact *= i
                steps.add("Factorial: $n! = $fact")
                x = fact.toDouble()
            }
            return x
        }

        private fun parsePrimary(): Double {
            skipSpaces()
            if (eat('(')) {
                val x = parseExpression()
                if (!eat(')')) throw IllegalArgumentException("Falta paréntesis de cierre ')'")
                return x
            }

            if (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) {
                val start = pos
                while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) pos++
                return input.substring(start, pos).toDouble()
            }

            if (pos < input.length && input[pos].isLetter()) {
                val start = pos
                while (pos < input.length && input[pos].isLetter()) pos++
                val name = input.substring(start, pos).lowercase()
                if (name == "pi") return PI
                if (name == "e") return Math.E

                if (!eat('(')) throw IllegalArgumentException("Falta '(' después de '$name'")
                val arg = parseExpression()
                if (!eat(')')) throw IllegalArgumentException("Falta ')' en '$name'")

                val angle = if (useDegrees) Math.toRadians(arg) else arg
                val res = when (name) {
                    "sin", "sen" -> sin(angle)
                    "cos" -> cos(angle)
                    "tan" -> tan(angle)
                    "asin" -> if (useDegrees) Math.toDegrees(asin(arg)) else asin(arg)
                    "acos" -> if (useDegrees) Math.toDegrees(acos(arg)) else acos(arg)
                    "atan" -> if (useDegrees) Math.toDegrees(atan(arg)) else atan(arg)
                    "sqrt" -> {
                        if (arg < 0) throw ArithmeticException("Raíz cuadrada de número negativo")
                        sqrt(arg)
                    }
                    "abs" -> abs(arg)
                    "log" -> {
                        if (arg <= 0) throw ArithmeticException("Logaritmo de número no positivo")
                        log10(arg)
                    }
                    "ln" -> {
                        if (arg <= 0) throw ArithmeticException("Logaritmo natural de número no positivo")
                        ln(arg)
                    }
                    "exp" -> exp(arg)
                    else -> throw IllegalArgumentException("Función desconocida: $name")
                }
                steps.add("Función $name(${formatNumber(arg)}) = ${formatNumber(res)}")
                return res
            }

            throw IllegalArgumentException("Expresión incompleta")
        }
    }
}
