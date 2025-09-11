package com.ssark;

import java.lang.Math;
import java.util.ArrayList;
import java.lang.Character;
public class BoardHelper{
    public BoardHelper(){
        preCompMoveData();
    }
    public static int[] createBoard(int[] board){
        int[] newBoard = board.clone();
        return newBoard;
    }
    public static int[] createBoard(){
        int[] board = new int[64];
        return board;
    }
    public static int[] createBoard(String fen){
        int[] board = new int[64];
        String onlyFen = fen.split(" ")[0];
        char[] parsedFen = onlyFen.toCharArray();
        int i = 63;
        for(char item: parsedFen){
            if(item == '/')continue;
            if(Character.isDigit(item)){
                int parsedInt = item - 48;
                i-=parsedInt;
            }else{
                boolean isWhite = Character.isUpperCase(item);
                char lowerItem = Character.toLowerCase(item);
                switch(lowerItem){
                    case 'p'://pawn
                        board[i] = ((isWhite)?1:2) | 4;
                        break;
                    case 'b'://bishop
                        board[i] = ((isWhite)?1:2) | 8;
                        break;
                    case 'n'://knight
                        board[i] = ((isWhite)?1:2) | 12;
                        break;
                    case 'r'://rook
                        board[i] = ((isWhite)?1:2) | 16;
                        break;
                    case 'q'://queen
                        board[i] = ((isWhite)?1:2) | 20;
                        break;
                    case 'k'://knight
                        board[i] = ((isWhite)?1:2) | 24;
                        break;
                }
                i--;
            }
        }

        return board;
    }
    private static int[] directionalOffsets = {8,-8,-1,1,7,-7,9,-9};
    private static int[][] squaresToEdge = new int[64][8];
    private static int[] knightOffsets = {15,17,-15,-17,6,-10,-6,10};
    private static boolean[][] knightEdgeCheck = new boolean[64][8];
    public static void preCompMoveData(){
        long startTime = System.nanoTime();
        for(int i = 0; i < 64; i++){
            int file = i % 8;
            int rank = i / 8;
            //sliding piece 
            int north = 7 - rank;
            int south = rank;
            int west = file;
            int east = 7 - file;
            


            squaresToEdge[i] = new int[]{
                north,
                south,
                west,
                east,
                Math.min(north,west),
                Math.min(south,east),
                Math.min(north,east),
                Math.min(south,west)
            };
            //horse
            boolean topLeft = true;
            boolean topRight = true;
            boolean bottomRight = true;
            boolean bottomLeft = true;
            boolean leftTop = true;
            boolean leftBottom = true;
            boolean rightBottom = true;
            boolean rightTop = true;
            if(file-2 < 0){
                leftTop = false;
                leftBottom = false;
                if(file-1 < 0){
                    topLeft = false;
                    bottomLeft = false;
                }
            }
            if(file+2 > 7){
                rightTop = false;
                rightBottom = false;
                if(file+1 > 7){
                    topRight = false;
                    bottomRight = false;
                }
            }
            if(rank+2 > 7){
                topLeft = false;
                topRight = false;
                if(rank+1 > 7){
                    leftTop = false;
                    rightTop = false;
                }
            }
            if(rank-2 < 0){
                bottomRight = false;
                bottomLeft = false;
                if(rank-1 < 0){
                    rightBottom = false;
                    leftBottom = false;
                }
            }
            knightEdgeCheck[i] = new boolean[]{
                topLeft,
                topRight,
                bottomRight,
                bottomLeft,
                leftTop,
                leftBottom,
                rightBottom,
                rightTop
            };
        }
        long endTime = System.nanoTime();
        
        System.out.println("Pre-Computed move data in " + (endTime-startTime)/1_000_000.0 + "ms");
        // System.out.println(Arrays.toString(knightEdgeCheck[0]));
    }
    public static ArrayList<Move> findLegalMoves(int[] board,int colorToMove){
        ArrayList<Move> legalMoves = new ArrayList<Move>();
        for(int i=0;i<board.length;i++){
            //Loops through every square on the board
            int squareValue = board[i];
            //Checks if square contains a piece
            if(squareValue == 0)continue;

            int color = ((squareValue & 3) == 1)?1:-1;//11000 = 1
            if(color != colorToMove)continue;

            int file = i % 8;
            int rank = i / 8;

            int piece = squareValue & 28;//00111 = 28
            switch(piece){
                case 4://00100 = Pawn
                    //if there is no piece in front of you && you are not on the last rank
                    if(board[i+(8*color)] <= 0 && rank != ((color == 1)?7:0)){
                        legalMoves.add(new Move(i,i+(8*color)));
                        if(rank == ((color == 1)?1:6) && board[i+(16*color)] <= 0){
                            legalMoves.add(new Move(i,i+(16*color)));
                        }
                    }
                    //capture and en-passant
                    //capture left
                    if(file != 0 && (Math.abs(board[i+((color == 1)?7:-9)]) & 3) != (squareValue&3) && (Math.abs(board[i+((color == 1)?7:-9)]) & 3) != 0){
                        boolean enPassant = false;
                        if(board[i+((color == 1)?7:-9)] < 0)enPassant = true;
                        legalMoves.add(new Move(i,i+((color == 1)?7:-9),enPassant));
                    }
                    //capture right
                    if(file != 7 && (Math.abs(board[i+((color == 1)?9:-7)]) & 3) != (squareValue&3) && (Math.abs(board[i+((color == 1)?9:-7)]) & 3) != 0){
                        boolean enPassant = false;
                        if(board[i+((color == 1)?9:-7)] < 0)enPassant = true;
                        legalMoves.add(new Move(i,i+((color == 1)?9:-7),enPassant));
                    }
                    break;
                case 8://00010 = Bishop
                    legalMoves.addAll(generateSlidingMoves(i, squareValue, board));
                    break;
                case 12://00110 = Knight
                    legalMoves.addAll(generateKnightMoves(i, squareValue, board));
                    break;
                case 16://00001 = Rook
                    legalMoves.addAll(generateSlidingMoves(i, squareValue, board));
                    break;
                case 20://00101 = Queen
                    legalMoves.addAll(generateSlidingMoves(i, squareValue, board));
                    break;
                case 24://00111 = King
                    break;
            }
            
            
            
            
        }
        return legalMoves;
        
    }
    public static long generateAttackedPositions(int[] board,int colorToMove){
        long bitmap = 0;
        for(int i=0;i<board.length;i++){
            //Loops through every square on the board
            int squareValue = board[i];
            //Checks if square contains a piece
            if(squareValue == 0)continue;

            int color = ((squareValue & 3) == 1)?1:-1;//11000 = 1
            if(color == colorToMove)continue;

            int file = i % 8;

            int piece = squareValue & 28;//00111 = 28
            switch(piece){
                case 4://00100 = Pawn
                    //capture left
                    if(file != 0){
                        bitmap = bitmap | (long)Math.pow(2,i+((color == 1)?7:-9));
                    }
                    //capture right
                    if(file != 7){
                        bitmap = bitmap | (long)Math.pow(2,i+((color == 1)?9:-7));
                    }
                    break;
                case 8://00010 = Bishop
                    bitmap = bitmap | generateSlidingMoveBitmap(i, squareValue, board);
                    break;
                case 12://00110 = Knight
                    bitmap = bitmap | generateKnightMovesBitmap(i, squareValue, board);
                    break;
                case 16://00001 = Rook
                    bitmap = bitmap | generateSlidingMoveBitmap(i, squareValue, board);
                    break;
                case 20://00101 = Queen
                    bitmap = bitmap | generateSlidingMoveBitmap(i, squareValue, board);
                    break;
                case 24://00111 = King
                    bitmap = bitmap | generateSlidingMoveBitmap(i, squareValue, board);
                    break;
            }
        
        }
        return bitmap;
    }
    private static ArrayList<Move> generateSlidingMoves(int square, int piece, int[] board){
        ArrayList<Move> moves = new ArrayList<Move>();
        int startDir = ((piece & 28) == 8)?4:0;
        int endDir = ((piece & 28) == 16)?4:8;

        for(int dir = startDir; dir < endDir; dir++){
            for(int dist = 0;dist < squaresToEdge[square][dir];dist++){
                int target  = square + directionalOffsets[dir] * (dist+1);
                int targetPiece = board[target];
                int targetPieceColor = targetPiece & 3;
                if(targetPieceColor == (piece & 3))break;//break if friendly

                moves.add(new Move(square,target));
                if((targetPiece & 28) > 0)break;//Stop on capture
            }
        }
        return moves;
    }
    private static long generateSlidingMoveBitmap(int square,int piece,int[] board){
        long map = 0b0L;
        int startDir = ((piece & 28) == 8)?4:0;
        int endDir = ((piece & 28) == 16)?4:8;

        for(int dir = startDir; dir < endDir; dir++){
            for(int dist = 0;dist < squaresToEdge[square][dir];dist++){
                if((piece & 28)==24 && dist > 0)break;
                int target  = square + directionalOffsets[dir] * (dist+1);
                int targetPiece = board[target];
                int targetPieceColor = targetPiece & 3;
                map = map | (long)Math.pow(2,target);
                if(targetPieceColor == (piece & 3))break;
                if((targetPiece & 28) > 0)break;
            }
        }
        return map;
    }
    private static ArrayList<Move> generateKnightMoves(int square, int piece, int[] board){
        ArrayList<Move> moves = new ArrayList<Move>();
        for(int dir = 0;dir<8;dir++){
            if(!knightEdgeCheck[square][dir])continue;
            int target = square + knightOffsets[dir];
            int targetPiece = board[target];
            int targetPieceColor = targetPiece & 3;
            if(targetPieceColor == (piece & 3))break;

            moves.add(new Move(square,target));
        }
        return moves;
    }
    private static long generateKnightMovesBitmap(int square, int piece, int[] board){
        long map = 0b0L;
        for(int dir = 0;dir<8;dir++){
            if(!knightEdgeCheck[square][dir])continue;
            int target = square + knightOffsets[dir];
            map = map | (long)Math.pow(2,target);
        }
        return map;
    }
}
