package pawnrace

import pawnrace.MoveType.*
import pawnrace.Piece.*

class Game(val board: Board, val player: Player, val lastMove: Move? = null) {
//    fun applyMove(move: Move): Game {
//        moves.addFirst(move)
//        player = player.opponent!!
//        board.move(move)
//        return this
//    }
//    fun unapplyMove(): Game {
//        if (moves.size > 0) {
//            val lastMove = moves.removeFirst()
//            player = player.opponent!!
//            board.undo(lastMove)
//        }
//        return this
//    }
    fun applyMove(move: Move): Game = Game(board.move(move), player.opponent!!, move)

    // This will return empty list if there are no more pieces of that colour
    fun allMoves(piece: Piece): List<Move> = board.positionsOf(piece).flatMap { pos ->
        listOfNotNull(
            moveForwardBy(pos, 1, piece),
            moveForwardBy(pos, 2, piece),
            moveDiagonalBy(pos, true, piece, CAPTURE),
            moveDiagonalBy(pos, false, piece, CAPTURE),
            moveDiagonalBy(pos, true, piece, EN_PASSANT),
            moveDiagonalBy(pos, false, piece, EN_PASSANT),
        )
    }

    private fun moveForwardBy(pos: Position, step: Int, piece: Piece): Move? {
        val newRank = Rank(8 - (pos.rank.getRowIndex() + step * piece.direction))
        val m = Move(piece, pos, Position(pos.file, newRank), PEACEFUL)
        return if (board.isValidMove(m)) m else null
    }

    // isLeft is to check whether the opponent pawn is on ur left side or not
    // Pass in last move to isValidMove to check for En Passant
    private fun moveDiagonalBy(pos: Position, isLeft: Boolean, piece: Piece, type: MoveType): Move? {
        val newRank = Rank(8 - (pos.rank.getRowIndex() + 1 * piece.direction))
        if (isLeft) {
            // Char - Int will result in ASCII value deduction and returns a char
            val newFile = File(pos.file.col - 1)
            val mLeft = Move(piece, pos, Position(newFile, newRank), type)
            return if (board.isValidMove(mLeft, lastMove)) mLeft else null
        } else {
            val newFile = File(pos.file.col + 1)
            val mRight = Move(piece, pos, Position(newFile, newRank), type)
            return if (board.isValidMove(mRight, lastMove)) mRight else null
//            return if (moves.isEmpty()) {
//                if (board.isValidMove(mRight)) mRight else null
//            } else {
//                if (board.isValidMove(mRight, moves.first())) mRight else null
//            }
        }
    }

    // 6 situations: Either side has pawn on the top row, either side cannot make a move
    // or either side has no more pawns (which is covered by the 2nd case above)
    fun over(): Boolean = board.board[0].any { it == W }
            || board.board[7].any { it == B }
            || allMoves(W).isEmpty()
            || allMoves(B).isEmpty()

    // Return null if it's a draw, or the game is not over
    fun winner(): Player? {
        return when {
            board.board[0].any { it == W } -> playerByPiece(W)
            board.board[7].any { it == B } -> playerByPiece(B)
            else -> null
        }
    }

    fun playerByPiece(piece: Piece): Player = if (piece == player.piece) player else player.opponent!!

    // Return null if the input format is invalid, or if the move itself is invalid
    fun parseMove(san: String): Move? {
        return when (san.length) {
            2 -> {
                try {
                    val file = File(san[0])
                    val rankNum = san[1].toString().toInt()
                    val endRank = Rank(rankNum)
                    if (file.validFile() && endRank.validRank()) {
                        val m1 = Move(player.piece,
                                Position(file, Rank(rankNum + 1 * player.piece.direction)),
                                Position(file, endRank),
                                PEACEFUL
                        )
                        val m2 = Move(player.piece,
                                Position(file, Rank(rankNum + 2 * player.piece.direction)),
                                Position(file, endRank),
                                PEACEFUL
                        )
                        return when {
                            board.isValidMove(m1) -> m1
                            board.isValidMove(m2) -> m2
                            else -> null
                        }
                    } else {
                        return null
                    }
                } catch (e: Exception) {
                    println(e.message)
                    return null
                }
            }
            4 -> {
                try {
                    if (san[1] == 'x') {
                        val startFile = File(san[0])
                        val endFile = File(san[2])
                        val rankNum = san[3].toString().toInt()
                        val endRank = Rank(rankNum)
                        if (startFile.validFile() && endFile.validFile() && endRank.validRank()) {
                            val m1 = Move(player.piece,
                                    Position(startFile, Rank(rankNum + 1 * player.piece.direction)),
                                    Position(endFile, endRank),
                                    CAPTURE
                            )
                            val m2 = Move(player.piece,
                                    Position(startFile, Rank(rankNum + 1 * player.piece.direction)),
                                    Position(endFile, endRank),
                                    EN_PASSANT
                            )
                            return when {
                                board.isValidMove(m1) -> m1
                                board.isValidMove(m2) -> m2
                                else -> null
                            }
                        } else {
                            return null
                        }
                    } else {
                        return null
                    }
                } catch (e: Exception) {
                    println(e.message)
                    return null
                }
            }
            else -> null
        }
    }

    override fun hashCode(): Int = board.hashCode() + player.hashCode()
}