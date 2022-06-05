package zsoltmester.bitle.ui.theme

import androidx.compose.ui.graphics.Color

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)

// Cell background colors
val EmptyCellColor = Color.Transparent
val InputCellColor = Color.Transparent
val FoundCellColor = Color(red = 106, green = 170, blue = 100)
val ContainsCellColor = Color(red = 201, green = 180, blue = 88)
val NotIncludedCellColor = Color(red = 120, green = 124, blue = 125)
val UnknownCellColor = Color(red = 211, green = 214, blue = 218)

// Cell border colors
val EmptyCellBorderColor = UnknownCellColor
val InputCellBorderColor = NotIncludedCellColor

// Cell text colors
val DefaultCellTextColor = Color.White
val UnknownKeyboardCellTextColor = Color.Black