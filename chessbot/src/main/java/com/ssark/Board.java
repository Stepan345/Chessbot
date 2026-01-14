package com.ssark;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Board{
    public static int findLegalMovesCount = 0;
    public static int generateAttackedPositionsCount = 0;
    private static final int[] directionalOffsets = {8,-8,-1,1,7,-7,9,-9};
    private static final int[][] squaresToEdge = new int[64][8];
    private static final int[] knightOffsets = {15,17,-15,-17,6,-10,-6,10};
    private static final boolean[][] knightEdgeCheck = new boolean[64][8];
    private static final int[] oppositeDirs = {1,0,3,2,5,4,7,6};
    public static final HashMap<Integer,Integer> pieceValue = new HashMap<>();
    public ArrayList<String> pastPositions = new ArrayList<>();
    public static void preCompMoveData(){
        long startTime = System.nanoTime();
        pieceValue.put(0,0);
        pieceValue.put(-4,100);
        pieceValue.put(4,100);//pawn
        pieceValue.put(8,330);//bishop
        pieceValue.put(12,320);//knight
        pieceValue.put(16,500);//rook
        pieceValue.put(20,900);//queen
        pieceValue.put(24,20000);//king
        for(int i = 0; i < 64; i++){
            int west = i % 8;
            int rank = i / 8;
            //sliding piece
            int north = 7 - rank;
            int east = 7 - west;



            squaresToEdge[i] = new int[]{
                    north,
                    rank,
                    west,
                    east,
                    Math.min(north,west),
                    Math.min(rank,east),
                    Math.min(north,east),
                    Math.min(rank,west)
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
            if(west -2 < 0){
                leftTop = false;
                leftBottom = false;
                if(west -1 < 0){
                    topLeft = false;
                    bottomLeft = false;
                }
            }
            if(west +2 > 7){
                rightTop = false;
                rightBottom = false;
                if(west +1 > 7){
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
    }

    public static class Pin{
        public int pieceSquare;
        public int[] movableDirections;
        public boolean enPassantEdgeCase;
//        /**
//         * The squares of all the pieces that are pinning this piece
//         */
//        public ArrayList<Integer> pinners;
        Pin(int square,int dirs,boolean passant){
            pieceSquare = square;
            movableDirections = new int[]{dirs,oppositeDirs[dirs]};
            enPassantEdgeCase = passant;
        }
    }
    public int[] board;
    public ArrayList<Pin>[] pinnedPieces = new ArrayList[]{new ArrayList<Pin>(),new ArrayList<Pin>()};
    public ArrayList<ArrayList<Integer>>[] checks = new ArrayList[]{new ArrayList<ArrayList<Integer>>(),new ArrayList<ArrayList<Integer>>()};
    Board(int[] board){
        this.board = board.clone();
    }
    Board(){
        board = new int[64];
    }
    Board(Board board){
        this.board = board.board.clone();
        this.pinnedPieces = new ArrayList[]{new ArrayList<Pin>(board.pinnedPieces[0]),new ArrayList<Pin>(board.pinnedPieces[0])};
        //this.findChecks();
    }
    Board(Board board,Move move){
        this.board = board.board.clone();
        this.lastMoves = new ArrayList<>(board.lastMoves);
        this.pastPositions = new ArrayList<>(board.pastPositions);
        this.makeMove(move);
    }
    Board(String fen){
        int[] board = new int[64];
        String onlyFen = fen.split(" ")[0];
        String[] splitByRows = onlyFen.split("/");
        for(int rank = 7; rank  >= 0; rank--){
            char[] parsedRow = splitByRows[rank].toCharArray();
            int rowIndex = 0;
            for(int file = 0; file < 8;file++){
                char item = parsedRow[rowIndex];
                if(Character.isDigit(item)){
                    int parsedInt = item - 48;
                    file+=parsedInt-1;
                }else{
                    boolean isWhite = Character.isUpperCase(item);
                    char lowerItem = Character.toLowerCase(item);
                    int i = ((7-rank)*8)+file;
                    switch(lowerItem){
                        case 'x'://en-passant
                            board[i] = -(((isWhite)?1:2) | 4);
                            break;
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
                }
                rowIndex++;
            }
        }
        this.board = board;
        findPins();
    }
    public void findChecks(){
        int kingSquareWhite = getKingPosition(1);
        int kingSquareBlack = getKingPosition(2);
        this.checks[0] = new ArrayList<>();
        this.checks[1] = new ArrayList<>();

        for(int dir = 0;dir<directionalOffsets.length;dir++){
            //white
            ArrayList<Integer> blockSquares = new ArrayList<>();
            for(int i = 0;i<squaresToEdge[kingSquareWhite][dir];i++){
                var targetSquare = kingSquareWhite+(directionalOffsets[dir]*(i+1));
                int piece = board[targetSquare];
                blockSquares.add(targetSquare);
                if(piece<=0)continue;
                int pieceColor = PieceHelper.getColor(piece);
                if((dir == 4 || dir == 6) && i == 0 && piece == 6){//pawn detection
                    this.checks[0].add(blockSquares);
                    break;
                }
                if(pieceColor != -1)break;
                switch(PieceHelper.getString(piece)){//sliding piece detection
                    case "b":
                        if(dir > 3)checks[0].add(blockSquares);
                        break;
                    case "r":
                        if(dir < 4)checks[0].add(blockSquares);
                        break;
                    case "q":
                        checks[0].add(blockSquares);
                        break;
                }
                break;
            }
            if(knightEdgeCheck[kingSquareWhite][dir] && this.board[kingSquareWhite+knightOffsets[dir]] == 14){//knight detection
                ArrayList<Integer> knightLocation = new ArrayList<>();
                knightLocation.add(kingSquareWhite+knightOffsets[dir]);
                checks[0].add(knightLocation);
            }

            //black
            blockSquares = new ArrayList<>();
            for(int i = 0;i<squaresToEdge[kingSquareBlack][dir];i++){
                var targetSquare = kingSquareBlack+(directionalOffsets[dir]*(i+1));
                int piece = board[targetSquare];
                blockSquares.add(targetSquare);
                if(piece<=0)continue;
                int pieceColor = PieceHelper.getColor(piece);
                if((dir == 5 || dir == 7) && i == 0 && piece == 5){//pawn detection
                    this.checks[1].add(blockSquares);
                    break;
                }
                if(pieceColor != 1)break;
                switch(PieceHelper.getString(piece)){//sliding piece detection
                    case "b":
                        if(dir > 3)checks[1].add(blockSquares);
                        break;
                    case "r":
                        if(dir < 4)checks[1].add(blockSquares);
                        break;
                    case "q":
                        checks[1].add(blockSquares);
                        break;
                }
                break;
            }
            if(knightEdgeCheck[kingSquareBlack][dir] && this.board[kingSquareBlack+knightOffsets[dir]] == 13){//knight detection
                ArrayList<Integer> knightLocation = new ArrayList<>();
                knightLocation.add(kingSquareBlack+knightOffsets[dir]);
                checks[1].add(knightLocation);
            }
        }
    }
    public void findPins(){
        int kingSquareWhite = getKingPosition(1);
        int kingSquareBlack = getKingPosition(2);
        this.pinnedPieces[0] = new ArrayList<>();
        this.pinnedPieces[1] = new ArrayList<>();
        this.checks[0] = new ArrayList<>();
        this.checks[1] = new ArrayList<>();

        for(int dir = 0;dir< directionalOffsets.length;dir++){
            int pinnedPieceSquareWhite = -1;
            ArrayList<Integer> blockSquares = new ArrayList<>();
            //white
            int passantFlag = 0;//0 = nothing 1 = pawn detected / same color 2 = pawn detected / diff color 3 = found both pawns
            for(int i=0;i<squaresToEdge[kingSquareWhite][dir];i++){
                int targetSquare = kingSquareWhite+(directionalOffsets[dir]*(i+1));
                int piece = board[targetSquare];
                blockSquares.add(targetSquare);
                if(piece<=0){
                    if(passantFlag == 2)break;
                    continue;
                }
                int pieceColor = PieceHelper.getColor(piece);
                if((dir == 4 || dir == 6) && i == 0 && piece == 6){//pawn detection
                    this.checks[0].add(blockSquares);
                    break;
                }
                if(pieceColor == -1) {//if the piece is black
                    switch (PieceHelper.getString(piece)) {
                        case "b":
                            if (dir > 3) {
                                //add a pin
                                if(pinnedPieceSquareWhite == -1){
                                    if(passantFlag == 0)checks[0].add(blockSquares);
                                }else if(passantFlag != 2)pinnedPieces[0].add(new Pin(pinnedPieceSquareWhite, dir,passantFlag == 3));
                            }
                            break;
                        case "r":
                            if (dir < 4) {
                                if(pinnedPieceSquareWhite == -1) {
                                    if(passantFlag == 0)checks[0].add(blockSquares);
                                }
                                else if(passantFlag != 2)pinnedPieces[0].add(new Pin(pinnedPieceSquareWhite, dir,passantFlag == 3));
                            }
                            break;
                        case "q":
                            if(pinnedPieceSquareWhite == -1) {
                                if(passantFlag == 0)checks[0].add(blockSquares);
                            }
                            else if(passantFlag != 2)pinnedPieces[0].add(new Pin(pinnedPieceSquareWhite, dir,passantFlag == 3));
                            break;
                    }
                    if(kingSquareWhite/8 == 4 && (dir == 2 || dir == 3) && PieceHelper.getString(piece).equals("p") && passantFlag-2 < 0) {
                        passantFlag += 2;
                    }else break;
                }else if(pinnedPieceSquareWhite == -1){//if the piece is white
                    pinnedPieceSquareWhite = targetSquare;
                    if(kingSquareWhite/8 == 4 && (dir == 2 || dir == 3) && PieceHelper.getString(piece).equals("p"))passantFlag +=1;
                }else{
                    break;
                }
            }
            if(knightEdgeCheck[kingSquareWhite][dir] && this.board[kingSquareWhite+knightOffsets[dir]] == 14){//knight detection
                ArrayList<Integer> knightLocation = new ArrayList<>();
                knightLocation.add(kingSquareWhite+knightOffsets[dir]);
                checks[0].add(knightLocation);
            }
            //black
            int pinnedPieceSquareBlack = -1;
            blockSquares = new ArrayList<>();
            passantFlag = 0;
            for(int i=0;i<squaresToEdge[kingSquareBlack][dir];i++){
                int targetSquare = kingSquareBlack+(directionalOffsets[dir]*(i+1));
                int piece = board[targetSquare];
                blockSquares.add(targetSquare);
                if(piece<=0){
                    if(passantFlag == 2)break;
                    continue;
                }
                int pieceColor = PieceHelper.getColor(piece);
                if((dir == 5 || dir == 7) && i == 0 && piece == 5){//pawn detection
                    this.checks[1].add(blockSquares);
                    break;
                }
                if(pieceColor == 1) {//if the piece is white
                    switch (PieceHelper.getString(piece)) {
                        case "b":
                            if (dir > 3) {
                                //add a pin
                                if(pinnedPieceSquareBlack == -1){
                                    if(passantFlag == 0)checks[1].add(blockSquares);
                                }
                                else if(passantFlag != 2)pinnedPieces[1].add(new Pin(pinnedPieceSquareBlack, dir,passantFlag == 3));
                            }
                            break;
                        case "r":
                            if (dir < 4) {
                                if(pinnedPieceSquareBlack == -1){
                                    if(passantFlag == 0)checks[1].add(blockSquares);
                                }
                                else if(passantFlag != 2)pinnedPieces[1].add(new Pin(pinnedPieceSquareBlack, dir,passantFlag == 3));
                            }
                            break;
                        case "q":
                            if(pinnedPieceSquareBlack == -1){
                                if(passantFlag == 0)checks[1].add(blockSquares);
                            }
                            else if(passantFlag != 2)pinnedPieces[1].add(new Pin(pinnedPieceSquareBlack, dir,passantFlag == 3));
                            break;
                    }
                    if(kingSquareBlack/8 == 3 && (dir == 2 || dir == 3) && PieceHelper.getString(piece).equals("p") && passantFlag-2 < 0) {
                        passantFlag += 2;
                    }else break;
                }else if(pinnedPieceSquareBlack == -1){//if the piece is black
                    pinnedPieceSquareBlack = targetSquare;
                    if(kingSquareBlack/8 == 3 && (dir == 2 || dir == 3) && PieceHelper.getString(piece).equals("p"))passantFlag +=1;
                }else{
                    break;
                }
            }
            if(knightEdgeCheck[kingSquareBlack][dir] && this.board[kingSquareBlack+knightOffsets[dir]] == 13){//knight detection
                ArrayList<Integer> knightLocation = new ArrayList<>();
                knightLocation.add(kingSquareBlack+knightOffsets[dir]);
                checks[1].add(knightLocation);
            }
        }

    }
    public String toString(){
        StringBuilder s = new StringBuilder();
        for(int i : board){
            s.append(i);
        }
        return s.toString();
    }
    public String boardToFen(){
        StringBuilder fen = new StringBuilder();
        for(int rank = 7;rank >= 0;rank--){
            int empty = 0;
            for(int file = 0;file < 8;file++){
                int piece = board[(rank*8)+file];
                if(piece == 0){
                    empty++;
                    continue;
                }else if(empty > 0){
                    fen.append(empty);
                    empty = 0;
                }

                int color = piece & 3;
                int pieceType = piece & 28;
                if(piece < 0){
                    piece = Math.abs(piece);
                    color = piece & 3;

                    fen.append((color == 1) ? "X" : "x");
                    continue;
                }
                switch(pieceType){
                    case 4://pawn
                        fen.append((color == 1) ? "P" : "p");
                        break;
                    case 8://bishop
                        fen.append((color == 1) ? "B" : "b");
                        break;
                    case 12://knight
                        fen.append((color == 1) ? "N" : "n");
                        break;
                    case 16://rook
                        fen.append((color == 1) ? "R" : "r");
                        break;
                    case 20://queen
                        fen.append((color == 1) ? "Q" : "q");
                        break;
                    case 24://king
                        fen.append((color == 1) ? "K" : "k");
                        break;
                }
            }
            if(empty > 0){
                fen.append(empty);
            }
            if(rank != 0) fen.append("/");
        }
        return fen.toString();
    }
    public void printBoard(){
        StringBuilder out = new StringBuilder("\n   a b c d e f g h\n   - - - - - - - -\n8- ");
        for(int rank = 7;rank >= 0;rank--){
            for(int file = 0;file < 8;file++){
                int i = (rank*8)+file;
                if(board[i] <= 0) out.append("# ");
                else out.append(PieceHelper.getString(board[i], true)).append(" ");
                if(file == 7 && rank != 0) out.append("\n").append(rank).append("- ");
            }
        }
        out.append("\n   - - - - - - - -");
        out.append("\n   a b c d e f g h");
        System.out.println(out);
    }
    public ArrayList<Move> lastMoves = new ArrayList<>();
    public void makeMove(Move move){
        int startSquare = move.getStartSquare();
        int endSquare = move.getEndSquare();
        int piece = board[startSquare];
        int color = ((piece & 3)==1)?1:-1;
        //var moves = this.findLegalMoves(color);
//        var valid = false;
//        for(Move i:moves){
//            if(startSquare == i.getStartSquare() && endSquare == i.getEndSquare()){
//                valid = true;
//                break;
//            }
//        }
//        if(!valid){
//            System.out.println("An error has occurred");
//            return;
//        }
        if(move.isCastle()){
            int targetSquare = startSquare + (2 * (int)Math.signum(endSquare-startSquare));
            int rookTargetSquare = targetSquare - ((int) Math.signum(endSquare - startSquare));

            board[targetSquare] = piece | 32;
            board[startSquare] = 0;
            board[rookTargetSquare] = board[endSquare] | 32;
            board[endSquare] = 0;
            this.pastPositions.clear();
        }
        if(move.isCapture())this.pastPositions.clear();
        if(move.isEnPassant()){
            board[endSquare - (8*color)] = 0;
            this.pastPositions.clear();
        }
        if((board[startSquare]&28) == 4)this.pastPositions.clear();
        if((piece&28) ==24 || (piece&28) == 16){
            piece |= 32;
        }
        if(move.isPromotion()){
            piece = move.getPromotion() | (piece & 3);
            this.pastPositions.clear();
        }
        for(int i = 0;i<board.length;i++){//remove phantom pawn
            if(board[i]<0){
                board[i] = 0;
            }
        }
        if((piece&28) == 4 && Math.abs(endSquare - startSquare) == 16){
            //add phantom pawn
            board[startSquare + (8*color)] = -(4|(piece&3));
            this.pastPositions.clear();
        }
        if(!move.isCastle())board[startSquare] = 0;
        if(!move.isCastle())board[endSquare] = piece;
        lastMoves.add(move);
        pastPositions.add(boardToFen());
        findPins();
    }

    public ArrayList<Move> findLegalMoves(int colorToMove){
        ArrayList<Move> legalMoves = new ArrayList<>();
        findLegalMovesCount++;
        for(int i=0;i<board.length;i++){
            //Loops through every square on the board
            int squareValue = board[i];
            //Checks if square contains a piece
            if(squareValue == 0)continue;
            if(squareValue < 0){
                continue;
            }
            int color = ((squareValue & 3) == 1)?1:-1;//11000 = 3
            if(color != colorToMove)continue;

            int piece = squareValue & 28;//00111 = 28
            switch(piece){
                case 4://00100 = Pawn
                    legalMoves.addAll(generatePawnMoves(i,squareValue));
                    break;
                case 8://00010 = Bishop
                    legalMoves.addAll(generateSlidingMoves(i, squareValue));
                    break;
                case 12://00110 = Knight
                    legalMoves.addAll(generateKnightMoves(i, squareValue));
                    break;
                case 16://00001 = Rook
                    legalMoves.addAll(generateSlidingMoves(i, squareValue));
                    break;
                case 20://00101 = Queen
                    legalMoves.addAll(generateSlidingMoves(i, squareValue));
                    break;
                case 24://00011 = King
                    legalMoves.addAll(generateKingMoves(i,squareValue));
                    break;
            }

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
//            Board checkTestBoardA = new Board(this,a);
//            Board checkTestBoardB = new Board(this,b);

            //int kingA = checkTestBoardA.getKingPosition(((board[a.getStartSquare()] & 3) == 1)?2:1);
            //int kingB = checkTestBoardA.getKingPosition(((board[b.getStartSquare()] & 3) == 1)?2:1);

            //if((generateAttackedPositions(checkTestBoardA, colorToMove*-1) & (1L << kingA)) != 0)valA += 75;
            //if((generateAttackedPositions(checkTestBoardB, colorToMove*-1) & (1L << kingB)) != 0)valB += 75;

            return valB - valA;
        });
        return legalMoves;
    }
    public ArrayList<Move> generatePawnMoves(int square, int piece){
        ArrayList<Move> moves = new ArrayList<>();

        int file = square % 8;
        int rank = square / 8;
        int color = ((piece & 3) == 1)?1:-1;
        if(checks[(color == 1) ? 0 : 1].size() >=2)return moves;

        int pinDir = 0;
        boolean canMoveForward = true;
        boolean canDoubleMoveForward = true;
        boolean canCaptureRight = true;
        boolean canCaptureLeft = true;
        boolean canPromote = true;
        boolean canEnPassantLeft = true;
        boolean canEnPassantRight = true;


        for(Pin pin:pinnedPieces[(PieceHelper.getColor(piece) == 1)?0:1]){
            if(pin.pieceSquare == square) {
                pinDir = pin.movableDirections[0] + pin.movableDirections[1];
                if(pin.enPassantEdgeCase){
                    canEnPassantLeft = false;
                    canEnPassantRight = false;
                }
            }
        }

        if(canEnPassantLeft){
            switch(pinDir){//handle pin
                case 1:
                    canCaptureLeft = false;
                    canCaptureRight = false;
                    break;
                case 5:
                    canCaptureLeft = false;
                    canCaptureRight = false;
                    canMoveForward = false;
                    canDoubleMoveForward = false;
                    break;
                case 9:
                    canMoveForward = false;
                    canDoubleMoveForward = false;
                    if(color == PieceHelper.WHITE){
                        canCaptureRight = false;
                    }else{
                        canCaptureLeft = false;
                    }
                    break;
                case 13:
                    canMoveForward = false;
                    canDoubleMoveForward = false;
                    if(color == PieceHelper.WHITE){
                        canCaptureLeft = false;
                    }else{
                        canCaptureRight = false;
                    }
                    break;
            }
        }
        if(file == 0)canCaptureLeft = false;
        if(file == 7)canCaptureRight = false;
        if(color == PieceHelper.WHITE){
            if(rank != 1 || board[square+16] > 0)canDoubleMoveForward = false;
            if(board[square+8] > 0){
                canMoveForward = false;
                canDoubleMoveForward = false;
            }
            if(rank != 6)canPromote = false;
            if(canCaptureLeft && PieceHelper.getColor(Math.abs(board[square+7])) != PieceHelper.BLACK)canCaptureLeft = false;
            if(canCaptureRight && PieceHelper.getColor(Math.abs(board[square+9])) != PieceHelper.BLACK)canCaptureRight = false;

            if(!canCaptureLeft || board[square+7]>=0)canEnPassantLeft = false;
            if(!canCaptureRight || board[square+9]>=0)canEnPassantRight = false;

            if(canMoveForward && !checks[0].isEmpty()){
                canMoveForward = false;
                for(int i: checks[0].getFirst()){
                    if(square+8 == i){
                        canMoveForward = true;
                        break;
                    }
                }
            }
            if(canDoubleMoveForward && !checks[0].isEmpty()){
                canDoubleMoveForward = false;
                for(int i: checks[0].getFirst()){
                    if(square+16 == i){
                        canDoubleMoveForward = true;
                        break;
                    }
                }
            }
            if(canCaptureRight && !checks[0].isEmpty()){
                canCaptureRight = false;
                for(int i: checks[0].getFirst()){
                    if(square+9 == i){
                        canCaptureRight = true;
                        break;
                    }
                }
            }
            if(canCaptureLeft && !checks[0].isEmpty()){
                canCaptureLeft = false;
                for(int i: checks[0].getFirst()){
                    if(square+7 == i){
                        canCaptureLeft = true;
                        break;
                    }
                }
            }
        }else{
            if(rank != 6 || board[square-16] > 0)canDoubleMoveForward = false;
            if(board[square-8] > 0){
                canMoveForward = false;
                canDoubleMoveForward = false;
            }
            if(rank != 1)canPromote = false;
            if(canCaptureLeft && PieceHelper.getColor(Math.abs(board[square-9])) != PieceHelper.WHITE)canCaptureLeft = false;
            if(canCaptureRight && PieceHelper.getColor(Math.abs(board[square-7])) != PieceHelper.WHITE)canCaptureRight = false;

            if(!canCaptureLeft || board[square-9]>=0)canEnPassantLeft = false;
            if(!canCaptureRight || board[square-7]>=0)canEnPassantRight = false;
            if(canMoveForward && !checks[1].isEmpty()){
                canMoveForward = false;
                for(int i: checks[1].getFirst()){
                    if(square-8 == i){
                        canMoveForward = true;
                        break;
                    }
                }
            }
            if(canDoubleMoveForward && !checks[1].isEmpty()){
                canDoubleMoveForward = false;
                for(int i: checks[1].getFirst()){
                    if(square-16 == i){
                        canDoubleMoveForward = true;
                        break;
                    }
                }
            }
            if(canCaptureRight && !checks[1].isEmpty()){
                canCaptureRight = false;
                for(int i: checks[1].getFirst()){
                    if(square-7 == i){
                        canCaptureRight = true;
                        break;
                    }
                }
            }
            if(canCaptureLeft && !checks[1].isEmpty()){
                canCaptureLeft = false;
                for(int i: checks[1].getFirst()){
                    if(square-9 == i){
                        canCaptureLeft = true;
                        break;
                    }
                }
            }
        }

        if(canMoveForward){
            Move move = new Move(square,square+(8*color),board);

            if(canPromote){
                moves.add(new Move(square,square+(8*color),8,board));
                moves.add(new Move(square,square+(8*color),12,board));
                moves.add(new Move(square,square+(8*color),16,board));
                moves.add(new Move(square,square+(8*color),20,board));
            }else moves.add(move);
        }
        if(canDoubleMoveForward){
            Move move = new Move(square,square+16*color);
            moves.add(move);
        }
        //capture and en-passant
        //capture left
        if(canCaptureLeft){
            Move move = new Move(square,square+((color == 1)?7:-9),canEnPassantLeft);
            if(canPromote){
                moves.add(new Move(square,square+((color == 1)?7:-9),8,board));
                moves.add(new Move(square,square+((color == 1)?7:-9),12,board));
                moves.add(new Move(square,square+((color == 1)?7:-9),16,board));
                moves.add(new Move(square,square+((color == 1)?7:-9),20,board));
            }else moves.add(move);
        }
        //capture right
        if(canCaptureRight){
            Move move = new Move(square,square+((color == 1)?9:-7),canEnPassantRight);
            if((rank == ((color == 1)?6:1))){
                moves.add(new Move(square,square+((color == 1)?9:-7),8,board));
                moves.add(new Move(square,square+((color == 1)?9:-7),12,board));
                moves.add(new Move(square,square+((color == 1)?9:-7),16,board));
                moves.add(new Move(square,square+((color == 1)?9:-7),20,board));
            }else moves.add(move);
        }
        return moves;
    }
    public long generateAttackedPositions(int colorToMove){
        long bitmap = 0;
        generateAttackedPositionsCount++;
        for(int i=0;i<board.length;i++){
            //Loops through every square on the board
            int squareValue = board[i];
            //Checks if square contains a piece
            if(squareValue <= 0)continue;

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
                    bitmap = bitmap | generateSlidingMoveBitmap(i, squareValue);
                    break;
                case 12://00110 = Knight
                    bitmap = bitmap | generateKnightMovesBitmap(i);
                    break;
                case 16://00001 = Rook
                    bitmap = bitmap | generateSlidingMoveBitmap(i, squareValue);
                    break;
                case 20://00101 = Queen
                    bitmap = bitmap | generateSlidingMoveBitmap(i, squareValue);
                    break;
                case 24://00111 = King
                    bitmap = bitmap | generateSlidingMoveBitmap(i, squareValue);
                    break;
            }

        }
        return bitmap;
    }
    private ArrayList<Move> generateKingMoves(int square, int piece){
        ArrayList<Move> moves = new ArrayList<>();
        long checkMap = this.generateAttackedPositions(((piece & 3) == 1)?1:-1);
        for(int dir = 0; dir < 8; dir++){
            if(squaresToEdge[square][dir] == 0)continue;
            int target = square + directionalOffsets[dir];
            int targetPiece = board[target];
            if(targetPiece < 0)targetPiece = 0;
            if((targetPiece & 3) == (piece & 3))continue;

            long kingMap = 1L << target;
            if((kingMap & checkMap) != 0)continue;

            moves.add(new Move(square,target,board));

        }

        if((piece & 32) == 0){
            int rookRight = ((square+3) < 64)?board[square + 3]:0;
            int rookLeft = ((square-4) >= 0)?board[square - 4]:0;
            long rightCheck = 7L << (((piece & 3)==1)?4:60);
            long leftCheck = 7L << (((piece & 3)==1)?2:58);
            //right castle
            if(
                (rookRight != 0) &&
                (rookRight & 28) == 16 &&
                (rookRight & 32) == 0 &&
                (checkMap & rightCheck) == 0 &&
                board[square+1] == 0 &&
                board[square+2] == 0
            ){
                moves.add(new Move(true,square,square+3));
            }
            //left castle
            if(
                (rookRight != 0) &&
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
    private ArrayList<Move> generateSlidingMoves(int square, int piece){
        ArrayList<Move> moves = new ArrayList<>();
        var coloredChecks = checks[(PieceHelper.getColor(piece) == 1)?0:1];
        if(coloredChecks.size() >= 2)return moves;
        int startDir = ((piece & 28) == 8)?4:0;
        int endDir = ((piece & 28) == 16)?4:8;
        int[] pinDir = {-1,-1};
        for(Pin pin:pinnedPieces[(PieceHelper.getColor(piece) == 1)?0:1]){
            if(pin.pieceSquare == square)pinDir = pin.movableDirections;
        }
        for(int dir = startDir; dir < endDir; dir++){
            if(pinDir[0] != -1 && pinDir[0] != dir && pinDir[1] != dir)continue;
            for(int dist = 0;dist < squaresToEdge[square][dir];dist++){
                int target  = square + directionalOffsets[dir] * (dist+1);
                int targetPiece = board[target];
                boolean canBePlayed = true;
                if(!coloredChecks.isEmpty()){
                    canBePlayed = false;
                    for(int i:coloredChecks.getFirst()){
                        if(i == target){
                            canBePlayed = true;
                            break;
                        }
                    }

                }
                if(targetPiece < 0)targetPiece = 0;

                int targetPieceColor = targetPiece & 3;
                if(targetPieceColor == (piece & 3))break;//break if friendly

                Move move = new Move(square,target,board);
                if(canBePlayed)moves.add(move);
                if((targetPiece & 28) > 0)break;//Stop on capture
            }
        }
        return moves;
    }
    private long generateSlidingMoveBitmap(int square,int piece){
        long map = 0b0L;
        int startDir = ((piece & 28) == 8)?4:0;
        int endDir = ((piece & 28) == 16)?4:8;

        for(int dir = startDir; dir < endDir; dir++){
            for(int dist = 0;dist < squaresToEdge[square][dir];dist++){
                if((piece & 28)==24 && dist > 0)break;
                int target  = square + directionalOffsets[dir] * (dist+1);
                int targetPiece = board[target];
                if(targetPiece <= 0)targetPiece = 0;
                int targetPieceColor = targetPiece & 3;
                map = map | 1L << target;
                if(targetPieceColor == (piece & 3))break;
                if((targetPiece & 28) > 0 && PieceHelper.getType(targetPiece)!=24)break;
            }
        }
        return map;
    }
    private ArrayList<Move> generateKnightMoves(int square, int piece){
        ArrayList<Move> moves = new ArrayList<>();
        var coloredChecks = checks[(PieceHelper.getColor(piece) == 1)?0:1];
        if(coloredChecks.size() >= 2)return moves;
        for(Pin pin:pinnedPieces[(PieceHelper.getColor(piece) == 1)?0:1]){
            if(pin.pieceSquare == square)return moves;
        }
        for(int dir = 0;dir<8;dir++){
            if(!knightEdgeCheck[square][dir])continue;
            int target = square + knightOffsets[dir];
            if(!coloredChecks.isEmpty()){
                boolean canBePlayed = false;
                for(int i:coloredChecks.getFirst()){
                    if(i == target){
                        canBePlayed = true;
                        break;
                    }
                }
                if(!canBePlayed)continue;
            }
            int targetPiece = board[target];
            if(targetPiece < 0)targetPiece = 0;
            int targetPieceColor = targetPiece & 3;
            if(targetPieceColor == (piece & 3))continue;

            Move move = new Move(square,target,board);
            moves.add(move);
        }
        return moves;
    }
    public int getKingPosition(int color){
        for(int i = 0;i<board.length;i++){
            if(board[i] == (24 | color)||board[i] == (56 | color)){
                return i;
            }
        }
        return -1;
    }
    private long generateKnightMovesBitmap(int square){
        long map = 0b0L;
        for(int dir = 0;dir<8;dir++){
            if(!knightEdgeCheck[square][dir])continue;
            int target = square + knightOffsets[dir];
            map = map | 1L << target;
        }
        return map;
    }
}