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
    public String getNotation(int[] board){
        //System.out.println(this.startSquare+" "+this.endSquare);
        if(this.castle){
            if(Math.abs(this.startSquare-this.endSquare)==2)return "O-O";
            if(Math.abs(this.startSquare-this.endSquare)==3)return "O-O-O";
            return "Invalid Castle";
        }
        String out = "";
        switch(board[this.startSquare]&28){
            case 4:
                break;
            case 8:
                out+="B";
                break;
            case 12:
                out+="N";
                break;
            case 16:
                out+="R";
                break;
            case 20:
                out+="Q";
                break;
            case 24:
                out+="K";
                break;
            default:
                return ""+(board[this.startSquare]&28);
        }
        
        int column = this.startSquare%8;
        switch (column) {
            case 0:
                out+="a";
                break;
            case 1:
                out+="b";
                break;
            case 2:
                out+="c";
                break;
            case 3:
                out+="d";
                break;
            case 4:
                out+="e";
                break;
            case 5:
                out+="f";
                break;
            case 6:
                out+="g";
                break;
            case 7:
                out+="h";
                break;
            default:
                return column+"";
        }
        //System.out.println(column);
        out+=(this.startSquare/8)+1;
        if(this.isCapture())out+="x";
        int targetColumn = this.endSquare%8;
        switch (targetColumn) {
            case 0:
                out+="a";
                break;
            case 1:
                out+="b";
                break;
            case 2:
                out+="c";
                break;
            case 3:
                out+="d";
                break;
            case 4:
                out+="e";
                break;
            case 5:
                out+="f";
                break;
            case 6:
                out+="g";
                break;
            case 7:
                out+="h";
                break;
            default:
                return targetColumn+"";
        }
        out+=(this.endSquare/8)+1;
        return out;
    }
}