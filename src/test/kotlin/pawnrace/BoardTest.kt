package pawnrace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BoardTest {
    @Test
    fun `Board can undo move` () {
        val board = Board(File('a'), File('h'))
        val ansBoard = Board(File('a'), File('h'))
        val player = Player(Piece.W, null)
        val opponent = Player(Piece.B, player)
        player.opponent = opponent
        var game1 = Game(board, player)

        game1 = game1.applyMove(game1.parseMove("e4")!!)
        game1 = game1.applyMove(game1.parseMove("d5")!!)
        //game1.unapplyMove()
        var game2 = Game(ansBoard, player)
        game2 = game2.applyMove(game2.parseMove("e4")!!)

        println(board)
        println(ansBoard)
        assertEquals(ansBoard.hashCode(), board.hashCode())
    }

    @Test
    fun `Board hashcode works` () {
        val board1 = Board(File('a'), File('h'))
        val player = Player(Piece.W, null)
        val opponent = Player(Piece.B, player)
        player.opponent = opponent
        var game1 = Game(board1, player)
        val board2 = Board(File('a'), File('h'))
        var game2 = Game(board2, player)

        game1 = game1.applyMove(game1.parseMove("e4")!!)
        game1 = game1.applyMove(game1.parseMove("d5")!!)
        game2 = game2.applyMove(game2.parseMove("e4")!!)
        game2 = game2.applyMove(game2.parseMove("d5")!!)

        initZobristTable()
        println(board1.hashCode())
        println(board2.hashCode())
        assertEquals(board1.hashCode(), board2.hashCode())
        //assertNotEquals(board1.hashCode(), board2.hashCode())
    }
}