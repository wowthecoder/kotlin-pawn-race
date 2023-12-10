package pawnrace

import java.io.PrintWriter
import java.io.InputStreamReader
import java.io.BufferedReader
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

// You should not add any more member values or member functions to this class
// (or change its name!). The autorunner will load it in via reflection and it
// will be safer for you to just call your code from within the playGame member
// function, without any unexpected surprises!
class PawnRace {
  // Don't edit the type or the name of this method
  // The colour can take one of two values: 'W' or 'B', this indicates your player colour
  fun playGame(colour: Char, output: PrintWriter, input: BufferedReader) {
    // You should call your code from within here
    // Step 1: If you are the black player, you should send a string containing the gaps
    // It should be of the form "wb" with the white gap first and then the black gap: i.e. "AH"
    // TODO: Send gaps with output.println()
    if (colour == 'B') {
      output.println("AH")
    }

    // Regardless of your colour, you should now receive the gaps verified by the autorunner
    // (or by the human if you are using your own main function below), these are provided
    // in the same form as above ("wb"), for example: "AH"
    // TODO: receive the confirmed gaps with input.readLine()
    val gaps = input.readLine()

    // Now you may construct your initial board
    // TODO: Initialise the board state
    val board = Board(File(gaps[0]), File(gaps[1]))
    println(board.toString())
    initZobristTable()
    try {
//      val numProcesses = (Runtime.getRuntime().availableProcessors() / 2) - 1 - Thread.activeCount()
//      println("Number of processes: ${Thread.activeCount()}")
//      val executor = Executors.newFixedThreadPool(maxOf(numProcesses,1))


      // If you are the white player, you are now allowed to move
      // you may send your move, once you have decided what it will be, with output.println(move)
      // for example: output.println("axb4")
      // TODO: White player should decide what move to make and send it
      val piece = charToPiece(colour)
      val player = Player(piece, null)
      val opponent = Player(piece.opposite(), player)
      player.opponent = opponent
      var game = Game(board, player)
      if (piece == Piece.W) {
        val m = player.makeMove(game)
        if (m != null) {
          game = game.applyMove(m)
        }
        output.println(m.toString())
        println(game.board.toString())
      } else {
        game = Game(board, opponent)
      }


      // After point, you may create a loop which waits to receive the other players move
      // (via input.readLine()), updates the state, checks for game over and, if not, decides
      // on a new move and again send that with output.println(move). You should check if the
      // game is over after every move.
      /* TODO: Create the "game loop", which:
          * gets the opponents move
          * updates board
          * checks game over, if not then
          * choose a move
          * send this move
          * update the state
          * check game over
          * rinse, and repeat.
    */
      do {
        val oppMove = game.parseMove(input.readLine())
        if (oppMove != null) {
          game = game.applyMove(oppMove)
          println(game.board.toString())
          if (game.over()) {
            println("AI lost")
          } else {
            val myMove = player.makeMove(game)
            output.println(myMove.toString())
            if (myMove != null) {
              game = game.applyMove(myMove)
              println(game.board.toString())
              //println(game.moves)
            } else {
              println(myMove.toString())
            }
          }
        } else {
          println("Opponent null move!")
        }
      } while (!game.over())

//      executor.shutdownNow()
//      executor.awaitTermination(4, TimeUnit.SECONDS)
    } catch (e: Exception) {
      e.printStackTrace()
      println(e.message)
    }
    // Once the loop is over, the game has finished and you may wish to print who has won
    // If your advanced AI has used any files, make sure you close them now!
    // TODO: tidy up resources, if any
    //executor.shutdownNow()
    //executor.awaitTermination(1, TimeUnit.SECONDS)
  }
}

// When running the command, provide an argument either W or B, this indicates your player colour
fun main(args: Array<String>) {
  PawnRace().playGame('W', PrintWriter(System.out, true), BufferedReader(InputStreamReader(System.`in`)))
}
