package pawnrace

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerTest {
    @Test
    fun `Eval calc correctly`() {
        val board = Board(File('a'), File('h'))
        val player = Player(Piece.W, null)
        val opponent = Player(Piece.B, player)
        player.opponent = opponent
        var game = Game(board, player)
        game = game.applyMove(game.parseMove("e4")!!)
        game = game.applyMove(game.parseMove("d5")!!)
        game = game.applyMove(game.parseMove("exd5")!!)
        game = game.applyMove(game.parseMove("c5")!!)
        game = game.applyMove(game.parseMove("b4")!!)
        game = game.applyMove(game.parseMove("g5")!!)
        game = game.applyMove(game.parseMove("h3")!!)
        game = game.applyMove(game.parseMove("cxb4")!!)
        game = game.applyMove(game.parseMove("d6")!!)
        game = game.applyMove(game.parseMove("a6")!!)
        game = game.applyMove(game.parseMove("f3")!!)
        game = game.applyMove(game.parseMove("e5")!!)
        println(game.board.toString())
        assertEquals(998950, player.eval(game, false))
    }
}