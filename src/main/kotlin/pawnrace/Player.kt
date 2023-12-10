package pawnrace

import pawnrace.Piece.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.pow

const val WIN = 100000000
const val TIME_LIMIT = 4500L // in ms
//const val INT_MAX = 10000000

// Stores the eval score/alpha-beta value, search depth, and best move for each board state
// flag: EXACT(eval score), LOWER(alpha), UPPER(beta)
enum class Flag {
    EXACT, LOWER, UPPER
}
data class TableItem(val value: Int, val depth: Int, val flag: Flag)

class Player(val piece: Piece, var opponent: Player? = null) {
    val transpositionTable: ConcurrentHashMap<Game, TableItem> = ConcurrentHashMap()
    //private val runnable = AtomicInteger(0)

    fun getAllPawns(board: Board): List<Position> = board.positionsOf(piece)

    fun getAllValidMoves(game: Game): List<Move> = game.allMoves(piece)

    // Search the capture moves first because they might give higher scores.
    fun orderMoves(moves: List<Move>): List<Move> = moves.sortedByDescending { it.type.priority }

    fun isPassedPawn(pos: Position, board: Board): Boolean {
        val c = pos.file.getCol()
        val r = pos.rank.getRowIndex()
        return if (piece == W){
            board.board.filterIndexed { index, row -> index < r }.flatMap { row ->
                row.filterIndexed { i, elem -> i in c-1..c+1 }
            }.all { it != piece.opposite() }
        } else {
            board.board.filterIndexed { index, row -> index > r }.flatMap { row ->
                row.filterIndexed { i, elem -> i in c-1..c+1 }
            }.all { it != piece.opposite() }
        }
    }

    fun makeMove(game: Game): Move? {
        val maxDepth = 25
        val ms = getAllValidMoves(game)
        //println("Available moves: $ms")
        val startTime = System.nanoTime() / 1000000
        // Stores the best score for each depth
         val depthBestScore = IntArray(maxDepth + 1) { Int.MIN_VALUE }
         //Stores the best move for each depth
         val depthBestMove = Array<Move?>(maxDepth + 1) { null }
         //Stores the number of moves/nodes traversed for each depth
         val depthSearchCount = IntArray(maxDepth + 1) { 0 }
        for (depth in 0..maxDepth) {
            ms.forEach { m ->
                val nextTurn = game.applyMove(m)
                val score = -negamax(nextTurn, depth, Int.MIN_VALUE, Int.MAX_VALUE, -1, startTime)
                if (score != Int.MIN_VALUE) {
                    depthSearchCount[depth]++
                    if (score > depthBestScore[depth]) {
                        depthBestScore[depth] = score
                        depthBestMove[depth] = m
                    }
                }
            }
        }
//        ms.forEach { m ->
//            game.applyMove(m)
//            val score = iterativeDeepeningSearch(game, timeLimit)
//            if (score > bestScore) {
//                bestScore = score
//                bestMove = m
//            }
//            game.unapplyMove()
//        }
        // get the depth where all moves/nodes are evaluated
        val maxCompleteDepth = depthSearchCount.indexOfLast { it == ms.size }
        //println("Max depth: $maxCompleteDepth")
        return if (maxCompleteDepth == -1) {
            ms.randomOrNull()
        } else {
            depthBestMove[maxCompleteDepth]
        }
    }

    // Run an iterative deepening search on a game state, taking no longer than the given time limit
//    private fun iterativeDeepeningSearch(game: Game, timeLimit: Long): Int {
//        val maxDepth = 25
//        val startTime = System.currentTimeMillis()
//        val endTime = startTime + timeLimit
//        var score = 0
//        for (depth in 0..maxDepth) {
//            val currentTime = System.currentTimeMillis()
//            if (currentTime >= endTime) {
//                println("Depth: $depth")
//                break
//            }
//            score = -negamax(game, depth, Int.MIN_VALUE, Int.MAX_VALUE, -1,  currentTime, timeLimit)
//            //score = -pvs(game, depth, Int.MIN_VALUE, Int.MAX_VALUE, -1, currentTime, timeLimit)
//
//            // If the search finds a winning move, stop searching
//            if (score >= WIN) {
//                return score
//            }
//        }
//        return score
//    }

    // Pawn at the topmost rank (won the game) is the highest score, 10000
    // Each pawn is worth (10 * rank), so the higher the rank the better.
    // Black is in opposite direction so (10 * (9 - rank))
    // A passed pawn of higher rank is better than several lower-ranked passed pawns.
    // Therefore a passed pawn's value is 10^rank, eg. pawn at row 6 better than 7 pawns at row 5 since 10^7 > 7*10^5
    fun eval(game: Game, gameOver: Boolean): Int {
        var score = 0
        if (gameOver) {
            when (game.winner()?.piece) {
                null -> score = 0
                piece -> score += WIN
                else -> score -= WIN
            }
        } else {
            getAllPawns(game.board).forEach { pos ->
                if (piece == W) {
                    if (isPassedPawn(pos, game.board)) {
                        score += 10.0.pow(pos.rank.row.toDouble()).toInt()
                    } else {
                        score += 10 * pos.rank.row
                    }
                } else {
                    if (isPassedPawn(pos, game.board)) {
                        score += 10.0.pow(9.0 - pos.rank.row).toInt()
                    } else {
                        score += 10 * (9 - pos.rank.row)
                    }
                }
            }
            opponent!!.getAllPawns(game.board).forEach { pos ->
                if (piece.opposite() == W) {
                    if (opponent!!.isPassedPawn(pos, game.board)) {
                        score -= 10.0.pow(pos.rank.row.toDouble()).toInt()
                    } else {
                        score -= 10 * pos.rank.row
                    }
                } else {
                    if (opponent!!.isPassedPawn(pos, game.board)) {
                        score -= 10.0.pow(9.0 - pos.rank.row).toInt()
                    } else {
                        score -= 10 * (9 - pos.rank.row)
                    }
                }
            }
        }
        return score
    }

