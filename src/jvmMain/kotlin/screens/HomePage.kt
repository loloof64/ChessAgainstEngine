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
import java.awt.KeyboardFocusManager
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

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
        } catch (ex: IllegalArgumentException) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    message = strings.wrongFEN,
                    actionLabel = strings.close,
                    duration = SnackbarDuration.Long,
                )
            }
        }
    }

    fun onLoadPgnClick() {
        val folder = PreferencesManager.loadLoadPgnFolder()
        val fileChooser = if (folder.isNotEmpty()) JFileChooser(folder) else JFileChooser()
        fileChooser.dialogTitle = strings.selectEnginePathDialogTitle
        fileChooser.approveButtonText = strings.validate

        val pgnFilter = FileNameExtensionFilter(strings.pgnFileType, "pgn")
        val currentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow
        fileChooser.addChoosableFileFilter(pgnFilter)
        fileChooser.isAcceptAllFileFilterUsed = true
        val actionResult = fileChooser.showOpenDialog(currentWindow)
        if (actionResult == JFileChooser.APPROVE_OPTION) {
            PreferencesManager.saveLoadPgnFolder(fileChooser.currentDirectory.absolutePath)
            navigation.push(Screen.PgnGames(selectedPath = fileChooser.selectedFile.absolutePath))
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

            Button(::onLoadPgnClick) {
                Text(strings.playFromPgnFile)
            }
        }
    }
}