package zsoltmester.bitle

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import zsoltmester.bitle.engine.*
import zsoltmester.bitle.ui.theme.*

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

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MainScreen(engine: GameEngine) {
    var presentInfoDialog: Boolean by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(onClick = {
                presentInfoDialog = true
            })
        }
    ) {
        var gameState: GameState by remember { mutableStateOf(engine.startNewGame()) }
        var previousGameState: GameState by remember { mutableStateOf(gameState) }

        if (presentInfoDialog) {
            AlertDialog(
                onDismissRequest = { presentInfoDialog = false },
                text = {
                    Text("Guess the equation.\n\nThe equation consists of 3 parts:\n1. Combination of numbers and operators.\n2. An equation sign.\n3. A number.\n\nThe numbers are unsigned 8 bit integers ranging from 0 to 255. The operators are bitwise operators and evaluated from left to right.\n\nIf you enter a valid equation and press enter, every position will turn into:\n* Green, if that position has the correct value.\n* Yellow, if the equation contains that value, but not on that position.\n* Gray, if the equation doesn't contain that value.\n\nYou win, if you find the equation.")
                },
                buttons = {
                    Box(
                        contentAlignment = CenterEnd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp, bottom = 16.dp)
                    ) {
                        Button(
                            onClick = {
                                presentInfoDialog = false
                            }) {
                            Text("OK")
                        }
                    }
                }
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Grid(
                    modifier = Modifier.weight(1f, fill = false),
                    gameState.gridCells,
                    previousCells = previousGameState.gridCells
                )
                MessageBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    message = gameState.message,
                    previousMessage = previousGameState.message,
                    gameStatus = gameState.status, onClick = {
                        previousGameState = gameState
                        gameState = engine.startNewGame()
                    }
                )
            }
            Keyboard(gameState.keyboardCells, onClick = {
                previousGameState = gameState
                gameState = engine.processAction(it)
            })
        }
    }
}

