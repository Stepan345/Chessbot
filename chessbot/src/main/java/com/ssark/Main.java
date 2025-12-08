package com.ssark;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

import chariot.Client;
import chariot.ClientAuth;
import chariot.model.Enums.Status;
import chariot.model.Event;
import chariot.model.Event.Type;
import chariot.model.GameStateEvent;

public class Main {
    
    public static void main(String[] args) {
        BoardHelper.preCompMoveData();
        //createBoard_TEST("c2c4 e7e5 b1c3 b8c6 e2e4 c6d4 d2d3 g8f6 g1f3 d7d6 f1e2 d4e2 d1e2 f8e7 e1g1 e8g8 c1e3 c8e6 d3d4 e6c4 e2c4");
        //findLegalMoves_TEST("r1bq3r/ppppkppp/2n2n2/4p1B1/2B1P3/P1P2N2/1PP2PPP/R2QK2R",99,-1);
        //findBestMove();
        //cpuVcpu();
        //playerVcpu();
        //testEvaluate();
        //chariotTest();
        hostGame();
    }
    private static void hostGame(){
        String token = "";
        try (BufferedReader reader = new BufferedReader(new FileReader("token.txt"))) {
            token = reader.readLine();
            System.out.println(token);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        ClientAuth client = Client.auth(token);
        System.out.println(client.account().emailAddress());
        client.bot().upgradeToBotAccount();
        Stream<Event> events = client.bot().connect().stream();
        Map<String,int[]> boards = new HashMap<String,int[]>();
        System.out.println("Bot connected and listening for events...");
        events.forEach(event -> {
            System.out.println("New event: " + event.type());
            switch (event.type()) {
                case Type.challenge:

                    System.out.println("Accepting challenge: " + event.id());
                    client.bot().acceptChallenge(event.id());
                    //boards.put(event.id(),BoardHelper.createBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"));
                    break;
                case Type.gameStart:
                    System.out.println("Game started: " + event.id());
                    Stream<GameStateEvent> gameEvents = client.bot().connectToGame(event.id()).stream();
                    gameEvents.forEach( gameEvent ->{
                        System.out.println("New game event: " + gameEvent.type());
                        switch(gameEvent.type()){
                            case GameStateEvent.Type.gameFull:
                                break;
                            case GameStateEvent.Type.gameState:
                                Status status = ((GameStateEvent.State)gameEvent).status();
                                if(status == Status.mate || status == Status.resign || status == Status.timeout || status == Status.draw){
                                    System.out.println("Game over: " + status);
                                    boards.remove(event.id());
                                    break;
                                }else{
                                    //move logic
                                    String movesStr = ((GameStateEvent.State)gameEvent).moves();
                                    String[] moves = movesStr.split(" ");
                                    int[] board = BoardHelper.createBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
                                    for(String move:moves){
                                        Move moveObj = new Move(move,board);
                                        board = BoardHelper.makeMove(board, moveObj);
                                    }
                                    
                                    System.out.println("Moves so far: " + movesStr);
                                    BoardHelper.printBoard(board);
                                    if(moves.length %2 == 1){//bot to move
                                        Computer comp = new Computer();
                                        ArrayList<Move> legalMoves = BoardHelper.findLegalMoves(board, -1);
                                        for(int i = 0;i<legalMoves.size();i++){
                                            System.out.println((i+1)+". "+legalMoves.get(i).getNotation(board));
                                        }
                                        long startTime = System.nanoTime();
                                        MoveEval bestMove = makeCpuMove(board,comp,-1,5.0);
                                        long endTime = System.nanoTime();
                                        int[] boardCopy = BoardHelper.createBoard(board);
                                        for(Move move:bestMove.line){
                                            System.out.print(move.getNotation(boardCopy)+" ");
                                            boardCopy = BoardHelper.makeMove(boardCopy, move);
                                        }
                                        System.out.println("Best Move: " + bestMove.move.getNotation(board)+" Eval: "+bestMove.evaluation);
                                        System.out.println((endTime-startTime)/1_000_000.0 + "ms");
                                        client.bot().move(event.id(),bestMove.move.getNotation(board,true));
                                        boards.put(event.id(),BoardHelper.makeMove(board, bestMove.move));
                                    }
                                }
                                break;
                            default:
                                System.out.println("Unhandled game event type: " + gameEvent.type());
                                break;
                        }
                    });
                    break;
                default:
                    System.out.println("Unhandled event type: " + event.type());
                    break;
            }
        });
    }
    private static void chariotTest(){
        Client client = Client.basic();
        System.out.println(client.teams().byTeamId("lichess-swiss").maybe()
            .map(team -> "Team %s has %d members!".formatted(team.name(), team.nbMembers()))
            .orElse("Couldn't find team!"));
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
    private static void createBoard_TEST(String boardMoves){
        boolean[] out = new boolean[3];
        //perform test
        String[] moves = boardMoves.split(" ");
        int[] board = BoardHelper.createBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        BoardHelper.printBoard(board);
        for(String move:moves){
            Move moveObj = new Move(move,board);
            System.out.println(moveObj.getNotation(board));
            board = BoardHelper.makeMove(board, moveObj);
            BoardHelper.printBoard(board);
        }
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
    private static void testEvaluate(){
        int[] board1 = BoardHelper.createBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        BoardHelper.printBoard(board1);
        //double eval = Computer.evaluate(board1,true);
        long startTime = System.nanoTime();
        double eval = Computer.evaluate(board1);
        long endTime = System.nanoTime();
        System.out.println("Board Eval: " + eval);
        System.out.println((endTime-startTime)/1_000_000.0 + "ms");
    }
    private static void cpuVcpu(){
        String gameID = "1765064125572";//System.currentTimeMillis()+"";
        Computer comp1 = new Computer();
        //Computer comp2 = new Computer();
        int[] board1 = BoardHelper.createBoard("8/1p1r4/p1k5/5R2/3K2P1/7P/8/8");
        //MoveEval bestMove1 = new MoveEval(new Move(0,0),0);
        //MoveEval bestMove2 = new MoveEval(new Move(0,0),0);
        long startTime = 0;
        long endTime = 0;  
        for (int MOVE = 0; MOVE < 30; MOVE++) {
            System.out.println(BoardHelper.boardToFen(board1));
            BoardHelper.printBoard(board1);
            System.out.println("White to move -----------------------------------");
            ArrayList<Move> moves = BoardHelper.findLegalMoves(board1, -1);
            for(int i = 0;i<moves.size();i++){
                System.out.println((i+1)+". "+moves.get(i).getNotation(board1));
                //System.out.println(BoardHelper.boardToFen(BoardHelper.makeMove(board, move)));
            }
            startTime = System.nanoTime();
            MoveEval bestMove = makeCpuMove(board1, comp1, 1,15.0);
            endTime = System.nanoTime();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("game"+gameID+".txt", true))) {
                writer.write(bestMove.move.getNotation(board1)+" - ");
                System.out.println("Move added to " + "game"+gameID+".txt");
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            }
            int[] boardCopy = BoardHelper.createBoard(board1);
            for(Move move:bestMove.line){
                System.out.print(move.getNotation(boardCopy)+" ");
                boardCopy = BoardHelper.makeMove(boardCopy, move);
            }
            board1 = BoardHelper.makeMove(board1, bestMove.move);
            System.out.println("Board Eval: " + Computer.evaluate(board1));
            System.out.println((endTime-startTime)/1_000_000.0 + "ms\n");
            System.out.println(BoardHelper.boardToFen(board1));
            BoardHelper.printBoard(board1);
            System.out.println("Black to move -----------------------------------");

            moves = BoardHelper.findLegalMoves(board1, -1);
            for(int i = 0;i<moves.size();i++){
                System.out.println((i+1)+". "+moves.get(i).getNotation(board1));
                //System.out.println(BoardHelper.boardToFen(BoardHelper.makeMove(board, move)));
            }
            startTime = System.nanoTime();
            bestMove = makeCpuMove(board1, comp1, -1,15.0);
            endTime = System.nanoTime();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("game"+gameID+".txt", true))) {
                writer.write(bestMove.move.getNotation(board1)+"\n");
                System.out.println("Move added to " + "game"+gameID+".txt");
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            }
            boardCopy = BoardHelper.createBoard(board1);
            for(Move move:bestMove.line){
                System.out.print(move.getNotation(boardCopy)+" ");
                boardCopy = BoardHelper.makeMove(boardCopy, move);
            }

            
            board1 = BoardHelper.makeMove(board1, bestMove.move);
            //BoardHelper.printBoard(board1);
            System.out.println("Board Eval: " + Computer.evaluate(board1));
            System.out.println((endTime-startTime)/1_000_000.0 + "ms\n");
        }
    }
    private static void playerVcpu(){
        Computer comp1 = new Computer();
        int[] board1 = BoardHelper.createBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        boolean gameOver = false;
        Scanner scanner = new Scanner(System.in);
        long startTime = 0;
        long endTime = 0;
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
            startTime = System.nanoTime();
            MoveEval bestMove = makeCpuMove(board1, comp1, -1,10.0);
            endTime = System.nanoTime();
            int[] boardCopy = BoardHelper.createBoard(board1);
            for(Move move:bestMove.line){
                System.out.print(move.getNotation(boardCopy)+" ");
                boardCopy = BoardHelper.makeMove(boardCopy, move);
            }

            
            board1 = BoardHelper.makeMove(board1, bestMove.move);
            //BoardHelper.printBoard(board1);
            System.out.println("Board Eval: " + Computer.evaluate(board1));
            System.out.println((endTime-startTime)/1_000_000.0 + "ms\n");
        }
        scanner.close();
    }
    private static MoveEval makeCpuMove(int[] board, Computer comp, int color,double timeLimitSeconds){
        //double gameProgress = 1-(BoardHelper.countPieces(board)/32.0);
        comp.timeLimitSeconds = timeLimitSeconds;
        comp.startTime = 0;
        int depth = 5;
        
        ArrayList<Move> legalMoves = BoardHelper.findLegalMoves(board, color);
        if(legalMoves.size() >= 20){
            depth = 4;
        }
        System.out.println("Searching at depth of "+depth);
        long startTime = System.nanoTime();
        MoveEval bestMove = comp.findBestMove(board, depth, color,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
        System.out.println("New best move found at depth "+(depth)+": "+bestMove.move.getNotation(board)+" Eval: "+bestMove.evaluation);
        depth++;
        comp.startTime = startTime;
        while(comp.startTime + timeLimitSeconds*1_000_000_000L > System.nanoTime()){
            System.out.println("Searching at depth "+depth);
            MoveEval moveFound = comp.findBestMove(board, depth, color,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
            if(comp.startTime + timeLimitSeconds*1_000_000_000L > System.nanoTime()){
                bestMove = moveFound;
                System.out.println("New best move found at depth "+(depth)+": "+bestMove.move.getNotation(board)+" Eval: "+bestMove.evaluation);
                depth++;
            }
        }
        //long endTime = System.nanoTime();
        return bestMove;
    }
}