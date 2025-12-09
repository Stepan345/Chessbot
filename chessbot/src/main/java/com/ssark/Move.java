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
        this.capture = (board[endSquare] > 0);
        if(PieceHelper.getType(board[endSquare])==4&&board[endSquare]<0&&PieceHelper.getColor(-board[endSquare]) != PieceHelper.getColor(board[startSquare])){
            this.enPassant = true;
            this.capture = true;
        }
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
    public Move(String uciMove,int[] board){
        this.startSquare = (uciMove.charAt(0)-'a') + (uciMove.charAt(1)-'1')*8;
        this.endSquare = (uciMove.charAt(2)-'a') + (uciMove.charAt(3)-'1')*8;
        this.capture = (board[endSquare] != 0);
        if(uciMove.length() == 5){
            switch(uciMove.charAt(4)){
                case 'b':
                    this.promotion = 8;
                    break;
                case 'n':
                    this.promotion = 12;
                    break;
                case 'r':
                    this.promotion = 16;
                    break;
                case 'q':
                    this.promotion = 20;
                    break;
            }
        }
        if(this.capture && board[this.endSquare]<0 && PieceHelper.getColor(-board[this.endSquare])!=PieceHelper.getColor(board[this.startSquare]))this.enPassant = true;
        if(PieceHelper.getType(board[this.startSquare]) == 24){
            int diff = this.startSquare-this.endSquare;
            //System.out.println("Diff: "+diff);
            if(diff < 0 && Math.abs(diff) == 2){
                this.castle = true;
                this.endSquare++;
            }else if (diff > 0 && Math.abs(diff) == 2){
                this.castle = true;
                this.endSquare-=2;
            }
            //.out.println("StartSquare "+this.startSquare+" EndSquare "+this.endSquare);
        }
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
        return getNotation(board,false);
    }
    public String getNotation(int[] board,boolean uci){
        //System.out.println(this.startSquare+" "+this.endSquare);
        String out = "";
        if(this.castle && !uci){
            if(Math.abs(this.startSquare-this.endSquare)==3)return "O-O";
            if(Math.abs(this.startSquare-this.endSquare)==4)return "O-O-O";
            return "Invalid Castle";
        }
        if(this.castle && uci){
            if(Math.abs(this.startSquare-this.endSquare)==3){//shortcastle
                //System.out.println(startSquare);
                if(PieceHelper.getColor(board[startSquare])==PieceHelper.WHITE){
                    return "e1g1";
                }else{
                    return "e8g8";
                }
            }
            if(Math.abs(this.startSquare-this.endSquare)==4){//longcastle
                if(PieceHelper.getColor(board[startSquare])==PieceHelper.WHITE){
                    return "e1c1";
                }else{
                    return "e8c8";
                }
            }
            return "Invalid Castle";
        }
        
        if(!uci){        
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
        if(this.isCapture() && !uci)out+="x";
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