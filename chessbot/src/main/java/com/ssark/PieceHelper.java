package com.ssark;

public class PieceHelper {
    public static int getType(int piece){
        return (piece & 28);
    }
    public static String getString(int piece){
        return switch (getType(piece)) {
            case 4 -> "p";
            case 8 -> "b";
            case 12 -> "n";
            case 16 -> "r";
            case 20 -> "q";
            case 24 -> "k";
            default -> "invalid piece id:"+getType(piece);
        };
    }
    public static String getString(int piece,boolean capitalize){
        if(getColor(piece) == 1){
            return switch (getType(piece)) {
                case 4 -> "p";
                case 8 -> "b";
                case 12 -> "n";
                case 16 -> "r";
                case 20 -> "q";
                case 24 -> "k";
                default -> "invalid piece id:"+getType(piece);
            };
        }
        else{
            return switch (getType(piece)) {
                case 4 -> "P";
                case 8 -> "B";
                case 12 -> "N";
                case 16 -> "R";
                case 20 -> "Q";
                case 24 -> "K";
                default -> "invalid piece id:"+getType(piece);
            };
        }
    }
    public static final int WHITE = 1;
    public static final int BLACK = -1;
    public static int getColor(int piece){
        return ((piece & 3)==1)?1:-1;
    }
    public static boolean canCastle(int piece){
        return (piece & 32) == 0;
    }

}
