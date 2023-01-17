package logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import chesspresso.Chess
import chesspresso.game.Game
import chesspresso.game.GameModel
import chesspresso.move.IllegalMoveException
import chesspresso.move.Move
import chesspresso.pgn.PGNWriter
import chesspresso.position.FEN
import chesspresso.position.Position
import components.*
import java.io.OutputStream
import java.io.PrintWriter

const val defaultPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
const val emptyPosition = "8/8/8/8/8/8/8/8 w - - 0 1"

fun String.toLastMoveArrow(): LastMoveArrow {
    if (length < 4) throw IllegalMoveException("Not a uci chess move string : $this.")

    val startFile = this[0].code - 'a'.code
    val startRank = this[1].code - '1'.code

    val endFile = this[2].code - 'a'.code
    val endRank = this[3].code - '1'.code

    return LastMoveArrow(
        startFile = startFile,
        startRank = startRank,
        endFile = endFile,
        endRank = endRank
    )
}

fun LastMoveArrow.toMoveCoordinates(): MoveCoordinates {
    return MoveCoordinates(
        startFile = startFile,
        startRank = startRank,
        endFile = endFile,
        endRank = endRank
    )
}

fun Position.playMove(uciMove: String) {
    val startSquareIndex = Chess.strToSqi(uciMove.substring(0..1))
    val endSquareIndex = Chess.strToSqi(uciMove.substring(2..3))
    val promotionPiece = if (uciMove.length >= 5) Chess.charToPiece(uciMove[4]).toShort() else Chess.NO_PIECE

    val matchingMove = getMove(startSquareIndex, endSquareIndex, promotionPiece.toInt())
    if (matchingMove != Move.ILLEGAL_MOVE) {
        doMove(matchingMove)
    }
}

fun Position.getLegalMove(startSquareIndex: Int, endSquareIndex: Int, promotionPiece: Short) : Short? {
    val legalMoves = allMoves
    for (testedMove in legalMoves) {
        val testedMoveStartSquareIndex = Move.getFromSqi(testedMove)
        val testedMoveEndSquareIndex = Move.getToSqi(testedMove)
        val testedMovePromotionPiece = Move.getPromotionPiece(testedMove)

        val isMatchingMove = (startSquareIndex == testedMoveStartSquareIndex)
                && (endSquareIndex == testedMoveEndSquareIndex)
                && (promotionPiece.toInt() == testedMovePromotionPiece)
        if (isMatchingMove) {
            return testedMove
        }
    }
    return null
}

fun Position.countPiece(type: Short): Int {
    var count = 0
    for (sqi in 0 until Chess.NUM_OF_SQUARES) {
        if (getPiece(sqi) == type.toInt()) {
            count++
        }
    }
    return count
}

fun Position.firstSquareColorIsWhiteForPiece(type: Short): Boolean {
    for (sqi in 0 until Chess.NUM_OF_SQUARES) {
        if (getPiece(sqi) == type.toInt()) {
            return Chess.isWhiteSquare(sqi)
        }
    }
    throw IllegalArgumentException("Piece not found ($type) in position ${FEN.getFEN(this)}.")
}

fun Position.isMissingMatingMaterial(): Boolean {
    val whiteQueensCount = countPiece(Chess.WHITE_QUEEN)
    val blackQueensCount = countPiece(Chess.BLACK_QUEEN)
    val whitePawnCount = countPiece(Chess.WHITE_PAWN)
    val blackPawnCount = countPiece(Chess.BLACK_PAWN)
    val whiteRookCount = countPiece(Chess.WHITE_ROOK)
    val blackRookCount = countPiece(Chess.BLACK_ROOK)

    val whiteKnightCount = countPiece(Chess.WHITE_KNIGHT)
    val blackKnightCount = countPiece(Chess.BLACK_KNIGHT)
    val whiteBishopCount = countPiece(Chess.WHITE_BISHOP)
    val blackBishopCount = countPiece(Chess.BLACK_BISHOP)

    if (whiteQueensCount > 0) return false
    if (blackQueensCount > 0) return false
    if (whiteRookCount > 0) return false
    if (blackRookCount > 0) return false
    if (whitePawnCount > 0) return false
    if (blackPawnCount > 0) return false

    val whiteKnightsAndBishops = whiteBishopCount + whiteKnightCount
    val blackKnightsAndBishops = blackBishopCount + blackKnightCount

    if (whiteKnightsAndBishops <= 2 && blackKnightsAndBishops <= 2) {
        // king against king,
        // king against king and bishop,
        // king against king and knight
        if ((whiteKnightsAndBishops == 0 && blackKnightsAndBishops <= 1)
            || (whiteKnightsAndBishops <= 1 && blackKnightsAndBishops == 0)
        ) {
            return true
        } else if (whiteBishopCount == 1 && blackBishopCount == 1) {
            // king and bishop against king and bishop, with both bishops on squares of the same color
            val whiteBishopSquareWhite = firstSquareColorIsWhiteForPiece(Chess.WHITE_BISHOP)
            val blackBishopSquareWhite = firstSquareColorIsWhiteForPiece(Chess.BLACK_BISHOP)
            return whiteBishopSquareWhite == blackBishopSquareWhite
        }
    }
    return false
}

