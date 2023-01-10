package logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import components.*
import io.github.wolfraam.chessgame.ChessGame
import io.github.wolfraam.chessgame.board.PieceType
import io.github.wolfraam.chessgame.board.Side
import io.github.wolfraam.chessgame.board.Square
import io.github.wolfraam.chessgame.move.Move
import io.github.wolfraam.chessgame.notation.NotationType
import io.github.wolfraam.chessgame.result.ChessGameResult
import io.github.wolfraam.chessgame.result.ChessGameResultType
import io.github.wolfraam.chessgame.result.DrawType

const val defaultPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
const val emptyPosition = "k/8/8/8/8/8/8/K w - - 0 1"

object ChessGameManager {
    private var _gameInProgress by mutableStateOf(false)
    private var _gameLogic by mutableStateOf(ChessGame(emptyPosition))
    private var _pendingPromotion by mutableStateOf(PendingPromotion.None)
    private var _pendingPromotionStartSquare by mutableStateOf<Square?>(null)
    private var _pendingPromotionEndSquare by mutableStateOf<Square?>(null)
    private var _lastMoveArrow by mutableStateOf<LastMoveArrow?>(null)
    private var _historyElements by mutableStateOf<MutableList<ChessHistoryItem>>(mutableListOf())
    private var _isFirstHistoryNode by mutableStateOf(false)
    private var _positionFenBeforeLastMove by mutableStateOf<String?>(null)
    private var _selectedNodeIndex by mutableStateOf<Int?>(null)
    private var _startPosition by mutableStateOf(defaultPosition)
    private var _whitePlayerType by mutableStateOf(PlayerType.Human)
    private var _blackPlayerType by mutableStateOf(PlayerType.Human)

