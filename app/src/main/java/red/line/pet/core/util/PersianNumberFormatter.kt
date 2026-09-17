package red.line.pet.core.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Utility for formatting digits, currency, and numerical strings into Persian (Farsi) representation.
 */
object PersianNumberFormatter {

    private val ENGLISH_TO_PERSIAN_DIGITS = mapOf(
        '0' to '۰',
        '1' to '۱',
        '2' to '۲',
        '3' to '۳',
        '4' to '۴',
        '5' to '۵',
        '6' to '۶',
        '7' to '۷',
        '8' to '۸',
        '9' to '۹'
    )

    /**
     * Converts any English digits inside the string to Persian digits.
     */
    fun toPersian(input: String): String {
        val builder = StringBuilder(input.length)
        for (char in input) {
            builder.append(ENGLISH_TO_PERSIAN_DIGITS[char] ?: char)
        }
        return builder.toString()
    }

    /**
     * Overload for numerical types.
     */
    fun toPersian(number: Number): String {
        return toPersian(number.toString())
    }

    /**
     * Formats an amount with 3-digit comma separators and Persian digits.
     * E.g. 1500000 -> "۱,۵۰۰,۰۰۰"
     */
    fun formatWithSeparators(number: Long): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        val formatter = DecimalFormat("#,###", symbols)
        return toPersian(formatter.format(number))
    }

    /**
     * Formats currency in Iranian Toman with Persian numerals.
     * E.g. 2500000 -> "۲,۵۰۰,۰۰۰ تومان"
     */
    fun formatToman(amount: Long): String {
        return "${formatWithSeparators(amount)} تومان"
    }

    /**
     * Formats a weight value with Persian digits and decimal separator.
     * E.g. 4.5 -> "۴٫۵ کیلوگرم"
     */
    fun formatWeight(kg: Double): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            decimalSeparator = '.'
        }
        val df = DecimalFormat("#0.##", symbols)
        val formatted = df.format(kg).replace('.', '٫')
        return "${toPersian(formatted)} کیلوگرم"
    }
}

/**
 * String extension to easily convert any text's digits to Persian.
 */
fun String.toPersianDigits(): String = PersianNumberFormatter.toPersian(this)

/**
 * Long extension for Toman currency formatting.
 */
fun Long.toTomanString(): String = PersianNumberFormatter.formatToman(this)
