package components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment

enum class GameTermination {
    InProgress,
    WhiteWin,
    BlackWin,
    Draw
}

sealed class ChessHistoryItem {
    data class MoveNumberItem(val number: Int, val isWhiteTurn: Boolean) : ChessHistoryItem()
    data class GameTerminationItem(val termination: GameTermination) : ChessHistoryItem()
    data class MoveItem(val san: String, val positionFen: String) : ChessHistoryItem()
}

@Composable
fun ChessHistory(
    modifier: Modifier = Modifier, items: List<ChessHistoryItem>,
    onPositionRequest: (String) -> Unit
) {
    FlowRow(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x88FFCC00)),
        mainAxisAlignment = MainAxisAlignment.Start,
        crossAxisAlignment = FlowCrossAxisAlignment.Start,
        mainAxisSpacing = 10.dp,
    ) {
        items.map { item ->
            when (item) {
                is ChessHistoryItem.MoveNumberItem -> Text(text = "${item.number}.${if (item.isWhiteTurn) "" else ".."}")
                is ChessHistoryItem.GameTerminationItem -> when (item.termination) {
                    GameTermination.InProgress -> '*'
                    GameTermination.WhiteWin -> "1-0"
                    GameTermination.BlackWin -> "0-1"
                    GameTermination.Draw -> "1/2-1/2"
                }

                is ChessHistoryItem.MoveItem -> ClickableText(
                    text = AnnotatedString(text = item.san),
                    onClick = {
                        onPositionRequest(item.positionFen)
                    },
                )
            }
        }
    }
}