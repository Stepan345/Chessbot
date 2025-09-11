package com.ssark;
public class Move{
    private int startSquare;
    private int endSquare;
    private boolean enPassant = false;
    private boolean castle = false;
    public Move(int startSquare,int endSquare){
        this.startSquare = startSquare;
        this.endSquare = endSquare;
    }
    public Move(int startSquare,int endSquare,boolean enPassant){
        this.startSquare = startSquare;
        this.endSquare = endSquare;
        this.enPassant = enPassant;
    }
    public Move(boolean castle,int king, int rook){
        this.startSquare = king;
        this.endSquare = rook;
        this.castle = true;
    }
    public int getStartSquare(){
        return startSquare;
    }
    public int getEndSquare(){
        return endSquare;
    }
    public boolean isEnPassant(){
        return enPassant;
    }
    public boolean isCastle(){
        return castle;
    }
}