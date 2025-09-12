package com.ssark;
import java.util.ArrayList;
/**
 * Main class of the Java program my name stephan i stinky++.
 */
import java.util.Arrays;
public class Main {
    
    public static void main(String[] args) {
        BoardHelper.preCompMoveData();
        createBoard_TEST();
        findLegalMoves_TEST();

        findBestMove();
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
        
        int[] fenBoard = BoardHelper.createBoard("8/8/8/8/8/8/8/7p w - - 0 1");
        out[2] = Arrays.equals(copyBoard,fenBoard);

        System.out.println("createBoard test0 " + ((out[0])?"Passed":"Failed"));
        System.out.println("createBoard test1 " + ((out[1])?"Passed":"Failed"));
        System.out.println("createBoard test2 " + ((out[2])?"Passed":"Failed"));

    }
    private static void findLegalMoves_TEST(){
        boolean[] out = new boolean[1];
        
        int[] board = BoardHelper.createBoard("rnbkqbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBKQBNR");
        //System.out.println(BoardHelper.generateAttackedPositions(board, 1));
        long startTime = System.nanoTime();
        ArrayList<Move> moves = BoardHelper.findLegalMoves(board, -1);
        // for(int i = 0; i < 1000;i++){
        //     BoardHelper.findLegalMoves(board, 1);
        // }
        long endTime = System.nanoTime();
        
        out[0] = (moves.size() == 16);
        System.out.println("findLegalMoves test0 " + ((out[0])?"Passed":"Failed"));
        System.out.println("Found " + moves.size() + "/" + "16 legal moves");
        System.out.println((endTime-startTime)/1_000_000.0 + "ms");
    }
    private static void findBestMove(){
        Computer comp = new Computer();
        int[] board = BoardHelper.createBoard("rnbkqbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBKQBNR");
        System.out.println(comp.findBestMove(board, 3, 1));
    }
}

