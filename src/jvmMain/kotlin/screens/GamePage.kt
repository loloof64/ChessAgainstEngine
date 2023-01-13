package screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import components.*
import i18n.LocalStrings
import io.github.wolfraam.chessgame.board.Square
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import logic.ChessGameManager
import logic.PreferencesManager
import logic.UciEngineChannel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GamePage(
    onBack: () -> Unit,
    onGoOptionsPageClick: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val strings = LocalStrings.current
    var purposeStopGameDialogOpen by rememberSaveable { mutableStateOf(false) }
    var boardReversed by rememberSaveable { mutableStateOf(false) }
    var gameInProgress by rememberSaveable { mutableStateOf(ChessGameManager.isGameInProgress()) }
    var boardPieces by rememberSaveable { mutableStateOf(ChessGameManager.getPieces()) }
    var isWhiteTurn by rememberSaveable { mutableStateOf(ChessGameManager.isWhiteTurn()) }
    var lastMoveArrow by rememberSaveable { mutableStateOf(ChessGameManager.getLastMoveArrow()) }
    var pendingPromotion by rememberSaveable { mutableStateOf(ChessGameManager.getPendingPromotion()) }
    var pendingPromotionStartSquare by rememberSaveable { mutableStateOf(ChessGameManager.getPendingPromotionStartSquare()) }
    var pendingPromotionEndSquare by rememberSaveable { mutableStateOf(ChessGameManager.getPendingPromotionEndSquare()) }
    val historyElements by rememberSaveable { mutableStateOf(ChessGameManager.getHistoryElements()) }
    var selectedHistoryNodeIndex by rememberSaveable { mutableStateOf(ChessGameManager.getSelectedHistoryNodeIndex()) }
    var whitePlayerType by rememberSaveable { mutableStateOf(ChessGameManager.getWhitePlayerType()) }
    var blackPlayerType by rememberSaveable { mutableStateOf(ChessGameManager.getBlackPlayerType()) }

    var cpuPlaysWhite by rememberSaveable { mutableStateOf(false) }
    var cpuPlaysBlack by rememberSaveable { mutableStateOf(false) }

    fun onCheckmate(whitePlayer: Boolean) {
        whitePlayerType = ChessGameManager.getWhitePlayerType()
        blackPlayerType = ChessGameManager.getBlackPlayerType()
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                if (whitePlayer) strings.whiteWonGame else strings.blackWonGame,
                actionLabel = strings.close,
                duration = SnackbarDuration.Long
            )
        }
    }

    fun onStalemate() {
        whitePlayerType = ChessGameManager.getWhitePlayerType()
        blackPlayerType = ChessGameManager.getBlackPlayerType()
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                strings.drawByStalemate,
                actionLabel = strings.close,
                duration = SnackbarDuration.Long
            )
        }
    }

    fun onThreeFoldRepetition() {
        whitePlayerType = ChessGameManager.getWhitePlayerType()
        blackPlayerType = ChessGameManager.getBlackPlayerType()
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                strings.drawByThreeFoldRepetition,
                actionLabel = strings.close,
                duration = SnackbarDuration.Long
            )
        }
    }

    fun onInsufficientMaterial() {
        whitePlayerType = ChessGameManager.getWhitePlayerType()
        blackPlayerType = ChessGameManager.getBlackPlayerType()
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                strings.drawByInsufficientMaterial,
                actionLabel = strings.close,
                duration = SnackbarDuration.Long
            )
        }
    }

    fun onFiftyMovesRuleDraw() {
        whitePlayerType = ChessGameManager.getWhitePlayerType()
        blackPlayerType = ChessGameManager.getBlackPlayerType()
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                strings.drawByFiftyMovesRule,
                actionLabel = strings.close,
                duration = SnackbarDuration.Long
            )
        }
    }

    fun purposeStopGame() {
        if (ChessGameManager.isGameInProgress()) {
            purposeStopGameDialogOpen = true
        }
    }

    fun stopGame() {
        ChessGameManager.stopGame()
        gameInProgress = ChessGameManager.isGameInProgress()
        selectedHistoryNodeIndex = ChessGameManager.getSelectedHistoryNodeIndex()
        whitePlayerType = ChessGameManager.getWhitePlayerType()
        blackPlayerType = ChessGameManager.getBlackPlayerType()
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                strings.gameAborted,
                actionLabel = strings.close,
                duration = SnackbarDuration.Long
            )
        }
    }

    fun onMovePlayed() {
        isWhiteTurn = ChessGameManager.isWhiteTurn()
        boardPieces = ChessGameManager.getPieces()
        pendingPromotion = ChessGameManager.getPendingPromotion()
        pendingPromotionStartSquare = ChessGameManager.getPendingPromotionStartSquare()
        pendingPromotionEndSquare = ChessGameManager.getPendingPromotionEndSquare()
        lastMoveArrow = ChessGameManager.getLastMoveArrow()
        gameInProgress = ChessGameManager.isGameInProgress()
        whitePlayerType = ChessGameManager.getWhitePlayerType()
        blackPlayerType = ChessGameManager.getBlackPlayerType()
        selectedHistoryNodeIndex = ChessGameManager.getSelectedHistoryNodeIndex()
    }

    fun onPromotionCancelled() {
        pendingPromotion = ChessGameManager.getPendingPromotion()
        pendingPromotionStartSquare = ChessGameManager.getPendingPromotionStartSquare()
        pendingPromotionEndSquare = ChessGameManager.getPendingPromotionEndSquare()
    }

    fun launchMoveComputation() {
        coroutineScope.launch (Dispatchers.Default) {
            UciEngineChannel.getBestMoveForPosition(ChessGameManager.getCurrentPosition())
        }
    }

    fun handleWhiteSideTypeChange(newState: Boolean) {
        cpuPlaysWhite = newState
        val isOurTurn = ChessGameManager.isWhiteTurn()
        if (isOurTurn && newState) {
            launchMoveComputation()
        }
    }

    fun handleBlackSideTypeChange(newState: Boolean) {
        cpuPlaysBlack = newState
        val isOurTurn = !ChessGameManager.isWhiteTurn()
        if (isOurTurn && newState) {
            launchMoveComputation()
        }
    }

    if (PreferencesManager.getEnginePath().isEmpty()) {
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                message = strings.noEngineDefinedWarning,
                actionLabel = strings.close,
                duration = SnackbarDuration.Long,
            )
            delay(500)
        }
    }

    BoxWithConstraints {
        val isLandscape = maxWidth > maxHeight
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
                            modifier = Modifier.size(30.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }, onClick = {
                        boardReversed = !boardReversed
                    })
                    IconButton(::purposeStopGame) {
                        Image(
                            painter = painterResource("icons/cancel.svg"),
                            contentDescription = strings.stopGame,
                            modifier = Modifier.size(30.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                    IconButton(
                        onGoOptionsPageClick
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            strings.preferences,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                })
            }) {

            Column {
                if (isLandscape) {
                    val heightRatio = if (PreferencesManager.getEnginePath().isNotEmpty()) 0.8f else 1.0f
                    Row(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(heightRatio),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        ChessBoardComponent(
                            isWhiteTurn = isWhiteTurn,
                            piecesValues = boardPieces,
                            reversed = boardReversed,
                            whitePlayerType = whitePlayerType,
                            blackPlayerType = blackPlayerType,
                            lastMoveArrow = lastMoveArrow,
                            pendingPromotion = pendingPromotion,
                            pendingPromotionStartSquare = pendingPromotionStartSquare,
                            pendingPromotionEndSquare = pendingPromotionEndSquare,
                            gameInProgress = gameInProgress,
                            onCheckmate = ::onCheckmate,
                            onStalemate = ::onStalemate,
                            onFiftyMovesRuleDraw = ::onFiftyMovesRuleDraw,
                            onThreeFoldRepetition = ::onThreeFoldRepetition,
                            onInsufficientMaterial = ::onInsufficientMaterial,
                            onMovePlayed = ::onMovePlayed,
                            onPromotionCancelled = ::onPromotionCancelled,
                        )

                        HistoryComponent(
                            historyElements = historyElements,
                            selectedHistoryNodeIndex = selectedHistoryNodeIndex,
                            onPositionSelected = {
                                isWhiteTurn = ChessGameManager.isWhiteTurn()
                                boardPieces = ChessGameManager.getPieces()
                                lastMoveArrow = ChessGameManager.getLastMoveArrow()
                                selectedHistoryNodeIndex = ChessGameManager.getSelectedHistoryNodeIndex()
                            },
                        )
                    }
                } else {
                    val heightRatio = if (PreferencesManager.getEnginePath().isNotEmpty()) 0.8f else 1.0f
                    Column(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(heightRatio),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    )
                    {
                        Column(modifier = Modifier.fillMaxHeight(0.6f)) {
                            ChessBoardComponent(
                                isWhiteTurn = isWhiteTurn,
                                piecesValues = boardPieces,
                                reversed = boardReversed,
                                whitePlayerType = whitePlayerType,
                                blackPlayerType = blackPlayerType,
                                lastMoveArrow = lastMoveArrow,
                                pendingPromotion = pendingPromotion,
                                pendingPromotionStartSquare = pendingPromotionStartSquare,
                                pendingPromotionEndSquare = pendingPromotionEndSquare,
                                gameInProgress = gameInProgress,
                                onCheckmate = ::onCheckmate,
                                onStalemate = ::onStalemate,
                                onFiftyMovesRuleDraw = ::onFiftyMovesRuleDraw,
                                onThreeFoldRepetition = ::onThreeFoldRepetition,
                                onInsufficientMaterial = ::onInsufficientMaterial,
                                onMovePlayed = ::onMovePlayed,
                                onPromotionCancelled = ::onPromotionCancelled,
                            )
                        }

                        HistoryComponent(
                            historyElements = historyElements,
                            selectedHistoryNodeIndex = selectedHistoryNodeIndex,
                            onPositionSelected = {
                                isWhiteTurn = ChessGameManager.isWhiteTurn()
                                boardPieces = ChessGameManager.getPieces()
                                lastMoveArrow = ChessGameManager.getLastMoveArrow()
                                selectedHistoryNodeIndex = ChessGameManager.getSelectedHistoryNodeIndex()
                            },
                        )
                    }
                }


                if (PreferencesManager.getEnginePath().isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(6.dp)
                    ) {
                        Text(strings.computerSidesOptions)

                        Checkbox(
                            modifier = Modifier.padding(start = 20.dp, end = 0.dp),
                            checked = cpuPlaysWhite,
                            onCheckedChange = ::handleWhiteSideTypeChange,
                        )
                        Text(strings.computerPlaysWhite)

                        Checkbox(
                            modifier = Modifier.padding(start = 20.dp, end = 0.dp),
                            checked = cpuPlaysBlack,
                            onCheckedChange = ::handleBlackSideTypeChange,
                        )
                        Text(strings.computerPlaysBlack)
                    }
                }

            }

            if (purposeStopGameDialogOpen) {
                AlertDialog(
                    onDismissRequest = {
                        purposeStopGameDialogOpen = false
                    },
                    title = {
                        Text(strings.purposeStopGameTitle)
                    },
                    text = {
                        Text(strings.purposeStopGameMessage)
                    },
                    confirmButton = {
                        Button({
                            purposeStopGameDialogOpen = false
                            stopGame()
                        }) {
                            Text(strings.validate)
                        }
                    },
                    dismissButton = {
                        Button({
                            purposeStopGameDialogOpen = false
                        }) {
                            Text(strings.cancel)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ChessBoardComponent(
    gameInProgress: Boolean,
    isWhiteTurn: Boolean,
    reversed: Boolean,
    piecesValues: List<List<Char>>,
    whitePlayerType: PlayerType,
    blackPlayerType: PlayerType,
    lastMoveArrow: LastMoveArrow?,
    pendingPromotion: PendingPromotion,
    pendingPromotionStartSquare: Square?,
    pendingPromotionEndSquare: Square?,
    onCheckmate: (Boolean) -> Unit,
    onStalemate: () -> Unit,
    onThreeFoldRepetition: () -> Unit,
    onInsufficientMaterial: () -> Unit,
    onFiftyMovesRuleDraw: () -> Unit,
    onMovePlayed: () -> Unit,
    onPromotionCancelled: () -> Unit,
) {
    ChessBoard(isWhiteTurn = isWhiteTurn,
        piecesValues = piecesValues,
        reversed = reversed,
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
                onCheckmate = onCheckmate,
                onStalemate = onStalemate,
                onThreeFoldsRepetition = onThreeFoldRepetition,
                onInsufficientMaterial = onInsufficientMaterial,
                onFiftyMovesRuleDraw = onFiftyMovesRuleDraw,
            )
            onMovePlayed()
        },
        onCancelPromotion = {
            if (!gameInProgress) return@ChessBoard
            ChessGameManager.cancelPromotion()
            onPromotionCancelled()
        },
        onValidatePromotion = {
            if (!gameInProgress) return@ChessBoard
            ChessGameManager.commitPromotion(
                pieceType = it,
                onCheckmate = onCheckmate,
                onStalemate = onStalemate,
                onThreeFoldsRepetition = onThreeFoldRepetition,
                onInsufficientMaterial = onInsufficientMaterial,
                onFiftyMovesRuleDraw = onFiftyMovesRuleDraw,
            )
            onMovePlayed()
        }
    )
}

@Composable
fun HistoryComponent(
    historyElements: List<ChessHistoryItem>,
    selectedHistoryNodeIndex: Int?,
    onPositionSelected: () -> Unit,
) {
    ChessHistory(
        items = historyElements,
        selectedNodeIndex = selectedHistoryNodeIndex,
        onPositionRequest = { positionFen, moveCoordinates, nodeToSelectIndex ->
            val success = ChessGameManager.requestPosition(
                positionFen = positionFen,
                moveCoordinates = moveCoordinates,
                nodeToSelectIndex
            )
            if (success) {
                onPositionSelected()
            }
        },
        onRequestBackOneMove = {
            val success = ChessGameManager.requestGotoPreviousHistoryNode()
            if (success) {
                onPositionSelected()
            }
        },
        onRequestForwardOneMove = {
            val success = ChessGameManager.requestGotoNextHistoryNode()
            if (success) {
                onPositionSelected()
            }
        },
        onRequestGotoFirstPosition = {
            val success = ChessGameManager.requestGotoFirstPosition()
            if (success) {
                onPositionSelected()
            }
        },
        onRequestGotoLastMove = {
            val success = ChessGameManager.requestGotoLastHistoryNode()
            if (success) {
                onPositionSelected()
            }
        }
    )
}