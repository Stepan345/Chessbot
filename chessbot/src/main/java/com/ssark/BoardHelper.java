package com.ssark;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
public class BoardHelper{
    

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
    public static int[] makeMove(int[] board, Move move){
        int[] newBoard = board.clone();
        int startSquare = move.getStartSquare();
        int endSquare = move.getEndSquare();
        int piece = board[startSquare];
        int color = ((piece & 3)==1)?1:-1;

        if(move.isCastle()){
            int targetSquare = startSquare + (2 * (int)Math.signum(endSquare-startSquare));
            int rookTargetSquare = targetSquare - (1 * (int)Math.signum(endSquare-startSquare));
            
            newBoard[targetSquare] = piece | 32;
            newBoard[startSquare] = 0;
            newBoard[rookTargetSquare] = board[endSquare] | 32;
            newBoard[endSquare] = 0;

            return newBoard;
        }
        if((piece&28) == 4 && Math.abs(endSquare - startSquare) == 16){
            //add phantom pawn
            newBoard[startSquare + (8*color)] = -(4|(piece&3));
        }
        if(move.isEnPassant()){
            newBoard[endSquare - (8*color)] = 0;
        }
        if((piece&28) ==24 || (piece&28) == 16){
            piece |= 32;
        }
        newBoard[startSquare] = 0;
        newBoard[endSquare] = piece;
        return newBoard;
    }
    private static int[] directionalOffsets = {8,-8,-1,1,7,-7,9,-9};
    private static int[][] squaresToEdge = new int[64][8];
    private static int[] knightOffsets = {15,17,-15,-17,6,-10,-6,10};
    private static boolean[][] knightEdgeCheck = new boolean[64][8];
    public static final HashMap<Integer,Integer> pieceValue = new HashMap<Integer,Integer>();
    public static void preCompMoveData(){
        long startTime = System.nanoTime();
        pieceValue.put(0,0);
        pieceValue.put(-4,100);
        pieceValue.put(4,100);
        pieceValue.put(8,250);
        pieceValue.put(12,300);
        pieceValue.put(16,500);
        pieceValue.put(20,900);
        pieceValue.put(24,10000);
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
        ArrayList<Integer> phantomPawnList = new ArrayList<Integer>();
        for(int i=0;i<board.length;i++){
            //Loops through every square on the board
            int squareValue = board[i];
            //Checks if square contains a piece
            if(squareValue == 0)continue;
            if(squareValue < 0)phantomPawnList.add(i);
            int color = ((squareValue & 3) == 1)?1:-1;//11000 = 3
            if(color != colorToMove)continue;

            int piece = squareValue & 28;//00111 = 28
            switch(piece){
                case 4://00100 = Pawn
                    legalMoves.addAll(generatePawnMoves(i,squareValue,board));
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
                case 24://00011 = King
                    legalMoves.addAll(generateKingMoves(i,squareValue,board));
                    break;
            }
            //Sort Moves
            legalMoves.sort((a, b)  -> {

                int valA = 0;
                int valB = 0;

                int capturedPieceValueA = pieceValue.get(board[a.getEndSquare()] & 28);
                int capturedPieceValueB = pieceValue.get(board[b.getEndSquare()] & 28);

                valA += capturedPieceValueA;
                valB += capturedPieceValueB;
                if(a.isCastle()) valA = 25;
                if(b.isCastle()) valB = 25;
                int[] checkTestBoardA = makeMove(board, a);
                int[] checkTestBoardB = makeMove(board, b);

                int kingA = getKingPosition(checkTestBoardA, ((board[a.getStartSquare()] & 3) == 1)?2:1);
                int kingB = getKingPosition(checkTestBoardB, ((board[b.getStartSquare()] & 3) == 1)?2:1);

                if((generateAttackedPositions(checkTestBoardA, colorToMove*-1) & (1L << kingA)) != 0)valA += 75;
                if((generateAttackedPositions(checkTestBoardB, colorToMove*-1) & (1L << kingB)) != 0)valB += 75;

                
                return valB - valA;
            });
        }
        for (Integer integer : phantomPawnList) {
            board[integer] = 0;
        }
        return legalMoves;   
    }
    public static ArrayList<Move> generatePawnMoves(int square, int piece, int[] board){
        ArrayList<Move> moves = new ArrayList<Move>();

        int file = square % 8;
        int rank = square / 8;
        int color = ((piece & 3) == 1)?1:-1;
        //if there is no piece in front of you && you are not on the last rank
        if(board[square+(8*color)] <= 0 && rank != ((color == 1)?7:0)){
            Move move = new Move(square,square+(8*color));
            long captureMap = generateAttackedPositions(makeMove(board,move),piece & 3);
            long kingMap = 1L << getKingPosition(board,piece & 3);

            if((captureMap & kingMap) == 0){
                moves.add(move);
                if(rank == ((color == 1)?1:6) && board[square+(16*color)] <= 0){
                    moves.add(new Move(square,square+(16*color)));
                }
            }
            
        }
        //capture and en-passant
        //capture left
        if(file != 0 && (Math.abs(board[square+((color == 1)?7:-9)]) & 3) != (piece&3) && (Math.abs(board[square+((color == 1)?7:-9)]) & 3) != 0){
            boolean enPassant = false;
            if(board[square+((color == 1)?7:-9)] < 0)enPassant = true;
            Move move = new Move(square,square+((color == 1)?7:-9),enPassant);
            long captureMap = generateAttackedPositions(makeMove(board,move),piece & 3);
            long kingMap = 1L << getKingPosition(board,piece & 3);

            if((captureMap & kingMap) == 0){
                moves.add(move);
            }
        }
        //capture right
        if(file != 7 && (Math.abs(board[square+((color == 1)?9:-7)]) & 3) != (piece&3) && (Math.abs(board[square+((color == 1)?9:-7)]) & 3) != 0){
            boolean enPassant = false;
            if(board[square+((color == 1)?9:-7)] < 0)enPassant = true;
            Move move = new Move(square,square+((color == 1)?9:-7),enPassant);
            long captureMap = generateAttackedPositions(makeMove(board,move),piece & 3);
            long kingMap = 1L << getKingPosition(board,piece & 3);

            if((captureMap & kingMap) == 0){
                moves.add(move);
            }
        }
        return moves;
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
                        bitmap = bitmap | 1L << i+((color == 1)?7:-9);
                    }
                    //capture right
                    if(file != 7){
                        bitmap = bitmap | 1L << i+((color == 1)?9:-7);
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
    private static ArrayList<Move> generateKingMoves(int square, int piece, int[] board){
        ArrayList<Move> moves = new ArrayList<Move>();
        long checkMap = generateAttackedPositions(board,((piece & 3) == 1)?1:-1);
        for(int dir = 0; dir < 8; dir++){
            if(squaresToEdge[square][dir] == 0)continue;
            int target = square + directionalOffsets[dir];
            if((board[target] & 3) == (piece & 3))continue;

            long kingMap = 1L << target;
            if((kingMap & checkMap) != 0)continue;

            moves.add(new Move(square,target));
            
        }
        if((piece & 32) == 0){
            int rookRight = board[square + 3];
            int rookLeft = board[square - 4];
            long rightCheck = 3L << (((piece & 3)==1)?5:61);
            long leftCheck = 3L << (((piece & 3)==1)?1:57);
            //right castle
            if(
                (rookRight & 28) == 16 && 
                (rookRight & 32) == 0 && 
                (checkMap & rightCheck) == 0 &&
                board[square+1] == 0 &&
                board[square+2] == 0
            ){
                moves.add(new Move(true,square,square+3));
            }
            if(
                (rookLeft & 28) == 16 &&
                (rookLeft & 32)==0 &&
                (checkMap & leftCheck) == 0 &&
                board[square-1] == 0 &&
                board[square-2] == 0 &&
                board[square-3] == 0
            ){
                moves.add(new Move(true,square,square-4));
            }
        }
        return moves;
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

                Move move = new Move(square,target);
                long captureMap = generateAttackedPositions(makeMove(board,move),piece & 3);
                long kingMap = 1L << getKingPosition(board,piece & 3);

                if((captureMap & kingMap) != 0){
                    continue;
                }

                moves.add(move);
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
                map = map | 1L << target;
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
            if(targetPieceColor == (piece & 3))continue;

            Move move = new Move(square,target);
            long captureMap = generateAttackedPositions(makeMove(board,move),piece & 3);
            long kingMap = 1L << getKingPosition(board,piece & 3);

            if((captureMap & kingMap) != 0){
                continue;
            }
            
            moves.add(move);
        }
        return moves;
    }
    private static int getKingPosition(int[] board, int color){
        for(int i = 0;i<board.length;i++){
            if(board[i] == (24 | color)){
                return i;
            }
        }
        return -1;
    }
    private static long generateKnightMovesBitmap(int square, int piece, int[] board){
        long map = 0b0L;
        for(int dir = 0;dir<8;dir++){
            if(!knightEdgeCheck[square][dir])continue;
            int target = square + knightOffsets[dir];
            map = map | 1L << target;
        }
        return map;
    }
}
