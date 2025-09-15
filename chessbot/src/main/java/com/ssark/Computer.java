package com.ssark;
import java.util.ArrayList;

public class Computer {
    public ArrayList<MoveEval> bestMoves = new ArrayList<MoveEval>();
    public int counter = 0;
    public MoveEval findBestMove(int[] board,int depth, int colorToMove, Move lastMove){
        ArrayList<Move> legalMoves = BoardHelper.findLegalMoves(board, colorToMove);
        ArrayList<MoveEval> moveEvals = new ArrayList<MoveEval>();
        if(legalMoves.size() == 0){
            return new MoveEval(lastMove, -1_000_000_000*colorToMove);
        }
        for(Move move:legalMoves){
            int[] newBoard = BoardHelper.makeMove(board, move);
            if((depth-1) == 0){
                counter++;
                MoveEval eval = new MoveEval(new Move(move),evaluate(newBoard));
                moveEvals.add(eval);
            }else {
                MoveEval bestMove = findBestMove(newBoard, depth-1, colorToMove * -1,move);
                moveEvals.add(new MoveEval(new Move(move),bestMove.evaluation));
            }
        }
        moveEvals.sort((a,b) -> {
            double valA = colorToMove * a.evaluation;
            double valB = colorToMove * b.evaluation;
            return (int)(valB - valA);
        });

        bestMoves = moveEvals;
        return moveEvals.get(0);
    }
    public double evaluate(int[] board){
        double evaluation = 0;
        for(int square = 0;square < 64;square++){
            int piece = board[square];
            if((piece & 28) == 24)continue;
            int color = ((piece & 3) == 1)?1:-1;

            evaluation += BoardHelper.pieceValue.get(piece & 28) * color;
        }
        return evaluation;
    }
}
