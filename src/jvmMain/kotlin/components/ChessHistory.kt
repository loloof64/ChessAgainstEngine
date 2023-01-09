package components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import logic.toFAN

enum class GameTermination {
    InProgress,
    WhiteWin,
    BlackWin,
    Draw
}

fun GameTermination.toText(): String = when (this) {
    GameTermination.InProgress -> "*"
    GameTermination.WhiteWin -> "1-0"
    GameTermination.BlackWin -> "0-1"
    GameTermination.Draw -> "1/2-1/2"
}

sealed class ChessHistoryItem {
    data class MoveNumberItem(val number: Int, val isWhiteTurn: Boolean) : ChessHistoryItem()
    data class GameTerminationItem(val termination: GameTermination) : ChessHistoryItem()
    data class MoveItem(val san: String, val positionFen: String, val isWhiteMove: Boolean) : ChessHistoryItem()
}

const val minFontSizePx = 10f
const val maxFontSizePx = 30f

@Composable
fun ChessHistory(
    modifier: Modifier = Modifier, items: List<ChessHistoryItem>,
    onPositionRequest: (String) -> Unit
) {
    BoxWithConstraints {
        val fontSize = with(LocalDensity.current) {
            var size = maxWidth * 0.1f
            if (size.toPx() > maxFontSizePx) {
                size = maxFontSizePx.toDp()
            }
            if (size.toPx() < minFontSizePx) {
                size = minFontSizePx.toDp()
            }
            size.toSp()
        }
        val textStyle = TextStyle(
            fontSize = fontSize,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily(
                Font(
                    resource = "fonts/FreeSerif.ttf",
                    weight = FontWeight.Normal,
                    style = FontStyle.Normal,
                ),
            )
        )
        FlowRow(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0x88FFCC00))
                .padding(6.dp),
            mainAxisAlignment = MainAxisAlignment.Start,
            crossAxisAlignment = FlowCrossAxisAlignment.Start,
            mainAxisSpacing = 10.dp,
        ) {
            items.map { item ->
                when (item) {
                    is ChessHistoryItem.MoveNumberItem -> Text(
                        style = textStyle,
                        text = "${item.number}.${if (item.isWhiteTurn) "" else ".."}"
                    )

                    is ChessHistoryItem.GameTerminationItem -> {
                        val text = item.termination.toText()
                        Text(
                            style = textStyle,
                            text = text,
                        )
                    }

                    is ChessHistoryItem.MoveItem -> ClickableText(
                        text = AnnotatedString(text = item.san.toFAN(forBlackTurn = !item.isWhiteMove)),
                        style = textStyle,
                        onClick = {
                            onPositionRequest(item.positionFen)
                        },
                    )
                }
            }
        }
    }
}