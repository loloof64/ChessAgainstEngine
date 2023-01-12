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
import i18n.LocalStrings
import kotlinx.coroutines.launch
import logic.UciEngineChannel

@Composable
fun HomePage(
    onGoGamePageClick: () -> Unit,
    onGoOptionsPageClick: () -> Unit,
    scaffoldState: ScaffoldState,
) {
    val coroutineScope = rememberCoroutineScope()

    if (!UciEngineChannel.isProcessStarted()) {
        coroutineScope.launch {
            UciEngineChannel.tryStartingEngineProcess()
            UciEngineChannel.sendCommand("uci")
            UciEngineChannel.sendCommand("isready")
        }
    }

    val strings = LocalStrings.current
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(strings.homePageTitle) },
            actions = {
                IconButton(
                    onGoOptionsPageClick
                ) {
                    Icon(Icons.Default.Settings, strings.preferences)
                }
            }
        )
    }, scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onGoGamePageClick) {
                Text(strings.goToGamePage)
            }
        }
    }
}