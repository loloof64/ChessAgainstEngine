package screens

import HorizontalNumberPicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import components.ChessBoard
import components.PendingPromotion
import components.PlayerType
import i18n.LocalStrings
import io.github.wolfraam.chessgame.ChessGame
import io.github.wolfraam.chessgame.pgn.PGNImporter
import io.github.wolfraam.chessgame.pgn.PgnTag
import kotlinx.coroutines.launch
import logic.*
import java.io.FileInputStream


@Composable
fun PgnGamesPage(
    navigation: StackNavigation<Screen>,
    selectedFilePath: String,
) {
    val strings = LocalStrings.current
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    var allGames by rememberSaveable { mutableStateOf<List<ChessGame>>(listOf()) }
    var currentGameIndex by rememberSaveable { mutableStateOf(0) }
    var currentFen by rememberSaveable { mutableStateOf(defaultPosition) }
    var whitePlayer by rememberSaveable { mutableStateOf("") }
    var blackPlayer by rememberSaveable { mutableStateOf("") }

    fun isWhiteTurnForFen(fen: String): Boolean {
        return fen.split(" ")[1] == "w"
    }

    fun loadDataFromCurrentGame() {
        val currentGame = allGames[currentGameIndex]
        val fenValue = currentGame.getPgnTagValue(PgnTag.FEN)
        currentFen = fenValue?.ifEmpty { defaultPosition } ?: defaultPosition
        whitePlayer = currentGame.getPgnTagValue(PgnTag.WHITE).ifEmpty { strings.unknownPlayer }
        blackPlayer = currentGame.getPgnTagValue(PgnTag.BLACK).ifEmpty { strings.unknownPlayer }
    }

    fun getGamesFromFile(): List<ChessGame> {
        val result = mutableListOf<ChessGame>()
        val errors = mutableListOf<String>()
        val pgnImporter = PGNImporter()
        pgnImporter.setOnGame {
            result.add(it)
        }
        pgnImporter.setOnError {
            errors.add(it)
        }
        pgnImporter.setOnWarning {
            println(it)
        }
        pgnImporter.run(FileInputStream(selectedFilePath))

        if (errors.isNotEmpty()) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    strings.errorImportingSomePgnGames,
                    actionLabel = strings.close,
                    duration = SnackbarDuration.Long
                )
            }
        }

        return result
    }

    val games = getGamesFromFile()
    if (games.isNotEmpty()) {
        allGames = games
        loadDataFromCurrentGame()
    } else {
        allGames = listOf()
    }

    fun gotoFirstGame() {
        currentGameIndex = 0
    }

    fun gotoPreviousGame() {
        if (currentGameIndex > 0) currentGameIndex--
    }

    fun gotoNextGame() {
        if (currentGameIndex < games.size.dec()) currentGameIndex++
    }

    fun gotoLastGame() {
        currentGameIndex = games.size.dec()
    }

    fun onValidate() {
        try {
            ChessGameManager.setStartPosition(currentFen)
            ChessGameManager.resetGame()
            navigation.pop()
            navigation.push(Screen.Game)
        }
        catch (ex: KingNotInTurnIsInCheck) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = strings.oppositeKingInCheckFen,
                    actionLabel = strings.close,
                    duration = SnackbarDuration.Long,
                )
            }
        }
        catch (ex: WrongFieldsCountException) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = strings.wrongFieldsCountFen,
                    actionLabel = strings.close,
                    duration = SnackbarDuration.Long,
                )
            }
        }
        catch (ex: WrongKingsCountException) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = strings.wrongKingsCountFen,
                    actionLabel = strings.close,
                    duration = SnackbarDuration.Long,
                )
            }
        }
    }

    fun onCancel() {
        navigation.pop()
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(strings.loadPgnPageTitle)
                },
                navigationIcon = {
                    IconButton({ navigation.pop() }) {
                        Icon(Icons.Default.ArrowBack, strings.goBack)
                    }
                },
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (allGames.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(::gotoFirstGame, modifier = Modifier.padding(5.dp)) {
                        Image(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource("images/material_vectors/keyboard_double_arrow_left.svg"),
                            contentDescription = strings.gotoFirstGame,
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                    Button(::gotoPreviousGame, modifier = Modifier.padding(5.dp)) {
                        Image(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource("images/material_vectors/arrow_back.svg"),
                            contentDescription = strings.gotoPreviousGame,
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }

                    HorizontalNumberPicker(
                        value = currentGameIndex.inc(),
                        range = 1..games.size,
                        onStateChanged = {
                            currentGameIndex = it.dec()
                        }
                    )

                    Button(::gotoNextGame, modifier = Modifier.padding(5.dp)) {
                        Image(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource("images/material_vectors/arrow_forward.svg"),
                            contentDescription = strings.gotoNextGame,
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                    Button(::gotoLastGame, modifier = Modifier.padding(5.dp)) {
                        Image(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource("images/material_vectors/keyboard_double_arrow_right.svg"),
                            contentDescription = strings.gotoLastGame,
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                }

                Text(
                    "${games.size} ${strings.gamesCount}"
                )

                Row(modifier = Modifier.fillMaxHeight(0.6f).padding(top = 10.dp)) {
                    ChessBoard(
                        piecesValues = positionFenToPiecesArray(currentFen),
                        isWhiteTurn = isWhiteTurnForFen(currentFen),
                        reversed = !isWhiteTurnForFen(currentFen),
                        whitePlayerType = PlayerType.None,
                        blackPlayerType = PlayerType.None,
                        lastMoveArrow = null,
                        pendingPromotion = PendingPromotion.None,
                        pendingPromotionStartFile = null,
                        pendingPromotionStartRank = null,
                        pendingPromotionEndFile = null,
                        pendingPromotionEndRank = null,
                        tryPlayingMove = { _ -> },
                        onCancelPromotion = {},
                        onValidatePromotion = {},
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("$whitePlayer / $blackPlayer")
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(::onValidate) {
                        Text(strings.validate)
                    }
                    Button(::onCancel) {
                        Text(strings.cancel)
                    }
                }
            } else {
                Text(strings.errorImportingSomePgnGames)
            }
        }
    }
}