package zsoltmester.bitle.engine

import android.content.Context
import android.util.Log
import zsoltmester.bitle.R
import kotlin.random.Random

enum class CellType {
    EMPTY, INPUT, FOUND, CONTAINS, NOT_INCLUDED, UNKNOWN, UTILITY_DISABLED, UTILITY_ENABLED
}

enum class CellValue(val valueInEquation: Char?) {
    EMPTY(null),
    ONE('1'),
    TWO('2'),
    THREE('3'),
    FOUR('4'),
    FIVE('5'),
    SIX('6'),
    SEVEN('7'),
    EIGHT('8'),
    NINE('9'),
    ZERO('0'),
    AND('&'),
    OR('|'),
    XOR('^'),
    EQUAL('='),
    DELETE(null),
    ENTER(null)
}

data class CellModel(val type: CellType, val value: CellValue)

enum class GameStatus {
    IN_PROGRESS, WON, LOST
}

enum class Message {
    WON,
    LOST,
    INPUT_ROW_FULL,
    INPUT_ROW_EMPTY,
    INPUT_ROW_INCOMPLETE,
    INVALID_EQUATION
}

data class GameState(
    val status: GameStatus,
    val gridCells: List<CellModel>,
    val keyboardCells: List<CellModel>,
    val message: Message?
)

interface GameEngine {
    fun startNewGame(): GameState
    fun processAction(cellValue: CellValue): GameState
}

// TODO: Shouldn't use context.
class GameEngineImpl(private val context: Context) : GameEngine {
    companion object {
        private const val EQUATION_LENGTH = 8
    }

    private var gameState: GameState? = null
    private var equation: String? = null

    override fun startNewGame(): GameState {
        gameState = createEmptyGameState()
        equation = createEquation()

        Log.d("Equation", "$equation")

        return gameState as GameState
    }

    override fun processAction(cellValue: CellValue): GameState {
        if (gameState!!.status != GameStatus.IN_PROGRESS) {
            return gameState!!
        }

        val message: Message? = when (cellValue) {
            CellValue.ONE,
            CellValue.TWO,
            CellValue.THREE,
            CellValue.FOUR,
            CellValue.FIVE,
            CellValue.SIX,
            CellValue.SEVEN,
            CellValue.EIGHT,
            CellValue.NINE,
            CellValue.ZERO,
            CellValue.AND,
            CellValue.OR,
            CellValue.XOR,
            CellValue.EQUAL ->
                appendToInputRow(cellValue)
            CellValue.DELETE ->
                deleteFromInputRow()
            CellValue.ENTER ->
                processEnter()
            else -> {
                // Should never happen.
                null
            }
        }

        gameState = gameState!!.copy(message = message)

        return gameState!!
    }

    private fun appendToInputRow(cellValue: CellValue): Message? {
        if (isInputRowFilled()) {
            return Message.INPUT_ROW_FULL
        }

        val firstEmptyCellIndex = firstEmptyCellIndex()
        replaceGridCell(firstEmptyCellIndex, CellModel(CellType.INPUT, cellValue))

        return null
    }

    private fun deleteFromInputRow(): Message? {
        val lastInputCellIndex = lastInputCellIndex()
        if (lastInputCellIndex < 0) {
            return Message.INPUT_ROW_EMPTY
        }

        replaceGridCell(lastInputCellIndex, CellModel(CellType.EMPTY, CellValue.EMPTY))

        return null
    }

    private fun processEnter(): Message? {
        if (!isInputRowFilled()) {
            return Message.INPUT_ROW_INCOMPLETE
        }

        if (!isInputRowValidEquation()) {
            return Message.INVALID_EQUATION
        }

        validateInputRow()

        return updateGameStatus()
    }

