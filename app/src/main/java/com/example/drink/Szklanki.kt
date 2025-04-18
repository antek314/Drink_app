package com.example.drink

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.first
import java.time.LocalDate

val Context.dataStore by preferencesDataStore(name = "szklanka")

object Szklanki {
    //private val Context.dataStore by preferencesDataStore(name = "szklanka")

    private var POJEMNOSC_1 = intPreferencesKey("pojemnosc1")
    private var POJEMNOSC_2 = intPreferencesKey("pojemnosc2")
    private var POJEMNOSC = intPreferencesKey("pojemnosc")


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
}
