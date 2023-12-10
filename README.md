# Kotlin Pawn Race
Code for Kotlin Chess: Pawn Race tournament 2023-24

## Board and Setup
Pawn races are played on a normal chess board, with 8x8 squares. Rows are commonly referred
to as ranks, and are labelled 1-8, while columns are referred to as files, labelled A-H. From
white’s perspective, the square in the bottom left corner would thus be referred to as a1, while
the bottom right corner is h1. White’s pawns are all placed on the second rank initially, while
black starts from the seventh rank. Figure 1 shows an example of an initial setup, in which the
gaps were chosen on the H and A files, for white and black respectively.

<img src="https://github.com/wowthecoder/kotlin-pawn-race/assets/82577844/bbb46977-8483-4eeb-80ba-50f46f2203e4" width="300">

## Pawn moves
 - A pawn can move straight forward by 1 square, if the targeted square is empty.
 -  A pawn can move straight forward by 2 squares, if it is on its starting position, and both
the targeted square and the passed-through square are empty.
 - A pawn can move diagonally forward by 1 square, iff that square is occupied by an
opposite-coloured pawn. This constitutes a capture, and the captured pawn is taken off
the board.
 - Combining the previous two rules, if a pawn has moved forward by 2 squares in the last
move played, it may be captured on the square that it passed through. This special type
of capture is a capture in passing and commonly referred to as the En Passant rule. A
pawn can only be captured en passant immediately after it moved forward two squares,
but not at any later stage in the game.

## Gameplay
Each player would only play with 7 pawns, thus leaving a gap somewhere in the line of pawns. Since white has the advantage
of starting the game, the black player chooses where the gaps are.\
Both players take turns to make moves. If a player cannot make any valid move because all his pawns are blocked from moving, the
game is considered a stale-mate, which is a draw. Whichever player first manages to promote one of his pawns all the way to the last rank, 
as seen from his own perspective, wins the game.However, the game can also be won by a player capturing all of the opponent’s pawns.

## Tournament Rules
1. Rounds and Scoring - The tournament will consist of several rounds, in which players let their AIs compete with each other. Each round is played as best-of-five games, with only the winner advancing to the next round. In the five games played, each win counts as 2 points, while a draw/stalemate counts as 1 point for both players.
2. The colour of players in the first match of any pairing will be determined randomly. Players swap colours after each game.
3. The auto-runner will be used to play off two students' AIs against each other. The specific machine that plays the game will be a fast machine with many CPU cores (up to 20).
4. In each game, the player whose AI plays black may determine where both of the pawn gaps are (since white has the starting advantage). A player may not choose the same setup (or its mirroring) twice within the same round, although the other player may wish to choose the same setup when it is his/her turn to choose. Each player may choose at most one game per round in which the gaps are directly opposite each other.
5. If a program outputs an invalid move, or refuses to accept a valid move played by the other player, the game in progress will be counted as a loss.
6. All programs should output a move in less than 5 seconds. If it takes longer to return a move, the auto marker will forfeit the game on that player's behalf.
7. The top-ranked players will sit down in their pairings and log in to adjacent lab machines, they will clone their GitLab repo, and will verbally communicate moves made by their AIs to each other. Your AI assumes your colour, and the moves of the other player need to be entered manually.
8. Any code from the Kotlin standard library (including the standard Java libraries) may be used, but any other external code (especially AI or chess libraries) is not allowed to be used.
9. You will participate with the GitLab commit that you have submitted before the deadline. Any late submissions cannot participate in this tournament.
10. The code submitted by the Top 4 winners will be inspected after the end of the tournament and you may be invited to explain how it works.
11. You may use up to 1 MB of pre-computed data.

## Approach to AI
Below are the main techniques I incorporated into the AI
1. Functional implementation
2. Negamax algorithm
3. Alpha-beta pruning
4. Transposition table (with Zobrist Hashing)
5. Iterative deepening with time limit
