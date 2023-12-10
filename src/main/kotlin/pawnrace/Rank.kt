package pawnrace

class Rank(val row: Int) {
    override fun toString(): String = row.toString()

    fun getRowIndex(): Int = 8 - row

    fun validRank(): Boolean = row in 1..8
}