object ChessGameManager {
    private var _gameInProgress by mutableStateOf(false)
    private var _gameLogic by mutableStateOf(Game(GameModel()))
    private var _pendingPromotion by mutableStateOf(PendingPromotion.None)
    private var _pendingPromotionStartSquare by mutableStateOf<Short?>(null)
    private var _pendingPromotionEndSquare by mutableStateOf<Short?>(null)
    private var _lastMoveArrow by mutableStateOf<LastMoveArrow?>(null)
    private var _historyElements by mutableStateOf<MutableList<ChessHistoryItem>>(mutableListOf())
    private var _isFirstHistoryNode by mutableStateOf(false)
    private var _positionFenBeforeLastMove by mutableStateOf<String?>(null)
    private var _selectedNodeIndex by mutableStateOf<Int?>(null)
    private var _startPosition by mutableStateOf(defaultPosition)
    private var _whitePlayerType by mutableStateOf(PlayerType.Human)
    private var _blackPlayerType by mutableStateOf(PlayerType.Human)
    private var _occurrences by mutableStateOf(mutableMapOf<String, Int>())

    fun processEngineMove(
        uciMove: String,
        onCheckmate: (Boolean) -> Unit,
        onStalemate: () -> Unit,
        onThreeFoldsRepetition: () -> Unit,
        onInsufficientMaterial: () -> Unit,
        onFiftyMovesRuleDraw: () -> Unit
    ) {
        try {
            _positionFenBeforeLastMove = FEN.getFEN(_gameLogic.position)
            _gameLogic.position.playMove(uciMove)
            _lastMoveArrow = uciMove.toLastMoveArrow()
            addMoveToHistory(_lastMoveArrow!!.toMoveCoordinates())
            _occurrences[FEN.getFEN(_gameLogic.position)] =
                if (_occurrences[FEN.getFEN(_gameLogic.position)] == null) 1 else _occurrences[FEN.getFEN(_gameLogic.position)]!!.inc()
            handleGameEndingStatus(
                onCheckmate = onCheckmate,
                onStalemate = onStalemate,
                onThreeFoldsRepetition = onThreeFoldsRepetition,
                onInsufficientMaterial = onInsufficientMaterial,
                onFiftyMovesRuleDraw = onFiftyMovesRuleDraw,
            )
        } catch (ex: IllegalMoveException) {
            println("Illegal move from engine : $uciMove")
            throw ex
        }
    }

    fun getCurrentPosition(): String = FEN.getFEN(_gameLogic.position)

    fun getPieces(): List<List<Char>> {
        val positionFen = FEN.getFEN(_gameLogic.position)
        val lineParts = positionFen.split(" ")[0].split('/')

        return lineParts.map { line ->
            line.flatMap { value ->
                if (value.isDigit()) {
                    List(value.digitToInt()) { emptyCell }
                } else {
                    listOf(value)
                }
            }
        }
    }

    fun setWhitePlayerType(playerType: PlayerType) {
        _whitePlayerType = playerType
    }

    fun setBlackPlayerType(playerType: PlayerType) {
        _blackPlayerType = playerType
    }

    fun getWhitePlayerType(): PlayerType = _whitePlayerType

    fun getBlackPlayerType(): PlayerType = _blackPlayerType

    fun getHistoryElements(): List<ChessHistoryItem> = _historyElements

    fun getLastMoveArrow(): LastMoveArrow? = _lastMoveArrow

    fun isGameInProgress(): Boolean = _gameInProgress

    fun getPendingPromotion(): PendingPromotion = _pendingPromotion

    fun getPendingPromotionStartSquare(): Short? = _pendingPromotionStartSquare

    fun getPendingPromotionEndSquare(): Short? = _pendingPromotionEndSquare

    fun getSelectedHistoryNodeIndex(): Int? = _selectedNodeIndex

    fun isWhiteTurn(): Boolean {
        val positionFen = FEN.getFEN(_gameLogic.position)
        return positionFen.split(" ")[1] == "w"
    }