    private fun createEmptyGameState(): GameState {
        val gridCells = List(size = 48, init = {
            CellModel(CellType.EMPTY, CellValue.EMPTY)
        }).toMutableList()

        val keyboardCells = listOf(
            CellModel(type = CellType.UNKNOWN, value = CellValue.ONE),
            CellModel(type = CellType.UNKNOWN, value = CellValue.TWO),
            CellModel(type = CellType.UNKNOWN, value = CellValue.THREE),
            CellModel(type = CellType.UNKNOWN, value = CellValue.FOUR),
            CellModel(type = CellType.UNKNOWN, value = CellValue.FIVE),
            CellModel(type = CellType.UNKNOWN, value = CellValue.SIX),
            CellModel(type = CellType.UNKNOWN, value = CellValue.SEVEN),
            CellModel(type = CellType.UNKNOWN, value = CellValue.EIGHT),
            CellModel(type = CellType.UNKNOWN, value = CellValue.NINE),
            CellModel(type = CellType.UNKNOWN, value = CellValue.ZERO),
            CellModel(type = CellType.UNKNOWN, value = CellValue.AND),
            CellModel(type = CellType.UNKNOWN, value = CellValue.XOR),
            CellModel(type = CellType.UNKNOWN, value = CellValue.OR),
            CellModel(type = CellType.UNKNOWN, value = CellValue.EQUAL),
            CellModel(type = CellType.UTILITY_DISABLED, value = CellValue.DELETE),
            CellModel(type = CellType.UTILITY_DISABLED, value = CellValue.ENTER)
        ).toMutableList()

        return GameState(GameStatus.IN_PROGRESS, gridCells, keyboardCells, null)
    }

    private fun createEquation(): String {
        val fileReader =
            context.resources.openRawResource(R.raw.available_bitle_equations).bufferedReader()
        fileReader.skip(Random.nextLong(139647) * 9)
        val equation = fileReader.readLine()
        fileReader.close()
        return equation
    }

    private fun replaceGridCell(gridCellIndex: Int, newGridCell: CellModel) {
        val updatedGridCells: MutableList<CellModel> = gameState!!.gridCells.toMutableList()
        updatedGridCells[gridCellIndex] = newGridCell
        gameState = gameState!!.copy(gridCells = updatedGridCells)
    }

    private fun inputRowFirstIndex(): Int {
        return gameState!!.gridCells.indexOfFirst {
            it.type == CellType.INPUT || it.type == CellType.EMPTY
        }
    }

    private fun inputRowLastIndex(): Int {
        val inputRowFirstIndex = inputRowFirstIndex()
        return inputRowFirstIndex + EQUATION_LENGTH - 1
    }

    private fun inputRowCells(): List<CellModel> {
        val inputRowFirstIndex = inputRowFirstIndex()
        val inputRowLastIndex = inputRowLastIndex()
        return gameState!!.gridCells.subList(inputRowFirstIndex, inputRowLastIndex + 1)
    }

    private fun previousInputRowLastIndex(): Int {
        return gameState!!.gridCells.indexOfLast {
            it.type != CellType.INPUT && it.type != CellType.EMPTY
        }
    }

    private fun previousInputRowFirstIndex(): Int {
        val inputRowFirstIndex = previousInputRowLastIndex()
        return inputRowFirstIndex - EQUATION_LENGTH + 1
    }

    private fun previousInputRowCells(): List<CellModel> {
        val previousInputRowFirstIndex = previousInputRowFirstIndex()
        val previousInputRowLastIndex = previousInputRowLastIndex()
        return gameState!!.gridCells.subList(
            previousInputRowFirstIndex,
            previousInputRowLastIndex + 1
        )
    }

    private fun isInputRowFilled(): Boolean {
        val inputRowCells = inputRowCells()
        val hasNonInputCell = inputRowCells.indexOfFirst { it.type != CellType.INPUT } >= 0
        return !hasNonInputCell
    }

    private fun firstEmptyCellIndex(): Int {
        return gameState!!.gridCells.indexOfFirst { it.type == CellType.EMPTY }
    }

    private fun lastInputCellIndex(): Int {
        return gameState!!.gridCells.indexOfLast { it.type == CellType.INPUT }
    }