    fun getPieces(): List<List<Char>> {
        val positionFen = _gameLogic.fen
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

    fun getWhitePlayerType(): PlayerType = _whitePlayerType

    fun getBlackPlayerType(): PlayerType = _blackPlayerType

    fun getHistoryElements(): List<ChessHistoryItem> = _historyElements

    fun getLastMoveArrow(): LastMoveArrow? = _lastMoveArrow

    fun isGameInProgress(): Boolean = _gameInProgress

    fun getPendingPromotion(): PendingPromotion = _pendingPromotion

    fun getPendingPromotionStartSquare(): Square? = _pendingPromotionStartSquare

    fun getPendingPromotionEndSquare(): Square? = _pendingPromotionEndSquare

    fun getSelectedHistoryNodeIndex(): Int? = _selectedNodeIndex

    fun isWhiteTurn(): Boolean {
        val positionFen = _gameLogic.fen
        return positionFen.split(" ")[1] == "w"
    }

    fun setStartPosition(startPosition: String) {
        startPosition.testIfIsLegalChessFen()
        _startPosition = startPosition
    }

    fun stopGame() {
        _gameInProgress = false
        _whitePlayerType = PlayerType.Computer
        _blackPlayerType = PlayerType.Computer
        selectLastHistoryMoveNodeIfAny()
    }

    fun resetGame() {
        _gameLogic = ChessGame(_startPosition)
        val isWhiteTurn = _gameLogic.sideToMove == Side.WHITE
        val moveNumber = _gameLogic.fullMoveCount
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
        val startSquare = Square.values()[8 * startFile + startRank]
        val endSquare = Square.values()[8 * endFile + endRank]
        val move = Move(startSquare, endSquare)

        if (_gameLogic.isLegalMove(move)) {
            _positionFenBeforeLastMove = _gameLogic.fen
            _gameLogic.playMove(move)
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
            handleGameEndingStatus(
                onCheckmate = onCheckmate,
                onStalemate = onStalemate,
                onThreeFoldsRepetition = onThreeFoldsRepetition,
                onInsufficientMaterial = onInsufficientMaterial,
                onFiftyMovesRuleDraw = onFiftyMovesRuleDraw,
            )
        } else {
            val isLegalPromotionMove = _gameLogic.isLegalMove(Move(startSquare, endSquare, PieceType.QUEEN))

            if (isLegalPromotionMove) {
                _pendingPromotion = if (isWhiteTurn()) PendingPromotion.White else PendingPromotion.Black
                _pendingPromotionStartSquare = startSquare
                _pendingPromotionEndSquare = endSquare
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
            PromotionType.Queen -> PieceType.QUEEN
            PromotionType.Rook -> PieceType.ROOK
            PromotionType.Bishop -> PieceType.BISHOP
            PromotionType.Knight -> PieceType.KNIGHT
        }
        val move = Move(_pendingPromotionStartSquare, _pendingPromotionEndSquare, promotionPiece)
        if (_gameLogic.isLegalMove(move)) {
            _positionFenBeforeLastMove = _gameLogic.fen
            _gameLogic.playMove(move)
            addMoveToHistory(
                MoveCoordinates(
                    startFile = _pendingPromotionStartSquare!!.x,
                    startRank = _pendingPromotionStartSquare!!.y,
                    endFile = _pendingPromotionEndSquare!!.x,
                    endRank = _pendingPromotionEndSquare!!.y
                )
            )
            _pendingPromotion = PendingPromotion.None
            _pendingPromotionStartSquare = null
            _pendingPromotionEndSquare = null

            _lastMoveArrow = LastMoveArrow(
                startFile = move.from.x,
                startRank = move.from.y,
                endFile = move.to.x,
                endRank = move.to.y
            )


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
        _gameLogic = ChessGame(positionFen)
        _lastMoveArrow = LastMoveArrow(
            startFile = moveCoordinates.startFile,
            startRank = moveCoordinates.startRank,
            endFile = moveCoordinates.endFile,
            endRank = moveCoordinates.endRank
        )
        _selectedNodeIndex = nodeToSelectIndex
        return true
    }

    fun requestGotoPreviousHistoryNode(): Boolean {
        if (_gameInProgress) return false
        if (_selectedNodeIndex == null) return false
        return if (requestBackOneMove()) {
            val currentHistoryNode = _historyElements[_selectedNodeIndex!!] as ChessHistoryItem.MoveItem
            _gameLogic = ChessGame(currentHistoryNode.positionFen)
            _lastMoveArrow = LastMoveArrow(
                startFile = currentHistoryNode.movesCoordinates.startFile,
                startRank = currentHistoryNode.movesCoordinates.startRank,
                endFile = currentHistoryNode.movesCoordinates.endFile,
                endRank = currentHistoryNode.movesCoordinates.endRank,
            )
            true
        } else {
            _gameLogic = ChessGame(_startPosition)
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
        _gameLogic = ChessGame(currentHistoryNode.positionFen)
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
        val lastMove: Move? = _gameLogic.lastMove
        val isWhiteTurnBeforeMove = _gameLogic.sideToMove == Side.BLACK
        val needingToAddMoveNumber = isWhiteTurnBeforeMove && !_isFirstHistoryNode

        if (needingToAddMoveNumber) {
            _historyElements.add(
                ChessHistoryItem.MoveNumberItem(
                    number = _gameLogic.fullMoveCount,
                    isWhiteTurn = true,
                )
            )
        }

        val gameLogicBeforeMove = ChessGame(_positionFenBeforeLastMove)
        val moveSan = gameLogicBeforeMove.getNotation(NotationType.SAN, lastMove)
        _historyElements.add(
            ChessHistoryItem.MoveItem(
                san = moveSan, positionFen = _gameLogic.fen, isWhiteMove = isWhiteTurnBeforeMove,
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
        val gameResult: ChessGameResult? = _gameLogic.gameResult
        when (gameResult?.chessGameResultType) {
            ChessGameResultType.WHITE_WINS -> {
                _gameInProgress = false
                _whitePlayerType = PlayerType.Computer
                _blackPlayerType = PlayerType.Computer
                selectLastHistoryMoveNodeIfAny()
                onCheckmate(true)
            }

            ChessGameResultType.BLACK_WINS -> {
                _gameInProgress = false
                _whitePlayerType = PlayerType.Computer
                _blackPlayerType = PlayerType.Computer
                selectLastHistoryMoveNodeIfAny()
                onCheckmate(false)
            }

            ChessGameResultType.DRAW -> {
                when (gameResult.drawType) {
                    DrawType.STALE_MATE -> {
                        _gameInProgress = false
                        _whitePlayerType = PlayerType.Computer
                        _blackPlayerType = PlayerType.Computer
                        selectLastHistoryMoveNodeIfAny()
                        onStalemate()
                    }

                    DrawType.THREEFOLD_REPETITION -> {
                        _gameInProgress = false
                        _whitePlayerType = PlayerType.Computer
                        _blackPlayerType = PlayerType.Computer
                        selectLastHistoryMoveNodeIfAny()
                        onThreeFoldsRepetition()
                    }

                    DrawType.INSUFFICIENT_MATERIAL -> {
                        _gameInProgress = false
                        _whitePlayerType = PlayerType.Computer
                        _blackPlayerType = PlayerType.Computer
                        selectLastHistoryMoveNodeIfAny()
                        onInsufficientMaterial()
                    }

                    DrawType.FIFTY_MOVE_RULE -> {
                        _gameInProgress = false
                        _whitePlayerType = PlayerType.Computer
                        _blackPlayerType = PlayerType.Computer
                        selectLastHistoryMoveNodeIfAny()
                        onFiftyMovesRuleDraw()
                    }

                    else -> throw RuntimeException("Not in a draw state.")
                }
            }

            else -> {}
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