    fun setStartPosition(startPosition: String) {
        try {
            val testGame = Game(GameModel())
            FEN.initFromFEN(testGame.position, startPosition, true)
            _startPosition = startPosition
        } catch (ex: IllegalArgumentException) {
            println(ex)
        }
    }

    fun stopGame() {
        _gameInProgress = false
        _whitePlayerType = PlayerType.None
        _blackPlayerType = PlayerType.None
        selectLastHistoryMoveNodeIfAny()
    }

    fun writePGNTo(outStream: OutputStream) {
        val writer = PGNWriter(PrintWriter(outStream))
        writer.write(_gameLogic.model)
    }

    fun resetGame() {
        loadPosition(_startPosition)
        val isWhiteTurn = _gameLogic.position.toPlay == Chess.WHITE
        val moveNumber = Chess.plyToMoveNumber(_gameLogic.position.plyNumber)
        _occurrences = mutableMapOf()
        _occurrences[_startPosition] = 1
        _whitePlayerType = PlayerType.Human
        _blackPlayerType = PlayerType.Human
        _historyElements = mutableListOf()
        _historyElements.add(ChessHistoryItem.MoveNumberItem(moveNumber, isWhiteTurn))
        _pendingPromotion = PendingPromotion.None
        _pendingPromotionStartSquare = null
        _pendingPromotionEndSquare = null
        _lastMoveArrow = null
        _isFirstHistoryNode = true
        _positionFenBeforeLastMove = null
        _selectedNodeIndex = null
        _gameInProgress = true
    }

    fun playMove(
        startFile: Int, startRank: Int,
        endFile: Int, endRank: Int,
        onCheckmate: (Boolean) -> Unit,
        onStalemate: () -> Unit,
        onThreeFoldsRepetition: () -> Unit,
        onInsufficientMaterial: () -> Unit,
        onFiftyMovesRuleDraw: () -> Unit
    ) {
        val startSquareIndex = Chess.coorToSqi(startFile, startRank)
        val endSquareIndex = Chess.coorToSqi(endFile, endRank)

        val moveWithoutPromotion = _gameLogic.position.getLegalMove(startSquareIndex, endSquareIndex, Chess.NO_PIECE)
        val moveWithPromotion = _gameLogic.position.getLegalMove(startSquareIndex, endSquareIndex, Chess.QUEEN)

        if (moveWithoutPromotion != null) {
            _positionFenBeforeLastMove = FEN.getFEN(_gameLogic.position)
            _gameLogic.position.doMove(moveWithoutPromotion)
            addMoveToHistory(
                MoveCoordinates(
                    startFile = startFile,
                    startRank = startRank,
                    endFile = endFile,
                    endRank = endRank
                )
            )
            _lastMoveArrow = LastMoveArrow(
                startFile = startFile,
                startRank = startRank,
                endFile = endFile,
                endRank = endRank
            )
            _occurrences[FEN.getFEN(_gameLogic.position)] =
                if (_occurrences[FEN.getFEN(_gameLogic.position)] == null) 1 else _occurrences[FEN.getFEN(_gameLogic.position)]!!.inc()
            handleGameEndingStatus(
                onCheckmate = onCheckmate,
                onStalemate = onStalemate,
                onThreeFoldsRepetition = onThreeFoldsRepetition,
                onInsufficientMaterial = onInsufficientMaterial,
                onFiftyMovesRuleDraw = onFiftyMovesRuleDraw,
            )
        } else {
            if (moveWithPromotion != null) {
                _pendingPromotion = if (isWhiteTurn()) PendingPromotion.White else PendingPromotion.Black
                _pendingPromotionStartSquare = startSquareIndex.toShort()
                _pendingPromotionEndSquare = endSquareIndex.toShort()
            }
        }
    }

    fun cancelPromotion() {
        _pendingPromotion = PendingPromotion.None
        _pendingPromotionStartSquare = null
        _pendingPromotionEndSquare = null
    }

