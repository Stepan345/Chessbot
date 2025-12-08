package com.ssark;
import java.util.ArrayList;
import java.util.concurrent.*;

import javax.management.RuntimeErrorException;

import java.lang.Math;


public class Computer {
    // class MoveThread extends Thread{
    //     private int[] board;
    //     private int depthLast;
    //     private int colorToMove;
    //     private double alpha;
    //     private double delta;
    //     public MoveThread(int[] board,int depthLast, int colorToMove, double alpha, double beta){

    //     }
    // }
    public ArrayList<MoveEval> lastBestMoves = new ArrayList<MoveEval>();
    //public int counter = 0;
    public double timeLimitSeconds = 5.0;
    public long startTime;
    public static final ExecutorService executor = Executors.newFixedThreadPool(4);
    public MoveEval findBestMove(int[] board,int depth,int colorToMove){
        var moves = BoardHelper.findLegalMoves(board, colorToMove);
        var tasks = new ArrayList<Future<MoveEval>>();
        var bestMove = new MoveEval(colorToMove*Double.NEGATIVE_INFINITY);
        for(Move move:moves){
            var task = executor.submit(() -> {
                var board1 = BoardHelper.makeMove(board, move);
                return findBestMove(board1, depth-1, colorToMove*-1,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
            });
            tasks.add(task);
        }


        try{
            for(int i = 0;i<tasks.size();i++){
                var task = tasks.get(i);
                var move = task.get();
                if(
                    (colorToMove == 1 && move.evaluation > bestMove.evaluation) ||//white
                    (colorToMove == -1 && move.evaluation < bestMove.evaluation)//black
                ){
                    bestMove = new MoveEval(move,moves.get(i),move.evaluation);
                }
            }
            return bestMove;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    public MoveEval findBestMove(int[] board,int depthLast, int colorToMove, double alpha, double beta){
        int depth = depthLast;
        if(this.startTime != 0 && (System.nanoTime() - this.startTime) / 1_000_000_000.0 > this.timeLimitSeconds){
            return new MoveEval(0);
        }
        if(depthLast <= 0){
            //if(!lastMove.isCapture() || depth < -5) 
            return new MoveEval(evaluate(board));
        }
        ArrayList<MoveEval> bestMoves = new ArrayList<MoveEval>();
        ArrayList<Move> legalMoves = BoardHelper.findLegalMoves(board, colorToMove);
        if(legalMoves.size() == 0){
            long checkMap = BoardHelper.generateAttackedPositions(board, colorToMove);
            long kingMap = 1L << BoardHelper.getKingPosition(board,colorToMove);
            if((checkMap | kingMap) == 0)return new MoveEval(0);
            return new MoveEval(-1_000_000*colorToMove);
        }
        MoveEval bestMove;
        if(colorToMove == 1){//white
            bestMove = new MoveEval(Double.NEGATIVE_INFINITY);
            for(Move move:legalMoves){

                int[] newBoard = BoardHelper.makeMove(board, move);
                MoveEval value;
                if(move.isCapture() && depth == 1) value = findBestMove(newBoard, depth, -1, alpha, beta);
                else value = findBestMove(newBoard, depth-1, -1, alpha, beta);
                bestMoves.add(new MoveEval(value, move, value.evaluation));
                if(value.evaluation > bestMove.evaluation){
                    bestMove = new MoveEval(value, move, value.evaluation);
                }
                alpha = Math.max(alpha, value/*bestMove*/.evaluation);
                if(beta <= alpha/*+ 1e-6*/){
                    break;
                }
            }
        }else{//black
            bestMove = new MoveEval(Double.POSITIVE_INFINITY);
            for(Move move:legalMoves){
                int[] newBoard = BoardHelper.makeMove(board, move);
                MoveEval value;
                if(move.isCapture() && depth == 1) value = findBestMove(newBoard, depth, 1, alpha, beta);
                else value = findBestMove(newBoard, depth-1, 1, alpha, beta);
                
                bestMoves.add(new MoveEval(value, move, value.evaluation));
                if(value.evaluation < bestMove.evaluation){
                    bestMove = new MoveEval(value, move, value.evaluation);
                }
                beta = Math.min(beta, value/*bestMove*/.evaluation);
                if(beta <= alpha/*+ 1e-6*/){
                    break;
                }
            }
        }
        lastBestMoves = bestMoves;
        return bestMove; 
    }
    private static final int[] knightWeights = new int[]{
        -50, -40, -30, -30, -30, -30, -40, -50,
        -40, -20, 0, 0, 0, 0, -20, -40,
        -30, 0, 10, 15, 15, 10, 0, -30,
        -30, 5, 15, 20, 20, 15, 5, -30,
        -30, 0, 15, 20, 20, 15, 0, -30,
        -30, 5, 10, 15, 15, 10, 5, -30,
        -40, -20, 0, 5, 5, 0, -20, -40,
        -50, -40, -30, -30, -30, -30, -40, -50
    };
    private static final int[] bishopWeights = new int[]{
        -20, -10, -10, -10, -10, -10, -10, -20,
        -10, 0, 0, 0, 0, 0, 0, -10,
        -10, 10, 10, 10, 10, 10, 10, -10,
        -10, 0, 10, 10, 10, 10, 0, -10,
        -10, 0, 5, 10, 10, 5, 0, -10,
        -10, 5, 5, 10, 10, 5, 5, -10,
        -10, 5, 0, 0, 0, 0, 5, -10,
        -20, -10, -10, -10, -10, -10, -10, -20
    };
    private static final int[] pawnWeightsEarly = new int[]{
        0,  0,  0,  0,  0,  0,  0,  0,
        5, 10, 10, -20,-20, 10, 10, 5,
        5,-5,-10,  0,  0,-10, -5, 5,
        0, 0, 0, 20, 20, 0, 0, 0,
        5, 5,10,25,25,10, 5, 5,
        10,10,20,30,30,20,10,10,
        50,50,50,50,50,50,50,50,
        0, 0, 0, 0, 0, 0, 0, 0
    };
    private static final int[] pawnWeightsLate = new int[]{
        0, 0, 0, 0, 0, 0, 0, 0,
        -10,-10,-10,-10,-10,-10,-10,-10,
        -5,-5,-5,-5,-5,-5,-5,-5,
        0,0,0,0,0,0,0,0,
        5,5,5,5,5,5,5,5,
        10,10,10,10,10,10,10,10,
        15,15,15,15,15,15,15,15,
        0, 0, 0, 0, 0, 0, 0, 0
    };
    private static final int[] rookWeights = new int[]{
        0,0,0,0,0,0,0,0,
        -5,0,0,0,0,0,0,-5,
        -5,0,0,0,0,0,0,-5,
        -5,0,0,0,0,0,0,-5,
        -5,0,0,0,0,0,0,-5,
        -5,0,0,0,0,0,0,-5,
        5,10,10,10,10,10,10,5,
        0,0,0,5,5,0,0,0
    };
    private static final int[] queenWeights = new int[]{
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10, 0, 0, 0, 0, 0, 0,-10,
        -10, 0, 5, 5, 5, 5, 0,-10,
        0, 0, 5, 5, 5, 5, 0, -5,
        -5, 0, 5, 5, 5, 5, 0, -5,
        -10, 5, 5, 5, 5, 5, 0,-10,
        -10, 0, 5, 0, 0, 0, 0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    };
    private static final int[] kingWeightsAbleToCastle = new int[]{
        20,30,10,0,0,10,30,20,
        20,20,-20,-20,-20,-20,20,20,
        -10,-20,-20,-20,-20,-20,-20,-10,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30
    };
    private static final int[] kingWeightsEndgame = new int[]{
        -50,-30,-30,-30,-30,-30,-30,-50,
        -30,-20,-10,0,0,-10,-20,-30,
        -30,-10,20,30,30,20,-10,-30,
        -30,-10,30,40,40,30,-10,-30,
        -30,-10,30,40,40,30,-10,-30,
        -30,-10,20,30,30,20,-10,-30,
        -30,-30,0,0,0,0,-30,-30,
        -50,-30,-30,-30,-30,-30,-30,-50
    };
    private static final int BISHOP_PAIR_BONUS = 50;
    public static double evaluate(int[] board){
        return evaluate(board, false);
    }
    public static double evaluate(int[] board,boolean doDebugPrint){
        double evaluation = 0;
        int[][] kingPositions = {{0,0},{0,0}};//white, black
        int[] queens = {0,0};//white, black
        int[] rooks = {0,0};//white, black
        int[] knights = {0,0};//white, black
        int[] bishops = {0,0};//white, black 
        for(int square = 0;square < 64;square++){
            int piece = board[square];
            if(piece <= 0){
                if(doDebugPrint)System.out.print("0 ");
                if(doDebugPrint && square % 8 == 7)System.out.print("\n");
                continue;
            }
            int pieceType = piece & 28;
            //if((piece & 28) == 24)continue;
            int color = ((piece & 3) == 1)?1:-1;
            int rank = square / 8;
            int file = square % 8;
            int lookupSquare;
            switch(pieceType){
                case 4:
                    double value = BoardHelper.pieceValue.get(pieceType);
                    if(color == -1){
                        lookupSquare = (7-rank)*8 + file;
                        //double gameProgress = BoardHelper.countPieces(board)/32.0;
                        
                        if(doDebugPrint)System.out.print(color * (value + pawnWeightsEarly[lookupSquare])+" ");
                        evaluation += color * (value + pawnWeightsEarly[lookupSquare]);
                    }else{
                        lookupSquare = square;
                        //double gameProgress = BoardHelper.countPieces(board)/32.0;
                        
                        if(doDebugPrint)System.out.print(color * (value + pawnWeightsEarly[lookupSquare])+" ");
                        evaluation += color * (value + pawnWeightsEarly[lookupSquare]);
                    }
                    break;
                case 8:
                    bishops[color == 1?0:1]++;
                    if(color == -1){
                        lookupSquare = (7-rank)*8 + file;
                    }else {
                        lookupSquare = square;
                    }
                    if(doDebugPrint)System.out.print(color * (BoardHelper.pieceValue.get(pieceType) + bishopWeights[lookupSquare])+" ");
                    evaluation += color * (BoardHelper.pieceValue.get(pieceType) + bishopWeights[lookupSquare]);
                    break;
                case 12:
                    knights[color == 1?0:1]++;
                    if(color == -1){
                        lookupSquare = (7-rank)*8 + file;
                    }else {
                        lookupSquare = square;
                    }
                    if(doDebugPrint)System.out.print(color * (BoardHelper.pieceValue.get(pieceType) + knightWeights[lookupSquare])+" ");
                    evaluation += color * (BoardHelper.pieceValue.get(pieceType) + knightWeights[lookupSquare]);
                    break;
                case 16:
                    rooks[color == 1?0:1]++;
                    //mirror the rank for black pieces
                    if(color == -1){
                        lookupSquare = (7-rank)*8 + file;
                    }else {
                        lookupSquare = square;
                    }
                    if(doDebugPrint)System.out.print(color * (BoardHelper.pieceValue.get(pieceType) + rookWeights[lookupSquare])+" ");
                    evaluation += color * (BoardHelper.pieceValue.get(pieceType) + rookWeights[lookupSquare]);
                    break;
                case 20:
                    queens[color == 1?0:1]++;
                    if(color == -1){
                        lookupSquare = (7-rank)*8 + file;
                    }else {
                        lookupSquare = square;
                    }
                    if(doDebugPrint)System.out.print(color * (BoardHelper.pieceValue.get(pieceType) + queenWeights[lookupSquare])+" ");
                    evaluation += color * (BoardHelper.pieceValue.get(pieceType) + queenWeights[lookupSquare]);
                    break;
                case 24:
                    if(doDebugPrint)System.out.print("K ");
                    kingPositions[(color == 1)?0:1] = new int[]{rank, file};
                    break;
            }
            if(doDebugPrint && file == 7)System.out.print("\n");
        }
        boolean isLateGame = false;
        if(queens[0] == 0 && queens[1] == 0 && (knights[0] + knights[1] + bishops[0] + bishops[1]) <= 3 && (rooks[0] + rooks[1]) <= 2){
            isLateGame = true;
        }else if(queens[0] == 1 && (rooks[0] + knights[0] +  bishops[0]) <= 1){
            isLateGame = true;
        }else if(queens[1] == 1 && (rooks[1] + knights[1] +  bishops[1]) <= 1){
            isLateGame = true;
        }
        //white king evaluation
        int lookupSquare = kingPositions[0][0]*8 + kingPositions[0][1];
        if(!isLateGame){
            if(doDebugPrint)System.out.print((BoardHelper.pieceValue.get(24) + kingWeightsAbleToCastle[lookupSquare])+" it's not late game\n");
            evaluation += (BoardHelper.pieceValue.get(24) + kingWeightsAbleToCastle[lookupSquare]);
        }else {
            if(doDebugPrint)System.out.print((BoardHelper.pieceValue.get(24) + kingWeightsEndgame[lookupSquare])+" it is late game\n");
            evaluation += (BoardHelper.pieceValue.get(24) + kingWeightsEndgame[lookupSquare]);
        }
        //black king evaluation
        lookupSquare = (7 - kingPositions[1][0])*8 + kingPositions[1][1];
        if(!isLateGame){
            if(doDebugPrint)System.out.print(-(BoardHelper.pieceValue.get(24) + kingWeightsAbleToCastle[lookupSquare])+" it's not late game\n");
            evaluation -= (BoardHelper.pieceValue.get(24) + kingWeightsAbleToCastle[lookupSquare]);
        }else {
            if(doDebugPrint)System.out.print(-(BoardHelper.pieceValue.get(24) + kingWeightsEndgame[lookupSquare])+" it is late game\n");
            evaluation -= (BoardHelper.pieceValue.get(24) + kingWeightsEndgame[lookupSquare]);
        }
        //bishop pair bonus
        if(bishops[0]>=2)evaluation+=BISHOP_PAIR_BONUS;
        if(bishops[1]>=2)evaluation-=BISHOP_PAIR_BONUS;
        return evaluation;
    }
}
