package com.ssark;

public class PieceHelper {
    public static int getType(int piece){
        return (piece & 28);
    }
    public static String getString(int piece){
        switch(getType(piece)){
            case 4:
                return "p";
            case 8:
                return "b";
            case 12:
                return "n";
            case 16:
                return "r";
            case 20:
                return "q";
            case 24:
                return "k";
            default:
                return "invalid piece id:"+getType(piece);
        }
    }
    public static String getString(int piece,boolean capitalize){
        if(getColor(piece) == 1){
            switch(getType(piece)){
                case 4:
                    return "p";
                case 8:
                    return "b";
                case 12:
                    return "n";
                case 16:
                    return "r";
                case 20:
                    return "q";
                case 24:
                    return "k";
                default:
                    return "invalid piece id:"+getType(piece);
            }
        }
        else{
            switch(getType(piece)){
                case 4:
                    return "P";
                case 8:
                    return "B";
                case 12:
                    return "N";
                case 16:
                    return "R";
                case 20:
                    return "Q";
                case 24:
                    return "K";
                default:
                    return "invalid piece id:"+getType(piece);
            }
        }
    }
    public static int getColor(int piece){
        return ((piece & 3)==2)?1:-1;
    }
    public static boolean canCastle(int piece){
        return (piece & 32) == 0;
    }

}