    fun commitPromotion(
        pieceType: PromotionType,
        onCheckmate: (Boolean) -> Unit,
        onStalemate: () -> Unit,
        onThreeFoldsRepetition: () -> Unit,
        onInsufficientMaterial: () -> Unit,
        onFiftyMovesRuleDraw: () -> Unit
    ) {
        if (_pendingPromotion == PendingPromotion.None) return
        val promotionPiece = when (pieceType) {
            PromotionType.Queen -> Chess.QUEEN
            PromotionType.Rook -> Chess.ROOK
            PromotionType.Bishop -> Chess.BISHOP
            PromotionType.Knight -> Chess.KNIGHT
        }
        val matchingMove = _gameLogic.position.getLegalMove(
            _pendingPromotionStartSquare!!.toInt(),
            _pendingPromotionEndSquare!!.toInt(),
            promotionPiece
        )
        if (matchingMove != null) {
            _positionFenBeforeLastMove = FEN.getFEN(_gameLogic.position)
            _gameLogic.position.doMove(matchingMove)
            addMoveToHistory(
                MoveCoordinates(
                    startFile = Chess.sqiToCol(_pendingPromotionStartSquare!!.toInt()),
                    startRank = Chess.sqiToRow(_pendingPromotionStartSquare!!.toInt()),
                    endFile = Chess.sqiToCol(_pendingPromotionEndSquare!!.toInt()),
                    endRank = Chess.sqiToRow(_pendingPromotionEndSquare!!.toInt())
                )
            )

            _lastMoveArrow = LastMoveArrow(
                startFile = Chess.sqiToCol(_pendingPromotionStartSquare!!.toInt()),
                startRank = Chess.sqiToRow(_pendingPromotionStartSquare!!.toInt()),
                endFile = Chess.sqiToCol(_pendingPromotionEndSquare!!.toInt()),
                endRank = Chess.sqiToRow(_pendingPromotionEndSquare!!.toInt())
            )

            _pendingPromotion = PendingPromotion.None
            _pendingPromotionStartSquare = null
            _pendingPromotionEndSquare = null

            _occurrences[FEN.getFEN(_gameLogic.position)] =
                if (_occurrences[FEN.getFEN(_gameLogic.position)] == null) 1 else _occurrences[FEN.getFEN(_gameLogic.position)]!!.inc()


            handleGameEndingStatus(
                onCheckmate = onCheckmate,
                onStalemate = onStalemate,
                onThreeFoldsRepetition = onThreeFoldsRepetition,
                onInsufficientMaterial = onInsufficientMaterial,
                onFiftyMovesRuleDraw = onFiftyMovesRuleDraw,
            )
        }
    }

    fun requestPosition(positionFen: String, moveCoordinates: MoveCoordinates, nodeToSelectIndex: Int): Boolean {
        if (_gameInProgress) return false
        loadPosition(positionFen)
        _lastMoveArrow = LastMoveArrow(
            startFile = moveCoordinates.startFile,
            startRank = moveCoordinates.startRank,
            endFile = moveCoordinates.endFile,
            endRank = moveCoordinates.endRank
        )
        _selectedNodeIndex = nodeToSelectIndex
        return true
    }

    private fun loadPosition(positionFen: String) {
        _gameLogic = Game(GameModel())
        FEN.initFromFEN(_gameLogic.position, positionFen, true)
    }

    fun requestGotoPreviousHistoryNode(): Boolean {
        if (_gameInProgress) return false
        if (_selectedNodeIndex == null) return false
        return if (requestBackOneMove()) {
            val currentHistoryNode = _historyElements[_selectedNodeIndex!!] as ChessHistoryItem.MoveItem
            loadPosition(currentHistoryNode.positionFen)
            _lastMoveArrow = LastMoveArrow(
                startFile = currentHistoryNode.movesCoordinates.startFile,
                startRank = currentHistoryNode.movesCoordinates.startRank,
                endFile = currentHistoryNode.movesCoordinates.endFile,
                endRank = currentHistoryNode.movesCoordinates.endRank,
            )
            true
        } else {
            loadPosition(_startPosition)
            _selectedNodeIndex = null
            _lastMoveArrow = null
            true
        }
    }

    fun requestGotoNextHistoryNode(): Boolean {
        if (_gameInProgress) return false
        return requestForwardOneMove()
    }

    fun requestGotoFirstPosition(): Boolean {
        if (_gameInProgress) return false
        while (requestGotoPreviousHistoryNode());
        return true
    }

    fun requestGotoLastHistoryNode(): Boolean {
        if (_gameInProgress) return false
        while (requestGotoNextHistoryNode());
        return true
    }

    private fun requestForwardOneMove(): Boolean {
        if (_gameInProgress) return false
        if (_selectedNodeIndex != null && _selectedNodeIndex!! >= _historyElements.size - 1) return false
        var newSelectedNodeIndex = if (_selectedNodeIndex == null) {
            0
        } else (_selectedNodeIndex!! + 1)
        while ((newSelectedNodeIndex < _historyElements.size - 1) && (_historyElements[newSelectedNodeIndex] !is ChessHistoryItem.MoveItem)) {
            newSelectedNodeIndex++
        }
        if (_historyElements[newSelectedNodeIndex] !is ChessHistoryItem.MoveItem) return false
        _selectedNodeIndex = newSelectedNodeIndex
        val currentHistoryNode = _historyElements[newSelectedNodeIndex] as ChessHistoryItem.MoveItem
        loadPosition(currentHistoryNode.positionFen)
        _lastMoveArrow = LastMoveArrow(
            startFile = currentHistoryNode.movesCoordinates.startFile,
            startRank = currentHistoryNode.movesCoordinates.startRank,
            endFile = currentHistoryNode.movesCoordinates.endFile,
            endRank = currentHistoryNode.movesCoordinates.endRank,
        )
        return true
    }

