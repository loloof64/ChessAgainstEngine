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
import components.PendingPromotion
import i18n.LocalStrings
import logic.ChessGameManager

@Composable
fun GamePage(
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    var boardReversed by rememberSaveable { mutableStateOf(false) }
    var boardPieces by rememberSaveable { mutableStateOf(ChessGameManager.getPieces()) }
    var isWhiteTurn by rememberSaveable { mutableStateOf(ChessGameManager.isWhiteTurn()) }
    var pendingPromotion by rememberSaveable { mutableStateOf(PendingPromotion.None) }
    var pendingPromotionStartSquare by rememberSaveable { mutableStateOf(ChessGameManager.getPendingPromotionStartSquare()) }
    var pendingPromotionEndSquare by rememberSaveable { mutableStateOf(ChessGameManager.getPendingPromotionEndSquare()) }

    Scaffold(topBar = {
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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ChessBoard(isWhiteTurn = isWhiteTurn,
                piecesValues = boardPieces,
                reversed = boardReversed,
                pendingPromotion = pendingPromotion,
                pendingPromotionStartFile = pendingPromotionStartSquare?.x,
                pendingPromotionStartRank = pendingPromotionStartSquare?.y,
                pendingPromotionEndFile = pendingPromotionEndSquare?.x,
                pendingPromotionEndRank = pendingPromotionEndSquare?.y,
                tryPlayingMove = { dragAndDropData ->
                    ChessGameManager.playMove(
                        startFile = dragAndDropData.startFile,
                        startRank = dragAndDropData.startRank,
                        endFile = dragAndDropData.endFile,
                        endRank = dragAndDropData.endRank,
                    )
                    isWhiteTurn = ChessGameManager.isWhiteTurn()
                    boardPieces = ChessGameManager.getPieces()
                    pendingPromotion = ChessGameManager.getPendingPromotion()
                    pendingPromotionStartSquare = ChessGameManager.getPendingPromotionStartSquare()
                    pendingPromotionEndSquare = ChessGameManager.getPendingPromotionEndSquare()
                },
                onCancelPromotion = {
                    ChessGameManager.cancelPromotion()
                    pendingPromotion = ChessGameManager.getPendingPromotion()
                    pendingPromotionStartSquare = ChessGameManager.getPendingPromotionStartSquare()
                    pendingPromotionEndSquare = ChessGameManager.getPendingPromotionEndSquare()
                },
                onValidatePromotion = {
                    ChessGameManager.commitPromotion(it)
                    isWhiteTurn = ChessGameManager.isWhiteTurn()
                    boardPieces = ChessGameManager.getPieces()
                    pendingPromotion = ChessGameManager.getPendingPromotion()
                    pendingPromotionStartSquare = ChessGameManager.getPendingPromotionStartSquare()
                    pendingPromotionEndSquare = ChessGameManager.getPendingPromotionEndSquare()
                }
            )
        }
    }
}