package com.ssark;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
public class Main {
    
    public static void main(String[] args) {
        BoardHelper.preCompMoveData();
        createBoard_TEST();
        //findLegalMoves_TEST();
        //findBestMove();
        //cpuVcpu();
        playerVcpu();
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
        
        int[] board = BoardHelper.createBoard("3k2b1/6N1/4x2R/4p1n1/1N1P4/r2X4/1n6/1B2K3");
        //System.out.println(BoardHelper.generateAttackedPositions(board, 1));
        long startTime = System.nanoTime();
        
        ArrayList<Move> moves = BoardHelper.findLegalMoves(board, 1);
        long endTime = System.nanoTime();
        ArrayList<Move> movesBlack = BoardHelper.findLegalMoves(board, -1);
        
        for(Move move:moves){
            System.out.println("From: "+move.getStartSquare()+" To: "+move.getEndSquare());
            System.out.println(BoardHelper.boardToFen(BoardHelper.makeMove(board, move)));
        }
        // for(int i = 0; i < 1000;i++){
        //     BoardHelper.findLegalMoves(board, 1);
        // }
        
        out[0] = (moves.size() == 16);
        System.out.println("findLegalMoves test0 " + ((out[0])?"Passed":"Failed"));
        System.out.println("Found " + moves.size() + " white legal moves");
        System.out.println("Found " + movesBlack.size() + " black legal moves");
        System.out.println((endTime-startTime)/1_000_000.0 + "ms");
    }
    private static void findBestMove(){
        Computer comp1 = new Computer();
        Computer comp2 = new Computer();
        int[] board1 = BoardHelper.createBoard("rnbkqbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBKQBNR");
        long startTime = System.nanoTime();
        MoveEval bestMove = comp1.findBestMove(board1, 4, -1,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
        long endTime = System.nanoTime();
        //System.out.println(comp1.counter);

        for (int i = 0; i < 20; i++) {
            MoveEval bestMove1 = comp1.findBestMove(board1, 4, 1,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
            board1 = BoardHelper.makeMove(board1, bestMove1.move);
            MoveEval bestMove2 = comp2.findBestMove(board1, 4, -1,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
            board1 = BoardHelper.makeMove(board1, bestMove1.move);
            System.out.println(bestMove1.move.getStartSquare());
        }
        System.out.println(bestMove.move.getStartSquare());
        //System.out.println(comp1.counter);
        System.out.println((endTime-startTime)/1_000_000.0 + "ms");
    }
    private static void cpuVcpu(){
        Computer comp1 = new Computer();
        Computer comp2 = new Computer();
        int[] board1 = BoardHelper.createBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        MoveEval bestMove1 = new MoveEval(new Move(0,0),0);
        MoveEval bestMove2 = new MoveEval(new Move(0,0),0);
        long startTime;
        long endTime;
        for (int i = 0; i < 10; i++) {
            System.out.println("White to move");
            startTime = System.nanoTime();
            bestMove1 = comp1.findBestMove(board1, 5, 1,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
            endTime = System.nanoTime();
            board1 = BoardHelper.makeMove(board1, bestMove1.move);
            System.out.println(BoardHelper.boardToFen(board1));
            System.out.println(bestMove1.move.getStartSquare() + " to " + bestMove1.move.getEndSquare() + " eval: " + bestMove1.evaluation);
            System.out.println("Board Eval: " + Computer.evaluate(board1));
            System.out.println((endTime-startTime)/1_000_000.0 + "ms\n");
            System.out.println("Black to move");
            startTime = System.nanoTime();
            bestMove2 = comp2.findBestMove(board1, 5, -1,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
            endTime = System.nanoTime();
            board1 = BoardHelper.makeMove(board1, bestMove2.move);
            System.out.println(BoardHelper.boardToFen(board1));
            System.out.println(bestMove2.move.getStartSquare() + " to " + bestMove2.move.getEndSquare() + " eval: " + bestMove2.evaluation);
            System.out.println("Board Eval: " + Computer.evaluate(board1));
            System.out.println((endTime-startTime)/1_000_000.0 + "ms\n");
        }
    }
    private static void playerVcpu(){
        Computer comp1 = new Computer();
        int[] board1 = BoardHelper.createBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        boolean gameOver = false;
        Scanner scanner = new Scanner(System.in);
        while(!gameOver){
            System.out.println("White to move\nWhat square to move from? (0-63)");
            int from = scanner.nextInt();
            if(from < 0) break;
            System.out.println("What square to move to? (0-63)");
            int to = scanner.nextInt();
            Move playerMove = new Move(from,to,board1);
            board1 = BoardHelper.makeMove(board1, playerMove);
            System.out.println(BoardHelper.boardToFen(board1));
            System.out.println("Black to move");
            long startTime = System.nanoTime();
            MoveEval bestMove = comp1.findBestMove(board1, 5, -1,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
            long endTime = System.nanoTime();
            board1 = BoardHelper.makeMove(board1, bestMove.move);
            System.out.println(BoardHelper.boardToFen(board1));
            System.out.println(bestMove.move.getStartSquare() + " to " + bestMove.move.getEndSquare() + " eval: " + bestMove.evaluation);
            System.out.println("Board Eval: " + Computer.evaluate(board1));
            System.out.println((endTime-startTime)/1_000_000.0 + "ms\n");

        }
    }
}

