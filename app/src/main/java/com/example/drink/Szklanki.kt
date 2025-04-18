package com.example.drink

import android.R
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import kotlinx.coroutines.flow.map
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date




val Context.dataStore by preferencesDataStore(name = "szklanka")

object Szklanki {
    //private val Context.dataStore by preferencesDataStore(name = "szklanka")

    private var POJEMNOSC_1 = intPreferencesKey("pojemnosc1")
    private var POJEMNOSC_2 = intPreferencesKey("pojemnosc2")
    private var POJEMNOSC = intPreferencesKey("pojemnosc")
    val GODZINA = stringPreferencesKey("godzina")

    private var STATUS_POWIADOMIENIA = booleanPreferencesKey("status")


    fun getPojemnosc1(context: Context): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[POJEMNOSC_1] ?: 250
        }
    }

    fun getPojemnosc2(context: Context): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[POJEMNOSC_2] ?: 500
        }
    }

    fun getPojemnosc(context: Context): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[POJEMNOSC] ?: 2500
        }
    }
    fun getStatus(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[STATUS_POWIADOMIENIA] ?: false
        }
    }
    fun getGodzina(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[GODZINA] ?: "12:00"
        }
    }

    suspend fun zmianaPojemnosc1(context: Context, pojemnosc: Int) {
        val dataStore = context.dataStore
        dataStore.edit { preferences ->
            preferences[POJEMNOSC_1] = pojemnosc
        }
    }

    suspend fun zmianaPojemnosc2(context: Context, pojemnosc: Int) {
        val dataStore = context.dataStore
        dataStore.edit { preferences ->
            preferences[POJEMNOSC_2] = pojemnosc
        }
    }
    suspend fun zmianaPojemnosc(context: Context, pojemnosc: Int) {
        val dataStore = context.dataStore
        dataStore.edit { preferences ->
            preferences[POJEMNOSC] = pojemnosc
        }
    }
    suspend fun zmianaStatusu(context: Context, status: Boolean) {
        val dataStore = context.dataStore
        dataStore.edit { preferences ->
            preferences[STATUS_POWIADOMIENIA] = status
        }
    }
    suspend fun zmianaGodziny(context: Context, godzina: String) {
        context.dataStore.edit { preferences ->
            preferences[GODZINA] = godzina
        }
    }
}

