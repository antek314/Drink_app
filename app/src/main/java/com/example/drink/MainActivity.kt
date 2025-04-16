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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.Alignment
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch




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
    DrawerScreen()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSelected by remember { mutableStateOf(true) }

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
    val brush = Brush.linearGradient(
        colors = listOf(Color(0xFF0E65B7), Color(0xFF00BFFF))
    )

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "Śledzenie picia wody",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = MaterialTheme.colorScheme.primary
            )
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Dzisiaj wypiłeś:  $waterIntake ml",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            )
            Button(onClick = { removeWater(250) }) {
                Text("edytuj")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { addWater(250) }) {
                Text("+250 ml", style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                ))
            }
            Button(onClick = { addWater(500) }) {
                Text("+500 ml", style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                ))
            }
        }


        Text(
            text = "WODA!",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            modifier = Modifier
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(brush = brush, blendMode = BlendMode.SrcAtop)
                    }
                }
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor =
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = "Dinner club",
                style = MaterialTheme.typography.bodyLarge,
                color =
                    if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
            )
        }
        Button(onClick = { isSelected = !isSelected }) {
            Text("styl?")
        }
    }
}
@Composable
fun DrawerScreen() {

    // Pamiętanie stanu wysuwanego panelu

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Zawartość panelu z menu
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(text = "Menu", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(20.dp))
                Divider()
                Spacer(modifier = Modifier.height(20.dp))
                // Dodaj kolejne opcje menu
                Text(text = "Opcja 1", modifier = Modifier.padding(vertical = 12.dp))
                Text(text = "Opcja 2", modifier = Modifier.padding(vertical = 12.dp))
                // itd.
            }
        }
    ) {
        // Główna treść ekranu
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                // Otworzenie wysuwanego panelu
                scope.launch { drawerState.open() }
            }) {
                Text("Pokaż menu")
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