package com.ssark;
public class Move{
    private int startSquare;
    private int endSquare;
    private boolean enPassant = false;
    private boolean castle = false;
    private boolean capture = false;
    private int promotion = 0;
    public Move(int startSquare,int endSquare){
        this.startSquare = startSquare;
        this.endSquare = endSquare;
    }
    public Move(int startSquare,int endSquare,int[] board){
        this.capture = (board[endSquare] != 0);
        this.startSquare = startSquare;
        this.endSquare = endSquare;
    }
    public Move(int startSquare,int endSquare,boolean enPassant){
        this.startSquare = startSquare;
        this.endSquare = endSquare;
        this.enPassant = enPassant;
        this.capture = true;
    }
    /**
     * 
     * @param startSquare
     * @param endSquare
     * @param promotion piece code of the piece to promote to \n 8 = bishop, 12 = knight, 16 = rook, 20 = queen
     * @param board
     */
    public Move(int startSquare,int endSquare,int promotion, int[] board){
        this.promotion = promotion;
        this.capture = (board[endSquare] != 0);
        this.startSquare = startSquare;
        this.endSquare = endSquare;
    }
    public Move(boolean castle,int king, int rook){
        this.startSquare = king;
        this.endSquare = rook;
        this.castle = true;
    }
    public Move(Move parent){
        this.startSquare = parent.getStartSquare();
        this.endSquare = parent.getEndSquare();
        this.enPassant = parent.isEnPassant();
        this.castle = parent.isCastle();
        this.capture = parent.isCapture();
        this.promotion = parent.getPromotion();
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
    public boolean isCapture(){
        return capture;
    }
    public int getPromotion(){
        return promotion;
    }
    public boolean isPromotion(){
        return promotion != 0;
    }
}