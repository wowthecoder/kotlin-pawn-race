package pawnrace

enum class MoveType(val priority: Int) {
    PEACEFUL(0), CAPTURE(1), EN_PASSANT(2)
}

data class Move(val piece: Piece, val from: Position, val to: Position, val type: MoveType) {
    override fun toString(): String {
        return when {
            type == MoveType.EN_PASSANT || type == MoveType.CAPTURE -> "${from.file}x${to.pos}"
            else -> to.pos
        }
    }
}