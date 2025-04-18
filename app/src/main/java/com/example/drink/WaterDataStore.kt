package com.example.drink

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.time.LocalDate

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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