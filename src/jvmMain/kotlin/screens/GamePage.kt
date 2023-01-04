package screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import components.ChessBoard
import i18n.LocalStrings

@Composable
fun GamePage(
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    var boardReversed by rememberSaveable{ mutableStateOf(false) }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(strings.gamePageTitle) },
            navigationIcon = {
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
                },
                    onClick = {
                        boardReversed = !boardReversed
                    })
            })
    }) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ChessBoard(
                position = "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2",
                reversed = boardReversed
            )
        }
    }
}