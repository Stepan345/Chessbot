package com.ssark;
import java.util.ArrayList;

public class Computer {
    public ArrayList<MoveEval> bestMoves = new ArrayList<MoveEval>();
    public MoveEval findBestMove(int[] board,int depth, int colorToMove){
        
        ArrayList<Move> legalMoves = BoardHelper.findLegalMoves(board, colorToMove);
        ArrayList<MoveEval> moveEvals = new ArrayList<MoveEval>();
        
        for(Move move:legalMoves){
            int[] newBoard = BoardHelper.makeMove(board, move);
            if((depth) == 0){
                MoveEval eval = new MoveEval(move,evaluate(newBoard));
                moveEvals.add(eval);
            }else {
                MoveEval bestMove = findBestMove(newBoard, depth-1, colorToMove * -1);
                moveEvals.add(bestMove);
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
        return 0.0;
    }
}
