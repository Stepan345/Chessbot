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
import chariot.model.ChallengeInfo.ColorOutcome;
import chariot.model.Enums.Color;
import chariot.model.Enums.Status;
import chariot.model.Event;
import chariot.model.Event.Type;
import chariot.model.GameStateEvent;

import javax.crypto.CipherInputStream;

public class Main {
    
    static void main() {
        Board.preCompMoveData();
        //playerVcpu();
        hostGame();
        /*
        Board board = new Board("8/8/1p6/p6P/kpP2Q2/8/1/8 w - - 0 1");
        var moves = board.findLegalMoves(1);
        board.printBoard();
        var bestMove = makeCpuMove(board,new Computer(),1,10);
        board.printBoard();
        */

    }
    public static void hostGame(){
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
        Map<String,Integer> colors = new HashMap<>();
        System.out.println("Bot connected and listening for events...");
        events.forEach(event -> {
            System.out.println("New event: " + event.type());
            switch (event.type()) {
                case Type.challenge:
                    var color = ((ColorOutcome)(client.bot().show(event.id()).get().colorInfo())).outcome();
                    colors.put(event.id(),(color == Color.white)?PieceHelper.BLACK:PieceHelper.WHITE);
                    System.out.println("Accepting challenge: " + event.id()+". Opponent color = " + color);
                    client.bot().acceptChallenge(event.id());
                    //boards.put(event.id(),Board.createBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"));
                    break;
                case Type.gameStart:
                    System.out.println("Game started: " + event.id());
                    Stream<GameStateEvent> gameEvents = client.bot().connectToGame(event.id()).stream();
                    gameEvents.forEach( gameEvent ->{
                        System.out.println("New game event: " + gameEvent.type());
                        String movesStr;
                        String[] moves;
                        Board board;
                        int colorToMove;
                        switch(gameEvent.type()){
                            case GameStateEvent.Type.gameFull:
                                if(colors.containsKey(event.id())){
                                    movesStr = ((GameStateEvent.Full)gameEvent).state().moves();
                                    if(movesStr.isEmpty() && colors.get(event.id()) == PieceHelper.WHITE){
                                        board = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
                                        System.out.println("No moves played yet");
                                        Computer comp = new Computer();
                                        ArrayList<Move> legalMoves = board.findLegalMoves(-1);
                                        for(int i = 0;i<legalMoves.size();i++){
                                            System.out.println((i+1)+". "+legalMoves.get(i).getNotation(board.board));
                                        }
                                        Board.findLegalMovesCount = 0;
                                        Board.generateAttackedPositionsCount = 0;
                                        Computer.findBestMoveCount = 0;
                                        Computer.evaluateCount = 0;
                                        long startTime = System.nanoTime();
                                        MoveEval bestMove = makeCpuMove(board,comp,1,10.0);
                                        long endTime = System.nanoTime();
                                        Board boardCopy = new Board(board);
                                        for(Move move:bestMove.line){
                                            System.out.print(move.getNotation(boardCopy.board)+" ");
                                            boardCopy.makeMove(move);
                                        }
                                        System.out.println("findLegalMoves() ran "+Board.findLegalMovesCount+" times");
                                        System.out.println("generateAttackedPositions() ran "+Board.generateAttackedPositionsCount+" times");
                                        System.out.println("findBestMove() ran "+Computer.findBestMoveCount+" times");
                                        System.out.println("evaluate() ran "+Computer.evaluateCount+" times");
                                        System.out.println("Best Move: " + bestMove.move.getNotation(board.board)+" Eval: "+bestMove.evaluation);
                                        System.out.println((endTime-startTime)/1_000_000.0 + "ms");
                                        client.bot().move(event.id(),bestMove.move.getNotation(board.board,true));
                                    }
                                }
                                break;
                            case GameStateEvent.Type.gameState:
                                Status status = ((GameStateEvent.State)gameEvent).status();
                                
                                if(status == Status.mate || status == Status.resign || status == Status.timeout || status == Status.draw){
                                    System.out.println("Game over: " + status);
                                    colors.remove(event.id());
                                    break;
                                }else{
                                    
                                    //move logic
                                    movesStr = ((GameStateEvent.State)gameEvent).moves();
                                    
                                    moves = movesStr.split(" ");
                                    if(!colors.containsKey(event.id())){
                                        System.out.println("Game started without challenge event, assigning color based on player id");
                                        colors.put(event.id(),(moves.length % 2 == 0)?PieceHelper.WHITE:PieceHelper.BLACK);
                                    }
                                    board = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
                                    for(String move:moves){
                                        Move moveObj = new Move(move,board.board);
                                        board.makeMove(moveObj);
                                    }
                                    
                                    System.out.println("Moves so far: " + movesStr);
                                    board.printBoard();
                                    colorToMove = colors.get(event.id());
                                    if(moves.length %2 == ((colorToMove == PieceHelper.WHITE)?0:1)){//bot to move
                                        Computer comp = new Computer();
                                        ArrayList<Move> legalMoves = board.findLegalMoves( -1);
                                        for(int i = 0;i<legalMoves.size();i++){
                                            System.out.println((i+1)+". "+legalMoves.get(i).getNotation(board.board));
                                        }
                                        Board.findLegalMovesCount = 0;
                                        Board.generateAttackedPositionsCount = 0;
                                        Computer.findBestMoveCount = 0;
                                        Computer.evaluateCount = 0;
                                        long startTime = System.nanoTime();
                                        MoveEval bestMove = makeCpuMove(board,comp,colorToMove,10.0);
                                        long endTime = System.nanoTime();
                                        Board boardCopy = new Board(board);
                                        for(Move move:bestMove.line){
                                            System.out.print(move.getNotation(boardCopy.board)+" ");
                                            boardCopy.makeMove( move);
                                        }
                                        System.out.println("findLegalMoves() ran "+Board.findLegalMovesCount+" times");
                                        System.out.println("generateAttackedPositions() ran "+Board.generateAttackedPositionsCount+" times");
                                        System.out.println("findBestMove() ran "+Computer.findBestMoveCount+" times");
                                        System.out.println("evaluate() ran "+Computer.evaluateCount+" times");
                                        System.out.println("Best Move: " + bestMove.move.getNotation(board.board)+" Eval: "+bestMove.evaluation);
                                        System.out.println((endTime-startTime)/1_000_000.0 + "ms");
                                        client.bot().move(event.id(),bestMove.move.getNotation(board.board,true));
                                        //boards.put(event.id(),Board.makeMove(board, bestMove.move));
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
    public static void playerVcpu(){
        Computer comp1 = new Computer();
        Board board1 = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        boolean gameOver = false;
        Scanner scanner = new Scanner(System.in);
        long startTime = 0;
        long endTime = 0;
        while(!gameOver){
            System.out.println(board1.boardToFen());
            board1.printBoard();
            System.out.println("White to move -----------------------------------\nPick a move");
            ArrayList<Move> legalMoves = board1.findLegalMoves(1);
            for(int i = 1;i<legalMoves.size()+1;i++){
                System.out.print(((i<10)?" ":"")+i+"."+((PieceHelper.getType(board1.board[legalMoves.get(i-1).getStartSquare()]) == 4)?" ":"")+legalMoves.get(i-1).getNotation(board1.board)+" ");
                if(i%5==0)System.out.println();
            }
            String from = scanner.nextLine();
            //if(from == 67) break;
            Move playerMove = legalMoves.getFirst();
            for(Move i:legalMoves) {
                if(i.getNotation(board1.board,true).equals(from)){
                    playerMove = i;
                    break;
                }
            }
            System.out.println(playerMove.getNotation(board1.board));
            board1.makeMove(playerMove);
            System.out.println(board1.boardToFen());
            for(int square:board1.board){
                System.out.print(square+" ");
            }
            board1.printBoard();
            System.out.println("Black to move -----------------------------------");

            ArrayList<Move> moves = board1.findLegalMoves( -1);
            for(int i = 0;i<moves.size();i++){
                System.out.println((i+1)+". "+moves.get(i).getNotation(board1.board));
                //System.out.println(Board.boardToFen(Board.makeMove(board, move)));
            }
            startTime = System.nanoTime();
            MoveEval bestMove = makeCpuMove(board1, comp1, -1,1000.0);
            endTime = System.nanoTime();
            Board boardCopy = new Board(board1);
            for(Move move:bestMove.line){
                System.out.print(move.getNotation(boardCopy.board)+" ");
                boardCopy.makeMove(move);
            }

            
            board1.makeMove(bestMove.move);
            //Board.printBoard(board1);
            System.out.println("Board Eval: " + Computer.evaluate(board1));
            System.out.println((endTime-startTime)/1_000_000.0 + "ms\n");
        }
        scanner.close();
    }
    private static MoveEval makeCpuMove(Board board, Computer comp, int color,double timeLimitSeconds){
        //double gameProgress = 1-(Board.countPieces(board)/32.0);
        comp.timeLimitSeconds = timeLimitSeconds;
        comp.startTime = 0;
        int depth = 5;
        
        ArrayList<Move> legalMoves = board.findLegalMoves(color);
        if(legalMoves.size() >= 20){
            depth = 4;
        }
        System.out.println("Searching at depth of "+depth);
        long startTime = System.nanoTime();
        MoveEval bestMove = comp.findBestMove(board, depth, color);
        System.out.println("New best move found at depth "+(depth)+": "+bestMove.move.getNotation(board.board)+" Eval: "+bestMove.toString());
        depth++;
        comp.startTime = startTime;
        while(comp.startTime + timeLimitSeconds*1_000_000_000L > System.nanoTime()){
            System.out.println("Searching at depth "+depth);
            MoveEval moveFound = comp.findBestMove(board, depth, color);
            if(comp.startTime + timeLimitSeconds*1_000_000_000L > System.nanoTime()){
                bestMove = moveFound;
                System.out.println("New best move found at depth "+(depth)+" in "+ (System.nanoTime()-startTime)/1e6+" ms : "+bestMove.move.getNotation(board.board)+" Eval: "+bestMove.toString());
                depth++;
            }
        }
        //long endTime = System.nanoTime();
        return bestMove;
    }
}