@Composable
fun TopAppBar(onClick: () -> Unit) {
    TopAppBar(
        content = {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = CenterVertically
            ) {
                Box(modifier = Modifier.weight(0.5f), contentAlignment = CenterStart) {
                    IconButton(onClick = { onClick() }) {
                        Icon(Icons.Filled.Info, null)
                    }
                }
                Text(
                    "Bitle",
                    style = MaterialTheme.typography.h5
                )
                Spacer(modifier = Modifier.weight(0.5f))
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Grid(modifier: Modifier, cells: List<CellModel>, previousCells: List<CellModel>) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.weight(1f))
        // TODO: 8 should come from the engine
        LazyVerticalGrid(
            columns = GridCells.Fixed(8)
        ) {
            itemsIndexed(cells) { index, cell ->
                GridCell(cell = cell, previousCell = previousCells[index], indexInRow = index % 8)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun GridCell(cell: CellModel, previousCell: CellModel, indexInRow: Int) {
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
        axis = RotationAxis.AxisY,
        front = {
            val cardBorderColor: Color by animateColorAsState(
                targetValue = when (cell.type) {
                    CellType.EMPTY -> EmptyCellBorderColor
                    else -> InputCellBorderColor
                },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize(),
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    AnimatedVisibility(
                        modifier = Modifier
                            .wrapContentHeight(CenterVertically),
                        visible = cell.type != CellType.EMPTY,
                        enter = scaleIn(
                            initialScale = 0.4f,
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ),
                        exit = scaleOut(
                            targetScale = 0.4f,
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        )
                    ) {
                        Text(
                            text = cellDisplayValue(cell.value, previousCell.value),
                            style = DefaultCellTextStyle,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        back = {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Center,
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize(),
                    backgroundColor = cellBackgroundColor(cell.type)
                ) {
                    Text(
                        text = cellDisplayValue(cell.value, previousCell.value),
                        color = DefaultCellTextColor,
                        style = DefaultCellTextStyle,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .wrapContentHeight(CenterVertically)
                    )
                }
            }
        },
    )
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MessageBox(
    modifier: Modifier,
    message: Message?,
    previousMessage: Message?,
    gameStatus: GameStatus,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Center
    ) {
        val evaluateMessage: (Message?) -> String? = {
            when (it) {
                Message.INPUT_ROW_FULL -> null
                Message.INPUT_ROW_EMPTY -> null
                Message.INPUT_ROW_INCOMPLETE -> "Incomplete equation"
                Message.INVALID_EQUATION -> "Invalid equation"
                Message.WON -> "You won! \uD83C\uDFC6 \uD83C\uDF89"
                Message.LOST -> "Game over \uD83D\uDE14"
                null -> null
            }
        }

        val evaluatedMessage = evaluateMessage(message)
        val evaluatedPreviousMessage = evaluateMessage(previousMessage)

        AnimatedVisibility(
            visible = evaluatedMessage != null,
            enter = scaleIn(
                initialScale = 0.4f,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ),
            exit = scaleOut(
                targetScale = 0.4f,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        ) {
            Row(verticalAlignment = CenterVertically) {
                Text(
                    evaluatedMessage ?: evaluatedPreviousMessage ?: "",
                    style = MaterialTheme.typography.body1
                )
                if (gameStatus != GameStatus.IN_PROGRESS) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = { onClick() }) {
                        Text(
                            text = if (gameStatus == GameStatus.WON) "Start new game" else "Try again",
                            style = UtilityCellTextStyle,
                        )
                    }
                }
            }
        }
    }
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

    val textColor: Color by animateColorAsState(
        targetValue = when (cell.type) {
            CellType.UNKNOWN -> UnknownKeyboardCellTextColor
            CellType.UTILITY_DISABLED, CellType.UTILITY_ENABLED -> MaterialTheme.colors.onPrimary
            else -> DefaultCellTextColor
        },
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
                text = cellDisplayValue(cell.value, withFullName = true),
                color = textColor,
                style = if (cell.type == CellType.UTILITY_DISABLED || cell.type == CellType.UTILITY_ENABLED) UtilityCellTextStyle else DefaultCellTextStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.wrapContentHeight(CenterVertically)
            )
        }
    }
}

private fun cellDisplayValue(
    cellValue: CellValue,
    previousCellValue: CellValue? = null,
    withFullName: Boolean = false
): String {
    val evaluateCellDisplayValue: (CellValue) -> String = {
        when (it) {
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
                if (withFullName) "AND" else "&"
            CellValue.OR ->
                if (withFullName) "OR" else "|"
            CellValue.XOR ->
                if (withFullName) "XOR" else "^"
            CellValue.EQUAL ->
                "="
            CellValue.DELETE ->
                "DELETE"
            CellValue.ENTER ->
                "ENTER"
        }
    }

    val cellDisplayValue = evaluateCellDisplayValue(cellValue)

    return previousCellValue?.let {
        val previousCellDisplayValue = evaluateCellDisplayValue(it)
        cellDisplayValue.ifEmpty { previousCellDisplayValue }
    } ?: run {
        cellDisplayValue
    }
}

@Composable
private fun cellBackgroundColor(cellType: CellType): Color {
    return when (cellType) {
        CellType.EMPTY ->
            EmptyCellColor
        CellType.INPUT ->
            InputCellColor
        CellType.FOUND ->
            FoundCellColor
        CellType.CONTAINS ->
            ContainsCellColor
        CellType.NOT_INCLUDED ->
            NotIncludedCellColor
        CellType.UNKNOWN ->
            UnknownCellColor
        CellType.UTILITY_DISABLED, CellType.UTILITY_ENABLED ->
            MaterialTheme.colors.primary
    }
}

@Preview(
    name = "Light Mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showSystemUi = true
)
@Composable
fun LightPreview() {
    BitleTheme {
        MainScreen(GameEngineImpl(LocalContext.current))
    }
}

@Preview(
    name = "Dark Mode",
    widthDp = 300,
    heightDp = 500,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showSystemUi = true
)
@Composable
fun DarkPreview() {
    BitleTheme {
        MainScreen(GameEngineImpl(LocalContext.current))
    }
}