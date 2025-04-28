package com.example.drink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.TextField
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.util.Log
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Text
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import java.util.Calendar
import android.app.PendingIntent
import android.app.AlarmManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.appwidget.AppWidgetManager
import android.content.IntentFilter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.DisposableEffect





class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //createNotificationChannel(context = this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
        setContent {
            DrinkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "main"
                    ) {
                        composable("main") {
                            WaterTrackerScreen(
                                modifier = Modifier.padding(innerPadding),
                                onNavigateToStatystyki = { navController.navigate("statystyki") },
                                onNavigateToHistoria = { navController.navigate("historia") },
                                onNavigateToUstawienia = { navController.navigate("ustawienia")}
                            )
                        }
                        composable("statystyki") {
                            StatystykiScreen(onBack = { navController.popBackStack() })
                        }
                        composable("historia") {
                            HistoriaScreen(onBack = { navController.popBackStack() })
                        }
                        composable("ustawienia") {
                            UstawieniaScreen(
                                onBack = { navController.popBackStack() },
                                navController
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WaterTrackerScreen(
modifier: Modifier = Modifier,
onNavigateToStatystyki: () -> Unit,
onNavigateToHistoria: () -> Unit,
onNavigateToUstawienia: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var waterIntake by remember { mutableStateOf(0) }

    var pojemnosc1 by remember { mutableStateOf(250) }
    var pojemnosc2 by remember { mutableStateOf(500) }
    var pojemnosc by remember { mutableStateOf(2500) }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "WATER_INTAKE_UPDATED") {
                    waterIntake = intent.getIntExtra("new_amount", 0)
                }
            }
        }

        val filter = IntentFilter("WATER_INTAKE_UPDATED")
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    //LaunchedEffect(context) {
     //   waterIntake = WaterDataStore.loadIntake(context)
    //}

    LaunchedEffect(Unit) {
        waterIntake = WaterDataStore.loadIntake(context)

        Szklanki.getPojemnosc1(context).collect { pojemnosc1 = it }
        Szklanki.getPojemnosc2(context).collect { pojemnosc2 = it }
        Szklanki.getPojemnosc(context).collect { pojemnosc = it }
    }

    val pojemnosc1State = Szklanki.getPojemnosc1(context).collectAsState(initial = pojemnosc1)
    val pojemnosc2State = Szklanki.getPojemnosc2(context).collectAsState(initial = pojemnosc2)
    val pojemnoscState = Szklanki.getPojemnosc(context).collectAsState(initial = pojemnosc)

    pojemnosc1 = pojemnosc1State.value
    pojemnosc2 = pojemnosc2State.value
    pojemnosc = pojemnoscState.value

    fun addWater(amount: Int) {
        scope.launch {
            val newAmount = waterIntake + amount
            waterIntake = newAmount
            WaterDataStore.saveIntake(context, newAmount)
            HistoriaRepository.zapisz(context, amount)

            // Powiadom widget
            val intent = Intent(context, WaterWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent)

            // Powiadom inne instancje aktywności
            context.sendBroadcast(
                Intent("WATER_INTAKE_UPDATED").apply {
                    putExtra("new_amount", newAmount)
                    // Poprawne ustawienie pakietu
                    `package` = context.packageName  // Użycie backticków dla właściwości package
                }
            )
        }
    }

    fun removeWater(amount: Int) {
        scope.launch {
            // Oblicz nową wartość z zabezpieczeniem przed ujemnymi wartościami
            val newAmount = maxOf(0, waterIntake - amount)

            // Aktualizuj stan lokalny
            waterIntake = newAmount

            // Zapisz w DataStore
            WaterDataStore.saveIntake(context, newAmount)

            // Zapisz w historii (z ujemnym znakiem)
            HistoriaRepository.zapisz(context, -amount)

            // Wymuś aktualizację widgetu
            val intent = Intent(context, WaterWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent)

            // Powiadom inne części aplikacji
            context.sendBroadcast(
                Intent("WATER_INTAKE_UPDATED").apply {
                    putExtra("new_amount", newAmount)
                    `package` = context.packageName
                }
            )
        }
    }

    var toggled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            toggled = !toggled
            kotlinx.coroutines.delay(15000)
        }
    }
    val animatedColor by animateColorAsState(
        targetValue = if (toggled) Color(0xFF0E65B7) else Color(0xFF00BFFF),
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "bg_anim"
    )
    val progress = if (pojemnosc != 0) {
        (waterIntake.toFloat() / pojemnosc).coerceIn(0f, 1f)
    } else 0f
    val drawerState = rememberDrawerState(DrawerValue.Closed)


    ModalNavigationDrawer(

        //drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.88f),
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
                Spacer(modifier = Modifier.height(40.dp))
                Divider()
                Spacer(modifier = Modifier.height(20.dp))

                Text(text = "Statystyki",
                    modifier = Modifier.clickable { onNavigateToStatystyki() },
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
                    modifier = Modifier.clickable { onNavigateToHistoria() },
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
                    modifier = Modifier.clickable { onNavigateToUstawienia() },
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
                .padding(2.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 1.dp, vertical = 1.dp), // mniejsze paddingi
            verticalAlignment = Alignment.CenterVertically
            ){
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f) // delikatne półprzezroczyste tło
            )
            {
                IconButton(

                    onClick = {
                        scope.launch { drawerState.open() }
                    },
                    modifier = Modifier.size(40.dp) // lekko zmniejszona strefa kliknięcia
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Więcej opcji",
                        tint = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Drink app      ",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    )
            }
        }

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
                modifier = Modifier.align(Alignment.CenterHorizontally),
                maxWater = pojemnosc
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WaterCard(amount = pojemnosc1) { addWater(pojemnosc1) }
                WaterCard(amount = pojemnosc2) { addWater(pojemnosc2) }
            }
            var showDialog by remember { mutableStateOf(false) }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { removeWater(100) }) {
                    Text("odlej 100 ml")
                }
                Button(onClick = {showDialog = true}
                ) {
                    Text("edytuj szklanki")
                }
            }
            if (showDialog) {
                Dialog(onDismissRequest = { showDialog = false }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Wybierz pojemność szklanek", style = MaterialTheme.typography.titleLarge)
                            val options = listOf(150, 200, 250, 300, 400, 500)
                            var selected1 by remember { mutableStateOf(pojemnosc1) }
                            var selected2 by remember { mutableStateOf(pojemnosc2) }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    options.forEach { ml ->
                                        Button(
                                            onClick = { selected1 = ml },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (selected1 == ml) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                            )
                                        ) {
                                            Text("$ml ml")
                                        }
                                    }
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    options.forEach { ml ->
                                        Button(
                                            onClick = { selected2 = ml },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (selected2 == ml) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                            )
                                        ) {
                                            Text("$ml ml")
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                TextButton(onClick = { showDialog = false }) {
                                    Text("Anuluj")
                                }
                                val coroutineScope = rememberCoroutineScope()
                                Button(onClick = {
                                    coroutineScope.launch {
                                        pojemnosc1 = selected1
                                        pojemnosc2 = selected2
                                        Szklanki.zmianaPojemnosc1(context, selected1)
                                        Szklanki.zmianaPojemnosc2(context, selected2)
                                        showDialog = false
                                    }

                                }) {
                                    Text("Zapisz")
                                }
                            }
                        }
                    }
                }
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
                "+$amount ml 💧",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}
