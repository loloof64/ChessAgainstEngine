package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import components.ChessBoard
import i18n.LocalStrings

@Composable
fun GamePage(
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(strings.gamePageTitle) },
            navigationIcon = {
                IconButton(onBack) {
                    Icon(Icons.Default.ArrowBack, strings.goBack)
                }
            }
        )
    }) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ChessBoard()
        }
    }
}