    private fun isInputRowValidEquation(): Boolean {
        val inputRowCells = inputRowCells()
        val inputEquation = inputRowCells.map { it.value.valueInEquation }
            .joinToString(separator = "", transform = { it.toString() })

        val equationFileReader =
            context.resources.openRawResource(R.raw.available_bitle_equations).bufferedReader()
        var equationFromFile: String? = ""
        var isValidInputEquation = false
        while (equationFromFile != null && !isValidInputEquation) {
            equationFromFile = equationFileReader.readLine()
            if (equationFromFile != null) {
                isValidInputEquation = inputEquation == equationFromFile
            }
        }
        equationFileReader.close()

        return isValidInputEquation
    }

    private fun validateInputRow() {
        val inputRowCells = inputRowCells().toMutableList()
        val newKeyboardCells = gameState!!.keyboardCells.toMutableList()

        // update input row and keyboard cells with FOUND state
        for ((inputRowCellIndex, inputRowCell) in inputRowCells.withIndex()) {
            if (equation!![inputRowCellIndex] == inputRowCell.value.valueInEquation!!) {
                inputRowCells[inputRowCellIndex] = CellModel(CellType.FOUND, inputRowCell.value)

                val keyboardCellIndex =
                    newKeyboardCells.indexOfFirst { it.value == inputRowCell.value }
                newKeyboardCells[keyboardCellIndex] = CellModel(CellType.FOUND, inputRowCell.value)
            }
        }

        // update input row and keyboard cells with CONTAINS and NOT_INCLUDED states
        for ((inputRowCellIndex, inputRowCell) in inputRowCells.withIndex()) {
            if (inputRowCell.type == CellType.FOUND)
                continue

            if (equation!!.contains(inputRowCell.value.valueInEquation!!)
                && equation!!.count { it == inputRowCell.value.valueInEquation } > inputRowCells.count {
                    it.value == inputRowCell.value && (it.type == CellType.FOUND || it.type == CellType.CONTAINS)
                }
            ) {
                // CONTAINS
                inputRowCells[inputRowCellIndex] = CellModel(CellType.CONTAINS, inputRowCell.value)

                val keyboardCellIndex =
                    newKeyboardCells.indexOfFirst { it.value == inputRowCell.value }
                if (newKeyboardCells[keyboardCellIndex].type == CellType.UNKNOWN)
                    newKeyboardCells[keyboardCellIndex] =
                        CellModel(CellType.CONTAINS, inputRowCell.value)
            } else {
                // NOT_INCLUDED
                inputRowCells[inputRowCellIndex] =
                    CellModel(CellType.NOT_INCLUDED, inputRowCell.value)

                val keyboardCellIndex =
                    newKeyboardCells.indexOfFirst { it.value == inputRowCell.value }
                if (newKeyboardCells[keyboardCellIndex].type == CellType.UNKNOWN)
                    newKeyboardCells[keyboardCellIndex] =
                        CellModel(CellType.NOT_INCLUDED, inputRowCell.value)
            }
        }

        val gridCells = gameState!!.gridCells
        val newGridCells = gridCells.take(inputRowFirstIndex()).toMutableList()
        newGridCells += inputRowCells
        newGridCells += gridCells.drop(inputRowLastIndex() + 1)

        gameState = gameState!!.copy(gridCells = newGridCells, keyboardCells = newKeyboardCells)
    }

    private fun updateGameStatus(): Message? {
        val newGameStatus = calculateGameStatus()
        gameState = gameState!!.copy(status = newGameStatus)
        return when (newGameStatus) {
            GameStatus.WON -> Message.WON
            GameStatus.LOST -> Message.WON
            else -> null
        }
    }

    private fun calculateGameStatus(): GameStatus {
        return when {
            isGameWon() -> {
                GameStatus.WON
            }
            isGameLost() -> {
                GameStatus.LOST
            }
            else -> {
                GameStatus.IN_PROGRESS
            }
        }
    }

    private fun isGameWon(): Boolean {
        val previousInputRowCells = previousInputRowCells()
        return previousInputRowCells.firstOrNull { it.type != CellType.FOUND } == null
    }

    private fun isGameLost(): Boolean {
        return inputRowFirstIndex() == -1
    }
}