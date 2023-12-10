package pawnrace

// row is counted top down from 0 index, so B starts at 2nd row from the top, W starts at 7th row
// direction means pawn increase or decrease row index when moving across the board.
// B goes down so +1, W goes up so -1
enum class Piece(val startRow: Int, val direction: Int) {
    B(1, 1), W(6, -1), None(0, 0);

    fun opposite(): Piece {
        return when(this) {
            B -> W
            W -> B
            else -> None
        }
    }

    override fun toString(): String {
        return when(this) {
            B -> "B"
            W -> "W"
            else -> "."
        }
    }
}

fun charToPiece(c: Char): Piece = if (c == 'W') Piece.W else Piece.B

class File(val col: Char) {
    override fun toString(): String = col.toString()
    fun getCol(): Int = col.lowercaseChar().code - 97

    fun validFile(): Boolean = col.lowercaseChar() in 'a'..'z'
}

fun coltoChar(x: Int): Char = (x + 97).toChar()
