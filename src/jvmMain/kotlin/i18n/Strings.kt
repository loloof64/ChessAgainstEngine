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
    val queenPromotion: String,
    val rookPromotion: String,
    val bishopPromotion: String,
    val knightPromotion: String,
    val playerWonGame: String,
    val playerLostGame: String,
    val drawByStalemate: String,
    val drawByThreeFoldRepetition: String,
    val drawByInsufficientMaterial: String,
    val drawByFiftyMovesRule: String,
    val goBackHistory: String,
    val goForwardHistory: String,
    val goStartHistory: String,
    val goEndHistory: String,
)

val strings = mapOf(
    "en" to enStrings,
    "fr" to frStrings,
    "es" to esStrings,
)

val LocalStrings = staticCompositionLocalOf { enStrings }