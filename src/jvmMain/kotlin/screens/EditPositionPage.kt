package screens

import NumberPicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

const val noEnPassant = "-"

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

    var boardReversed by rememberSaveable { mutableStateOf(false) }
    val isWhiteTurn by rememberSaveable { mutableStateOf(true) }
    var piecesValues by rememberSaveable { mutableStateOf(positionFenToPiecesArray(defaultPosition)) }
    var currentEditingPiece by rememberSaveable { mutableStateOf(emptyCell) }
    var whiteOO by rememberSaveable { mutableStateOf(true) }
    var whiteOOO by rememberSaveable { mutableStateOf(true) }
    var blackOO by rememberSaveable { mutableStateOf(true) }
    var blackOOO by rememberSaveable { mutableStateOf(true) }
    var enPassantFile by rememberSaveable { mutableStateOf<Int?>(null) }
    var drawHalfMovesCount by rememberSaveable { mutableStateOf(0) }
    var moveNumber by rememberSaveable{ mutableStateOf(1) }

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
                IconButton(
                    modifier = Modifier.padding(end = 5.dp),
                    content = {
                        Image(
                            painter = painterResource("icons/swap_vert.svg"),
                            contentDescription = strings.swapBoardOrientation,
                            modifier = Modifier.size(50.dp)
                                .border(width = 2.dp, shape = CircleShape, color = Color.Blue),
                            colorFilter = ColorFilter.tint(Color.Blue)
                        )
                    }, onClick = {
                        boardReversed = !boardReversed
                    })
                Column(modifier = Modifier.fillMaxWidth(0.4f).fillMaxHeight()) {
                    ChessBoard(
                        piecesValues = piecesValues,
                        whitePlayerType = PlayerType.None,
                        blackPlayerType = PlayerType.None,
                        isWhiteTurn = isWhiteTurn,
                        reversed = boardReversed,
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
                    hasWhiteOO = whiteOO,
                    hasWhiteOOO = whiteOOO,
                    hasBlackOO = blackOO,
                    hasBlackOOO = blackOOO,
                    selectedEnPassantFile = enPassantFile,
                    drawHalfMovesCount = drawHalfMovesCount,
                    moveNumber = moveNumber,
                    onEditingPieceChange = { newPiece ->
                        currentEditingPiece = newPiece
                    },
                    onWhiteOOChange = {
                        whiteOO = it
                    },
                    onWhiteOOOChange = {
                        whiteOOO = it
                    },
                    onBlackOOChange = {
                        blackOO = it
                    },
                    onBlackOOOChange = {
                        blackOOO = it
                    },
                    onEnPassantSelection = {
                        enPassantFile = it
                    },
                    onDrawHalfMovesCountChange = {
                        drawHalfMovesCount = it
                    },
                    onMoveNumberChange = {
                        moveNumber = it
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
    hasWhiteOO: Boolean,
    hasWhiteOOO: Boolean,
    hasBlackOO: Boolean,
    hasBlackOOO: Boolean,
    selectedEnPassantFile: Int?,
    drawHalfMovesCount: Int,
    moveNumber: Int,
    onEditingPieceChange: (Char) -> Unit,
    onWhiteOOChange: (Boolean) -> Unit,
    onWhiteOOOChange: (Boolean) -> Unit,
    onBlackOOChange: (Boolean) -> Unit,
    onBlackOOOChange: (Boolean) -> Unit,
    onEnPassantSelection: (Int?) -> Unit,
    onDrawHalfMovesCountChange: (Int) -> Unit,
    onMoveNumberChange: (Int) -> Unit,
) {
    val strings = LocalStrings.current
    var enPassantMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val enPassantValues = listOf(noEnPassant, "a", "b", "c", "d", "e", "f", "g", "h")
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
            } else {
                Row(modifier = Modifier.border(width = 1.dp, color = Color.Black, shape = RectangleShape).size(50.dp)) {

                }
            }
            IconButton({ onEditingPieceChange(emptyCell) }) {
                Image(
                    modifier = Modifier.size(45.dp),
                    painter = painterResource("images/material_vectors/delete.svg"),
                    contentDescription = strings.selectEraseCell,
                    colorFilter = ColorFilter.tint(Color.Red)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = hasWhiteOO,
                onCheckedChange = onWhiteOOChange,
            )
            Text(
                text = strings.whiteOO
            )

            Checkbox(
                checked = hasWhiteOOO,
                onCheckedChange = onWhiteOOOChange,
            )
            Text(
                text = strings.whiteOOO
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = hasBlackOO,
                onCheckedChange = onBlackOOChange,
            )
            Text(
                text = strings.blackOO
            )

            Checkbox(
                checked = hasBlackOOO,
                onCheckedChange = onBlackOOOChange,
            )
            Text(
                text = strings.blackOOO
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(strings.enPassantFile)
            Text(
                text = "[${enPassantValues[if (selectedEnPassantFile == null) 0 else selectedEnPassantFile + 1]}]",
                modifier = Modifier.padding(horizontal = 5.dp)
            )
            Button(onClick = { enPassantMenuExpanded = true }, modifier = Modifier.padding(horizontal = 5.dp)) {
                Text(strings.select)
            }
            DropdownMenu(
                expanded = enPassantMenuExpanded,
                onDismissRequest = { enPassantMenuExpanded = false },
            ) {
                enPassantValues.zip(listOf(null, 0, 1, 2, 3, 4, 5, 6, 7))
                    .forEach { (caption, value) ->
                        DropdownMenuItem({
                            enPassantMenuExpanded = false
                            onEnPassantSelection(value)
                        }) {
                            Text(caption)
                        }
                    }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(strings.drawHalfMovesCount)
            NumberPicker(
                modifier = Modifier.padding(horizontal = 5.dp),
                value = drawHalfMovesCount,
                onStateChanged = onDrawHalfMovesCountChange,
                range = 0..50,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(strings.moveNumber)
            NumberPicker(
                modifier = Modifier.padding(horizontal = 5.dp),
                value = moveNumber,
                onStateChanged = onMoveNumberChange,
                range = 1..Int.MAX_VALUE,
            )
        }
    }
}
