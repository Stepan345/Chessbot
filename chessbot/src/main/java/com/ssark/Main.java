package com.ssark;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
public class Main {
    
    public static void main(String[] args) {
        BoardHelper.preCompMoveData();
        //createBoard_TEST();
        //findLegalMoves_TEST("r1bq3r/ppppkppp/2n2n2/4p1B1/2B1P3/P1P2N2/1PP2PPP/R2QK2R",99,-1);
        //findBestMove();
        cpuVcpu();
        //playerVcpu();
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
        BoardHelper.printBoard(board);
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
        //Computer comp2 = new Computer();
        int[] board1 = BoardHelper.createBoard("rnbqkbnr/pppppppp/8/8/3P4/3X4/PPP1PPPP/RNBQKBNR");
        BoardHelper.printBoard(board1);
        long startTime = System.nanoTime();
        MoveEval bestMove = comp1.findBestMove(board1, 5, -1,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
        long endTime = System.nanoTime();
        //System.out.println(comp1.counter);

        // for (int i = 0; i < 20; i++) {
        //     MoveEval bestMove1 = comp1.findBestMove(board1, 4, 1,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
        //     board1 = BoardHelper.makeMove(board1, bestMove1.move);
        //     MoveEval bestMove2 = comp2.findBestMove(board1, 4, -1,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
        //     board1 = BoardHelper.makeMove(board1, bestMove1.move);
        //     System.out.println(bestMove1.move.getStartSquare());
        // }
        System.out.println(bestMove.move.getNotation(board1));
        //System.out.println(comp1.counter);
        System.out.println((endTime-startTime)/1_000_000.0 + "ms");
    }
    private static void cpuVcpu(){
        Computer comp1 = new Computer();
        //Computer comp2 = new Computer();
        int[] board1 = BoardHelper.createBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        //MoveEval bestMove1 = new MoveEval(new Move(0,0),0);
        //MoveEval bestMove2 = new MoveEval(new Move(0,0),0);
        long startTime;
        long endTime;
        for (int MOVE = 0; MOVE < 30; MOVE++) {
            System.out.println(BoardHelper.boardToFen(board1));
            for(int square:board1){
                System.out.print(square+" ");
            }
            BoardHelper.printBoard(board1);
            System.out.println("White to move -----------------------------------");

            ArrayList<Move> moves = BoardHelper.findLegalMoves(board1, -1);
            for(int i = 0;i<moves.size();i++){
                System.out.println((i+1)+". "+moves.get(i).getNotation(board1));
                //System.out.println(BoardHelper.boardToFen(BoardHelper.makeMove(board, move)));
            }
            double gameProgress = 1-(BoardHelper.countPieces(board1)/32.0);
            int depth = (int)(4*gameProgress)+6;
            System.out.println("Evaluating at a depth of "+depth);
            startTime = System.nanoTime();
            MoveEval bestMove = comp1.findBestMove(board1, depth, 1,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
            endTime = System.nanoTime();

            int[] boardCopy = BoardHelper.createBoard(board1);
            for(Move move:bestMove.line){
                System.out.print(move.getNotation(boardCopy)+" ");
                boardCopy = BoardHelper.makeMove(boardCopy, move);
            }
            board1 = BoardHelper.makeMove(board1, bestMove.move);
            //System.out.println(BoardHelper.boardToFen(board1));
            //System.out.println(bestMove.move.getStartSquare() + " to " + bestMove.move.getEndSquare() + " eval: " + bestMove.evaluation);
            System.out.println("Board Eval: " + Computer.evaluate(board1));
            System.out.println((endTime-startTime)/1_000_000.0 + "ms\n");
            System.out.println(BoardHelper.boardToFen(board1));
            for(int square:board1){
                System.out.print(square+" ");
            }
            BoardHelper.printBoard(board1);
            System.out.println("Black to move -----------------------------------");

            moves = BoardHelper.findLegalMoves(board1, -1);
            for(int i = 0;i<moves.size();i++){
                System.out.println((i+1)+". "+moves.get(i).getNotation(board1));
                //System.out.println(BoardHelper.boardToFen(BoardHelper.makeMove(board, move)));
            }
            gameProgress = 1-(BoardHelper.countPieces(board1)/32.0);
            depth = (int)(4*gameProgress)+6;
            System.out.println("Evaluating at a depth of "+depth);
            startTime = System.nanoTime();
            bestMove = comp1.findBestMove(board1, depth, -1,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
            endTime = System.nanoTime();

            boardCopy = BoardHelper.createBoard(board1);
            for(Move move:bestMove.line){
                System.out.print(move.getNotation(boardCopy)+" ");
                boardCopy = BoardHelper.makeMove(boardCopy, move);
            }
            board1 = BoardHelper.makeMove(board1, bestMove.move);
            //System.out.println(BoardHelper.boardToFen(board1));
            //System.out.println(bestMove.move.getStartSquare() + " to " + bestMove.move.getEndSquare() + " eval: " + bestMove.evaluation);
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
            System.out.println("White to move -----------------------------------\nPick a move");
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
            System.out.println(BoardHelper.boardToFen(board1));
            for(int square:board1){
                System.out.print(square+" ");
            }
            BoardHelper.printBoard(board1);
            System.out.println("Black to move -----------------------------------");

            ArrayList<Move> moves = BoardHelper.findLegalMoves(board1, -1);
            for(int i = 0;i<moves.size();i++){
                System.out.println((i+1)+". "+moves.get(i).getNotation(board1));
                //System.out.println(BoardHelper.boardToFen(BoardHelper.makeMove(board, move)));
            }
            double gameProgress = 1-(BoardHelper.countPieces(board1)/32.0);
            int depth = (int)(4*gameProgress)+6;
            System.out.println("Evaluating at a depth of "+depth);
            long startTime = System.nanoTime();
            MoveEval bestMove = comp1.findBestMove(board1, depth, -1,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
            long endTime = System.nanoTime();

            int[] boardCopy = BoardHelper.createBoard(board1);
            for(Move move:bestMove.line){
                System.out.print(move.getNotation(boardCopy)+" ");
                boardCopy = BoardHelper.makeMove(boardCopy, move);
            }
            board1 = BoardHelper.makeMove(board1, bestMove.move);
            //System.out.println(BoardHelper.boardToFen(board1));
            //System.out.println(bestMove.move.getStartSquare() + " to " + bestMove.move.getEndSquare() + " eval: " + bestMove.evaluation);
            System.out.println("Board Eval: " + Computer.evaluate(board1));
            System.out.println((endTime-startTime)/1_000_000.0 + "ms\n");
        }
        scanner.close();
    }
}