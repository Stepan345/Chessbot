package com.ssark;
import java.util.ArrayList;

public class MoveEval {
    public Move move;
    public double evaluation;
    public ArrayList<Move> line = new ArrayList<>();
    public MoveEval(Move move,double evaluation){
        this.move = move;
        this.evaluation = evaluation;
        this.line.add(move);
    }
    public MoveEval(MoveEval parent, Move move, double evaluation){
        this.move = move;
        this.evaluation = evaluation;
        this.line.add(move);
        this.line.addAll(parent.line);
    }
    public MoveEval(double evaluation){
        this.evaluation = evaluation;
    }
    public void addMoveToLine(Move move,double evaluation){
        this.line.addFirst(move);
        this.evaluation = evaluation;
        this.move = move;
    }
    @Override
    public String toString(){
        if(Math.abs(evaluation) >= 1_000_000){//checkmate
            return "M"+(line.size()+1)/2;
        }
        return ""+this.evaluation;
    }

}