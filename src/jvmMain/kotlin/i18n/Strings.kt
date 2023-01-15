package i18n

import androidx.compose.runtime.staticCompositionLocalOf

data class Strings(
    val validate: String,
    val cancel: String,
    val homePageTitle: String,
    val gamePageTitle: String,
    val optionsPageTitle: String,
    val editPositionPageTitle: String,
    val goBack: String,
    val chessPiece: String,
    val playDirectly: String,
    val editStartPosition: String,
    val swapBoardOrientation: String,
    val draggedPiece: String,
    val wrongFieldsCountFen: String,
    val oppositeKingInCheckFen: String,
    val close: String,
    val queenPromotion: String,
    val rookPromotion: String,
    val bishopPromotion: String,
    val knightPromotion: String,
    val whiteWonGame: String,
    val blackWonGame: String,
    val drawByStalemate: String,
    val drawByThreeFoldRepetition: String,
    val drawByInsufficientMaterial: String,
    val drawByFiftyMovesRule: String,
    val goBackHistory: String,
    val goForwardHistory: String,
    val goStartHistory: String,
    val goEndHistory: String,
    val stopGame: String,
    val purposeStopGameTitle: String,
    val purposeStopGameMessage: String,
    val gameAborted: String,
    val preferences: String,
    val enginePath: String,
    val selectEnginePathDialogTitle: String,
    val chooseUciEngine: String,
    val notChessUCIEngineError: String,
    val clearEnginePath: String,
    val savedPreferences: String,
    val save: String,
    val noEngineDefinedWarning: String,
    val engineThinkingTime: String,
    val computerSidesOptions: String,
    val computerPlaysWhite: String,
    val computerPlaysBlack: String,
    val showComputerScoreEvaluation: String,
    val emptyCell: String,
    val whitePawn: String,
    val whiteKnight: String,
    val whiteBishop: String,
    val whiteRook: String,
    val whiteQueen: String,
    val whiteKing: String,
    val blackPawn: String,
    val blackKnight: String,
    val blackBishop: String,
    val blackRook: String,
    val blackQueen: String,
    val blackKing: String,
    val selectedPiece: String,
    val selectEraseCell: String,
    val whiteOO: String,
    val whiteOOO: String,
    val blackOO: String,
    val blackOOO: String,
    val enPassantFile: String,
    val select: String,
    val drawHalfMovesCount: String,
    val goUp: String,
    val goDown: String,
    val moveNumber: String,
    val playerTurn: String,
    val whiteTurn: String,
    val blackTurn: String,
)

val strings = mapOf(
    "en" to enStrings,
    "fr" to frStrings,
    "es" to esStrings,
)

val LocalStrings = staticCompositionLocalOf { enStrings }