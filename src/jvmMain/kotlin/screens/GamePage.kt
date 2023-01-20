package screens

import VerticalNumberPicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import components.*
import i18n.LocalStrings
import io.github.wolfraam.chessgame.board.Square
import kotlinx.coroutines.*
import logic.ChessGameManager
import logic.MoveTime
import logic.PreferencesManager
import logic.UciEngineChannel
import java.awt.KeyboardFocusManager
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GamePage(
    navigation: StackNavigation<Screen>,
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val strings = LocalStrings.current
    var purposeStopGameDialogOpen by rememberSaveable { mutableStateOf(false) }
    var confirmExitGameDialogOpen by rememberSaveable { mutableStateOf(false) }
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
    var engineIsThinking by rememberSaveable { mutableStateOf(false) }

    var cpuPlaysWhiteChecked by rememberSaveable { mutableStateOf(false) }
    var cpuPlaysBlackChecked by rememberSaveable { mutableStateOf(false) }
    var cpuScoreEvaluation by rememberSaveable { mutableStateOf(0.0f) }
    var showCpuScoreEvaluation by rememberSaveable { mutableStateOf(false) }

    var whiteTimeInDeciSeconds by rememberSaveable { mutableStateOf(0) }
    var blackTimeInDeciSeconds by rememberSaveable { mutableStateOf(0) }
    var whiteTimeActive by rememberSaveable { mutableStateOf(true) }
    var clockActive by rememberSaveable { mutableStateOf(false) }
    var allocatedTimeInDeciSeconds by rememberSaveable { mutableStateOf(600) }

    var clockJob by rememberSaveable { mutableStateOf<Job?>(null) }

    fun stopGameByTimeout(whiteTimeout: Boolean) {
        engineIsThinking = false
        clockJob?.cancel()
        clockJob = null
        clockActive = false

        if (ChessGameManager.checkIfPlayerWinningOnTimeIsMissingMaterialAndUpdatePgnResultTag()) {
            ChessGameManager.stopGame()
            gameInProgress = ChessGameManager.isGameInProgress()
            selectedHistoryNodeIndex = ChessGameManager.getSelectedHistoryNodeIndex()
            whitePlayerType = ChessGameManager.getWhitePlayerType()
            blackPlayerType = ChessGameManager.getBlackPlayerType()

            val message = strings.drawOnTimeByInsufficientMaterial
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message,
                    actionLabel = strings.close,
                    duration = SnackbarDuration.Long
                )
            }
        } else {
            ChessGameManager.stopGame()
            gameInProgress = ChessGameManager.isGameInProgress()
            selectedHistoryNodeIndex = ChessGameManager.getSelectedHistoryNodeIndex()
            whitePlayerType = ChessGameManager.getWhitePlayerType()
            blackPlayerType = ChessGameManager.getBlackPlayerType()

            val message = if (whiteTimeout) strings.blackWonOnTime else strings.whiteWonOnTime
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message,
                    actionLabel = strings.close,
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    fun handleClockActiveChange(newState: Boolean) {
        clockActive = newState
        if (newState) {
            whiteTimeInDeciSeconds = allocatedTimeInDeciSeconds
            blackTimeInDeciSeconds = allocatedTimeInDeciSeconds

            clockJob = coroutineScope.launch {
                while (isActive) {
                    delay(100)
                    if (whiteTimeActive) {
                        whiteTimeInDeciSeconds--
                        if (whiteTimeInDeciSeconds <= 0) {
                            stopGameByTimeout(true)
                        }
                    } else {
                        blackTimeInDeciSeconds--
                        if (blackTimeInDeciSeconds <= 0) {
                            stopGameByTimeout(false)
                        }
                    }
                }
            }
        } else {
            clockJob?.cancel()
            clockJob = null
        }
    }

    fun justUpdatePositionEvaluation() {
        coroutineScope.launch {
            UciEngineChannel.getNewPositionEvaluation(ChessGameManager.getCurrentPosition())
        }
    }

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
                strings.drawByStalemate, actionLabel = strings.close, duration = SnackbarDuration.Long
            )
        }
    }

    fun onThreeFoldRepetition() {
        whitePlayerType = ChessGameManager.getWhitePlayerType()
        blackPlayerType = ChessGameManager.getBlackPlayerType()
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                strings.drawByThreeFoldRepetition, actionLabel = strings.close, duration = SnackbarDuration.Long
            )
        }
    }

    fun onInsufficientMaterial() {
        whitePlayerType = ChessGameManager.getWhitePlayerType()
        blackPlayerType = ChessGameManager.getBlackPlayerType()
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                strings.drawByInsufficientMaterial, actionLabel = strings.close, duration = SnackbarDuration.Long
            )
        }
    }

    fun onFiftyMovesRuleDraw() {
        whitePlayerType = ChessGameManager.getWhitePlayerType()
        blackPlayerType = ChessGameManager.getBlackPlayerType()
        coroutineScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                strings.drawByFiftyMovesRule, actionLabel = strings.close, duration = SnackbarDuration.Long
            )
        }
    }

    fun purposeStopGame() {
        if (ChessGameManager.isGameInProgress()) {
            purposeStopGameDialogOpen = true
        }
    }

    fun stopGame(shouldShowSnackBarMessage: Boolean = true) {
        handleClockActiveChange(false)

        engineIsThinking = false
        ChessGameManager.stopGame()
        gameInProgress = ChessGameManager.isGameInProgress()
        selectedHistoryNodeIndex = ChessGameManager.getSelectedHistoryNodeIndex()
        whitePlayerType = ChessGameManager.getWhitePlayerType()
        blackPlayerType = ChessGameManager.getBlackPlayerType()
        if (shouldShowSnackBarMessage) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    strings.gameAborted, actionLabel = strings.close, duration = SnackbarDuration.Long
                )
            }
        }
    }

    fun launchMoveComputation() {
        coroutineScope.launch(Dispatchers.Default) {
            if (clockActive) {
                UciEngineChannel.getBestMoveForPosition(ChessGameManager.getCurrentPosition(), MoveTime(
                    whiteTimeMillis = whiteTimeInDeciSeconds * 100L,
                    blackTimeMillis = blackTimeInDeciSeconds * 100L,
                ))
            } else {
                UciEngineChannel.getBestMoveForPosition(ChessGameManager.getCurrentPosition(), null)
            }
        }
    }

    fun makeCpuPlayIfAppropriated() {
        if (!gameInProgress) return
        if (engineIsThinking) return

        val isCpuTurn =
            (isWhiteTurn && whitePlayerType == PlayerType.Computer) || (!isWhiteTurn && blackPlayerType == PlayerType.Computer)
        if (isCpuTurn) {
            engineIsThinking = true
            launchMoveComputation()
        }
    }

    fun chainCpuMoveIfAppropriated() {
        val isPlayerTurn = (isWhiteTurn && whitePlayerType == PlayerType.Human) ||
                (!isWhiteTurn && blackPlayerType == PlayerType.Human)

        if (isPlayerTurn) {
            justUpdatePositionEvaluation()
        } else {
            makeCpuPlayIfAppropriated()
        }
    }

    fun onMovePlayed() {
        whiteTimeActive = !whiteTimeActive
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

        chainCpuMoveIfAppropriated()
    }

    fun onPromotionCancelled() {
        pendingPromotion = ChessGameManager.getPendingPromotion()
        pendingPromotionStartSquare = ChessGameManager.getPendingPromotionStartSquare()
        pendingPromotionEndSquare = ChessGameManager.getPendingPromotionEndSquare()
    }

    fun handleWhiteSideTypeChange(newState: Boolean) {
        cpuPlaysWhiteChecked = newState
        if (gameInProgress) {
            whitePlayerType = if (newState) PlayerType.Computer else PlayerType.Human
            ChessGameManager.setWhitePlayerType(whitePlayerType)
            val weMustCancelCpuThinking = !newState && engineIsThinking && isWhiteTurn
            if (weMustCancelCpuThinking) {
                engineIsThinking = false
                coroutineScope.launch {
                    UciEngineChannel.stopCurrentComputation()
                }
            }
            makeCpuPlayIfAppropriated()
        }
    }

    fun handleBlackSideTypeChange(newState: Boolean) {
        cpuPlaysBlackChecked = newState
        if (gameInProgress) {
            blackPlayerType = if (newState) PlayerType.Computer else PlayerType.Human
            ChessGameManager.setBlackPlayerType(blackPlayerType)
            val weMustCancelCpuThinking = !newState && engineIsThinking && !isWhiteTurn
            if (weMustCancelCpuThinking) {
                engineIsThinking = false
                coroutineScope.launch {
                    UciEngineChannel.stopCurrentComputation()
                }
            }
            makeCpuPlayIfAppropriated()
        }
    }

    fun hoursFor(timeInDeciSeconds: Int): Int {
        val timeSeconds = timeInDeciSeconds / 10
        return timeSeconds / 3600
    }

    fun minutesFor(timeInDeciSeconds: Int): Int {
        val timeSeconds = timeInDeciSeconds / 10
        var result = timeSeconds % 3600
        result /= 60
        return result
    }

    fun secondsFor(timeInDeciSeconds: Int): Int {
        val timeSeconds = timeInDeciSeconds / 10
        var result = timeSeconds % 3600
        result %= 60
        return result
    }

    fun updateAllocatedHours(newHoursCount: Int) {
        val currentAllocatedMinutes = minutesFor(allocatedTimeInDeciSeconds)
        val currentAllocatedSeconds = secondsFor(allocatedTimeInDeciSeconds)
        allocatedTimeInDeciSeconds =
            newHoursCount * 3600_0 + currentAllocatedMinutes * 60_0 + currentAllocatedSeconds * 10
    }

    fun updateAllocatedMinutes(newMinutesCount: Int) {
        val currentAllocatedHours = hoursFor(allocatedTimeInDeciSeconds)
        val currentAllocatedSeconds = secondsFor(allocatedTimeInDeciSeconds)
        allocatedTimeInDeciSeconds =
            currentAllocatedHours * 3600_0 + newMinutesCount * 60_0 + currentAllocatedSeconds * 10
    }

    fun updateAllocatedSeconds(newSecondsCount: Int) {
        val currentAllocatedHours = hoursFor(allocatedTimeInDeciSeconds)
        val currentAllocatedMinutes = minutesFor(allocatedTimeInDeciSeconds)
        allocatedTimeInDeciSeconds =
            currentAllocatedHours * 3600_0 + currentAllocatedMinutes * 60_0 + newSecondsCount * 10
    }

    fun purposeSaveGameInPgnFile() {
        if (gameInProgress) return
        val folder = PreferencesManager.loadSavePgnFolder()
        val fileChooser = if (folder.isNotEmpty()) JFileChooser(folder) else JFileChooser()
        fileChooser.dialogTitle = strings.selectSavePgnPathDialogTitle
        fileChooser.approveButtonText = strings.validate

        val pgnFilter = FileNameExtensionFilter(strings.pgnFileType, "pgn")
        val currentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow
        fileChooser.addChoosableFileFilter(pgnFilter)
        fileChooser.isAcceptAllFileFilterUsed = true
        val actionResult = fileChooser.showSaveDialog(currentWindow)
        if (actionResult == JFileChooser.APPROVE_OPTION) {
            PreferencesManager.saveSavePgnFolder(fileChooser.currentDirectory.absolutePath)
            val selectedFile = fileChooser.selectedFile
            try {
                ChessGameManager.exportAsPgn(selectedFile)
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = strings.pgnExported,
                        actionLabel = strings.close,
                        duration = SnackbarDuration.Long,
                    )
                }
            } catch (ex: Exception) {
                println(ex)
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = strings.failedSavingPgnFile,
                        actionLabel = strings.close,
                        duration = SnackbarDuration.Long,
                    )
                }
            }
        }
    }

    UciEngineChannel.setBestMoveCallback {
        if (!engineIsThinking) return@setBestMoveCallback
        ChessGameManager.processEngineMove(
            uciMove = it,
            onCheckmate = ::onCheckmate,
            onStalemate = ::onStalemate,
            onThreeFoldsRepetition = ::onThreeFoldRepetition,
            onInsufficientMaterial = ::onInsufficientMaterial,
            onFiftyMovesRuleDraw = ::onFiftyMovesRuleDraw,
        )
        whiteTimeActive = !whiteTimeActive

        isWhiteTurn = ChessGameManager.isWhiteTurn()
        boardPieces = ChessGameManager.getPieces()
        lastMoveArrow = ChessGameManager.getLastMoveArrow()
        gameInProgress = ChessGameManager.isGameInProgress()
        whitePlayerType = ChessGameManager.getWhitePlayerType()
        blackPlayerType = ChessGameManager.getBlackPlayerType()
        selectedHistoryNodeIndex = ChessGameManager.getSelectedHistoryNodeIndex()

        engineIsThinking = false

        chainCpuMoveIfAppropriated()
    }
    UciEngineChannel.setScoreCallback {
        cpuScoreEvaluation = if (isWhiteTurn) it else -it
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

    chainCpuMoveIfAppropriated()

    BoxWithConstraints {
        val isLandscape = maxWidth > maxHeight
        Scaffold(scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { Text(strings.gamePageTitle) },
                    navigationIcon = {
                        IconButton({ confirmExitGameDialogOpen = true }) {
                            Icon(Icons.Default.ArrowBack, strings.goBack)
                        }
                    }, actions = {
                        IconButton(content = {
                            Image(
                                painter = painterResource("images/material_vectors/swap_vert.svg"),
                                contentDescription = strings.swapBoardOrientation,
                                modifier = Modifier.size(30.dp),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }, onClick = {
                            boardReversed = !boardReversed
                        })
                        IconButton(::purposeStopGame) {
                            Image(
                                painter = painterResource("images/material_vectors/cancel.svg"),
                                contentDescription = strings.stopGame,
                                modifier = Modifier.size(30.dp),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }
                        if (!gameInProgress) {
                            IconButton({
                                navigation.push(Screen.Options)
                            }) {
                                Icon(
                                    Icons.Default.Settings, strings.preferences, modifier = Modifier.size(30.dp)
                                )
                            }
                            IconButton({
                                purposeSaveGameInPgnFile()
                            }) {
                                Image(
                                    painter = painterResource("images/material_vectors/save.svg"),
                                    contentDescription = strings.saveGameInPgn,
                                    modifier = Modifier.size(30.dp),
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                            }
                        }
                    })
            }) {

            val spinnerSizeRatio = 0.3f

            Column {
                Box {
                    if (isLandscape) {
                        val heightRatio =
                            if (PreferencesManager.getEnginePath().isNotEmpty() && gameInProgress) 0.65f else 1.0f
                        Row(
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(heightRatio),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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

                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top,
                            ) {
                                ClockComponent(
                                    whiteTimeInDeciSeconds = whiteTimeInDeciSeconds,
                                    blackTimeInDeciSeconds = blackTimeInDeciSeconds,
                                    whiteTimeActive = whiteTimeActive,
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
                        }
                    } else {
                        val heightRatio =
                            if (PreferencesManager.getEnginePath().isNotEmpty() && gameInProgress) 0.70f else 1.0f
                        Column(
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(heightRatio),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
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

                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top,
                            ) {
                                ClockComponent(
                                    whiteTimeInDeciSeconds = whiteTimeInDeciSeconds,
                                    blackTimeInDeciSeconds = blackTimeInDeciSeconds,
                                    whiteTimeActive = whiteTimeActive,
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
                        }
                    }

                    if (engineIsThinking) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxSize(spinnerSizeRatio)
                                .align(Alignment.Center)
                        )
                    }
                }


                if (PreferencesManager.getEnginePath().isNotEmpty() && gameInProgress) {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(6.dp)
                    ) {
                        Text(strings.computerSidesOptions)

                        Checkbox(
                            modifier = Modifier.padding(start = 20.dp, end = 0.dp),
                            checked = cpuPlaysWhiteChecked,
                            onCheckedChange = ::handleWhiteSideTypeChange,
                        )
                        Text(strings.computerPlaysWhite)

                        Checkbox(
                            modifier = Modifier.padding(start = 20.dp, end = 0.dp),
                            checked = cpuPlaysBlackChecked,
                            onCheckedChange = ::handleBlackSideTypeChange,
                        )
                        Text(strings.computerPlaysBlack)

                        Checkbox(
                            modifier = Modifier.padding(start = 50.dp, end = 0.dp),
                            checked = showCpuScoreEvaluation,
                            onCheckedChange = { showCpuScoreEvaluation = !showCpuScoreEvaluation },
                        )
                        Text(strings.showComputerScoreEvaluation)

                        if (showCpuScoreEvaluation) {
                            var color = Color.Blue
                            if (cpuScoreEvaluation >= 1.0f) color = Color.Green
                            if (cpuScoreEvaluation <= -1.0f) color = Color.Red

                            Text(
                                modifier = Modifier.padding(start = 20.dp, end = 0.dp),
                                text = cpuScoreEvaluation.toString(),
                                color = color
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(6.dp)
                    ) {
                        Checkbox(
                            checked = clockActive,
                            onCheckedChange = { handleClockActiveChange(it) },
                        )
                        Text(strings.timedGame)

                        VerticalNumberPicker(
                            value = hoursFor(allocatedTimeInDeciSeconds),
                            range = 0..3,
                            onStateChanged = {
                                updateAllocatedHours(it)
                            }
                        )
                        Text("h")

                        VerticalNumberPicker(
                            value = minutesFor(allocatedTimeInDeciSeconds),
                            range = 0..59,
                            onStateChanged = {
                                updateAllocatedMinutes(it)
                            }
                        )
                        Text("m")

                        VerticalNumberPicker(
                            value = secondsFor(allocatedTimeInDeciSeconds),
                            range = 0..59,
                            onStateChanged = {
                                updateAllocatedSeconds(it)
                            }
                        )
                        Text("s")
                    }
                }
            }
        }

        if (purposeStopGameDialogOpen) {
            AlertDialog(onDismissRequest = {
                purposeStopGameDialogOpen = false
            }, title = {
                Text(strings.purposeStopGameTitle)
            }, text = {
                Text(strings.purposeStopGameMessage)
            }, confirmButton = {
                Button({
                    purposeStopGameDialogOpen = false
                    stopGame()
                }) {
                    Text(strings.validate)
                }
            }, dismissButton = {
                Button({
                    purposeStopGameDialogOpen = false
                }) {
                    Text(strings.cancel)
                }
            })
        }

        if (confirmExitGameDialogOpen) {
            AlertDialog(onDismissRequest = {
                confirmExitGameDialogOpen = false
            }, title = {
                Text(strings.confirmExitGameTitle)
            }, text = {
                Text(strings.confirmExitGameMessage)
            }, confirmButton = {
                Button({
                    confirmExitGameDialogOpen = false
                    stopGame(shouldShowSnackBarMessage = false)
                    navigation.pop()
                }) {
                    Text(strings.validate)
                }
            }, dismissButton = {
                Button({
                    confirmExitGameDialogOpen = false
                }) {
                    Text(strings.cancel)
                }
            })
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
    ChessBoard(
        isWhiteTurn = isWhiteTurn,
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
        })
}

@Composable
fun HistoryComponent(
    historyElements: List<ChessHistoryItem>,
    selectedHistoryNodeIndex: Int?,
    onPositionSelected: () -> Unit,
) {
    ChessHistory(items = historyElements,
        selectedNodeIndex = selectedHistoryNodeIndex,
        onPositionRequest = { positionFen, moveCoordinates, nodeToSelectIndex ->
            val success = ChessGameManager.requestPosition(
                positionFen = positionFen, moveCoordinates = moveCoordinates, nodeToSelectIndex
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
        })
}

private fun getTimeText(timeInDeciSeconds: Int): String {
    val pattern = if (timeInDeciSeconds >= 36000) "HH:mm:ss"
    else if (timeInDeciSeconds >= 600) "mm:ss"
    else "ss.S"
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    val simpleDateFormat = SimpleDateFormat(pattern, Locale.US)
    simpleDateFormat.timeZone = cal.timeZone
    cal.timeInMillis = (timeInDeciSeconds * 100).toLong()
    return simpleDateFormat.format(cal.time)
}

@Composable
fun ClockComponent(
    modifier: Modifier = Modifier,
    whiteTimeInDeciSeconds: Int,
    blackTimeInDeciSeconds: Int,
    whiteTimeActive: Boolean,
) {
    val whiteText = getTimeText(whiteTimeInDeciSeconds)
    val blackText = getTimeText(blackTimeInDeciSeconds)

    var whiteZoneFgColor = Color.Black
    var blackZoneFgColor = Color.White

    var whiteZoneBgColor = Color.White
    var blackZoneBgColor = Color.Black

    if (whiteTimeActive) {
        if (whiteTimeInDeciSeconds < 600) {
            whiteZoneBgColor = Color.Red
            whiteZoneFgColor = Color.White
        } else {
            whiteZoneBgColor = Color.Blue
            whiteZoneFgColor = Color.Yellow
        }
    } else {
        if (blackTimeInDeciSeconds < 600) {
            blackZoneBgColor = Color.Red
            blackZoneFgColor = Color.White
        } else {
            blackZoneBgColor = Color.Blue
            blackZoneFgColor = Color.Yellow
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.1f)
            .border(width = 1.dp, color = Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight()
                .background(whiteZoneBgColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = whiteText,
                color = whiteZoneFgColor,
                textAlign = TextAlign.Center,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(blackZoneBgColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = blackText,
                color = blackZoneFgColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}