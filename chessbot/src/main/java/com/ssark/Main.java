package com.ssark;
import java.util.ArrayList;
import java.util.Arrays;
public class Main {
    
    public static void main(String[] args) {
        BoardHelper.preCompMoveData();
        createBoard_TEST();
        findLegalMoves_TEST();

        //findBestMove();
    }
    private static void createBoard_TEST(){
        boolean[] out = new boolean[3];
        //perform test
        int[] newBoard = BoardHelper.createBoard();
        newBoard[0] = 5; //set the first square to a white pawn
        int[] copyBoard = BoardHelper.createBoard(newBoard);
        out[0] = Arrays.equals(newBoard,copyBoard); //test0
        
        copyBoard[0] = 6;
        out[1] = !Arrays.equals(newBoard,copyBoard);//test1
        
        int[] fenBoard = BoardHelper.createBoard("8/8/8/8/8/8/8/p7 w - - 0 1");
        out[2] = Arrays.equals(copyBoard,fenBoard);

        System.out.println("createBoard test0 " + ((out[0])?"Passed":"Failed"));
        System.out.println("createBoard test1 " + ((out[1])?"Passed":"Failed"));
        System.out.println("createBoard test2 " + ((out[2])?"Passed":"Failed"));

    }
    private static void findLegalMoves_TEST(){
        boolean[] out = new boolean[1];
        
        int[] board = BoardHelper.createBoard("4k3/8/8/3pP3/8/8/8/4K3");
        //System.out.println(BoardHelper.generateAttackedPositions(board, 1));
        long startTime = System.nanoTime();
        
        ArrayList<Move> moves = BoardHelper.findLegalMoves(board, -1);
        for(Move move:moves){
            System.out.println("From: "+move.getStartSquare()+" To: "+move.getEndSquare());
            System.out.println(BoardHelper.boardToFen(BoardHelper.makeMove(board, move)));
        }
        // for(int i = 0; i < 1000;i++){
        //     BoardHelper.findLegalMoves(board, 1);
        // }
        long endTime = System.nanoTime();
        System.out.println(BoardHelper.boardToFen(board));
        out[0] = (moves.size() == 16);
        System.out.println("findLegalMoves test0 " + ((out[0])?"Passed":"Failed"));
        System.out.println("Found " + moves.size() + "/" + "16 legal moves");
        System.out.println((endTime-startTime)/1_000_000.0 + "ms");
    }
    private static void findBestMove(){
        Computer comp1 = new Computer();
        Computer comp2 = new Computer();
        int[] board1 = BoardHelper.createBoard("rnbkqbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBKQBNR");
        long startTime = System.nanoTime();
        MoveEval bestMove = comp1.findBestMove(board1, 4, 1,new Move(0, 0));
        long endTime = System.nanoTime();
        

        for (int i = 0; i < 20; i++) {
            MoveEval bestMove1 = comp1.findBestMove(board1, 4, 1, null);
            board1 = BoardHelper.makeMove(board1, bestMove1.move);
            MoveEval bestMove2 = comp2.findBestMove(board1, 4, -1, null);
            board1 = BoardHelper.makeMove(board1, bestMove1.move);
            System.out.println(bestMove1.move.getStartSquare());
        }
        System.out.println(bestMove.move.getStartSquare());
        //System.out.println(comp1.counter);
        System.out.println((endTime-startTime)/1_000_000.0 + "ms");
    }
}

