package com.example.calculadoralysifix.snake

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

enum class Direction { UP, DOWN, LEFT, RIGHT }

data class Point(val x: Int, val y: Int)

data class GameRecord(val score: Int, val date: String)

class SnakeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnakeGameScreen()
        }
    }
}

@Composable
fun SnakeGameScreen() {
    val context = LocalContext.current
    var snake by remember { mutableStateOf(listOf(Point(5, 10), Point(5, 11), Point(5, 12))) }
    var food by remember { mutableStateOf(Point(10, 10)) }
    var direction by remember { mutableStateOf(Direction.UP) }
    var score by remember { mutableStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf(getGameHistory(context)) }

    val gridSize = 20

    // Game Loop
    LaunchedEffect(key1 = snake, key2 = isGameOver) {
        if (!isGameOver) {
            delay(150)
            val head = snake.first()
            val newHead = when (direction) {
                Direction.UP -> Point(head.x, head.y - 1)
                Direction.DOWN -> Point(head.x, head.y + 1)
                Direction.LEFT -> Point(head.x - 1, head.y)
                Direction.RIGHT -> Point(head.x + 1, head.y)
            }

            // Check collisions
            if (newHead.x < 0 || newHead.x >= gridSize || newHead.y < 0 || newHead.y >= gridSize || snake.contains(newHead)) {
                isGameOver = true
                saveGame(context, score)
                history = getGameHistory(context)
            } else {
                if (newHead == food) {
                    score += 10
                    food = Point(Random.nextInt(gridSize), Random.nextInt(gridSize))
                    snake = listOf(newHead) + snake
                } else {
                    snake = listOf(newHead) + snake.dropLast(1)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Header Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A1A1A),
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SNAKE GAME",
                    color = Color.Green,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = "LYSIFIX BRAND",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Score: ",
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "$score",
                        color = Color.Yellow,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Game Board
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .aspectRatio(1f)
                .background(Color(0xFF0A0A0A), RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val (x, y) = dragAmount
                        if (kotlin.math.abs(x) > kotlin.math.abs(y)) {
                            if (x > 0 && direction != Direction.LEFT) direction = Direction.RIGHT
                            else if (x < 0 && direction != Direction.RIGHT) direction = Direction.LEFT
                        } else {
                            if (y > 0 && direction != Direction.UP) direction = Direction.DOWN
                            else if (y < 0 && direction != Direction.DOWN) direction = Direction.UP
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = size.width / gridSize

                // Draw Food (Red)
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(food.x * cellSize, food.y * cellSize),
                    size = Size(cellSize, cellSize)
                )

                // Draw Snake (Green)
                snake.forEachIndexed { index, point ->
                    drawRect(
                        color = if (index == 0) Color(0xFF00FF00) else Color(0xFF00AA00),
                        topLeft = Offset(point.x * cellSize, point.y * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }

            if (isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "GAME OVER",
                            color = Color.Red,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                snake = listOf(Point(5, 10), Point(5, 11), Point(5, 12))
                                direction = Direction.UP
                                score = 0
                                isGameOver = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                        ) {
                            Text("RETRY", color = Color.Black)
                        }
                    }
                }
            }
        }

        // History Section
        Text(
            text = "Historial de Partidas",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.Green,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(horizontal = 16.dp)
        ) {
            items(history) { record ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(record.date, color = Color.LightGray, fontSize = 12.sp)
                    Text("Score: ${record.score}", color = Color.White, fontSize = 14.sp)
                }
            }
        }

        Text(
            text = "Swipe to control the snake",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center,
            color = Color.DarkGray,
            fontSize = 12.sp
        )
    }
}

fun saveGame(context: Context, score: Int) {
    val sharedPref = context.getSharedPreferences("snake_prefs", Context.MODE_PRIVATE)
    val historyString = sharedPref.getString("history", "") ?: ""
    val date = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())
    val newEntry = "$score|$date"

    val historyList = if (historyString.isEmpty()) mutableListOf() else historyString.split(";").toMutableList()
    historyList.add(0, newEntry) // Add to top

    // Keep only last 10 games
    val trimmedHistory = historyList.take(10).joinToString(";")

    with(sharedPref.edit()) {
        putString("history", trimmedHistory)
        apply()
    }
}

fun getGameHistory(context: Context): List<GameRecord> {
    val sharedPref = context.getSharedPreferences("snake_prefs", Context.MODE_PRIVATE)
    val historyString = sharedPref.getString("history", "") ?: ""
    if (historyString.isEmpty()) return emptyList()

    return historyString.split(";").mapNotNull {
        val parts = it.split("|")
        if (parts.size == 2) {
            GameRecord(parts[0].toInt(), parts[1])
        } else null
    }
}
