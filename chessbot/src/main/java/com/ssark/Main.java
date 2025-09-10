package com.ssark;
/**
 * Main class of the Java program.
 */
import java.util.Arrays;
public class Main {
    
    public static void main(String[] args) {
        createBoard_TEST();
        
    }
    private static void createBoard_TEST(){
        BoardHelper bh = new BoardHelper();
        boolean[] out = new boolean[2];
        //perform test
        int[] newBoard = bh.createBoard();
        newBoard[0] = 5; //set the first square to a white pawn
        int[] copyBoard = bh.createBoard(newBoard);
        out[0] = Arrays.equals(newBoard,copyBoard); //test0
        
        copyBoard[1] = 6;
        out[1] = !Arrays.equals(newBoard,copyBoard);//test1
        
        System.out.println("createBoard test0 " + ((out[0])?"Passed":"Failed"));
        System.out.println("createBoard test1 " + ((out[1])?"Passed":"Failed"));
        
    }
}
