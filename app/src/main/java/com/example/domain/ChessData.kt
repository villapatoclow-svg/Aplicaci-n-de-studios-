package com.example.domain

import com.example.data.model.ChessPuzzle

object ChessData {

    val builtInPuzzles: List<ChessPuzzle> = listOf(
        ChessPuzzle(
            id = "mate_pasillo",
            title = "Mate del Pasillo",
            theme = "Jaque Mate en 1",
            sideToMove = "Blancas",
            prompt = "El rey negro está atrapado en la octava fila detrás de sus peones. Encuentra el mate con tu torre.",
            boardFen = "6k1/5ppp/8/8/8/8/5PPP/3R2K1",
            solutionFrom = 7 to 3, // d1
            solutionTo = 0 to 3,   // d8
            solutionMoveSan = "Td8#",
            explanation = "La torre blanca sube de d1 a d8 (Td8#). Los peones negros en f7, g7 y h7 bloquean la huida del rey, produciendo un mate del pasillo clásico."
        ),
        ChessPuzzle(
            id = "horquilla_caballo",
            title = "Horquilla Real",
            theme = "Táctica de Ataque Doble",
            sideToMove = "Blancas",
            prompt = "Salta con el caballo blanco para dar jaque al rey en e7 y atacar a la torre en h8.",
            boardFen = "7r/4k3/8/8/5N2/8/8/4K3",
            solutionFrom = 4 to 5, // f4
            solutionTo = 2 to 6,   // g6
            solutionMoveSan = "Cg6+",
            explanation = "El caballo salta de f4 a g6 (Cg6+), dando jaque al rey en e7 y atacando a la torre indefensa en h8 al mismo tiempo."
        ),
        ChessPuzzle(
            id = "dama_apoyo",
            title = "Beso de la Muerte",
            theme = "Mate con Dama y Alfil",
            sideToMove = "Blancas",
            prompt = "Tu alfil protege la casilla g7. Lleva la dama blanca junto al rey negro para rematar la partida.",
            boardFen = "6k1/5ppp/8/8/3B4/5Q2/8/6K1",
            solutionFrom = 5 to 5, // f3
            solutionTo = 1 to 6,   // g7
            solutionMoveSan = "Dg7#",
            explanation = "La dama captura en g7 (Dg7#) apoyada en la gran diagonal por el alfil en d4. El rey negro no puede capturar a la dama porque quedaría en jaque."
        ),
        ChessPuzzle(
            id = "clavada_alfil",
            title = "Clavada Absoluta",
            theme = "Clavada Táctica",
            sideToMove = "Blancas",
            prompt = "La dama negra en d7 está delante de su rey en e8. Lleva el alfil de c2 a la diagonal a4-e8 para clavarla.",
            boardFen = "4k3/3q4/8/8/8/8/2B5/4K3",
            solutionFrom = 6 to 2, // c2
            solutionTo = 4 to 0,   // a4
            solutionMoveSan = "Aa4",
            explanation = "Al mover el alfil a a4 (Aa4), la dama negra en d7 queda clavada frente a su rey en e8 y no puede moverse fuera de la diagonal."
        )
    )

    fun parseFenBoard(fen: String): Array<Array<Char?>> {
        val board = Array(8) { arrayOfNulls<Char>(8) }
        val ranks = fen.split(" ")[0].split("/")
        for (r in 0 until minOf(8, ranks.size)) {
            var c = 0
            for (ch in ranks[r]) {
                if (ch.isDigit()) {
                    c += ch.digitToInt()
                } else if (c < 8) {
                    board[r][c] = ch
                    c++
                }
            }
        }
        return board
    }

    fun pieceSymbol(piece: Char?): String = when (piece) {
        'K' -> "♔"
        'Q' -> "♕"
        'R' -> "♖"
        'B' -> "♗"
        'N' -> "♘"
        'P' -> "♙"
        'k' -> "♚"
        'q' -> "♛"
        'r' -> "♜"
        'b' -> "♝"
        'n' -> "♞"
        'p' -> "♟"
        else -> ""
    }

    fun isWhitePiece(piece: Char?): Boolean = piece != null && piece.isUpperCase()
}
