package com.example.drink

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.time.LocalDate

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.drink.WaterDataStore.DATE_KEY
import com.example.drink.WaterDataStore.WATER_KEY
import com.example.drink.WaterDataStore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


object WaterDataStore {
    private val Context.dataStore by preferencesDataStore(name = "water_prefs")

    private val WATER_KEY = intPreferencesKey("water_intake")
    private val DATE_KEY = stringPreferencesKey("last_date")

    suspend fun loadIntake(context: Context): Int {
        val prefs = context.dataStore.data.first()
        val savedDate = prefs[DATE_KEY]
        return (if (savedDate == LocalDate.now().toString()) {
            prefs[WATER_KEY] ?: 0
        } else {
            saveIntake(context, 0) // reset
            0
        }) as Int
    }

    suspend fun saveIntake(context: Context, intake: Int) {
        context.dataStore.edit {
            it[WATER_KEY] = intake
            it[DATE_KEY] = LocalDate.now().toString()
        }
    }

}
object HistoriaRepository {

    private val HISTORIA_KEY = stringPreferencesKey("historia_json")

    suspend fun zapisz(context: Context, intake: Int) {
        val today = LocalDate.now().toString()

        context.dataStore.edit { preferences ->
            val json = preferences[HISTORIA_KEY]
            val lista = if (json != null) {
                Json.decodeFromString<List<HistoriaEntry>>(json)
            } else {
                emptyList()
            }.toMutableList()

            val index = lista.indexOfFirst { it.date == today }
            if (index >= 0) {
                val entry = lista[index]
                lista[index] = entry.copy(intake = entry.intake + intake)
            } else {
                lista.add(HistoriaEntry(today, intake))
            }

            preferences[HISTORIA_KEY] = Json.encodeToString(lista)
        }
    }

    suspend fun pobierzHistorie(context: Context): List<HistoriaEntry> {
        val preferences = context.dataStore.data.first()
        val json = preferences[HISTORIA_KEY]
        return if (json != null) {
            Json.decodeFromString(json)
        } else {
            emptyList()
        }
    }
    suspend fun wyczysc(context: Context) {
        context.dataStore.edit { it.remove(HISTORIA_KEY) }
    }

}
