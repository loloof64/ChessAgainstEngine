package screens

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.arkivanov.composenavigatorexample.navigator.ChildStack
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import i18n.LocalStrings
import kotlinx.coroutines.launch
import logic.ChessGameManager
import logic.KingNotInTurnIsInCheck
import logic.WrongFieldsCountException

const val defaultStartPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun MainContent() {
    val navigation = remember { StackNavigation<Screen>() }
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current

    ChildStack(
        source = navigation,
        initialStack = { listOf(Screen.Home) },
        animation = stackAnimation(fade() + scale()),
    ) { screen ->
        when (screen) {
            is Screen.Home -> HomePage(
                onGoGamePageClick = {
                    try {
                        ChessGameManager.resetGame("8/8/8/8/2k5/2K5/8/8 w - - 0 1")
                        navigation.push(Screen.Game())
                    } catch (ex: WrongFieldsCountException) {
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(
                                message = strings.wrongFieldsCountFen,
                                actionLabel = strings.close,
                                duration = SnackbarDuration.Long,
                            )
                        }
                    } catch (ex: KingNotInTurnIsInCheck) {
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar(
                                message = strings.oppositeKingInCheckFen,
                                actionLabel = strings.close,
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                },
                scaffoldState = scaffoldState
            )

            is Screen.Game -> GamePage(onBack = navigation::pop)
        }
    }
}

sealed class Screen : Parcelable {

    @Parcelize
    object Home : Screen()

    @Parcelize
    data class Game(val startPosition: String = defaultStartPosition) : Screen()
}