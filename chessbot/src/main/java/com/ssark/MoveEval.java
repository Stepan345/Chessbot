package com.ssark;
import javax.xml.transform.TransformerFactory;
import java.util.ArrayList;

public class MoveEval {
    public Move move;
    public final double evaluation;
    public enum TransFlags {
        EXACT,
        ALPHA,
        BETA
    }
    public TransFlags flag;
    public int depth;
    public final ArrayList<Move> line;
    public MoveEval(Move move,double evaluation){
        this.line = new ArrayList<>();
        this.move = move;
        this.evaluation = evaluation;
        this.line.add(move);
    }
    public MoveEval(MoveEval parent, Move move, double evaluation){
        this.line = new ArrayList<>();
        this.move = move;
        this.evaluation = evaluation;
        this.line.add(move);
        this.line.addAll(parent.line);
    }
    public MoveEval(MoveEval parent, Move move, double evaluation,TransFlags flag){
        this.line = new ArrayList<>();
        this.move = move;
        this.evaluation = evaluation;
        this.line.add(move);
        this.line.addAll(parent.line);
    }
    public MoveEval(MoveEval parent,TransFlags flag,int depth){
        this.evaluation = parent.evaluation;
        this.move = new Move(parent.move);
        this.flag = flag;
        this.depth = depth;
        this.line = new ArrayList<>(parent.line);
    }
    public MoveEval(double evaluation){
        this.line = new ArrayList<>();
        this.evaluation = evaluation;
    }
    public void addMoveToLine(Move move){
        this.line.addFirst(move);
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