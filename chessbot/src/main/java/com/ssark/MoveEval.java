package com.ssark;
import java.util.ArrayList;
public class MoveEval {
    public Move move;
    public double evaluation;
    public ArrayList<Move> line = new ArrayList<Move>();
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
        this.line.add(0, move);
        this.evaluation = evaluation;
        this.move = move;
    }

}