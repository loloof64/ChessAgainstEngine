package screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import components.*
import i18n.LocalStrings
import logic.defaultPosition

private fun positionFenToPiecesArray(positionFen: String): List<List<Char>> {
    return positionFen.split(" ")[0].split('/').map { line ->
        line.flatMap { value ->
            if (value.isDigit()) {
                List(value.digitToInt()) { emptyCell }
            } else {
                listOf(value)
            }
        }.toMutableList()
    }.toMutableList()
}

private fun boardPositionFromPiecesValues(piecesValues: List<List<Char>>): String {
    return piecesValues.joinToString(separator = "/") { line ->
        var result = ""
        var groupedHoles = 0

        for (elem in line) {
            if (elem.isDigit()) {
                groupedHoles++
            } else {
                if (groupedHoles > 0) result += groupedHoles.toString()
                groupedHoles = 0
                result += elem
            }
        }
        if (groupedHoles > 0) result += groupedHoles.toString()

        result
    }
}

private fun <T> List<List<T>>.replace(row: Int, col: Int, newValue: T): List<List<T>> {
    return mapIndexed { currRow, line ->
        line.mapIndexed { currCol, elem ->
            if (row == currRow && col == currCol) newValue
            else elem
        }.toList()
    }.toList()
}


@Composable
fun EditPositionPage(
    navigation: StackNavigation<Screen>
) {
    val strings = LocalStrings.current

    val isWhiteTurn by rememberSaveable { mutableStateOf(true) }
    var piecesValues by rememberSaveable { mutableStateOf(positionFenToPiecesArray(defaultPosition)) }
    var currentEditingPiece by rememberSaveable { mutableStateOf(emptyCell) }

    fun onValidate() {
        // TODO set start position and goto game page
    }

    fun onCancel() {
        navigation.pop()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(strings.editPositionPageTitle)
                },
                navigationIcon = {
                    IconButton({ navigation.pop() }) {
                        Icon(Icons.Default.ArrowBack, strings.goBack)
                    }
                },
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        )
        {
            Row(
                modifier = Modifier.fillMaxSize(0.8f).padding(top = 15.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Column(modifier = Modifier.fillMaxWidth(0.4f).fillMaxHeight()) {
                    ChessBoard(
                        piecesValues = piecesValues,
                        whitePlayerType = PlayerType.None,
                        blackPlayerType = PlayerType.None,
                        isWhiteTurn = isWhiteTurn,
                        reversed = false,
                        lastMoveArrow = null,
                        pendingPromotionStartFile = null,
                        pendingPromotionStartRank = null,
                        pendingPromotionEndFile = null,
                        pendingPromotionEndRank = null,
                        tryPlayingMove = { _ -> },
                        pendingPromotion = PendingPromotion.None,
                        onValidatePromotion = { _ -> },
                        onCancelPromotion = { },
                        onCellClick = { file, rank ->
                            piecesValues = piecesValues.replace(7 - rank, file, currentEditingPiece)
                        }
                    )
                }

                PositionEditingControls(
                    currentEditingPiece = currentEditingPiece,
                    onEditingPieceChange = { newPiece ->
                        currentEditingPiece = newPiece
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(30.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(::onValidate) {
                    Text(strings.validate)
                }
                Button(::onCancel) {
                    Text(strings.cancel)
                }
            }
        }
    }
}

@Composable
fun PositionEditingControls(
    modifier: Modifier = Modifier,
    currentEditingPiece: Char,
    onEditingPieceChange: (Char) -> Unit,
) {
    val strings = LocalStrings.current
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf('P', 'N', 'B', 'R', 'Q', 'K').forEach { piece ->
                IconButton({ onEditingPieceChange(piece) }) {
                    Image(
                        painter = painterResource(getVectorForPiece(piece)),
                        contentDescription = getContentDescriptionForPiece(piece, strings)
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf('p', 'n', 'b', 'r', 'q', 'k').forEach { piece ->
                IconButton({ onEditingPieceChange(piece) }) {
                    Image(
                        painter = painterResource(getVectorForPiece(piece)),
                        contentDescription = getContentDescriptionForPiece(piece, strings)
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(strings.selectedPiece)
            if (currentEditingPiece != emptyCell) {
                Image(
                    modifier = Modifier.border(width = 1.dp, color = Color.Black, shape = RectangleShape).size(50.dp),
                    painter = painterResource(getVectorForPiece(currentEditingPiece)),
                    contentDescription = getContentDescriptionForPiece(currentEditingPiece, strings)
                )
            }
            else {
                Row(modifier = Modifier.border(width = 1.dp, color = Color.Black, shape = RectangleShape).size(50.dp)) {

                }
            }
            IconButton({onEditingPieceChange(emptyCell)}) {
                Image(
                    modifier = Modifier.size(45.dp),
                    painter = painterResource("images/material_vectors/delete.svg"),
                    contentDescription = strings.selectEraseCell,
                    colorFilter = ColorFilter.tint(Color.Red)
                )
            }
        }
    }
}