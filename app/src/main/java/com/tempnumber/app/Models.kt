package com.tempnumber.app

data class Country(val key: String, val name: String, val iso: String?)
data class PhoneNumber(val number: String)
data class Sms(val sender: String, val text: String, val time: String)
data class SavedNumber(val countryKey: String, val countryName: String, val number: String) {
    fun country() = Country(countryKey, countryName, Flags.isoFor(countryName, countryKey))
}

class ApiException(message: String) : Exception(message)

private val codeRegex = Regex("""(?<!\d)(\d{3}[- ]\d{3}|\d{4,8})(?!\d)""")

/** Pulls the verification code (4 to 8 digits) out of an SMS text. */
fun extractCode(text: String): String? =
    codeRegex.find(text)?.value?.filter { it.isDigit() }

fun groupCode(code: String): String =
    if (code.length == 6) "${code.take(3)} ${code.drop(3)}" else code

fun prettyNumber(n: String): String = if (n.isNotEmpty() && n.all { it.isDigit() }) "+$n" else n
