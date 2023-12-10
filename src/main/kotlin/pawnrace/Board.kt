package pawnrace

import kotlin.math.absoluteValue
import pawnrace.MoveType.*
import pawnrace.Piece.*
import kotlin.math.pow

val zobristTable: Array<Array<Array<ULong>>> = Array(8) { Array(8) { Array(2) {0UL} } }

fun initZobristTable() {
    for (i in 0..7) {
        for (j in 0..7) {
            var randLong = (0UL until (2.0.pow(64.0).toULong() - 1UL)).random()
            zobristTable[i][j][0] = randLong
            randLong = (0UL until (2.0.pow(64.0).toULong() - 1UL)).random()
            zobristTable[i][j][1] = randLong
        }
    }
}

class Board(val board: Array<Array<Piece>>) {
    private var hash = 0

    /*init {
        // Row index starts from the top, 0 to 7
        // Fill the pawns
        for (i in 0..7) {
            board[B.startRow][i] = B
            board[W.startRow][i] = W
        }
        //Remove the pawns on the gaps
        board[W.startRow][whiteGap.getCol()] = None
        board[B.startRow][blackGap.getCol()] = None

        hash = computeHash()
    }*/

    constructor(whiteGap: File, blackGap: File) : this(Array(8) { Array(8) {Piece.None} }) {
        // Row index starts from the top, 0 to 7
        // Fill the pawns
        for (i in 0..7) {
            board[B.startRow][i] = B
            board[W.startRow][i] = W
        }
        //Remove the pawns on the gaps
        board[W.startRow][whiteGap.getCol()] = None
        board[B.startRow][blackGap.getCol()] = None
    }

    init {
        hash = computeHash()
    }

    fun computeHash(): Int {
        var hashCode: ULong = 0UL
        board.forEachIndexed { i, row ->
            row.forEachIndexed { j, piece ->
                when (piece) {
                    W -> hashCode = hashCode xor zobristTable[i][j][0]
                    B -> hashCode = hashCode xor zobristTable[i][j][1]
                    else -> {}
                }
            }
        }
        return hashCode.toInt()
    }

    fun pieceAt(pos: Position): Piece = board[pos.rank.getRowIndex()][pos.file.getCol()]
    fun positionsOf(piece: Piece): List<Position> = board.mapIndexed { r, row ->
        row.mapIndexedNotNull { c, elem ->
            if (elem == piece) (r to c) else null
        }
    }.flatten().map{ Position(File(coltoChar(it.second)), Rank(8 - it.first)) }

    fun isValidMove(move: Move, lastMove: Move? = null): Boolean {
        val fromR = move.from.rank.getRowIndex()
        val fromC = move.from.file.getCol()
        val correctFrom: Boolean = board[fromR][fromC] == move.piece
        val toR = move.to.rank.getRowIndex()
        val toC = move.to.file.getCol()
        val notOutOfBounds: Boolean = toR in 0..7 && toC in 0..7
        val movedR = (toR - fromR) * move.piece.direction
        val movedC = (toC - fromC).absoluteValue
        when (move.type) {
            // Either one step forward only, or two steps forward if from starting position
            // The path cannot be blocked by another pawn (even my own)!
            PEACEFUL -> return if (movedR == 1) {
                correctFrom && notOutOfBounds && fromC == toC && board[toR][toC] == None
            } else {
                if (movedR == 2) {
                    (correctFrom
                            && notOutOfBounds
                            && fromC == toC
                            && board[fromR + 1 * move.piece.direction][toC] == None
                            && board[toR][toC] == None
                            && fromR == move.piece.startRow)
                } else {
                    false
                }
            }
            // (Capture) Check if there's a opponent piece to the diagonal of the pawn
            // Only 1 horizontal + 1 vertical forward, no more no less
            CAPTURE -> return correctFrom
                    && notOutOfBounds
                    && movedC == 1
                    && movedR == 1
                    && board[toR][toC] == move.piece.opposite()
            // Check if opponent's last move is moving the pawn up by 2 steps
            EN_PASSANT -> return correctFrom
                    && notOutOfBounds
                    && movedC == 1
                    && movedR == 1
                    && board[fromR][toC] == move.piece.opposite()
                    && lastMove != null
                    && lastMove.from.rank.getRowIndex() == lastMove.piece.startRow
                    && lastMove.to.rank.getRowIndex() - lastMove.from.rank.getRowIndex() == 2 * lastMove.piece.direction
                    && lastMove.from.file.getCol() == toC
        }
    }

    fun move(m: Move): Board {
        val fromR = m.from.rank.getRowIndex()
        val toR = m.to.rank.getRowIndex()
        val toC = m.to.file.getCol()
        val newBoard = board.map { it.clone() }.toTypedArray()
        newBoard[fromR][m.from.file.getCol()] = None
        newBoard[toR][toC] = m.piece
        when (m.type) {
            EN_PASSANT -> newBoard[fromR][toC] = None
            else -> {}
        }
        return Board(newBoard)
    }
//
//    fun undo(m: Move): Board {
//        val prevR = m.from.rank.getRowIndex()
//        val prevC = m.from.file.getCol()
//        val currR = m.to.rank.getRowIndex()
//        val currC = m.to.file.getCol()
//        board[currR][currC] = None
//        board[prevR][prevC] = m.piece
//        // Replace the captured pieces
//        when (m.type) {
//            EN_PASSANT -> board[prevR][currC] = m.piece.opposite()
//            CAPTURE -> board[currR][currC] = m.piece.opposite()
//            PEACEFUL -> {}
//        }
//        hash = computeHash()
//        return this
//    }

    override fun toString(): String {
        val res = StringBuilder()
        res.append("  A B C D E F G H  \n")
        board.forEachIndexed { i, row ->
            val line = row.map{ it.toString() }.joinToString(" ")
            res.append("${8-i} $line ${8-i}\n")
        }
        res.append("  A B C D E F G H  \n")
        return res.toString()
    }

    override fun hashCode(): Int = hash

    override fun equals(other: Any?) = if (other is Board) (hash == other.hash) else false

}