@Composable
fun WaterGlass(waterIntake: Int, modifier: Modifier = Modifier, maxWater:Int) {
    val waterPercent = (waterIntake.toFloat() / maxWater.toFloat()).coerceIn(0f, 1f)

    val animatedFill by animateFloatAsState(
        targetValue = waterPercent,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "water_fill"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val textColor = when {
            waterPercent < 0.33f -> Color(0xFFFFB74D)
            waterPercent < 0.66f -> Color(0xFFFFEB3B)
            else -> Color(0xFF81C784)
        }
        Canvas(modifier = Modifier
            .width(140.dp)
            .height(200.dp)
        ) {
            val glassTop = size.width * 0.1f
            val glassBottom = size.height
            val glassWidth = size.width * 0.8f

            drawRoundRect(
                color = Color.Gray,
                topLeft = Offset(x = (size.width - glassWidth) / 2, y = glassTop),
                size = Size(glassWidth, glassBottom - glassTop),
                style = Stroke(width = 6f),
                cornerRadius = CornerRadius(12f)
            )

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
        Text(
            text = if (waterIntake > maxWater*2) "Przestań pić" else "$waterIntake ml",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = if (waterIntake > maxWater*1.5) Color.Red else textColor,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatystykiScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    var historia by remember { mutableStateOf<List<HistoriaEntry>>(emptyList()) }

    LaunchedEffect(Unit) {
        historia = HistoriaRepository.pobierzHistorie(context)
    }

    val srednia = historia.map { it.intake }.average().toInt()
    val min = historia.minOfOrNull { it.intake } ?: 0
    val max = historia.maxOfOrNull { it.intake } ?: 0

    val stats = listOf(
        "Średnie spożycie" to "$srednia ml",
        "Maksymalnie" to "$max ml",
        "Minimalnie" to "$min ml"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statystyki", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Twoje nawyki wody 💧",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            stats.forEach { (label, value) ->
                StatCard(label = label, value = value)
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Wykres dzienny:",
                style = MaterialTheme.typography.titleMedium
            )
            Divider()

            Spacer(modifier = Modifier.height(1.dp))
            val ostatnie = historia.takeLast(10)
            if (ostatnie.isEmpty()) {
                Text("Brak danych do wyświetlenia wykresu.")
            } else {
                SimpleBarChart(
                    data = ostatnie.map { it.intake },
                    labels = ostatnie.map {
                        LocalDate.parse(it.date).format(DateTimeFormatter.ofPattern("dd.MM"))
                    }
                )
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, style = MaterialTheme.typography.bodyLarge)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Icon(
                imageVector = Icons.Rounded.WaterDrop,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

        }
    }
}

@Composable
fun SimpleBarChart(data: List<Int>, labels: List<String>) {
    val max = data.maxOrNull()?.takeIf { it > 0 }?.toFloat() ?: 1f

    if (data.isEmpty()) {
        Text("Brak danych do wyświetlenia", modifier = Modifier.padding(16.dp))
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, value ->
                val heightFraction = (value.toFloat() / max).coerceIn(0f, 1f)
                val animacja by animateFloatAsState(targetValue = heightFraction)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(30.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(animacja)
                            .width(20.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                ),
                                shape = RoundedCornerShape(6.dp)
                            )
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(35.dp)
                )
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoriaScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var historia by remember { mutableStateOf<List<HistoriaEntry>>(emptyList()) }

    LaunchedEffect(Unit) {
        historia = HistoriaRepository.pobierzHistorie(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historia \uD83D\uDCA7") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }

    ) { padding ->
        Column(
            modifier = Modifier
                //.padding(padding)
                //.fillMaxSize()
                //.padding(3.dp),
            //verticalArrangement = Arrangement.spacedBy(12.dp),
            //horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Spacer(modifier = Modifier.height(4.dp))
//            Text(
//                text = "Twoja historia 💧",
//                style = MaterialTheme.typography.headlineSmall,
//                fontWeight = FontWeight.SemiBold
//            )
            //Spacer(modifier = Modifier.height(3.dp))
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    //.fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (historia.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Brak zapisanej historii 💧",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {
                    items(historia) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            )
                            {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Data: ${entry.date}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "Wypito: ${entry.intake} ml",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 17.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val kolorIkony = when {
                                        entry.intake > 1650 -> Color(0xFF4CAF50)     // Zielony (jasny)
                                        entry.intake > 1400 -> Color(0xFFFF9800)     // Pomarańczowy
                                        else -> Color(0xFFF44336)                    // Czerwony
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.WaterDrop,
                                        contentDescription = null,
                                        tint = kolorIkony,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UstawieniaScreen(onBack: () -> Unit, navController: NavController) {
    val context = LocalContext.current

    val savedGoal by Szklanki.getPojemnosc(context).collectAsState(initial = 3000)
    var dailyGoal by remember { mutableStateOf(savedGoal) }

    LaunchedEffect(savedGoal) {
        dailyGoal = savedGoal
    }


    val status by Szklanki.getStatus(context).collectAsState(initial = false)
    var remindersEnabled by remember { mutableStateOf(status) }
    LaunchedEffect(savedGoal) {
        remindersEnabled = status
    }
    val godzina by Szklanki.getGodzina(context).collectAsState(initial = "12:00")
    var notificationsTime by remember { mutableStateOf(godzina) }

    LaunchedEffect(godzina) {
        notificationsTime = godzina
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia 💧") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Cel dzienny (ml):",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = dailyGoal.toFloat(),
                onValueChange = { dailyGoal = it.toInt() },
                valueRange = 1000f..4000f,
                steps = 5
            )
            Text(
                text = "$dailyGoal ml",
                style = MaterialTheme.typography.bodyLarge
            )

            Divider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Przypomnienia", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = { remindersEnabled = it }
                )
            }

            if (remindersEnabled) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Godzina powiadomienia:", modifier = Modifier.weight(1f))
                    TextField(
                        value = notificationsTime,
                        onValueChange = { notificationsTime = it },
                        label = { Text("Wpisz godzinę") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }


            Divider()
            val coroutineScope = rememberCoroutineScope()

            Button(onClick = {

                coroutineScope.launch {
                    Szklanki.zmianaPojemnosc(context, dailyGoal)
                    Szklanki.zmianaStatusu(context, remindersEnabled)
                    Szklanki.zmianaGodziny(context, notificationsTime.toString())

                    // Ustaw alarm
                    cancelNotificationAlarm(context)
                    val prefs = context.getSharedPreferences("ustawienia", Context.MODE_PRIVATE)
                    prefs.edit().putString("notificationTime", notificationsTime.toString()).apply()

                    if (remindersEnabled) {
                        setDailyNotificationAlarm(context, notificationsTime)
                    }

                    navController.navigate("main") {
                        popUpTo("settings") { inclusive = true }
                    }
                }
            }
            ) {
                Text("Zapisz ustawienia")
            }
        }
    }
}
fun setDailyNotificationAlarm(context: Context, time: String) {
    val (hour, minute) = time.split(":").map { it.toInt() }

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (before(Calendar.getInstance())) {
            add(Calendar.DATE, 1)
        }
    }

    val intent = Intent(context, Powiadomienia::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
}
fun cancelNotificationAlarm(context: Context) {
    val intent = Intent(context, Powiadomienia::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DrinkTheme {
        WaterTrackerScreen(onNavigateToStatystyki = {}, onNavigateToHistoria = {}, onNavigateToUstawienia = {})
    }
}

