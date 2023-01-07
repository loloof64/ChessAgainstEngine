package i18n

import androidx.compose.runtime.staticCompositionLocalOf

data class Strings(
    val homePageTitle: String,
    val gamePageTitle: String,
    val goBack: String,
    val chessPiece: String,
    val goToGamePage: String,
    val swapBoardOrientation: String,
    val draggedPiece: String,
    val wrongFieldsCountFen: String,
    val oppositeKingInCheckFen: String,
    val close: String,
)

val strings = mapOf(
    "en" to enStrings,
    "fr" to frStrings,
    "es" to esStrings,
)

val LocalStrings = staticCompositionLocalOf { enStrings }