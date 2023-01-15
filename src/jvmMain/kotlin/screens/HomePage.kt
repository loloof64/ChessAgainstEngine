package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.push
import i18n.LocalStrings
import kotlinx.coroutines.launch
import logic.*

@Composable
fun HomePage(
    navigation: StackNavigation<Screen>,
    scaffoldState: ScaffoldState,
) {
    val coroutineScope = rememberCoroutineScope()
    val strings = LocalStrings.current

    if (!UciEngineChannel.isProcessStarted()) {
        coroutineScope.launch {
            UciEngineChannel.tryStartingEngineProcess()
        }
    }

    fun onGoEditPositionPageClick() {
        navigation.push(Screen.EditPosition)
    }

    fun onGoOptionsPageClick() {
        navigation.push(Screen.Options)
    }

    fun onGoGamePageClick() {
        try {
            ChessGameManager.setStartPosition(defaultPosition)
            ChessGameManager.resetGame()
            navigation.push(Screen.Game)
        } catch (ex: WrongFieldsCountException) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = strings.wrongFieldsCountFen,
                    actionLabel = strings.close,
                    duration = SnackbarDuration.Long,
                )
            }
        } catch (ex: KingNotInTurnIsInCheck) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = strings.oppositeKingInCheckFen,
                    actionLabel = strings.close,
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(strings.homePageTitle) },
            actions = {
                IconButton(::onGoOptionsPageClick) {
                    Icon(Icons.Default.Settings, strings.preferences)
                }
            }
        )
    }, scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(::onGoGamePageClick) {
                Text(strings.playDirectly)
            }

            Button(::onGoEditPositionPageClick) {
                Text(strings.editStartPosition)
            }
        }
    }
}