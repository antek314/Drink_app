package com.example.drink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.drink.ui.theme.DrinkTheme
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrinkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    com.example.drink.WaterTrackerScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun WaterTrackerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var waterIntake by remember { mutableStateOf(0) }

    // Wczytaj dane na start
    LaunchedEffect(Unit) {
        waterIntake = WaterDataStore.loadIntake(context)
    }

    fun addWater(amount: Int) {
        waterIntake += amount
        scope.launch {
            WaterDataStore.saveIntake(context, waterIntake)
        }
    }
    fun removeWater(amount: Int) {
        waterIntake -= amount
        scope.launch {
            WaterDataStore.saveIntake(context, waterIntake)
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "Śledzenie picia wody",
            style = MaterialTheme.typography.headlineMedium
        )


        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                "Dzisiaj wypiłeś: $waterIntake ml",
                style = MaterialTheme.typography.titleLarge
            )
            Button(onClick = { removeWater(250) }) { // tu zrobic edycje wartosci
                Text("edytuj")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(30.dp)) {
            Button(onClick = { addWater(250) }) {
                Text("+250 ml")
            }
            Button(onClick = { addWater(500) }) {
                Text("+500 ml")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DrinkTheme {
        WaterTrackerScreen()
    }
}