    fun negamax(game: Game, depth: Int, alpha: Int, beta: Int, colour: Int, startTime: Long): Int {
        var a = alpha
        var b = beta

        // Lookup transposition table
        val tableEntry = transpositionTable[game]
        if (tableEntry != null && tableEntry.depth >= depth)
        {
            when (tableEntry.flag) {
                Flag.EXACT -> return tableEntry.value
                Flag.LOWER -> a = maxOf(a, tableEntry.value)
                Flag.UPPER -> b = minOf(b, tableEntry.value)
            }
            if (a >= b) {
                return tableEntry.value
            }
        }

        // Calculate score at the end of recursion
        val gameOver = game.over()
        val elapsed = System.nanoTime() / 1000000 - startTime
        if (elapsed >= TIME_LIMIT) {
            return Int.MIN_VALUE
        }
        if (depth == 0 || gameOver) {
            return colour * eval(game, gameOver)
        }

        // negamax
        val nextMoves: List<Move>
        if (colour == 1) {
            nextMoves = orderMoves(getAllValidMoves(game))
        } else {
            nextMoves = orderMoves(opponent!!.getAllValidMoves(game))
        }
        var score = Int.MIN_VALUE
        for (m in nextMoves) {
            val nextTurn = game.applyMove(m)
            //println(child.board.toString())
            score = maxOf(score, -negamax(nextTurn, depth - 1, -b, -a, -colour, startTime))
            a = maxOf(a, score)
            // Clean up child move
            //game.unapplyMove()
            if (a >= b) {
                break
            }
            //println(game.board.toString())
        }

        // Store result in transposition table, game is the key
        val flag: Flag
        when {
            score <= alpha -> flag = Flag.UPPER
            score >= beta -> flag = Flag.LOWER
            else -> flag = Flag.EXACT
        }
        transpositionTable[game] = TableItem(score, depth, flag)

        return score
    }

//    fun pvs(game: Game, depth: Int, alpha: Int, beta: Int, colour: Int, startTime: Long): Int {
//        var a = alpha
//        var b = beta
//
//        // Lookup transposition table
//        val tableEntry = transpositionTable[game]
//        if (tableEntry != null && tableEntry.depth >= depth)
//        {
//            when (tableEntry.flag) {
//                Flag.EXACT -> return tableEntry.value
//                Flag.LOWER -> a = maxOf(a, tableEntry.value)
//                Flag.UPPER -> b = minOf(b, tableEntry.value)
//            }
//            if (a >= b) {
//                return tableEntry.value
//            }
//        }
//
//        // Calculate score at the end of recursion
//        val gameOver = game.over()
//        val elapsed = System.nanoTime() / 1000000 - startTime
//        if (elapsed >= TIME_LIMIT) {
//            return Int.MIN_VALUE
//        }
//        if (depth == 0 || gameOver) {
//            return colour * eval(game, gameOver)
//        }
//
//        // pvs
//        val nextMoves: List<Move>
//        if (colour == 1) {
//            nextMoves = orderMoves(getAllValidMoves(game))
//        } else {
//            nextMoves = orderMoves(opponent!!.getAllValidMoves(game))
//        }
//        var score = Int.MIN_VALUE
//        for (i in nextMoves.indices) {
//            val m = nextMoves[i]
//            game.applyMove(m)
//            if (i == 0) {
//                score = -pvs(game, depth - 1, -b, -a, -colour, startTime)
//            } else {
//                score = -pvs(game, depth - 1, -a - 1, -a, -colour, startTime)
//                if (score in (a + 1)..<b) {
//                    score = -pvs(game, depth - 1, -b, -a, -colour, startTime)
//                }
//            }
//            a = maxOf(a, score)
//            // Clean up child move
//            //game.unapplyMove()
//            if (a >= b) {
//                break
//            }
//            //println(game.board.toString())
//        }
//
//        // Store result in transposition table, game is the key
//        val flag: Flag
//        when {
//            score <= alpha -> flag = Flag.UPPER
//            score >= beta -> flag = Flag.LOWER
//            else -> flag = Flag.EXACT
//        }
//        transpositionTable[game] = TableItem(score, depth, flag)
//
//        return score
//    }

    override fun equals(other: Any?): Boolean {
        return if (other is Player) {
            piece == other.piece
        } else {
            false
        }
    }

    override fun hashCode(): Int = piece.hashCode()
}