package com.example.drink
import kotlinx.serialization.Serializable

@Serializable
data class HistoriaEntry(val date: String, val intake: Int)
