package com.tempnumber.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.util.concurrent.TimeUnit

data class Country(val key: String, val name: String, val iso: String?)
data class PhoneNumber(val number: String)
data class Sms(val sender: String, val text: String, val time: String)
data class SavedNumber(val countryKey: String, val countryName: String, val number: String) {
    fun country() = Country(countryKey, countryName, Flags.isoFor(countryName, countryKey))
}

class ApiException(message: String) : Exception(message)

object Api {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private suspend fun get(path: String, params: Map<String, String> = emptyMap()): Any =
        withContext(Dispatchers.IO) {
            val url = "https://${BuildConfig.RAPIDAPI_HOST}$path".toHttpUrl().newBuilder().apply {
                params.forEach { (k, v) -> addQueryParameter(k, v) }
            }.build()
            val request = Request.Builder()
                .url(url)
                .header("X-RapidAPI-Key", BuildConfig.RAPIDAPI_KEY)
                .header("X-RapidAPI-Host", BuildConfig.RAPIDAPI_HOST)
                .build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw ApiException("Server error ${response.code}: ${body.take(160)}")
                }
                try {
                    JSONTokener(body).nextValue() ?: throw ApiException("Empty reply")
                } catch (e: ApiException) {
                    throw e
                } catch (e: Exception) {
                    throw ApiException("Unexpected reply: ${body.take(160)}")
                }
            }
        }

    /** Finds the list of items in a reply, whatever the wrapper looks like. */
    private fun items(v: Any?): List<Any> {
        when (v) {
            is JSONArray -> return (0 until v.length()).mapNotNull { v.opt(it) }
            is JSONObject -> {
                for (k in v.keys()) {
                    val c = v.opt(k)
                    if (c is JSONArray) return items(c)
                }
                for (k in v.keys()) {
                    val c = v.opt(k)
                    if (c is JSONObject) {
                        for (k2 in c.keys()) {
                            val d = c.opt(k2)
                            if (d is JSONArray) return items(d)
                        }
                    }
                }
                val out = mutableListOf<Any>()
                for (k in v.keys()) {
                    val c = v.opt(k)
                    if (c is String) out.add(JSONObject().put("code", k).put("name", c))
                }
                return out
            }
        }
        return emptyList()
    }

    private fun JSONObject.firstString(vararg keys: String): String? {
        for (k in keys) {
            val v = opt(k)
            if (v != null && v != JSONObject.NULL) {
                val s = v.toString().trim()
                if (s.isNotEmpty()) return s
            }
        }
        return null
    }

    /** If the API gives only a 2-letter code like "fi", show the full country name. */
    private fun displayName(name: String): String =
        if (name.length == 2 && name.all { it.isLetter() }) {
            java.util.Locale("", name.uppercase()).displayCountry.ifBlank { name }
        } else {
            name
        }

    suspend fun countries(): List<Country> {
        val list = items(get(BuildConfig.PATH_COUNTRIES)).mapNotNull { item ->
            when (item) {
                is String -> Country(item, displayName(item), Flags.isoFor(item, item))
                is JSONObject -> {
                    val name = item.firstString("name", "country", "country_name", "countryName", "title")
                    val id = item.firstString("id", "countryId", "country_id")
                    val iso = item.firstString("code", "iso", "iso2", "country_code", "countryCode", "short_name", "shortName", "alpha2")
                    val label = name ?: iso
                    val key = id ?: iso ?: name
                    if (label == null || key == null) null
                    else Country(key, displayName(label), Flags.isoFor(label, iso))
                }
                else -> null
            }
        }
        return list.distinctBy { it.key }.sortedBy { it.name }
    }

    suspend fun numbers(country: Country): List<PhoneNumber> {
        val reply = get(BuildConfig.PATH_NUMBERS, mapOf(BuildConfig.PARAM_COUNTRY to country.key))
        return items(reply).mapNotNull { item ->
            val n = when (item) {
                is String -> item
                is JSONObject -> item.firstString("number", "phone", "phone_number", "phoneNumber", "msisdn", "full_number")
                else -> null
            }
            n?.let { PhoneNumber(it) }
        }.distinctBy { it.number }
    }

    suspend fun messages(number: String): List<Sms> {
        val reply = get(BuildConfig.PATH_MESSAGES, mapOf(BuildConfig.PARAM_NUMBER to number))
        return items(reply).mapNotNull { item ->
            when (item) {
                is String -> Sms("", item, "")
                is JSONObject -> {
                    val text = item.firstString("text", "message", "body", "content", "sms", "msg")
                    if (text == null) {
                        null
                    } else {
                        Sms(
                            sender = item.firstString("from", "sender", "source", "originator") ?: "",
                            text = text,
                            time = item.firstString("time", "date", "received", "received_at", "created_at", "createdAt", "timestamp", "age") ?: ""
                        )
                    }
                }
                else -> null
            }
        }
    }
}

private val codeRegex = Regex("""(?<!\d)(\d{3}[- ]\d{3}|\d{4,8})(?!\d)""")

/** Pulls the verification code (4 to 8 digits) out of an SMS text. */
fun extractCode(text: String): String? =
    codeRegex.find(text)?.value?.filter { it.isDigit() }

fun groupCode(code: String): String =
    if (code.length == 6) "${code.take(3)} ${code.drop(3)}" else code

fun prettyNumber(n: String): String = if (n.isNotEmpty() && n.all { it.isDigit() }) "+$n" else n
