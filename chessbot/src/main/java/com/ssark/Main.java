package com.ssark;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
public class Main {
    
    public static void main(String[] args) {
        BoardHelper.preCompMoveData();
        //createBoard_TEST();
        //findLegalMoves_TEST("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR",20,-1);
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
    private static void findLegalMoves_TEST(String fen,int expected,int color){
        boolean out;
        
        int[] board = BoardHelper.createBoard(fen);
        //System.out.println(BoardHelper.generateAttackedPositions(board, 1));

        long startTime = System.nanoTime();
        ArrayList<Move> moves = BoardHelper.findLegalMoves(board, color);
        long endTime = System.nanoTime();

        ArrayList<Move> movesBlack = BoardHelper.findLegalMoves(board, -1);
        
        for(int i = 0;i<moves.size();i++){
            System.out.println((i+1)+". "+moves.get(i).getNotation(board));
            //System.out.println(BoardHelper.boardToFen(BoardHelper.makeMove(board, move)));
        }
        // for(int i = 0; i < 1000;i++){
        //     BoardHelper.findLegalMoves(board, 1);
        // }
        
        out = (moves.size() == expected);
        System.out.println("findLegalMoves test0 " + ((out)?"Passed":"Failed"));
        System.out.println("Found " + moves.size() + " white legal moves");
        System.out.println("Found " + movesBlack.size() + " black legal moves");
        System.out.println((endTime-startTime)/1_000_000.0 + "ms");
    }
    private static void findBestMove(){
        Computer comp1 = new Computer();
        Computer comp2 = new Computer();
        int[] board1 = BoardHelper.createBoard("r1bq3r/ppppkppp/2n2n2/4p1B1/2B1P3/P1P2N2/1PP2PPP/R2QK2R");
        long startTime = System.nanoTime();
        MoveEval bestMove = comp1.findBestMove(board1, 4, 1,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
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
            System.out.println(BoardHelper.boardToFen(board1));
            BoardHelper.printBoard(board1);
            System.out.println("White to move\nPick a move");
            ArrayList<Move> legalMoves = BoardHelper.findLegalMoves(board1, 1);
            for(int i = 1;i<legalMoves.size()+1;i++){
                System.out.print(((i<10)?" ":"")+i+"."+((PieceHelper.getType(board1[legalMoves.get(i-1).getStartSquare()]) == 4)?" ":"")+legalMoves.get(i-1).getNotation(board1)+" ");
                if(i%5==0)System.out.println();
            }
            int from = scanner.nextInt();
            if(from == 67) break;
            Move playerMove = legalMoves.get(from-1);
            System.out.println(playerMove.getNotation(board1));
            board1 = BoardHelper.makeMove(board1, playerMove);
            BoardHelper.printBoard(board1);
            System.out.println("Black to move");
            long startTime = System.nanoTime();
            MoveEval bestMove = comp1.findBestMove(board1, 5, -1,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
            long endTime = System.nanoTime();
            System.out.println(bestMove.move.getNotation(board1));
            board1 = BoardHelper.makeMove(board1, bestMove.move);
            //System.out.println(BoardHelper.boardToFen(board1));
            //System.out.println(bestMove.move.getStartSquare() + " to " + bestMove.move.getEndSquare() + " eval: " + bestMove.evaluation);
            System.out.println("Board Eval: " + Computer.evaluate(board1));
            System.out.println((endTime-startTime)/1_000_000.0 + "ms\n");
        }
        scanner.close();
    }
}