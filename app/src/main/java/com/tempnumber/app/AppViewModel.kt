package com.tempnumber.app

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface Load<out T> {
    object Loading : Load<Nothing>
    data class Failure(val message: String) : Load<Nothing>
    data class Done<T>(val data: T) : Load<T>
}

sealed interface Screen {
    object Splash : Screen
    object Home : Screen
    object MyNumbers : Screen
    data class Numbers(val country: Country) : Screen
    data class Inbox(val country: Country, val number: String, val from: Screen) : Screen
}

private class SavedStore(context: Context) {
    private val prefs = context.getSharedPreferences("saved_numbers", Context.MODE_PRIVATE)

    fun all(): List<SavedNumber> =
        prefs.getString("list", "").orEmpty().split("\n").mapNotNull { line ->
            val f = line.split("\t")
            if (f.size == 3) SavedNumber(f[0], f[1], f[2]) else null
        }

    fun write(list: List<SavedNumber>) {
        prefs.edit()
            .putString("list", list.joinToString("\n") { "${it.countryKey}\t${it.countryName}\t${it.number}" })
            .apply()
    }
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val store = SavedStore(app)

    var screen by mutableStateOf<Screen>(Screen.Splash)
        private set
    var countries by mutableStateOf<Load<List<Country>>>(Load.Loading)
        private set
    var numbers by mutableStateOf<Load<List<PhoneNumber>>>(Load.Loading)
        private set
    var messages by mutableStateOf<Load<List<Sms>>>(Load.Loading)
        private set
    var refreshing by mutableStateOf(false)
        private set
    var saved by mutableStateOf(store.all())
        private set

    init {
        loadCountries()
    }

    fun go(s: Screen) {
        screen = s
    }

    fun back() {
        screen = when (val s = screen) {
            is Screen.Numbers -> Screen.Home
            is Screen.Inbox -> s.from
            Screen.MyNumbers -> Screen.Home
            else -> screen
        }
    }

    private fun friendly(e: Exception): String = when (e) {
        is IOException -> "No internet connection. Check your network and try again."
        else -> e.message ?: "Something went wrong."
    }

    fun loadCountries() {
        countries = Load.Loading
        viewModelScope.launch {
            countries = try {
                Load.Done(Api.countries())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Load.Failure(friendly(e))
            }
        }
    }

    fun loadNumbers(country: Country) {
        numbers = Load.Loading
        viewModelScope.launch {
            numbers = try {
                Load.Done(Api.numbers(country))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Load.Failure(friendly(e))
            }
        }
    }

    suspend fun refreshMessages(number: String, silent: Boolean) {
        if (!silent) messages = Load.Loading
        refreshing = true
        try {
            messages = Load.Done(Api.messages(number))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val keepOld = silent && messages is Load.Done
            if (!keepOld) messages = Load.Failure(friendly(e))
        } finally {
            refreshing = false
        }
    }

    fun isSaved(number: String) = saved.any { it.number == number }

    fun toggleSaved(country: Country, number: String) {
        saved = if (isSaved(number)) {
            saved.filterNot { it.number == number }
        } else {
            listOf(SavedNumber(country.key, country.name, number)) + saved
        }
        store.write(saved)
    }

    fun removeSaved(item: SavedNumber) {
        saved = saved.filterNot { it.number == item.number }
        store.write(saved)
    }
}
