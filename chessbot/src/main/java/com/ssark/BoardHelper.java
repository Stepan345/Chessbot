package com.ssark;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
public class BoardHelper{
    public BoardHelper(){
        
    }
    public static int[] createBoard(int[] board){
        int[] newBoard = board.clone();
        return newBoard;
    }
    public static int[] createBoard(){
        int[] board = new int[64];
        return board;
    }
    public static ArrayList<Move> findLegalMoves(int[] board,int colorToMove){
        ArrayList<Move> legalMoves = new ArrayList<Move>();
        for(int i=0;i<board.length;i++){
            //Loops through every square on the board
            int squareValue = board[i];
            //Checks if square contains a piece
            if(squareValue == 0)continue;
            int color = squareValue & 1;//1000 = 1
            if(color == colorToMove)continue;
            
            int piece = squareValue & 14;//0111 = 14
            switch(piece){
                case 2://0100 = Pawn
                    if(board[i-8] <= 0)legalMoves.add(new Move(i,i-8));
                    
                    
                    
                    break;
                case 4://0010 = Bishop
                    break;
                case 6://0110 = Knight
                    break;
                case 8://0001 = Rook
                    break;
                case 10://0101 = Queen
                    break;
                case 12://0111 = King
                    break;
            }
            
            
            
            
        }
        return legalMoves;
    }
}