    private fun requestBackOneMove(): Boolean {
        if (_gameInProgress) return false
        if (_selectedNodeIndex == null) return false
        if (_selectedNodeIndex!! <= 0) return false
        var newSelectedNodeIndex = _selectedNodeIndex!! - 1
        while ((newSelectedNodeIndex >= 0) && (_historyElements[newSelectedNodeIndex] !is ChessHistoryItem.MoveItem)) {
            newSelectedNodeIndex--
        }
        return if (newSelectedNodeIndex >= 0) {
            _selectedNodeIndex = newSelectedNodeIndex
            true
        } else {
            false
        }
    }

    private fun addMoveToHistory(moveCoordinates: MoveCoordinates) {
        val lastMove: Move = _gameLogic.lastMove
        val isWhiteTurnBeforeMove = _gameLogic.position.toPlay == Chess.BLACK
        val needingToAddMoveNumber = isWhiteTurnBeforeMove && !_isFirstHistoryNode

        if (needingToAddMoveNumber) {
            _historyElements.add(
                ChessHistoryItem.MoveNumberItem(
                    number = Chess.plyToMoveNumber(_gameLogic.position.plyNumber),
                    isWhiteTurn = true,
                )
            )
        }

        _historyElements.add(
            ChessHistoryItem.MoveItem(
                san = lastMove.san, positionFen = FEN.getFEN(_gameLogic.position), isWhiteMove = isWhiteTurnBeforeMove,
                movesCoordinates = moveCoordinates,
            )
        )
        _isFirstHistoryNode = false
    }

    private fun handleGameEndingStatus(
        onCheckmate: (Boolean) -> Unit,
        onStalemate: () -> Unit,
        onThreeFoldsRepetition: () -> Unit,
        onInsufficientMaterial: () -> Unit,
        onFiftyMovesRuleDraw: () -> Unit,
    ) {
        if (_gameLogic.position.isMate) {
            if (_gameLogic.position.toPlay == Chess.BLACK) {
                _gameInProgress = false
                _whitePlayerType = PlayerType.None
                _blackPlayerType = PlayerType.None
                selectLastHistoryMoveNodeIfAny()
                onCheckmate(true)
            } else {
                _gameInProgress = false
                _whitePlayerType = PlayerType.None
                _blackPlayerType = PlayerType.None
                selectLastHistoryMoveNodeIfAny()
                onCheckmate(false)
            }
        } else if (_gameLogic.position.isStaleMate) {
            _gameInProgress = false
            _whitePlayerType = PlayerType.None
            _blackPlayerType = PlayerType.None
            selectLastHistoryMoveNodeIfAny()
            onStalemate()
        } else if (_occurrences[FEN.getFEN(_gameLogic.position)]!! >= 3) {
            _gameInProgress = false
            _whitePlayerType = PlayerType.None
            _blackPlayerType = PlayerType.None
            selectLastHistoryMoveNodeIfAny()
            onThreeFoldsRepetition()
        } else if (_gameLogic.position.isMissingMatingMaterial()) {
            _gameInProgress = false
            _whitePlayerType = PlayerType.None
            _blackPlayerType = PlayerType.None
            selectLastHistoryMoveNodeIfAny()
            onInsufficientMaterial()
        } else if (_gameLogic.position.isTerminal) {
            _gameInProgress = false
            _whitePlayerType = PlayerType.None
            _blackPlayerType = PlayerType.None
            selectLastHistoryMoveNodeIfAny()
            onFiftyMovesRuleDraw()
        }

    }


    private fun selectLastHistoryMoveNodeIfAny() {
        var lastHistoryMoveNodeIndex = _historyElements.size - 1
        while ((lastHistoryMoveNodeIndex >= 0) && (_historyElements[lastHistoryMoveNodeIndex] !is ChessHistoryItem.MoveItem)) {
            lastHistoryMoveNodeIndex--
        }
        _selectedNodeIndex = if (lastHistoryMoveNodeIndex >= 0) lastHistoryMoveNodeIndex else null
    }
}