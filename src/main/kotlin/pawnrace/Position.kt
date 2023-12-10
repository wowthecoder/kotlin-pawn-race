package pawnrace

// Pre-condition: string `pos` consists of 1 char and 1 int
class Position(val file: File, val rank: Rank) {
    val pos: String = file.col + rank.row.toString()

    override fun toString(): String = pos
}