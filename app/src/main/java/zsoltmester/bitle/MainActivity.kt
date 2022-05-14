package zsoltmester.bitle

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import zsoltmester.bitle.engine.*
import zsoltmester.bitle.ui.theme.BitleTheme
import zsoltmester.bitle.ui.theme.Teal200

class MainActivity : ComponentActivity() {

    private val engine: GameEngine = GameEngineImpl(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BitleTheme {
                MainScreen(engine)
            }
        }
    }
}

@Composable
fun MainScreen(engine: GameEngine) {
    val context = LocalContext.current

    Scaffold(
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            TopAppBar()
        }
    ) {
        var gameState: GameState by remember { mutableStateOf(engine.startNewGame()) }

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.weight(0.5f))
            Grid(gameState.gridCells)
            Spacer(modifier = Modifier.weight(0.5f))
            Keyboard(gameState.keyboardCells, onClick = {
                val response = engine.processAction(it)
                gameState = response.first

                when (response.second) {
                    ActionError.INPUT_ROW_FULL -> {
                        Toast.makeText(context, "Input row is full.", Toast.LENGTH_SHORT).show()
                    }
                    ActionError.INPUT_ROW_EMPTY -> {
                        Toast.makeText(context, "Input row is empty.", Toast.LENGTH_SHORT).show()
                    }
                    ActionError.INPUT_ROW_INCOMPLETE -> {
                        Toast.makeText(context, "Input row is incomplete.", Toast.LENGTH_SHORT)
                            .show()
                    }
                    ActionError.INVALID_EQUATION -> {
                        Toast.makeText(context, "Invalid equation.", Toast.LENGTH_SHORT).show()
                    }
                    null -> {
                        // Nothing to do.
                    }
                }
            })
        }
    }
}

@Composable
fun TopAppBar() {
    TopAppBar(
        backgroundColor = MaterialTheme.colors.primary,
        content = {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Info, null)
                }
                Spacer(modifier = Modifier.weight(0.5f))
                Text(
                    "Bitle",
                    style = MaterialTheme.typography.h6
                )
                Spacer(modifier = Modifier.weight(0.5f))
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Person, null)
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Grid(cells: List<CellModel>) {
    // TODO: 8 should come from the engine
    LazyVerticalGrid(
        cells = GridCells.Fixed(8)
    ) {
        itemsIndexed(cells) { index, cell ->
            GridCell(cell = cell, indexInRow = index % 8)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun GridCell(cell: CellModel, indexInRow: Int) {
    FlipCard(
        cardFace = if (cell.type == CellType.EMPTY || cell.type == CellType.INPUT) CardFace.Front else CardFace.Back,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearOutSlowInEasing,
            delayMillis = indexInRow * 300
        ),
        modifier = Modifier
            .padding(4.dp)
            .height(48.dp)
            .fillMaxWidth(),
        front = {
            val cardBorderColor: Color by animateColorAsState(
                targetValue = when (cell.type) {
                    CellType.EMPTY -> Color.LightGray
                    else -> Color.Gray
                },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )

            val textScale: Float by animateFloatAsState(
                targetValue = if (cell.type == CellType.EMPTY) 0.4f else 1f,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize(),
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Text(
                        text = cellDisplayValue(cell.value),
                        style = MaterialTheme.typography.body1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .wrapContentHeight(CenterVertically)
                            .scale(textScale)
                    )
                }
            }
        },
        back = {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize(),
                    backgroundColor = cellBackgroundColor(cell.type)
                ) {
                    Text(
                        text = cellDisplayValue(cell.value),
                        style = MaterialTheme.typography.body1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .wrapContentHeight(CenterVertically)
                    )
                }
            }
        },
    )
}

@Composable
fun Keyboard(cells: List<CellModel>, onClick: (CellValue) -> Unit) {
    // TODO: Cells' order should be defined by the view layer.
    Column {
        Row(Modifier.fillMaxWidth()) {
            cells.subList(0, 5).forEach {
                Box(modifier = Modifier.weight(1f)) {
                    KeyboardCell(cell = it, onClick = onClick)
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            cells.subList(5, 10).forEach {
                Box(modifier = Modifier.weight(1f)) {
                    KeyboardCell(cell = it, onClick = onClick)
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            cells.subList(10, 14).forEach {
                Box(modifier = Modifier.weight(1f)) {
                    KeyboardCell(cell = it, onClick = onClick)
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            cells.subList(14, 16).forEach {
                Box(modifier = Modifier.weight(1f)) {
                    KeyboardCell(cell = it, onClick = onClick)
                }
            }
        }
    }
}

@Composable
fun KeyboardCell(cell: CellModel, onClick: (CellValue) -> Unit) {
    val backgroundColor: Color by animateColorAsState(
        targetValue = cellBackgroundColor(cell.type),
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    )

    Card(
        modifier = Modifier
            .padding(4.dp)
            .height(48.dp)
            .fillMaxWidth()
    ) {
        Button(
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = backgroundColor
            ),
            onClick = { onClick(cell.value) }
        ) {
            Text(
                text = cellDisplayValue(cell.value),
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center,
                modifier = Modifier.wrapContentHeight(CenterVertically)
            )
        }
    }
}

private fun cellDisplayValue(cellValue: CellValue): String {
    return when (cellValue) {
        CellValue.EMPTY ->
            ""
        CellValue.ONE ->
            "1"
        CellValue.TWO ->
            "2"
        CellValue.THREE ->
            "3"
        CellValue.FOUR ->
            "4"
        CellValue.FIVE ->
            "5"
        CellValue.SIX ->
            "6"
        CellValue.SEVEN ->
            "7"
        CellValue.EIGHT ->
            "8"
        CellValue.NINE ->
            "9"
        CellValue.ZERO ->
            "0"
        CellValue.AND ->
            "AND"
        CellValue.OR ->
            "OR"
        CellValue.XOR ->
            "XOR"
        CellValue.EQUAL ->
            "="
        CellValue.DELETE ->
            "DELETE"
        CellValue.ENTER ->
            "ENTER"
    }
}

private fun cellBackgroundColor(cellType: CellType): Color {
    return when (cellType) {
        CellType.EMPTY ->
            Color.White
        CellType.INPUT ->
            Color.White
        CellType.FOUND ->
            Color.Green
        CellType.CONTAINS ->
            Color.Yellow
        CellType.NOT_INCLUDED ->
            Color.Gray
        CellType.UNKNOWN ->
            Color.LightGray
        CellType.UTILITY_DISABLED ->
            Teal200
        CellType.UTILITY_ENABLED ->
            Teal200
    }
}

//@Preview(
//    uiMode = Configuration.UI_MODE_NIGHT_YES,
//    showBackground = true,
//    name = "Dark Mode"
//)
@Preview(
//    name = "Light Mode",
    showSystemUi = true
)
@Composable
fun DefaultPreview() {
    BitleTheme {
        MainScreen(GameEngineImpl(LocalContext.current))
    }
}