package screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import components.*
import i18n.LocalStrings
import kotlinx.coroutines.launch
import logic.ChessGameManager

@Composable
fun GamePage(
    onBack: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val strings = LocalStrings.current
    var boardReversed by rememberSaveable { mutableStateOf(false) }
    var gameInProgress by rememberSaveable { mutableStateOf(ChessGameManager.isGameInProgress()) }
    var boardPieces by rememberSaveable { mutableStateOf(ChessGameManager.getPieces()) }
    var isWhiteTurn by rememberSaveable { mutableStateOf(ChessGameManager.isWhiteTurn()) }
    var lastMoveArrow by rememberSaveable { mutableStateOf(ChessGameManager.getLastMoveArrow()) }
    var pendingPromotion by rememberSaveable { mutableStateOf(ChessGameManager.getPendingPromotion()) }
    var pendingPromotionStartSquare by rememberSaveable { mutableStateOf(ChessGameManager.getPendingPromotionStartSquare()) }
    var pendingPromotionEndSquare by rememberSaveable { mutableStateOf(ChessGameManager.getPendingPromotionEndSquare()) }
    var whitePlayerType by rememberSaveable { mutableStateOf(PlayerType.Human) }
    var blackPlayerType by rememberSaveable { mutableStateOf(PlayerType.Human) }

    fun onCheckmate(whitePlayer: Boolean) {
        whitePlayerType = PlayerType.Computer
        blackPlayerType = PlayerType.Computer
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                if (whitePlayer) strings.playerWonGame else strings.playerLostGame,
                actionLabel = strings.close,
                duration = SnackbarDuration.Long
            )
        }
    }

    fun onStalemate() {
        whitePlayerType = PlayerType.Computer
        blackPlayerType = PlayerType.Computer
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                strings.drawByStalemate,
                actionLabel = strings.close,
                duration = SnackbarDuration.Long
            )
        }
    }

    fun onThreeFoldRepetition() {
        whitePlayerType = PlayerType.Computer
        blackPlayerType = PlayerType.Computer
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                strings.drawByThreeFoldRepetition,
                actionLabel = strings.close,
                duration = SnackbarDuration.Long
            )
        }
    }

    fun onInsufficientMaterial() {
        whitePlayerType = PlayerType.Computer
        blackPlayerType = PlayerType.Computer
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                strings.drawByInsufficientMaterial,
                actionLabel = strings.close,
                duration = SnackbarDuration.Long
            )
        }
    }

    fun onFiftyMovesRuleDraw() {
        whitePlayerType = PlayerType.Computer
        blackPlayerType = PlayerType.Computer
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                strings.drawByFiftyMovesRule,
                actionLabel = strings.close,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(title = { Text(strings.gamePageTitle) }, navigationIcon = {
                IconButton(onBack) {
                    Icon(Icons.Default.ArrowBack, strings.goBack)
                }
            }, actions = {
                IconButton(content = {
                    Image(
                        painter = painterResource("icons/swap_vert.svg"),
                        contentDescription = strings.swapBoardOrientation,
                        modifier = Modifier,
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }, onClick = {
                    boardReversed = !boardReversed
                })
            })
        }) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChessBoard(isWhiteTurn = isWhiteTurn,
                piecesValues = boardPieces,
                reversed = boardReversed,
                whitePlayerType = whitePlayerType,
                blackPlayerType = blackPlayerType,
                lastMoveArrow = lastMoveArrow,
                pendingPromotion = pendingPromotion,
                pendingPromotionStartFile = pendingPromotionStartSquare?.x,
                pendingPromotionStartRank = pendingPromotionStartSquare?.y,
                pendingPromotionEndFile = pendingPromotionEndSquare?.x,
                pendingPromotionEndRank = pendingPromotionEndSquare?.y,
                tryPlayingMove = { dragAndDropData ->
                    if (!gameInProgress) return@ChessBoard
                    ChessGameManager.playMove(
                        startFile = dragAndDropData.startFile,
                        startRank = dragAndDropData.startRank,
                        endFile = dragAndDropData.endFile,
                        endRank = dragAndDropData.endRank,
                        onCheckmate = ::onCheckmate,
                        onStalemate = ::onStalemate,
                        onThreeFoldsRepetition = ::onThreeFoldRepetition,
                        onInsufficientMaterial = ::onInsufficientMaterial,
                        onFiftyMovesRuleDraw = ::onFiftyMovesRuleDraw,
                    )
                    isWhiteTurn = ChessGameManager.isWhiteTurn()
                    boardPieces = ChessGameManager.getPieces()
                    pendingPromotion = ChessGameManager.getPendingPromotion()
                    pendingPromotionStartSquare = ChessGameManager.getPendingPromotionStartSquare()
                    pendingPromotionEndSquare = ChessGameManager.getPendingPromotionEndSquare()
                    lastMoveArrow = ChessGameManager.getLastMoveArrow()
                    gameInProgress = ChessGameManager.isGameInProgress()
                },
                onCancelPromotion = {
                    if (!gameInProgress) return@ChessBoard
                    ChessGameManager.cancelPromotion()
                    pendingPromotion = ChessGameManager.getPendingPromotion()
                    pendingPromotionStartSquare = ChessGameManager.getPendingPromotionStartSquare()
                    pendingPromotionEndSquare = ChessGameManager.getPendingPromotionEndSquare()
                },
                onValidatePromotion = {
                    if (!gameInProgress) return@ChessBoard
                    ChessGameManager.commitPromotion(
                        pieceType = it,
                        onCheckmate = ::onCheckmate,
                        onStalemate = ::onStalemate,
                        onThreeFoldsRepetition = ::onThreeFoldRepetition,
                        onInsufficientMaterial = ::onInsufficientMaterial,
                        onFiftyMovesRuleDraw = ::onFiftyMovesRuleDraw,
                    )
                    isWhiteTurn = ChessGameManager.isWhiteTurn()
                    boardPieces = ChessGameManager.getPieces()
                    pendingPromotion = ChessGameManager.getPendingPromotion()
                    pendingPromotionStartSquare = ChessGameManager.getPendingPromotionStartSquare()
                    pendingPromotionEndSquare = ChessGameManager.getPendingPromotionEndSquare()
                    lastMoveArrow = ChessGameManager.getLastMoveArrow()
                    gameInProgress = ChessGameManager.isGameInProgress()
                }
            )

            ChessHistory(
                items = listOf(
                    ChessHistoryItem.MoveNumberItem(2, false),
                    ChessHistoryItem.MoveItem("Nf3", ""),
                    ChessHistoryItem.MoveItem("Nf6", ""),
                    ChessHistoryItem.GameTerminationItem(GameTermination.InProgress)
                )
            ) {
                // not processing requests as now
            }
        }
    }
}