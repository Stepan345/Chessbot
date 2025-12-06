package com.ssark;
import java.util.ArrayList;
import java.lang.Math;

public class Computer {
    public ArrayList<MoveEval> lastBestMoves = new ArrayList<MoveEval>();
    //public int counter = 0;
    public double timeLimitSeconds = 5.0;
    public long startTime;
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
                if(move.isCapture() && depth == 1) value = findBestMove(newBoard, depth-1, -1, alpha, beta);
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
                if(move.isCapture() && depth == 1) value = findBestMove(newBoard, depth-1, 1, alpha, beta);
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
    private static final double[] knightWeights = new double[]{
        0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
        0.8,0.9,0.9,0.9,0.9,0.9,0.9,0.8,
        0.8,0.9,1.0,1.0,1.0,1.0,0.9,0.8,
        0.8,0.9,1.0,1.1,1.1,1.0,0.9,0.8,
        0.8,0.9,1.0,1.1,1.1,1.0,0.9,0.8,
        0.8,0.9,1.0,1.0,1.0,1.0,0.9,0.8,
        0.8,0.9,0.9,0.9,0.9,0.9,0.9,0.8,
        0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8
    };
    private static final double[] bishopWeights = new double[]{
        0.9,0.8,0.8,0.8,0.8,0.8,0.8,0.9,
        0.8,1.0,0.9,0.9,0.9,0.9,0.9,0.8,
        0.8,0.9,1.0,0.9,0.9,1.0,0.9,0.8,
        0.8,0.9,0.9,1.1,1.1,0.9,0.9,0.8,
        0.8,0.9,0.9,1.1,1.1,0.9,0.9,0.8,
        0.8,0.9,1.0,0.9,0.9,1.0,0.9,0.8,
        0.8,0.9,0.8,0.9,0.9,0.9,1.0,0.8,
        0.9,0.8,0.8,0.8,0.8,0.8,0.8,0.9
    };
    private static final double[] pawnWeightsEarly = new double[]{
        0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,
        1.4,1.0,1.0,1.0,1.0,2.2,1.0,1.4,
        1.4,0.9,0.9,1.2,1.2,0.9,1.4,1.4,
        0.9,0.9,0.9,2.2,2.2,0.9,0.9,0.9,
        0.9,0.9,0.9,0.9,0.9,0.9,0.9,0.9,
        0.9,0.9,0.9,0.9,0.9,0.9,0.9,0.9,
        0.9,0.9,0.9,0.9,0.9,0.9,0.9,0.9,
        0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0
    };
    private static final double[] pawnWeightsLate = new double[]{
        0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,
        0.9,0.9,0.9,0.9,0.9,0.9,0.9,0.9,
        0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
        0.7,0.7,0.7,0.7,0.7,0.7,0.7,0.7,
        0.6,0.6,0.6,0.6,0.6,0.6,0.6,0.6,
        1.1,1.1,1.1,1.1,1.1,1.1,1.1,1.1,
        1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,
        0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0
    };
    private static final double[] rookWeights = new double[]{
        0.9,0.8,0.9,1.2,1.1,1.2,0.8,0.9,
        0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
        0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
        0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
        0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
        0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
        1.1,1.1,1.1,1.1,1.1,1.1,1.1,1.1,
        1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0
    };
    private static final double[] queenWeights = new double[]{
        0.8,0.8,0.8,1.0,0.9,0.8,0.8,0.8,
        0.8,0.9,0.9,1.0,1.0,0.9,0.9,0.8,
        0.8,0.9,1.0,1.0,1.0,1.0,0.9,0.8,
        0.8,0.9,1.0,1.0,1.0,1.0,0.9,0.8,
        0.8,0.9,1.0,1.0,1.0,1.0,0.9,0.8,
        0.8,0.9,1.0,1.0,1.0,1.0,0.9,0.8,
        0.8,0.9,0.9,1.0,1.0,0.9,0.9,0.8,
        0.8,0.8,0.8,1.0,0.9,0.8,0.8,0.8
    };
    private static final double[] kingWeightsAbleToCastle = new double[]{
        1.0,1.0,1.0,0.7,1.0,0.7,1.0,1.0,
        0.7,0.7,0.7,0.7,0.7,0.7,0.7,0.7,
        0.7,0.7,0.7,0.7,0.7,0.7,0.7,0.7,
        0.7,0.7,0.7,0.7,0.7,0.7,0.7,0.7,
        0.7,0.7,0.7,0.7,0.7,0.7,0.7,0.7,
        0.7,0.7,0.7,0.7,0.7,0.7,0.7,0.7,
        0.7,0.7,0.7,0.7,0.7,0.7,0.7,0.7,
        0.7,0.7,0.7,0.7,0.7,0.7,0.7,0.7
    };
    private static final double[] kingWeightsUnableToCastle = new double[]{
        1.0,1.0,1.1,1.0,1.0,1.0,1.1,1.0,
        0.7,0.7,0.7,0.7,0.7,0.7,0.7,0.7,
        0.7,0.7,0.7,0.7,0.7,0.7,0.7,0.7,
        0.7,0.7,0.7,0.7,0.7,0.7,0.7,0.7,
        0.7,0.7,0.7,0.7,0.7,0.7,0.7,0.7,
        0.7,0.7,0.7,0.7,0.7,0.7,0.7,0.7,
        0.7,0.7,0.7,0.7,0.7,0.7,0.7,0.7,
        0.7,0.7,0.7,0.7,0.7,0.7,0.7,0.7
    };
    private static final double[] kingWeightsEndgame = new double[]{
        0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
        0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
        0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
        0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
        0.9,0.9,0.9,0.9,0.9,0.9,0.9,0.9,
        1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,
        1.1,1.1,1.1,1.1,1.1,1.1,1.1,1.1,
        1.1,1.1,1.1,1.1,1.1,1.1,1.1,1.1
    };
    public static double evaluate(int[] board){
        double evaluation = 0;
        for(int square = 0;square < 64;square++){
            int piece = board[square];
            if(piece <= 0)continue;
            int pieceType = piece & 28;
            if((piece & 28) == 24)continue;
            int color = ((piece & 3) == 1)?1:-1;
            int rank = square / 8;
            int file = square % 8;
            int lookupSquare;
            switch(pieceType){
                case 4:
                    double value = BoardHelper.pieceValue.get(pieceType);
                    if(color == -1){
                        lookupSquare = (7-rank)*8 + file;
                        double gameProgress = BoardHelper.countPieces(board)/32.0;
                        double[] lookupTable = new double[64];
                        for(int i = 0; i < 64; i++){
                            //interpolate between early and late game weights based on game progress
                            lookupTable[i] = (pawnWeightsEarly[i] * (gameProgress)) + (pawnWeightsLate[i] * (1-gameProgress));
                        }
                        evaluation += color * value * lookupTable[lookupSquare];
                    }else{
                        lookupSquare = square;
                        double gameProgress = BoardHelper.countPieces(board)/32.0;
                        double[] lookupTable = new double[64];
                        for(int i = 0; i < 64; i++){
                            //interpolate between early and late game weights based on game progress
                            lookupTable[i] = (pawnWeightsEarly[i] * (gameProgress)) + (pawnWeightsLate[i] * (1-gameProgress));
                        }
                        evaluation += color * value * lookupTable[lookupSquare];
                    }
                    break;
                case 8:
                    evaluation += color * BoardHelper.pieceValue.get(pieceType) * knightWeights[square];
                    break;
                case 12:
                    evaluation += color * BoardHelper.pieceValue.get(pieceType) * bishopWeights[square];
                    break;
                case 16:
                    //mirror the rank for black pieces
                    lookupSquare = ((color == 1)?(7-rank):rank)*8 + file;
                    evaluation += color * BoardHelper.pieceValue.get(pieceType) * rookWeights[lookupSquare];
                    break;
                case 20:
                    evaluation += color * BoardHelper.pieceValue.get(pieceType) * queenWeights[square];
                    break;
                case 24:
                    //mirror the rank for black pieces
                    lookupSquare = ((color == 1)?(7-rank):rank)*8 + file;
                    boolean canCastle = (piece & 32) == 0;
                    double gameProgress = BoardHelper.countPieces(board)/32.0;
                    double[] lookupTable = new double[64];
                    for(int i = 0; i < 64; i++){
                        //interpolate between early and late game weights based on game progress
                        lookupTable[i] = (((canCastle)?kingWeightsAbleToCastle[i]:kingWeightsUnableToCastle[i]) * (gameProgress)) + (kingWeightsEndgame[i] * (1-gameProgress));
                    }
                    evaluation += color * BoardHelper.pieceValue.get(pieceType) * lookupTable[lookupSquare];
                    break;
            }
        }
        return evaluation;
    }
}
