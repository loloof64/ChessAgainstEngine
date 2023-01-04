/*
    Compose chess experiment
    Copyright (C) 2022  Laurent Bernabe

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

 */
package components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.arkivanov.essenty.parcelable.Parcelize
import i18n.LocalStrings

const val emptyCell = ' '

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChessBoard(
    position: String = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
    reversed: Boolean = false,
) {
    val strings = LocalStrings.current
    var dndData by rememberSaveable { mutableStateOf<DragAndDropData?>(null) }

    val positionParts = position.split(' ')
    val lineParts = positionParts[0].split('/')

    val pieces = lineParts.map { line ->
        line.flatMap { value ->
            if (value.isDigit()) {
                List(value.digitToInt()) { emptyCell }
            } else {
                listOf(value)
            }
        }
    }

    val isWhiteTurn = positionParts[1] == "w"
    val bgColor = Color(0xFF9999FF)
    BoxWithConstraints {
        val heightBasedAspectRatio = maxHeight > maxWidth
        val minAvailableSide = if (maxWidth < maxHeight) maxWidth else maxHeight
        val cellSize = minAvailableSide * 0.11f

        val cellSizePx = with(LocalDensity.current) {
            cellSize.toPx()
        }

        Box(
            modifier = Modifier.aspectRatio(1f, heightBasedAspectRatio).background(bgColor)
        ) {
            LowerLayer(cellSize, reversed, pieces, isWhiteTurn, dndData)
            Column(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset: Offset ->
                        val col = ((offset.x - cellSizePx * 0.5) / cellSizePx).toInt()
                        val row = ((offset.y - cellSizePx * 0.5) / cellSizePx).toInt()
                        val file = if (reversed) 7 - col else col
                        val rank = if (reversed) row else 7 - row

                        if (file < 0 || file > 7) return@detectDragGestures
                        if (rank < 0 || rank > 7) return@detectDragGestures

                        val piece = pieces[row][col]
                        if (piece == emptyCell) return@detectDragGestures

                        val isOurPiece = piece.isUpperCase() == isWhiteTurn
                        if (!isOurPiece) return@detectDragGestures

                        val startLocation = Offset(cellSizePx * (col + 0.5f), cellSizePx * (row + 0.5f))

                        dndData = DragAndDropData(
                            startFile = file,
                            startRank = rank,
                            endFile = file,
                            endRank = rank,
                            carriedPiece = piece,
                            startLocation = startLocation,
                            currentLocation = startLocation,
                        )
                    },
                    onDrag = { _, dragAmount ->
                        if (dndData == null) return@detectDragGestures

                        val currentLocation = dndData!!.currentLocation + dragAmount

                        val col = ((currentLocation.x - cellSizePx * 0.5) / cellSizePx).toInt()
                        val row = ((currentLocation.y - cellSizePx * 0.5) / cellSizePx).toInt()
                        val file = if (reversed) 7 - col else col
                        val rank = if (reversed) row else 7 - row

                        dndData = dndData!!.copy(
                            endRank = rank,
                            endFile = file,
                            currentLocation = currentLocation,
                        )
                    },
                    onDragEnd = {
                        if (dndData == null) return@detectDragGestures
                        dndData = null
                    },
                    onDragCancel = {
                        if (dndData == null) return@detectDragGestures
                        dndData = null
                    }
                )
            }) {
                if (dndData != null) {
                    val xDp = with(LocalDensity.current) {
                        dndData!!.currentLocation.x.toDp()
                    }
                    val yDp = with(LocalDensity.current) {
                        dndData!!.currentLocation.y.toDp()
                    }
                    Image(
                        painter = painterResource(getVectorForPiece(dndData!!.carriedPiece)),
                        contentDescription = strings.draggedPiece,
                        modifier = Modifier.fillMaxSize(0.111f).offset(xDp, yDp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LowerLayer(
    cellSize: Dp,
    reversed: Boolean,
    pieces: List<List<Char>>,
    isWhiteTurn: Boolean,
    dndData: DragAndDropData?,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ChessBoardHorizontalLabels(cellSize = cellSize, whiteTurn = null, reversed = reversed)
        (0..7).forEach { rowIndex ->
            val rank = if (reversed) rowIndex + 1 else 8 - rowIndex
            val rankLabel = "${Char('0'.code + rank)}"
            val firstIsWhite = rowIndex % 2 == 0
            val piecesValues = pieces[if (reversed) 7 - rowIndex else rowIndex]
            ChessBoardCellsLine(
                cellSize = cellSize, firstCellWhite = firstIsWhite,
                rankLabel = rankLabel, piecesValues = piecesValues,
                reversed = reversed, dndData = dndData,
                rowIndex = rowIndex,
            )
        }
        ChessBoardHorizontalLabels(cellSize = cellSize, whiteTurn = isWhiteTurn, reversed = reversed)
    }
}

@Composable
private fun ChessBoardCellsLine(
    modifier: Modifier = Modifier,
    cellSize: Dp,
    firstCellWhite: Boolean,
    rankLabel: String,
    piecesValues: List<Char>,
    reversed: Boolean,
    rowIndex: Int,
    dndData: DragAndDropData?,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChessBoardVerticalLabel(text = rankLabel, cellSize = cellSize)
        (0..7).forEach { colIndex ->
            val file = if (reversed) 7-colIndex else colIndex
            val rank = if (reversed) rowIndex else 7-rowIndex

            val isDraggedPieceOrigin = if (dndData != null) {
                file == dndData.startFile && rank == dndData.startRank
            } else false

            ChessBoardCell(
                isWhite = if ((colIndex % 2) == 0) firstCellWhite else !firstCellWhite,
                size = cellSize,
                pieceValue = piecesValues[if (reversed) 7 - colIndex else colIndex],
                isDraggedPieceOrigin = isDraggedPieceOrigin,
            )
        }
        ChessBoardVerticalLabel(text = rankLabel, cellSize = cellSize)
    }
}

@Composable
private fun ChessBoardVerticalLabel(
    modifier: Modifier = Modifier,
    text: String,
    cellSize: Dp
) {
    val fontSize = with(LocalDensity.current) {
        (cellSize * 0.3f).toSp()
    }
    Column(
        modifier = modifier.width(cellSize / 2).height(cellSize / 2).background(Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text, fontWeight = FontWeight.Bold, color = Color.Yellow, fontSize = fontSize)
    }
}

@Composable
private fun ChessBoardHorizontalLabels(
    modifier: Modifier = Modifier, cellSize: Dp, whiteTurn: Boolean?, reversed: Boolean
) {
    val fontSize = with(LocalDensity.current) {
        (cellSize * 0.3f).toSp()
    }
    Row(
        modifier = modifier.fillMaxWidth().height(cellSize / 2)
    ) {
        Row(
            modifier = Modifier.width(cellSize / 2).height(cellSize / 2)
        ) {
            Text(
                text = "", fontWeight = FontWeight.Bold, color = Color.Transparent, fontSize = fontSize
            )
        }
        (0..7).forEach {
            val col = if (reversed) 7 - it else it
            val colLabel = "${Char('A'.code + col)}"
            Row(
                modifier = Modifier.width(cellSize).height(cellSize / 2),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = colLabel,
                    fontWeight = FontWeight.Bold,
                    color = Color.Yellow,
                    fontSize = fontSize,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (whiteTurn == null) {
            Row(
                modifier = Modifier.width(cellSize / 2).height(cellSize / 2)
            ) {
                Text(
                    text = "", fontWeight = FontWeight.Bold, color = Color.Transparent, fontSize = fontSize
                )
            }
        } else {
            val color = if (whiteTurn) Color.White else Color.Black
            Column(
                modifier = Modifier.width(cellSize / 2).height(cellSize / 2).clip(CircleShape).background(color)
            ) {

            }
        }
    }
}

@Composable
private fun ChessBoardCell(
    modifier: Modifier = Modifier,
    isWhite: Boolean,
    size: Dp,
    pieceValue: Char,
    isDraggedPieceOrigin: Boolean,
) {
    val strings = LocalStrings.current
    val bgColor = if (isWhite) Color(0xFFFFDEAD) else Color(0xFFCD853F)
    Surface(modifier = modifier.size(size)) {
        Column(modifier = Modifier.background(bgColor)) {
            val noPiece = pieceValue == emptyCell
            if (!noPiece && !isDraggedPieceOrigin) {
                Image(
                    painter = painterResource(getVectorForPiece(pieceValue)),
                    contentDescription = strings.chessPiece,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

fun getVectorForPiece(pieceValue: Char): String {
    val name = when (pieceValue) {
        'P' -> "Chess_plt45.svg"
        'N' -> "Chess_nlt45.svg"
        'B' -> "Chess_blt45.svg"
        'R' -> "Chess_rlt45.svg"
        'Q' -> "Chess_qlt45.svg"
        'K' -> "Chess_klt45.svg"

        'p' -> "Chess_pdt45.svg"
        'n' -> "Chess_ndt45.svg"
        'b' -> "Chess_bdt45.svg"
        'r' -> "Chess_rdt45.svg"
        'q' -> "Chess_qdt45.svg"
        'k' -> "Chess_kdt45.svg"
        else -> throw IllegalArgumentException("Not recognized piece $pieceValue")
    }
    return "chess_vectors/$name"
}

@Parcelize
data class DragAndDropData(
    val startFile: Int,
    val startRank: Int,
    var endFile: Int,
    var endRank: Int,
    val startLocation: Offset,
    var currentLocation: Offset,
    val carriedPiece: Char,
)