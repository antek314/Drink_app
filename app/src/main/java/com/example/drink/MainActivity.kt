package com.example.drink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.drink.ui.theme.DrinkTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Shadow
import androidx.compose.foundation.border




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrinkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WaterTrackerScreen(Modifier.padding(innerPadding))
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
    var toggled by remember { mutableStateOf(false) }

    // Automatyczne przełączanie koloru co 4 sekundy
    LaunchedEffect(Unit) {
        while (true) {
            toggled = !toggled
            kotlinx.coroutines.delay(15000)
        }
    }
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val animatedColor by animateColorAsState(
        targetValue = if (toggled) Color(0xFF0E65B7) else Color(0xFF00BFFF),
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "bg_anim"
    )

    val progress = (waterIntake / 2000f).coerceIn(0f, 1f)

    ModalNavigationDrawer(
        drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
        scrimColor = Color.Black.copy(alpha = 0.9f), // Przyciemnienie tła
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(25.dp)
            ) {
                Text(
                    text = "MENU",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(1f, 1f),
                            blurRadius = 2f
                        )
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                Divider()
                Spacer(modifier = Modifier.height(20.dp))

                Text(text = "Statystyki",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Historia",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Ustawienia",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
            }
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(animatedColor, Color.White)
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Drink app",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(20.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = "${(progress * 100).toInt()}% celu osiągnięte",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                var text=""
                if (waterIntake < 1000){ text = "Szklanka ma dopiero: "}
                else{text = "Szklanka ma już: "}
                Text(
                    "$text  $waterIntake ml",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
            }

            WaterGlass(
                waterIntake = waterIntake,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WaterCard(amount = 250) { addWater(250) }
                WaterCard(amount = 500) { addWater(500) }
            }

            Button(onClick = { removeWater(250) }) {
                Text("edytuj szklanki")
            }
            Text(
                text = "🚰 Pij wodę, organizm ci podziękuje!",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun WaterCard(amount: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier
            .padding(5.dp)
            //.fillMaxWidth()
            .height(40.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                "+$amount ml",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}
@Composable
fun WaterGlass(waterIntake: Int, modifier: Modifier = Modifier) {
    val maxWater = 2500f // Maksymalne napełnienie (ml)
    val waterPercent = (waterIntake / maxWater).coerceIn(0f, 1f)

    val animatedFill by animateFloatAsState(
        targetValue = waterPercent,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "water_fill"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier
            .width(140.dp)
            .height(200.dp)
        ) {
            val glassTop = size.width * 0.1f
            val glassBottom = size.height
            val glassWidth = size.width * 0.8f

            // Rysuj szklankę (obramowanie)
            drawRoundRect(
                color = Color.Gray,
                topLeft = Offset(x = (size.width - glassWidth) / 2, y = glassTop),
                size = Size(glassWidth, glassBottom - glassTop),
                style = Stroke(width = 6f),
                cornerRadius = CornerRadius(12f)
            )

            // Rysuj poziom wody
            val waterHeight = (glassBottom - glassTop) * animatedFill
            drawRoundRect(
                color = Color(0xFF00BFFF),
                topLeft = Offset(
                    x = (size.width - glassWidth) / 2,
                    y = glassBottom - waterHeight
                ),
                size = Size(glassWidth, waterHeight),
                cornerRadius = CornerRadius(12f)
            )
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
